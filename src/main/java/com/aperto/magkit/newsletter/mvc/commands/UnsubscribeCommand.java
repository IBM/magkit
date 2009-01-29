package com.aperto.magkit.newsletter.mvc.commands;

import com.aperto.magkit.newsletter.mvc.controller.NewsletterCommandController;

/**
 * The form backing object for newsletter unsubscriptions. Implements ExecutorVisitor.
 * @author wolf.bubenik
 * Date: 29.01.2009
 */
public class UnsubscribeCommand implements NewsletterCommand {

    private String _email;

    public void accept(NewsletterCommandController controller) throws Exception {
        controller.execute(this);
    }

    public String getEmail() {
        return _email;
    }

    public void setEmail(String email) {
        _email = email;
    }
}
