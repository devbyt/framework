/*
@VaadinApache2LicenseForJavaFiles@
 */
package com.vaadin.terminal.gwt.client.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.UIDL;
import com.vaadin.terminal.gwt.client.VPaintableMap;
import com.vaadin.terminal.gwt.client.VPaintableWidget;

public class VFormPaintable extends VAbstractPaintableWidgetContainer implements
        SimpleManagedLayout {

    @Override
    public void init() {
        VForm form = getWidgetForPaintable();
        getLayoutManager().registerDependency(this, form.footerContainer);
    }

    @Override
    protected boolean delegateCaptionHandling() {
        return false;
    }

    @Override
    public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
        getWidgetForPaintable().client = client;
        getWidgetForPaintable().id = uidl.getId();

        super.updateFromUIDL(uidl, client);
        if (!isRealUpdate(uidl)) {
            return;
        }

        boolean legendEmpty = true;
        if (uidl.hasAttribute("caption")) {
            getWidgetForPaintable().caption.setInnerText(uidl
                    .getStringAttribute("caption"));
            legendEmpty = false;
        } else {
            getWidgetForPaintable().caption.setInnerText("");
        }
        if (uidl.hasAttribute("icon")) {
            if (getWidgetForPaintable().icon == null) {
                getWidgetForPaintable().icon = new Icon(client);
                getWidgetForPaintable().legend
                        .insertFirst(getWidgetForPaintable().icon.getElement());
            }
            getWidgetForPaintable().icon
                    .setUri(uidl.getStringAttribute("icon"));
            legendEmpty = false;
        } else {
            if (getWidgetForPaintable().icon != null) {
                getWidgetForPaintable().legend
                        .removeChild(getWidgetForPaintable().icon.getElement());
            }
        }
        if (legendEmpty) {
            getWidgetForPaintable().addStyleDependentName("nocaption");
        } else {
            getWidgetForPaintable().removeStyleDependentName("nocaption");
        }

        if (uidl.hasAttribute("error")) {
            final UIDL errorUidl = uidl.getErrors();
            getWidgetForPaintable().errorMessage.updateFromUIDL(errorUidl);
            getWidgetForPaintable().errorMessage.setVisible(true);
        } else {
            getWidgetForPaintable().errorMessage.setVisible(false);
        }

        if (uidl.hasAttribute("description")) {
            getWidgetForPaintable().desc.setInnerHTML(uidl
                    .getStringAttribute("description"));
            if (getWidgetForPaintable().desc.getParentElement() == null) {
                getWidgetForPaintable().fieldSet.insertAfter(
                        getWidgetForPaintable().desc,
                        getWidgetForPaintable().legend);
            }
        } else {
            getWidgetForPaintable().desc.setInnerHTML("");
            if (getWidgetForPaintable().desc.getParentElement() != null) {
                getWidgetForPaintable().fieldSet
                        .removeChild(getWidgetForPaintable().desc);
            }
        }

        // first render footer so it will be easier to handle relative height of
        // main layout
        if (uidl.getChildCount() > 1
                && !uidl.getChildUIDL(1).getTag().equals("actions")) {
            // render footer
            VPaintableWidget newFooter = client.getPaintable(uidl
                    .getChildUIDL(1));
            Widget newFooterWidget = newFooter.getWidgetForPaintable();
            if (getWidgetForPaintable().footer == null) {
                getWidgetForPaintable().add(newFooter.getWidgetForPaintable(),
                        getWidgetForPaintable().footerContainer);
                getWidgetForPaintable().footer = newFooterWidget;
            } else if (newFooter != getWidgetForPaintable().footer) {
                getWidgetForPaintable().remove(getWidgetForPaintable().footer);
                client.unregisterPaintable(VPaintableMap.get(getConnection())
                        .getPaintable(getWidgetForPaintable().footer));
                getWidgetForPaintable().add(newFooter.getWidgetForPaintable(),
                        getWidgetForPaintable().footerContainer);
            }
            getWidgetForPaintable().footer = newFooterWidget;
            newFooter.updateFromUIDL(uidl.getChildUIDL(1), client);
        } else {
            if (getWidgetForPaintable().footer != null) {
                getWidgetForPaintable().remove(getWidgetForPaintable().footer);
                client.unregisterPaintable(VPaintableMap.get(getConnection())
                        .getPaintable(getWidgetForPaintable().footer));
            }
        }

        final UIDL layoutUidl = uidl.getChildUIDL(0);
        VPaintableWidget newLayout = client.getPaintable(layoutUidl);
        Widget newLayoutWidget = newLayout.getWidgetForPaintable();
        if (getWidgetForPaintable().lo == null) {
            // Layout not rendered before
            getWidgetForPaintable().lo = newLayoutWidget;
            getWidgetForPaintable().add(newLayoutWidget,
                    getWidgetForPaintable().fieldContainer);
        } else if (getWidgetForPaintable().lo != newLayoutWidget) {
            // Layout has changed
            client.unregisterPaintable(VPaintableMap.get(getConnection())
                    .getPaintable(getWidgetForPaintable().lo));
            getWidgetForPaintable().remove(getWidgetForPaintable().lo);
            getWidgetForPaintable().lo = newLayoutWidget;
            getWidgetForPaintable().add(newLayoutWidget,
                    getWidgetForPaintable().fieldContainer);
        }
        newLayout.updateFromUIDL(layoutUidl, client);

        // also recalculates size of the footer if undefined size form - see
        // #3710
        client.runDescendentsLayout(getWidgetForPaintable());

        // We may have actions attached
        if (uidl.getChildCount() > 1) {
            UIDL childUidl = uidl.getChildByTagName("actions");
            if (childUidl != null) {
                if (getWidgetForPaintable().shortcutHandler == null) {
                    getWidgetForPaintable().shortcutHandler = new ShortcutActionHandler(
                            getId(), client);
                    getWidgetForPaintable().keyDownRegistration = getWidgetForPaintable()
                            .addDomHandler(getWidgetForPaintable(),
                                    KeyDownEvent.getType());
                }
                getWidgetForPaintable().shortcutHandler
                        .updateActionMap(childUidl);
            }
        } else if (getWidgetForPaintable().shortcutHandler != null) {
            getWidgetForPaintable().keyDownRegistration.removeHandler();
            getWidgetForPaintable().shortcutHandler = null;
            getWidgetForPaintable().keyDownRegistration = null;
        }
    }

    public void updateCaption(VPaintableWidget component, UIDL uidl) {
        // NOP form don't render caption for neither field layout nor footer
        // layout
    }

    @Override
    public VForm getWidgetForPaintable() {
        return (VForm) super.getWidgetForPaintable();
    }

    @Override
    protected Widget createWidget() {
        return GWT.create(VForm.class);
    }

    public void layout() {
        VForm form = getWidgetForPaintable();

        int footerHeight = getLayoutManager().getOuterHeight(
                form.footerContainer);

        form.fieldContainer.getStyle().setPaddingBottom(footerHeight, Unit.PX);
        form.footerContainer.getStyle().setMarginTop(-footerHeight, Unit.PX);
    }

}
