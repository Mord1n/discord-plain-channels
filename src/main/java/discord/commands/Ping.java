package discord.commands;

import discord.ICommand;
import discord4j.core.object.entity.Message;
import reactor.core.publisher.Mono;

public class Ping implements ICommand {
    public String getName() {
        return "ping";
    }

    public Boolean execute(Message message) {
        System.out.println(message.getContent());
        message.getChannel().flatMap(channel -> channel.createMessage("pong!")).then();
        return true;
    }
}
