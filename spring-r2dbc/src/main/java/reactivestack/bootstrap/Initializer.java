package reactivestack.bootstrap;

import io.r2dbc.client.R2dbc;
import io.r2dbc.postgresql.PostgresqlConnectionConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionFactory;
import io.r2dbc.spi.ConnectionFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import reactivestack.handler.EmojiHandler;
import reactivestack.repository.EmojiRepository;

public class Initializer implements ApplicationContextInitializer<GenericApplicationContext> {

    private static final String DB = "db.";

    @Override
    public void initialize(final GenericApplicationContext ctx) {
        ctx.registerBean(PostgresqlConnectionConfiguration.class, () -> connectionConfiguration(ctx));
        ctx.registerBean(PostgresqlConnectionFactory.class);
        ctx.registerBean(R2dbc.class);
        ctx.registerBean(EmojiRepository.class);
        ctx.registerBean(EmojiHandler.class);
    }

    private PostgresqlConnectionConfiguration connectionConfiguration(final GenericApplicationContext ctx) {
        return PostgresqlConnectionConfiguration.builder()
            .host(getProperty(ctx, DB + "host"))
            .port(Integer.valueOf(getProperty(ctx, DB + "port")))
            .database(getProperty(ctx, DB + "database"))
            .username(getProperty(ctx, DB + "username"))
            .password(getProperty(ctx, DB + "password"))
            .build();
    }

    private ConnectionFactory connectionFactory(final PostgresqlConnectionConfiguration connectionConfiguration) {
        return new PostgresqlConnectionFactory(connectionConfiguration);
    }

    private R2dbc r2dbc(final ConnectionFactory connectionFactory) {
        return new R2dbc(connectionFactory);
    }

    private String getProperty(final GenericApplicationContext ctx, final String key) {
        return ctx.getEnvironment().getProperty(key);
    }

}
