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

import com.vaadin.server.ExternalResource;
import com.vaadin.server.FileResource;
import com.vaadin.ui.AbstractComponent;
import com.vaadin.ui.Link;
import com.vaadin.v7.data.Item;
import com.vaadin.v7.data.Property;
import com.vaadin.v7.ui.Table;
import info.magnolia.cms.beans.config.ServerConfiguration;
import info.magnolia.context.WebContext;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.jcr.RepositoryException;
import java.io.File;
import java.util.Arrays;
import java.util.List;

import static de.ibmix.magkit.test.cms.context.ContextMockUtils.cleanContext;
import static de.ibmix.magkit.test.cms.context.ContextMockUtils.mockWebContext;
import static de.ibmix.magkit.test.cms.context.ServerConfigurationMockUtils.mockServerConfiguration;
import static de.ibmix.magkit.test.cms.context.ServerConfigurationStubbingOperation.stubDefaultBaseUrl;
import static de.ibmix.magkit.test.cms.context.WebContextStubbingOperation.stubContextPath;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

/**
 * Test TableAsExcelResourceProvider.
 *
 * @author wolf.bubenik
 * @since 31.01.18.
 */
public class TableAsExcelResourceProviderTest {

    private Table _source;
    private TableAsExcelResourceProvider _resourceProvider;

    @Before
    public void setUp() throws Exception {
        _source = mock(Table.class);
        doReturn(new Object[]{"first", "second"}).when(_source).getVisibleColumns();
        _resourceProvider = new TableAsExcelResourceProvider(_source, "test:base file-name", "test title");
    }

    @After
    public void tearDown() {
        cleanContext();
    }

    @Test
    public void createExcel() {
        //TODO: implement test case
    }

    @Test
    public void getHeaderStyle() {
        Workbook wb = new XSSFWorkbook();
        CellStyle style = _resourceProvider.getHeaderStyle(wb);
        assertThat(style, notNullValue());
        assertThat(((int) style.getFontIndex()), is(1));
    }

    @Test
    public void renderSheet() {
        //TODO: implement test case
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
        assertThat(sheet.getRow(1), notNullValue());
        assertThat(((int) sheet.getRow(1).getLastCellNum()), is(2));
        Cell first = sheet.getRow(1).getCell(0);
        Cell second = sheet.getRow(1).getCell(1);
        Cell hidden = sheet.getRow(1).getCell(2);
        assertThat(first.getStringCellValue(), is("Spalte 1"));
        assertThat(second.getStringCellValue(), is("Spalte 2"));
        assertThat(hidden, nullValue());
    }

