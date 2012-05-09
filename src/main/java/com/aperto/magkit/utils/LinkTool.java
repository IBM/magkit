package com.aperto.magkit.utils;

import static info.magnolia.cms.beans.config.ContentRepository.WEBSITE;
import info.magnolia.cms.core.*;
import info.magnolia.cms.util.ContentUtil;
import static info.magnolia.context.MgnlContext.getHierarchyManager;
import info.magnolia.link.LinkUtil;
import static info.magnolia.link.LinkUtil.DEFAULT_EXTENSION;
import static info.magnolia.link.LinkUtil.isExternalLinkOrAnchor;
import info.magnolia.module.dms.beans.Document;
import static org.apache.commons.lang.ArrayUtils.remove;
import static org.apache.commons.lang.StringUtils.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.PropertyType;
import java.io.UnsupportedEncodingException;
import static java.net.URLEncoder.encode;
import static java.util.Locale.ENGLISH;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helper class for links.
 *
 * @author Rainer Blumenthal (13.02.2007), Frank Sommer (25.10.2007)
 */
public final class LinkTool {
    private static final Logger LOGGER = LoggerFactory.getLogger(LinkTool.class);
    private static final String DMS_REPOSITORY = "dms";
    public static final Pattern UUID_PATTERN = Pattern.compile("^[-a-z0-9]{30,40}$");
    private static final char SLASH = '/';

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
     * @param link         the link or uuid, in case of uuid it converts it to a absolute link
     * @param addExtension if true .html as added at the end of the link, only adds .html if there is no .htm or .html already
     * @return the absolute link with optional .html
     */
    public static String convertLink(String link, boolean addExtension) {
        return convertLink(link, addExtension, null);
    }

    /**
     * Returns a handle nevertheless if you give it a uuidLink or normal link with optional added .html.
     * For document links to the dms module the encoded file name of the document will be used.
     *
     * @param link                  the link or uuid, in case of uuid it converts it to a absolute link
     * @param addExtension          if true .html as added at the end of the link, only adds .html if there is no .htm or .html already
     * @param alternativeRepository which were checked, too.
     * @return the handle with optional .html
     */
    public static String convertLink(String link, boolean addExtension, String alternativeRepository) {
        String newLink = EMPTY;
        String extension = DEFAULT_EXTENSION;
        if (isNotEmpty(link)) {
            String path = EMPTY;
            if (isUuid(link)) {
                String handle = convertUUIDtoHandle(link, WEBSITE);
                if (handle == null && !isBlank(alternativeRepository)) {
                    handle = convertUUIDtoHandle(link, alternativeRepository);
                    if (handle != null) {
                        StringBuilder dmsHandle = new StringBuilder(32);
                        dmsHandle.append(SLASH).append(alternativeRepository).append(handle);
                        // in dms the file name is additional nessecary
                        if (DMS_REPOSITORY.equalsIgnoreCase(alternativeRepository) && addExtension) {
                            Document doc = new Document(ContentUtil.getContent(DMS_REPOSITORY, handle));
                            dmsHandle.append(SLASH).append(mgnlUrlEncode(doc.getFileName()));
                            extension = doc.getFileExtension();
                        }
                        handle = dmsHandle.toString();
                    }
                }
                if (isNotEmpty(handle)) {
                    path = handle;
                }
            }
            newLink = determineNewLink(path, link);
        }
        if (isNotBlank(newLink) && addExtension && !hasHtmlExtension(newLink)) {
            newLink += "." + extension;
        }
        return newLink;
    }

    /**
     * Api compatibility method for magnolia 4.0 api change.
     * {@link LinkUtil#convertUUIDtoHandle} throws new info.magnolia.link.LinkException class.
     */
    public static String convertUUIDtoHandle(final String link, final String repository) {
        String handle = null;
        try {
            handle = LinkUtil.convertUUIDtoHandle(link, repository);
        } catch (Exception e) {
            LOGGER.warn(e.getMessage(), e);
        }
        return handle;
    }

