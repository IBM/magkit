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
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.substringBeforeLast;

/**
 * Voter that evaluates a JCR node property against a configured regular expression pattern.
 * <p>This voter is typically used inside Magnolia voting chains to influence caching, security or other
 * conditional logic based on dynamic node properties. A node is resolved from the provided value (usually
 * a URI String) or from the current aggregation state if available. The value of the configured property
 * is then matched against the compiled {@link Pattern} derived from the set pattern String.</p>
 * <p><strong>Main features:</strong></p>
 * <ul>
 *   <li>Resolves target node path either from Magnolia aggregation state or by trimming the extension from a URI.</li>
 *   <li>Retrieves a property value in the <code>website</code> workspace via the injected {@link SystemContext}.</li>
 *   <li>Matches the property value against a pre-compiled regex for performance.</li>
 *   <li>Graceful degradation: returns false if configuration is incomplete or repository lookup fails.</li>
 * </ul>
 * <p><strong>Usage preconditions:</strong> You must configure both a non blank property name and a non blank pattern
 * string before calling {@link #boolVote(Object)}; otherwise the voter will always return <code>false</code>.</p>
 * <p><strong>Null and error handling:</strong> Repository access exceptions are caught and logged at WARN level; a
 * missing node or property results in a <code>false</code> vote. No exception is propagated to callers.</p>
 * <p><strong>Thread-safety:</strong> This class is <em>not</em> thread-safe due to its mutable configuration state
 * (property name, pattern and compiled regex). Create a dedicated instance per configuration or ensure external
 * synchronization if modified at runtime.</p>
 * <p><strong>Example:</strong></p>
 * <pre>{@code
 * NodePropertyVoter voter = new NodePropertyVoter();
 * voter.setSystemContext(systemContext);
 * voter.setPropertyName("secure");
 * voter.setPattern("true|yes");
 * boolean allowed = voter.boolVote("/path/page.html");
 * }</pre>
 *
 * @author frank.sommer
 * @since 14.01.13
 */
public class NodePropertyVoter extends BasePatternVoter {
    private static final Logger LOGGER = LoggerFactory.getLogger(NodePropertyVoter.class);

    private Context _systemContext;
    private String _propertyName;
    private Pattern _regex;

    /**
     * Sets the JCR property name whose value should be matched against the configured pattern.
     * Leading and trailing whitespace is preserved; callers should trim if necessary.
     *
     * @param propertyName the property name to evaluate; if blank the voter will always return false
     */
    public void setPropertyName(final String propertyName) {
        _propertyName = propertyName;
    }

    /**
     * Injects the Magnolia {@link SystemContext} used to obtain a JCR session (website workspace).
     * Must be set before voting, otherwise repository access will fail.
     *
     * @param systemContext the system context providing privileged JCR access
     */
    @Inject
    public void setSystemContext(final SystemContext systemContext) {
        _systemContext = systemContext;
    }

    /**
     * Performs the boolean vote: resolves the target node path, reads the configured property value and
     * matches it against the compiled regex pattern.
     *
     * @param value the incoming vote value (typically a URI String); used to resolve the node path when
     *              no aggregation state node is present
     * @return true if property name and pattern are configured, the node exists and the property value matches;
     *         false otherwise
     */
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

    /**
     * Resolves the JCR node path either from the current aggregation state (if available) or from the incoming
     * value (expected to be a URI String) by stripping the file extension.
     *
     * @param value the input value passed to {@link #boolVote(Object)}
     * @return the resolved absolute node path in the website workspace
     */
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

    /**
     * Sets the regex pattern string and compiles it for subsequent fast matching. Overrides the super implementation
     * to also keep a compiled {@link Pattern} instance.
     *
     * @param pattern the regex pattern string; must be non blank for the voter to operate
     * @throws java.util.regex.PatternSyntaxException if the pattern is invalid (propagated from {@link Pattern#compile(String)})
     */
    @Override
    public void setPattern(String pattern) {
        super.setPattern(pattern);
        _regex = Pattern.compile(pattern);
    }
}
