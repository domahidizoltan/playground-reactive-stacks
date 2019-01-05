package reactivestack.vertx.codec;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.MessageCodec;
import reactivestack.model.EmojiUsage;

import java.util.List;

public class EmojiUsageListCodec implements MessageCodec<List<EmojiUsage>, List<EmojiUsage>> {

    public static final String NAME = EmojiUsageListCodec.class.getSimpleName();

    @Override
    public void encodeToWire(Buffer buffer, List<EmojiUsage> emojis) { }

    @Override
    public List<EmojiUsage> decodeFromWire(int pos, Buffer buffer) {
        return null;
    }

    @Override
    public List<EmojiUsage> transform(List<EmojiUsage> emojis) {
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
