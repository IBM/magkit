package com.aperto.magkit.filter;

import info.magnolia.context.Context;
import info.magnolia.context.MgnlContext;
import info.magnolia.context.SystemContext;
import info.magnolia.voting.voters.BasePatternVoter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.util.regex.Pattern;

import static info.magnolia.jcr.util.NodeUtil.getNodePathIfPossible;
import static info.magnolia.jcr.util.PropertyUtil.getString;
import static info.magnolia.repository.RepositoryConstants.WEBSITE;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang.StringUtils.substringBeforeLast;

/**
 * Voter that matches on a configured property name the configured regex pattern.
 *
 * @author frank.sommer
 * @since 14.01.13
 */
public class NodePropertyVoter extends BasePatternVoter {
    private static final Logger LOGGER = LoggerFactory.getLogger(SecureRedirectFilter.class);

    private Context _systemContext;
    private String _propertyName;
    private Pattern _regex;

    public void setPropertyName(final String propertyName) {
        _propertyName = propertyName;
    }

    @Inject
    public void setSystemContext(final SystemContext systemContext) {
        _systemContext = systemContext;
    }

    @Override
    protected boolean boolVote(final Object value) {
        boolean vote = false;

        if (isNotBlank(_propertyName) && isNotBlank(getPattern())) {
            String path = "no-path";
            try {
                path = resolveNodePath(value);
                Session jcrSession = _systemContext.getJCRSession(WEBSITE);
                Node node = jcrSession.getNode(path);
                String currentValue = getString(node, _propertyName, "");
                vote = _regex.matcher(currentValue).matches();
            } catch (RepositoryException e) {
                LOGGER.warn("No website content found on {}. Perhaps use an additional voter.", path);
            }
        } else {
            LOGGER.warn("Configuration of a {} seems to be incomplete.", getClass().getName());
        }
        return vote;
    }

    private String resolveNodePath(final Object value) {
        String nodePath;
        if (MgnlContext.hasInstance() && MgnlContext.getAggregationState() != null && MgnlContext.getAggregationState().getCurrentContentNode() != null) {
            nodePath = getNodePathIfPossible(MgnlContext.getAggregationState().getCurrentContentNode());
        } else {
            String uri = resolveURIFromValue(value);
            nodePath = substringBeforeLast(uri, ".");
        }
        return nodePath;
    }

    @Override
    public void setPattern(String pattern) {
        super.setPattern(pattern);
        _regex = Pattern.compile(pattern);
    }
}
