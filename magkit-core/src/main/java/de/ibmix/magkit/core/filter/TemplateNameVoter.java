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

import de.ibmix.magkit.core.utils.NodeUtils;
import info.magnolia.voting.voters.BasePatternVoter;
import org.apache.commons.lang3.ArrayUtils;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import static info.magnolia.context.MgnlContext.getJCRSession;
import static info.magnolia.repository.RepositoryConstants.WEBSITE;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.substringBeforeLast;

/**
 * Voter that evaluates a target page's template name against a configured list of template identifiers.
 * <p>Intended for use in Magnolia caching or access control decisions where pages with certain templates
 * should be treated differently (e.g. excluded from cache). The voter extracts the node path from a URI
 * (removing the extension) and retrieves the template using {@link NodeUtils#getTemplate(Node)} for a node
 * in the <code>website</code> workspace. A vote returns {@code true} if the template matches any configured
 * template value and optional root path constraints are satisfied.</p>
 * <p><strong>Main features:</strong></p>
 * <ul>
 *   <li>Supports configuration of multiple template names via {@link #addTemplate(String)}.</li>
 *   <li>Optional filtering by a root path prefix to avoid unnecessary repository lookups.</li>
 *   <li>Fails gracefully: nonexistent nodes or missing templates yield a {@code false} vote.</li>
 *   <li>Lightweight integer true value configured in constructor (value 2) to integrate with Magnolia voting.</li>
 * </ul>
 * <p><strong>Usage preconditions:</strong> At least one non blank template must be added for meaningful results.
 * Root path is optional; if specified only URIs starting with that prefix are considered.</p>
 * <p><strong>Null and error handling:</strong> A null or blank input value produces a {@code false} vote. Repository
 * exceptions are ignored and treated as non matches.</p>
 * <p><strong>Thread-safety:</strong> Not thread-safe due to internal mutable arrays. Configure once per instance
 * or ensure external synchronization if modified concurrently.</p>
 * <p><strong>Example:</strong></p>
 * <pre>{@code
 * TemplateNameVoter voter = new TemplateNameVoter();
 * voter.addTemplate("special-template");
 * voter.setRootPath("/special");
 * boolean skipCache = voter.boolVote("/special/page.html");
 * }</pre>
 *
 * @author Wolf Bubenik, Aperto AG
 * @author frank.sommer
 * @since 2010-06-10
 */
public class TemplateNameVoter extends BasePatternVoter {
    private String[] _templates = ArrayUtils.EMPTY_STRING_ARRAY;
    private String _rootPath = EMPTY;

    /**
     * Constructs the voter and sets a Magnolia specific true value weight (2) to integrate with other voters.
     */
    public TemplateNameVoter() {
        setTrueValue(2);
    }

    /**
     * Adds a template identifier considered a match for voting. Blank values are ignored; non blank values
     * are trimmed before storage.
     *
     * @param template the template name; ignored if null or blank
     */
    public void addTemplate(String template) {
        if (isNotBlank(template)) {
            _templates = ArrayUtils.add(_templates, template.trim());
        }
    }

    /**
     * Returns the configured template names. Never null; may be empty if no template was added.
     *
     * @return array of template names
     */
    public String[] getTemplates() {
        return _templates;
    }

    /**
     * Sets an optional root path prefix. Only URIs starting with this prefix will trigger a repository lookup.
     * Use blank value to disable root path filtering.
     *
     * @param rootPath the root path prefix; may be blank
     */
    public void setRootPath(String rootPath) {
        _rootPath = rootPath;
    }

    /**
     * Performs the boolean vote by resolving a template name and checking for containment in the configured
     * template array.
     *
     * @param value a URI String representing the requested page; may be null
     * @return true if a matching template is found; false otherwise
     */
    protected boolean boolVote(Object value) {
        String template = getTargetTemplateName(value);
        return ArrayUtils.contains(_templates, template);
    }

    /**
     * Resolves the template name of the target page node. Applies root path filtering to reduce repository access
     * cost. Returns an empty string if lookup fails.
     *
     * @param value the vote input (expected URI String)
     * @return template name or empty string if not applicable
     */
    private String getTargetTemplateName(Object value) {
        String template = EMPTY;
        if (value != null) {
            String uri = resolveURIFromValue(value);
            // skip expensive content lookup if URI is not of interest, e.g. ressource URI...
            if (isBlank(_rootPath) || uri.startsWith(_rootPath)) {
                String path = substringBeforeLast(uri, ".");
                try {
                    Session session = getJCRSession(WEBSITE);
                    Node node = session.getNode(path);
                    template = NodeUtils.getTemplate(node);
                } catch (RepositoryException e) {
                    // ignore, use empty string
                }
            }
        }
        return template;
    }
}
