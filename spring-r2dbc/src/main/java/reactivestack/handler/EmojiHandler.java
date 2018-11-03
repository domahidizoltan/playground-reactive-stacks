package reactivestack.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactivestack.model.Emoji;
import reactivestack.repository.EmojiRepository;
import reactor.core.publisher.Mono;

import java.time.Instant;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Slf4j
public class EmojiHandler {

    private EmojiRepository emojiRepository;

    public EmojiHandler(final EmojiRepository emojiRepository) {
        this.emojiRepository = emojiRepository;
    }

    public Mono<ServerResponse> listWithOK(final ServerRequest request) {
        log.debug("Listing all emojis");
        return emojiRepository.findAll()
            .flatMap(body -> ServerResponse.ok().syncBody(body));
    }

    public Mono<ServerResponse> createWithCreated(final ServerRequest request) {
        log.info("Creating emoji");
        return request.bodyToMono(Emoji.class)
            .flatMap(emojiRepository::save)
            .flatMap(body -> ServerResponse.status(CREATED).build())
            .onErrorResume(body -> ServerResponse.status(INTERNAL_SERVER_ERROR).syncBody(body.getMessage()));
    }

    public Mono<ServerResponse> listRecentsWithOk(ServerRequest request) {
        var secondsString = request.queryParam("seconds").orElse("0");
        var seconds = Long.valueOf(secondsString);
        log.info("Listing emoji usage of the last {} seconds", seconds);

        return emojiRepository.findUsageWhereUsedAtAfter(seconds)
            .log("ss")
            .flatMap(body -> ServerResponse.ok().syncBody(body));
    }

    public Mono<ServerResponse> getByCodeWithOk(ServerRequest request) {
        var code = request.pathVariable("code");

        log.debug("Getting emoji with code " + code);
        return emojiRepository.findByCode(code)
            .flatMap(body -> ServerResponse.ok().syncBody(body))
            .onErrorResume(body -> ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> deleteWithNoContent(ServerRequest request) {
        var code = request.pathVariable("code");

        log.info("Delete by code " + code);
        return emojiRepository.deleteByCode(code)
            .flatMap(body -> ServerResponse.noContent().build())
            .onErrorResume(body -> ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> saveAtDateWithCreated(ServerRequest request) {
        var code = request.pathVariable("code");
        var usedAt = request.queryParam("usedAt").orElse(Instant.now().toString());
        var instant = Instant.parse(usedAt);

        log.info("Save with code {} at date {}", code, instant);
        return emojiRepository.saveUsage(code, instant)
            .log("xx")
            .flatMap(body -> ServerResponse.status(CREATED).build())
            .onErrorResume(body -> ServerResponse.status(INTERNAL_SERVER_ERROR).syncBody(body.getMessage()));
    }

}
