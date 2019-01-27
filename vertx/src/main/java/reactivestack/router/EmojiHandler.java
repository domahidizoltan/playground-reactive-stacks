package reactivestack.router;

import io.vertx.core.AsyncResult;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.RoutingContext;
import reactivestack.bootstrap.DatabaseVerticle;
import reactivestack.http.HttpHeader;
import reactivestack.http.HttpStatus;
import reactivestack.repository.RepoAction;
import reactivestack.vertx.MessageType;

import java.time.Instant;
import java.util.Map;

import static reactivestack.vertx.RequestParameters.*;

public class EmojiHandler {

    private final static Logger LOG = LoggerFactory.getLogger(EmojiHandler.class);
    private final static HttpHeader APPLICATION_JSON = HttpHeader.APPLICATION_JSON;
    private final static String CODE_PARAM = CODE.getName();
    private final static String SECONDS_PARAM = SECONDS.getName();
    private final static String USED_AT_PARAM = USED_AT.getName();

    public void listWithOk(RoutingContext ctx) {
        LOG.debug("Listing all emojis");

        var findAllAction = makeAction(RepoAction.FIND_ALL);
        publishActionMessage(ctx, findAllAction, null, HttpStatus.OK);
    }

    public void createWithCreated(RoutingContext ctx) {
        LOG.info("Creating emoji");

        var saveAction = makeAction(RepoAction.SAVE);
        ctx.request().bodyHandler(handler -> {
            var emoji = handler.toJsonObject();
            publishActionMessage(ctx, saveAction, emoji, HttpStatus.CREATED);
        });
    }

    public void listRecentsWithOk(RoutingContext ctx) {
        var seconds = Integer.valueOf(getParam(ctx, "seconds"));
        LOG.info("Listing emoji usage of the last {0} seconds", seconds);

        var findUsageAction = makeAction(RepoAction.FIND_USAGE);
        var params = new JsonObject(Map.of(SECONDS_PARAM, seconds));
        publishActionMessage(ctx, findUsageAction, params, HttpStatus.OK);
    }

    public void getByCodeWithOk(RoutingContext ctx) {
        var code = getParam(ctx, CODE_PARAM);
        LOG.debug("Getting emoji with code " + code);

        var findByCodeAction = makeAction(RepoAction.FIND_BY_CODE);
        var params = new JsonObject(Map.of(CODE_PARAM, code));
        publishActionMessage(ctx, findByCodeAction, params, HttpStatus.OK);
    }

    public void deleteWithNoContent(RoutingContext ctx) {
        var code = getParam(ctx, CODE_PARAM);
        LOG.debug("Delete by code " + code);

        var deleteByCode = makeAction(RepoAction.DELETE_BY_CODE);
        var params = new JsonObject(Map.of(CODE_PARAM, code));
        publishActionMessage(ctx, deleteByCode, params, HttpStatus.NO_CONTENT);
    }

    public void saveAtDateWithCreated(RoutingContext ctx) {
        var code = getParam(ctx, CODE_PARAM);
        var usedAt = getParam(ctx, "usedAt");
        if (usedAt == null) {
            usedAt = Instant.now().toString();
        }
        var instant = Instant.parse(usedAt);
        LOG.info("Save with code {0} at date {1}", code, instant);

        var saveUsageAction = makeAction(RepoAction.SAVE_USAGE);
        var params = new JsonObject(Map.of(CODE_PARAM, code, USED_AT_PARAM, usedAt));
        publishActionMessage(ctx, saveUsageAction, params, HttpStatus.CREATED);
    }

    private DeliveryOptions makeAction(RepoAction action) {
        return new DeliveryOptions().addHeader(MessageType.DB_ACTION.name(), action.name());
    }

    private void publishActionMessage(RoutingContext ctx, DeliveryOptions action, JsonObject message, HttpStatus responseStatus) {
        ctx.vertx().eventBus()
            .send(DatabaseVerticle.DB_QUEUE_NAME, message, action, reply ->
                responseHandler(reply, ctx, responseStatus));
    }

    private void responseHandler(AsyncResult<Message<Object>> reply, RoutingContext ctx, HttpStatus status) {
        if (reply.succeeded()) {
            respondWith(ctx, status, reply.result().body());
        } else {
            var message = reply.cause().getMessage();
            LOG.error(message);
            var errorStatus = message.contains("not found")
                ? HttpStatus.NOT_FOUND
                : HttpStatus.INTERNAL_SERVER_ERROR;
            respondWith(ctx, errorStatus, message);
        }
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
