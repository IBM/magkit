package com.aperto.magkit.freemarker;

import com.aperto.magkit.mockito.SiteMockUtils;
import info.magnolia.rendering.engine.RenderException;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.jcr.RepositoryException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;

import static com.aperto.magkit.mockito.jcr.NodeMockUtils.mockNode;
import static org.apache.commons.lang.CharEncoding.UTF_8;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Unit tests for the folder Freemarker script.
 *
 * @author frank.sommer
 */
public class FolderTest extends FreemarkerTest {

    @Before
    public void setUp() throws RepositoryException {
        SiteMockUtils.mockSiteManager();
    }

    @Ignore
    @Test
    public void testFolderSkriptInEditMode() throws RepositoryException, RenderException, IOException {
        getServerConfiguration().setAdmin(true);
        String renderingResult = getRenderingResult(mockNode("/de"), "/magkit/templates/pages/folder.ftl", null);

        final InputStream expectedResultStream = getClass().getResourceAsStream("folder.html");
        final String expectedResult = IOUtils.toString(new InputStreamReader(expectedResultStream, UTF_8));

        //TODO: Fix setup of STKRenderer with RenderingEngine in FreemarkerTest.getRenderingResult()
        //assertEquals(renderingResult.trim(), expectedResult.trim());
    }

    @Test
    public void testFolderSkriptInPreviewMode() throws RepositoryException, RenderException {
        getServerConfiguration().setAdmin(false);
        String renderingResult = getRenderingResult(mockNode("/de"), "/magkit/templates/pages/folder.ftl", null);
        assertThat(renderingResult, is(""));
    }

    @Override
    protected Locale getLocale() {
        return Locale.GERMAN;
    }

    @Override
    protected String getI18nBasename() {
        return "mgnl-i18n.magkit-messages";
    }
}
