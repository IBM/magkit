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
 * Install task for load groovy scripts in scripts workspace.
 * <pre>
 * new ScriptInstallTask("/täst.groovy"); = create groovy script in root
 * new ScriptInstallTask("/täst.groovy", "/folder"); = create groovy script in folder
 * new ScriptInstallTask("/täst.groovy", "/folder2", false); = create groovy file (e.g. class) in folder2
 * </pre>
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

    public ScriptInstallTask(final String resource) {
        this(resource, "/");
    }

    public ScriptInstallTask(final String resource, final String basePath) {
        this(resource, basePath, true);
    }

    public ScriptInstallTask(final String resource, final String basePath, final boolean script) {
        this("Install script", "Install script (if not already there): " + resource + " below " + basePath, resource, basePath, script);
    }

    protected ScriptInstallTask(final String taskName, final String taskDescription, final String resource, final String basePath, final boolean script) {
        super(taskName, taskDescription);
        _resource = resource;
        _basePath = basePath;
        _script = script;
    }

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

    protected String getText() throws IOException {
        String text;
        try (InputStream is = ClasspathResourcesUtil.getStream(_resource)) {
            text = IOUtils.toString(is, UTF_8);
        }
        return text;
    }
}
