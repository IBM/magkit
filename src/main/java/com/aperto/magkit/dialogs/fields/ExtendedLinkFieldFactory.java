package com.aperto.magkit.dialogs.fields;

import com.aperto.magkit.utils.ExtendedLinkFieldHelper;
import com.vaadin.data.Item;
import com.vaadin.ui.Button;
import com.vaadin.ui.Field;
import info.magnolia.objectfactory.ComponentProvider;
import info.magnolia.ui.api.app.AppController;
import info.magnolia.ui.api.app.ChooseDialogCallback;
import info.magnolia.ui.api.context.UiContext;
import info.magnolia.ui.form.field.LinkField;
import info.magnolia.ui.form.field.definition.FieldDefinition;
import info.magnolia.ui.form.field.definition.LinkFieldDefinition;
import info.magnolia.ui.form.field.factory.LinkFieldFactory;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemId;
import info.magnolia.ui.vaadin.integration.jcr.JcrItemUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.jcr.Node;
import javax.jcr.RepositoryException;

import static com.aperto.magkit.utils.LinkTool.isExternalLink;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.removeStart;

/**
 * The factory creates a {@link LinkField}. The callback mechanism is overwritten to allow additional link components like fragments, queries and selectors.
 *
 * @param <D> FieldDefinition type of field definition
 * @author Philipp Güttler (Aperto AG)
 * @since 03.06.2015
 */
public class ExtendedLinkFieldFactory<D extends FieldDefinition> extends LinkFieldFactory<ExtendedLinkFieldDefinition> {

    public static final Logger LOGGER = LoggerFactory.getLogger(ExtendedLinkFieldFactory.class);

    private final LinkFieldDefinition _definition;
    private final AppController _appController;
    private final UiContext _uiContext;
    private final ComponentProvider _componentProvider;
    private final ExtendedLinkFieldHelper _extendedLinkFieldHelper;
    private LinkField _linkField;

    @Inject
    public ExtendedLinkFieldFactory(LinkFieldDefinition definition, Item relatedFieldItem, AppController appController, UiContext uiContext, ComponentProvider componentProvider) {
        super(definition, relatedFieldItem, appController, uiContext, componentProvider);
        _definition = definition;
        _appController = appController;
        _uiContext = uiContext;
        _componentProvider = componentProvider;
        _extendedLinkFieldHelper = new ExtendedLinkFieldHelper();
    }

    @Override
    protected Field<String> createFieldComponent() {
        _linkField = new LinkField(_definition, _appController, _uiContext, _componentProvider);
        _linkField.setButtonCaptionNew(getMessage(_definition.getButtonSelectNewLabel()));
        _linkField.setButtonCaptionOther(getMessage(_definition.getButtonSelectOtherLabel()));
        _linkField.getSelectButton().addClickListener(createButtonClickListener());
        return _linkField;
    }

    private Button.ClickListener createButtonClickListener() {
        return new ExtendedLinkFieldClickListener();
    }

    @Override
    protected ChooseDialogCallback createChooseDialogCallback() {
        return new ExtendedLinkFieldChooseDialogCallback();
    }

    private class ExtendedLinkFieldChooseDialogCallback implements ChooseDialogCallback {
        /**
         * Returns the components for internal uri.
         *
         * @param value old field value
         * @return the uri components other than path
         */
        private String unstripUriExtension(final String value) {
            String unStripped = EMPTY;
            if (!isExternalLink(value) && isNotBlank(value)) {
                unStripped = removeStart(value, _extendedLinkFieldHelper.getBase(value));
            }
            return unStripped;
        }

        @Override
        public void onCancel() {
            // keine Funktionalität
        }

        @Override
        public void onItemChosen(String actionName, final Object chosenValue) {
            String propertyName = _definition.getTargetPropertyToPopulate();
            String newValue = null;
            if (chosenValue instanceof JcrItemId) {
                try {
                    javax.jcr.Item jcrItem = JcrItemUtil.getJcrItem((JcrItemId) chosenValue);
                    if (jcrItem.isNode()) {
                        final Node selected = (Node) jcrItem;
                        boolean isPropertyExisting = isNotBlank(propertyName) && selected.hasProperty(propertyName);
                        newValue = (isPropertyExisting ? selected.getProperty(propertyName).getString() : selected.getPath()) + unstripUriExtension(_linkField.getTextField().getValue());
                    }
                } catch (RepositoryException e) {
                    LOGGER.error("Not able to access the configured property. Value will not be set.", e);
                }
            }
            _linkField.setValue(newValue);
        }
    }

    private class ExtendedLinkFieldClickListener implements Button.ClickListener {
        @Override
        public void buttonClick(Button.ClickEvent event) {
            ChooseDialogCallback callback = createChooseDialogCallback();
            String value = _linkField.getTextField().getValue();
            if (isNotBlank(_definition.getTargetTreeRootPath())) {
                _appController.openChooseDialog(_definition.getAppName(), _uiContext, _definition.getTargetTreeRootPath(), stripUriExtension(value), callback);
            } else {
                _appController.openChooseDialog(_definition.getAppName(), _uiContext, stripUriExtension(value), callback);
            }
        }

        /**
         * Removes components other than path from an internal uri.
         *
         * @param value the uri or identifier with additional components
         * @return the base path or identifier
         */
        private String stripUriExtension(final String value) {
            String stripped = EMPTY;
            if (!isExternalLink(value) && isNotBlank(value)) {
                stripped = _extendedLinkFieldHelper.getBase(value);
            }
            return stripped;
        }
    }
}
