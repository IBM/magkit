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
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.removeEnd;

/**
 * Helper / provider that converts a Vaadin {@link Table} (legacy v7 component) into a downloadable Excel workbook.
 * <p>
 * It exposes a lightweight {@link ConnectorResource} whose {@link DownloadStream} is created lazily when a user
 * triggers the download (e.g. by clicking a link or button bound to the resource). The workbook is generated in
 * memory, flushed to a temporary file (due to POI streaming constraints), then streamed back via an {@link InputStream}.
 * </p>
 * <p>
 * Key features:
 * <ul>
 *   <li>Supports exporting only currently visible columns (respects user column visibility).</li>
 *   <li>Adds a title row and header row with bold styling, merges the title across all visible columns.</li>
 *   <li>Auto-calculates column widths based on encountered cell values (bounded by Excel limit 255 characters).</li>
 *   <li>Detects {@link Link} values and converts them into Excel hyperlinks (external URL or file path).</li>
 *   <li>Generates safe sheet and file names by removing unsupported characters and normalizing spaces.</li>
 *   <li>Removes configured Magnolia context path portion from the server base URL for correct public links.</li>
 * </ul>
 * </p>
 * <p>
 * Usage preconditions: Provide a non-null {@link Table} with initialized visible columns. The table should already be
 * populated; dynamic changes after constructing this provider are not reflected unless a new instance is created.
 * </p>
 * <p>
 * Side effects: Creates a temporary file inside Magnolia's temp directory for each download request which is not
 * explicitly deleted here (left to OS temp cleanup). Keeps workbook only for the duration of stream creation.
 * </p>
 * <p>
 * Null & error handling: Methods validate mandatory arguments with {@link IllegalArgumentException}. IO failures during
 * file creation / writing are logged and result in a {@code null} stream which Vaadin will treat as an empty download.
 * </p>
 * <p>
 * Thread-safety: Not thread-safe. Instances should be used within the UI thread. No shared mutable static state except
 * constants. A single instance should not be accessed concurrently by multiple threads.
 * </p>
 *
 * @author wolf.bubenik
 * @since 2018-01-26
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

    /**
     * Construct a provider for exporting the supplied table.
     *
     * @param source the table to export (must not be {@code null})
     * @param fileNameBase base name used for sheet and file (invalid characters and colons removed)
     * @param title title placed into the first (merged) row; may be empty but not {@code null}
     * @throws IllegalArgumentException if {@code source} is {@code null}
     */
    public TableAsExcelResourceProvider(final Table source, final String fileNameBase, final String title) {
        checkArgument(source != null, "The Table source must not be null.");
        _source = source;
        _visibleColumns = new HashSet<>(Arrays.asList(_source.getVisibleColumns()));
        _sheetName = fileNameBase.replaceAll("[:,]*", EMPTY).replaceAll("/", "-");
        _fileName = _sheetName.replace(' ', '_') + EXCEL_FILE_EXTENSION;
        _title = title;
    }

    /**
     * Create a Vaadin resource representing the Excel file download.
     *
     * @return connector resource ready to be attached to a {@link Link} or button
     */
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

    /**
     * Serialize the workbook into a temporary file and return an input stream for download.
     * Ensures proper closing of the {@link Workbook} to release underlying OPCPackage resources (required for POI >= 5.x).
     *
     * @param wb the workbook to serialize (must not be {@code null})
     * @return input stream positioned at the beginning of the serialized workbook or {@code null} on IO error
     */
    InputStream toInputStream(Workbook wb) {
        checkArgument(wb != null, "The Workbook must not be null.");
        FileSystemHelper fileSystemHelper = getComponent(FileSystemHelper.class);
        File file = getTempFile(fileSystemHelper.getTempDirectory());
        InputStream result = null;
        if (file != null) {
            boolean success = writeWorkbookToFile(wb, file);
            if (success) {
                try {
                    result = new FileInputStream(file);
                } catch (IOException e) {
                    LOGGER.error("Error opening input stream for excel workbook.", e);
                }
            }
        }
        return result;
    }

    File getTempFile(File tempDir) {
        File file = null;
        try {
            file = File.createTempFile(_sheetName, EXCEL_FILE_EXTENSION, tempDir);
        } catch (IOException e) {
            LOGGER.error("Error creating temporary excel file.", e);
        }
        return file;
    }

    boolean writeWorkbookToFile(Workbook wb, File file) {
        boolean success = false;
        try {
            OutputStream out = new FileOutputStream(file);
            wb.write(out);
            out.flush();
            success = true;
        } catch (IOException e) {
            LOGGER.error("Error writing excel workbook to file.", e);
        } finally {
            try {
                wb.close();
            } catch (IOException e) {
                LOGGER.warn("Error closing workbook after serialization.", e);
            }
        }
        return success;
    }

    /**
     * Build the workbook in memory including header and all visible rows.
     *
     * @return populated workbook (never {@code null})
     */
    Workbook createExcel() {
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

    /**
     * Create header cell style (bold font) used for both title and column headers.
     *
     * @param wb workbook (must not be {@code null})
     * @return header style
     */
    CellStyle getHeaderStyle(final Workbook wb) {
        CellStyle headerStyle = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        headerStyle.setFont(font);
        return headerStyle;
    }

    /**
     * Render all visible rows (after the header) including link formatting and column width detection.
     *
     * @param sheet target sheet (must not be {@code null})
     */
    void renderSheet(final Sheet sheet) {
        int rowCount = HEADER_ROW_NUMBER;
        Collection<?> itemIds = _source.getVisibleItemIds();
        CellStyle linkStyle = getLinkStyle(sheet.getWorkbook());
        int[] maxColumnWidth = new int[_visibleColumns.size()];
        for (Object itemId : itemIds) {
            Row row = sheet.createRow(++rowCount);
            Item item = _source.getItem(itemId);
            Collection<?> propertyIds = item.getItemPropertyIds();
            int cellId = 0;
            for (Object id : propertyIds) {
                if (_visibleColumns.contains(id)) {
                    Cell cell = row.createCell(cellId++);
                    Property<?> property = item.getItemProperty(id);
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

    /**
     * Add hyperlink metadata to a cell representing a Vaadin {@link Link}.
     *
     * @param value the link (must not be {@code null})
     * @param cell excel cell (must not be {@code null})
     * @param helper POI creation helper (must not be {@code null})
     */
    void addCellLink(Link value, Cell cell, CreationHelper helper) {
        checkArgument(cell != null, "The Cell must not be null.");
        checkArgument(helper != null, "The CreationHelper must not be null.");
        Hyperlink link = helper.createHyperlink(HyperlinkType.URL);
        link.setAddress(toUrl(value));
        cell.setHyperlink(link);
    }

    /**
     * Turn a Vaadin {@link Link} into a full URL (server base + target path) if possible.
     *
     * @param value link (must not be {@code null})
     * @return absolute URL or empty string if no resolvable target
     * @throws IllegalArgumentException if {@code value} is {@code null}
     */
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

    /**
     * Resolve configured base URL without Magnolia context path suffix.
     *
     * @return base URL suitable for concatenation with resource paths
     */
    String getBaseUrl() {
        // On our and migros systems, the defaultBaseUrl is configured with the context path.
        // This must be removed to ensure building of correct URLs.
        String result = getComponent(ServerConfiguration.class).getDefaultBaseUrl();
        return removeEnd(result, MgnlContext.getContextPath());
    }

    /**
     * Create style for hyperlink cells (blue, underlined).
     *
     * @param wb workbook (must not be {@code null})
     * @return cell style with link formatting
     */
    private CellStyle getLinkStyle(final Workbook wb) {
        checkArgument(wb != null, "The Workbook must not be null.");
        Font hlinkFont = wb.createFont();
        hlinkFont.setUnderline(Font.U_SINGLE);
        hlinkFont.setColor(IndexedColors.BLUE.getIndex());
        CellStyle hlinkStyle = wb.createCellStyle();
        hlinkStyle.setFont(hlinkFont);
        return hlinkStyle;
    }

    /**
     * Write a value into a cell (converting components to their caption) and return its trimmed string form.
     *
     * @param value any object or Vaadin component (may be {@code null})
     * @param cell excel cell (must not be {@code null})
     * @return trimmed cell value string (never {@code null})
     * @throws IllegalArgumentException if {@code cell} is {@code null}
     */
    String addCellString(Object value, Cell cell) {
        checkArgument(cell != null, "The Cell must not be null.");
        String valueString = value instanceof AbstractComponent ? ((AbstractComponent) value).getCaption() : value != null ? value.toString() : EMPTY;
        valueString = valueString == null ? EMPTY : valueString.trim();
        cell.setCellValue(valueString);
        return valueString;
    }

    /**
     * Apply calculated column widths to the sheet (bounded by Excel limit) expressed in character fractions.
     *
     * @param sheet sheet (must not be {@code null})
     * @param maxColumnWidth array with max characters per column (may be empty)
     */
    private void setColumnWidth(Sheet sheet, int[] maxColumnWidth) {
        checkArgument(sheet != null, "The Sheet must not be null.");
        if (ArrayUtils.isNotEmpty(maxColumnWidth)) {
            for (int index = 0; index < maxColumnWidth.length; index++) {
                int width = Math.min(maxColumnWidth[index], MAX_CHARS_PER_CELL);
                sheet.setColumnWidth(index, width * CHAR_WIDTH_FRACTION);
            }
        }
    }

    /**
     * Update maximum width tracking for a column.
     *
     * @param maxColumnWidth tracking array (must not be {@code null})
     * @param value cell value string (may be {@code null})
     * @param index column index (must be within bounds)
     */
    void updateColumnWidth(int[] maxColumnWidth, String value, int index) {
        if (value != null && maxColumnWidth[index] < value.length()) {
            maxColumnWidth[index] = value.length();
        }
    }

    /**
     * Render title row and column headers. Assumes at least one visible item exists to derive header ordering.
     * Safely skips merging if there are no visible columns.
     *
     * @param sheet target sheet (must not be {@code null})
     * @param headerStyle style to apply to header cells (must not be {@code null})
     */
    void renderHeader(final Sheet sheet, final CellStyle headerStyle) {
        checkArgument(sheet != null, "The Sheet must not be null.");
        Row contextRow = sheet.createRow(TITLE_ROW_NUMBER);
        contextRow.setHeight((short) 600);
        final Cell title = contextRow.createCell(0);
        title.setCellValue(_title);
        title.setCellStyle(getTitleStyle(sheet.getWorkbook()));
        if (!_visibleColumns.isEmpty()) {
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, _visibleColumns.size() - 1));
        }
        Collection<?> itemIds = _source.getVisibleItemIds();
        if (!itemIds.isEmpty()) {
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
    }

    /**
     * Derive the style for the title row (based on header style plus vertical centering).
     *
     * @param wb workbook (must not be {@code null})
     * @return derived title style
     */
    private CellStyle getTitleStyle(final Workbook wb) {
        CellStyle style = getHeaderStyle(wb);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }
}
