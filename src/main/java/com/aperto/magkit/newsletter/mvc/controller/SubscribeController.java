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
 * For propper form handling usind spring for each command a seperate controller spring bean must be declared
 * where individual values for the command class, name and viewnames are configured.
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
            // May be a bit overdone for two commands: Type resolution with a visitor.
            ((NewsletterCommand) command).accept(this);
            view = getSuccessView();
        } catch (RemoteException e) {
            errors.reject("newsletter.system.error", "newsletter.system.error");
            view = getFormView();
        } catch (IllegalArgumentException e) {
            errors.reject("newsletter.email.invalid", "newsletter.email.invalid");
            view = getFormView();
        } catch (IllegalStateException e) {
            errors.reject("newsletter.system.error", "newsletter.system.error");
            view = getFormView();
            LOGGER.error("Api key or list id invalid while executing subscriber action:", e);
        } catch (Throwable e) {
            errors.reject("newsletter.system.error", "newsletter.system.error");
            view = getFormView();
            LOGGER.error("Unexpected Error while executing subscriber action:", e);
        }
        return new ModelAndView(view, errors.getModel());
    }

    public void execute(SubscribeCommand command) throws RemoteException, IllegalArgumentException, IllegalStateException {
        _subscriberService.subscribe(command.getEmail(), command.getDisplayName());
    }

    public void execute(UnsubscribeCommand command) throws RemoteException, IllegalArgumentException, IllegalStateException {
        _subscriberService.unsubscribe(command.getEmail());
    }

    public void setSubscriberService(Subscriber subscriberService) {
        _subscriberService = subscriberService;
    }
}
