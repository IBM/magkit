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

import info.magnolia.cms.util.ClasspathResourcesUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.jcr.util.NodeNameHelper;
import info.magnolia.jcr.util.NodeTypes.Content;
import info.magnolia.jcr.util.NodeTypes.Folder;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AbstractTask;
import info.magnolia.module.delta.TaskExecutionException;
import info.magnolia.objectfactory.Components;
import org.apache.commons.io.IOUtils;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.io.IOException;
import java.io.InputStream;

import static info.magnolia.jcr.util.NodeUtil.createPath;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.io.FilenameUtils.getBaseName;
import static org.apache.commons.lang3.StringUtils.removeEnd;

/**
 * Installation task that imports Groovy resources into the Magnolia "scripts" workspace.
 * <p>
 * Depending on the constructor parameters it either creates a script node (property {@code script=true}) or a plain
 * Groovy file node (e.g. for class definitions) with the file content stored in the {@code text} property. The target
 * node hierarchy is created if missing using {@link Folder} node types for intermediate folders and {@link Content}
 * nodes for the script itself.
 * </p>
 * <p>Preconditions: The resource path must exist on the classpath; the scripts workspace must be accessible.</p>
 * <p>Side Effects: Creates nodes and properties in the {@code scripts} workspace if they do not exist yet.</p>
 * <p>Error Handling: Any {@link RepositoryException} or {@link IOException} is wrapped in a {@link TaskExecutionException}.</p>
 * <p>Thread-Safety: Intended for execution during single-threaded module installation.</p>
 * <p>Usage Example:</p>
 * <pre>
 * new ScriptInstallTask("/myScript.groovy");                // installs under root
 * new ScriptInstallTask("/myScript.groovy", "/util");      // installs under /util
 * new ScriptInstallTask("/MyClass.groovy", "/classes", false); // installs non-script groovy file
 * </pre>
 * <p>
 * Deprecated: Use {@code info.magnolia.module.groovy.setup.InstallGroovyFile} instead for newer Magnolia versions.
 * </p>
 *
 * @deprecated use info.magnolia.module.groovy.setup.InstallGroovyFile instead
 * @author diana.racho
 * @author frank.sommer
 * @since version 3.1.2
 */
@Deprecated
public class ScriptInstallTask extends AbstractTask {
    private static final String SCRIPT_WORKSPACE_NAME = "scripts";

    private final String _resource;
    private final String _basePath;
    private final boolean _script;

    /**
     * Creates an installation task for a Groovy script stored at root.
     *
     * @param resource classpath resource path (e.g. /myScript.groovy)
     */
    public ScriptInstallTask(final String resource) {
        this(resource, "/");
    }

    /**
     * Creates an installation task for a Groovy script stored below the given base path.
     *
     * @param resource classpath resource path
     * @param basePath repository folder path (will be created if missing)
     */
    public ScriptInstallTask(final String resource, final String basePath) {
        this(resource, basePath, true);
    }

    /**
     * Creates an installation task for a Groovy resource which may be either a runnable script or a plain Groovy file.
     *
     * @param resource classpath resource path
     * @param basePath repository folder path (will be created if missing)
     * @param script true to mark it as script, false for plain groovy file
     */
    public ScriptInstallTask(final String resource, final String basePath, final boolean script) {
        this("Install script", "Install script (if not already there): " + resource + " below " + basePath, resource, basePath, script);
    }

    /**
     * Protected constructor allowing custom task name/description.
     *
     * @param taskName readable task name
     * @param taskDescription readable description
     * @param resource classpath resource path
     * @param basePath repository folder path
     * @param script true if script
     */
    protected ScriptInstallTask(final String taskName, final String taskDescription, final String resource, final String basePath, final boolean script) {
        super(taskName, taskDescription);
        _resource = resource;
        _basePath = basePath;
        _script = script;
    }

    /**
     * Executes the installation: validates the node name, ensures folder path exists, creates content node and sets
     * script metadata and text properties unless the node already exists.
     *
     * @param installContext current install context (unused but part of signature)
     * @throws TaskExecutionException on repository or IO issues
     */
    @Override
    public void execute(final InstallContext installContext) throws TaskExecutionException {
        String validNodeName = Components.getComponent(NodeNameHelper.class).getValidatedName(getBaseName(_resource));
        try {
            final Session scriptSession = MgnlContext.getJCRSession(SCRIPT_WORKSPACE_NAME);
            String nodePath = removeEnd(_basePath, "/") + "/" + validNodeName;
            if (!scriptSession.itemExists(nodePath)) {
                Node baseNode;
                if (scriptSession.itemExists(_basePath)) {
                    baseNode = scriptSession.getNode(_basePath);
                } else {
                    baseNode = createPath(scriptSession.getRootNode(), _basePath, Folder.NAME);
                }
                Node node = baseNode.addNode(validNodeName, Content.NAME);
                node.setProperty("script", _script);
                node.setProperty("text", getText());
            }
        } catch (RepositoryException | IOException e) {
            throw new TaskExecutionException("Failed to add script with " + e.getMessage(), e);
        }
    }

    /**
     * Reads the classpath resource content into a String using UTF-8.
     *
     * @return resource content
     * @throws IOException if reading fails
     */
    protected String getText() throws IOException {
        String text;
        try (InputStream is = ClasspathResourcesUtil.getStream(_resource)) {
            text = IOUtils.toString(is, UTF_8);
        }
        return text;
    }
}
