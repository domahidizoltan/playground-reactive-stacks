package reactivestack.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Value;
import reactivestack.serde.InstantSerializer;

import java.time.Instant;

@Value
@AllArgsConstructor
public class EmojiUsage {
    private String code;

    @JsonSerialize(using = InstantSerializer.class)
    @JsonDeserialize(using = InstantDeserializer.class)
    private Instant usedAt;
}
