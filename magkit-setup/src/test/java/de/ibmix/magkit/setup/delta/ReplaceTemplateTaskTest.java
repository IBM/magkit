package de.ibmix.magkit.setup.delta;

/*-
 * #%L
 * IBM iX Magnolia Kit
 * %%
 * Copyright (C) 2025 IBM iX
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
import de.ibmix.magkit.test.jcr.NodeMockUtils;
import info.magnolia.jcr.util.NodeTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import static de.ibmix.magkit.test.jcr.NodeStubbingOperation.stubProperty;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link ReplaceTemplateTask} covering constructor pathways and template replacement operation.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2025-10-28
 */
public class ReplaceTemplateTaskTest {

    @BeforeEach
    public void setUp() {
        ContextMockUtils.cleanContext();
    }

    /**
     * Convenience constructor delegates to base path root and null query type; query building unaffected by new template id.
     */
    @Test
    public void convenienceConstructorBuildsQueryForCurrentTemplateOnly() {
        ReplaceTemplateTask task = new ReplaceTemplateTask("app:pages/home", "app:pages/home2");
        String statement = task.buildQueryStatement();
        assertEquals("select * from [mgnl:page] where [mgnl:template] = 'app:pages/home'", statement);
    }

    /**
     * Auto-detects component node type when queryType is null and template contains ":components/".
     */
    @Test
    public void autoDetectsComponentNodeTypeWithoutDescendantClause() {
        ReplaceTemplateTask task = new ReplaceTemplateTask("app:components/box", "app:components/boxNew");
        String statement = task.buildQueryStatement();
        assertEquals("select * from [mgnl:component] where [mgnl:template] = 'app:components/box'", statement);
    }

    /**
     * Auto-detects component node type and adds descendant clause when basePath length > 1 and queryType is null.
     */
    @Test
    public void autoDetectsComponentNodeTypeWithDescendantClause() {
        ReplaceTemplateTask task = new ReplaceTemplateTask("app:components/teaser", "app:components/teaserNew", "/parent", null);
        String statement = task.buildQueryStatement();
        assertEquals("select * from [mgnl:component] where [mgnl:template] = 'app:components/teaser' and ISDESCENDANTNODE([/parent])", statement);
    }

    /**
     * Auto-detects page node type and adds descendant clause when basePath length > 1 and queryType is null.
     */
    @Test
    public void autoDetectsPageNodeTypeWithDescendantClause() {
        ReplaceTemplateTask task = new ReplaceTemplateTask("app:pages/home", "app:pages/home2", "/sites", null);
        String statement = task.buildQueryStatement();
        assertEquals("select * from [mgnl:page] where [mgnl:template] = 'app:pages/home' and ISDESCENDANTNODE([/sites])", statement);
    }

    /**
     * Constructor with explicit base path and query type includes descendant clause and respects provided node type.
     */
    @Test
    public void fullConstructorIncludesDescendantClause() {
        ReplaceTemplateTask task = new ReplaceTemplateTask("app:components/teaser", "app:components/teaserNew", "/parent", NodeTypes.Component.NAME);
        String statement = task.buildQueryStatement();
        assertEquals("select * from [mgnl:component] where [mgnl:template] = 'app:components/teaser' and ISDESCENDANTNODE([/parent])", statement);
    }

    /**
     * Full constructor with custom task name does not alter query building logic.
     */
    @Test
    public void fullConstructorWithCustomTaskNameBuildsSameQuery() {
        ReplaceTemplateTask task = new ReplaceTemplateTask("app:components/teaser", "app:components/teaserNew", "/parent", NodeTypes.Component.NAME, "customName");
        String statement = task.buildQueryStatement();
        assertEquals("select * from [mgnl:component] where [mgnl:template] = 'app:components/teaser' and ISDESCENDANTNODE([/parent])", statement);
    }

    /**
     * Node operation sets the new template id on the renderable property.
     */
    @Test
    public void doNodeOperationSetsNewTemplate() throws RepositoryException {
        Node node = NodeMockUtils.mockNode("website", "/root/node", stubProperty(NodeTypes.Renderable.TEMPLATE, "app:pages/home"));
        ReplaceTemplateTask task = new ReplaceTemplateTask("app:pages/home", "app:pages/home2");
        task.doNodeOperation(node);
        verify(node, times(1)).setProperty(NodeTypes.Renderable.TEMPLATE, "app:pages/home2");
    }

    /**
     * Node operation propagates RepositoryException thrown while setting the property.
     */
    @Test
    public void doNodeOperationPropagatesException() throws RepositoryException {
        Node node = NodeMockUtils.mockNode("website", "/root/fail", stubProperty(NodeTypes.Renderable.TEMPLATE, "app:pages/home"));
        doThrow(new RepositoryException("fail")).when(node).setProperty(NodeTypes.Renderable.TEMPLATE, "app:pages/home2");
        ReplaceTemplateTask task = new ReplaceTemplateTask("app:pages/home", "app:pages/home2");
        assertThrows(RepositoryException.class, () -> task.doNodeOperation(node));
    }
}
