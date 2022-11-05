package discord.interfaces;

import discord4j.core.object.entity.Message;

public interface ICommand {
    String getName();

    Boolean execute(Message message);
}
