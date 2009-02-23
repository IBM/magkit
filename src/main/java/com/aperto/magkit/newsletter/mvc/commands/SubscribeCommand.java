package com.aperto.magkit.newsletter.mvc.commands;

import com.aperto.magkit.newsletter.mvc.controller.NewsletterCommandController;

/**
 * The form backing object for newsletter subscriptions. Implements NewsletterCommand.
 * @author wolf.bubenik
 * Date: 29.01.2009
 */
public class SubscribeCommand implements NewsletterCommand {
    private String _email;
    private String _displayName;

    public void accept(NewsletterCommandController controller) throws Exception {
        controller.execute(this);
    }

    public String getEmail() {
        return _email;
    }

    public void setEmail(String email) {
        _email = email;
    }

    public String getDisplayName() {
        return _displayName;
    }

    public void setDisplayName(String displayName) {
        _displayName = displayName;
    }
}
