package com.aperto.magkit.apps.ui;

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
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.Calendar;

import static de.ibmix.magkit.test.cms.context.ContextMockUtils.cleanContext;
import static de.ibmix.magkit.test.jcr.NodeMockUtils.mockNode;
import static de.ibmix.magkit.test.jcr.NodeStubbingOperation.stubProperty;
import static info.magnolia.ui.contentapp.column.jcr.JcrStatusColumnDefinition.ActivationStatus.Activated;
import static info.magnolia.ui.contentapp.column.jcr.JcrStatusColumnDefinition.ActivationStatus.Modified;
import static info.magnolia.ui.contentapp.column.jcr.JcrStatusColumnDefinition.ActivationStatus.NotActivated;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * Test ComparableStatusLabel.
 *
 * @author wolf.bubenik
 * @since 26.01.2018
 */
public class ComparableStatusLabelTest {

    private ComparableStatusLabel _label;
    private Node _node;

    @Before
    public void setUp() throws Exception {
        _node = mockNode("test");
        _label = new ComparableStatusLabel(_node);
    }

    @After
    public void tearDown() throws Exception {
        cleanContext();
    }

    // TODO: mock the type of the root node
    @Ignore
    @Test
    public void constructorTest() throws RepositoryException {
        ComparableStatusLabel label = new ComparableStatusLabel(_node);
        assertThat(label.getPrimaryStyleName(), is(NotActivated.getStyleName()));
        assertThat(label.getCaption(), is("not published"));

        stubProperty("mgnl:activationStatus", true).of(_node);
        label = new ComparableStatusLabel(_node);
        assertThat(label.getPrimaryStyleName(), is(Activated.getStyleName()));
        assertThat(label.getCaption(), is("published"));

        Calendar lastModified = Calendar.getInstance();
        Calendar lastActivated = Calendar.getInstance();
        lastActivated.add(Calendar.YEAR, -1);
        stubProperty("mgnl:lastModified", lastModified).of(_node);
        stubProperty("mgnl:lastActivated", lastActivated).of(_node);

        label = new ComparableStatusLabel(_node);
        assertThat(label.getPrimaryStyleName(), is(Modified.getStyleName()));
        assertThat(label.getCaption(), is("modified"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getActivationStatusTestNull() {
        _label.getActivationStatus(null);
    }

    // TODO: mock the type of the root node
    @Ignore
    @Test
    public void getActivationStatus() throws Exception {
        assertThat(_label.getActivationStatus(_node), is(0));
        stubProperty("mgnl:activationStatus", true).of(_node);
        assertThat(_label.getActivationStatus(_node), is(2));
    }

    @Test
    public void compareTo() {
        Label other = null;
        assertThat(_label.compareTo(other), is(1));

        other = new Label();
        assertThat(_label.compareTo(other), is(1));

        // This label is not activated
        other.setPrimaryStyleName(NotActivated.getStyleName());
        assertThat(_label.compareTo(other), is(0));

        other.setPrimaryStyleName(Modified.getStyleName());
        assertThat(_label.compareTo(other), is(1));

        other.setPrimaryStyleName(Activated.getStyleName());
        assertThat(_label.compareTo(other), is(1));

        // This label is modified
        _label.setPrimaryStyleName(Modified.getStyleName());
        assertThat(_label.compareTo(other), is(1));

        other.setPrimaryStyleName(Modified.getStyleName());
        assertThat(_label.compareTo(other), is(0));

        other.setPrimaryStyleName(NotActivated.getStyleName());
        assertThat(_label.compareTo(other), is(-1));

        // This label is activated
        _label.setPrimaryStyleName(Activated.getStyleName());
        assertThat(_label.compareTo(other), is(-1));

        other.setPrimaryStyleName(Modified.getStyleName());
        assertThat(_label.compareTo(other), is(-1));

        other.setPrimaryStyleName(Activated.getStyleName());
        assertThat(_label.compareTo(other), is(0));
    }

    @Test
    public void toIndex() {
        assertThat(_label.toIndex(null), is(0));
        assertThat(_label.toIndex(""), is(0));
        assertThat(_label.toIndex("any"), is(0));
        assertThat(_label.toIndex(Modified.getStyleName()), is(1));
        assertThat(_label.toIndex(NotActivated.getStyleName()), is(2));
    }
}
