package com.aperto.magkit.utils;

import info.magnolia.cms.core.Content;
import org.apache.commons.collections15.Predicate;
import static org.apache.commons.lang.BooleanUtils.isTrue;
import static org.apache.commons.lang.BooleanUtils.toBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

/**
 * Checks a node data of the content. In positive case the content would be excluded.
 * The node data could be a boolean or a string.
 * By string 'yes', 'on' and 'true' are valid values.
 *
 * "hideFromSearch" is default node data name.
 * inheritance is per default off.
 *
 * @author frank.sommer (22.10.2009)
 */
public class NodeDataPredicate implements Predicate<Content> {
    private static final Logger LOGGER = LoggerFactory.getLogger(NodeDataPredicate.class);
    private String _nodeName = "hideFromSearch";
    private boolean _inheritance = false;

    public NodeDataPredicate() {
    }

    public NodeDataPredicate(String nodeName) {
        _nodeName = nodeName;
    }

    public NodeDataPredicate(String nodeName, boolean inheritance) {
        _nodeName = nodeName;
        _inheritance = inheritance;
    }

    public boolean evaluate(Content content) {
        boolean accept = true;
        try {
            if (content.hasNodeData(_nodeName)) {
                Value data = content.getNodeData(_nodeName).getValue();
                switch (data.getType()) {
                    case PropertyType.BOOLEAN:
                        accept = !isTrue(data.getBoolean());
                        break;
                    case PropertyType.STRING:
                        accept = !isTrue(toBoolean(data.getString()));
                        break;
                    default:
                        LOGGER.info("Not a valid type: {}.", data.getType());
                }
            }
            if (_inheritance) {
                Content parent = content.getParent();
                if (accept && parent != null) {
                    accept = evaluate(parent);
                }
            }
        } catch (RepositoryException e) {
            LOGGER.warn("Could not read content.", e);
        }
        return accept;
    }
}