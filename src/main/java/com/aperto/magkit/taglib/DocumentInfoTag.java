package com.aperto.magkit.taglib;

import com.aperto.magkit.beans.DocumentInfo;
import info.magnolia.cms.core.Content;
import static info.magnolia.cms.util.ContentUtil.getContentByUUID;
import static info.magnolia.context.MgnlContext.getAggregationState;
import info.magnolia.module.dms.beans.Document;
import static org.apache.commons.lang.StringUtils.isBlank;
import org.apache.myfaces.tobago.apt.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

/**
 * This Tag saves several information from a dms-document in the request.
 * The commited nodedata must be a UUID to a dms-document.
 * Saves a DocumentInfo-Object with the attributes fileSize, fileName, fileExtention and fileModificationDate in request.
 * The default filesizeunit is kb, other can be set with the fileSize attribute ("byte" or "mb").
 *
 * @author diana racho
 * @since 28.04.2008
 */
@Tag(name = "documentInfo", bodyContent = BodyContent.JSP)
public class DocumentInfoTag extends TagSupport {
    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentInfoTag.class);
    private static final String ND_SUBJECT = "subject";
    private static final String ND_TITLE = "title";

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
        String link = getAggregationState().getCurrentContent().getNodeData(_nodeDataName).getString();
        if (!isBlank(link)) {
            Content node = retrieveContent(link);
            if (node != null) {
                Document doc = new Document(node);
                int divisor = 1024;
                if (!isBlank(_fileSize)) {
                    if ("mb".equalsIgnoreCase(_fileSize)) {
                        divisor = 1024 * 1024;
                    }
                    if ("byte".equalsIgnoreCase(_fileSize)) {
                        divisor = 1;
                    }
                }
                DocumentInfo documentInfo = new DocumentInfo();
                documentInfo.setFileExtension(doc.getFileExtension());
                if (doc.getNode().hasMetaData()) {
                    documentInfo.setFileModificationDate(doc.getModificationDate());
                }
                documentInfo.setFileName(doc.getFileName());
                long fileSize = 0;
                try {
                    fileSize = doc.getFileSize();
                } catch (NumberFormatException e) {
                    LOGGER.info(e.getLocalizedMessage());
                }
                documentInfo.setFileSize((fileSize / divisor));
                determineSubject(doc, documentInfo);
                pageContext.setAttribute("documentInfo", documentInfo);
            } else {
                LOGGER.info("NodeData is not a uuid to a dms-document");
            }
        } else {
            LOGGER.info("NodeData is not valid");
        }
        return super.doEndTag();
    }

    private void determineSubject(Document doc, DocumentInfo documentInfo) {
        try {
            Content content = doc.getNode();
            if (content.hasNodeData(ND_SUBJECT)) {
                documentInfo.setFileSubject(content.getNodeData(ND_SUBJECT).getString());
            } else {
                documentInfo.setFileSubject(content.getNodeData(ND_TITLE).getString());
            }
        } catch (RepositoryException e) {
            LOGGER.error("Error", e);
        }
    }

    protected Content retrieveContent(String link) {
        return getContentByUUID("dms", link);
    }
}