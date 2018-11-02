package reactivestack;

import reactivestack.bootstrap.AppSystem;
import reactivestack.bootstrap.Server;
import reactivestack.controller.EmojiRoutes;

import java.io.IOException;

public class AkkaApplication {

    public static void main(String[] args) throws IOException {
        var appSystem = new AppSystem("app");
        var routes = new EmojiRoutes(appSystem.getSystem());
        var app = new Server(appSystem, routes);

        app.serve();
        System.in.read();
        app.terminate();
    }

}
