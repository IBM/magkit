package de.ibmix.magkit.setup.delta;

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

import info.magnolia.jcr.util.NodeTypes;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

/**
 * Replaces any occurrence of a given template in pages or components with another template.
 * <p>
 * Iterates all nodes matching the query assembled by {@link RemoveTemplateNodesTask} and sets the new template id on
 * each node without altering other properties. This provides a controlled bulk template refactoring approach.
 * </p>
 * <h1>ATTENTION!</h1>
 * This task may result in a heavy load on the repository, depending on the number of nodes with current template of {@link RemoveTemplateNodesTask}.
 * Especially when the project is in an "after go-live"-phase (i.e. is already in production use), think twice before renaming a template.
 * <p>Preconditions: The current template id must exist on target nodes; repository access must be available.</p>
 * <p>Side Effects: Writes new template id to each matched node; session saves batched by parent class logic.</p>
 * <p>Error Handling: Exceptions during individual node updates propagate as {@link RepositoryException} to caller.</p>
 * <p>Thread-Safety: Not thread-safe for parallel execution on same workspace due to batch session saves.</p>
 * <p>Usage Example: {@code tasks.add(new ReplaceTemplateTask("app:pages/home", "app:pages/home2"));}</p>
 *
 * @author lars.gendner
 * @author frank.sommer
 * @since 2.0.0
 */
public class ReplaceTemplateTask extends RemoveTemplateNodesTask {

    private final String _newTemplate;

    /**
     * Convenience constructor replacing a template id below the root base path.
     *
     * @param currentTemplate template id to be replaced
     * @param newTemplate new template id
     */
    public ReplaceTemplateTask(String currentTemplate, String newTemplate) {
        this(currentTemplate, newTemplate, "/", null);
    }

    /**
     * Constructor allowing base path and query type control.
     *
     * @param currentTemplate template id to be replaced
     * @param newTemplate new template id
     * @param basePath base path for replacement
     * @param queryType query type
     */
    public ReplaceTemplateTask(String currentTemplate, String newTemplate, String basePath, String queryType) {
        this(currentTemplate, newTemplate, basePath, queryType, createTaskName(currentTemplate, newTemplate));
    }

    /**
     * Full constructor including custom task name.
     *
     * @param currentTemplate template id to be replaced
     * @param newTemplate new template id
     * @param basePath base path for replacement
     * @param queryType query type
     * @param taskName task name
     */
    public ReplaceTemplateTask(String currentTemplate, String newTemplate, String basePath, String queryType, String taskName) {
        super(currentTemplate, basePath, queryType, taskName);
        _newTemplate = newTemplate;
    }

    private static String createTaskName(final String currentTemplate, final String newTemplate) {
        return "Replacing template " + currentTemplate + " with " + newTemplate;
    }

    /**
     * Applies the new template id to the given node.
     *
     * @param node matched page/component node
     * @throws RepositoryException if setting property fails
     */
    @Override
    protected void doNodeOperation(final Node node) throws RepositoryException {
        NodeTypes.Renderable.set(node, _newTemplate);
    }
}
