package reactivestack.bootstrap;

import akka.actor.ActorSystem;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.server.AllDirectives;
import akka.stream.ActorMaterializer;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.util.concurrent.CompletionStage;

public class Server extends AllDirectives {

    private final Config config;
    private final ActorSystem system;
    private final ActorMaterializer materializer;
    private final Routes routes;
    private final LoggingAdapter log;
    private CompletionStage<ServerBinding> binding;

    public Server(final String configName, final Routes routes) {
        this.config = ConfigFactory.load().getConfig(configName);
        this.system = ActorSystem.create(configName, config);
        this.materializer = ActorMaterializer.create(system);
        this.routes = routes;

        this.log = Logging.getLogger(system, this);
        log.info("Creating server...");
    }

    public void serve() {
        if (binding == null) {
            bindRoutes();
        }
    }

    public void terminate() {
        log.info("Shutting down server");
        binding.thenCompose(ServerBinding::unbind)
            .thenAccept(unbound -> system.terminate());
    }

    private void bindRoutes() {
        var http = Http.get(system);
        var routeFlow = routes.createRoute().flow(system, materializer);
        ConnectHttp connect = ConnectHttp.toHost(config.getString("interface"), config.getInt("port"));
        this.binding = http.bindAndHandle(routeFlow, connect, materializer);
        log.info("Serving requests on port " + connect.port());
    }

}
