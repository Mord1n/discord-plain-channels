package discord;

import discord4j.core.event.domain.Event;
import reactor.core.publisher.Mono;

/**
 * Allows multiple event types to be implemented.
 *
 * @param <T>
 */
public interface IEventListener<T extends Event> {
//    Logger LOG = LoggerFactory.getLogger(IEventListener.class);

    Class<T> getEventType();

    Mono<Void> execute(T event);

    default Mono<Void> handleError(Throwable error) {
//      LOG.error("Unable to process " + getEventType().getSimpleName(), error);
        return Mono.empty();
    }
}