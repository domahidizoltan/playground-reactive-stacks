package reactivestack.repository;

import com.github.mauricio.async.db.postgresql.exceptions.GenericDatabaseException;
import com.github.mauricio.async.db.postgresql.messages.backend.ErrorMessage;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.sql.UpdateResult;
import reactivestack.model.Emoji;
import reactivestack.model.EmojiUsage;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import static reactivestack.model.EmojiUsage.DATE_FORMAT;

public class EmojiRepository {

    private final SQLClient client;

    public EmojiRepository(SQLClient client) {
        this.client = client;
    }

    public Future<List<Emoji>> findAll() {
        var operationResult = Future.<List<Emoji>>future();
        client.getConnection(connection -> {
            if (connection.succeeded()) {
                var conn = connection.result();
                conn.query("SELECT * FROM emoji ORDER BY usage_count desc",
                    result -> multipleRecordHandler(result, operationResult, conn));
            }
        });
        return operationResult;
    }

    public Future<Emoji> findByCode(final String code) {
        var operationResult = Future.<Emoji>future();
        var params = new JsonArray(List.of(code));
        client.getConnection(connection -> {
            if (connection.succeeded()) {
                var conn = connection.result();
                conn.queryWithParams("SELECT * FROM emoji WHERE code = ?", params,
                    result -> singleRecordHandler(result, operationResult, code, conn));
            }
        });
        return operationResult;
    }


    public Future<List<EmojiUsage>> findUsageWhereUsedAtAfter(final long seconds) {
        var operationResult = Future.<List<EmojiUsage>>future();
        var params = new JsonArray(List.of(seconds));
        client.getConnection(connection -> {
            if (connection.succeeded()) {
                var conn = connection.result();
                conn.queryWithParams("SELECT * FROM emoji_usage WHERE used_at::timestamptz>=NOW() - INTERVAL '? SECOND' " +
                    "order by used_at desc", params,
                        result -> multipleEmojiUsageRecordHandler(result, operationResult, conn));
            }
        });
        return operationResult;
    }

    public Future<Integer> save(final Emoji emoji) {
        var operationResult = Future.<Integer>future();
        var params = new JsonArray(List.of(emoji.getCode(), emoji.getCategory().name(), emoji.getName()));
        client.getConnection(connection -> {
            if (connection.succeeded()) {
                var conn = connection.result();
                conn.updateWithParams("INSERT INTO emoji(code, category, name) VALUES(?, ?, ?)", params,
                    result -> updateRecordHandler(result, operationResult, conn));
            }
        });
        return operationResult;
    }

    public Future<Integer> saveUsage(final String code, final Instant usedAt) {
        var insertUsageResult = Future.<Integer>future();
        var insertParams = new JsonArray(List.of(code, DATE_FORMAT.format(usedAt)));
        client.getConnection(connection -> {
                if (connection.succeeded()) {
                    var conn = connection.result();
                    conn.updateWithParams("INSERT INTO emoji_usage(code, used_at) VALUES(?, ?)", insertParams,
                        result -> updateRecordHandler(result, insertUsageResult, conn));
                }
            });

        var updateEmojiResult = Future.<Integer>future();
        var updateParams = new JsonArray(List.of(code, code));
        insertUsageResult.setHandler(insertResult -> {
            if (insertResult.succeeded()) {
                client.getConnection(connection -> {
                    if (connection.succeeded()) {
                        var conn = connection.result();
                        conn.updateWithParams("UPDATE emoji SET usage_count=(SELECT usage_count+1 FROM emoji WHERE code=?) WHERE code=?", updateParams,
                            result -> updateRecordHandler(result, updateEmojiResult, conn));
                    }
                });
            } else {
                updateEmojiResult.fail(insertResult.cause().getMessage());
            }
        });

        return updateEmojiResult;
    }

    public Future<Integer> deleteByCode(final String code) {
        var operationResult = Future.<Integer>future();
        var params = new JsonArray(List.of(code));
        client.getConnection(connection -> {
            if (connection.succeeded()) {
                var conn = connection.result();
                conn.updateWithParams("DELETE FROM emoji WHERE code = ?", params,
                    result -> updateRecordHandler(result, operationResult, conn));
            }
        });
        return operationResult;
    }

    private void multipleRecordHandler(AsyncResult<ResultSet> asyncResult, Future<List<Emoji>> operationResult, SQLConnection connection) {
        var emojis = asyncResult
            .result()
            .getRows()
            .stream()
            .map(json -> json.mapTo(Emoji.class))
            .collect(Collectors.toList());
        connection.close();
        operationResult.complete(emojis);
    }

    private void singleRecordHandler(AsyncResult<ResultSet> asyncResult, Future<Emoji> operationResult, String code, SQLConnection connection) {
        var rows = asyncResult.result().getRows();

        connection.close();
        if (rows.isEmpty()) {
            operationResult.fail(new NoSuchElementException("Emoji not found with code " + code));
        } else {
            var emoji = rows.get(0).mapTo(Emoji.class);
            operationResult.complete(emoji);
        }
    }

    private void updateRecordHandler(AsyncResult<UpdateResult> asyncResult, Future<Integer> operationResult, SQLConnection connection) {
        connection.close();
        if (asyncResult.succeeded()) {
            var rowsUpdated = asyncResult.result().getUpdated();
            operationResult.complete(rowsUpdated);
        } else {
            failOperation(asyncResult, operationResult);
        }
    }

    private void failOperation(AsyncResult<UpdateResult> asyncResult, Future<Integer> operationResult) {
        var cause = asyncResult.cause();
        if (cause instanceof GenericDatabaseException) {
            ErrorMessage errorMessage = ((GenericDatabaseException) cause).errorMessage();
            operationResult.fail(errorMessage.message());
        } else {
            operationResult.fail(asyncResult.cause().getMessage());
        }
    }

    private void multipleEmojiUsageRecordHandler(AsyncResult<ResultSet> asyncResult, Future<List<EmojiUsage>> operationResult, SQLConnection connection) {
        var usages = asyncResult
            .result()
            .getRows()
            .stream()
            .map(this::toEmojiUsage)
            .collect(Collectors.toList());
        connection.close();
        operationResult.complete(usages);
    }

    private EmojiUsage toEmojiUsage(JsonObject json) {
        var used_at = json.getString("used_at");
        var usedAt = DATE_FORMAT.parse(used_at);
        return new EmojiUsage(json.getString("code"), Instant.from(usedAt));
    }
}
