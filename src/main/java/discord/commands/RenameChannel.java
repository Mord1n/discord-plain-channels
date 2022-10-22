package discord.commands;

import discord.ICommand;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.VoiceChannel;
import discord4j.core.spec.VoiceChannelEditSpec;

import java.util.function.Consumer;

public class RenameChannel implements ICommand {
    public String getName() {
        return "name";
    }

    public Boolean execute(Message message) {

        // TEMPORARLY DISABLED
        message.delete().block();
        return true;

        /*
        Member user = message.getAuthorAsMember().block();
        VoiceState state = user.getVoiceState().block();
        VoiceChannel channel = state.getChannel().block();

        String[] parts = message.getContent().split(" ");
        String nameBuilder = "";
        for (int i = 1; i < parts.length; i++) {

            String text = parts[i].replaceAll("[^a-zA-Z0-9]", "");
            if (!text.isEmpty()) {
                nameBuilder += text;
            } else {
                nameBuilder += "s";
            }

            if (i < parts.length - 1) {
                nameBuilder += " ";
            }
        }

        if (nameBuilder.length() > 19) {
            nameBuilder = nameBuilder.substring(0, 20);
        }

        final String name = nameBuilder;
        System.out.println(user.getDisplayName() + " changed " + channel.getName() + " name to  " + name);
        Consumer editChannel = a -> {
            VoiceChannelEditSpec voiceSpec = (VoiceChannelEditSpec) a;
            voiceSpec.setName(name);
            System.out.println("Renaming channel: " + channel.getName() + " -> " + name);
        };
        channel.edit(editChannel).subscribe();
        message.delete().block();
        return true;
         */
    }
}
