package reactivestack.bootstrap;

import io.vertx.config.ConfigRetriever;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import reactivestack.util.VertxHelper;

import java.util.Optional;

public class AppConfig {

    private final Vertx vertx;
    private Optional<JsonObject> config = Optional.of(new JsonObject());

    public AppConfig() {
        this.vertx = Vertx.vertx();
    }

    public Vertx getVertx() {
        return this.vertx;
    }

    public JsonObject getConfig() {
        return config.get();
    }

    public Future<Void> loadConfig() {
        Future<Void> loaded = Future.future();
        var configRetriever = ConfigRetriever.create(vertx);
        configRetriever.getConfig(handleConfigLoad(loaded));
        return loaded;
    }

    private Handler<AsyncResult<JsonObject>> handleConfigLoad(Future<Void> loaded) {
        return result -> {
            if (result.succeeded()) {
                this.config = Optional.of(result.result());
            }
            VertxHelper.debugCompletion(result, loaded, "Retrieving default configuration");
        };
    }
}
