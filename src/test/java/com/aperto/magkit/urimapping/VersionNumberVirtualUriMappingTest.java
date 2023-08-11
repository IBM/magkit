package com.aperto.magkit.urimapping;

import info.magnolia.virtualuri.VirtualUriMapping;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

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

    @Before
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

    @Test
    public void notMatching() throws Exception {
        assertNull(mapUri(_missingSlashAtTheEnd, "/templates/theme/gollum/a.html"));
        assertNull(mapUri(_withSlashAtTheEnd, "/templates/theme/gollum/a.html"));
        assertNull(mapUri(_withToUri, "/templates/theme/gollum/a.html"));
    }

    @Test
    public void svnMatching() throws Exception {
        assertEquals("/templates/theme/gollum/a.html", mapUri(_svnMapping, "/templates/theme/gollum/1.2.1/a.html").getToUri());
    }

    @Test
    public void gitMatching() throws Exception {
        assertEquals("/templates/theme/gollum/a.html", mapUri(_missingSlashAtTheEnd, "/templates/theme/gollum/1.0.0-master-4-d760a70/a.html").getToUri());
        assertEquals("/templates/theme/gollum/a.html", mapUri(_missingSlashAtTheEnd, "/templates/theme/gollum/1.0-master-124-d760a70/a.html").getToUri());
        assertEquals("/templates/theme/gollum/a.html", mapUri(_withSlashAtTheEnd, "/templates/theme/gollum/1.0.0-master-44698-d760a70/a.html").getToUri());
        assertEquals("/templates/theme/gollum/a.html", mapUri(_withSlashAtTheEnd, "/templates/theme/gollum/1.0-master-4-d760a70/a.html").getToUri());
        assertEquals("/schnuffeltuch/a/oops.html", mapUri(_withToUri, "/templates/theme/gollum/1.0.0-master-124-d760a70/a").getToUri());
        assertEquals("/schnuffeltuch/a/oops.html", mapUri(_withToUri, "/templates/theme/gollum/1.0-master-44-d760a70/a").getToUri());
    }

    private VirtualUriMapping.Result mapUri(VersionNumberVirtualUriMapping mapping, String uri) throws URISyntaxException {
        Optional<VirtualUriMapping.Result> result = mapping.mapUri(new URI(uri));
        assertNotNull(result);
        return result.orElse(null);
    }
}
