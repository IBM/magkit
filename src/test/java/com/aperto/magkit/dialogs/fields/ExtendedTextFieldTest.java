package com.aperto.magkit.dialogs.fields;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * Test the [@link ExtendedTextField}.
 *
 * @author frank.sommer
 */
public class ExtendedTextFieldTest {

    @Test
    public void testJustRecommendedLength() throws Exception {
        final ExtendedTextField extendedTextField = new ExtendedTextField(createDefinition(-1, 10), null);
        assertThat(extendedTextField.determineLabelMaxLength(), is(10));
    }

    @Test
    public void testJustMaxLength() throws Exception {
        final ExtendedTextField extendedTextField = new ExtendedTextField(createDefinition(10, -1), null);
        assertThat(extendedTextField.determineLabelMaxLength(), is(10));
    }

    @Test
    public void testMaxGtRecommendedLength() throws Exception {
        final ExtendedTextField extendedTextField = new ExtendedTextField(createDefinition(10, 20), null);
        assertThat(extendedTextField.determineLabelMaxLength(), is(10));
    }

    @Test
    public void testMaxLtRecommendedLength() throws Exception {
        final ExtendedTextField extendedTextField = new ExtendedTextField(createDefinition(20, 10), null);
        assertThat(extendedTextField.determineLabelMaxLength(), is(10));
    }

    private ExtendedTextFieldDefinition createDefinition(int max, int recommend) {
        final ExtendedTextFieldDefinition definition = new ExtendedTextFieldDefinition();
        definition.setMaxLength(max);
        definition.setRecommendedLength(recommend);
        return definition;
    }
}