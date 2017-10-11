package com.aperto.magkit.dialogs.fields;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.PropertysetItem;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.api.i18n.I18NAuthoringSupport;
import info.magnolia.ui.form.field.MultiField;
import info.magnolia.ui.form.field.definition.ConfiguredFieldDefinition;
import info.magnolia.ui.form.field.definition.MultiValueFieldDefinition;
import info.magnolia.ui.form.field.factory.FieldFactoryFactory;
import info.magnolia.ui.form.field.transformer.TransformedProperty;
import info.magnolia.ui.form.field.transformer.Transformer;
import info.magnolia.ui.form.field.transformer.multi.MultiTransformer;
import info.magnolia.ui.vaadin.integration.jcr.JcrNewNodeAdapter;
import info.magnolia.ui.vaadin.integration.jcr.JcrNodeAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import static com.aperto.magkit.dialogs.fields.ExtendedTextField.FULL_WIDTH;
import static info.magnolia.jcr.util.PropertyUtil.getLong;
import static org.apache.commons.lang.StringUtils.isNotBlank;

/**
 * Generic Multi Field.<br>
 * This generic SpecificMultiField allows to handle a Field Set.
 * The Field is build based on a generic {@link ConfiguredFieldDefinition}.<br>
 * The Field values are handle by a configured {@link info.magnolia.ui.form.field.transformer.Transformer} dedicated to create/retrieve properties as {@link PropertysetItem}.<br>
 *
 * @author diana.racho (Aperto AG)
 */
public class SpecificMultiField extends MultiField {
    private static final Logger LOGGER = LoggerFactory.getLogger(SpecificMultiField.class);
    private static final long DEFAULT_COUNT = 3;

    private final ConfiguredFieldDefinition _fieldDefinition;

    private Long _multiValueCount = -1L;

    public SpecificMultiField(MultiValueFieldDefinition definition, FieldFactoryFactory fieldFactoryFactory, ComponentProvider componentProvider, Item relatedFieldItem, I18NAuthoringSupport i18nAuthoringSupport) {
        super(definition, fieldFactoryFactory, componentProvider, relatedFieldItem, i18nAuthoringSupport);
        _fieldDefinition = definition.getField();
        initMultiValueCount();
    }

    private void initMultiValueCount() {
        String parentCountFieldName = ((SpecificMultiFieldDefinition) definition).getParentCountFieldName();
        if (isNotBlank(parentCountFieldName)) {
            Node parent = null;
            try {
                if (relatedFieldItem instanceof JcrNewNodeAdapter) {
                    parent = ((JcrNodeAdapter) relatedFieldItem).getJcrItem().getParent();
                } else {
                    parent = ((JcrNodeAdapter) relatedFieldItem).getJcrItem().getParent().getParent();
                }
            } catch (RepositoryException e) {
                LOGGER.error("Can't get property '" + parentCountFieldName + "' from parent node.", e);
            }
            if (parent != null) {
                _multiValueCount = getLong(parent, parentCountFieldName);
            }
        } else {
            Long count = ((SpecificMultiFieldDefinition) definition).getCount();
            if (count != null) {
                _multiValueCount = count;
            }
        }
        if (_multiValueCount < 1) {
            _multiValueCount = DEFAULT_COUNT;
        }
    }

    @Override
    protected Component initContent() {
        // Init root layout
        addStyleName("linkfield");
        root = new VerticalLayout();
        root.setSpacing(true);
        root.setWidth(FULL_WIDTH, Unit.PERCENTAGE);
        root.setHeight(-1, Unit.PIXELS);

        // Initialize Existing field
        initFields();

        if (root.getComponentCount() < getMultiValueCount()) {
            for (int index = root.getComponentCount(); index < getMultiValueCount(); index++) {
                int newPropertyId;
                Property<?> property = null;
                Transformer<?> transformer = ((TransformedProperty<?>) getPropertyDataSource()).getTransformer();
                PropertysetItem item = (PropertysetItem) getPropertyDataSource().getValue();
                if (transformer instanceof MultiTransformer) {
                    // create property and find its propertyId
                    property = ((MultiTransformer) transformer).createProperty();
                    newPropertyId = findPropertyId(item, property);
                } else {
                    // get next propertyId based on property count
                    newPropertyId = item.getItemPropertyIds().size();
                }
                if (newPropertyId == -1) {
                    LOGGER.warn("Could not resolve new propertyId; cannot add new multifield entry to item '{}'.", item);
                    break;
                }
                root.addComponent(createEntryComponent(newPropertyId, property), root.getComponentCount());
            }
        }

        return root;
    }

    /**
     * Initialize the SpecificMultiField. <br>
     * Create as many configured Field as we have related values already stored.
     */
    @Override
    protected void initFields(PropertysetItem newValue) {
        root.removeAllComponents();
        for (Object propertyId : newValue.getItemPropertyIds()) {
            Property<?> property = newValue.getItemProperty(propertyId);
            root.addComponent(createEntryComponent(propertyId, property));
        }
    }

    /**
     * Create a single element.<br>
     * This single element is composed of:<br>
     * - a configured field <br>
     * - a remove Button<br>
     */
    private Component createEntryComponent(Object propertyId, Property<?> property) {
        HorizontalLayout layout = new HorizontalLayout();
        layout.setWidth(FULL_WIDTH, Unit.PERCENTAGE);
        layout.setHeight(-1, Unit.PIXELS);

        // creates property datasource if given property is null
        Field<?> field = createLocalField(_fieldDefinition, property, true);
        layout.addComponent(field);

        // bind the field's property to the item
        Property<?> newProperty = property;
        if (newProperty == null) {
            newProperty = field.getPropertyDataSource();
            ((PropertysetItem) getPropertyDataSource().getValue()).addItemProperty(propertyId, newProperty);
        }

        // set layout to full width
        layout.setWidth(FULL_WIDTH, Unit.PERCENTAGE);

        // distribute space in favour of field over delete button
        layout.setExpandRatio(field, 1);
        return layout;
    }

    @Override
    public Class<? extends PropertysetItem> getType() {
        return PropertysetItem.class;
    }

    private Long getMultiValueCount() {
        return _multiValueCount;
    }
}