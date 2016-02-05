package com.aperto.magkit.urimapping;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

import info.magnolia.cms.beans.config.VirtualURIMapping;

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
    public void notMatching() {
        assertNull(_missingSlashAtTheEnd.mapURI("/templates/theme/gollum/a.html"));
        assertNull(_withSlashAtTheEnd.mapURI("/templates/theme/gollum/a.html"));
        assertNull(_withToUri.mapURI("/templates/theme/gollum/a.html"));
    }
    
    @Test
    public void svnMatching() {
        assertEquals("/templates/theme/gollum/a.html", mapUri(_svnMapping, "/templates/theme/gollum/1.2.1/a.html").getToURI());
    }

    @Test
    public void gitMatching() {
        assertEquals("/templates/theme/gollum/a.html", mapUri(_missingSlashAtTheEnd, "/templates/theme/gollum/1.0.0-master-4-d760a70/a.html").getToURI());
        assertEquals("/templates/theme/gollum/a.html", mapUri(_missingSlashAtTheEnd, "/templates/theme/gollum/1.0-master-124-d760a70/a.html").getToURI());
        assertEquals("/templates/theme/gollum/a.html", mapUri(_withSlashAtTheEnd, "/templates/theme/gollum/1.0.0-master-44698-d760a70/a.html").getToURI());
        assertEquals("/templates/theme/gollum/a.html", mapUri(_withSlashAtTheEnd, "/templates/theme/gollum/1.0-master-4-d760a70/a.html").getToURI());
        assertEquals("/schnuffeltuch/a/oops.html", mapUri(_withToUri, "/templates/theme/gollum/1.0.0-master-124-d760a70/a").getToURI());
        assertEquals("/schnuffeltuch/a/oops.html", mapUri(_withToUri, "/templates/theme/gollum/1.0-master-44-d760a70/a").getToURI());
    }

    private VirtualURIMapping.MappingResult mapUri(VersionNumberVirtualUriMapping mapping, String uri) {
        VirtualURIMapping.MappingResult result = mapping.mapURI(uri);
        assertNotNull(result);
        return result;
    }

}
