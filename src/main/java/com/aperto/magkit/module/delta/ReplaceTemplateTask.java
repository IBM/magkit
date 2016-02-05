package com.aperto.magkit.module.delta;

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
