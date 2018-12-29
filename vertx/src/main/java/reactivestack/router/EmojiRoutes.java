package reactivestack.router;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.impl.RouterImpl;
import reactivestack.http.HttpHeader;
import reactivestack.http.HttpStatus;
import reactivestack.model.Emoji;
import reactivestack.model.EmojiUsage;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static reactivestack.model.CategoryType.SMILEYS_AND_EMOTION;

public class EmojiRoutes extends RouterImpl {

    private final static Logger LOG = LoggerFactory.getLogger(EmojiRoutes.class);
    private final static HttpHeader APPLICATION_JSON = HttpHeader.APPLICATION_JSON;


    private final Vertx vertx;

    public EmojiRoutes(final Vertx vertx) {
        super(vertx);
        this.vertx = vertx;

        this.mountSubRouter("/emojis", emojisRouter())
            .mountSubRouter("/recent", recentEmojisRouter());
    }

    private Router recentEmojisRouter() {
        var router = Router.router(vertx);
        router.get().handler(this::listRecentsWithOk);
        return router;
    }

    private Router emojisRouter() {
        var router = Router.router(vertx);
        router.get("/:code").handler(this::getByCodeWithOk);
        router.delete("/:code").handler(this::deleteWithNoContent);
        router.post("/:code").handler(this::saveAtDateWithCreated);

        router.get().handler(this::listWithOk);
        router.post().handler(this::createWithCreated);
        return router;
    }

    private void listWithOk(RoutingContext ctx) {
        LOG.debug("Listing all emojis");

        var e1 = new Emoji("1", SMILEYS_AND_EMOTION, "xxx", 1);
        var e2 = new Emoji("2", SMILEYS_AND_EMOTION, "yyy", 2);
        var response = List.of(e1, e2);

        respondWith(ctx, HttpStatus.OK, response);
    }

    private void createWithCreated(RoutingContext ctx) {
        LOG.info("Creating emoji");
        respondWith(ctx, HttpStatus.CREATED);
    }

    private void listRecentsWithOk(RoutingContext ctx) {
        var seconds = getParam(ctx, "seconds");
        LOG.info("Listing emoji usage of the last {0} seconds", seconds);

        var u1 = new EmojiUsage("1", Instant.now());
        var u2 = new EmojiUsage("1", Instant.now().minus(1, ChronoUnit.HOURS));
        var response = List.of(u1, u2);

        respondWith(ctx, HttpStatus.OK, response);
    }

    private void getByCodeWithOk(RoutingContext ctx) {
        var code = getParam(ctx, "code");
        LOG.debug("Getting emoji with code " + code);
        var emoji = new Emoji(code, SMILEYS_AND_EMOTION, "xxx", 1);

        respondWith(ctx, HttpStatus.OK, emoji);
    }

    private void deleteWithNoContent(RoutingContext ctx) {
        var code = getParam(ctx, "code");
        LOG.debug("Delete by code " + code);
        respondWith(ctx, HttpStatus.NO_CONTENT);
    }

    private void saveAtDateWithCreated(RoutingContext ctx) {
        var code = getParam(ctx, "code");
        var usedAt = getParam(ctx, "usedAt");
        var instant = Instant.parse(usedAt);
        LOG.info("Save with code {0} at date {1}", code, instant);

        respondWith(ctx, HttpStatus.CREATED);
    }

    private void respondWith(RoutingContext ctx, HttpStatus status) {
        createResponse(ctx, status).end();
    }

    private void respondWith(RoutingContext ctx, HttpStatus status, Object payload) {
        createResponse(ctx, status).end(Json.encode(payload));
    }


    private HttpServerResponse createResponse(RoutingContext ctx, HttpStatus status) {
        return ctx.response()
            .setStatusCode(status.getCode())
            .putHeader(APPLICATION_JSON.getKey(), APPLICATION_JSON.getValue());
    }

    private String getParam(RoutingContext ctx, String key) {
        return ctx.request().getParam(key);
    }

}
