package reactivestack.controller;

import akka.Done;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.model.*;
import akka.http.javadsl.server.PathMatchers;
import akka.http.javadsl.server.Route;
import akka.http.javadsl.server.directives.RouteAdapter;
import akka.http.javadsl.unmarshalling.StringUnmarshallers;
import akka.http.javadsl.unmarshalling.Unmarshaller;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import reactivestack.actor.EmojiActor;
import reactivestack.model.ActorCommand.*;
import reactivestack.model.Emoji;

import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.function.Supplier;

import static akka.http.javadsl.model.StatusCodes.*;
import static akka.http.javadsl.server.Directives.*;
import static akka.http.javadsl.server.Directives.get;
import static akka.pattern.PatternsCS.ask;
import static reactivestack.bootstrap.Server.TIMEOUT;

public class EmojiRoutes {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Unmarshaller<HttpEntity, Emoji> emojiUnmarshaller = Jackson.unmarshaller(Emoji.class);
    private final ActorRef actor;
    private final LoggingAdapter log;


    public EmojiRoutes(final ActorSystem system) {
        actor = system.actorOf(EmojiActor.props(), EmojiActor.class.getSimpleName());
        log = Logging.getLogger(system, this);
    }

    public Route createRoute() {
        return route(
            pathPrefix("emojis", () -> route(
                path("recent", listRecentsWithOk()),

                path(PathMatchers.segment(), code -> route(
                    get(() -> getByCodeWithOk(code)),
                    delete(() -> deleteWithNoContent(code)),

                    post(() -> saveAtDateWithCreated(code, Instant.now().toString())),
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
        var response = ask(actor, command, TIMEOUT);
        return onComplete(response, result -> makeRouteAdapter(successStatusCode, result.get()));
    }

    private Route makeRouteAdapter(StatusCode successStatusCode, Object payload) {
        if (payload instanceof NoSuchElementException) {
            log.error("Item not found: ", payload);
            return completeResponse(StatusCodes.NOT_FOUND);
        } else if (payload instanceof Exception) {
            log.error("Operation failed: ", payload);
            return completeResponse(StatusCodes.INTERNAL_SERVER_ERROR, ((Exception) payload).getMessage());
        } else if (payload instanceof Done) {
            return completeResponse(successStatusCode);
        } else {
            return completeResponse(successStatusCode, payload);
        }
    }

    private Route completeResponse(final StatusCode statusCode) {
        return completeResponse(statusCode, null);
    }

    private Route completeResponse(final StatusCode statusCode, final Object payload) {
        var responseStatus = HttpResponse.create().withStatus(statusCode);
        var response = addResponseBody(responseStatus, payload);
        return complete(response);
    }

    private HttpResponse addResponseBody(final HttpResponse response, final Object payload) {
        if (payload != null) {
            try {
                var json = objectMapper.writeValueAsString(payload);
                var body = HttpEntities.create(ContentTypes.APPLICATION_JSON, json);
                return response.withEntity(body);
            } catch (JsonProcessingException e) {
                log.error("Could not serialize payload", e);
            }
        }
        return response;
    }

}
