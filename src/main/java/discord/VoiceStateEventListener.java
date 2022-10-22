package discord;

import com.vdurmont.emoji.EmojiParser;
import discord4j.core.event.domain.VoiceStateUpdateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.channel.Category;
import discord4j.core.object.entity.channel.GuildChannel;
import discord4j.core.object.entity.channel.VoiceChannel;
import discord4j.core.spec.GuildMemberEditSpec;
import discord4j.core.spec.VoiceChannelCreateSpec;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class VoiceStateEventListener implements IEventListener<VoiceStateUpdateEvent> {

    private static final String CHANNEL_CATEGORY_PREFIX = ":black_small_square:";
    private static final String CREATE_CHANNEL_CATEGORY_PREFIX = ":white_small_square:";

    @Override
    public Class<VoiceStateUpdateEvent> getEventType() {
        return VoiceStateUpdateEvent.class;
    }

    @Override
    public Mono<Void> execute(VoiceStateUpdateEvent event) {

        // ----------- Delete old channel block
        if (event.getOld().isPresent()) {
            event.getOld().get().getChannel().doOnSuccess(oldChannel -> {
                oldChannel.getVoiceStates().collect(Collectors.toList()).subscribe(users -> {
                    if (users.isEmpty()) {
                        oldChannel.getCategory().subscribe(category -> {
                            if (endsWithPrefix(category, CHANNEL_CATEGORY_PREFIX)) {
                                oldChannel.delete().block();
                            }
                        });
                    }
                });
            }).subscribe();
        }
        // ----------- END

        // ----------- Create channel block
        event.getCurrent().getChannel().subscribe(channel -> {
            System.out.println("Joined: " + channel.getName());
            Category channelCategory = channel.getCategory().block();
            if (endsWithPrefix(channelCategory, CREATE_CHANNEL_CATEGORY_PREFIX)) {
                System.out.println("Create category!");
                VoiceChannel newChannel = createChannel(event);
                Consumer memberSpec = a -> ((GuildMemberEditSpec) a).setNewVoiceChannel(newChannel.getId());
                event.getCurrent().getMember().subscribe(member -> member.edit(memberSpec).subscribe());
                /*
                3.2.2
                GuildMemberEditSpec guildMemberEditSpec = GuildMemberEditSpec.builder().newVoiceChannelOrNull(newChannel.getId()).build();
                event.getCurrent().getMember().subscribe(member -> member.edit(guildMemberEditSpec).block());
                 */
            } else if (endsWithPrefix(channelCategory, CHANNEL_CATEGORY_PREFIX)) {
                System.out.println("Game Category!");
            }
        });
        // ----------- END

        return Mono.empty();
    }

    boolean endsWithPrefix(Category category, String prefix) {
        String[] s = category.getName().split(" ");
        String icon = EmojiParser.parseToAliases(s[s.length - 1]);
        return icon.equalsIgnoreCase(prefix);
    }

    VoiceChannel createChannel(VoiceStateUpdateEvent event) {
        Guild guild = event.getCurrent().getGuild().block();
        VoiceChannel joinedChannel = event.getCurrent().getChannel().block();
        int size = findIntegers(joinedChannel.getName());
        GuildChannel channelCategory = getChannelCategory(guild);

        Consumer spec = a -> {
            ((VoiceChannelCreateSpec) a).setName(joinedChannel.getName());
            ((VoiceChannelCreateSpec) a).setBitrate(joinedChannel.getBitrate());
            ((VoiceChannelCreateSpec) a).setUserLimit(size);
            ((VoiceChannelCreateSpec) a).setParentId(channelCategory.getId());
        };
        return (VoiceChannel) guild.createVoiceChannel(spec).block();
        /*

        3.2.2

        VoiceChannelCreateSpec newChannel = VoiceChannelCreateSpec.builder()
                .userLimit(size)
                .name(joinedChannel.getName())
                .bitrate(joinedChannel.getBitrate())
                .parentId(channelCategory.getId())
                .build();
        return guild.createVoiceChannel(newChannel).block();

         */
    }

    int findIntegers(String stringToSearch) {
        Pattern integerPattern = Pattern.compile("-?\\d+");
        Matcher matcher = integerPattern.matcher(stringToSearch);

        List<String> integerList = new ArrayList<>();
        while (matcher.find()) {
            integerList.add(matcher.group());
        }

        // If we didnt find any number make it unlimited.
        if (integerList.size() < 1) {
            return 0;
        }
        int i = parseInteger(integerList, 0);

        // No bigger than 99 plz, make it unlimited.
        if (i > 99) {
            return 0;
        }

        // turn negative numbers to positive.
        if (i < 0) {
            i = i * -1;
        }
        System.out.println("Found integer: " + i);
        return i;
    }

    private int parseInteger(List<String> strArr, int index) {
        if (strArr == null || strArr.size() < index) {
            System.out.println("Could not parse nothing.");
            return 0;
        }
        try {
            System.out.println("Parsed integer: " + Integer.parseInt(strArr.get(index)));
            return Integer.parseInt(strArr.get(index));
        } catch (Exception e) {
            System.out.println("Exception parsing");
            return 0;
        }
    }

    GuildChannel getChannelCategory(Guild guild) {
        List<GuildChannel> categories = guild.getChannels()
                .filter(channel -> channel.getType().equals(Category.Type.GUILD_CATEGORY))
                .filter(channel -> endsWithPrefix((Category) channel, CHANNEL_CATEGORY_PREFIX))
                .collect(Collectors.toList())
                .block();
        if (categories.size() > 0) {
            return categories.get(0);
        } else {
            return null;
        }
    }


}