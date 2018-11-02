package reactivestack.bootstrap;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import reactivestack.handler.EmojiHandler;

import static org.springframework.web.reactive.function.server.RequestPredicates.path;
import static org.springframework.web.reactive.function.server.RouterFunctions.nest;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class Config {

    @Bean
    public RouterFunction router(final EmojiHandler handler) {
        return nest(path("/emojis"), route()
                .GET("", handler::listWithOK)
                .POST("", handler::createWithCreated)
                .GET("/recent", handler::listRecentsWithOk)
                .nest(path("/{code}"), () -> route()
                    .GET("", handler::getByCodeWithOk)
                    .DELETE("", handler::deleteWithNoContent)
                    .POST("", handler::saveAtDateWithCreated)
                    .build())
                .build()
            );
    }

}
