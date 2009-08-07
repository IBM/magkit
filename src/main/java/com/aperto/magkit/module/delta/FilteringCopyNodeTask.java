package com.aperto.magkit.module.delta;

import javax.jcr.RepositoryException;

import static com.aperto.magkit.utils.ContentUtils.copyInSessionFiltered;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.HierarchyManager;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.AbstractRepositoryTask;
import info.magnolia.module.delta.TaskExecutionException;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLFilter;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * Copies a node by performing a in session copy operation. Additionally you may specify a {@link XMLFilter} that is
 * applied to
 *
 * @author Norman Wiechmann (Aperto AG)
 */
public class FilteringCopyNodeTask extends AbstractRepositoryTask {
    private final String _workspaceName;
    private final String _src;
    private final String _dest;
    private final boolean _overwrite;
    private final XMLFilter _xmlFilter;

    public FilteringCopyNodeTask(String name, String description, String workspaceName, String src, String dest, boolean overwrite, XMLFilter xmlFilter) {
        super(name, description);
        _workspaceName = workspaceName;
        _src = src;
        _dest = dest;
        _overwrite = overwrite;
        _xmlFilter = xmlFilter;
    }

    protected void doExecute(InstallContext installContext) throws RepositoryException, TaskExecutionException {
        HierarchyManager hm = installContext.getHierarchyManager(_workspaceName);
        if (hm.isExist(_dest)) {
            if (_overwrite) {
                hm.delete(_dest);
            } else {
                installContext.error("Can't copy " + _src + " to " + _dest + " because the target node already exists.", null);
                return;
            }
        }
        Content srcNode = hm.getContent(_src);
        try {
            copyInSessionFiltered(srcNode, _dest, _xmlFilter);
        } catch (RepositoryException e) {
            installContext.error("Can't copy " + _src + " to " + _dest, e);
        }
    }

    /**
     * A {@link XMLFilter} that performs a replacement of content of the sax characters event using a regular expression.
     */
    public static class ReplaceInValueFilter extends XMLFilterImpl {

        private static final String VALUE = "value";
        private final String _regex;
        private final String _replacement;
        private boolean _filterCharactersEnabled = false;

        public ReplaceInValueFilter(final String regex, final String replacement) {
            _regex = regex;
            _replacement = replacement;
        }

        @Override
        public void startDocument() throws SAXException {
            _filterCharactersEnabled = false;
            super.startDocument();
        }

        @Override
        public void startElement(final String uri, final String localName, final String qName, final Attributes atts) throws SAXException {
            if (VALUE.equalsIgnoreCase(localName)) {
                _filterCharactersEnabled = true;
            }
            super.startElement(uri, localName, qName, atts);
        }

        @Override
        public void characters(final char[] ch, final int start, final int length) throws SAXException {
            if (_filterCharactersEnabled && ch != null && ch.length > 0) {
                // CHECKSTYLE:OFF creating an instance of java.lang.String is required here
                String characters = new String(ch, start, length);
                // CHECKSTYLE:ON
                char[] newCh = characters.replaceAll(_regex, _replacement).toCharArray();
                super.characters(newCh, 0, newCh.length);
            } else {
                super.characters(ch, start, length);
            }
        }

        @Override
        public void endElement(final String uri, final String localName, final String qName) throws SAXException {
            if (VALUE.equalsIgnoreCase(localName)) {
                _filterCharactersEnabled = false;
            }
            super.endElement(uri, localName, qName);
        }

        @Override
        public void endDocument() throws SAXException {
            _filterCharactersEnabled = false;
            super.endDocument();
        }
    }
}