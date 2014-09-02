package com.aperto.magkit.filter;

import com.aperto.magkit.mockito.jcr.SessionMockUtils;
import info.magnolia.context.SystemContext;
import info.magnolia.repository.RepositoryConstants;
import org.junit.Before;
import org.junit.Test;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import static com.aperto.magkit.mockito.AggregationStateStubbingOperation.stubCurrentContent;
import static com.aperto.magkit.mockito.ContextMockUtils.*;
import static com.aperto.magkit.mockito.MagnoliaNodeMockUtils.mockPageNode;
import static com.aperto.magkit.mockito.SystemContextStubbingOperation.stubJcrSession;
import static com.aperto.magkit.mockito.jcr.NodeStubbingOperation.stubProperty;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.contains;
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
        mockAggregationState(stubCurrentContent("/bs/secure"));
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
        Session session = SessionMockUtils.mockSession(RepositoryConstants.WEBSITE);
        when(session.getNode(contains("old"))).thenThrow(new PathNotFoundException());
        mockPageNode("/bs/secure", stubProperty("secure", "true"));
        SystemContext systemContext = mockSystemContext(stubJcrSession(RepositoryConstants.WEBSITE));
        _propertyVoter.setSystemContext(systemContext);
    }
}
