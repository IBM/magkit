package com.aperto.magkit.controls;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.core.Content;
import info.magnolia.cms.core.NodeData;
import info.magnolia.cms.gui.dialog.DialogMultiSelect;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Multi select control storing nodes as UUIDs rather then paths.
 * @author jan.haderka
 *
 */
public class OrderingMultiSelect extends DialogMultiSelect {

    /**
     * Gets repository path.
     * @see info.magnolia.cms.gui.dialog.UUIDDialogControl#getRepository()
     * @return Current repository path.
     */
    public String getRepository() {
        return getConfigValue("repository", ContentRepository.WEBSITE);
    }

    /**
     * Reads and formats values for this control.
     * @see info.magnolia.cms.gui.dialog.DialogControlImpl#readValues()
     */
    protected List readValues() {
        List values = new ArrayList();
        Map<String, String> tmp = new HashMap<String, String>();
        Content cnt = getStorageNode();
        if (cnt != null) {
            try {                 
                Iterator it = cnt.getContent(getName()).getNodeDataCollection().iterator();
                while (it.hasNext()) {
                    NodeData data = (NodeData) it.next();
                    tmp.put(data.getName(), data.getString());
                }
                SortedSet<String> s = new TreeSet<String>(new Comparator<String>() {

                    public int compare(String o1, String o2) {
                        int ret = 0;
                    
                        if (o1.length() < o2.length()) {
                            ret = -1;
                        } else if (o2.length() < o1.length()) {
                            ret = 1;
                        } else {
                            ret = o1.compareTo(o2);
                        }
                        return ret;
                    }
                });
                s.addAll(tmp.keySet());
                it = s.iterator();
                while (it.hasNext()) {
                    values.add(tmp.get(it.next()));
                }
            } catch (PathNotFoundException e) {
                // not yet existing: OK
            } catch (RepositoryException re) {
                // should not happen.
            }
        }
        return values;
    }

}
