package com.aperto.magkit.controller;

import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.AggregationState;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.context.MgnlContext;
import static info.magnolia.voting.voters.DontDispatchOnForwardAttributeVoter.DONT_DISPATCH_ON_FORWARD_ATTRIBUTE;
import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

/**
 * This spring {@link org.springframework.web.servlet.HandlerInterceptor} implementation is responsible for providing
 * a basic valid {@link info.magnolia.cms.core.AggregationState} for controllers that bypass the cms filter chain and furthermore it sets a
 * signal for magnolia to don't dispatch on forward request after controller processing.
 * <p/>
 * http://static.springframework.org/spring/docs/2.5.x/reference/mvc.html#mvc-handlermapping-interceptor
 * <p/>
 *
 * @author Norman Wiechmann (Aperto AG)
 */
public class BypassedControllerInterceptor extends HandlerInterceptorAdapter {
    private static final Logger LOGGER = Logger.getLogger(BypassedControllerInterceptor.class);
    private static final String DEFAULT_REPOSITORY = ContentRepository.WEBSITE;

    /**
     * Configures the {@link info.magnolia.cms.core.AggregationState} of the current {@link info.magnolia.context.MgnlContext}. This is possible only if a context
     * is available. It tries to guess if an aggregation already occurred to prevent from overwriting existing values.
     * The state will be configured with the 'website' repository root content node.
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (MgnlContext.hasInstance()) {
            AggregationState state = MgnlContext.getAggregationState();
            if (state.getHandle() == null && state.getMainContent() == null && state.getCurrentContent() == null) {
                state.setHandle("");
                state.setSelector("");
                state.setRepository(DEFAULT_REPOSITORY);
                HierarchyManager hierarchyManager = MgnlContext.getHierarchyManager(DEFAULT_REPOSITORY);
                try {
                    Content root = hierarchyManager.getRoot();
                    state.setMainContent(root);
                    state.setCurrentContent(root);
                } catch (RepositoryException e) {
                    LOGGER.warn("Unable to get content repository root.", e);
                }
            }
        }
        return true;
    }

    /**
     * Adds an attribute to the request that is evaluated by
     * {@link info.magnolia.voting.voters.DontDispatchOnForwardAttributeVoter} that must be configured with magnolia
     * server filter bypass chain. The attribute avoids another dispatching on request forwards.
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
        request.setAttribute(DONT_DISPATCH_ON_FORWARD_ATTRIBUTE, Boolean.TRUE);
    }
}