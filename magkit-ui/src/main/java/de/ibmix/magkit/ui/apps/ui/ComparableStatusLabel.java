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
 * UI label component representing the publication/activation status of a Magnolia JCR {@link Node} with a natural ordering.
 * <p>
 * The component maps the node's activation state to both: (1) a primary style name used by Magnolia / Vaadin themes
 * and (2) a human readable caption ("published", "modified", "not published"). Its {@link #compareTo(Label)} method
 * imposes a deterministic order on status labels so that activated labels sort before modified ones, which in turn
 * sort before not activated labels. This enables consistent status based sorting inside workbench tables without
 * leaking style name details into calling code.
 * </p>
 * <p>Key features:</p>
 * <ul>
 *   <li>Derives activation status safely, logging repository exceptions instead of propagating them.</li>
 *   <li>Provides a stable ordering independent of locale by mapping style names to integer indices.</li>
 *   <li>Immutable caption once constructed; the visual style reflects the captured activation state at construction time.</li>
 * </ul>
 *
 * <p>
 * Usage preconditions: The provided {@link Node} must be non-null and (optionally) mix:activatable. Nodes lacking
 * activation metadata are treated as not activated. For current status after external updates, create a new instance.
 * </p>
 * <p>
 * Thread-safety: This component is not thread-safe and must be confined to the UI thread like any Vaadin component.
 * </p>
 *
 * @author wolf.bubenik
 * @since 2018-01-25
 */
public class ComparableStatusLabel extends Label implements Comparable<Label> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ComparableStatusLabel.class);

    private final String _label;

    /**
     * Construct a status label for the given node, capturing its activation state at creation time.
     *
     * @param node the JCR node whose activation status should be represented; must not be {@code null}
     */
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

    /**
     * Resolve the activation status of the provided node.
     * <p>
     * In case of repository access issues a default of {@link NodeTypes.Activatable#ACTIVATION_STATUS_NOT_ACTIVATED}
     * is returned and the exception is logged.
     * </p>
     *
     * @param node the node to inspect (must not be {@code null})
     * @return one of the {@code ACTIVATION_STATUS_*} constants defined in {@link NodeTypes.Activatable}
     * @throws IllegalArgumentException if {@code node} is {@code null}
     */
    int getActivationStatus(final Node node) {
        Preconditions.checkArgument(node != null, "The node must not be null.");
        int activationStatus = NodeTypes.Activatable.ACTIVATION_STATUS_NOT_ACTIVATED;
        try {
            activationStatus = NodeTypes.Activatable.getActivationStatus(node);
        } catch (RepositoryException e) {
            LOGGER.error("Error on getting status of node [{}]", getNodePathIfPossible(node), e);
        }
        return activationStatus;
    }

    /**
     * Return the human readable caption representing the captured activation state.
     *
     * @return one of "published", "modified" or "not published"
     */
    @Override
    public String getCaption() {
        return _label;
    }

    /**
     * Compare this label with another label based on their activation state ordering.
     * <p>
     * Ordering: activated &lt; modified &lt; not activated. A {@code null} other label or one with an unknown/empty style
     * is treated as activated (lowest index) for simplicity.
     * </p>
     *
     * @param other the other label; may be {@code null}
     * @return negative, zero or positive following the {@link Comparable} contract
     */
    @Override
    public int compareTo(Label other) {
        Integer thisValue = toIndex(getPrimaryStyleName());
        Integer otherValue = toIndex(other != null ? other.getPrimaryStyleName() : "");
        return thisValue.compareTo(otherValue);
    }

    /**
     * Map a primary style name to its sort index.
     *
     * @param style the style name (may be {@code null})
     * @return 0 for activated or unknown, 1 for modified, 2 for not activated
     */
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
