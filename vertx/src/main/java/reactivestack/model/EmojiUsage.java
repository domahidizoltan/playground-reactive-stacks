package reactivestack.model;

import lombok.Value;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Value
public class EmojiUsage {
    public final static DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
        .withZone(ZoneId.systemDefault());

    private final String code;
    private final Instant usedAt;
}
