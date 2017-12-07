package com.aperto.magkit.dialogs.fields;

import com.aperto.magkit.utils.ExtendedLinkFieldHelper;
import com.vaadin.v7.data.Item;
import com.vaadin.v7.data.Property;
import com.vaadin.v7.data.util.PropertysetItem;
import info.magnolia.ui.form.field.definition.ConfiguredFieldDefinition;
import info.magnolia.ui.framework.i18n.DefaultI18NAuthoringSupport;
import org.junit.Before;
import org.junit.Test;

import static com.aperto.magkit.utils.ExtendedLinkFieldHelper.SUFFIX_ANCHOR;
import static com.aperto.magkit.utils.ExtendedLinkFieldHelper.SUFFIX_QUERY;
import static com.aperto.magkit.utils.ExtendedLinkFieldHelper.SUFFIX_SELECTOR;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Philipp Güttler (Aperto AG)
 * @since 03.06.2015
 */
public class ExtendedLinkTransformerTest {

    private static final String PN_TEST = "testProp";
    private static final String UUID = java.util.UUID.randomUUID().toString();
    private static final String ANCHOR = "anchor";
    private static final String QUERY = "param=value";
    private static final String SELECTOR = "foo=bar";
    private static final String FULL_PATH = UUID + "~" + SELECTOR + "~" + "?" + QUERY + "#" + ANCHOR;

    private ConfiguredFieldDefinition _fieldDefinition;
    private Property _propUuid;
    private Item _item;
    private Property _propSelector;
    private Property _propQuery;
    private Property _propAnchor;

    @Before
    public void setUp() throws Exception {
        _fieldDefinition = new ConfiguredFieldDefinition();
        _fieldDefinition.setName(PN_TEST);

        _item = mock(Item.class);

        _propUuid = mock(Property.class);
        when(_propUuid.getValue()).thenReturn(UUID);
        when(_propUuid.getType()).thenReturn(String.class);
        _propSelector = mock(Property.class);
        when(_propSelector.getValue()).thenReturn(SELECTOR);
        when(_propSelector.getType()).thenReturn(String.class);
        _propQuery = mock(Property.class);
        when(_propQuery.getValue()).thenReturn(QUERY);
        when(_propQuery.getType()).thenReturn(String.class);
        _propAnchor = mock(Property.class);
        when(_propAnchor.getValue()).thenReturn(ANCHOR);
        when(_propAnchor.getType()).thenReturn(String.class);
    }

    @Test
    public void testWriteToItem() throws Exception {
        PropertysetItem item = new PropertysetItem();
        ExtendedLinkTransformer transformer = new ExtendedLinkTransformer(item, _fieldDefinition, String.class, new DefaultI18NAuthoringSupport());
        transformer.setExtendedLinkFieldHelper(new ExtendedLinkFieldHelper());

        transformer.writeToItem(FULL_PATH);

        assertNotNull(item.getItemPropertyIds());
        assertThat(item.getItemPropertyIds().size(), equalTo(4));
        assertNotNull(item.getItemProperty(PN_TEST));
        assertNotNull((String) item.getItemProperty(PN_TEST).getValue(), equalTo(UUID));
        assertNotNull(item.getItemProperty(PN_TEST + SUFFIX_ANCHOR));
        assertNotNull((String) item.getItemProperty(PN_TEST + SUFFIX_ANCHOR).getValue(), equalTo(ANCHOR));
        assertNotNull(item.getItemProperty(PN_TEST + SUFFIX_SELECTOR));
        assertNotNull((String) item.getItemProperty(PN_TEST + SUFFIX_SELECTOR).getValue(), equalTo(SELECTOR));
        assertNotNull(item.getItemProperty(PN_TEST + SUFFIX_QUERY));
        assertNotNull((String) item.getItemProperty(PN_TEST + SUFFIX_QUERY).getValue(), equalTo(QUERY));
    }

    @Test
    public void testReadFromItemSimple() throws Exception {
        when(_item.getItemProperty(PN_TEST)).thenReturn(_propUuid);
        ExtendedLinkTransformer transformer = new ExtendedLinkTransformer(_item, _fieldDefinition, String.class, new DefaultI18NAuthoringSupport());
        transformer.setExtendedLinkFieldHelper(new ExtendedLinkFieldHelper());
        assertThat(transformer.readFromItem(), equalTo(UUID));

        when(_propUuid.getValue()).thenReturn(null);
        assertThat(transformer.readFromItem(), nullValue());
    }
    @Test
    public void testReadFromItemExtended() throws Exception {
        when(_item.getItemProperty(PN_TEST)).thenReturn(_propUuid);
        when(_item.getItemProperty(PN_TEST + SUFFIX_ANCHOR)).thenReturn(_propAnchor);
        when(_item.getItemProperty(PN_TEST + SUFFIX_SELECTOR)).thenReturn(_propSelector);
        when(_item.getItemProperty(PN_TEST + SUFFIX_QUERY)).thenReturn(_propQuery);
        ExtendedLinkTransformer transformer = new ExtendedLinkTransformer(_item, _fieldDefinition, String.class, new DefaultI18NAuthoringSupport());
        transformer.setExtendedLinkFieldHelper(new ExtendedLinkFieldHelper());
        assertThat(transformer.readFromItem(), equalTo(FULL_PATH));
    }
}
