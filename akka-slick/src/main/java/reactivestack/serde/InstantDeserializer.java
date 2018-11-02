package reactivestack.serde;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.Instant;

public class InstantDeserializer extends JsonDeserializer {

    @Override
    public Instant deserialize(JsonParser arg0, DeserializationContext arg1) throws IOException {
        return Instant.parse(arg0.getText());
    }

}
