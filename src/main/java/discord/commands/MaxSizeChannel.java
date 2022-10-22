package discord.commands;

import discord.ICommand;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.VoiceChannel;
import discord4j.core.spec.VoiceChannelEditSpec;

import java.util.function.Consumer;

public class MaxSizeChannel implements ICommand {
    public String getName() {
        return "max";
    }

    public Boolean execute(Message message) {

        // TEMPORARLY DISABLED
        message.delete().block();
        return true;

        /*
        Member user = message.getAuthorAsMember().block();
        VoiceState state = user.getVoiceState().block();
        VoiceChannel channel = state.getChannel().block();

        String[] s = message.getContent().split(" ");
        System.out.println(s.length);
        if (s.length < 2) {
            return false;
        }

        int sizeBuilder = parseInteger(s, 1);
        if (sizeBuilder > 99) {
            sizeBuilder = 99;
        }

        System.out.println(sizeBuilder);

        final int size = sizeBuilder;
        if (size != -1) {
            System.out.println(user.getDisplayName() + " changed " + channel.getName() + " size to " + size);
            Consumer editChannel = a -> {
                VoiceChannelEditSpec voiceSpec = (VoiceChannelEditSpec) a;
                voiceSpec.setUserLimit(size);
            };
            channel.edit(editChannel).block();
            message.delete().block();
            return true;
        }

        return false;
        */
    }

    private int parseInteger(String[] strArr, int index) {
        if (strArr == null || strArr.length < index) {
            return -1;
        }
        try {
            return Integer.parseInt(strArr[index]);
        } catch (Exception e) {
            return -1;
        }
    }
}
