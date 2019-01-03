package com.aperto.magkit.module.delta;

import info.magnolia.jcr.util.NodeNameHelper;
import info.magnolia.module.InstallContext;
import info.magnolia.module.model.ModuleDefinition;
import info.magnolia.module.model.ServletDefinition;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.jcr.RepositoryException;
import javax.jcr.Session;

import static com.aperto.magkit.mockito.ComponentsMockUtils.mockComponentInstance;
import static com.aperto.magkit.mockito.ContextMockUtils.cleanContext;
import static com.google.common.collect.Sets.newHashSet;
import static info.magnolia.repository.RepositoryConstants.CONFIG;
import static info.magnolia.test.mock.jcr.SessionTestUtil.createSession;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test the reinstall of the module servlets.
 *
 * @author frank.sommer
 * @since 03.01.2019
 */
public class CheckModuleServletsTaskTest {

    private CheckModuleServletsTask _servletsTask;
    private InstallContext _installContext;

    @Before
    public void setUp() throws Exception {
        _installContext = mock(InstallContext.class);
        Session configSession = createSession(CONFIG, getClass().getResourceAsStream("servlets.properties"));
        when(_installContext.getConfigJCRSession()).thenReturn(configSession);
        when(_installContext.getJCRSession(CONFIG)).thenReturn(configSession);

        ModuleDefinition moduleDefinition = new ModuleDefinition();
        moduleDefinition.setName("TestModule");
        when(_installContext.getCurrentModuleDefinition()).thenReturn(moduleDefinition);

        mockComponentInstance(NodeNameHelper.class);
        _servletsTask = new CheckModuleServletsTask();
    }

    @Test
    public void noModuleServlets() throws Exception {
        _servletsTask.execute(_installContext);

        assertThat(_servletsTask.getDescription(), equalTo("Registers servlets for this module."));
        assertThat(_servletsTask.toString(), equalTo("[]"));
    }

    @Test
    public void oneNewServlet() throws Exception {
        _installContext.getCurrentModuleDefinition().setServlets(newHashSet(createServlet("new-servlet")));
        _servletsTask.execute(_installContext);

        assertThat(_servletsTask.getDescription(), equalTo("Registers servlets for this module."));
        assertThat(_servletsTask.toString(), equalTo("[task: Servlet new-servlet]"));
    }

    @Test
    public void newAndExistingServlets() throws Exception {
        _installContext.getCurrentModuleDefinition().setServlets(newHashSet(createServlet("new-servlet"), createServlet("existing-servlet")));
        _servletsTask.execute(_installContext);

        assertThat(_servletsTask.getDescription(), equalTo("Registers servlets for this module."));
        assertThat(_servletsTask.toString(), equalTo("[task: Remove servlet configuration, task: Servlet existing-servlet, task: Servlet new-servlet]"));
    }

    @Test
    public void repositoryException() throws Exception {
        _installContext.getCurrentModuleDefinition().setServlets(newHashSet(createServlet("new-servlet")));
        Session session = mock(Session.class);
        when(session.itemExists(anyString())).thenThrow(new RepositoryException("Exception on item exists."));
        when(_installContext.getConfigJCRSession()).thenReturn(session);
        _servletsTask.execute(_installContext);

        assertThat(_servletsTask.getDescription(), equalTo("Registers servlets for this module."));
        assertThat(_servletsTask.toString(), equalTo("[]"));
        verify(_installContext).warn("Unable to access workspace config to check on servlet definitions for module 'TestModule'.");
    }

    private ServletDefinition createServlet(final String servletName) {
        ServletDefinition servletDefinition = new ServletDefinition();
        servletDefinition.setName(servletName);
        return servletDefinition;
    }

    @After
    public void tearDown() {
        cleanContext();
    }
}