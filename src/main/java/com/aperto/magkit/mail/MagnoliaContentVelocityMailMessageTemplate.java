package com.aperto.magkit.mail;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.context.MgnlContext;
import org.apache.commons.collections.ExtendedProperties;
import static org.apache.commons.lang.StringUtils.*;
import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.resource.Resource;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.apache.velocity.runtime.resource.loader.ResourceLoader;
import javax.jcr.RepositoryException;
import java.io.InputStream;
import static java.lang.System.currentTimeMillis;
import java.util.Map;
import java.util.Properties;

/**
 * This class generates {@link org.springframework.mail.SimpleMailMessage}s by evaluating a velocity template stored
 * within the magnolia content repository.
 * It is an implementation of the {@link com.aperto.bsh.cafe.email.MailMessageTemplate} interface.
 * <p/>
 * The first line of the velocity template will be interpreted as mail subject, the remaining lines as text.
 * <p/>
 * The velocity template name may be set on configuration time or dynamically for each evaluation.
 *
 * @author Norman Wiechmann (Aperto AG)
 * @see <a href="http://velocity.apache.org/engine/">The Apache Velocity Engine</a>
 */
public class MagnoliaContentVelocityMailMessageTemplate extends AbstractMailMessageTemlate {

    private VelocityEngine _velocityEngine;

    private String _repositoryId = ContentRepository.WEBSITE;

    public MagnoliaContentVelocityMailMessageTemplate() throws Exception {
        _velocityEngine = new VelocityEngine();
        Properties properties = new Properties();
        properties.setProperty("resource.loader", "classpath, magnolia");
        properties.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        properties.setProperty("magnolia.resource.loader.class", MagnoliaContentResourceLoader.class.getName());
        _velocityEngine.init(properties);
    }

    //  ---------------------------------------------------------------------
    //  Configuration
    //  ---------------------------------------------------------------------

    /**
     * This method can be used to override the default content repository id 'website'.
     */
    public void setRepositoryId(final String repositoryId) {
        _repositoryId = repositoryId;
    }

    //  ---------------------------------------------------------------------
    //  Inheritence interface implementation
    //  ---------------------------------------------------------------------

    /**
     * Returns the template by the configured name or a dynamic template that is set by a parameter.
     * <p/>
     * If a dynamic template is used, its name is looked up within the given parameters. A name of a dynamic template
     * must not have a repository id or path part.
     *
     * @throws IllegalArgumentException A dynamic template name is required but it was not found or invalid characters were used.
     * @see #setTemplateName(String)
     * @see #getDynamicTemplateParameterName()
     */
    protected Template getTemplate(final Map<String, ? extends Object> parameters) throws Exception {
        String templateName = _repositoryId + getTemplateName();
        if (isBlank(templateName) || templateName.endsWith("/")) {
            String dynamicTemplateName = null;
            String dynamicTemplateParameterName = getDynamicTemplateParameterName();
            if (parameters != null && parameters.containsKey(dynamicTemplateParameterName)) {
                dynamicTemplateName = parameters.get(dynamicTemplateParameterName).toString();
            }
            if (isNotBlank(dynamicTemplateName) && indexOfAny(dynamicTemplateName, "/.") == -1) {
                templateName += dynamicTemplateName.trim();
            } else {
                throw new IllegalArgumentException("A dynamic template name is required but it was not found or invalid characters were used.");
            }
        }
        return _velocityEngine.getTemplate(templateName, getTemplateEncoding());
    }

    //  ---------------------------------------------------------------------
    //  Helper
    //  ---------------------------------------------------------------------

    /**
     * Implementation of {@link ResourceLoader} that reads the resource stream from magnolia content repository.
     */
    public static class MagnoliaContentResourceLoader extends ResourceLoader {

        private static final Logger LOGGER = Logger.getLogger(MagnoliaContentVelocityMailMessageTemplate.class);

        @Override
        public void init(final ExtendedProperties extendedProperties) {
            // nothing to do
        }

        @Override
        public InputStream getResourceStream(final String path) throws ResourceNotFoundException {
            InputStream nodeData = null;
            int pathSplitter = path.indexOf('/');
            // some requests do not come from the mail message template, e.g. velocimacro file 'VM_global_library.vm'
            if (pathSplitter > 0) {
                String repositoryId = path.substring(0, pathSplitter);
                String propertyPath = path.substring(pathSplitter);
                nodeData = retrieveNodeDataStream(repositoryId, propertyPath);
            }
            if (nodeData == null) {
                throw new ResourceNotFoundException("Resource not found. path:" + path);
            }
            return nodeData;
        }

        protected InputStream retrieveNodeDataStream(String repositoryId, String propertyPath) {
            InputStream nodeData = null;
            HierarchyManager hierarchyManager = MgnlContext.getHierarchyManager(repositoryId);
            try {
                if (hierarchyManager.isExist(propertyPath) && hierarchyManager.isNodeData(propertyPath)) {
                    nodeData = hierarchyManager.getNodeData(propertyPath).getStream();
                }
            } catch (RepositoryException e) {
                LOGGER.error("Resource lookup failed. repository:" + repositoryId + ", path:" + propertyPath, e);
            }
            return nodeData;
        }

        @Override
        public boolean isSourceModified(final Resource resource) {
            return true;
        }

        @Override
        public long getLastModified(final Resource resource) {
            return currentTimeMillis();
        }
    }
}