package de.ibmix.magkit.ui.dialogs.fields;

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

import info.magnolia.ui.datasource.jcr.JcrDatasource;
import info.magnolia.ui.datasource.jcr.JcrSessionWrapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import static de.ibmix.magkit.test.cms.context.ContextMockUtils.cleanContext;
import static de.ibmix.magkit.test.cms.node.MagnoliaNodeMockUtils.mockPageNode;
import static de.ibmix.magkit.test.jcr.NodeStubbingOperation.stubIdentifier;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link LinkConverter} basic path/external/anchor conversion behavior.
 *
 * @author frank.sommer
 * @since 3.1.3
 */
public class LinkConverterTest {

    private LinkConverter _linkConverter;

    @Test
    public void testConvertToModel() {
        _linkConverter.convertToModel(null, null).ifOk(value -> assertNull(value));
        _linkConverter.convertToModel("", null).ifOk(value -> assertNull(value));
        _linkConverter.convertToModel("#test", null).ifOk(value -> assertEquals("#test", value));
        _linkConverter.convertToModel("http://www.aperto.de", null).ifOk(value -> assertEquals("http://www.aperto.de", value));
        _linkConverter.convertToModel("https://www.aperto.de", null).ifOk(value -> assertEquals("https://www.aperto.de", value));
        _linkConverter.convertToModel("/path/to/page", null).ifOk(value -> assertEquals("123-133", value));
    }

    @Test
    public void testConvertToPresentation() {
        assertNull(_linkConverter.convertToPresentation(null, null));
        assertNull(_linkConverter.convertToPresentation("", null));
        assertEquals("#test", _linkConverter.convertToPresentation("#test", null));
        assertEquals("http://www.aperto.de", _linkConverter.convertToPresentation("http://www.aperto.de", null));
        assertEquals("https://www.aperto.de", _linkConverter.convertToPresentation("https://www.aperto.de", null));
        assertEquals("/path/to/page", _linkConverter.convertToPresentation("123-133", null));
    }

    @BeforeEach
    public void prepareMgnl() throws RepositoryException {
        final JcrDatasource jcrDatasource = mock(JcrDatasource.class);
        final JcrSessionWrapper jcrSessionWrapper = mock(JcrSessionWrapper.class);
        final Node pageNode = mockPageNode("/path/to/page", stubIdentifier("123-133"));
        when(jcrSessionWrapper.getNode("/path/to/page")).thenReturn(pageNode);
        when(jcrSessionWrapper.getNodeByIdentifier("123-133")).thenReturn(pageNode);
        when(jcrDatasource.getJCRSession()).thenReturn(jcrSessionWrapper);
        _linkConverter = new LinkConverter(jcrDatasource);
    }

    @AfterEach
    public void cleanMgnl() {
        cleanContext();
    }
}
