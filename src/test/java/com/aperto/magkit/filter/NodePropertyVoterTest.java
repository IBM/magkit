package com.aperto.magkit.filter;

import info.magnolia.cms.core.AggregationState;
import info.magnolia.cms.core.Content;
import info.magnolia.context.SystemContext;
import org.junit.Before;
import org.junit.Test;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import static com.aperto.magkit.mockito.ContextMockUtils.cleanContext;
import static com.aperto.magkit.mockito.ContextMockUtils.mockWebContext;
import static com.aperto.magkit.mockito.MagnoliaNodeMockUtils.mockPageNode;
import static com.aperto.magkit.mockito.WebContextStubbingOperation.stubAggregationState;
import static com.aperto.magkit.mockito.jcr.NodeStubbingOperation.stubProperty;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Test for node property voter.
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
        assertThat(voting, is(false));
    }

    @Test
    public void testMissingPattern() {
        _propertyVoter.setPattern(".*");
        boolean voting = _propertyVoter.boolVote("/bs/secure.html");
        assertThat(voting, is(false));
    }

    @Test
    public void testMissingNode() {
        _propertyVoter.setPropertyName("secure");
        _propertyVoter.setPattern("true");
        boolean voting = _propertyVoter.boolVote("/bs/old.html");
        assertThat(voting, is(false));
    }

    @Test
    public void testNodePropertyNoMatch() {
        _propertyVoter.setPropertyName("secure");
        _propertyVoter.setPattern("false");
        boolean voting = _propertyVoter.boolVote("/bs/secure.html");
        assertThat(voting, is(false));
    }

    @Test
    public void testNodePropertyMatch() {
        _propertyVoter.setPropertyName("secure");
        _propertyVoter.setPattern("true");
        boolean voting = _propertyVoter.boolVote("/bs/secure.html");
        assertThat(voting, is(true));
    }

    @Test
    public void testNodePropertyMatchWithContext() throws RepositoryException {
        AggregationState aggregationState = new AggregationState();
        Content content = mock(Content.class);
        when(content.getHandle()).thenReturn("/bs/secure");
        aggregationState.setCurrentContent(content);
        mockWebContext(stubAggregationState(aggregationState));
        _propertyVoter.setPropertyName("secure");
        _propertyVoter.setPattern("true");
        boolean voting = _propertyVoter.boolVote("/bs/secure.html");
        assertThat(voting, is(true));
    }

    @Test
    public void testNodePropertyNullMatch() {
        _propertyVoter.setPropertyName("notexists");
        _propertyVoter.setPattern("true");
        boolean voting = _propertyVoter.boolVote("/bs/secure.html");
        assertThat(voting, is(false));
    }

    @Before
    public void initVoter() throws RepositoryException {
        cleanContext();
        _propertyVoter = new NodePropertyVoter();
        SystemContext systemContext = mock(SystemContext.class);
        Session session = mock(Session.class);
        when(session.getNode(contains("old"))).thenThrow(new PathNotFoundException());
        Node node = mockPageNode("/bs/secure", stubProperty("secure", "true"));
        when(session.getNode("/bs/secure")).thenReturn(node);
        when(systemContext.getJCRSession(anyString())).thenReturn(session);
        _propertyVoter.setSystemContext(systemContext);
    }
}