    @Test
    public void updateColumnWidthTest() {
        int[] colWidth = new int[]{0, 0, 0};
        _resourceProvider.updateColumnWidth(colWidth, null, 1);
        assertThat(colWidth[0], is(0));
        assertThat(colWidth[1], is(0));
        assertThat(colWidth[2], is(0));

        _resourceProvider.updateColumnWidth(colWidth, "test", 1);
        assertThat(colWidth[0], is(0));
        assertThat(colWidth[1], is(4));
        assertThat(colWidth[2], is(0));

        _resourceProvider.updateColumnWidth(colWidth, "test again", 1);
        assertThat(colWidth[0], is(0));
        assertThat(colWidth[1], is(10));
        assertThat(colWidth[2], is(0));

        _resourceProvider.updateColumnWidth(colWidth, "test again", 1);
        assertThat(colWidth[0], is(0));
        assertThat(colWidth[1], is(10));
        assertThat(colWidth[2], is(0));

        _resourceProvider.updateColumnWidth(colWidth, "test more", 1);
        assertThat(colWidth[0], is(0));
        assertThat(colWidth[1], is(10));
        assertThat(colWidth[2], is(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void addCellStringTestForNull() {
        _resourceProvider.addCellString(null, null);
    }

    @Test
    public void addCellStringTest() {
        Workbook wb = new XSSFWorkbook();
        Row row = wb.createSheet().createRow(0);
        Cell cell = row.createCell(0);
        assertThat(_resourceProvider.addCellString(null, cell), is(EMPTY));
        assertThat(_resourceProvider.addCellString("test", cell), is("test"));

        AbstractComponent value = mock(AbstractComponent.class);
        doReturn("test caption").when(value).getCaption();
        assertThat(_resourceProvider.addCellString(value, cell), is("test caption"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void toUrlTestForNull() {
        _resourceProvider.toUrl(null);
    }

    @Test
    public void toUrlTest() throws RepositoryException {
        Link link = mock(Link.class);
        assertThat(_resourceProvider.toUrl(link), is(EMPTY));

        mockServerConfiguration(stubDefaultBaseUrl("https://test.aperto.de"));
        mockWebContext();
        ExternalResource external = mock(ExternalResource.class);
        doReturn(external).when(link).getResource();
        assertThat(_resourceProvider.toUrl(link), is(EMPTY));

        doReturn("/path/to/page.html").when(external).getURL();
        assertThat(_resourceProvider.toUrl(link), is("https://test.aperto.de/path/to/page.html"));

        FileResource fileResource = mock(FileResource.class);
        doReturn(fileResource).when(link).getResource();
        assertThat(_resourceProvider.toUrl(link), is(EMPTY));

        File file = mock(File.class);
        doReturn(file).when(fileResource).getSourceFile();
        assertThat(_resourceProvider.toUrl(link), is(EMPTY));

        doReturn("/path/to/file.pdf").when(file).getAbsolutePath();
        assertThat(_resourceProvider.toUrl(link), is("https://test.aperto.de/path/to/file.pdf"));
    }

    @Test
    public void getBaseUrlTest() throws RepositoryException {
        ServerConfiguration serverConfig = mockServerConfiguration(stubDefaultBaseUrl("https://test.aperto.de"));
        WebContext ctx = mockWebContext(stubContextPath(""));
        assertThat(_resourceProvider.getBaseUrl(), is("https://test.aperto.de"));

        stubDefaultBaseUrl("https://test.aperto.de/contextPath").of(serverConfig);
        assertThat(_resourceProvider.getBaseUrl(), is("https://test.aperto.de/contextPath"));

        stubContextPath("/author").of(ctx);
        assertThat(_resourceProvider.getBaseUrl(), is("https://test.aperto.de/contextPath"));

        stubContextPath("/contextPath").of(ctx);
        assertThat(_resourceProvider.getBaseUrl(), is("https://test.aperto.de"));
    }

    @Test
    public void renderSheetTest() {
        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet("test sheet");
        Item[] rows = mockVisibleRowItems(Arrays.asList("1", "2", "3"));
        _resourceProvider.renderSheet(sheet);
        assertThat(sheet.getColumnWidth(0), is(0));
        assertThat(sheet.getColumnWidth(1), is(0));
        assertThat(sheet.getRow(2), notNullValue());
        assertThat(sheet.getRow(3), notNullValue());
        assertThat(sheet.getRow(4), notNullValue());

        mockProperties(rows, new String[]{"first value", "invisible value", "second value"}, new String[]{"first", "invisible", "second"});
        _resourceProvider.renderSheet(sheet);
        assertThat(sheet.getRow(2).getCell(0).getStringCellValue(), is("first value"));
        assertThat(sheet.getRow(2).getCell(1).getStringCellValue(), is("second value"));
        assertThat(((int) sheet.getRow(2).getLastCellNum()), is(2));
        assertThat(sheet.getRow(3).getCell(0).getStringCellValue(), is("first value"));
        assertThat(sheet.getRow(3).getCell(1).getStringCellValue(), is("second value"));
        assertThat(((int) sheet.getRow(3).getLastCellNum()), is(2));
        assertThat(sheet.getRow(4).getCell(0).getStringCellValue(), is("first value"));
        assertThat(sheet.getRow(4).getCell(1).getStringCellValue(), is("second value"));
        assertThat(((int) sheet.getRow(4).getLastCellNum()), is(2));

        assertThat(sheet.getColumnWidth(0), is(11 * 256));
        assertThat(sheet.getColumnWidth(1), is(12 * 256));
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
