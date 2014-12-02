package com.aperto.magkit.module.delta;

import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeTypes;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AbstractTask;
import info.magnolia.module.delta.TaskExecutionException;
import info.magnolia.objectfactory.Components;
import info.magnolia.templating.functions.TemplatingFunctions;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

import static info.magnolia.jcr.util.PropertyUtil.setProperty;
import static java.lang.Boolean.TRUE;
import static org.apache.commons.lang3.StringUtils.EMPTY;

/**
 * Install task installing scripts.
 * @author Aperto AG
 */
public class ScriptInstallTask extends AbstractTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScriptInstallTask.class);

    private static final String SCRIPT_WORKSPACE_NAME = "scripts";

    private String _resource;
    private final TemplatingFunctions _templatingFunctions = Components.getComponent(TemplatingFunctions.class);

    public ScriptInstallTask(final String resource) {
        this("Install scripts", "Install or update scripts.", resource);
    }

    public ScriptInstallTask(final String taskName, final String taskDescription, final String resource) {
        super(taskName, taskDescription);
        _resource = resource;
    }

    @Override
    public void execute(final InstallContext installContext) throws TaskExecutionException {
        String nodeName = _resource.substring(StringUtils.lastIndexOf(_resource, '/') + 1, _resource.indexOf('.'));
        try {
            final Session scriptSession = MgnlContext.getJCRSession(SCRIPT_WORKSPACE_NAME);
            Node node = _templatingFunctions.nodeByPath(nodeName, SCRIPT_WORKSPACE_NAME);
            if (node == null || !node.isNodeType(NodeTypes.Content.NAME)) {
                node = scriptSession.getRootNode().addNode(nodeName, NodeTypes.Content.NAME);
                setProperty(node, "script", TRUE);
                setProperty(node, "text", getText());
            }
            scriptSession.save();
        } catch (RepositoryException e) {
            throw new TaskExecutionException("Failed to add script with " + e.getMessage(), e);
        } catch (IOException e) {
            throw new TaskExecutionException("Failed to add script with " + e.getMessage(), e);
        }
    }

    protected String getText() throws IOException {
        String text = EMPTY;
        InputStream is = ScriptInstallTask.class.getResourceAsStream(_resource);
        StringWriter sw = new StringWriter();
        try {
            IOUtils.copy(is, sw);
            text = sw.getBuffer().toString();
        } catch (IOException e) {
            LOGGER.error("Failed to read script.", e);
        } finally {
            is.close();
            sw.close();
        }
        return text;
    }
}