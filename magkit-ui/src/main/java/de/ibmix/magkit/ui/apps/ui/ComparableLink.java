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

