package discord;

import discord4j.core.object.entity.Message;
import reactor.core.publisher.Mono;

public interface ICommand {
        String getName();
        Boolean execute(Message message);
}
