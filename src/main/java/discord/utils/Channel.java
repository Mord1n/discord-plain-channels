package discord.utils;

import discord4j.core.object.PermissionOverwrite;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.Category;
import discord4j.core.object.entity.channel.GuildChannel;
import discord4j.core.object.entity.channel.VoiceChannel;
import discord4j.rest.util.PermissionSet;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.HashSet;
import java.util.Set;

public class Channel {

    private static final Config config = new Config();

    public static Mono<VoiceChannel> create(VoiceState current, VoiceChannel joinedChannelInCreateCategory, Member member) {
        return current.getGuild()
            .subscribeOn(Schedulers.newParallel("createGetGuildThread"))
            .flatMap(guild ->
                getVoiceChannelCategory(guild).flatMap(voiceChannelCategory ->
                    createChannel(guild, joinedChannelInCreateCategory, voiceChannelCategory, member)
                ));

    }

    public static Mono<GuildChannel> getVoiceChannelCategory(Guild guild) {
        return guild.getChannels()
            .filter(channel -> channel.getType().equals(Category.Type.GUILD_CATEGORY))
            .filter(channel -> Emoji.endsWithEmoji(channel.getName(), config.getVoiceCategoryEmoji()))
            .single();
    }

    public static Mono<VoiceChannel> createChannel(Guild guild, VoiceChannel joinedChannel, GuildChannel guildChannel, Member member) {
        Int size = new Int()
            .findAllIntegersInString(joinedChannel.getName())
            .withFallback(joinedChannel.getUserLimit());
        Set<PermissionOverwrite> permissions = new HashSet<>(joinedChannel.getPermissionOverwrites());
        permissions.add(PermissionOverwrite.forMember(
            member.getId(), PermissionSet.of(16L), PermissionSet.none()));
        //TODO: Cange to guild.createVoiceChannel(VoiceChannelCreateSpec)
        return guild.createVoiceChannel(a -> {
            a.setName(joinedChannel.getName());
            a.setBitrate(joinedChannel.getBitrate());
            a.setUserLimit(size.getValue());
            a.setParentId(guildChannel.getId());
            a.setPermissionOverwrites(permissions);
        });
    }
}