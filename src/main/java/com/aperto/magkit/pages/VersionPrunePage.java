package com.aperto.magkit.pages;

import com.aperto.webkit.utils.ExceptionEater;
import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.i18n.Messages;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.admininterface.TemplatedMVCHandler;
import info.magnolia.module.dms.DMSModule;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.PropertyIterator;
import javax.jcr.ReferentialIntegrityException;
import javax.jcr.RepositoryException;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionIterator;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static info.magnolia.cms.util.ContentUtil.path2uuid;
import static info.magnolia.context.MgnlContext.getHierarchyManager;
import static java.lang.String.valueOf;
import static org.apache.commons.lang.StringUtils.*;

/**
 * AdminPage to prune version of Pages.
 *
 * @author oliver.emke, Aperto AG
 * @since 02.09.11
 */
public class VersionPrunePage extends TemplatedMVCHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(VersionPrunePage.class);

    private static final String MESSAGES_BASENAME = "com.aperto.magkit.messages";

    private static final String QUERY_PARAM_REPO = "repo";

    private int _versionCount = 0;
    private String _path = "/";
    private String _repository = ContentRepository.WEBSITE;
    private StringBuilder _resultMessages = new StringBuilder();
    private List<String> _prunedHandles = new ArrayList<String>();


    private static final Map<String, String> AVAILABLE_REPOSITORIES = new HashMap<String, String>() {
        {
            put(DMSModule.getInstance().getRepository(), "DMS");
            put(ContentRepository.WEBSITE, "Website");
        }
    };

    public VersionPrunePage(String name, HttpServletRequest request, HttpServletResponse response) {
        super(name, request, response);
        String repo = MgnlContext.getWebContext().getParameter(QUERY_PARAM_REPO);
        if (AVAILABLE_REPOSITORIES.containsKey(repo)) {
            _repository = repo;
        }
    }

    @Override
    public String execute(String commandName) {
        if (StringUtils.equals("execute", commandName)) {
            try {
                pruneVersions();
            } catch (RepositoryException e) {
                LOGGER.error("Error on prune versions.", e);
            }
            handleResultMessages();
        }
        return VIEW_SHOW;
    }

    private void pruneVersions() throws RepositoryException {
        Content startContent = getStartContent();

        if (startContent != null) {
            try {
                ContentUtil.visit(startContent, new ContentUtil.Visitor() {
                    public void visit(Content node) throws RepositoryException {
                        LOGGER.debug("Check node handle: [" + node.getHandle() + "] and index [" + node.getIndex() + "].");
                        if (node.getLevel() > 0) {
                            handleNode(node);
                        }
                    }
                });
            } catch (Exception e) {
                LOGGER.error("Error on prune versions.", e);
            }
        } else {
            _resultMessages.append("'").append(getPath()).append("' ").append(getMessages().get("versionPrune.path.wrong"));
        }
    }

    private void handleNode(Content node) {
        LOGGER.debug("Check node with uuid [" + node.getUUID() + "].");

        VersionHistory versionHistory = getVersionHistory(node);
        if (versionHistory != null) {
            VersionIterator allVersions = getAllVersions(node, versionHistory);
            if (allVersions != null) {
                long indexToRemove = getIndexToRemove(allVersions);

                if (indexToRemove > 0) {
                    // skip root version
                    allVersions.nextVersion();
                    // remove the version after rootVersion
                    while (indexToRemove > 0) {
                        Version currentVersion = allVersions.nextVersion();
                        String versionNameToRemove = getVersionName(currentVersion);
                        String errorMessage = EMPTY;
                        try {
                            versionHistory.removeVersion(versionNameToRemove);
                        } catch (UnsupportedRepositoryOperationException e) {
                            errorMessage = MessageFormat.format("Unversionable node with uuid [{0}].", node.getUUID());
                            LOGGER.warn(errorMessage, e);
                        } catch (ReferentialIntegrityException e) {
                            errorMessage = MessageFormat.format("Node with Handle [{0}] and VersionNumber [{1}] is referenced by [{2}] - uuid: [{3}]",
                                    node.getHandle(), getVersionName(currentVersion), getReferences(currentVersion), node.getUUID());
                            LOGGER.warn(errorMessage, e);
                        } catch (Exception e) {
                            errorMessage = MessageFormat.format("Unable to perform a versioning operation on node with uuid [{0}].", node.getUUID());
                            LOGGER.warn(errorMessage, e);
                        }

                        if (isEmpty(errorMessage)) {
                            String info = "Handle: " + node.getHandle() + ", Version: " + versionNameToRemove;
                            LOGGER.info("Removed version [{}].", info);
                            _prunedHandles.add(info);
                        } else {
                            _resultMessages.append(errorMessage).append("\n");
                        }
                        indexToRemove--;
                    }
                }
            }
        }
    }

    private long getIndexToRemove(VersionIterator allVersions) {
        // size - 2 to skip root version
        return (allVersions.getSize() - 2) - ((getVersionCount() > 0 ? getVersionCount() - 1 : 0));
    }

    private VersionHistory getVersionHistory(Content node) {
        VersionHistory versionHistory = null;
        try {
            versionHistory = node.getVersionHistory();
        } catch (RepositoryException e) {
            String message = MessageFormat.format("Unable to get versionsHistory from node with handle [{0}].", node.getHandle());
            LOGGER.error(message, e);
            _resultMessages.append(message).append("\n");
        }
        return versionHistory;
    }

    private VersionIterator getAllVersions(Content node, VersionHistory versionHistory) {
        VersionIterator allVersions = null;

        try {
            allVersions = versionHistory.getAllVersions();
        } catch (RepositoryException e) {
            String message = MessageFormat.format("Unable to get all versions from node with handle [{0}].", node.getHandle());
            LOGGER.error(message, e);
            _resultMessages.append(message).append("\n");
        }
        return allVersions;
    }

    private Content getStartContent() throws RepositoryException {
        Content startContent;
        if (isNotBlank(getPath()) && !StringUtils.equals("/", getPath())) {
            String uuid = path2uuid(getRepository(), getPath());
            startContent = getHierarchyManager(getRepository()).getContentByUUID(uuid);
        } else {
            startContent = getHierarchyManager(getRepository()).getRoot();
        }
        return startContent;
    }

    private String getVersionName(Version version) {
        String returnValue = EMPTY;
        if (version != null) {
            try {
                returnValue = version.getName();
            } catch (RepositoryException e) {
                ExceptionEater.eat(e);
            }
        }
        return returnValue;
    }

    private String getReferences(Version version) {
        String returnValue = EMPTY;
        if (version != null) {
            try {
                PropertyIterator references = version.getReferences();
                if (references != null) {
                    returnValue = references.toString();
                }
            } catch (RepositoryException e) {
                ExceptionEater.eat(e);
            }
        }
        return returnValue;
    }

    private void handleResultMessages() {
        if (_prunedHandles.isEmpty()) {
            if (getVersionCount() > 0) {
                _resultMessages.append(getSpacerRow());
                _resultMessages.append(getMessages().get("versionPrune.nothingPruned.prefix")).append(" ");
                _resultMessages.append(valueOf(getVersionCount()));
                _resultMessages.append(" ").append(getMessages().get("versionPrune.nothingPruned.postfix"));
                _resultMessages.append(getSpacerRow());
            } else {
                _resultMessages.append(getSpacerRow());
                _resultMessages.append(getMessages().get("versionPrune.nothingPruned"));
                _resultMessages.append(getSpacerRow());
            }
        } else {
            _resultMessages.append(getSuccessfulPrunedMessage());
            for (String handle : _prunedHandles) {
                _resultMessages.append(handle).append("\n");
            }
            _resultMessages.append(getSuccessfulPrunedMessage());
        }
    }

    private String getSuccessfulPrunedMessage() {
        StringBuilder successfulPrunedMessage = new StringBuilder();
        successfulPrunedMessage.append(getSpacerRow());
        successfulPrunedMessage.append(valueOf(_prunedHandles.size())).append(" ");
        successfulPrunedMessage.append(getMessages().get("versionPrune.pruned")).append("\n");
        successfulPrunedMessage.append(getSpacerRow());
        return successfulPrunedMessage.toString();
    }

    private String getSpacerRow() {
        return "\n\n-------------------------------------------------------------\n";
    }

    public Map<String, String> getAvailableRepositories() {
        return AVAILABLE_REPOSITORIES;
    }

    public int getVersionCount() {
        return _versionCount;
    }

    public void setVersionCount(int versionCount) {
        _versionCount = versionCount;
    }

    public String getPath() {
        return _path;
    }

    public void setPath(String path) {
        _path = path;
    }

    public String getRepository() {
        return _repository;
    }

    public String getResultMessages() {
        return _resultMessages.toString();
    }

    public Messages getMessages() {
        return MessagesManager.getMessages(MESSAGES_BASENAME);
    }
}