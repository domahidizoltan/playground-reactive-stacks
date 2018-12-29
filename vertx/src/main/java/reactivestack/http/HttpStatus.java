package reactivestack.http;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum HttpStatus {
    OK(200),
    CREATED(201),
    NO_CONTENT(204);

    private final int code;
}
