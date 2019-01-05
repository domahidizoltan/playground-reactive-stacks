package reactivestack;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import reactivestack.bootstrap.AppConfig;
import reactivestack.bootstrap.DatabaseVerticle;
import reactivestack.bootstrap.ServerVerticle;

import static reactivestack.util.VertxHelper.logCompletion;

public class VertxApplication {

    private final static Logger LOG = LoggerFactory.getLogger(VertxApplication.class);
    private static final Vertx vertx = Vertx.vertx();


    public static void main(String[] args) {
        var config = new AppConfig();
        config.loadConfig()
            .compose(result -> startDatabase(config))
            .setHandler(result -> startServer(result, config));
    }

    private static Future<Object> startDatabase(AppConfig config) {
        var dbVerticleDeployment = Future.future();
        var dbConfig = config.getConfig().getJsonObject("db");
        vertx.deployVerticle(new DatabaseVerticle(dbConfig), result ->
            logCompletion(result, dbVerticleDeployment, "Created DB connection"));
        return dbVerticleDeployment;
    }

    private static void startServer(AsyncResult<?> result, AppConfig config) {
        if (result.succeeded()) {
            var serverConfig = config.getConfig().getJsonObject("server");
            vertx.deployVerticle(new ServerVerticle(serverConfig));
        } else {
            LOG.error("Could not load Database", result.cause());
        }
    }

}
