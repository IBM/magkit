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
import com.vaadin.server.ExternalResource;
import com.vaadin.server.FileResource;
import com.vaadin.server.DownloadStream;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Link;
import com.vaadin.v7.data.Item;
import com.vaadin.v7.data.Property;
import com.vaadin.v7.ui.Table;
import de.ibmix.magkit.test.cms.context.ComponentsMockUtils;
import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.cms.core.FileSystemHelper;
import info.magnolia.context.WebContext;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.jcr.RepositoryException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import static de.ibmix.magkit.test.cms.context.ContextMockUtils.cleanContext;
import static de.ibmix.magkit.test.cms.context.ContextMockUtils.mockWebContext;
import static de.ibmix.magkit.test.cms.context.ServerConfigurationMockUtils.mockServerConfiguration;
import static de.ibmix.magkit.test.cms.context.ServerConfigurationStubbingOperation.stubDefaultBaseUrl;
import static de.ibmix.magkit.test.cms.context.WebContextStubbingOperation.stubContextPath;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

/**
 * Unit tests for {@link TableAsExcelResourceProvider} export logic and helpers.
 *
 * @since 2018-01-31
 * @author wolf.bubenik
 */
public class TableAsExcelResourceProviderTest {

    private Table _source;
    private TableAsExcelResourceProvider _resourceProvider;

    @BeforeEach
    public void setUp() {
        _source = mock(Table.class);
        doReturn(new Object[]{"first", "second"}).when(_source).getVisibleColumns();
        _resourceProvider = new TableAsExcelResourceProvider(_source, "test:base file-name", "test title");
    }

    @AfterEach
    public void tearDown() {
        cleanContext();
    }

    @Test
    public void createExcel() {
        doReturn(Arrays.asList("row1")).when(_source).getVisibleItemIds();
        Item row1 = mock(Item.class);
        doReturn(row1).when(_source).getItem("row1");
        doReturn(Arrays.asList("first", "second")).when(row1).getItemPropertyIds();
        doReturn("Header 1").when(_source).getColumnHeader("first");
        doReturn("Header 2").when(_source).getColumnHeader("second");
        Workbook wb = _resourceProvider.createExcel();
        assertNotNull(wb);
        Sheet sheet = wb.getSheetAt(0);
        assertEquals("testbase file-name", sheet.getSheetName());
        Row titleRow = sheet.getRow(0);
        assertEquals("test title", titleRow.getCell(0).getStringCellValue());
        Cell headerCell = sheet.getRow(1).getCell(0);
        assertEquals("Header 1", headerCell.getStringCellValue());
        CellRangeAddress merged = sheet.getMergedRegion(0);
        assertEquals(0, merged.getFirstRow());
        assertEquals(0, merged.getLastRow());
        assertEquals(0, merged.getFirstColumn());
        assertEquals(1, merged.getLastColumn());
        CellStyle titleStyle = titleRow.getCell(0).getCellStyle();
        Font titleFont = wb.getFontAt(titleStyle.getFontIndex());
        // title font is bold
        assertTrue(titleFont.getBold());
        // title vertical alignment center
        assertEquals(VerticalAlignment.CENTER, titleStyle.getVerticalAlignment());
        // sheet settings landscape & centered
        assertTrue(sheet.getPrintSetup().getLandscape());
        assertTrue(sheet.getHorizontallyCenter());
    }

    @Test
    public void getHeaderStyle() {
        Workbook wb = new XSSFWorkbook();
        CellStyle style = _resourceProvider.getHeaderStyle(wb);
        assertNotNull(style);
        assertEquals(1, style.getFontIndex());
    }

