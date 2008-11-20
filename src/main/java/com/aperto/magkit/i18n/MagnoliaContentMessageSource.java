package com.aperto.magkit.i18n;

import java.text.MessageFormat;
import java.util.Locale;
import javax.jcr.RepositoryException;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.util.NodeDataUtil;
import info.magnolia.context.MgnlContext;
import org.apache.log4j.Logger;
import org.springframework.context.support.AbstractMessageSource;

/**
 * Implementation of spring {@link org.springframework.context.MessageSource} interface.
 * To lookup message codes the magnolia content repository will be used.
 * <p/>
 * E.g. assuming a single path of 'i18n/foo' is configured. A message with a code 'bar' and current locale 'en' is
 * requested. This implementation will lookup at default within the website repository for a value node data at the path
 * '/en/i18n/foo'. A fallback to a default language is not supported.
 * <p/>
 * TODO: add caching of resolved messages; requires content change listener
 *
 * @author Norman Wiechmann, Aperto AG (07-22-2008)
 */
public class MagnoliaContentMessageSource extends AbstractMessageSource {
    private static final Logger LOGGER = Logger.getLogger(MagnoliaContentMessageSource.class);
    public static final String LANGUAGE_PLACEHOLDER = "{language}";
    private String[] _paths;
    private String _repositoryId = ContentRepository.WEBSITE;

    /**
     * Sets a single content repository path for lookup message codes. Use {@code {language}} placeholder to mark a
     * section of the path for beeing replaced by the current locale language.
     * <p/>
     * E.g. /config/{language}/labels
     */
    public void setPath(final String path) {
        setPaths(path);
    }

    /**
     * Sets multiple content repository paths for lookup message codes. Use {@code {language}} placeholder to mark a
     * section of a path for beeing replaced by the current locale language.
     * <p/>
     * E.g. /config/{language}/labels
     */
    public void setPaths(final String... paths) {
        _paths = paths;
        if (_paths != null) {
            for (int i = 0; i < _paths.length; i++) {
                // normalize path; it must start with a '/'
                if (!_paths[i].startsWith("/")) {
                    _paths[i] = "/" + _paths[i];
                }
            }
        }
    }

    /**
     * Changes the repository that is used for lookup message codes. Defaults to {@link ContentRepository#WEBSITE}.
     */
    public void setRepositoryId(final String repositoryId) {
        _repositoryId = repositoryId;
    }

    @Override
    protected MessageFormat resolveCode(final String code, final Locale locale) {
        String message = resolveCodeWithoutArguments(code, locale);
        return message != null ? createMessageFormat(message, locale) : null;
    }

    @Override
    protected String resolveCodeWithoutArguments(final String code, final Locale locale) {
        String message = null;
        HierarchyManager hierarchyManager = MgnlContext.getHierarchyManager(_repositoryId);
        for (String currentPath : _paths) {
            String path = currentPath.replace(LANGUAGE_PLACEHOLDER, locale.getLanguage());
            if (hierarchyManager.isExist(path)) {
                try {
                    Content messageContent = hierarchyManager.getContent(path);
                    if (messageContent.hasNodeData(code)) {
                        message = NodeDataUtil.getValueString(messageContent.getNodeData(code));
                        break;
                    }
                } catch (RepositoryException e) {
                    LOGGER.error("Message lookup failed, path:" + path, e);
                }
            }
        }
        return message;
    }
}