package discord.utils;

import discord4j.common.util.Snowflake;
import discord4j.core.object.PermissionOverwrite;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Role;
import discord4j.core.object.entity.channel.*;
import discord4j.rest.util.Permission;
import discord4j.rest.util.PermissionSet;
import org.javatuples.Pair;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.HashSet;
import java.util.Set;

public class Channel {

    private static final Config config = new Config();


    public static Mono<Pair<Mono<VoiceChannel>,Mono<TextChannel>>> create(VoiceState current, VoiceChannel joinedChannelInCreateCategory, Member member) {
        return current.getGuild()
            .subscribeOn(Schedulers.newParallel("createGetGuildThread"))
            .flatMap(guild ->
                getVoiceChannelCategory(guild).flatMap(voiceChannelCategory -> Mono.just(Pair.with(
                    createVoiceChannel(guild, joinedChannelInCreateCategory, voiceChannelCategory, member),
                    createTextChannel(guild, voiceChannelCategory, member)
                )))
            );
    }

    public static Mono<GuildChannel> getVoiceChannelCategory(Guild guild) {
        return guild.getChannels()
            .filter(channel -> channel.getType().equals(Category.Type.GUILD_CATEGORY))
            .filter(channel -> Emoji.endsWithEmoji(channel.getName(), config.getVoiceCategoryEmoji()))
            .single();
    }

    public static Mono<TextChannel> createTextChannel(Guild guild, GuildChannel category, Member member) {
        Set<PermissionOverwrite> permissions = new HashSet<>();
        permissions.add(PermissionOverwrite.forMember(
            member.getId(), PermissionSet.of(Permission.VIEW_CHANNEL.getValue()), PermissionSet.none()));

        guild.getRoles().filter(Role::isEveryone).subscribe(everyone ->
            permissions.add(PermissionOverwrite.forRole(everyone.getId(), PermissionSet.none(),
                PermissionSet.of(Permission.VIEW_CHANNEL.getValue()))));

        return guild.createTextChannel(a ->
            a.setName(member.getDisplayName().toLowerCase().replace(' ', '-') + "-text-channel")
            .setPermissionOverwrites(permissions)
            .setParentId(category.getId())
            .setPermissionOverwrites(permissions)
        );
    }

    public static Mono<VoiceChannel> createVoiceChannel(Guild guild, VoiceChannel joinedChannel, GuildChannel category, Member member) {
        Int size = new Int()
            .findAllIntegersInString(joinedChannel.getName())
            .withFallback(joinedChannel.getUserLimit());
        Set<PermissionOverwrite> permissions = new HashSet<>(joinedChannel.getPermissionOverwrites());
        permissions.add(PermissionOverwrite.forMember(
            member.getId(), PermissionSet.of(Permission.MANAGE_CHANNELS.getValue()), PermissionSet.none()));

        return guild.createVoiceChannel(a ->
            a.setName(member.getDisplayName() + " Voice Channel")
            .setBitrate(joinedChannel.getBitrate())
            .setUserLimit(size.getValue())
            .setParentId(category.getId())
            .setPermissionOverwrites(permissions)
        );
    }
}