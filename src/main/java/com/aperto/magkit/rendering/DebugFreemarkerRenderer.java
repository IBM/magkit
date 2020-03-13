package com.aperto.magkit.rendering;

import info.magnolia.freemarker.FreemarkerHelper;
import info.magnolia.rendering.context.RenderingContext;
import info.magnolia.rendering.engine.RenderException;
import info.magnolia.rendering.engine.RenderingEngine;
import info.magnolia.rendering.renderer.FreemarkerRenderer;
import info.magnolia.rendering.template.RenderableDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.jcr.Node;
import java.util.Map;

import static info.magnolia.jcr.util.NodeUtil.getNodePathIfPossible;

/**
 * Freemarker renderer with debug time logging.
 *
 * @author frank.sommer
 * @since 13.03.2020
 */
public class DebugFreemarkerRenderer extends FreemarkerRenderer {
    private static final Logger LOGGER = LoggerFactory.getLogger(DebugFreemarkerRenderer.class);

    @Inject
    public DebugFreemarkerRenderer(FreemarkerHelper fmRenderer, RenderingEngine renderingEngine) {
        super(fmRenderer, renderingEngine);
    }

    @Override
    protected void onRender(final Node content, final RenderableDefinition definition, final RenderingContext renderingCtx, final Map<String, Object> ctx, final String templateScript) throws RenderException {
        long start = System.currentTimeMillis();
        String definitionId = definition.getId();
        String nodePath = getNodePathIfPossible(content);
        LOGGER.info("Start rendering {} with {} ...", definitionId, nodePath);
        try {
            super.onRender(content, definition, renderingCtx, ctx, templateScript);
        } finally {
            LOGGER.info("Finished rendering {} with {} in {}ms.", definitionId, nodePath, System.currentTimeMillis() - start);
        }
    }
}