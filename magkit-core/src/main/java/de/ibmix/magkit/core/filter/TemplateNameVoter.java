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
 * A custom Voter to exclude the pages with a certain template from caching.
 * Votes "true" if the URI starts with the configured rootPath
 * and if the template name of the target content equals the configured name.
 *
 * @author Wolf Bubenik, Aperto AG
 * @author frank.sommer
 * @since 2010-06-10
 */
public class TemplateNameVoter extends BasePatternVoter {
    private String[] _templates = ArrayUtils.EMPTY_STRING_ARRAY;
    private String _rootPath = EMPTY;

    public TemplateNameVoter() {
        setTrueValue(2);
    }

    /**
     * Adder for the name of the template to be excluded from caching.
     *
     * @param template the template name as String.
     */
    public void addTemplate(String template) {
        if (isNotBlank(template)) {
            _templates = ArrayUtils.add(_templates, template.trim());
        }
    }

    public String[] getTemplates() {
        return _templates;
    }

    /**
     * Setter for the starts with uri.
     *
     * @param rootPath the rootPath as String.
     */
    public void setRootPath(String rootPath) {
        _rootPath = rootPath;
    }

    protected boolean boolVote(Object value) {
        String template = getTargetTemplateName(value);
        return ArrayUtils.contains(_templates, template);
    }

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
