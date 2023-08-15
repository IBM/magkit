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

import com.aperto.magkit.utils.ExtendedLinkFieldHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import static de.ibmix.magkit.test.cms.context.ContextMockUtils.cleanContext;
import static de.ibmix.magkit.test.cms.node.MagnoliaNodeMockUtils.mockPageNode;
import static de.ibmix.magkit.test.jcr.NodeStubbingOperation.stubIdentifier;
import static info.magnolia.repository.RepositoryConstants.WEBSITE;
import static java.util.Locale.GERMAN;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Tests for converter of extended link field.
 *
 * @author Philipp Güttler (Aperto AG)
 * @since 04.06.2015
 */
public class ExtendedLinkConverterTest {

    private static final String IDENTIFIER = UUID.randomUUID().toString();
    private static final String PATH = "/path/to/node";
    private static final String PATH_NOT_FOUND = "/path/not/found";
    private static final String ANCHOR = "#anchor";
    private static final String EXTERNAL_URL = "http://www.aperto.com";
    private static final String IDENTIFIER_NOT_FOUND = UUID.randomUUID().toString();

    private ExtendedLinkConverter _converter;

    @Before
    public void setUp() throws Exception {
        _converter = new ExtendedLinkConverter();
        _converter.setExtendedLinkFieldHelper(new ExtendedLinkFieldHelper());
        _converter.setWorkspaceName(WEBSITE);
        mockPageNode(PATH, stubIdentifier(IDENTIFIER));
    }

    @Test
    public void testConvertToModelNull() {
        assertThat(_converter.convertToModel(null, String.class, GERMAN), nullValue());
    }

    @Test
    public void testConvertToModelPath() {
        assertThat(_converter.convertToModel(PATH, String.class, GERMAN), equalTo(IDENTIFIER));
        assertThat(_converter.convertToModel(PATH_NOT_FOUND, String.class, GERMAN), equalTo(PATH_NOT_FOUND));
    }

    @Test
    public void testConvertToModelAnchor() {
        assertThat(_converter.convertToModel(ANCHOR, String.class, GERMAN), equalTo(ANCHOR));
    }

    @Test
    public void testConvertToModelExternalUrl() {
        assertThat(_converter.convertToModel(EXTERNAL_URL, String.class, GERMAN), equalTo(EXTERNAL_URL));
    }

    @Test
    public void testConvertToPresentationNull() {
        assertThat(_converter.convertToPresentation(null, String.class, GERMAN), equalTo(EMPTY));
    }

    @Test
    public void testConvertToPresentationUuid() {
        assertThat(_converter.convertToPresentation(IDENTIFIER, String.class, GERMAN), equalTo(PATH));
        assertThat(_converter.convertToPresentation(IDENTIFIER_NOT_FOUND, String.class, GERMAN), equalTo(IDENTIFIER_NOT_FOUND));
    }

    @Test
    public void testConvertToPresentationAnchor() {
        assertThat(_converter.convertToPresentation(ANCHOR, String.class, GERMAN), equalTo(ANCHOR));
    }

    @Test
    public void testConvertToPresentationExternalUrl() {
        assertThat(_converter.convertToPresentation(EXTERNAL_URL, String.class, GERMAN), equalTo(EXTERNAL_URL));
    }

    @After
    public void tearDown() throws Exception {
        cleanContext();
    }
}
