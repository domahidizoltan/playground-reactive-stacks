package reactivestack;

import reactivestack.bootstrap.Routes;
import reactivestack.bootstrap.Server;

import java.io.IOException;

public class AkkaApplication {

    public static void main(String[] args) throws IOException {
        var app = new Server("app", new Routes());
        app.serve();
        System.in.read();
        app.terminate();
    }

}
