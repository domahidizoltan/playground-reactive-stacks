package reactivestack.repository;

import io.r2dbc.client.R2dbc;
import io.r2dbc.spi.Row;
import reactivestack.model.CategoryType;
import reactivestack.model.Emoji;
import reactivestack.model.EmojiUsage;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Function;

import static reactivestack.model.EmojiUsage.DATE_FORMAT;

public class EmojiRepository {

    private final static Function<Row, Emoji> TO_EMOJI = row -> new Emoji(
        row.get("code", String.class),
        CategoryType.valueOf(row.get("category", String.class)),
        row.get("name", String.class),
        Long.valueOf(row.get("usage_count", Integer.class)));

    private final static Function<Row, EmojiUsage> TO_EMOJI_USAGE = row -> new EmojiUsage(
        row.get("code", String.class),
        DATE_FORMAT.parse(row.get("used_at", String.class), Instant::from));


    private R2dbc r2dbc;

    public EmojiRepository(final R2dbc r2dbc) {
        this.r2dbc = r2dbc;
    }

    public Mono<List<Emoji>> findAll() {
        return r2dbc.withHandle(handle -> handle
                .select("SELECT * FROM emoji ORDER BY usage_count desc")
                .mapRow(TO_EMOJI))
            .collectList();
    }

    public Mono<Emoji> findByCode(final String code) {
        return r2dbc.withHandle(handle -> handle
                .select("SELECT * FROM emoji WHERE code = $1", code)
                .mapRow(TO_EMOJI))
            .switchIfEmpty(Mono.error(new NoSuchElementException()))
            .single();
    }

    public Mono<List<EmojiUsage>> findUsageWhereUsedAtAfter(final long seconds) {
        return r2dbc.withHandle(handle -> handle
                .select("SELECT * FROM emoji_usage WHERE used_at::timestamptz>=NOW() - INTERVAL '" + seconds + " SECOND' " +
                    "order by used_at desc")
                .mapRow(TO_EMOJI_USAGE))
            .collectList();
    }

    public Mono<Integer> save(final Emoji emoji) {
        return r2dbc.withHandle(handle -> handle
                .execute("INSERT INTO emoji(code, category, name) VALUES($1, $2, $3)",
                    emoji.getCode(), emoji.getCategory(), emoji.getName()))
            .single();
    }

    public Mono<Integer> saveUsage(final String code, final Instant usedAt) {
        Mono<Integer> storeUsage = r2dbc.withHandle(handle -> handle
            .execute("INSERT INTO emoji_usage(code, used_at) VALUES($1, $2)",
                code, DATE_FORMAT.format(usedAt)))
            .single();

        Mono<Integer> updateUsageCount = r2dbc.withHandle(handle -> handle
            .execute("UPDATE emoji SET usage_count=(SELECT usage_count+1 FROM emoji WHERE code=$1) WHERE code=$2",
                code, code))
            .filter(rowCount -> rowCount > 0)
            .switchIfEmpty(Mono.error(new NoSuchElementException()))
            .single();

        return storeUsage.then(updateUsageCount);
    }

    public Mono<Integer> deleteByCode(final String code) {
        return r2dbc.withHandle(handle -> handle
            .execute("DELETE FROM emoji WHERE code = $1", code))
            .filter(rowCount -> rowCount > 0)
            .switchIfEmpty(Mono.error(new NoSuchElementException()))
            .single();
    }
}
