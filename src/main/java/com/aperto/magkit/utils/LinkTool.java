package com.aperto.magkit.utils;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.link.LinkHelper;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.cms.util.LinkUtil;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.dms.beans.Document;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import static org.apache.commons.lang.StringUtils.isNotEmpty;
import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.codec.EncoderException;
import org.apache.log4j.Logger;
import javax.jcr.PropertyType;
import static java.util.Locale.ENGLISH;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.net.URLEncoder;
import java.io.UnsupportedEncodingException;

/**
 * Helper class for links.
 *
 * @author Rainer Blumenthal (13.02.2007), Frank Sommer (25.10.2007)
 */
public final class LinkTool {
    private static final Logger LOGGER = Logger.getLogger(LinkTool.class);
    private static final String DMS_REPOSITORY = "dms";
    public static final Pattern UUID_PATTERN = Pattern.compile("^[-a-z0-9]{30,40}$");
    private static final char SLASH = '/';
    private static final URLCodec URL_ENCODER = new URLCodec("UTF-8");

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
     * For document links to the dms module the encoded file name of the document will be used.
     *
     * @param link the link or uuid, in case of uuid it converts it to a absolute link
     * @param addExtension if true .html as added at the end of the link, only adds .html if there is no .htm or .html already
     * @param alternativeRepository which were checked, too.
     * @return the handle with optional .html
     */
    public static String convertLink(String link, boolean addExtension, String alternativeRepository) {
        String newLink = StringUtils.EMPTY;
        String extension = LinkUtil.DEFAULT_EXTENSION;
        if (isNotEmpty(link)) {
            try {
                String path = StringUtils.EMPTY;
                String handle = LinkHelper.convertUUIDtoHandle(link, ContentRepository.WEBSITE);
                if (handle == null && !StringUtils.isBlank(alternativeRepository)) {
                    handle = LinkHelper.convertUUIDtoHandle(link, alternativeRepository);
                    if (handle != null) {
                        StringBuilder dmsHandle = new StringBuilder(10);
                        dmsHandle.append(SLASH).append(alternativeRepository).append(handle);
                        // in dms the file name is additional nessecary
                        if (DMS_REPOSITORY.equalsIgnoreCase(alternativeRepository) && addExtension) {
                            Document doc = new Document(ContentUtil.getContent(DMS_REPOSITORY, handle));
                            dmsHandle.append(SLASH).append(doc.getEncodedFileName());
                            extension = doc.getFileExtension();
                        }
                        handle = dmsHandle.toString();
                    }
                }
                if (isNotEmpty(handle)) {
                    path = handle;
                }
                newLink = determineNewLink(path, link);
            } catch (NullPointerException e) {
                // should only occur in unit tests if the mgnlContext is not present
                newLink = isUuid(link) ? StringUtils.EMPTY : link;
            }
        }
        if (StringUtils.isNotBlank(newLink) && addExtension && !hasHtmlExtension(newLink)) {
            newLink += "." + extension;
        }
        return newLink;
    }

    private static boolean hasHtmlExtension(String link) {
        String lowerCaseLink = link.toLowerCase(ENGLISH);
        return lowerCaseLink.endsWith(".html") || lowerCaseLink.endsWith(".htm");
    }

    private static String determineNewLink(String path, String link) {
        return StringUtils.isBlank(path) ? (isUuid(link) ? StringUtils.EMPTY : link) : path;
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
        boolean result;
        HierarchyManager hm = MgnlContext.getHierarchyManager(ContentRepository.WEBSITE);
        result = isNotEmpty(link) && hm.isExist(link);
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
                if (StringUtils.isNotBlank(repository)) {
                    binaryLink.append(SLASH).append(repository);
                }
                String fileName = binaryNode.getAttribute("fileName");
                String extension = binaryNode.getAttribute("extension");
                try {
                    binaryLink.append(binaryNode.getHandle()).append(SLASH).append(urlEncode(fileName)).append('.').append(extension);
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException("Cannot url encode filename '" + fileName + "' with encoding 'UTF-8'", e);
                }
            } else {
                LOGGER.info("Given NodeData is not from type binary: " + binaryNode.getHandle());
            }
        } else {
            LOGGER.info("Given NodeData was null or does not exist.");
        }
        return binaryLink.toString();
    }

    /**
     * URL encodes the passed String using the code from info.magnolia.module.dms.beans.Document.
     * @param s the string to be encoded
     * @return a new URL encoded String or an empty String if the parameter s has been NULL.
     * @throws UnsupportedEncodingException if encoding fails for encoding 'UTF-8'
     */
    public static String urlEncode(String s) throws UnsupportedEncodingException {
        String name = StringUtils.EMPTY;
        if (s != null) {
            // from magnolia Document class:
            name = StringUtils.replaceChars(s, "ÇÈ<>\"'/\\", "________");
            name = URLEncoder.encode(name, "UTF-8");
            name = StringUtils.replace(name, "+", "%20");
        }
        return name;
//        return URL_ENCODER.encode(s);
    }

    /**
     * Inserts a selector in the given link.
     */
    public static String insertSelector(String link, String selector) {
        String newLink = link;
        if (StringUtils.isNotBlank(selector) && !LinkHelper.isExternalLinkOrAnchor(link)) {
            String[] pathParts = StringUtils.split(link, SLASH);
            String lastPart = pathParts[pathParts.length - 1];
            pathParts = (String[]) ArrayUtils.remove(pathParts, pathParts.length - 1);
            String[] parts = StringUtils.split(lastPart, '.');
            String[] newParts = new String[parts.length + 1];
            newParts[0] = parts[0];
            newParts[1] = selector;
            System.arraycopy(parts, 1, newParts, 2, newParts.length - 2);
            newLink = SLASH + StringUtils.join(pathParts, SLASH) + SLASH;
            newLink += StringUtils.join(newParts, '.');
        }
        return newLink;
    }

    private LinkTool() {
    }
}