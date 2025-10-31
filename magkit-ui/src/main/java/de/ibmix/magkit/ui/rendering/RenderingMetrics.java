package de.ibmix.magkit.ui.rendering;

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

import info.magnolia.rendering.engine.OutputProvider;
import info.magnolia.rendering.listeners.AbstractRenderingListener;
import info.magnolia.rendering.template.RenderableDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;

/**
 * Rendering listener that records and logs elapsed rendering time per content element (component, area, page).
 * <p>Purpose: Provide lightweight performance diagnostics during Magnolia rendering by measuring the duration
 * between listener {@code before} and {@code after} callbacks.</p>
 * <p>Main features:
 * <ul>
 *     <li>Measures wall-clock time in milliseconds per rendered definition.</li>
 *     <li>Outputs structured DEBUG log lines with identifier and JCR path.</li>
 *     <li>Stack-based timing allowing nested rendering operations.</li>
 *     <li>Zero overhead when DEBUG logging is disabled (no timestamp pushes).</li>
 * </ul>
 * </p>
 * <p>Usage preconditions: Register the class under {@code config:/server/rendering/engine/listeners} and enable
 * DEBUG level for logger {@code de.ibmix.magkit.ui.rendering.RenderingMetrics}.</p>
 * <p>Side effects: None beyond DEBUG logging.</p>
 * <p>Null & error handling: Returns {@code null} to indicate no alteration of rendering flow. Repository access
 * may throw {@link RepositoryException} in {@link #after(Node, RenderableDefinition, Map, OutputProvider)} which is propagated.</p>
 * <p>Thread-safety: Instances are not thread-safe due to internal {@link Deque}; Magnolia typically creates listener
 * instances per rendering engine lifecycle. Do not share across threads without external synchronization.</p>
 * <p>Usage example:
 * <pre>{@code
 * // In configuration (YAML / JCR): add de.ibmix.magkit.ui.rendering.RenderingMetrics as listener.
 * // Enable DEBUG logging:
 * log4j.logger.de.ibmix.magkit.ui.rendering.RenderingMetrics=DEBUG
 * }</pre>
 * </p>
 *
 * @author Philipp Güttler (Aperto GmbH)
 * @since 2020-04-03
 */
public class RenderingMetrics extends AbstractRenderingListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(RenderingMetrics.class);

    private final Deque<Long> _stack = new ArrayDeque<>();

    /**
     * Capture start time for the current rendering definition if DEBUG logging is active.
     * Returns {@code null} to allow normal rendering.
     *
     * @param content the JCR node being rendered
     * @param definition the renderable definition (component, area, page)
     * @param contextObjects rendering context objects (unused here)
     * @param out output provider (unused for metrics)
     * @return {@code null} (no influence on rendering engine flow)
     */
    @Override
    public RenderingListenerReturnCode before(final Node content, final RenderableDefinition definition, final Map<String, Object> contextObjects, final OutputProvider out) {
        if (LOGGER.isDebugEnabled()) {
            _stack.push(System.currentTimeMillis());
        }
        return null;
    }

    /**
     * Log elapsed time for the current rendering operation if DEBUG logging is active.
     * Returns {@code null} to continue normal rendering flow.
     *
     * @param content the JCR node rendered
     * @param definition the renderable definition
     * @param contextObjects rendering context objects (unused)
     * @param out output provider (unused)
     * @return {@code null} (no influence on rendering engine flow)
     * @throws RepositoryException if obtaining node path fails
     */
    @Override
    public RenderingListenerReturnCode after(final Node content, final RenderableDefinition definition, final Map<String, Object> contextObjects, final OutputProvider out) throws RepositoryException {
        if (LOGGER.isDebugEnabled()) {
            String idOrName = definition.getId() != null ? definition.getId() : definition.getName();
            LOGGER.debug("Took {} for [{}] at [{}].", String.format("%3dms", System.currentTimeMillis() - _stack.pop()), idOrName, content.getPath());
        }
        return null;
    }

    // For testing purposes
    int getStackSize() {
        return _stack.size();
    }
}
