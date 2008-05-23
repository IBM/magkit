package com.aperto.magkit.utils;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.cms.util.LinkUtil;
import info.magnolia.context.MgnlContext;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import javax.jcr.PropertyType;
import static java.util.Locale.ENGLISH;

/**
 * Helper class for links.
 *
 * @author Rainer Blumenthal (13.02.2007), Frank Sommer (25.10.2007)
 */
public final class LinkTool {
    private static final Logger LOGGER = Logger.getLogger(LinkTool.class);

    /**
     * Returns absolutePath-Link nevertheless if u give it a "uuidLink" or a "link".
     * <p/>
     * - works for "internal" and "external" Links, cause makeAbsolutePathFromUUID() returns "null" if
     * it gets a non-uuid (an external Link) and "null" causes the given String to be returned
     * - usage e.g.: String link = LinkTool.convertLink(Resource.getLocalContentNode(request).getNodeData("linkTeaserLink1").getString());
     *
     * @param link the uuid or normal external link
     * @return the converted link
     */
    public static String convertLink(String link) {
        return convertLink(link, false);
    }

    /**
     * Returns an absolute path link nevertheless if u give it a uuidLink or normal link with optional added .html.
     *
     * @param link the link or uuid, in case of uuid it converts it to a absolute link
     * @param addExtension if true .html as added at the end of the link, only adds .html if there is no .htm or .html already
     * @return the absolute link with optional .html
     */
    public static String convertLink(String link, boolean addExtension) {
        return convertLink(link, addExtension, null);
    }

    /**
     * Returns a handle nevertheless if you give it a uuidLink or normal link with optional added .html.
     *
     * @param link the link or uuid, in case of uuid it converts it to a absolute link
     * @param addExtension if true .html as added at the end of the link, only adds .html if there is no .htm or .html already
     * @param alternativeRepository which were checked, too.
     * @return the handle with optional .html
     */
    public static String convertLink(String link, boolean addExtension, String alternativeRepository) {
        String newLink = "";
        boolean withExtension = addExtension;
        String extension = LinkUtil.DEFAULT_EXTENSION;
        if (StringUtils.isNotEmpty(link)) {
            try {
                String path = null;
                Content content = ContentUtil.getContentByUUID(ContentRepository.WEBSITE, link);
                if (content == null && !StringUtils.isBlank(alternativeRepository)) {
                    content = ContentUtil.getContentByUUID(alternativeRepository, link);
                    if (content != null) {
                        path = "/" + alternativeRepository + content.getHandle();
                        if ("dms".equalsIgnoreCase(alternativeRepository)) {
                            // dms servlet makes filename and extension by itself 
                            withExtension = false;
                        }
                    }
                } else {
                    path = content.getHandle();
                }
                newLink = StringUtils.defaultString(path, link);
            } catch (NullPointerException e) {
                // should only occur in unit tests if the mgnlContext is not present
                newLink = link;
            }
        }
        if (withExtension && !newLink.toLowerCase(ENGLISH).endsWith(".html") && !newLink.toLowerCase(ENGLISH).endsWith(".htm")) {
            newLink += "." + extension;
        }

        return newLink;
    }

    /**
     * Method checks whether an internal link is broken or not it is not usable for external Links.
     *
     * @param link the link to check
     * @return true if the link is present, false if broken
     */
    public static boolean checkLink(String link) {
        boolean result;
        HierarchyManager hm = MgnlContext.getHierarchyManager(ContentRepository.WEBSITE);
        result = StringUtils.isNotEmpty(link) && hm.isExist(link);
        return result;
    }

    /**
     * Gets the url of the content and adds .html if not already present.
     *
     * @param content the content to get the url for
     * @return the url as String
     */
    public static String getUrl(Content content) {
        String url = content.getHandle();
        if (!url.endsWith(".html") && !url.endsWith(".htm")) {
            url += "." + LinkUtil.DEFAULT_EXTENSION;
        }
        return url;
    }

    /**
     * Method build the link to a binary file which is given as NodeData.
     *
     * @param binaryNode binary NodeData
     * @return link to a binary
     */
    public static String getBinaryLink(NodeData binaryNode) {
        return getBinaryLink(binaryNode, "");
    }

    /**
     * Method build the link to a binary file which is given as NodeData and the repository.
     *
     * @param binaryNode binary NodeData
     * @return link to a binary
     */
    public static String getBinaryLink(NodeData binaryNode, String repository) {
        String binaryLink = "";
        if (binaryNode != null && binaryNode.isExist()) {
            if (binaryNode.getType() == PropertyType.BINARY) {
                binaryLink = binaryNode.getHandle() + '/' + binaryNode.getAttribute("fileName") + '.' + binaryNode.getAttribute("extension");
                if (!StringUtils.isBlank(repository)) {
                    binaryLink = "/" + repository + binaryLink;    
                }
            } else {
                LOGGER.info("Given NodeData is not from type binary: " + binaryNode.getHandle());
            }
        } else {
            LOGGER.info("Given NodeData was null or does not exist.");
        }
        return binaryLink;
    }

    private LinkTool() {
    }
}