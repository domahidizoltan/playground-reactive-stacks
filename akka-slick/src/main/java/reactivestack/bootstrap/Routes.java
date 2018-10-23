package reactivestack.bootstrap;

import akka.http.javadsl.marshallers.jackson.Jackson;
import akka.http.javadsl.marshalling.Marshaller;
import akka.http.javadsl.model.RequestEntity;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.server.PathMatchers;
import akka.http.javadsl.server.Route;
import akka.http.javadsl.unmarshalling.StringUnmarshallers;

import static akka.http.javadsl.server.Directives.*;

public class Routes {

    private final Marshaller<String, RequestEntity> mapper = Jackson.marshaller();

    public Route createRoute() {
        return route(
            pathPrefix("emojis", () -> route(
                recentSubpath(),
                codeSubpath(),

                get(() -> completeOK("list", mapper)),
                post(() -> complete(StatusCodes.CREATED, "create"))
            ))
        );
    }

    private Route recentSubpath() {
        return path("recent", () ->
            parameter(StringUnmarshallers.INTEGER, "seconds", seconds ->
                get(() -> completeOK("query recent of " + seconds, mapper)))
        );
    }

    private Route codeSubpath() {
        return path(PathMatchers.segment(), code -> route(
            get(() -> completeOK("get with code " + code, mapper)),
            delete(() -> complete(StatusCodes.NO_CONTENT)),

            parameter(StringUnmarshallers.STRING, "usedAt", usedAt ->
                post(() -> complete(StatusCodes.CREATED, "post usedAt with code " + code + " at time " + usedAt))
            )
        ));
    }

}
