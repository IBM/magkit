package de.ibmix.magkit.ui.templates;

/*-
 * #%L
 * IBM iX Magnolia Kit
 * %%
 * Copyright (C) 2025 IBM iX
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

import info.magnolia.rendering.template.configured.ConfiguredTemplateDefinition;
import info.magnolia.templating.functions.TemplatingFunctions;
import info.magnolia.cms.util.RequestDispatchUtil;
import info.magnolia.context.MgnlContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import static de.ibmix.magkit.test.cms.context.ContextMockUtils.cleanContext;
import static de.ibmix.magkit.test.cms.context.ContextMockUtils.mockWebContext;
import static de.ibmix.magkit.test.cms.context.ServerConfigurationMockUtils.mockServerConfiguration;
import static de.ibmix.magkit.test.cms.context.ServerConfigurationStubbingOperation.stubDefaultBaseUrl;
import static de.ibmix.magkit.test.cms.context.WebContextStubbingOperation.stubContextPath;
import static de.ibmix.magkit.test.cms.node.MagnoliaNodeMockUtils.mockPageNode;
import static de.ibmix.magkit.test.jcr.NodeStubbingOperation.stubIdentifier;
import static de.ibmix.magkit.test.jcr.NodeStubbingOperation.stubProperty;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link FolderModel} covering edit mode property handling and redirect URI resolution branches
 * (external target, UUID target and fallback to parent) including permanent redirect dispatch invocation.
 *
 * @author wolf.bubenik@ibmix.de
 * @since 2025-10-30
 */
public class FolderModelTest {

    private static final String EXTERNAL_URL = "https://example.org/page";
    private static final String UUID_VALUE = "12345678-1234-1234-1234-123456789012";
    private static final String INVALID_VALUE = "notAValidTarget";

    private Node _page;
    private ConfiguredTemplateDefinition _definition;
    private TemplatingFunctions _templatingFunctions;

    @BeforeEach
    public void setUp() throws RepositoryException {
        _page = mockPageNode("/root/folder", stubProperty("title", "Folder"));
        _definition = mock(ConfiguredTemplateDefinition.class);
        _templatingFunctions = mock(TemplatingFunctions.class);
        mockWebContext(stubContextPath("/context"));
    }

    @AfterEach
    public void tearDown() {
        cleanContext();
    }

    /**
     * Sets hideInNav when absent and saves the session in edit mode.
     */
    @Test
    public void executeEditModeSetsHideInNav() throws RepositoryException {
        doReturn(true).when(_templatingFunctions).isEditMode();
        FolderModel model = new FolderModel(_page, _definition, null, _templatingFunctions);
        model.execute();
        verify(_page, times(1)).hasProperty(FolderModel.PN_HIDE_IN_NAV);
        verify(_page, times(1)).setProperty(FolderModel.PN_HIDE_IN_NAV, true);
        verify(_page.getSession(), times(1)).save();
    }

    /**
     * Does not overwrite existing hideInNav property in edit mode (no new setProperty/save).
     */
    @Test
    public void executeEditModePropertyExists() throws RepositoryException {
        stubProperty(FolderModel.PN_HIDE_IN_NAV, true).of(_page);
        doReturn(true).when(_templatingFunctions).isEditMode();
        FolderModel model = new FolderModel(_page, _definition, null, _templatingFunctions);
        model.execute();
        verify(_page, times(1)).hasProperty(FolderModel.PN_HIDE_IN_NAV);
        verify(_page, never()).setProperty(FolderModel.PN_HIDE_IN_NAV, true);
        verify(_page.getSession(), never()).save();
    }

