package com.aperto.magkit.dialogs.fields;

import com.vaadin.annotations.StyleSheet;
import com.vaadin.ui.Button;
import com.vaadin.ui.Component;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Layout;
import com.vaadin.ui.NativeButton;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.v7.data.Item;
import com.vaadin.v7.data.Property;
import com.vaadin.v7.data.util.PropertysetItem;
import com.vaadin.v7.ui.Field;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.api.i18n.I18NAuthoringSupport;
import info.magnolia.ui.form.field.AbstractCustomMultiField;
import info.magnolia.ui.form.field.definition.ConfiguredFieldDefinition;
import info.magnolia.ui.form.field.factory.FieldFactoryFactory;
import info.magnolia.ui.form.field.transformer.TransformedProperty;
import info.magnolia.ui.form.field.transformer.Transformer;
import info.magnolia.ui.form.field.transformer.multi.MultiTransformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.aperto.magkit.dialogs.fields.ExtendedTextField.FULL_WIDTH;

/**
 * Sortable MultiValueField based on {@link info.magnolia.ui.form.field.MultiField}.
 * This field contains two new buttons (up and down) to sort the fields.
 * maxComponents to set a number of maximum components.
 * with sortable you can switch between the normal MultiValueField and the sortable one.
 *
 * @author Stefan Jahn
 * @since 02.12.14
 * @deprecated use new ui 6 field
 */
@Deprecated(since = "3.5.2")
@StyleSheet("sortableMultiValueField.css")
public class SortableMultiValueField extends AbstractCustomMultiField<SortableMultiValueFieldDefinition, PropertysetItem> {
    private static final long serialVersionUID = 5843108445147449041L;

    private static final Logger LOGGER = LoggerFactory.getLogger(SortableMultiValueField.class);

    private static final int PROPERTY_ID_NOT_FOUND = -1;

    private final ConfiguredFieldDefinition _fieldDefinition;
    private final Button _addButton = new NativeButton();

    private String _buttonCaptionAdd;
    private String _buttonCaptionRemove;

    public SortableMultiValueField(final SortableMultiValueFieldDefinition definition, final FieldFactoryFactory fieldFactoryFactory, final ComponentProvider componentProvider,
                                   final Item relatedFieldItem, final I18NAuthoringSupport i18nAuthoringSupport) {
        super(definition, fieldFactoryFactory, componentProvider, relatedFieldItem, i18nAuthoringSupport);
        _fieldDefinition = definition.getField();
    }

    @Override
    protected Component initContent() {
        // Init root layout
        addStyleName("linkfield");
        root = new VerticalLayout();
        root.setSpacing(true);
        root.setWidth(FULL_WIDTH, Unit.PERCENTAGE);
        root.setHeight(-1, Unit.PIXELS);

        addRootListener();

        // Init _addButton
        _addButton.setCaption(_buttonCaptionAdd);
        _addButton.addStyleName("magnoliabutton");
        _addButton.addClickListener(new Button.ClickListener() {
            @Override
            public void buttonClick(final Button.ClickEvent event) {
                int newPropertyId;
                Property<?> property = null;
                Transformer<?> transformer = ((TransformedProperty<?>) getPropertyDataSource()).getTransformer();
                PropertysetItem item = (PropertysetItem) getPropertyDataSource().getValue();

                if (transformer instanceof MultiTransformer) {
                    property = ((MultiTransformer) transformer).createProperty();
                    newPropertyId = findPropertyId(item, property);
                } else {
                    newPropertyId = item.getItemPropertyIds().size();
                }

                if (newPropertyId == PROPERTY_ID_NOT_FOUND) {
                    LOGGER.warn("Could not resolve new propertyId; cannot add new multifield entry to item '{}'.", item);
                } else {
                    root.addComponent(createEntryComponent(newPropertyId, property), root.getComponentCount() - 1);
                }
            }
        });

        initFields();
        return root;
    }

