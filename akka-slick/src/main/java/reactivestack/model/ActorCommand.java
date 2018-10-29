package reactivestack.model;

import lombok.Value;

import java.time.Instant;

public class ActorCommand {

    public static class ListAll {}

    @Value
    public static class Create {
        private final Emoji emoji;
    }

    @Value
    public static class GetByCode {
        private final String code;
    }

    @Value
    public static class DeleteByCode {
        private final String code;
    }

    @Value
    public static class StoreAtDate {
        private final String code;
        private final Instant date;
    }

    @Value
    public static class ListRecent {
        private final long seconds;
    }

}
