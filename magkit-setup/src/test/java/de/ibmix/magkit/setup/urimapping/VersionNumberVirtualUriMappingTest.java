package de.ibmix.magkit.setup.urimapping;

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

import info.magnolia.virtualuri.VirtualUriMapping;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Test for {@link VersionNumberVirtualUriMapping}.
 *
 * @author daniel.kasmeroglu@aperto.de
 */
public class VersionNumberVirtualUriMappingTest {
    private VersionNumberVirtualUriMapping _missingSlashAtTheEnd;
    private VersionNumberVirtualUriMapping _withSlashAtTheEnd;
    private VersionNumberVirtualUriMapping _withToUri;
    private VersionNumberVirtualUriMapping _svnMapping;

    /**
     * Prepares reusable mapping instances with different configurations.
     */
    @BeforeEach
    public void before() {
        _missingSlashAtTheEnd = new VersionNumberVirtualUriMapping();
        _missingSlashAtTheEnd.setFromPrefix("/templates/theme/gollum");

        _withSlashAtTheEnd = new VersionNumberVirtualUriMapping();
        _withSlashAtTheEnd.setFromPrefix("/templates/theme/gollum/");

        _withToUri = new VersionNumberVirtualUriMapping();
        _withToUri.setFromPrefix("/templates/theme/gollum/");
        _withToUri.setToUri("/schnuffeltuch/%s/oops.html");

        _svnMapping = new VersionNumberVirtualUriMapping();
        _svnMapping.setFromPrefix("/templates/theme/gollum/");
        _svnMapping.setPattern(VersionNumberVirtualUriMapping.SVN_PATTERN);
    }

    /**
     * Ensures URIs without a version segment are not mapped.
     *
     * @throws Exception when URI creation fails
     */
    @Test
    public void notMatching() throws Exception {
        assertNull(mapUri(_missingSlashAtTheEnd, "/templates/theme/gollum/a.html"));
        assertNull(mapUri(_withSlashAtTheEnd, "/templates/theme/gollum/a.html"));
        assertNull(mapUri(_withToUri, "/templates/theme/gollum/a.html"));
    }

    /**
     * Verifies matching using the SVN pattern and the getter for the pattern value.
     *
     * @throws Exception when URI creation fails
     */
    @Test
    public void svnMatching() throws Exception {
        assertEquals(VersionNumberVirtualUriMapping.SVN_PATTERN, _svnMapping.getPattern());
        assertEquals("/templates/theme/gollum/a.html", mapUri(_svnMapping, "/templates/theme/gollum/1.2.1/a.html").getToUri());
    }

    /**
     * Verifies matching using various git style version segments and custom target formatting.
     *
     * @throws Exception when URI creation fails
     */
    @Test
    public void gitMatching() throws Exception {
        assertEquals("/templates/theme/gollum/a.html", mapUri(_missingSlashAtTheEnd, "/templates/theme/gollum/1.0.0-master-4-d760a70/a.html").getToUri());
        assertEquals("/templates/theme/gollum/a.html", mapUri(_missingSlashAtTheEnd, "/templates/theme/gollum/1.0-master-124-d760a70/a.html").getToUri());
        assertEquals("/templates/theme/gollum/a.html", mapUri(_withSlashAtTheEnd, "/templates/theme/gollum/1.0.0-master-44698-d760a70/a.html").getToUri());
        assertEquals("/templates/theme/gollum/a.html", mapUri(_withSlashAtTheEnd, "/templates/theme/gollum/1.0-master-4-d760a70/a.html").getToUri());
        assertEquals("/schnuffeltuch/a/oops.html", mapUri(_withToUri, "/templates/theme/gollum/1.0.0-master-124-d760a70/a").getToUri());
        assertEquals("/schnuffeltuch/a/oops.html", mapUri(_withToUri, "/templates/theme/gollum/1.0-master-44-d760a70/a").getToUri());
    }

    /**
     * Ensures an invalid version segment does not produce a mapping result.
     *
     * @throws Exception when URI creation fails
     */
    @Test
    public void invalidVersionSegmentNotMapped() throws Exception {
        assertNull(mapUri(_withSlashAtTheEnd, "/templates/theme/gollum/abc/a.html"));
    }

    /**
     * Ensures a missing fromPrefix leads to no mapping being produced.
     *
     * @throws Exception when URI creation fails
     */
    @Test
    public void missingFromPrefixDoesNotMap() throws Exception {
        VersionNumberVirtualUriMapping mapping = new VersionNumberVirtualUriMapping();
        assertNull(mapUri(mapping, "/templates/theme/gollum/1.0.0-master-4-d760a70/a.html"));
    }

    /**
     * Verifies the level configured on the mapping instance is propagated to the result.
     *
     * @throws Exception when URI creation fails
     */
    @Test
    public void levelPropagation() throws Exception {
        VersionNumberVirtualUriMapping mapping = new VersionNumberVirtualUriMapping();
        mapping.setFromPrefix("/templates/theme/gollum/");
        mapping.setLevel(42);
        VirtualUriMapping.Result result = mapUri(mapping, "/templates/theme/gollum/1.0.0-master-4-d760a70/a.html");
        assertEquals(42, result.getWeight());
    }

