package de.ibmix.magkit.query;

/*-
 * #%L
 * magkit-query
 * %%
 * Copyright (C) 2023 - 2024 IBM iX
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

import de.ibmix.magkit.test.cms.context.ContextMockUtils;
import info.magnolia.jcr.util.NodeTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.query.Query;

import static de.ibmix.magkit.test.cms.context.ContextMockUtils.mockQuery;
import static de.ibmix.magkit.test.jcr.NodeMockUtils.mockNode;
import static de.ibmix.magkit.test.jcr.query.QueryStubbingOperation.stubResult;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Tests for {@link NodeByQuery}.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2023-10-28
 **/
class NodeByQueryTest {

    @BeforeEach
    void setUp() {
        ContextMockUtils.cleanContext();
    }

    @Test
    void apply() throws RepositoryException {
        NodeByQuery nodesByQuery = new NodeByQuery("test", NodeTypes.Content.NAME, "title");
        String expectedQuery = "SELECT * FROM [mgnl:content] WHERE [title] = 'Some Title' ORDER BY [mgnl:lastModified] DESC";
        Query query = mockQuery("test", Query.JCR_SQL2, expectedQuery, stubResult());
        assertNull(nodesByQuery.apply("Some Title"));

        Node n1 = mockNode("test", "/node1");
        Node n2 = mockNode("test", "/node2");
        stubResult(n1, n2).of(query);
        assertEquals(n1, nodesByQuery.apply("Some Title"));
    }
}