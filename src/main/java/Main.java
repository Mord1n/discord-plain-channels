import discord.Annotation;
import discord.VoiceStateEventListener;
import discord.utils.Config;
import discord.utils.Logger;
import discord4j.core.DiscordClient;
import discord4j.core.event.domain.VoiceStateUpdateEvent;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.core.scheduler.Schedulers;

public class Main {

    private static final Annotation annotation = new Annotation();
    private static Config config;
    private static VoiceStateEventListener voiceStateEventListener;

    public static void main(String[] args) {
        config = new Config();
        voiceStateEventListener = new VoiceStateEventListener();
        registerEvents();
    }

    public static void registerEvents() {
        DiscordClient.create(config.getDiscordToken()).withGateway(client -> {
                client.getEventDispatcher().on(ReadyEvent.class).subscribe(annotation::onReadyEvent);

                client.getEventDispatcher().on(MessageCreateEvent.class).subscribe(messageCreateEvent -> {
                    try {
                        annotation.onMessageEvent(messageCreateEvent);
                    } catch (Exception e) {
                        Logger.log(Logger.Level.ERROR, "Error with message." + e.getLocalizedMessage());
                        e.printStackTrace();
                    }
                });

                client.getEventDispatcher().on(VoiceStateUpdateEvent.class).subscribe(voiceStateUpdateEvent -> {
                    try {
                        voiceStateEventListener.execute(voiceStateUpdateEvent).subscribe();
                    } catch (Exception e) {
                        Logger.log(Logger.Level.ERROR, "Error on voice change." + e.getLocalizedMessage());
                        e.printStackTrace();
                    }
                });

                return client.onDisconnect();
            })
            .block();
    }
}
