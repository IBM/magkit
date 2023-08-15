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
 * <h1>ATTENTION!</h1>
 * This task may result in a heavy load on the repository, depending on the number of nodes with {@link #_currentTemplate}.
 * Especially when the project is in an "after go-live"-phase (i.e. is already in production use), think twice before renaming a template.
 *
 * @author lars.gendner
 * @author frank.sommer
 * @since 2.0.0
 */
public class ReplaceTemplateTask extends RemoveTemplateNodesTask {

    private final String _newTemplate;

    /**
     * Replace current template id with new template id in website repository.
     *
     * @param currentTemplate template id to be replaced
     * @param newTemplate     new template id
     */
    public ReplaceTemplateTask(String currentTemplate, String newTemplate) {
        this(currentTemplate, newTemplate, "/", null);
    }

    /**
     * Replace current template id with new template id in website repository.
     *
     * @param currentTemplate template id to be replaced
     * @param newTemplate     new template id
     * @param basePath        base path for replacement
     */
    public ReplaceTemplateTask(String currentTemplate, String newTemplate, String basePath, String queryType) {
        this(currentTemplate, newTemplate, basePath, queryType, createTaskName(currentTemplate, newTemplate));
    }

    /**
     * Replace current template id with new template id in website repository.
     *
     * @param currentTemplate template id to be replaced
     * @param newTemplate     new template id
     * @param basePath        base path for replacement
     */
    public ReplaceTemplateTask(String currentTemplate, String newTemplate, String basePath, String queryType, String taskName) {
        super(currentTemplate, basePath, queryType, taskName);
        _newTemplate = newTemplate;
    }

    private static String createTaskName(final String currentTemplate, final String newTemplate) {
        return "Replacing template " + currentTemplate + " with " + newTemplate;
    }

    protected void doNodeOperation(final Node node) throws RepositoryException {
        NodeTypes.Renderable.set(node, _newTemplate);
    }
}
