package reactivestack.controller;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.marshalling.Marshaller;
import akka.http.javadsl.model.HttpEntity;
import akka.http.javadsl.model.RequestEntity;
import akka.http.javadsl.model.StatusCode;
import akka.http.javadsl.server.PathMatchers;
import akka.http.javadsl.server.Route;
import akka.http.javadsl.server.directives.RouteAdapter;
import akka.http.javadsl.unmarshalling.StringUnmarshallers;
import akka.http.javadsl.unmarshalling.Unmarshaller;
import akka.util.Timeout;
import reactivestack.actor.EmojiActor;
import reactivestack.model.ActorCommand.*;
import reactivestack.model.Emoji;
import scala.concurrent.duration.FiniteDuration;

import java.time.Instant;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static akka.http.javadsl.model.StatusCodes.*;
import static akka.http.javadsl.server.Directives.*;
import static akka.http.javadsl.server.Directives.get;
import static akka.pattern.PatternsCS.ask;

public class Routes {

    private static final Timeout timeout = Timeout.durationToTimeout(FiniteDuration.apply(5, TimeUnit.SECONDS));
    private final Marshaller<Object, RequestEntity> mapper = Jackson.marshaller();
    private final Unmarshaller<HttpEntity, Emoji> emojiUnmarshaller = Jackson.unmarshaller(Emoji.class);
    private final ActorRef actor;
    private final LoggingAdapter log;


    public Routes(final ActorSystem system) {
        actor = system.actorOf(EmojiActor.props(), "emoji");
        log = Logging.getLogger(system, this);
    }

    public Route createRoute() {
        return route(
            pathPrefix("emojis", () -> route(
                path("recent", listRecentsWithOk()),

                path(PathMatchers.segment(), code -> route(
                    get(() -> getByCodeWithOk(code)),
                    delete(() -> deleteWithNoContent(code)),

                    parameter(StringUnmarshallers.STRING, "usedAt", usedAt ->
                        post(() -> saveAtDateWithCreated(code, usedAt))
                    )
                )),

                get(this::listWithOk),
                post(createWithCreated())
            ))
        );
    }

    private Supplier<Route> listRecentsWithOk() {
        return () -> parameter(StringUnmarshallers.INTEGER, "seconds", seconds ->
            get(() -> {
                log.debug("Received request to list recent emojis");
                return askAndRespond(new ListRecent(seconds), OK);
            })
        );
    }

    private RouteAdapter getByCodeWithOk(String code) {
        log.debug("Get by code" + code);
        return askAndRespond(new GetByCode(code), OK);
    }

    private Route deleteWithNoContent(String code) {
        log.debug("Delete by code " + code);
        return askAndRespond(new DeleteByCode(code), NO_CONTENT);
    }

    private RouteAdapter saveAtDateWithCreated(String code, String usedAt) {
        var instant = Instant.parse(usedAt);
        log.debug("Save with code {} at date {}", code, usedAt);
        return askAndRespond(new StoreAtDate(code, instant), CREATED);
    }

    private Route listWithOk() {
        log.debug("Received request to list emojis");
        return askAndRespond(new ListAll(), OK);
    }

    private Supplier<Route> createWithCreated() {
        return () -> entity(emojiUnmarshaller, emoji -> {
            log.debug("Received request to create emoji");
            return askAndRespond(new Create(emoji), CREATED);
        });
    }

    private RouteAdapter askAndRespond(final Object command, final StatusCode successStatusCode) {
        CompletionStage<Object> response = ask(actor, command, timeout);
        return onSuccess(response, done ->
            complete(successStatusCode, done, mapper)
        );
    }

}
