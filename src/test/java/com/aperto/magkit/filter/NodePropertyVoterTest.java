package com.aperto.magkit.filter;

import info.magnolia.context.SystemContext;
import org.junit.Before;
import org.junit.Test;

import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import static com.aperto.magkit.mockito.NodeMockUtils.mockPageNode;
import static com.aperto.magkit.mockito.NodeStubbingOperation.stubProperty;
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
    public void testMissingNode() {
        _propertyVoter.setPropertyName("secure");
        _propertyVoter.setPropertyValue("true");
        boolean voting = _propertyVoter.boolVote("/bs/old.html");
        assertThat(voting, is(false));
    }

    @Test
    public void testNodePropertyNoMatch() {
        _propertyVoter.setPropertyName("secure");
        _propertyVoter.setPropertyValue("false");
        boolean voting = _propertyVoter.boolVote("/bs/secure.html");
        assertThat(voting, is(false));
    }

    @Test
    public void testNodePropertyMatch() {
        _propertyVoter.setPropertyName("secure");
        _propertyVoter.setPropertyValue("true");
        boolean voting = _propertyVoter.boolVote("/bs/secure.html");
        assertThat(voting, is(true));
    }

    @Before
    public void initVoter() throws RepositoryException {
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
