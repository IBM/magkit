package com.aperto.magkit.dialogs.fields;

import info.magnolia.ui.form.field.converter.BaseIdentifierToPathConverter;

import java.util.Locale;

import static com.aperto.magkit.utils.LinkTool.isExternalLink;
import static org.apache.commons.lang.StringUtils.isNotBlank;

/**
 * Handles input of links by urls (external) or absolute paths (internal).
 *
 * @author philipp.guettler
 * @since 06.03.14
 */
public class LinkConverter extends BaseIdentifierToPathConverter {

    @Override
    public String convertToModel(final String path, final Class<? extends String> targetType, final Locale locale) {
        String result = path;
        if (isNotBlank(path) && !isExternalLink(path) && !isAnchor(path)) {
            result = super.convertToModel(path, targetType, locale);
        }
        return result;
    }

    @Override
    public String convertToPresentation(final String uuid, final Class<? extends String> targetType, final Locale locale) {
        String result = uuid;
        if (isNotBlank(uuid) && !isExternalLink(uuid) && !isAnchor(uuid)) {
            result = super.convertToPresentation(uuid, targetType, locale);
        }
        return result;
    }

    private boolean isAnchor(final String value) {
        return value.startsWith("#");
    }
}
