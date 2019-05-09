package com.aperto.magkit.apps.ui;

import com.vaadin.server.ExternalResource;
import com.vaadin.ui.Link;

import javax.annotation.Nullable;
import java.text.Collator;
import java.util.Locale;

/**
 * Link to be comparable.
 *
 * @author oliver.emke
 * @since 19.01.17
 */
public class ComparableLink extends Link implements Comparable<Link> {

    private static final long serialVersionUID = -6830522377462589854L;
    private static final Collator COLLATOR = Collator.getInstance(Locale.GERMAN);

    public ComparableLink(String title, ExternalResource externalResource) {
        super(title, externalResource);
        setPrimaryStyleName("v-label");
    }

    @Override
    public int compareTo(@Nullable Link link) {
        int result;
        if (link == null || link.getCaption() == null) {
            result = 1;
        } else if (getCaption() == null) {
            result = -1;
        } else {
            result = COLLATOR.compare(getCaption(), link.getCaption());
        }
        return result;
    }
}

