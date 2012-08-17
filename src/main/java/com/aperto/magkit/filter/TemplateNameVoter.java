package com.aperto.magkit.filter;

import info.magnolia.voting.voters.BasePatternVoter;
import org.apache.commons.lang.ArrayUtils;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import static info.magnolia.context.MgnlContext.getJCRSession;
import static info.magnolia.jcr.util.MetaDataUtil.getTemplate;
import static info.magnolia.repository.RepositoryConstants.WEBSITE;
import static org.apache.commons.lang.StringUtils.*;

/**
 * A custom Voter to exclude the pages with a certain template from caching.
 * Votes "true" if the URI starts with the configured rootPath
 * and if the template name of the target content equals the configured name.
 *
 * @author Wolf Bubenik, Aperto AG
 * @author frank.sommer
 * @since 2010-06-10
 */
public class TemplateNameVoter extends BasePatternVoter {
    private String[] _templates = ArrayUtils.EMPTY_STRING_ARRAY;
    private String _rootPath = EMPTY;

    public TemplateNameVoter() {
        setTrueValue(2);
    }

    /**
     * Adder for the name of the template to be excluded from caching.
     *
     * @param template the template name as String.
     */
    public void addTemplate(String template) {
        _templates = (String[]) ArrayUtils.add(_templates, template);
    }

    public String[] getTemplates() {
        return _templates;
    }

    /**
     * Setter for the starts with uri.
     *
     * @param rootPath the rootPath as String.
     */
    public void setRootPath(String rootPath) {
        _rootPath = rootPath;
    }

    protected boolean boolVote(Object value) {
        String template = getTargetTemplateName(value);
        return ArrayUtils.contains(_templates, template);
    }

    private String getTargetTemplateName(Object value) {
        String template = EMPTY;
        String uri = resolveURIFromValue(value);
        // skip expensive content lookup if URI is not of interest, e.g. ressource URI...
        if (isBlank(_rootPath) || uri.startsWith(_rootPath)) {
            String path = substringBeforeLast(uri, ".");
            try {
                Session session = getJCRSession(WEBSITE);
                Node node = session.getNode(path);
                template = getTemplate(node);
            } catch (RepositoryException e) {
                // ignore, use empty string
            }
        }
        return template;
    }
}