    /**
     * Dispatches permanent redirect to external URL when redirect property holds an external link.
     */
    @Test
    public void executeViewModeExternalRedirect() throws RepositoryException {
        stubProperty(FolderModel.PN_REDIRECT, EXTERNAL_URL).of(_page);
        doReturn(false).when(_templatingFunctions).isEditMode();
        FolderModel model = new FolderModel(_page, _definition, null, _templatingFunctions);
        try (MockedStatic<RequestDispatchUtil> dispatchMock = mockStatic(RequestDispatchUtil.class)) {
            model.execute();
            dispatchMock.verify(() -> RequestDispatchUtil.dispatch(
                RequestDispatchUtil.PERMANENT_PREFIX + EXTERNAL_URL,
                MgnlContext.getWebContext().getRequest(),
                MgnlContext.getWebContext().getResponse()
            ));
        }
    }

    /**
     * Dispatches permanent redirect to parent when redirect property is blank.
     */
    @Test
    public void executeViewModeFallbackRedirect() throws RepositoryException {
        doReturn(false).when(_templatingFunctions).isEditMode();
        FolderModel model = new FolderModel(_page, _definition, null, _templatingFunctions);
        try (MockedStatic<RequestDispatchUtil> dispatchMock = mockStatic(RequestDispatchUtil.class)) {
            model.execute();
            dispatchMock.verify(() -> RequestDispatchUtil.dispatch(
                RequestDispatchUtil.PERMANENT_PREFIX + "/root",
                MgnlContext.getWebContext().getRequest(),
                MgnlContext.getWebContext().getResponse()
            ));
        }
    }

    /**
     * Dispatches permanent redirect for UUID property value to resolved target using REDIRECT path variant.
     */
    @Test
    public void executeViewModeUuidRedirect() throws RepositoryException {
        stubProperty(FolderModel.PN_REDIRECT, UUID_VALUE).of(_page);
        doReturn(false).when(_templatingFunctions).isEditMode();
        mockPageNode("/root/target", stubIdentifier(UUID_VALUE));
        FolderModel model = new FolderModel(_page, _definition, null, _templatingFunctions);
        try (MockedStatic<RequestDispatchUtil> dispatchMock = mockStatic(RequestDispatchUtil.class)) {
            model.execute();
            dispatchMock.verify(() -> RequestDispatchUtil.dispatch(
                RequestDispatchUtil.PERMANENT_PREFIX + "/root/target",
                MgnlContext.getWebContext().getRequest(),
                MgnlContext.getWebContext().getResponse()
            ));
        }
    }

    /**
     * Dispatches fallback redirect when UUID cannot be resolved (NodeUtils returns null).
     */
    @Test
    public void executeViewModeUuidMissingFallback() throws RepositoryException {
        stubProperty(FolderModel.PN_REDIRECT, UUID_VALUE).of(_page);
        doReturn(false).when(_templatingFunctions).isEditMode();
        FolderModel model = new FolderModel(_page, _definition, null, _templatingFunctions);
        try (MockedStatic<RequestDispatchUtil> dispatchMock = mockStatic(RequestDispatchUtil.class)) {
            model.execute();
            dispatchMock.verify(() -> RequestDispatchUtil.dispatch(
                RequestDispatchUtil.PERMANENT_PREFIX + "/root",
                MgnlContext.getWebContext().getRequest(),
                MgnlContext.getWebContext().getResponse()
            ));
        }
    }

    /**
     * Dispatches fallback redirect when redirect property contains invalid non-external, non-UUID value.
     */
    @Test
    public void executeViewModeInvalidValueFallback() throws RepositoryException {
        stubProperty(FolderModel.PN_REDIRECT, INVALID_VALUE).of(_page);
        doReturn(false).when(_templatingFunctions).isEditMode();
        FolderModel model = new FolderModel(_page, _definition, null, _templatingFunctions);
        try (MockedStatic<RequestDispatchUtil> dispatchMock = mockStatic(RequestDispatchUtil.class)) {
            model.execute();
            dispatchMock.verify(() -> RequestDispatchUtil.dispatch(
                RequestDispatchUtil.PERMANENT_PREFIX + "/root",
                MgnlContext.getWebContext().getRequest(),
                MgnlContext.getWebContext().getResponse()
            ));
        }
    }

