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

import de.ibmix.magkit.test.cms.context.ContextMockUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import info.magnolia.rendering.template.RenderableDefinition;

import static de.ibmix.magkit.test.jcr.NodeMockUtils.mockNode;
import static org.apache.logging.log4j.Level.DEBUG;
import static org.apache.logging.log4j.Level.INFO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link RenderingMetrics} covering conditional debug logging branches, stack behavior and exception propagation.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2025-10-31
 */
public class RenderingMetricsTest {

    @AfterEach
    public void tearDown() {
        ContextMockUtils.cleanContext();
    }

    @Test
    public void testBeforeAfterWithDebugDisabled() throws Exception {
        setLoggerLevel(INFO);
        RenderingMetrics metrics = new RenderingMetrics();
        Node node = mockNode("website", "/root/node/path/nodeName");
        RenderableDefinition definition = mock(RenderableDefinition.class);
        assertNull(metrics.before(node, definition, null, null));
        assertEquals(0, metrics.getStackSize());
        assertNull(metrics.after(node, definition, null, null));
        assertEquals(0, metrics.getStackSize());
        verify(node, never()).getPath();
    }

    @Test
    public void testBeforeAfterWithDebugEnabledAndId() throws Exception {
        setLoggerLevel(DEBUG);
        RenderingMetrics metrics = new RenderingMetrics();
        Node node = mockNode("website", "/root/node/path/nodeName");
        RenderableDefinition definition = mock(RenderableDefinition.class);
        when(definition.getId()).thenReturn("myDefId");
        when(definition.getName()).thenReturn("ignoredName");
        assertNull(metrics.before(node, definition, null, null));
        assertEquals(1, metrics.getStackSize());
        assertNull(metrics.after(node, definition, null, null));
        assertEquals(0, metrics.getStackSize());
        verify(node).getPath();
    }

    @Test
    public void testBeforeAfterWithDebugEnabledAndNameFallback() throws Exception {
        setLoggerLevel(DEBUG);
        RenderingMetrics metrics = new RenderingMetrics();
        Node node = mockNode("website", "/root/node/path/nodeName");
        RenderableDefinition definition = mock(RenderableDefinition.class);
        when(definition.getId()).thenReturn(null);
        when(definition.getName()).thenReturn("myName");
        assertNull(metrics.before(node, definition, null, null));
        assertEquals(1, metrics.getStackSize());
        assertNull(metrics.after(node, definition, null, null));
        assertEquals(0, metrics.getStackSize());
        verify(node).getPath();
    }

    @Test
    public void testAfterPropagatesRepositoryException() throws Exception {
        setLoggerLevel(DEBUG);
        RenderingMetrics metrics = new RenderingMetrics();
        Node failingNode = mock(Node.class);
        when(failingNode.getPath()).thenThrow(new RepositoryException("fail"));
        RenderableDefinition definition = mock(RenderableDefinition.class);
        when(definition.getId()).thenReturn("someId");
        assertNull(metrics.before(null, null, null, null));
        assertEquals(1, metrics.getStackSize());
        assertThrows(RepositoryException.class, () -> metrics.after(failingNode, definition,null, null));
        verify(failingNode).getPath();
        assertEquals(0, metrics.getStackSize());
    }

    private void setLoggerLevel(Level level) {
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Logger coreLogger = ctx.getLogger(RenderingMetrics.class.getName());
        coreLogger.setLevel(level);
    }
}
