package de.ibmix.magkit.ui.apps.ui.download;

/*-
 * #%L
 * IBM iX Magnolia Kit
 * %%
 * Copyright (C) 2023 IBM iX
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.vaadin.server.ConnectorResource;
import com.vaadin.server.DownloadStream;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.FileResource;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Link;
import com.vaadin.v7.data.Item;
import com.vaadin.v7.data.Property;
import com.vaadin.v7.ui.Table;
import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.cms.core.FileSystemHelper;
import info.magnolia.context.MgnlContext;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.PrintSetup;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static com.google.common.base.Preconditions.checkArgument;
import static info.magnolia.objectfactory.Components.getComponent;
import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.removeEnd;

/**
 * A ResourceProvider that provides the Table content as DownloadStream.
 *
 * @author wolf.bubenik
 * @since 26.01.18
 */
public class TableAsExcelResourceProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(TableAsExcelResourceProvider.class);

    private static final int TITLE_ROW_NUMBER = 0;
    private static final int HEADER_ROW_NUMBER = 1;
    private static final int MAX_CHARS_PER_CELL = 255;
    private static final int CHAR_WIDTH_FRACTION = 256;
    private static final String EXCEL_FILE_EXTENSION = ".xlsx";

    private final Table _source;
    private final String _fileName;
    private final String _sheetName;
    private final Set<Object> _visibleColumns;
    private final String _title;

    public TableAsExcelResourceProvider(final Table source, final String fileNameBase, final String title) {
        _source = source;
        _visibleColumns = new HashSet<>(Arrays.asList(_source.getVisibleColumns()));
        _sheetName = fileNameBase.replaceAll("[:,]*", EMPTY).replaceAll("/", "-");
        _fileName = _sheetName.replace(' ', '_') + EXCEL_FILE_EXTENSION;
        _title = title;
    }

    public ConnectorResource getResource() {
        return new ConnectorResource() {
            @Override
            public DownloadStream getStream() {
                return new DownloadStream(toInputStream(createExcel()), "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", _fileName);
            }

            @Override
            public String getFilename() {
                return _fileName;
            }

            @Override
            public String getMIMEType() {
                return "application/vnd.ms-excel";
            }
        };
    }

    private InputStream toInputStream(Workbook wb) {
        FileSystemHelper fileSystemHelper = getComponent(FileSystemHelper.class);
        InputStream in = null;
        OutputStream out = null;
        try {
            File file = File.createTempFile(_sheetName, EXCEL_FILE_EXTENSION, fileSystemHelper.getTempDirectory());
            out = new FileOutputStream(file);
            wb.write(out);
            in = new FileInputStream(file);
        } catch (IOException e) {
            LOGGER.error("Error writing excel workbook to file.", e);
            closeQuietly(out);
        }
        return in;
    }

    private Workbook createExcel() {
        Workbook wb = new XSSFWorkbook();
        CellStyle headerStyle = getHeaderStyle(wb);
        Sheet sheet = wb.createSheet(_sheetName);
        sheet.setFitToPage(true);
        sheet.setHorizontallyCenter(true);
        PrintSetup printSetup = sheet.getPrintSetup();
        printSetup.setLandscape(true);

        renderHeader(sheet, headerStyle);
        renderSheet(sheet);

        return wb;
    }

    CellStyle getHeaderStyle(final Workbook wb) {
        CellStyle headerStyle = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        headerStyle.setFont(font);
        return headerStyle;
    }

    void renderSheet(final Sheet sheet) {
        int rowCount = HEADER_ROW_NUMBER;
        Collection itemIds = _source.getVisibleItemIds();
        CellStyle linkStyle = getLinkStyle(sheet.getWorkbook());
        int[] maxColumnWidth = new int[_visibleColumns.size()];
        for (Object itemId : itemIds) {
            Row row = sheet.createRow(++rowCount);
            Item item = _source.getItem(itemId);
            Collection propertyIds = item.getItemPropertyIds();
            int cellId = 0;
            for (Object id : propertyIds) {
                if (_visibleColumns.contains(id)) {
                    Cell cell = row.createCell(cellId++);
                    Property property = item.getItemProperty(id);
                    Object value = property != null ? property.getValue() : EMPTY;
                    String valueString = addCellString(value, cell);
                    if (value instanceof Link) {
                        cell.setCellStyle(linkStyle);
                        addCellLink((Link) value, cell, sheet.getWorkbook().getCreationHelper());
                    }
                    updateColumnWidth(maxColumnWidth, valueString, cellId - 1);
                }
            }
        }
        setColumnWidth(sheet, maxColumnWidth);
    }

    private void addCellLink(Link value, Cell cell, CreationHelper helper) {
        checkArgument(cell != null, "The Cell must not be null.");
        checkArgument(helper != null, "The CreationHelper must not be null.");
        Hyperlink link = helper.createHyperlink(HyperlinkType.URL);
        link.setAddress(toUrl(value));
        cell.setHyperlink(link);
    }

    String toUrl(Link value) {
        checkArgument(value != null, "The Link must not be null.");
        String url = EMPTY;
        if (value.getResource() instanceof ExternalResource) {
            url = ((ExternalResource) value.getResource()).getURL();
        } else if (value.getResource() instanceof FileResource) {
            File source = ((FileResource) value.getResource()).getSourceFile();
            url = source != null ? source.getAbsolutePath() : EMPTY;
        }
        return isNotEmpty(url) ? getBaseUrl() + url : EMPTY;
    }

    String getBaseUrl() {
        // On our and migros systems, the defaultBaseUrl is configured with the context path.
        // This must be removed to ensure building of correct URLs.
        String result = getComponent(ServerConfiguration.class).getDefaultBaseUrl();
        return removeEnd(result, MgnlContext.getContextPath());
    }

    private CellStyle getLinkStyle(final Workbook wb) {
        checkArgument(wb != null, "The Workbook must not be null.");
        Font hlinkFont = wb.createFont();
        hlinkFont.setUnderline(Font.U_SINGLE);
        hlinkFont.setColor(IndexedColors.BLUE.getIndex());
        CellStyle hlinkStyle = wb.createCellStyle();
        hlinkStyle.setFont(hlinkFont);
        return hlinkStyle;
    }

    String addCellString(Object value, Cell cell) {
        checkArgument(cell != null, "The Cell must not be null.");
        String valueString = value instanceof AbstractComponent ? ((AbstractComponent) value).getCaption() : value != null ? value.toString() : EMPTY;
        valueString = valueString == null ? EMPTY : valueString.trim();
        cell.setCellValue(valueString);
        return valueString;
    }

    private void setColumnWidth(Sheet sheet, int[] maxColumnWidth) {
        checkArgument(sheet != null, "The Sheet must not be null.");
        if (ArrayUtils.isNotEmpty(maxColumnWidth)) {
            for (int index = 0; index < maxColumnWidth.length; index++) {
                int width = Math.min(maxColumnWidth[index], MAX_CHARS_PER_CELL);
                sheet.setColumnWidth(index, width * CHAR_WIDTH_FRACTION);
            }
        }
    }

    void updateColumnWidth(int[] maxColumnWidth, String value, int index) {
        if (value != null && maxColumnWidth[index] < value.length()) {
            maxColumnWidth[index] = value.length();
        }
    }

    void renderHeader(final Sheet sheet, final CellStyle headerStyle) {
        checkArgument(sheet != null, "The Sheet must not be null.");
        Row contextRow = sheet.createRow(TITLE_ROW_NUMBER);
        contextRow.setHeight((short) 600);
        final Cell title = contextRow.createCell(0);
        title.setCellValue(_title);
        title.setCellStyle(getTitleStyle(sheet.getWorkbook()));
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, _visibleColumns.size() - 1));

        // getColumnHeaders() may give Headers in wrong order :-(
        Collection itemIds = _source.getVisibleItemIds();
        Item item = _source.getItem(itemIds.iterator().next());
        Collection<?> propertyIds = item.getItemPropertyIds();
        int cellCount = 0;
        Row columnHeaders = sheet.createRow(HEADER_ROW_NUMBER);
        for (Object propertyId : propertyIds) {
            if (_visibleColumns.contains(propertyId)) {
                final Cell cell = columnHeaders.createCell(cellCount++);
                cell.setCellValue(_source.getColumnHeader(propertyId));
                cell.setCellStyle(headerStyle);
            }
        }
    }

    private CellStyle getTitleStyle(final Workbook wb) {
        CellStyle style = getHeaderStyle(wb);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }
}