    @Test
    public void renderSheet() throws RepositoryException {
        mockServerConfiguration(stubDefaultBaseUrl("https://example.org"));
        mockWebContext(stubContextPath(EMPTY));
        doReturn(Arrays.asList("row1")).when(_source).getVisibleItemIds();
        Item row1 = mock(Item.class);
        doReturn(row1).when(_source).getItem("row1");
        ExternalResource external = mock(ExternalResource.class);
        doReturn("/page.html").when(external).getURL();
        Link link = mock(Link.class);
        doReturn(external).when(link).getResource();
        doReturn("Link Caption").when(link).getCaption();
        Property linkProperty = mock(Property.class);
        doReturn(link).when(linkProperty).getValue();
        Property textProperty = mock(Property.class);
        doReturn("Some text value that is quite long").when(textProperty).getValue();
        doReturn(Arrays.asList("first", "second")).when(row1).getItemPropertyIds();
        doReturn(linkProperty).when(row1).getItemProperty("first");
        doReturn(textProperty).when(row1).getItemProperty("second");
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet("test sheet");
        _resourceProvider.renderSheet(sheet);
        Row dataRow = sheet.getRow(2);
        assertNotNull(dataRow);
        Cell linkCell = dataRow.getCell(0);
        Cell textCell = dataRow.getCell(1);
        assertEquals("Link Caption", linkCell.getStringCellValue());
        assertNotNull(linkCell.getHyperlink());
        assertEquals("https://example.org/page.html", linkCell.getHyperlink().getAddress());
        Font linkFont = wb.getFontAt(linkCell.getCellStyle().getFontIndex());
        assertEquals(Font.U_SINGLE, linkFont.getUnderline());
        // link font is not bold here
        assertFalse(linkFont.getBold());
        assertEquals("Some text value that is quite long", textCell.getStringCellValue());
    }

    @Test
    public void renderHeader() {
        doReturn(Arrays.asList("row1", "row2")).when(_source).getVisibleItemIds();
        Item row1 = mock(Item.class);
        doReturn(row1).when(_source).getItem("row1");
        doReturn(Arrays.asList("first", "invisible", "second")).when(row1).getItemPropertyIds();
        doReturn("Spalte 1").when(_source).getColumnHeader("first");
        doReturn("Spalte 2").when(_source).getColumnHeader("second");
        doReturn("hidden").when(_source).getColumnHeader("invisible");
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet("test");
        CellStyle style = _resourceProvider.getHeaderStyle(wb);
        _resourceProvider.renderHeader(sheet, style);
        assertNotNull(sheet.getRow(1));
        assertEquals(2, sheet.getRow(1).getLastCellNum());
        Cell first = sheet.getRow(1).getCell(0);
        Cell second = sheet.getRow(1).getCell(1);
        Cell hidden = sheet.getRow(1).getCell(2);
        assertEquals("Spalte 1", first.getStringCellValue());
        assertEquals("Spalte 2", second.getStringCellValue());
        assertNull(hidden);
    }

    @Test
    public void updateColumnWidthTest() {
        int[] colWidth = new int[]{0, 0, 0};
        _resourceProvider.updateColumnWidth(colWidth, null, 1);
        assertEquals(0, colWidth[0]);
        assertEquals(0, colWidth[1]);
        assertEquals(0, colWidth[2]);
        _resourceProvider.updateColumnWidth(colWidth, "test", 1);
        assertEquals(0, colWidth[0]);
        assertEquals(4, colWidth[1]);
        assertEquals(0, colWidth[2]);
        _resourceProvider.updateColumnWidth(colWidth, "test again", 1);
        assertEquals(0, colWidth[0]);
        assertEquals(10, colWidth[1]);
        assertEquals(0, colWidth[2]);
        _resourceProvider.updateColumnWidth(colWidth, "test again", 1);
        assertEquals(0, colWidth[0]);
        assertEquals(10, colWidth[1]);
        assertEquals(0, colWidth[2]);
        _resourceProvider.updateColumnWidth(colWidth, "test more", 1);
        assertEquals(0, colWidth[0]);
        assertEquals(10, colWidth[1]);
        assertEquals(0, colWidth[2]);
    }

    @Test
    public void addCellStringTestForNull() {
        assertThrows(IllegalArgumentException.class, () -> _resourceProvider.addCellString(null, null));
    }

    @Test
    public void addCellStringTest() {
        Workbook wb = new XSSFWorkbook();
        Row row = wb.createSheet().createRow(0);
        Cell cell = row.createCell(0);
        assertEquals(EMPTY, _resourceProvider.addCellString(null, cell));
        assertEquals("test", _resourceProvider.addCellString("test", cell));
        AbstractComponent value = mock(AbstractComponent.class);
        doReturn("  test caption  ").when(value).getCaption();
        assertEquals("test caption", _resourceProvider.addCellString(value, cell));
    }

    @Test
    public void toUrlTestForNull() {
        assertThrows(IllegalArgumentException.class, () -> _resourceProvider.toUrl(null));
    }

