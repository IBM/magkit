package com.aperto.magkit.apps.ui;


import com.vaadin.ui.Label;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.util.Calendar;

import static com.aperto.magkit.mockito.ContextMockUtils.cleanContext;
import static com.aperto.magkit.mockito.jcr.NodeMockUtils.mockNode;
import static com.aperto.magkit.mockito.jcr.NodeStubbingOperation.stubProperty;
import static info.magnolia.ui.workbench.column.StatusColumnFormatter.ActivationStatus.ACTIVATED;
import static info.magnolia.ui.workbench.column.StatusColumnFormatter.ActivationStatus.MODIFIED;
import static info.magnolia.ui.workbench.column.StatusColumnFormatter.ActivationStatus.NOT_ACTIVATED;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

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
        cleanContext();
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
        assertThat(label.getPrimaryStyleName(), is(NOT_ACTIVATED.getStyleName()));
        assertThat(label.getCaption(), is("not published"));

        stubProperty("mgnl:activationStatus", true).of(_node);
        label = new ComparableStatusLabel(_node);
        assertThat(label.getPrimaryStyleName(), is(ACTIVATED.getStyleName()));
        assertThat(label.getCaption(), is("published"));

        Calendar lastModified = Calendar.getInstance();
        Calendar lastActivated = Calendar.getInstance();
        lastActivated.add(Calendar.YEAR, -1);
        stubProperty("mgnl:lastModified", lastModified).of(_node);
        stubProperty("mgnl:lastActivated", lastActivated).of(_node);

        label = new ComparableStatusLabel(_node);
        assertThat(label.getPrimaryStyleName(), is(MODIFIED.getStyleName()));
        assertThat(label.getCaption(), is("modified"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getActivationStatusTestNull() throws Exception {
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
    public void compareTo() throws Exception {
        Label other = null;
        assertThat(_label.compareTo(other), is(1));

        other = new Label();
        assertThat(_label.compareTo(other), is(1));

        // This label is not activated
        other.setPrimaryStyleName(NOT_ACTIVATED.getStyleName());
        assertThat(_label.compareTo(other), is(0));

        other.setPrimaryStyleName(MODIFIED.getStyleName());
        assertThat(_label.compareTo(other), is(1));

        other.setPrimaryStyleName(ACTIVATED.getStyleName());
        assertThat(_label.compareTo(other), is(1));

        // This label is modified
        _label.setPrimaryStyleName(MODIFIED.getStyleName());
        assertThat(_label.compareTo(other), is(1));

        other.setPrimaryStyleName(MODIFIED.getStyleName());
        assertThat(_label.compareTo(other), is(0));

        other.setPrimaryStyleName(NOT_ACTIVATED.getStyleName());
        assertThat(_label.compareTo(other), is(-1));

        // This label is activated
        _label.setPrimaryStyleName(ACTIVATED.getStyleName());
        assertThat(_label.compareTo(other), is(-1));

        other.setPrimaryStyleName(MODIFIED.getStyleName());
        assertThat(_label.compareTo(other), is(-1));

        other.setPrimaryStyleName(ACTIVATED.getStyleName());
        assertThat(_label.compareTo(other), is(0));
    }

    @Test
    public void toIndex() throws Exception {
        assertThat(_label.toIndex(null), is(0));
        assertThat(_label.toIndex(""), is(0));
        assertThat(_label.toIndex("any"), is(0));
        assertThat(_label.toIndex(MODIFIED.getStyleName()), is(1));
        assertThat(_label.toIndex(NOT_ACTIVATED.getStyleName()), is(2));
    }
}