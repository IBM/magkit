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

import com.vaadin.ui.Label;
import de.ibmix.magkit.test.cms.context.ContextMockUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.Calendar;

import static de.ibmix.magkit.test.cms.context.ContextMockUtils.cleanContext;
import static de.ibmix.magkit.test.cms.node.MagnoliaNodeStubbingOperation.stubActivationStatus;
import static de.ibmix.magkit.test.cms.node.MagnoliaNodeStubbingOperation.stubLastActivated;
import static de.ibmix.magkit.test.cms.node.MagnoliaNodeStubbingOperation.stubLastModified;
import static de.ibmix.magkit.test.jcr.NodeMockUtils.mockNode;
import static info.magnolia.ui.contentapp.column.jcr.JcrStatusColumnDefinition.ActivationStatus.Activated;
import static info.magnolia.ui.contentapp.column.jcr.JcrStatusColumnDefinition.ActivationStatus.Modified;
import static info.magnolia.ui.contentapp.column.jcr.JcrStatusColumnDefinition.ActivationStatus.NotActivated;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link ComparableStatusLabel} comparing logic and status mapping.
 *
 * @author wolf.bubenik
 * @since 2018-01-26
 */
public class ComparableStatusLabelTest {

    private ComparableStatusLabel _label;
    private Node _node;

    @BeforeEach
    public void setUp() throws Exception {
        ContextMockUtils.cleanContext();
        _node = mockNode("test");
        _label = new ComparableStatusLabel(_node);
    }

    @AfterAll
    public static void tearDown() throws Exception {
        cleanContext();
    }

    @Test
    public void constructorTest() throws RepositoryException {
        ComparableStatusLabel label = new ComparableStatusLabel(_node);
        assertEquals(NotActivated.getStyleName(), label.getPrimaryStyleName());
        assertEquals("not published", label.getCaption());

        stubActivationStatus(true).of(_node);
        label = new ComparableStatusLabel(_node);
        assertEquals(Activated.getStyleName(), label.getPrimaryStyleName());
        assertEquals("published", label.getCaption());

        Calendar lastModified = Calendar.getInstance();
        Calendar lastActivated = Calendar.getInstance();
        lastActivated.add(Calendar.YEAR, -1);
        stubLastModified(lastModified).of(_node);
        stubLastActivated(lastActivated).of(_node);

        label = new ComparableStatusLabel(_node);
        assertEquals(Modified.getStyleName(), label.getPrimaryStyleName());
        assertEquals("modified", label.getCaption());
    }

    @Test
    public void getActivationStatusTestNull() {
        assertThrows(IllegalArgumentException.class, () -> _label.getActivationStatus(null));
    }

    @Test
    public void getActivationStatus() throws Exception {
        assertEquals(0, _label.getActivationStatus(_node));
        stubActivationStatus(true).of(_node);
        assertEquals(2, _label.getActivationStatus(_node));
    }

    @Test
    public void compareTo() {
        Label other = null;
        assertEquals(1, _label.compareTo(other));

        other = new Label();
        assertEquals(1, _label.compareTo(other));

        // This label is not activated
        other.setPrimaryStyleName(NotActivated.getStyleName());
        assertEquals(0, _label.compareTo(other));

        other.setPrimaryStyleName(Modified.getStyleName());
        assertEquals(1, _label.compareTo(other));

        other.setPrimaryStyleName(Activated.getStyleName());
        assertEquals(1, _label.compareTo(other));

        // This label is modified
        _label.setPrimaryStyleName(Modified.getStyleName());
        assertEquals(1, _label.compareTo(other));

        other.setPrimaryStyleName(Modified.getStyleName());
        assertEquals(0, _label.compareTo(other));

        other.setPrimaryStyleName(NotActivated.getStyleName());
        assertEquals(-1, _label.compareTo(other));

        // This label is activated
        _label.setPrimaryStyleName(Activated.getStyleName());
        assertEquals(-1, _label.compareTo(other));

        other.setPrimaryStyleName(Modified.getStyleName());
        assertEquals(-1, _label.compareTo(other));

        other.setPrimaryStyleName(Activated.getStyleName());
        assertEquals(0, _label.compareTo(other));
    }

    @Test
    public void toIndex() {
        assertEquals(0, _label.toIndex(null));
        assertEquals(0, _label.toIndex(""));
        assertEquals(0, _label.toIndex("any"));
        assertEquals(1, _label.toIndex(Modified.getStyleName()));
        assertEquals(2, _label.toIndex(NotActivated.getStyleName()));
    }
}