    private static boolean hasHtmlExtension(String link) {
        String lowerCaseLink = link.toLowerCase(ENGLISH);
        return lowerCaseLink.endsWith(".html") || lowerCaseLink.endsWith(".htm");
    }

    private static String determineNewLink(String path, String link) {
        return isBlank(path) ? (isUuid(link) ? EMPTY : link) : path;
    }

    public static boolean isUuid(String link) {
        boolean isUuid = false;
        Matcher matcher = UUID_PATTERN.matcher(link);
        if (matcher.matches()) {
            isUuid = true;
        }
        return isUuid;
    }

    /**
     * Method checks whether an internal link is broken or not it is not usable for external Links.
     *
     * @param link the link to check
     * @return true if the link is present, false if broken
     */
    public static boolean checkLink(String link) {
        HierarchyManager hm = getHierarchyManager(WEBSITE);
        return isNotEmpty(link) && hm.isExist(link);
    }

    /**
     * Gets the url of the content and adds .html if not already present.
     *
     * @param content the content to get the url for
     * @return the url as String
     */
    public static String getUrl(Content content) {
        String url = content.getHandle();
        if (!hasHtmlExtension(url)) {
            url += "." + DEFAULT_EXTENSION;
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
     * The file name will be URL encoded using apache commons URLCodec and charset "UTF-8".
     *
     * @param binaryNode binary NodeData
     * @param repository the repository name. Should be null or empty for website repository.
     * @return link to a binary
     * @throws RuntimeException if file name could not be url encoded with encoding 'UTF-8'.
     */
    public static String getBinaryLink(NodeData binaryNode, String repository) {
        StringBuilder binaryLink = new StringBuilder(64);
        if (binaryNode != null && binaryNode.isExist()) {
            if (binaryNode.getType() == PropertyType.BINARY) {
                if (isNotBlank(repository)) {
                    binaryLink.append(SLASH).append(repository);
                }
                String fileName = binaryNode.getAttribute("fileName");
                String extension = binaryNode.getAttribute("extension");
                binaryLink.append(binaryNode.getHandle()).append(SLASH).append(mgnlUrlEncode(fileName)).append('.').append(extension);
            } else {
                LOGGER.info("Given NodeData is not from type binary: {}.", binaryNode.getHandle());
            }
        } else {
            LOGGER.info("Given NodeData was null or does not exist.");
        }
        return binaryLink.toString();
    }

    /**
     * URL encodes the passed String using the code from {@link info.magnolia.module.dms.beans.Document}.
     *
     * @param s the string to be encoded
     * @return a new URL encoded String or an empty String if the parameter s has been NULL.
     * @throws UnsupportedEncodingException if encoding fails for encoding 'UTF-8'
     */
    public static String mgnlUrlEncode(String s) {
        String name = EMPTY;
        try {
            if (s != null) {
                // from magnolia Document class:
                name = replaceChars(s, "\u00AB\u00BB<>\"'/\\", "________");
                name = encode(name, "UTF-8");
                name = replace(name, "+", "%20");
            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Could not URL encode filename with encoding UTF-8", e);
        }
        return name;
    }

    /**
     * Inserts a selector in the given link.
     */
    public static String insertSelector(String link, String selector) {
        String newLink = link;
        if (isNotBlank(selector) && !isExternalLinkOrAnchor(link)) {
            String[] pathParts = split(link, SLASH);
            String lastPart = pathParts[pathParts.length - 1];
            pathParts = (String[]) remove(pathParts, pathParts.length - 1);
            String[] parts = split(lastPart, '.');
            String[] newParts = new String[parts.length + 1];
            newParts[0] = parts[0];
            newParts[1] = selector;
            System.arraycopy(parts, 1, newParts, 2, newParts.length - 2);
            newLink = SLASH + join(pathParts, SLASH) + SLASH;
            newLink += join(newParts, '.');
        }
        return newLink;
    }

    private LinkTool() {
    }
}