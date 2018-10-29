package reactivestack.bootstrap;

import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import lombok.Getter;

@Getter
public class AppSystem {

    private final Config config;
    private final ActorSystem system;
    private final ActorMaterializer materializer;

    public AppSystem(final String configName) {
        this.config = ConfigFactory.load().getConfig(configName);
        this.system = ActorSystem.create(configName, config);
        this.materializer = ActorMaterializer.create(system);
    }

}