    protected void addRootListener() {
        root.addComponentAttachListener(new ComponentAttachListener() {
            @Override
            public void componentAttachedToContainer(final ComponentAttachEvent event) {
                if (root.getComponentCount() > definition.getMaxComponents()) {
                    _addButton.setEnabled(false);
                }
            }
        });

        root.addComponentDetachListener(new ComponentDetachListener() {
            @Override
            public void componentDetachedFromContainer(final ComponentDetachEvent event) {
                if (root.getComponentCount() <= definition.getMaxComponents()) {
                    _addButton.setEnabled(true);
                }
            }
        });
    }

    /**
     * Copied from Magnolia.
     * <p/>
     * Initialize the MultiField. <br> Create as many configured Field as we have related values already stored.
     */
    @Override
    protected void initFields(final PropertysetItem newValue) {
        root.removeAllComponents();
        for (Object propertyId : newValue.getItemPropertyIds()) {
            Property<?> property = newValue.getItemProperty(propertyId);
            root.addComponent(createEntryComponent(propertyId, property));
        }
        root.addComponent(_addButton);
    }

    /**
     * Copied from Magnolia, except initButtons()
     * <p/>
     * Create a single element.<br> This single element is composed of:<br> - a configured field <br> - a remove Button<br>
     */
    protected Component createEntryComponent(final Object propertyId, final Property<?> property) {
        Property propertyToBind = property;

        HorizontalLayout layout = new HorizontalLayout();
        layout.addStyleName("aperto-multifield");
        layout.setWidth(FULL_WIDTH, Unit.PERCENTAGE);
        layout.setHeight(-1, Unit.PIXELS);

        // creates property datasource if given property is null
        Field<?> field = createLocalField(_fieldDefinition, propertyToBind, true);
        layout.addComponent(field);

        // bind the field's property to the item
        if (propertyToBind == null) {
            propertyToBind = field.getPropertyDataSource();
            ((PropertysetItem) getPropertyDataSource().getValue()).addItemProperty(propertyId, propertyToBind);
        }
        initButtons(layout, propertyToBind);

        // set layout to full width
        layout.setWidth(FULL_WIDTH, Unit.PERCENTAGE);
        return layout;
    }

    protected void initButtons(final Layout layout, final Property property) {
        boolean isSortable = definition.getSortable();
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setPrimaryStyleName("aperto-multi-buttons");
        // Delete Button - inspired by Magnolia
        Button deleteButton = new Button();
        deleteButton.setHtmlContentAllowed(true);
        deleteButton.setCaption("<span class=\"" + "icon-trash" + "\"></span>");
        deleteButton.addStyleName("inline");
        deleteButton.setDescription(_buttonCaptionRemove);
        deleteButton.addClickListener(new Button.ClickListener() {

            @Override
            public void buttonClick(final Button.ClickEvent event) {
                Component layout = event.getComponent().getParent().getParent();
                root.removeComponent(layout);
                Transformer<?> transformer = ((TransformedProperty<?>) getPropertyDataSource()).getTransformer();
                Object propertyId = findPropertyId(getValue(), property);

                if (transformer instanceof MultiTransformer) {
                    ((MultiTransformer) transformer).removeProperty(propertyId);
                    ((MultiTransformer) transformer).createProperty();
                } else {
                    if (propertyId.getClass().isAssignableFrom(Integer.class)) {
                        removeValueProperty((Integer) propertyId);
                    }
                    getPropertyDataSource().setValue(getValue());
                }
            }
        });

        if (isSortable) {
            initializeSortButtons(buttonLayout, deleteButton);
        } else {
            buttonLayout.addComponent(deleteButton);
        }

        layout.addComponent(buttonLayout);
    }

