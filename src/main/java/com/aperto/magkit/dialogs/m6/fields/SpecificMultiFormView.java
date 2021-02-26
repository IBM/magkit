package com.aperto.magkit.dialogs.m6.fields;

import com.vaadin.server.ClientConnector;
import com.vaadin.ui.Component;
import com.vaadin.ui.HasComponents;
import com.vaadin.ui.Notification;
import com.vaadin.ui.VerticalLayout;
import info.magnolia.i18nsystem.SimpleTranslator;
import info.magnolia.ui.contentapp.Datasource;
import info.magnolia.ui.editor.LocaleContext;
import info.magnolia.ui.editor.MultiFormDefinition;
import info.magnolia.ui.editor.MultiFormView;

import javax.inject.Inject;

/**
 * Editor view which hosts a number of similar child editors.
 *
 * @param <T> item type.
 * @author payam.tabrizi
 * @since 23.02.21
 */
public class SpecificMultiFormView<T> extends MultiFormView<T> implements ClientConnector.AttachListener, ClientConnector.DetachListener, HasComponents.ComponentAttachListener, HasComponents.ComponentDetachListener {

    private static final long DEFAULT_MAX = 3;

    private final MultiFormDefinition<T> _definition;
    private final SimpleTranslator _i18n;

    private VerticalLayout _rootLayout;
    private Long _maxComponents;

    @Inject
    public SpecificMultiFormView(MultiFormDefinition<T> definition, SimpleTranslator i18n, LocaleContext localeContext, Datasource<T> datasource) {
        super(definition, i18n, localeContext, datasource);
        _definition = definition;
        _i18n = i18n;
    }

    @Override
    public void layout() {
        super.layout();
        if (_definition != null) {
            initMaxComponents();
        }
    }

    private void initMaxComponents() {
        _rootLayout = (VerticalLayout) asVaadinComponent();
        int componentCount = _rootLayout.getComponentCount();
        if (componentCount > 1) {
            _maxComponents = ((SpecificMultiFieldDefinition) _definition).getMaxComponents();
            if (_maxComponents == null) {
                _maxComponents = DEFAULT_MAX;
            }
            _rootLayout.addAttachListener(this);
            _rootLayout.addDetachListener(this);
            _rootLayout.addComponentAttachListener(this);
            _rootLayout.addComponentDetachListener(this);
        }

    }

    @Override
    public void attach(final ClientConnector.AttachEvent event) {
        addButtonDisabled();
    }

    @Override
    public void detach(final ClientConnector.DetachEvent event) {
        addButtonEnabled();
    }

    @Override
    public void componentAttachedToContainer(final HasComponents.ComponentAttachEvent event) {
        addButtonDisabled();
    }

    @Override
    public void componentDetachedFromContainer(final HasComponents.ComponentDetachEvent event) {
        addButtonEnabled();
    }

    private void addButtonEnabled() {
        Component component = _rootLayout.getComponent(_rootLayout.getComponentCount() - 1);
        if (_rootLayout.getComponentCount() <= _maxComponents) {
            component.setEnabled(true);
        }
    }

    private void addButtonDisabled() {
        Component component = _rootLayout.getComponent(_rootLayout.getComponentCount() - 1);
        if (_rootLayout.getComponentCount() > _maxComponents) {
            component.setEnabled(false);
            Notification.show(_i18n.translate("specificMultiField.maxNumber.reached", _maxComponents));
        }
    }

}
