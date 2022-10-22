package discord;

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
                    System.out.println("Success!");
                } else {
                    System.out.println("Crapz!");
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
