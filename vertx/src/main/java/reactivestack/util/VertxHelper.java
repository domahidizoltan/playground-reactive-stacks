package reactivestack.util;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

public final class VertxHelper {

    private final static Logger LOG = LoggerFactory.getLogger(VertxHelper.class);


    private VertxHelper() {}

    public static void logCompletion(AsyncResult<?> result, Future<Void> future, String message) {
        if (result.succeeded()) {
            LOG.info(message);
            future.complete();
        } else {
            LOG.error(message + " :: failed");
            future.failed();
        }
    }

    public static void debugCompletion(AsyncResult<?> result, Future<Void> future, String message) {
        if (result.succeeded()) {
            LOG.debug(message);
            future.complete();
        } else {
            LOG.error(message + " :: failed");
            future.failed();
        }
    }

}
