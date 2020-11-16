package com.aperto.magkit.rendering;

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

import static org.apache.commons.lang3.StringUtils.defaultString;

/**
 * Measure and display time taken by rendering for each component, area, page.
 * <p>
 * Register listener class under config:/server/rendering/engine/listeners
 * and make sure you enable level DEBUG for this logger.
 *
 * @author Philipp Güttler (Aperto GmbH)
 * @since 03.04.2020
 */
public class RenderingMetrics extends AbstractRenderingListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(RenderingMetrics.class);

    private final Deque<Long> _stack = new ArrayDeque<>();

    @Override
    public RenderingListenerReturnCode before(final Node content, final RenderableDefinition definition, final Map<String, Object> contextObjects, final OutputProvider out) {
        if (LOGGER.isDebugEnabled()) {
            _stack.push(System.currentTimeMillis());
        }
        return null;
    }

    @Override
    public RenderingListenerReturnCode after(final Node content, final RenderableDefinition definition, final Map<String, Object> contextObjects, final OutputProvider out) throws RepositoryException {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Took {} for [{}] at [{}].", String.format("%3dms", System.currentTimeMillis() - _stack.pop()), defaultString(definition.getId(), definition.getName()), content.getPath());
        }
        return null;
    }
}