    @Test
    public void toUrlTest() throws RepositoryException {
        Link link = mock(Link.class);
        assertEquals(EMPTY, _resourceProvider.toUrl(link));
        mockServerConfiguration(stubDefaultBaseUrl("https://test.aperto.de"));
        mockWebContext();
        ExternalResource external = mock(ExternalResource.class);
        doReturn(external).when(link).getResource();
        assertEquals(EMPTY, _resourceProvider.toUrl(link));
        doReturn("/path/to/page.html").when(external).getURL();
        assertEquals("https://test.aperto.de/path/to/page.html", _resourceProvider.toUrl(link));
        FileResource fileResource = mock(FileResource.class);
        doReturn(fileResource).when(link).getResource();
        assertEquals(EMPTY, _resourceProvider.toUrl(link));
        File file = mock(File.class);
        doReturn(file).when(fileResource).getSourceFile();
        assertEquals(EMPTY, _resourceProvider.toUrl(link));
        doReturn("/path/to/file.pdf").when(file).getAbsolutePath();
        assertEquals("https://test.aperto.de/path/to/file.pdf", _resourceProvider.toUrl(link));
    }

    @Test
    public void getBaseUrlTest() throws RepositoryException {
        ServerConfiguration serverConfig = mockServerConfiguration(stubDefaultBaseUrl("https://test.aperto.de"));
        WebContext ctx = mockWebContext(stubContextPath(EMPTY));
        assertEquals("https://test.aperto.de", _resourceProvider.getBaseUrl());
        stubDefaultBaseUrl("https://test.aperto.de/contextPath").of(serverConfig);
        assertEquals("https://test.aperto.de/contextPath", _resourceProvider.getBaseUrl());
        stubContextPath("/author").of(ctx);
        assertEquals("https://test.aperto.de/contextPath", _resourceProvider.getBaseUrl());
        stubContextPath("/contextPath").of(ctx);
        assertEquals("https://test.aperto.de", _resourceProvider.getBaseUrl());
    }

    @Test
    public void renderSheetTest() {
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet("test sheet");
        Item[] rows = mockVisibleRowItems(Arrays.asList("1", "2", "3"));
        _resourceProvider.renderSheet(sheet);
        assertEquals(0, sheet.getColumnWidth(0));
        assertEquals(0, sheet.getColumnWidth(1));
        assertNotNull(sheet.getRow(2));
        assertNotNull(sheet.getRow(3));
        assertNotNull(sheet.getRow(4));
        mockProperties(rows, new String[]{"first value", "invisible value", "second value"}, new String[]{"first", "invisible", "second"});
        _resourceProvider.renderSheet(sheet);
        assertEquals("first value", sheet.getRow(2).getCell(0).getStringCellValue());
        assertEquals("second value", sheet.getRow(2).getCell(1).getStringCellValue());
        assertEquals(2, sheet.getRow(2).getLastCellNum());
        assertEquals("first value", sheet.getRow(3).getCell(0).getStringCellValue());
        assertEquals("second value", sheet.getRow(3).getCell(1).getStringCellValue());
        assertEquals(2, sheet.getRow(3).getLastCellNum());
        assertEquals("first value", sheet.getRow(4).getCell(0).getStringCellValue());
        assertEquals("second value", sheet.getRow(4).getCell(1).getStringCellValue());
        assertEquals(2, sheet.getRow(4).getLastCellNum());
        assertEquals(11 * 256, sheet.getColumnWidth(0));
        assertEquals(12 * 256, sheet.getColumnWidth(1));
    }

    @Test
    public void getResourceTest() {
        ComponentsMockUtils.mockComponentInstance(FileSystemHelper.class);
        ConnectorResource resource = _resourceProvider.getResource();
        assertEquals("testbase_file-name.xlsx", resource.getFilename());
        assertEquals("application/vnd.ms-excel", resource.getMIMEType());
        DownloadStream stream = resource.getStream();
        assertNotNull(stream);
        InputStream in = stream.getStream();
        assertNotNull(in);
        try (Workbook wb = new XSSFWorkbook(in)) {
            Sheet sheet = wb.getSheetAt(0);
            assertEquals("testbase file-name", sheet.getSheetName());
            Row titleRow = sheet.getRow(0);
            assertNotNull(titleRow);
            assertEquals("test title", titleRow.getCell(0).getStringCellValue());
        } catch (Exception e) {
            // should not throw
            assertNull(e);
        }
    }

    @Test
    public void widthCapTest() {
        doReturn(Arrays.asList("row1")).when(_source).getVisibleItemIds();
        Item row1 = mock(Item.class);
        doReturn(row1).when(_source).getItem("row1");
        String longValue = "x".repeat(300);
        Property longProp = mock(Property.class);
        doReturn(longValue).when(longProp).getValue();
        doReturn(Arrays.asList("first")).when(row1).getItemPropertyIds();
        doReturn(longProp).when(row1).getItemProperty("first");
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet("cap");
        _resourceProvider.renderSheet(sheet);
        // Excel cap 255 chars * 256 fraction
        assertEquals(255 * 256, sheet.getColumnWidth(0));
    }

