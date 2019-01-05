package reactivestack.vertx.codec;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import reactivestack.model.Emoji;

public class EmojiCodec implements MessageCodec<Emoji, Emoji> {

    public static final String NAME = EmojiCodec.class.getSimpleName();

    @Override
    public void encodeToWire(Buffer buffer, Emoji emoji) { }

    @Override
    public Emoji decodeFromWire(int pos, Buffer buffer) {
        return null;
    }

    @Override
    public Emoji transform(Emoji emoji) {
        return emoji.toBuilder().build();
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
