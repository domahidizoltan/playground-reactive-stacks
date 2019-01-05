package reactivestack.bootstrap;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.PostgreSQLClient;
import io.vertx.ext.sql.SQLClient;
import reactivestack.model.Emoji;
import reactivestack.model.EmojiUsage;
import reactivestack.repository.EmojiRepository;
import reactivestack.repository.RepoAction;
import reactivestack.vertx.ErrorCode;
import reactivestack.vertx.MessageType;
import reactivestack.vertx.codec.EmojiCodec;
import reactivestack.vertx.codec.EmojiListCodec;
import reactivestack.vertx.codec.EmojiUsageListCodec;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static reactivestack.util.VertxHelper.debugCompletion;
import static reactivestack.vertx.ErrorCode.FAILED_DB_ACTION;
import static reactivestack.vertx.ErrorCode.NO_DB_ACTION;
import static reactivestack.vertx.RequestParameters.*;

public class DatabaseVerticle extends AbstractVerticle {

    public static final String DB_QUEUE_NAME = "emoji-db-queue";

    private final JsonObject config;
    private SQLClient client;
    private EmojiRepository emojiRepository;

    public DatabaseVerticle(JsonObject config) {
        this.config = config;
    }

    @Override
    public void start(Future<Void> startFuture) {
        vertx.eventBus()
            .registerCodec(new EmojiCodec())
            .registerCodec(new EmojiListCodec())
            .registerCodec(new EmojiUsageListCodec());

        client = PostgreSQLClient.createShared(vertx, config);
        client.getConnection(result -> {
            if (result.succeeded()) {
                emojiRepository = new EmojiRepository(result.result());
                vertx.eventBus().consumer(DB_QUEUE_NAME, this::onMessage);
            }
            debugCompletion(result, startFuture, "Open DB connection");
        });
    }

    @Override
    public void stop(Future<Void> stopFuture) {
        client.close(result -> debugCompletion(result, stopFuture, "Close DB client"));
        vertx.eventBus().unregisterCodec(EmojiCodec.NAME);
        vertx.eventBus().unregisterCodec(EmojiListCodec.NAME);
    }

    private void onMessage(Message<JsonObject> message) {
        getAction(message)
            .ifPresentOrElse(action -> getRepoResult(action, message.body())
                    .setHandler(res -> reply(message, res)),
            () -> failMessage(message, NO_DB_ACTION));
    }

    private Future<?> getRepoResult(String action, JsonObject body) {
        var actionType = RepoAction.valueOf(action);
        Future<?> response = Future.future();
        switch (actionType) {
            case FIND_ALL:
                response = emojiRepository.findAll();
                break;
            case FIND_BY_CODE:
                var findCode = parseCode(body);
                response = emojiRepository.findByCode(findCode);
                break;
            case FIND_USAGE:
                long seconds = parseSeconds(body);
                response = emojiRepository.findUsageWhereUsedAtAfter(seconds);
                break;
            case DELETE_BY_CODE:
                var deleteCode = parseCode(body);
                response = emojiRepository.deleteByCode(deleteCode);
                break;
            case SAVE:
                var emoji = body.mapTo(Emoji.class);
                response = emojiRepository.save(emoji);
                break;
            case SAVE_USAGE:
                var usageCode = parseCode(body);
                var usedAt = parseUsedAt(body);
                response = emojiRepository.saveUsage(usageCode, usedAt);
                break;
            default:
                response.fail(ErrorCode.INVALID_DB_ACTION.name());
        }
        return response;
    }

    private Optional<String> getAction(Message<JsonObject> message) {
        var actionName = MessageType.DB_ACTION.name();
        var actionHeader = message.headers().get(actionName);
        return Optional.ofNullable(actionHeader);
    }

    private void reply(Message<JsonObject> message, AsyncResult<?> res) {
        if (res.succeeded()) {
            var result = res.result();
            var options = createCodecOptions(result);
            message.reply(result, options);
        } else {
            message.fail(FAILED_DB_ACTION.ordinal(), res.cause().getMessage());
        }
    }

    private DeliveryOptions createCodecOptions(Object result) {
        var options = new DeliveryOptions();
        if (isEmoji(result)) {
            options.setCodecName(EmojiCodec.NAME);
        } else if (isEmojiList(result)) {
            options.setCodecName(EmojiListCodec.NAME);
        } else if (isEmojiUsageLst(result)) {
            options.setCodecName(EmojiUsageListCodec.NAME);
        }
        return options;
    }

    private boolean isEmoji(Object result) {
        return result instanceof Emoji;
    }

    private boolean isEmojiList(Object result) {
        return result instanceof List && ((List) result).get(0) instanceof Emoji;
    }

    private boolean isEmojiUsageLst(Object result) {
        return result instanceof List && ((List) result).get(0) instanceof EmojiUsage;
    }

    private void failMessage(Message<JsonObject> message, ErrorCode errorCode) {
        message.fail(errorCode.ordinal(), errorCode.getMessage());
    }

    private String parseCode(JsonObject body) {
        return body.getString(CODE.getName());
    }

    private Long parseSeconds(JsonObject body) {
        return body.getLong(SECONDS.getName());
    }

    private Instant parseUsedAt(JsonObject body) {
        var usedAt = body.getString(USED_AT.getName());
        return Instant.parse(usedAt);
    }

}
