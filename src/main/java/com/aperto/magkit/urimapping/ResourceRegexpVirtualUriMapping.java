package com.aperto.magkit.urimapping;

import info.magnolia.cms.beans.config.RegexpVirtualURIMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static info.magnolia.cms.core.Path.getValidatedLabel;
import static org.apache.commons.io.FilenameUtils.getBaseName;
import static org.apache.commons.lang.StringUtils.replace;

/**
 * Extends the {@link info.magnolia.cms.beans.config.RegexpVirtualURIMapping} by replacing dots by minus in filename.
 * Additionally makes it possible to set level explicitly (used to prioritize mappings when more than one pattern
 * matches).
 *
 * @author frank.sommer
 * @author immanuel.scheerer
 * @since 13.10.2010
 */
public class ResourceRegexpVirtualUriMapping extends RegexpVirtualURIMapping {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceRegexpVirtualUriMapping.class);

    /**
     * @see #updateLevelIfExplicitlyConfigured(info.magnolia.cms.beans.config.VirtualURIMapping.MappingResult)
     */
    private Integer _level = null;

    @Override
    //CHECKSTYLE:OFF
    public MappingResult mapURI(String uri) {
        //CHECKSTYLE:ON
        MappingResult mappingResult = super.mapURI(uri);
        if (mappingResult != null) {
            mappingResult.setToURI(updateToUri(mappingResult.getToURI()));
            updateLevelIfExplicitlyConfigured(mappingResult);
        }
        return mappingResult;
    }

    public Integer getLevel() {
        return _level;
    }

    public void setLevel(Integer level) {
        _level = level;
    }

    /**
     * Method is called when URI pattern matches, so that mapped URI (toUri) may be manipulated.
     * @param toUri toUri returned from superclass.
     * @return The new value to be used as toUri.
     */
    protected String updateToUri(final String toUri) {
        LOGGER.debug("Original mapping uri: {}.", toUri);
        String result = toUri;
        if (toUri.contains(".")) {
            // only perform basename replacement if basename can be determined properly (dot in URI necessary)
            String basename = getBaseName(toUri);
            String replacedName = getValidatedLabel(basename);

            // preserve prefix, so that replacement does not accidentally occur in prefix when basename also
            // appears in prefix (example: /path/path.jpg)
            String prefix = toUri.substring(0, toUri.lastIndexOf(basename));

            // perform replacement
            result = prefix + replace(toUri.substring(prefix.length()), basename, replacedName);
        }
        LOGGER.debug("New modified mapping uri: {}.", result);
        return result;
    }

    /**
     * MGKT-117: This method makes explicitly configuration of mapping level possible. In parent class, the level
     * is automatically determined based on the number of groups used in the regular expression
     * (see {@link RegexpVirtualURIMapping#mapURI(String)}. This results in a quite counterintuitive behaviour
     * if more than one pattern matches, since the number of groups does not say anything about the wanted
     * priorization.
     *
     * @param mappingResult The mapping result to update. May not be <code>null</code>.
     */
    private void updateLevelIfExplicitlyConfigured(MappingResult mappingResult) {
        if (_level != null) {
            LOGGER.debug("Level is explicitly set to {}.", _level);
            mappingResult.setLevel(_level);
        }
    }
}