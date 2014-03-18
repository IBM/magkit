package com.aperto.magkit.controls;

import info.magnolia.cms.beans.config.ContentRepository;
import info.magnolia.cms.gui.dialog.UUIDDialogControl;

/**
 * Multi select control storing nodes as UUIDs rather then paths.
 *
 * @author jan.haderka
 */
public class UuidMultiSelect extends OrderingMultiSelect implements UUIDDialogControl {

    /**
     * Gets repository path.
     *
     * @return Current repository path.
     * @see info.magnolia.cms.gui.dialog.UUIDDialogControl#getRepository()
     */
    public String getRepository() {
        return getConfigValue("repository", ContentRepository.WEBSITE);
    }

}
