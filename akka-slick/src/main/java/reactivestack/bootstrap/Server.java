package reactivestack.bootstrap;

import akka.actor.ActorSystem;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.server.AllDirectives;
import akka.stream.ActorMaterializer;
import akka.stream.alpakka.slick.javadsl.SlickSession;
import akka.util.Timeout;
import com.typesafe.config.Config;
import reactivestack.controller.EmojiRoutes;
import scala.concurrent.duration.FiniteDuration;

import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

public class Server extends AllDirectives {

    public static final SlickSession DB_SESSION = SlickSession.forConfig("slick-postgres");
    public static final Timeout TIMEOUT = Timeout.durationToTimeout(FiniteDuration.apply(5, TimeUnit.SECONDS));

    private final Config config;
    private final ActorSystem system;
    private final ActorMaterializer materializer;
    private final EmojiRoutes routes;
    private final LoggingAdapter log;
    private CompletionStage<ServerBinding> binding;

    public Server(final AppSystem appSystem, final EmojiRoutes routes) {
        this.config = appSystem.getConfig();
        this.system = appSystem.getSystem();
        this.materializer = appSystem.getMaterializer();
        this.routes = routes;

        this.log = Logging.getLogger(system, this);
        log.info("Creating server...");
    }

    public void serve() {
        if (binding == null) {
            bindRoutes();
        }
        system.registerOnTermination(DB_SESSION::close);
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
