package com.aperto.magkit.newsletter.mvc.controller;

import com.aperto.magkit.newsletter.mvc.commands.SubscribeCommand;
import com.aperto.magkit.newsletter.mvc.commands.UnsubscribeCommand;

/**
 * An Interface for controllers that should provide visitor methods for command objects.
 * @author wolf.bubenik
 * Date: 29.01.2009
 */
public interface NewsletterCommandController {

    void execute(SubscribeCommand command) throws Exception;

    void execute(UnsubscribeCommand command) throws Exception;
}
