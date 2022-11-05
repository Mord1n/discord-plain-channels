package discord;

import discord.interfaces.IEventListener;
import discord.utils.Channel;
import discord.utils.Config;
import discord.utils.Emoji;
import discord.utils.Logger;
import discord4j.core.event.domain.VoiceStateUpdateEvent;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

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
            Logger.log(Logger.Level.ERROR, e.getMessage());
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

                                    Logger.log(Logger.Level.INFO, "createChannel: '" + member.getDisplayName() + "' created channel.");
                                })
                            );
                    }
                })
            );
    }
}