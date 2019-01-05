package reactivestack.vertx;

import lombok.Getter;

@Getter
public enum RequestParameters {
    CODE("code"), SECONDS("seconds"), USED_AT("usedAt");

    private String name;

    RequestParameters(String name) {
        this.name = name;
    }
}
