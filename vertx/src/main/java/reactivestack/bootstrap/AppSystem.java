package reactivestack.bootstrap;

import io.vertx.config.ConfigRetriever;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.util.Optional;

public class AppSystem {

    private final static Logger LOG = LoggerFactory.getLogger(AppSystem.class);

    private final Vertx vertx;
    private Optional<JsonObject> config = Optional.of(new JsonObject());

    public AppSystem() {
        this.vertx = Vertx.vertx();
    }

    public void initWith(Handler<AsyncResult<Void>> step) {
        loadConfig().setHandler(step);
    }

    public Vertx getVertx() {
        return this.vertx;
    }

    public JsonObject getConfig() {
        return config.get();
    }

    private Future<Void> loadConfig() {
        Future<Void> loaded = Future.future();
        var configRetriever = ConfigRetriever.create(vertx);
        configRetriever.getConfig(handleConfigLoad(loaded));
        return loaded;
    }

    private Handler<AsyncResult<JsonObject>> handleConfigLoad(Future<Void> loaded) {
        return result -> {
            if (result.succeeded()) {
                this.config = Optional.of(result.result());
                loaded.complete();
            } else {
                LOG.error("Could not retrieve default configuration");
                loaded.failed();
            }
        };
    }
}
