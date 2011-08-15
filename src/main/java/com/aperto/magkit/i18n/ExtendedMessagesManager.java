package com.aperto.magkit.i18n;

import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.i18n.DefaultMessagesManager;
import info.magnolia.cms.i18n.Messages;
import info.magnolia.cms.i18n.MessagesChain;
import info.magnolia.context.MgnlContext;
import org.apache.log4j.Logger;

import javax.jcr.RepositoryException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import static info.magnolia.cms.beans.config.ContentRepository.CONFIG;

/**
 * Delivers all configurated basenames under /server/i18n/content/i18nBasenames for all basenames which matchs pattern PATTERN_MESSAGES and PATTERN_DIALOGS.
 * The ExtendedMessagesManager class must be specified in magnolia.proeprties under info.magnolia.cms.i18n.MessagesManager.
 *
 * @author Achim.Herbertz, diana.racho (Aperto AG)
 */
public class ExtendedMessagesManager extends DefaultMessagesManager {

    private static final Logger LOGGER = Logger.getLogger(ExtendedMessagesManager.class);

    private static final String PATTERN_MESSAGES = "com.aperto.+.messages";
    private static final String PATTERN_DIALOGS = "com.aperto.+.dialogs";
    public static final String SERVER_I18N_CONTENT = "/server/i18n/content";
    public static final String CN_BASENAMES = "i18nBasenames";
    public static final String SERVER_I18N_BASENAMES = SERVER_I18N_CONTENT + "/" + CN_BASENAMES;

    @Override
    protected Messages newMessages(MessagesID messagesId) {
        Messages result = null;
        String[] basenames = null;
        if (messagesId != null && messagesId.getBasename() != null) {
            if (isProjectBasename(messagesId.getBasename())) {
                Set<String> basenamesList = retrieveBasenames();
                if (!basenamesList.contains(messagesId.getBasename())) {
                    basenamesList.add(messagesId.getBasename());
                }
                basenames = basenamesList.toArray(new String[basenamesList.size()]);
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
        return basename.matches(PATTERN_MESSAGES) || basename.matches(PATTERN_DIALOGS);
    }

    protected static Set<String> retrieveBasenames() {
        Set<String> basenames = new HashSet<String>();
        HierarchyManager cfgManager = MgnlContext.getSystemContext().getHierarchyManager(CONFIG);
        if (cfgManager.isExist(SERVER_I18N_BASENAMES)) {
            try {
                Content content = cfgManager.getContent(SERVER_I18N_BASENAMES);
                for (NodeData basename : content.getNodeDataCollection()) {
                    basenames.add(basename.getString());
                }
            } catch (RepositoryException e) {
                LOGGER.error("Error retrieving i18n basenames from i18n config.", e);
            }
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
}