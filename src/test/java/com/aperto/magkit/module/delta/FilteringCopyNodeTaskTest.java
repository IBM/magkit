package com.aperto.magkit.module.delta;

import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * TODO: comment.
 *
 * @author Norman Wiechmann (Aperto AG)
 */
public class FilteringCopyNodeTaskTest {
    private static final String FOO = "foo";
    private static final String VALUE = "value";
    private static final char[] HELLO = "{hallo}".toCharArray();

    @Test
    public void replaceInValueFilter() throws SAXException {
        CharacterCollector testHandler = new CharacterCollector();
        FilteringCopyNodeTask.ReplaceInValueFilter filter = new FilteringCopyNodeTask.ReplaceInValueFilter("\\{hallo\\}", "hi");
        filter.setContentHandler(testHandler);
        filter.startElement("", FOO, FOO, null);
        filter.startElement("", FOO, FOO, null);
        filter.characters(HELLO, 0, HELLO.length);
        filter.endElement("", FOO, FOO);
        filter.startElement("", VALUE, VALUE, null);
        filter.characters(HELLO, 0, HELLO.length);
        filter.endElement("", VALUE, VALUE);
        filter.startElement("", FOO, FOO, null);
        filter.characters(HELLO, 0, HELLO.length);
        filter.endElement("", FOO, FOO);
        filter.endElement("", FOO, FOO);
        Assert.assertThat(testHandler.getCharacters(), CoreMatchers.equalTo("{hallo}hi{hallo}"));
    }

    /**
     * Simple {@link org.xml.sax.ContentHandler ContentHandler} that collects all character events.
     */
    public class CharacterCollector extends DefaultHandler {
        private StringBuilder _characters = new StringBuilder();

        @Override
        public void characters(final char[] ch, final int start, final int length) throws SAXException {
            _characters.append(ch, start, length);
        }

        public String getCharacters() {
            return _characters.toString();
        }
    }
}
