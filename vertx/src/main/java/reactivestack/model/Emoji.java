package reactivestack.model;

import lombok.Value;

@Value
public class Emoji {
    private final String code;
    private final CategoryType category;
    private final String name;
    private final long usageCount;
}
