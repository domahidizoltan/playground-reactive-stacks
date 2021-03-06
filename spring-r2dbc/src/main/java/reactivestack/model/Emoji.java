package reactivestack.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

@Value
public class Emoji {
    private final String code;
    private final CategoryType category;
    private final String name;
    private final long usageCount;

    @JsonCreator
    public Emoji(
        @JsonProperty("code") String code,
        @JsonProperty("category") CategoryType category,
        @JsonProperty("name") String name,
        @JsonProperty("usageCount") long usageCount
    ) {
        this.code = code;
        this.category = category;
        this.name = name;
        this.usageCount = usageCount;
    }
}