    /**
     * Resolves redirect URI from external URL property value.
     */
    @Test
    public void retrieveRedirectUriExternal() throws RepositoryException {
        stubProperty(FolderModel.PN_REDIRECT, EXTERNAL_URL).of(_page);
        String uri = FolderModel.retrieveRedirectUri(_page);
        assertEquals(EXTERNAL_URL, uri);
    }

    /**
     * Resolves redirect URI from UUID property value using REDIRECT link type transformation.
     */
    @Test
    public void retrieveRedirectUriUuid() throws RepositoryException {
        stubProperty(FolderModel.PN_REDIRECT, UUID_VALUE).of(_page);
        mockPageNode("/root/target", stubIdentifier(UUID_VALUE));
        String uri = FolderModel.retrieveRedirectUri(_page);
        assertEquals("/root/target", uri);
    }

    /**
     * Falls back to parent page link when redirect property is empty.
     */
    @Test
    public void retrieveRedirectUriFallbackToParent() throws RepositoryException {
        assertEquals("/root", FolderModel.retrieveRedirectUri(_page));
    }

    /**
     * Falls back to parent page link when redirect property holds invalid non-external, non-UUID value.
     */
    @Test
    public void retrieveRedirectUriInvalidValueFallback() throws RepositoryException {
        stubProperty(FolderModel.PN_REDIRECT, INVALID_VALUE).of(_page);
        assertEquals("/root", FolderModel.retrieveRedirectUri(_page));
    }

    /**
     * Produces external link variant when asExternal flag true (EXTERNAL link type).
     */
    @Test
    public void retrieveRedirectUriAsExternalFallback() throws RepositoryException {
        stubProperty(FolderModel.PN_REDIRECT, "https://domain/root").of(_page);
        assertEquals("https://domain/root", FolderModel.retrieveRedirectUri(_page, true));
    }

    /**
     * Resolves external redirect URI from UUID property value when asExternal flag true.
     */
    @Test
    public void retrieveRedirectUriAsExternalUuid() throws RepositoryException {
        mockServerConfiguration(stubDefaultBaseUrl("https://domain"));
        stubProperty(FolderModel.PN_REDIRECT, UUID_VALUE).of(_page);
        mockPageNode("/root/target", stubIdentifier(UUID_VALUE));
        assertEquals("https://domain/root/target", FolderModel.retrieveRedirectUri(_page, true));
    }

    /**
     * Handles RepositoryException thrown while setting hideInNav (logs and suppresses) without saving session.
     */
    @Test
    public void executeEditModeRepositoryExceptionHandled() throws RepositoryException {
        doReturn(true).when(_templatingFunctions).isEditMode();
        Session session = _page.getSession();
        when(_page.setProperty(FolderModel.PN_HIDE_IN_NAV, true)).thenThrow(new RepositoryException("boom"));
        FolderModel model = new FolderModel(_page, _definition, null, _templatingFunctions);
        String outcome = model.execute();
        assertNull(outcome);
        verify(session, never()).save();
    }

    /**
     * Suppresses dispatch when a RepositoryException occurs during redirect resolution (parent fallback failing).
     */
    @Test
    public void executeViewModeRepositoryExceptionInSendRedirect() throws RepositoryException {
        stubProperty(FolderModel.PN_REDIRECT, INVALID_VALUE).of(_page);
        doReturn(false).when(_templatingFunctions).isEditMode();
        when(_page.getParent()).thenThrow(new RepositoryException("cannot access parent"));
        FolderModel model = new FolderModel(_page, _definition, null, _templatingFunctions);
        try (MockedStatic<RequestDispatchUtil> dispatchMock = mockStatic(RequestDispatchUtil.class)) {
            model.execute();
            dispatchMock.verifyNoInteractions();
        }
    }
}
