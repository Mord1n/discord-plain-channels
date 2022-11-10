package discord;

import discord.utils.Logger;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;

public class Annotation {
    public final static String PREFIX = "=";
    public static Processor cmdProcessor;

    public void onReadyEvent(ReadyEvent event) {
        Logger.log(Logger.Level.INFO, "Bot signed in with name: " + event.getSelf().getUsername());

        // Init simple cron to keep application alive.
        cmdProcessor = new Processor();
//        cmdProcessor.addCommand(new Ping());
    }


    public void onMessageEvent(MessageCreateEvent event) {
        if (event.getMessage().getContent().toLowerCase().startsWith(PREFIX)) {
            try {
                cmdProcessor.execute(event.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
