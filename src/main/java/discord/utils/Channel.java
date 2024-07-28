package discord.utils;

import discord4j.core.object.PermissionOverwrite;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.Category;
import discord4j.core.object.entity.channel.GuildChannel;
import discord4j.core.object.entity.channel.VoiceChannel;
import discord4j.core.spec.VoiceChannelCreateSpec;
import discord4j.core.spec.VoiceChannelEditSpec;
import discord4j.rest.util.PermissionSet;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Channel {

    private static final Config config = new Config();

    public static Mono<Void> deleteChannel(VoiceChannel oldChannel) {
        return oldChannel.delete()
            .subscribeOn(Schedulers.newParallel("deleteOldChannelThread"));
    }


    public static Mono<VoiceChannel> create(VoiceState current, VoiceChannel joinedChannelInCreateCategory, Member member) {
        return current.getGuild()
            .subscribeOn(Schedulers.newParallel("createGetGuildThread"))
            .flatMap(guild -> getVoiceChannelCategory(guild)
                .flatMap(voiceChannelCategory ->
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
        return guild.createVoiceChannel(VoiceChannelCreateSpec.builder()
            .name(member.getDisplayName() + " Channel")
            .bitrate(joinedChannel.getBitrate())
            .userLimit(joinedChannel.getUserLimit())
            .parentId(guildChannel.getId())
            .permissionOverwrites(List.of(
                PermissionOverwrite.forMember(
                    member.getId(),
                    PermissionSet.of(16L),
                    PermissionSet.none()
                ))
            )
            .build());
    }

    public static void checkIfLeaderLeftChannel(VoiceState old, VoiceChannel oldChannel, List<VoiceState> members) {
        old.getMember()
            .subscribeOn(Schedulers.newParallel("checkLeaderLeaveThread"))
            .subscribe(member -> oldChannel.getPermissionOverwrites()
                .forEach(permission -> permission.getMemberId()
                    .ifPresent(id -> {
                        if (id.equals(member.getId()) && permission.getAllowed().getRawValue() == 16L) {
                            members.get(0)
                                .getMember()
                                .subscribe(firstMember -> oldChannel.edit(
                                        VoiceChannelEditSpec.builder()
                                            .permissionOverwrites(List.of(
                                                PermissionOverwrite.forMember(
                                                    firstMember.getId(),
                                                    PermissionSet.of(16L),
                                                    PermissionSet.none()
                                                )
                                            )).build()
                                    ).subscribe()
                                );
                        }
                    })
                )
            );
    }

    public static void deleteOldChannelWhenEmpty(VoiceChannel oldChannel) {
        oldChannel.getCategory()
            .doOnSuccess(category -> {
                if (Emoji.endsWithEmoji(category.getName(), config.getVoiceCategoryEmoji())) {
                    String name = oldChannel.getName();
                    Channel.deleteChannel(oldChannel).subscribe();
                    Logger.log(Logger.Level.INFO, "deleteOldChannel: '" + name + "' was deleted.");
                }
            }).subscribe();
    }

}