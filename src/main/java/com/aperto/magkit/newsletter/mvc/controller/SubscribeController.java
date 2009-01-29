package com.aperto.magkit.newsletter.mvc.controller;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;
import org.springframework.validation.BindException;
import org.apache.log4j.Logger;
import com.aperto.freshview.campaignmonitor.Subscriber;
import com.aperto.magkit.newsletter.mvc.commands.NewsletterCommand;
import com.aperto.magkit.newsletter.mvc.commands.SubscribeCommand;
import com.aperto.magkit.newsletter.mvc.commands.UnsubscribeCommand;

import java.rmi.RemoteException;

/**
 * This controller is designed to be called as template resource of a magnolia paragraph.
 * It handles subscribe and unsubscribe commands and dispatches to the approriate form or success views.
 * @author wolf.bubenik
 * Date: 29.01.2009
 */
public class SubscribeController extends SimpleFormController implements NewsletterCommandController {

    private static final Logger LOGGER = Logger.getLogger(SubscribeController.class);

    private Subscriber _subscriberService;

    @Override
    protected ModelAndView onSubmit(Object command, BindException errors){
        String view;
        try {
            // May be a bit overdone: Type resolution with a visitor.
            ((NewsletterCommand) command).accept(this);
            view = getSuccessView();
        } catch (RemoteException e) {
            errors.reject("form.system.error", "form.system.error");
            view = getFormView();
        } catch (Exception e) {
            errors.reject("form.system.error", "form.system.error");
            view = getFormView();
            LOGGER.error("Unexpected Error while executing subscriber action:", e);
        }
        return new ModelAndView(view, errors.getModel());
    }

    public void execute(SubscribeCommand command) throws RemoteException {
        _subscriberService.subscribe(command.getEmail(), "");
    }

    public void execute(UnsubscribeCommand command) throws RemoteException {
        _subscriberService.unsubscribe(command.getEmail());
    }

    public void setSubscriberService(Subscriber subscriberService) {
        _subscriberService = subscriberService;
    }
}
