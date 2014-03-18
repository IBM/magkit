package com.aperto.magkit.controls;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.gui.control.Button;
import info.magnolia.cms.gui.control.Select;
import info.magnolia.cms.gui.control.SelectOption;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.cms.util.NodeDataUtil;
import info.magnolia.freemarker.FreemarkerUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;


/**
 * Control to select multiple values. The values can get stored as list, in JSON format or as a multiple values
 *
 * @author Philipp Bracher
 * @version $Revision: 6341 $ ($Author: philipp $)
 */
public class OrderingMultiSelectMultiRepository extends OrderingMultiSelect {

    private static final Logger LOGGER = Logger.getLogger(OrderingMultiSelectMultiRepository.class);

    /**
     * Gets repository path.
     *
     * @return Current repository path.
     */
    public String getRepository() {
        String result = super.getRepository();
        for (SelectOption option : (List<SelectOption>) getOptions().iterator()) {
            if (option.getSelected()) {
                result = option.getValue();
            }
        }
        return result;
    }

    /**
     * If this control has a choose button.
     */
    public String getChooseButton() {
        String chooseOnclick = "mgnlOpenTreeBrowserWithControl($('${prefix}'), document.getElementById('${prefix}repositorySelect').value);";
        Button choose = new Button();
        choose.setLabel(getMessage("buttons.choose"));
        choose.setOnclick(chooseOnclick);
        choose.setSmall(true);
        return choose.getHtml();
    }


    private void setOptions(Content configNode, String repository) {
        List options = new ArrayList();
        try {
            Iterator it = getOptionNodes(configNode).iterator();
            while (it.hasNext()) {
                Content n = (Content) it.next();
                String valueNodeData = getConfigValue("valueNodeData", "value");
                String labelNodeData = getConfigValue("labelNodeData", "label");

                String value = NodeDataUtil.getString(n, valueNodeData);
                String label = NodeDataUtil.getString(n, labelNodeData, value);

                SelectOption option = new SelectOption(label, value);
                if (value.equals(repository)) {
                    option.setSelected(true);
                }
                options.add(option);
            }
        } catch (RepositoryException e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Exception caught: " + e.getMessage(), e);
            }
        }
        setOptions(options);
    }

    protected Collection getOptionNodes(Content configNode) throws RepositoryException {
        Collection result = new ArrayList();
        Content optionsNode = null;

        if (configNode.hasContent("options")) {
            optionsNode = configNode.getContent("options");
        } else {
            String repository = getConfigValue("repository", ContentRepository.WEBSITE);
            String path = getConfigValue("path");
            if (StringUtils.isNotEmpty(path)) {
                optionsNode = ContentUtil.getContent(repository, path);
            }
        }

        if (optionsNode != null) {
            result = ContentUtil.getAllChildren(optionsNode);
        }
        return result;
    }

    /**
     * @see info.magnolia.cms.gui.dialog.DialogControl#init(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, Content, Content)
     */
    public void init(HttpServletRequest request, HttpServletResponse response, Content websiteNode, Content configNode) throws RepositoryException {
        super.init(request, response, websiteNode, configNode);
        String repository = "website";
        if (configNode != null) {
            setOptions(configNode, repository);
        }
    }

    /**
     * If this control has a select field.
     *
     * @return the html-snippet for a select box with magnolia repostiories
     */
    public String getRepositorySelect() {

        Select select = new Select();
        select.setName("${prefix}repositorySelect");
        select.setEvent("onChange", "${prefix}.value='';" + getName() + "DynamicTable.persist();");
        select.setOptions(getOptions());
        select.setSaveInfo(false);
        return select.getHtml();
    }

    /**
     * Called by the template. It renders the dynamic inner row using trimpaths templating mechanism.
     */
    public String getInnerHtml() {
        String name = "/" + StringUtils.replace(OrderingMultiSelectMultiRepository.class.getName(), ".", "/") + "Inner.html";
        Map map = new HashMap();
        map.put("this", this);
        return FreemarkerUtil.process(name, map);
    }
}