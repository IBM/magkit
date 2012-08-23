package com.aperto.magkit.utils;

import info.magnolia.cms.beans.runtime.FileProperties;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.i18n.I18nContentSupportFactory;
import info.magnolia.cms.util.DateUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import java.util.Date;

/**
 * ContentHelper.
 *
 * TODO: check use and move to ContentUtils
 *
 * @author jds
 */
public final class ContentHelper {
    private static final Logger LOGGER = LoggerFactory.getLogger(ContentHelper.class);
    private static final String DATE_PATTERN = "dd.MM.yyyy";

    public static String getData(Content content, String nodeName) {
        String value = "";
        try {
            if (content.hasNodeData(nodeName)) {
                NodeData node = content.getNodeData(nodeName);
                if (node.getType() == PropertyType.BINARY) {
                    FileProperties props = new FileProperties(content, node.getName());
                    value = props.getProperty(StringUtils.EMPTY);
                } else if (node.getType() == PropertyType.DATE) {
                    Date date = content.getNodeData(nodeName).getDate().getTime();
                    value = DateUtil.format(date, DATE_PATTERN, I18nContentSupportFactory.getI18nSupport().getLocale());
                } else {
                    value = node.getString();
                }
            }
        } catch (RepositoryException e) {
            LOGGER.info("Context " + nodeName + " don't exist.");
        }
        return value;
    }

    private ContentHelper() {
    }
}