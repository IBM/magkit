package com.aperto.magkit.i18n;

import info.magnolia.cms.i18n.DefaultMessagesManager;
import info.magnolia.cms.i18n.Messages;
import info.magnolia.cms.i18n.MessagesChain;
import info.magnolia.context.SystemContext;
import info.magnolia.jcr.node2bean.Node2BeanProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

import static com.aperto.magkit.utils.PropertyUtils.retrieveOrderedMultiSelectValues;
import static info.magnolia.repository.RepositoryConstants.CONFIG;

/**
 * Delivers all configurated basenames under /server/i18n/content/i18nBasenames for all basenames which matchs pattern PATTERN_MESSAGES and PATTERN_DIALOGS.
 * The ExtendedMessagesManager class must be specified in magnolia.properties under info.magnolia.cms.i18n.MessagesManager.
 *
 * @author Achim.Herbertz, diana.racho, frank.sommer
 */
public class ExtendedMessagesManager extends DefaultMessagesManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExtendedMessagesManager.class);

    private static final String PATTERN_MESSAGES = "com.aperto.+.messages";
    public static final String SERVER_I18N_CONTENT = "/server/i18n/content";
    public static final String CN_BASENAMES = "i18nBasenames";
    public static final String SERVER_I18N_BASENAMES = SERVER_I18N_CONTENT + "/" + CN_BASENAMES;

    private SystemContext _systemContext;

    @Inject
    public ExtendedMessagesManager(Node2BeanProcessor nodeToBean) {
        super(nodeToBean);
    }

    @Override
    protected Messages newMessages(MessagesID messagesId) {
        Messages result = null;
        String[] basenames = null;
        if (messagesId != null && messagesId.getBasename() != null) {
            if (isProjectBasename(messagesId.getBasename())) {
                Set<String> basenameSet = retrieveBasenames();
                if (!basenameSet.contains(messagesId.getBasename())) {
                    // put configured message id at first place
                    Set<String> tempSet = new LinkedHashSet<String>();
                    tempSet.add(messagesId.getBasename());
                    tempSet.addAll(basenameSet);
                    basenameSet = tempSet;
                }
                basenames = basenameSet.toArray(new String[basenameSet.size()]);
            } else {
                basenames = new String[]{messagesId.getBasename()};
            }
        }
        if (basenames != null) {
            if (basenames.length == 1) {
                result = getMessagesWithDefaultLocale(basenames[0], messagesId.getLocale());
            } else {
                for (String bn : basenames) {
                    if (result == null) {
                        result = getMessagesWithDefaultLocale(bn, messagesId.getLocale());
                    } else {
                        result = new MessagesChain(result).chain(getMessagesWithDefaultLocale(bn, messagesId.getLocale()));
                    }
                }
            }
        }
        return result;
    }

    protected boolean isProjectBasename(String basename) {
        return basename.matches(PATTERN_MESSAGES);
    }

    protected Set<String> retrieveBasenames() {
        Set<String> basenames = new LinkedHashSet<String>();

        try {
            Session jcrSession = _systemContext.getJCRSession(CONFIG);
            if (jcrSession.nodeExists(SERVER_I18N_BASENAMES)) {
                Node basenamesNode = jcrSession.getNode(SERVER_I18N_BASENAMES);
                basenames.addAll(retrieveOrderedMultiSelectValues(basenamesNode));
            }
        } catch (RepositoryException e) {
            LOGGER.error("Error retrieving i18n basenames from i18n config.", e);
        }

        return basenames;
    }

    private Messages getMessagesWithDefaultLocale(String basename, Locale locale) {
        Messages msgs = new ExtendedMessagesImpl(basename, locale);
        if (!getDefaultLocale().equals(locale)) {
            msgs = new MessagesChain(msgs).chain(new ExtendedMessagesImpl(basename, getDefaultLocale()));
        }
        return msgs;
    }

    @Inject
    public void setSystemContext(final SystemContext systemContext) {
        _systemContext = systemContext;
    }
}
