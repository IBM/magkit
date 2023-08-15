package de.ibmix.magkit.ui.apps.ui;

/*-
 * #%L
 * IBM iX Magnolia Kit
 * %%
 * Copyright (C) 2023 IBM iX
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.google.common.base.Preconditions;
import com.vaadin.ui.Label;
import info.magnolia.jcr.util.NodeTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import static info.magnolia.jcr.util.NodeUtil.getNodePathIfPossible;
import static info.magnolia.ui.workbench.column.StatusColumnFormatter.ActivationStatus.ACTIVATED;
import static info.magnolia.ui.workbench.column.StatusColumnFormatter.ActivationStatus.MODIFIED;
import static info.magnolia.ui.workbench.column.StatusColumnFormatter.ActivationStatus.NOT_ACTIVATED;

/**
 * A status label that is sortable by its style in the order activated -> modified -> not activated.
 *
 * @author wolf.bubenik
 * @since 25.01.18.
 */
public class ComparableStatusLabel extends Label implements Comparable<Label> {

    private static final Logger LOG = LoggerFactory.getLogger(ComparableStatusLabel.class);

    private final String _label;

    public ComparableStatusLabel(Node node) {
        super();
        switch (getActivationStatus(node)) {
            case NodeTypes.Activatable.ACTIVATION_STATUS_MODIFIED:
                setPrimaryStyleName(MODIFIED.getStyleName());
                _label = "modified";
                break;
            case NodeTypes.Activatable.ACTIVATION_STATUS_ACTIVATED:
                setPrimaryStyleName(ACTIVATED.getStyleName());
                _label = "published";
                break;
            default:
                setPrimaryStyleName(NOT_ACTIVATED.getStyleName());
                _label = "not published";
        }
    }

    int getActivationStatus(final Node node) {
        Preconditions.checkArgument(node != null, "The node must not be null.");
        int activationStatus = NodeTypes.Activatable.ACTIVATION_STATUS_NOT_ACTIVATED;
        try {
            activationStatus = NodeTypes.Activatable.getActivationStatus(node);
        } catch (RepositoryException e) {
            LOG.error("Error on getting status of node [{}]", getNodePathIfPossible(node), e);
        }
        return activationStatus;
    }

    @Override
    public String getCaption() {
        return _label;
    }

    @Override
    public int compareTo(Label other) {
        Integer thisValue = toIndex(getPrimaryStyleName());
        Integer otherValue = toIndex(other != null ? other.getPrimaryStyleName() : "");
        return thisValue.compareTo(otherValue);
    }

    int toIndex(String style) {
        int result = 0;
        if (MODIFIED.getStyleName().equals(style)) {
            result = 1;
        } else if (NOT_ACTIVATED.getStyleName().equals(style)) {
            result = 2;
        }
        return result;
    }
}
