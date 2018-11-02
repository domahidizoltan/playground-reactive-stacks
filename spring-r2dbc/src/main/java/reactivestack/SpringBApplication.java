package reactivestack;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import reactivestack.bootstrap.Initializer;

@SpringBootApplication
public class SpringBApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder()
            .sources(SpringBApplication.class)
            .initializers(new Initializer())
            .run(args);
    }

}
