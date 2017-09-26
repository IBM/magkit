package com.aperto.magkit.freemarker;

import info.magnolia.jcr.node2bean.Node2BeanProcessor;
import info.magnolia.rendering.engine.RenderException;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.jcr.RepositoryException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Locale;

import static com.aperto.magkit.mockito.ComponentsMockUtils.mockComponentInstance;
import static com.aperto.magkit.mockito.ContextMockUtils.cleanContext;
import static com.aperto.magkit.mockito.SiteMockUtils.mockSiteManager;
import static com.aperto.magkit.mockito.jcr.NodeMockUtils.mockNode;
import static org.apache.commons.lang.CharEncoding.UTF_8;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Unit tests for the folder Freemarker script.
 * TODO: fix folder freemarker test
 *
 * @author frank.sommer
 */
public class FolderTest extends FreemarkerTest {

    @Before
    public void setUp() throws RepositoryException {
        mockSiteManager();
        mockComponentInstance(Node2BeanProcessor.class);
    }

    @After
    public void cleanUp() throws RepositoryException {
        cleanContext();
    }

    @Ignore
    @Test
    public void testFolderScriptInEditMode() throws RepositoryException, RenderException, IOException {
        getServerConfiguration().setAdmin(true);
        String renderingResult = getRenderingResult(mockNode("/de"), "/magkit/templates/pages/folder.ftl", null);

        final InputStream expectedResultStream = getClass().getResourceAsStream("folder.html");
        final String expectedResult = IOUtils.toString(new InputStreamReader(expectedResultStream, UTF_8));
    }

    @Ignore
    @Test
    public void testFolderScriptInPreviewMode() throws RepositoryException, RenderException {
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
