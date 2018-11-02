package reactivestack.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactivestack.model.Emoji;
import reactivestack.model.EmojiUsage;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;
import static reactivestack.model.CategoryType.SMILEYS_AND_EMOTION;

@Slf4j
public class EmojiHandler {

    public Mono<ServerResponse> listWithOK(final ServerRequest request) {
        log.debug("Listing all emojis");
        var e1 = new Emoji("1", SMILEYS_AND_EMOTION, "xxx", 1);
        var e2 = new Emoji("2", SMILEYS_AND_EMOTION, "yyy", 2);
        var response = List.of(e1, e2);
        return respondWith(OK, response);
    }

    public Mono<ServerResponse> createWithCreated(final ServerRequest request) {
        log.info("Creating emoji");
        return respondWith(CREATED);
    }

    public Mono<ServerResponse> listRecentsWithOk(ServerRequest request) {
        log.debug("Listing emoji usage of the last {} seconds", request.queryParam("usedAt"));
        var u1 = new EmojiUsage("1", Instant.now());
        var u2 = new EmojiUsage("1", Instant.now().minus(1, ChronoUnit.HOURS));
        var response = List.of(u1, u2);
        return respondWith(OK, response);
    }

    public Mono<ServerResponse> getByCodeWithOk(ServerRequest request) {
        var code = request.pathVariable("code");
        log.debug("Getting emoji with code " + code);
        var emoji = new Emoji(code, SMILEYS_AND_EMOTION, "xxx", 1);
        return respondWith(OK, emoji);
    }

    public Mono<ServerResponse> deleteWithNoContent(ServerRequest request) {
        var code = request.pathVariable("code");
        log.debug("Delete by code " + code);
        return respondWith(NO_CONTENT);
    }

    public Mono<ServerResponse> saveAtDateWithCreated(ServerRequest request) {
        var code = request.pathVariable("code");
        var usedAt = request.queryParam("usedAt").get();
        var instant = Instant.parse(usedAt);
        log.info("Save with code {} at date {}", code, instant);
        return respondWith(CREATED);
    }

    private Mono<ServerResponse> respondWith(final HttpStatus status) {
        return respondWith(status, null);
    }

    private Mono<ServerResponse> respondWith(final HttpStatus status, final Object payload) {
        var builder = ServerResponse
            .status(status)
            .contentType(MediaType.APPLICATION_JSON_UTF8);

        if (payload != null) {
            ParameterizedTypeReference<Object> typeReference = ParameterizedTypeReference.forType(payload.getClass());
            return builder.body(Mono.just(payload), typeReference);
        } else {
            return builder.build();
        }
    }
}
