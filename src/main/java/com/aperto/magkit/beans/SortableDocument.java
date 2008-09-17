package com.aperto.magkit.beans;

import info.magnolia.cms.core.Content;
import info.magnolia.module.dms.beans.Document;
import org.apache.log4j.Logger;
import javax.jcr.RepositoryException;
import java.util.Calendar;

/**
 * Extends the Magnolia document with a sort criteria.
 * {@link Document}
 *
 * @author frank.sommer (17.09.2008)
 */
public class SortableDocument extends Document implements Comparable {
    private static final Logger LOGGER = Logger.getLogger(SortableDocument.class);
    private DocumentSortCriteria _sortCriteria = DocumentSortCriteria.title;

    public DocumentSortCriteria getSortCriteria() {
        return _sortCriteria;
    }

    public void setSortCriteria(DocumentSortCriteria sortCriteria) {
        _sortCriteria = sortCriteria;
    }

    /**
     * constructor.
     * @see Document#Constructor(Content)
     */
    public SortableDocument(Content node) {
        super(node);
    }

    /**
     * constructor.
     * @see Document#Constructor(Content, String)
     */
    public SortableDocument(Content node, String version) throws RepositoryException {
        super(node, version);
    }

    /**
     * Implementation of {@link Comparable}.
     * Compares by given sort criteria.
     * @see #_sortCriteria
     */
    public int compareTo(Object o) {
        int compareValue = 0;
        SortableDocument otherDocument = (SortableDocument) o;
        switch(_sortCriteria) {
            case title: {
                compareValue = getTitle().compareToIgnoreCase(otherDocument.getTitle());
                break;
            }
            case modDate: {
                Calendar thisDate = getModificationDate();
                Calendar otherDate = otherDocument.getModificationDate();
                compareValue = compareDates(thisDate, otherDate, otherDocument);
                break;
            }
            case createDate: {
                Calendar thisDate = getNode().getMetaData().getCreationDate();
                Calendar otherDate = otherDocument.getNode().getMetaData().getCreationDate();
                compareValue = compareDates(thisDate, otherDate, otherDocument);
                break;
            }
            case publisher: {
                compareValue = compareNodeData(otherDocument, "publisher");
                break;
            }
            case subject: {
                compareValue = compareNodeData(otherDocument, "subject");
                break;
            }
            case type: {
                compareValue = compareNodeData(otherDocument, "type");
                break;
            }
            default: {
                compareValue = 0;
            }
        }
        return compareValue;
    }

    private int compareDates(Calendar thisDate, Calendar otherDate, SortableDocument otherDocument) {
        int compareValue;
        compareValue = thisDate.equals(otherDate) ? 0 : thisDate.before(otherDate) ? 1 : -1;
        if (compareValue == 0) {
            compareValue = getTitle().compareToIgnoreCase(otherDocument.getTitle());
        }
        return compareValue;
    }

    private int compareNodeData(SortableDocument otherDocument, String nodeName) {
        int compareValue = 0;
        try {
            if (!getNode().hasNodeData(nodeName) && !otherDocument.getNode().hasNodeData(nodeName)) {
                compareValue = 0;
            } else if (!getNode().hasNodeData(nodeName)) {
                compareValue = -1;
            } else if (!otherDocument.getNode().hasNodeData(nodeName)) {
                compareValue = 1;
            } else {
                String thisValue = getNode().getNodeData(nodeName).getString();
                String otherValue = otherDocument.getNode().getNodeData(nodeName).getString();
                compareValue = thisValue.compareToIgnoreCase(otherValue);
            }
        } catch (RepositoryException e) {
            LOGGER.info("Error in accessing document node.");
        }
        if (compareValue == 0) {
            compareValue = getTitle().compareToIgnoreCase(otherDocument.getTitle());
        }
        return compareValue;
    }
}
