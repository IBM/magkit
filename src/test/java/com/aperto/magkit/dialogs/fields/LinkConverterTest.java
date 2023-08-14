package com.aperto.magkit.dialogs.fields;

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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.jcr.RepositoryException;

import static de.ibmix.magkit.test.cms.context.ContextMockUtils.cleanContext;
import static de.ibmix.magkit.test.cms.node.MagnoliaNodeMockUtils.mockPageNode;
import static de.ibmix.magkit.test.jcr.NodeStubbingOperation.stubIdentifier;
import static info.magnolia.repository.RepositoryConstants.WEBSITE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.nullValue;

/**
 * Test the {@link LinkConverter}.
 *
 * @author frank.sommer
 * @since 3.1.3
 */
public class LinkConverterTest {

    @Test
    public void testConvertToModel() {
        final LinkConverter linkConverter = new LinkConverter();
        linkConverter.setWorkspaceName(WEBSITE);
        assertThat(linkConverter.convertToModel(null, null, null), nullValue());
        assertThat(linkConverter.convertToModel("", null, null), equalTo(""));
        assertThat(linkConverter.convertToModel("#test", null, null), equalTo("#test"));
        assertThat(linkConverter.convertToModel("http://www.aperto.de", null, null), equalTo("http://www.aperto.de"));
        assertThat(linkConverter.convertToModel("https://www.aperto.de", null, null), equalTo("https://www.aperto.de"));
        assertThat(linkConverter.convertToModel("/path/to/page", null, null), equalTo("123-133"));
    }

    @Test
    public void testConvertToPresentation() {
        final LinkConverter linkConverter = new LinkConverter();
        linkConverter.setWorkspaceName(WEBSITE);
        assertThat(linkConverter.convertToPresentation(null, null, null), nullValue());
        assertThat(linkConverter.convertToPresentation("", null, null), equalTo(""));
        assertThat(linkConverter.convertToPresentation("#test", null, null), equalTo("#test"));
        assertThat(linkConverter.convertToPresentation("http://www.aperto.de", null, null), equalTo("http://www.aperto.de"));
        assertThat(linkConverter.convertToPresentation("https://www.aperto.de", null, null), equalTo("https://www.aperto.de"));
        assertThat(linkConverter.convertToPresentation("123-133", null, null), equalTo("/path/to/page"));

    }

    @Before
    public void prepareMgnl() throws RepositoryException {
        mockPageNode("/path/to/page", stubIdentifier("123-133"));
    }

    @After
    public void cleanMgnl() throws RepositoryException {
        cleanContext();
    }
}
