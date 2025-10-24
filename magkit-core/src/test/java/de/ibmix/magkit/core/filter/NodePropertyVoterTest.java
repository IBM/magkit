package de.ibmix.magkit.core.filter;

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

import info.magnolia.context.SystemContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import static de.ibmix.magkit.test.cms.context.AggregationStateStubbingOperation.stubCurrentContentNode;
import static de.ibmix.magkit.test.cms.context.ContextMockUtils.cleanContext;
import static de.ibmix.magkit.test.cms.context.ContextMockUtils.mockAggregationState;
import static de.ibmix.magkit.test.cms.context.ContextMockUtils.mockSystemContext;
import static de.ibmix.magkit.test.cms.context.SystemContextStubbingOperation.stubJcrSession;
import static de.ibmix.magkit.test.cms.node.MagnoliaNodeMockUtils.mockPageNode;
import static de.ibmix.magkit.test.jcr.NodeStubbingOperation.stubProperty;
import static de.ibmix.magkit.test.jcr.SessionMockUtils.mockSession;
import static info.magnolia.repository.RepositoryConstants.WEBSITE;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link NodePropertyVoter} covering configuration completeness, property matching scenarios
 * and context based path resolution.
 *
 * @author frank.sommer
 * @since 14.01.13
 */
public class NodePropertyVoterTest {
    private NodePropertyVoter _propertyVoter;

    @Test
    public void testMissingConfig() {
        _propertyVoter.setPropertyName("secure");
        boolean voting = _propertyVoter.boolVote("/bs/secure.html");
        assertFalse(voting);
    }

    @Test
    public void testMissingPattern() {
        _propertyVoter.setPattern(".*");
        boolean voting = _propertyVoter.boolVote("/bs/secure.html");
        assertFalse(voting);
    }

    @Test
    public void testMissingNode() {
        _propertyVoter.setPropertyName("secure");
        _propertyVoter.setPattern("true");
        boolean voting = _propertyVoter.boolVote("/bs/old.html");
        assertFalse(voting);
    }

    @Test
    public void testNodePropertyNoMatch() {
        _propertyVoter.setPropertyName("secure");
        _propertyVoter.setPattern("false");
        boolean voting = _propertyVoter.boolVote("/bs/secure.html");
        assertFalse(voting);
    }

    @Test
    public void testNodePropertyMatch() {
        _propertyVoter.setPropertyName("secure");
        _propertyVoter.setPattern("true");
        boolean voting = _propertyVoter.boolVote("/bs/secure.html");
        assertTrue(voting);
    }

    @Test
    public void testNodePropertyMatchWithContext() throws RepositoryException {
        mockAggregationState(stubCurrentContentNode("/bs/secure"));
        _propertyVoter.setPropertyName("secure");
        _propertyVoter.setPattern("true");
        boolean voting = _propertyVoter.boolVote("/bs/secure.html");
        assertTrue(voting);
    }

    @Test
    public void testNodePropertyNullMatch() {
        _propertyVoter.setPropertyName("notexists");
        _propertyVoter.setPattern("true");
        boolean voting = _propertyVoter.boolVote("/bs/secure.html");
        assertFalse(voting);
    }

    @BeforeEach
    public void initVoter() throws RepositoryException {
        _propertyVoter = new NodePropertyVoter();
        Session session = mockSession(WEBSITE);
        SystemContext systemContext = mockSystemContext(stubJcrSession(WEBSITE));
        when(session.getNode(contains("old"))).thenThrow(new PathNotFoundException());
        mockPageNode("/bs/secure", stubProperty("secure", "true"));
        _propertyVoter.setSystemContext(systemContext);
    }

    @AfterEach
    public void tearDown() throws Exception {
        cleanContext();
    }
}
