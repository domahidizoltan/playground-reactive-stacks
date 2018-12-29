package reactivestack.http;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum HttpHeader {
    APPLICATION_JSON("Content-Type", "application/json");

    private final String key;
    private final String value;
}
