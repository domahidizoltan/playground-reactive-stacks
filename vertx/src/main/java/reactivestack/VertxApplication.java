package reactivestack;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import reactivestack.router.EmojiRoutes;
import reactivestack.bootstrap.AppSystem;

public class VertxApplication {

    private final static Logger LOG = LoggerFactory.getLogger(VertxApplication.class);


    public static void main(String[] args) {
        var system = new AppSystem();
        var router = new EmojiRoutes(system.getVertx());

        var serverHandler = handleServerCreate(system, router);
        system.initWith(serverHandler);
    }

    private static Handler<AsyncResult<Void>> handleServerCreate(AppSystem system, EmojiRoutes router) {
        return result -> {
            if (result.succeeded()) {
                var port = system.getConfig().getInteger("http.port");
                system.getVertx()
                    .createHttpServer()
                    .requestHandler(router)
                    .listen(port);
                LOG.info("Application is listening on port " + port);
            }
        };
    }

}