    @Test
    public void addCellLinkNullArgumentTest() {
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet("link");
        Link link = mock(Link.class);
        // Expect exception for null cell
        assertThrows(IllegalArgumentException.class, () -> _resourceProvider.addCellLink(link, null, wb.getCreationHelper()));
        // Expect exception for null helper
        Row row = sheet.createRow(0);
        Cell cell = row.createCell(0);
        assertThrows(IllegalArgumentException.class, () -> _resourceProvider.addCellLink(link, cell, null));
    }

    @Test
    public void addCellLinkFileResourceTest() throws RepositoryException {
        mockServerConfiguration(stubDefaultBaseUrl("https://host/base/context"));
        mockWebContext(stubContextPath("/context"));
        doReturn(Arrays.asList("row1")).when(_source).getVisibleItemIds();
        Item row1 = mock(Item.class);
        doReturn(row1).when(_source).getItem("row1");
        FileResource fileResource = mock(FileResource.class);
        File file = mock(File.class);
        doReturn("/doc/file.pdf").when(file).getAbsolutePath();
        doReturn(file).when(fileResource).getSourceFile();
        Link link = mock(Link.class);
        doReturn(fileResource).when(link).getResource();
        doReturn("FileCaption").when(link).getCaption();
        Property p = mock(Property.class);
        doReturn(link).when(p).getValue();
        doReturn(Arrays.asList("first")).when(row1).getItemPropertyIds();
        doReturn(p).when(row1).getItemProperty("first");
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet("files");
        _resourceProvider.renderSheet(sheet);
        Row dataRow = sheet.getRow(2);
        Cell linkCell = dataRow.getCell(0);
        assertEquals("FileCaption", linkCell.getStringCellValue());
        assertNotNull(linkCell.getHyperlink());
        // context path removed from base url
        assertEquals("https://host/base/doc/file.pdf", linkCell.getHyperlink().getAddress());
    }

    @Test
    public void constructorNullTableTest() {
        assertThrows(IllegalArgumentException.class, () -> new TableAsExcelResourceProvider(null, "base", "title"));
    }

    @Test
    public void toInputStreamIOExceptionTest() throws Exception {
        ComponentsMockUtils.mockComponentInstance(FileSystemHelper.class);
        Table table = mock(Table.class);
        doReturn(new Object[]{"col1"}).when(table).getVisibleColumns();
        TableAsExcelResourceProvider provider = new TableAsExcelResourceProvider(table, "file", "title");
        Workbook failingWb = mock(Workbook.class);
        doThrow(new IOException("write failed")).when(failingWb).write(Mockito.any());
        InputStream in = provider.toInputStream(failingWb);
        assertNull(in);
    }

    @Test
    public void renderSheetWithNoVisibleColumnsTest() {
        Table emptySource = mock(Table.class);
        doReturn(new Object[]{}).when(emptySource).getVisibleColumns();
        doReturn(Arrays.asList("row1")).when(emptySource).getVisibleItemIds();
        Item row1 = mock(Item.class);
        doReturn(row1).when(emptySource).getItem("row1");
        doReturn(Arrays.asList("a", "b")).when(row1).getItemPropertyIds();
        TableAsExcelResourceProvider provider = new TableAsExcelResourceProvider(emptySource, "empty", "title");
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet("empty");
        provider.renderSheet(sheet);
        Row dataRow = sheet.getRow(2);
        assertNotNull(dataRow);
        assertEquals(-1, dataRow.getLastCellNum());
    }

    private void mockProperties(Item[] rows, String[] values, String[] propertyIds) {
        for (Item row : rows) {
            doReturn(Arrays.asList(propertyIds)).when(row).getItemPropertyIds();
            for (int i = 0; i < propertyIds.length; i++) {
                Property prop = mock(Property.class);
                doReturn(values[i]).when(prop).getValue();
                doReturn(prop).when(row).getItemProperty(propertyIds[i]);
            }
        }
    }

    private Item[] mockVisibleRowItems(List<String> rowIds) {
        doReturn(rowIds).when(_source).getVisibleItemIds();
        Item[] result = new Item[rowIds.size()];
        int i = 0;
        for (Object id : rowIds) {
            Item row = mock(Item.class);
            doReturn(row).when(_source).getItem(id);
            result[i++] = row;
        }
        return result;
    }

}
