package de.ibmix.magkit.ui.dialogs.fields;

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

import de.ibmix.magkit.core.utils.ExtendedLinkFieldHelper;
import info.magnolia.context.MgnlContext;
import info.magnolia.ui.form.field.converter.IdentifierToPathConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.util.Locale;

import static de.ibmix.magkit.core.utils.LinkTool.isAnchor;
import static de.ibmix.magkit.core.utils.LinkTool.isExternalLink;
import static de.ibmix.magkit.core.utils.LinkTool.isPath;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Handles input of links by urls (external) or absolute paths (internal) with additional components. Use this converter with the {@link ExtendedLinkFieldDefinition}.
 *
 * @author Philipp Güttler (Aperto AG)
 * @since 03.06.2015
 */
public class ExtendedLinkConverter implements IdentifierToPathConverter {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExtendedLinkConverter.class);
    private static final long serialVersionUID = 4484406102548210913L;

    private transient ExtendedLinkFieldHelper _extendedLinkFieldHelper;
    private String _workspaceName;

    @Override
    public String convertToModel(final String value, final Class<? extends String> targetType, final Locale locale) {

        // Null is required for the property to be removed if path is empty
        String result = null;

        if (isExternalLink(value) || isAnchor(value)) {
            result = value;
        } else if (isPath(value)) {
            String nodePath = _extendedLinkFieldHelper.getBase(value);
            try {
                final Session jcr = MgnlContext.getJCRSession(_workspaceName);
                final Node node = jcr.getNode(nodePath);
                if (node != null) {
                    result = node.getIdentifier();
                }
            } catch (RepositoryException e) {
                LOGGER.error("Unable to convert Path to UUID", e);
            }
            if (isNotBlank(result)) {
                result += _extendedLinkFieldHelper.stripBase(value);
            } else {
                result = value;
            }
        }

        return result;
    }

    @Override
    public String convertToPresentation(final String value, final Class<? extends String> targetType, final Locale locale) {

        String result = EMPTY;

        if (isExternalLink(value) || isAnchor(value)) {
            result = value;
        } else if (isNotBlank(value)) {
            String stripped = _extendedLinkFieldHelper.stripBase(value);
            String identifier = _extendedLinkFieldHelper.getBase(value);
            try {
                final Session jcr = MgnlContext.getJCRSession(_workspaceName);
                final Node node = jcr.getNodeByIdentifier(identifier);
                if (node != null) {
                    result = node.getPath();
                }
            } catch (RepositoryException e) {
                LOGGER.error("Unable to convert UUID to Path", e);
            }
            if (isNotBlank(result)) {
                result += stripped;
            } else {
                result = value;
            }
        }

        return result;
    }

    @Override
    public void setWorkspaceName(String workspaceName) {
        _workspaceName = workspaceName;
    }

    @Inject
    public void setExtendedLinkFieldHelper(final ExtendedLinkFieldHelper extendedLinkFieldHelper) {
        _extendedLinkFieldHelper = extendedLinkFieldHelper;
    }

}
