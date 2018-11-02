package reactivestack.model;

import lombok.Value;

import java.time.Instant;

@Value
public class EmojiUsage {
    private String code;
    private Instant usedAt;
}
