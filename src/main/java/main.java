import discord.Annotation;
import discord.VoiceStateEventListener;
import discord4j.core.DiscordClient;
import discord4j.core.event.domain.VoiceStateUpdateEvent;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;

public class main {

    public final static Annotation annotation = new Annotation();
    public static String TOKEN = "";
    public static VoiceStateEventListener voiceStateEventListener;

    public static void main(String[] args) {
        voiceStateEventListener = new VoiceStateEventListener();
        registerEvents();
    }

    public static void registerEvents() {
        DiscordClient.create(TOKEN)
                .withGateway(client -> {
                    client.getEventDispatcher()
                            .on(ReadyEvent.class)
                            .subscribe(ready -> {
                                annotation.onReadyEvent(ready);
                            });

                    client.getEventDispatcher()
                            .on(MessageCreateEvent.class)
                            .subscribe(messageCreateEvent ->
                            {
                                try {
                                    annotation.onMessageEvent(messageCreateEvent);
                                } catch (Exception e) {
                                    System.out.println("Error with message." + e.getLocalizedMessage());
                                    e.printStackTrace();
                                }
                            });

                    client.getEventDispatcher().on(VoiceStateUpdateEvent.class)
                            .subscribe(voiceStateUpdateEvent -> {
                                try {
                                    voiceStateEventListener.execute(voiceStateUpdateEvent).subscribe();
                                } catch (Exception e) {
                                    System.out.println("Error on voice change." + e.getLocalizedMessage());
                                    e.printStackTrace();
                                }
                            });

                    return client.onDisconnect();
                })
                .block();
    }
}
