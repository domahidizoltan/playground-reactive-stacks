package reactivestack.bootstrap;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpServer;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import reactivestack.router.EmojiHandler;

import static reactivestack.util.VertxHelper.logCompletion;

public class ServerVerticle extends AbstractVerticle {

    private final static Logger LOG = LoggerFactory.getLogger(ServerVerticle.class);

    private final AppSystem system;
    private final EmojiHandler emojiHandler;
    private HttpServer server;

    public ServerVerticle(AppSystem system) {
        this.system = system;
        emojiHandler = new EmojiHandler();
    }

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        var port = system.getConfig().getInteger("http.port");
        server = system.getVertx()
            .createHttpServer()
            .requestHandler(createRouter())
            .listen(port, asyncResult -> logCompletion(asyncResult, startFuture, "Application is listening on port " + port));
    }

    @Override
    public void stop(Future<Void> stopFuture) throws Exception {
        LOG.info("Shutting down server");
        server.close(result -> logCompletion(result, stopFuture, "Shutting down server"));
    }


    private Router createRouter() {
        return Router.router(system.getVertx())
            .mountSubRouter("/emojis", emojisRouter())
            .mountSubRouter("/recent", recentEmojisRouter());
    }

    private Router recentEmojisRouter() {
        var router = Router.router(vertx);
        router.get().handler(emojiHandler::listRecentsWithOk);
        return router;
    }

    private Router emojisRouter() {
        var router = Router.router(vertx);
        router.get("/:code").handler(emojiHandler::getByCodeWithOk);
        router.delete("/:code").handler(emojiHandler::deleteWithNoContent);
        router.post("/:code").handler(emojiHandler::saveAtDateWithCreated);

        router.get().handler(emojiHandler::listWithOk);
        router.post().handler(emojiHandler::createWithCreated);
        return router;
    }

}
