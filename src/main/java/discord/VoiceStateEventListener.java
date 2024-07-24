package discord;

import discord.interfaces.IEventListener;
import discord.utils.Channel;
import discord.utils.Config;
import discord.utils.Emoji;
import discord.utils.Logger;
import discord.utils.Logger.Level;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.VoiceStateUpdateEvent;
import discord4j.core.object.PermissionOverwrite;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.VoiceChannel;
import discord4j.core.spec.GuildMemberEditSpec;
import discord4j.core.spec.VoiceChannelEditSpec;
import discord4j.rest.util.PermissionSet;

import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class VoiceStateEventListener implements IEventListener<VoiceStateUpdateEvent> {

    @Override
    public Class<VoiceStateUpdateEvent> getEventType() {
        return VoiceStateUpdateEvent.class;
    }

    private Config config = new Config();

    @Override
    public Mono<Void> execute(VoiceStateUpdateEvent event) {
            // ----------- Delete old channel block
            this.deleteOldChannel(event);
            // ----------- END

            // ----------- Create channel block
            this.createChannel(event);
            // ----------- END
        return Mono.empty();
    }

    private void deleteOldChannel(VoiceStateUpdateEvent event) {
        event.getOld()
            .map(old -> old.getChannel()
                .doOnSuccess(oldChannel -> oldChannel.getVoiceStates()
                    .collect(Collectors.toList())
                    .doOnSuccess(members -> {
                        if (members.isEmpty()) {
                            Channel.deleteOldChannelWhenEmpty(oldChannel);
                        } else {
                            Channel.checkIfLeaderLeftChannel(old, oldChannel, members);
                        }
                    }).subscribe()
                ).subscribeOn(Schedulers.newParallel("deleteOldThread")).subscribe());
    }

    private void createChannel(VoiceStateUpdateEvent event) {
        event.getCurrent()
            .getChannel()
            .subscribeOn(Schedulers.newParallel("createChannelThread"))
            .subscribe(channel -> channel.getCategory()
                .subscribe(category -> {
                    if (Emoji.endsWithEmoji(category.getName(), this.config.getCreateCategoryEmoji())) {
                        event.getCurrent()
                            .getMember()
                            .subscribe(member -> Channel.create(event.getCurrent(), channel, member)
                                .subscribeOn(Schedulers.newParallel("createNewChannelThread"))
                                .subscribe(voiceChannel -> {
                                    this.moveMember(member, voiceChannel.getId());
                                    Logger.log(Level.INFO, "createChannel: '" + member.getDisplayName() + "' created channel.");
                                })
                            );
                    }
                })
            );
    }

    private Disposable moveMember(Member member, Snowflake voiceChannelId) {
        return member.edit(GuildMemberEditSpec.builder()
                .newVoiceChannelOrNull(voiceChannelId)
                .build())
            .subscribeOn(Schedulers.newParallel("moveMemberThread"))
            .subscribe();
    }
}