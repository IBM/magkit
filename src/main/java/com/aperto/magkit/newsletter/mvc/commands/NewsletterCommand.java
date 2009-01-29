package com.aperto.magkit.newsletter.mvc.commands;

import com.aperto.magkit.newsletter.mvc.controller.NewsletterCommandController;

/**
 * A Interface for command objects that shoul provide a callback method for executing controllers.
 * @author wolf.bubenik
 * Date: 29.01.2009
 */
public interface NewsletterCommand {
    void accept(NewsletterCommandController controller) throws Exception;
}
