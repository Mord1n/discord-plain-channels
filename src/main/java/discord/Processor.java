package discord;

import discord.interfaces.ICommand;
import discord.utils.Logger;
import discord4j.core.object.entity.Message;

import java.util.ArrayList;
import java.util.List;

public class Processor {

    private static List<ICommand> commands = new ArrayList<>();

    public static void execute(Message message) {
        String[] command = message.getContent().toLowerCase().replaceFirst(Annotation.PREFIX, "").split(" ");

        if (command.length < 1) {
            return;
        }
        for (ICommand cmd : commands) {
            if (cmd.getName().toLowerCase().equals(command[0].trim().toLowerCase())) {
                if (cmd.execute(message)) {
                    Logger.log(Logger.Level.TRACE, "Success!");
                } else {
                    Logger.log(Logger.Level.TRACE, "Crapz!");
                }
            }
        }
    }

    public void addCommand(ICommand cmd) {
        if (!commands.contains(cmd.getName())) {
            commands.add(cmd);
        }
    }
}
