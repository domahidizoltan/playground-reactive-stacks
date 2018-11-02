package reactivestack.bootstrap;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.support.GenericApplicationContext;
import reactivestack.handler.EmojiHandler;

public class Initializer implements ApplicationContextInitializer<GenericApplicationContext> {

    @Override
    public void initialize(GenericApplicationContext ctx) {
        ctx.registerBean(EmojiHandler.class);
    }

}
