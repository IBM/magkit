package de.ibmix.magkit.ui.apps.ui;

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

import com.vaadin.server.ExternalResource;
import com.vaadin.ui.Link;
import jakarta.annotation.Nullable;

import java.text.Collator;
import java.util.Locale;

/**
 * Vaadin {@link Link} implementation that offers locale aware ordering by caption text.
 * <p>
 * Instances are comparable using a German locale {@link Collator}. A {@code null} other link or a link with a
 * {@code null} caption is always considered greater so that links with valid captions appear first when sorted
 * ascending. This behavior supports deterministic sorting in UI tables containing optional link captions.
 * </p>
 * <p>Key features:</p>
 * <ul>
 *   <li>Locale specific comparison using a pre-created {@link Collator} (German locale).</li>
 *   <li>Graceful handling of {@code null} links and {@code null} captions without throwing exceptions.</li>
 *   <li>Applies Magnolia/Vaadin label primary style for visual consistency.</li>
 * </ul>
 *
 * <p>
 * Thread-safety: Not thread-safe; must only be accessed from the Vaadin UI thread.
 * </p>
 *
 * @author oliver.emke
 * @since 2017-01-19
 */
public class ComparableLink extends Link implements Comparable<Link> {

    private static final long serialVersionUID = -6830522377462589854L;
    private static final Collator COLLATOR = Collator.getInstance(Locale.GERMAN);

    /**
     * Create a comparable link.
     *
     * @param title the caption shown for the link; may be {@code null}
     * @param externalResource the external resource (URL) to open when the link is activated; may be {@code null}
     */
    public ComparableLink(String title, ExternalResource externalResource) {
        super(title, externalResource);
        setPrimaryStyleName("v-label");
    }

    /**
     * Compare this link to another link using locale aware caption ordering.
     * <p>Ordering rules:</p>
     * <ul>
     *   <li>This link with a non-null caption sorts before a {@code null} link or a link with {@code null} caption.</li>
     *   <li>If both captions are non-null, {@link Collator#compare(String, String)} provides the comparison result.</li>
     * </ul>
     *
     * @param link the other link to compare; may be {@code null}
     * @return negative, zero or positive consistent with the {@link Comparable} contract
     */
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