    /**
     * Initializes the up and down button.
     *
     * @param buttonLayout the layout where the buttons will be added
     * @param deleteButton the delete button to be added
     */
    protected void initializeSortButtons(final HorizontalLayout buttonLayout, final Button deleteButton) {
        // move up Button
        Button moveUpButton = new Button();
        moveUpButton.setHtmlContentAllowed(true);
        moveUpButton.setCaption("<span class=\"icon-arrow2_n\"></span>");
        moveUpButton.addStyleName("inline");
        moveUpButton.setDescription("move up");
        moveUpButton.addClickListener(new Button.ClickListener() {

            @Override
            public void buttonClick(final Button.ClickEvent event) {
                VerticalLayout parentLayout = (VerticalLayout) event.getComponent().getParent().getParent().getParent();
                int currPos = parentLayout.getComponentIndex(event.getComponent().getParent().getParent());

                if (currPos != 0) {
                    parentLayout.replaceComponent(parentLayout.getComponent(currPos - 1), parentLayout.getComponent(currPos));
                    switchItemProperties(currPos - 1, currPos);
                }

            }
        });

        // move down Button
        Button moveDownButton = new Button();
        moveDownButton.setHtmlContentAllowed(true);
        moveDownButton.setCaption("<span class=\"icon-arrow2_s\"></span>");
        moveDownButton.addStyleName("inline");
        moveDownButton.setDescription("move down");
        moveDownButton.addClickListener(new Button.ClickListener() {

            @Override
            public void buttonClick(final Button.ClickEvent event) {
                VerticalLayout parentLayout = (VerticalLayout) event.getComponent().getParent().getParent().getParent();
                int currPos = parentLayout.getComponentIndex(event.getComponent().getParent().getParent());
                int numberOfComponents = parentLayout.getComponentCount() - 1;

                if (currPos < numberOfComponents - 1) {
                    parentLayout.replaceComponent(parentLayout.getComponent(currPos + 1), parentLayout.getComponent(currPos));
                    switchItemProperties(currPos + 1, currPos);
                }
            }
        });

        buttonLayout.addComponents(moveUpButton, moveDownButton, deleteButton);
    }

    @Override
    public Class<? extends PropertysetItem> getType() {
        return PropertysetItem.class;
    }

    /**
     * Caption section.
     */
    public void setButtonCaptionAdd(final String buttonCaptionAdd) {
        _buttonCaptionAdd = buttonCaptionAdd;
    }

    public void setButtonCaptionRemove(final String buttonCaptionRemove) {
        _buttonCaptionRemove = buttonCaptionRemove;
    }

    /**
     * Copied from Magnolia.
     * <p/>
     * Ensure that id of the {@link PropertysetItem} stay coherent.<br> Assume that we have 3 values 0:a, 1:b, 2:c, and 1 is removed <br> If we just remove 1, the {@link PropertysetItem} will contain 0:a, 2:c,
     * .<br> But we should have : 0:a, 1:c, .
     */
    protected void removeValueProperty(final int toDelete) {
        getValue().removeItemProperty(toDelete);
        int fromIndex = toDelete;
        int valuesSize = getValue().getItemPropertyIds().size();
        if (fromIndex == valuesSize) {
            return;
        }
        while (fromIndex < valuesSize) {
            int toIndex = fromIndex;
            fromIndex += 1;
            getValue().addItemProperty(toIndex, getValue().getItemProperty(fromIndex));
            getValue().removeItemProperty(fromIndex);
        }
    }

    // switches the values of two properties.
    protected void switchItemProperties(final int first, final int second) {
        Property propertyFirst = getValue().getItemProperty(first);
        Property propertySecond = getValue().getItemProperty(second);

        PropertysetItem storedValues = null;
        try {
            storedValues = (PropertysetItem) getValue().clone();
        } catch (CloneNotSupportedException e) {
            LOGGER.debug("Unable to clone PropertysetItem.", e);
        }

        if (storedValues != null) {
            for (int i = 0; i < storedValues.getItemPropertyIds().size(); i++) {
                getValue().removeItemProperty(i);
                if (i == first) {
                    getValue().addItemProperty(first, propertySecond);
                } else if (i == second) {
                    getValue().addItemProperty(second, propertyFirst);
                } else {
                    getValue().addItemProperty(i, storedValues.getItemProperty(i));
                }
            }
            getPropertyDataSource().setValue(getValue());
        }
    }
}
