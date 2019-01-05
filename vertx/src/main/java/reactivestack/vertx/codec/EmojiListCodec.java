package reactivestack.vertx.codec;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import reactivestack.model.Emoji;

import java.util.List;

public class EmojiListCodec implements MessageCodec<List<Emoji>, List<Emoji>> {

    public static final String NAME = EmojiListCodec.class.getSimpleName();

    @Override
    public void encodeToWire(Buffer buffer, List<Emoji> emojis) { }

    @Override
    public List<Emoji> decodeFromWire(int pos, Buffer buffer) {
        return null;
    }

    @Override
    public List<Emoji> transform(List<Emoji> emojis) {
        return List.copyOf(emojis);
    }

    @Override
    public String name() {
        return NAME;
    }

    @Override
    public byte systemCodecID() {
        return -1;
    }
}
