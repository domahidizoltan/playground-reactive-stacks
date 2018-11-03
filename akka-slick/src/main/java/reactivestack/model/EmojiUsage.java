package reactivestack.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Value;
import reactivestack.serde.InstantDeserializer;
import reactivestack.serde.InstantSerializer;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Value
@AllArgsConstructor
public class EmojiUsage {

    public final static DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
        .withZone(ZoneId.systemDefault());

    private final String code;

    @JsonSerialize(using = InstantSerializer.class)
    @JsonDeserialize(using = InstantDeserializer.class)
    private final Instant usedAt;
}
