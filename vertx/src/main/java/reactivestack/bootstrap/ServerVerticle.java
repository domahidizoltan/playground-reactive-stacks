package reactivestack.bootstrap;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import reactivestack.router.EmojiHandler;

import static reactivestack.util.VertxHelper.logCompletion;

public class ServerVerticle extends AbstractVerticle {

    private final JsonObject config;
    private final EmojiHandler emojiHandler;
    private HttpServer server;

    public ServerVerticle(JsonObject config) {
        this.config = config;
        emojiHandler = new EmojiHandler();
    }

    @Override
    public void start(Future<Void> startFuture) {
        var port = config.getInteger("port");
        var router = Router.router(vertx)
            .mountSubRouter("/emojis", emojisRouter());
        server = vertx.createHttpServer()
            .requestHandler(router)
            .listen(port, asyncResult -> logCompletion(asyncResult, startFuture, "Application is listening on port " + port));
    }

    @Override
    public void stop(Future<Void> stopFuture) {
        server.close(result -> logCompletion(result, stopFuture, "Shutting down server"));
    }

    private Router emojisRouter() {
        var router = Router.router(vertx);
        router.get("/recent").handler(emojiHandler::listRecentsWithOk);
        router.get("/:code").handler(emojiHandler::getByCodeWithOk);
        router.delete("/:code").handler(emojiHandler::deleteWithNoContent);
        router.post("/:code").handler(emojiHandler::saveAtDateWithCreated);

        router.get().handler(emojiHandler::listWithOk);
        router.post().handler(emojiHandler::createWithCreated);
        return router;
    }

}
