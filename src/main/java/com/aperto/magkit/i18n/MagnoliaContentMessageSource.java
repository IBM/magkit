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
 * '/config/en/i18n/foo'. A fallback to a default language is not supported.
 *
 * TODO: add caching of resolved messages; requires content change listener
 *
 * @author Norman Wiechmann, Aperto AG (07-22-2008)
 */
public class MagnoliaContentMessageSource extends AbstractMessageSource {
    private static final Logger LOGGER = Logger.getLogger(MagnoliaContentMessageSource.class);
    private static final String LANG_PLACEHOLDER = "XX";
    private String[] _paths;
    private boolean _languageAware = true;
    private String _repositoryId = ContentRepository.WEBSITE;
    private String _configPrefix = "/config";

    /**
     * Sets a single content repository path for lookup message codes.
     */
    public void setPath(final String path) {
        setPaths(path);
    }

    /**
     * Sets multiple content repository paths for lookup message codes.
     */
    public void setPaths(final String... paths) {
        _paths = paths;
        if (_paths != null) {
            for (int i = 0; i < _paths.length; i++) {
                // normalize path; it must start with a '/'
                if (!_paths[i].startsWith("/")) {
                    _paths[i] = "/" + _paths[i];
                }
                // add language placeholder if need
                if (_languageAware) {
                    _paths[i] = "/" + LANG_PLACEHOLDER + _paths[i];
                }
                // add config area prefix
                _paths[i] = _configPrefix + _paths[i];
            }
        }
    }

    public void setConfigPrefix(String configPrefix) {
        _configPrefix = configPrefix;
    }

    /**
     * Set to {@code false} to disable support of language code specific part within content repository path.
     * Default is {@code true}.
     * <p/>
     * E.g. if a path of '/i18n/foo' is configured and current locale is 'en', with language awareness enabled the path
     * will be resolved to '/config/en/i18n/foo'. Without language awareness it will be resolved to '/config/i18n/foo'.
     */
    public void setLanguageAware(final boolean languageAware) {
        _languageAware = languageAware;
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
            String path = currentPath;
            if (_languageAware) {
                path = currentPath.replace(LANG_PLACEHOLDER, locale.getLanguage());
            }
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