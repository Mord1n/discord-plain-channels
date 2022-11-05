package discord.commands;

import discord.interfaces.ICommand;
import discord4j.core.object.entity.Message;

public class Ping implements ICommand {
    public String getName() {
        return "ping";
    }

    public Boolean execute(Message message) {
        message.getChannel().flatMap(channel -> channel.createMessage("pong!")).then();
        return true;
    }
}
