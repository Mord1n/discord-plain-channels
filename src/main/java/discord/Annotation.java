package discord;

import discord.commands.MaxSizeChannel;
import discord.commands.Ping;
import discord.commands.RenameChannel;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;


public class Annotation {
    public final static String PREFIX = "=";
    public static Processor cmdProcessor;

    public void onReadyEvent(ReadyEvent event) {
        System.out.println("Bot signed in with name: " + event.getSelf().getUsername());

        // Init simple cron to keep application alive.
        cmdProcessor = new Processor();
        cmdProcessor.addCommand(new Ping());
        cmdProcessor.addCommand(new RenameChannel());
        cmdProcessor.addCommand(new MaxSizeChannel());
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
