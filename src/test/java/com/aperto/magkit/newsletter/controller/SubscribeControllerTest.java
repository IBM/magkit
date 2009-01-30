package com.aperto.magkit.newsletter.controller;

import org.junit.Test;
import static org.mockito.Mockito.*;
import com.aperto.magkit.newsletter.mvc.controller.SubscribeController;
import com.aperto.magkit.newsletter.mvc.commands.SubscribeCommand;
import com.aperto.magkit.newsletter.mvc.commands.UnsubscribeCommand;
import com.aperto.freshview.campaignmonitor.SubscriberService;

/**
 * Well, a collection of testcases for the SubscribeController.
 * @author wolf.bubenik
 * Date: 29.01.2009
 */
public class SubscribeControllerTest {

    @Test
    public void testCommandResolution() throws Exception {
        SubscribeController sc = new SubscribeController();
        SubscriberService subscriber = mock(SubscriberService.class);

        sc.setSubscriberService(subscriber);
        SubscribeCommand subCommand = new SubscribeCommand();
        subCommand.setEmail("test@aperto.de");
        subCommand.accept(sc);
        verify(subscriber, times(1)).subscribe(eq("test@aperto.de"), anyString());

        UnsubscribeCommand unsubCommand = new UnsubscribeCommand();
        unsubCommand.setEmail("test@aperto.de");
        unsubCommand.accept(sc);
        verify(subscriber, times(1)).unsubscribe(eq("test@aperto.de"));
    }
}
