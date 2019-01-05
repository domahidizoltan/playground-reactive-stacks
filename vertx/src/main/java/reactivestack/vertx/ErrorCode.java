package reactivestack.vertx;

import lombok.Getter;

@Getter
public enum ErrorCode {
    NO_DB_ACTION("No DB action header"),
    INVALID_DB_ACTION("Invalid DB action"),
    FAILED_DB_ACTION("DB action failed");

    private String message;

    ErrorCode(String message) {
        this.message = message;
    }
}
