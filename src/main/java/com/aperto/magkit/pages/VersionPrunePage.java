package com.aperto.magkit.pages;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.i18n.Messages;
import info.magnolia.cms.i18n.MessagesManager;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.admininterface.TemplatedMVCHandler;
import info.magnolia.module.dms.DMSModule;
import info.magnolia.module.templating.MagnoliaTemplatingUtilities;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.UnsupportedRepositoryOperationException;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionIterator;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        try {
            if (StringUtils.equals("execute", commandName)) {
                pruneVersions();
                if (StringUtils.isEmpty(_resultMessages.toString())) {
                    handleMessages();
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error on prune versions.", e);
        }
        return VIEW_SHOW;
    }

    private void pruneVersions() throws Exception {
        Content startContent;
        if (StringUtils.isNotBlank(getPath()) && !StringUtils.equals("/", getPath())) {
            String uuid = ContentUtil.path2uuid(getRepository(), getPath());
            startContent = MgnlContext.getHierarchyManager(getRepository()).getContentByUUID(uuid);
        } else {
            startContent = MgnlContext.getHierarchyManager(getRepository()).getRoot();
        }

        if (startContent != null) {
            ContentUtil.visit(startContent, new ContentUtil.Visitor() {
                @Override
                public void visit(Content node) throws RepositoryException {
                    LOGGER.debug("Check node handle: [" + node.getHandle() + "] and index [" + node.getIndex() + "].");
                    if (node.getLevel() > 0) {
                        LOGGER.debug("Check node with uuid [" + node.getUUID() + "].");
                        try {
                            VersionHistory versionHistory = node.getVersionHistory();
                            if (versionHistory != null) {
                                VersionIterator allVersions = versionHistory.getAllVersions();
                                // size - 2 to skip root version
                                long indexToRemove = (allVersions.getSize() - 2) - ((getVersionCount() > 0 ? getVersionCount() - 1 :  0));
                                if (indexToRemove > 0) {
                                    // skip root version
                                    allVersions.nextVersion();
                                    // remove the version after rootVersion
                                    while (indexToRemove > 0) {
                                        String versionNameToRemove = allVersions.nextVersion().getName();
                                        versionHistory.removeVersion(versionNameToRemove);
                                        String info = "Handle: " + node.getHandle() + ", Version: " + versionNameToRemove;
                                        LOGGER.info("Removed version [{}].", info);
                                        _prunedHandles.add(info);
                                        indexToRemove--;
                                    }
                                }
                            }
                        } catch (UnsupportedRepositoryOperationException e) {
                            LOGGER.debug("Unversionable node with uuid [{}].", node.getUUID());
                        }
                    }
                }
            });
        } else {
            _resultMessages.append("'").append(getPath()).append("' ").append(getMessages().get("versionPrune.path.wrong"));
        }
    }

    private void handleMessages() {
        if (_prunedHandles.isEmpty()) {
            if (getVersionCount() > 0) {
                _resultMessages.append(getMessages().get("versionPrune.nothingPruned.prefix")).append(" ");
                _resultMessages.append(String.valueOf(getVersionCount()));
                _resultMessages.append(" ").append(getMessages().get("versionPrune.nothingPruned.postfix"));
            } else {
                _resultMessages.append(getMessages().get("versionPrune.nothingPruned"));
            }
        } else {
            _resultMessages.append(String.valueOf(_prunedHandles.size())).append(" ");
            _resultMessages.append(getMessages().get("versionPrune.pruned")).append("\n");
            for (String handle : _prunedHandles) {
                _resultMessages.append(handle).append("\n");
            }
        }
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