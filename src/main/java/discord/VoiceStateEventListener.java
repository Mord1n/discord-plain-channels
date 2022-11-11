package discord;

import discord.interfaces.IEventListener;
import discord.utils.Channel;
import discord.utils.Config;
import discord.utils.Emoji;
import discord.utils.Logger;
import discord.utils.Logger.Level;

import discord4j.core.event.domain.VoiceStateUpdateEvent;
import discord4j.core.object.PermissionOverwrite;
import discord4j.core.object.VoiceState;
import discord4j.core.object.entity.channel.VoiceChannel;
import discord4j.rest.util.PermissionSet;

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

    private Config config;

    @Override
    public Mono<Void> execute(VoiceStateUpdateEvent event) {
        this.config = new Config();
        try {
            // ----------- Delete old channel block
            this.deleteOldChannel(event);
            // ----------- END

            // ----------- Create channel block
            this.createChannel(event);
            // ----------- END
        } catch (Exception e) {
            Logger.log(Level.ERROR, e.getMessage());
        }

        return Mono.empty();
    }

    private void deleteOldChannel(VoiceStateUpdateEvent event) {
        event.getOld()
            .map(old -> old.getChannel()
                .doOnSuccess(oldChannel -> oldChannel.getVoiceStates()
                    .collect(Collectors.toList())
                    .doOnSuccess(members -> {
                        if (members.isEmpty()) {
                            this.deleteOldChannelWhenEmpty(oldChannel);
                        } else {
                            this.checkIfLeaderLeftChannel(old, oldChannel, members);
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

                                    member.edit(a -> a.setNewVoiceChannel(voiceChannel.getId()))
                                        .subscribeOn(Schedulers.newParallel("moveMemberThread"))
                                        .subscribe();

                                    Logger.log(Level.INFO, "createChannel: '" + member.getDisplayName() + "' created channel.");
                                })
                            );
                    }
                })
            );
    }

    private void deleteOldChannelWhenEmpty(VoiceChannel oldChannel) {
        oldChannel.getCategory()
            .doOnSuccess(category -> {
                if (Emoji.endsWithEmoji(category.getName(), this.config.getVoiceCategoryEmoji())) {
                    String name = oldChannel.getName();

                    oldChannel.delete()
                        .subscribeOn(Schedulers.newParallel("deleteOldChannelThread"))
                        .subscribe();

                    Logger.log(Logger.Level.INFO, "deleteOldChannel: '" + name + "' was deleted.");
                }
            }).subscribe();
    }

    private void checkIfLeaderLeftChannel(VoiceState old, VoiceChannel oldChannel, List<VoiceState> members) {
        old.getMember()
            .subscribeOn(Schedulers.newParallel("checkLeaderLeaveThread"))
            .subscribe(member -> oldChannel.getPermissionOverwrites()
                .forEach(permission -> permission.getMemberId()
                    .ifPresent(id -> {
                        if (id.equals(member.getId()) && permission.getAllowed().getRawValue() == 16L) {
                            members.get(0)
                                .getMember()
                                .subscribe(firstMember -> oldChannel.edit(channel -> {
                                        Set<PermissionOverwrite> permissions = new HashSet<>();
                                        permissions.add(PermissionOverwrite.forMember(
                                            firstMember.getId(),
                                            PermissionSet.of(16L), PermissionSet.none()
                                        ));
                                        channel.setPermissionOverwrites(permissions);
                                        Logger.log(Level.INFO, "Updated Permissions");
                                    }).subscribe()
                                );
                        }
                    })
                )
            );
    }
}