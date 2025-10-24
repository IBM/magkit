package de.ibmix.magkit.ui.freemarker;

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

import de.ibmix.magkit.test.cms.freemarker.FreemarkerTest;
import info.magnolia.jcr.node2bean.Node2BeanProcessor;
import info.magnolia.rendering.engine.RenderException;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.jcr.RepositoryException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import static de.ibmix.magkit.test.cms.context.ComponentsMockUtils.mockComponentInstance;
import static de.ibmix.magkit.test.cms.context.ContextMockUtils.cleanContext;
import static de.ibmix.magkit.test.cms.site.SiteMockUtils.mockSiteManager;
import static de.ibmix.magkit.test.jcr.NodeMockUtils.mockNode;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for the folder Freemarker script.
 * TODO: fix folder freemarker test
 *
 * @author frank.sommer
 */
public class FolderTest extends FreemarkerTest {

    @BeforeEach
    public void setUp() throws RepositoryException {
        mockSiteManager();
        mockComponentInstance(Node2BeanProcessor.class);
    }

    @AfterEach
    public void cleanUp() {
        cleanContext();
    }

    @Disabled
    @Test
    public void testFolderScriptInEditMode() throws RepositoryException, RenderException, IOException {
        getServerConfiguration().setAdmin(true);
        String renderingResult = getRenderingResult(mockNode("/de"), "/magkit/templates/pages/folder.ftl", null);

        final InputStream expectedResultStream = getClass().getResourceAsStream("folder.html");
        final String expectedResult = IOUtils.toString(new InputStreamReader(expectedResultStream, StandardCharsets.UTF_8));
    }

    @Disabled
    @Test
    public void testFolderScriptInPreviewMode() throws RepositoryException, RenderException {
        getServerConfiguration().setAdmin(false);
        String renderingResult = getRenderingResult(mockNode("/de"), "/magkit/templates/pages/folder.ftl", null);
        assertEquals("", renderingResult);
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
