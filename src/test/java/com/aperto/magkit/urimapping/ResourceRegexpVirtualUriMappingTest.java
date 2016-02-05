package com.aperto.magkit.urimapping;

import info.magnolia.cms.beans.config.VirtualURIMapping;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Test for {@link ResourceRegexpVirtualUriMapping}.
 *
 * @author Immanuel Scheerer (Aperto AG)
 * @since 18.07.11
 */
public class ResourceRegexpVirtualUriMappingTest {
    private ResourceRegexpVirtualUriMapping _underTest;
    private ResourceRegexpVirtualUriMapping _themeTest;

    @Before
    public void before() {
      
        _underTest = new ResourceRegexpVirtualUriMapping();
        _underTest.setFromURI("^/basepath/(.*)$");
        _underTest.setToURI("forward:/resources/basepath/$1");
        
        // see ApertoThemeUtils
        _themeTest = new ResourceRegexpVirtualUriMapping();
        _themeTest.setFromURI("^/resources/templating-kit/themes/(gollum)/(?:(?:[\\.0-9]+|[0-9]+\\.[0-9]+(?:\\.[0-9]+)?-[a-zA-Z0-9]+-[0-9]+-[0-9a-fA-F]{7}))/(.+)$");
        _themeTest.setToURI("forward:/resources/templating-kit/themes/$1/$2");
        
    }

    @Test
    public void testMapUri() {
        VirtualURIMapping.MappingResult result = _underTest.mapURI("/basepath/a/path/resource.ext");
        assertEquals("forward:/resources/basepath/a/path/resource.ext", result.getToURI());
    }

    @Test
    public void testMapUriWithoutDotInUri() {
        VirtualURIMapping.MappingResult result = _underTest.mapURI("/basepath/a/path/without/dot");
        assertEquals("forward:/resources/basepath/a/path/without/dot", result.getToURI());
    }

    @Ignore
    @Test
    public void testMapUriWithMultipleDotsInUri() {
        VirtualURIMapping.MappingResult result = _underTest.mapURI("/basepath/a/path/resource.test.ext");
        assertEquals("forward:/resources/basepath/a/path/resource-test.ext", result.getToURI());
    }

    @Test
    public void testImplicitLevel() {
        Assert.assertNull(_underTest.getLevel());
        VirtualURIMapping.MappingResult result = _underTest.mapURI("/basepath/a/path/resource.ext");
        assertEquals("forward:/resources/basepath/a/path/resource.ext", result.getToURI());
        assertEquals("Implicit level must be number of groups in regular expression + 1", 2, result.getLevel());
    }

    @Test
    public void testExplicitLevel() {
        _underTest.setLevel(10);
        assertEquals(Integer.valueOf(10), _underTest.getLevel());
        VirtualURIMapping.MappingResult result = _underTest.mapURI("/basepath/a/path/resource.ext");
        assertEquals("forward:/resources/basepath/a/path/resource.ext", result.getToURI());
        assertEquals("Explicit level must be returned", 10, result.getLevel());
    }
    
    @Test
    public void testSvnVersion() {
        VirtualURIMapping.MappingResult result = _themeTest.mapURI("/resources/templating-kit/themes/gollum/2.1.2/hello/world.html");
        assertEquals("forward:/resources/templating-kit/themes/gollum/hello/world.html", result.getToURI());
    }

    @Test
    public void testGitVersion() {
        VirtualURIMapping.MappingResult result = _themeTest.mapURI("/resources/templating-kit/themes/gollum/1.2.3-master-512-fad31da/hello/world.html");
        assertEquals("forward:/resources/templating-kit/themes/gollum/hello/world.html", result.getToURI());
    }

    @Test
    public void testSvnVersionFail() {
        VirtualURIMapping.MappingResult result = _themeTest.mapURI("/resources/templating-kit/themes/gollum/2_1.2/hello/world.html");
        assertNull(result);
    }

    @Test
    public void testGitVersionFail() {
        VirtualURIMapping.MappingResult result = _themeTest.mapURI("/resources/templating-kit/themes/gollum/1.2.3-master-512-fa31da/hello/world.html");
        assertNull(result);
    }

}