    /**
     * Verifies isValid() depends on both fromPrefix and toUri configuration.
     */
    @Test
    public void isValidStateTransition() {
        VersionNumberVirtualUriMapping mapping = new VersionNumberVirtualUriMapping();
        assertFalse(mapping.isValid());
        mapping.setFromPrefix("/templates/theme/gollum");
        assertFalse(mapping.isValid());
        mapping.setToUri("/templates/theme/%s");
        assertTrue(mapping.isValid());
    }

    /**
     * Verifies setPattern(null) resets to the default git pattern and still allows matching.
     *
     * @throws Exception when URI creation fails
     */
    @Test
    public void setPatternNullResetsToDefault() throws Exception {
        VersionNumberVirtualUriMapping mapping = new VersionNumberVirtualUriMapping();
        mapping.setFromPrefix("/templates/theme/gollum/");
        mapping.setPattern(null);
        assertEquals(VersionNumberVirtualUriMapping.GIT_PATTERN, mapping.getPattern());
        assertEquals("/templates/theme/gollum/a.html", mapUri(mapping, "/templates/theme/gollum/1.0.0-master-4-d760a70/a.html").getToUri());
    }

    /**
     * Verifies toString() contains informative data (pattern and prefix at least).
     */
    @Test
    public void toStringContainsConfiguration() {
        String value = _withToUri.toString();
        assertTrue(value.contains(VersionNumberVirtualUriMapping.GIT_PATTERN));
        assertTrue(value.contains("/templates/theme/gollum/"));
        assertTrue(value.contains("..."));
    }

    /**
     * Ensures URIs with a version segment but no following slash are not mapped.
     *
     * @throws Exception when URI creation fails
     */
    @Test
    public void versionSegmentWithoutRemainderNotMapped() throws Exception {
        assertNull(mapUri(_withSlashAtTheEnd, "/templates/theme/gollum/1.0.0-master-4-d760a70"));
    }

    /**
     * Verifies setFromPrefix() enforces a trailing slash when missing.
     */
    @Test
    public void fromPrefixNormalizationAddsTrailingSlash() {
        VersionNumberVirtualUriMapping mapping = new VersionNumberVirtualUriMapping();
        mapping.setFromPrefix("/templates/theme/gollum");
        assertEquals("/templates/theme/gollum/", mapping.getFromPrefix());
    }

    /**
     * Helper converting a string URI to a mapping result or null for empty optional.
     *
     * @param mapping the mapping instance used for resolution
     * @param uri the URI string to resolve
     * @return the mapping result or null if no mapping applies
     * @throws URISyntaxException when the URI cannot be parsed
     */
    private VirtualUriMapping.Result mapUri(VersionNumberVirtualUriMapping mapping, String uri) throws URISyntaxException {
        Optional<VirtualUriMapping.Result> result = mapping.mapUri(new URI(uri));
        assertNotNull(result);
        return result.orElse(null);
    }

    /**
     * Verifies custom toUri without placeholder is processed correctly.
     *
     * @throws Exception when URI creation fails
     */
    @Test
    public void customToUriWithoutPlaceholder() throws Exception {
        VersionNumberVirtualUriMapping mapping = new VersionNumberVirtualUriMapping();
        mapping.setFromPrefix("/templates/theme/gollum/");
        mapping.setToUri("/fixed");
        VirtualUriMapping.Result result = mapUri(mapping, "/templates/theme/gollum/1.0.0-master-4-d760a70/a.html");
        assertEquals("/fixed", result.getToUri());
    }

    /**
     * Verifies that a custom pattern preventing any match yields no mapping result.
     *
     * @throws Exception when URI creation fails
     */
    @Test
    public void invalidCustomPatternPreventsMapping() throws Exception {
        VersionNumberVirtualUriMapping mapping = new VersionNumberVirtualUriMapping();
        mapping.setFromPrefix("/templates/theme/gollum/");
        mapping.setPattern("^abc$");
        assertNull(mapUri(mapping, "/templates/theme/gollum/1.0.0-master-4-d760a70/a.html"));
    }

    /**
     * Verifies default level value (1) is propagated when not explicitly set.
     *
     * @throws Exception when URI creation fails
     */
    @Test
    public void defaultLevelIsOne() throws Exception {
        VirtualUriMapping.Result result = mapUri(_withSlashAtTheEnd, "/templates/theme/gollum/1.0.0-master-4-d760a70/a.html");
        assertEquals(1, result.getWeight());
    }

    /**
     * Ensures URIs with an empty version segment (double slash) are not mapped.
     *
     * @throws Exception when URI creation fails
     */
    @Test
    public void emptyVersionSegmentNotMapped() throws Exception {
        assertNull(mapUri(_withSlashAtTheEnd, "/templates/theme/gollum//a.html"));
    }
}
