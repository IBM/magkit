package com.aperto.magkit.taglib;

import com.aperto.magkit.beans.DocumentInfo;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.util.ContentUtil;
import info.magnolia.cms.util.Resource;
import info.magnolia.module.dms.beans.Document;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.myfaces.tobago.apt.annotation.BodyContent;
import org.apache.myfaces.tobago.apt.annotation.Tag;
import org.apache.myfaces.tobago.apt.annotation.TagAttribute;
import javax.servlet.ServletRequest;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * This Tag saves several information from a dms-document in the request.
 * The commited nodedata must be a UUID to a dms-document.
 * Saves a DocumentInfo-Object with the attributes fileSize, fileName, fileExtention and fileModificationDate in request.
 * The default filesizeunit is kb, other can be set with the fileSize attribute ("byte" or "mb").
 *
 * @author diana racho (28.04.2008)
 */
@Tag(name = "documentInfo", bodyContent = BodyContent.JSP)
public class DocumentInfoTag extends TagSupport {
    private static final Logger LOGGER = Logger.getLogger(DocumentInfoTag.class);

    private String _nodeDataName;
    private String _fileSize;

    @TagAttribute(required = true)
    public void setNodeDataName(String nodeDataName) {
        _nodeDataName = nodeDataName;
    }

    @TagAttribute(required = false)
    public void setFileSize(String fileSize) {
        _fileSize = fileSize;
    }

    /**
     * Saves several information from a dms-document in request.
     *
     * @return jsp output
     * @throws javax.servlet.jsp.JspException
     */
    public int doEndTag() throws JspException {
        ServletRequest request = pageContext.getRequest();
        String link = Resource.getLocalContentNode().getNodeData(_nodeDataName).getString();
        if (!StringUtils.isBlank(link)) {
            Content node = retrieveContent(link);
            if (node != null) {
                Document doc = new Document(node);
                int divisor = 1024;
                if (!StringUtils.isBlank(_fileSize)) {
                    if (StringUtils.equals(_fileSize.toLowerCase(), "mb")) {
                        divisor = 1024 * 1024;
                    }
                    if (StringUtils.equals(_fileSize.toLowerCase(), "byte")) {
                        divisor = 1;
                    }
                }
                DocumentInfo documentInfo = new DocumentInfo();
                documentInfo.setFileExtension(doc.getFileExtension());
                if (doc.getNode().hasMetaData()) {
                    documentInfo.setFileModificationDate(doc.getModificationDate());
                }
                documentInfo.setFileName(doc.getFileName());
                documentInfo.setFileSize((doc.getFileSize() / divisor));
                request.setAttribute("documentInfo", documentInfo);
            } else {
                LOGGER.error("NodeData is not a uuid to a dms-document");
            }
        } else {
            LOGGER.error("NodeData is not valid");
        }
        return super.doEndTag();
    }

    protected Content retrieveContent(String link) {
        return ContentUtil.getContentByUUID("dms", link);
    }
}