package reactivestack;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import reactivestack.bootstrap.AppSystem;
import reactivestack.bootstrap.ServerVerticle;

public class VertxApplication {

    private final static Logger LOG = LoggerFactory.getLogger(VertxApplication.class);


    public static void main(String[] args) {
        var system = new AppSystem();
        system.loadConfig().setHandler(result -> {
            if (result.succeeded()) {
                system.getVertx().deployVerticle(new ServerVerticle(system));
            } else {
                LOG.error("Could not load config");
            }
        });
    }

}
