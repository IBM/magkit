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

import com.machinezoo.noexception.Exceptions;
import com.vaadin.data.Result;
import com.vaadin.data.ValueContext;
import info.magnolia.cms.util.SelectorUtil;
import info.magnolia.ui.datasource.jcr.JcrDatasource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import static de.ibmix.magkit.core.utils.LinkTool.isAnchor;
import static de.ibmix.magkit.core.utils.LinkTool.isExternalLink;
import static de.ibmix.magkit.core.utils.LinkTool.isPath;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.containsAny;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.substringBefore;

/**
 * Handles input of links by urls (external) or absolute paths (internal) with additional selector, anchor or parameters.<br>
 * Used in your page link fields:<br>
 * <code>
 * $type: pageLinkField<br>
 * textInputAllowed: true<br>
 * converterClass: de.ibmix.magkit.ui.dialogs.fields.ExtendedLinkConverter<br>
 * fieldBinderClass: de.ibmix.magkit.ui.dialogs.fields.ExtendedLinkBinder<br>
 * </code>
 *
 * @author frank.sommer
 * @since 28.11.2023
 */
public class ExtendedLinkConverter extends LinkConverter {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExtendedLinkConverter.class);
    private static final long serialVersionUID = 4484406102548210913L;

    public static final String TAG_ANCHOR = "#";
    public static final String TAG_QUERY = "?";
    public static final String TAG_SELECTOR = SelectorUtil.SELECTOR_DELIMITER;

    @Inject
    public ExtendedLinkConverter(JcrDatasource datasource) {
        super(datasource);
    }

    @Override
    public Result<String> convertToModel(String path, ValueContext context) {
        Result<String> result = null;

        if (isCoveredBySuperConverter(path)) {
            result = super.convertToModel(path, context);
        } else if (isPath(path)) {
            result = Result.of(() -> Exceptions.wrap().get(() -> convertToIdentifier(path)), Throwable::getMessage);
        }

        return result;
    }

    @Override
    public String convertToPresentation(String uuid, ValueContext context) {
        String result;

        if (isCoveredBySuperConverter(uuid)) {
            result = super.convertToPresentation(uuid, context);
        } else {
            result = convertToPath(uuid);
        }

        return result;
    }

    private String convertToIdentifier(final String pathWithSuffix) throws RepositoryException {
        String path = getNodePart(pathWithSuffix);
        String query = pathWithSuffix.replace(path, EMPTY);
        Node node = getNodeByPath(path);
        return node != null ? node.getIdentifier() + query : EMPTY;
    }

    protected String convertToPath(final String value) {
        String result = null;
        try {
            String identifier = getNodePart(value);
            String query = value.replace(identifier, EMPTY);
            Node node = getNodeByIdentifier(identifier);
            result = node != null ? node.getPath() + query : null;
        } catch (RepositoryException e) {
            LOGGER.error("Could not convert entry {} to path.", value, e);
        }
        return result;
    }

    private static String getNodePart(String value) {
        return substringBefore(substringBefore(substringBefore(value, TAG_SELECTOR), TAG_QUERY), TAG_ANCHOR);
    }

    private static boolean isCoveredBySuperConverter(String path) {
        return isBlank(path) || isExternalLink(path) || isAnchor(path) || !containsAny(path, TAG_ANCHOR, TAG_QUERY, TAG_SELECTOR);
    }

}
