package reactivestack.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.Pair;
import akka.stream.ActorMaterializer;
import akka.stream.alpakka.slick.javadsl.Slick;
import akka.stream.alpakka.slick.javadsl.SlickRow;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import reactivestack.model.ActorCommand.*;
import reactivestack.model.CategoryType;
import reactivestack.model.Emoji;
import reactivestack.model.EmojiUsage;

import java.time.Instant;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static reactivestack.bootstrap.Server.DB_SESSION;

public class EmojiActor extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    private static final String LIST_ALL_SQL = "SELECT * FROM emoji order by usage_count desc";
    private static final Function<String, String> GET_BY_CODE_SQL = code ->
        String.format("SELECT * FROM emoji WHERE code = '%s'", code);
    private static final Function<Emoji, String> CREATE_SQL = emoji ->
        String.format("INSERT INTO emoji(code, category, name) VALUES('%s','%s','%s')",
            emoji.getCode(), emoji.getCategory(), emoji.getName());
    private static final Function<String, String> DELETE_BY_CODE_SQL = code ->
        String.format("DELETE FROM emoji WHERE code = '%s'", code);
    private static final Function<Pair<String, Instant>, String> CREATE_USAGE_SQL = usagePair ->
        String.format("INSERT INTO emoji_usage(code, used_at) VALUES('%s','%s'); " +
                "UPDATE emoji SET usage_count=(SELECT usage_count+1 FROM emoji WHERE code='%s') WHERE code='%s';",
            usagePair.first(), usagePair.second(), usagePair.first(), usagePair.first());
    private static final Function<Long, String> LIST_RECENT_SQL = seconds ->
        String.format("SELECT * FROM emoji_usage WHERE used_at>=NOW() - INTERVAL '%d SECOND' " +
            "order by used_at desc", seconds);

    public static final Function<SlickRow, Emoji> ROW_TO_EMOJI = (row) ->
        new Emoji(row.nextString(), CategoryType.valueOf(row.nextString()), row.nextString(), row.nextInt());
    public static final Function<SlickRow, EmojiUsage> ROW_TO_EMOJI_USAGE = (row) ->
        new EmojiUsage(row.nextString(), row.nextTimestamp().toInstant());

    private final ActorMaterializer materializer;


    public static Props props() {
        return Props.create(EmojiActor.class);
    }

    public EmojiActor() {
        this.materializer = ActorMaterializer.create(getContext().getSystem());
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
            .match(ListAll.class, this::listAll)
            .match(Create.class, this::create)
            .match(GetByCode.class, this::getByCode)
            .match(DeleteByCode.class, this::deleteByCode)
            .match(StoreAtDate.class, this::storeAtDate)
            .match(ListRecent.class, this::listRecent)
            .build();
    }

    private void listAll(final ListAll command) {
        var sender = sender();

        log.debug("Listing all emojis");
        Slick.source(DB_SESSION, LIST_ALL_SQL, ROW_TO_EMOJI)
            .runWith(Sink.seq(), materializer)
            .whenComplete(replyTo(sender));
    }

    private void create(final Create command) {
        var insertSink = Slick.sink(DB_SESSION, CREATE_SQL);
        var newEmojis = List.of(command.getEmoji());
        var sender = sender();

        log.info("Creating emoji");
        Source.from(newEmojis)
            .runWith(insertSink, materializer)
            .whenComplete(replyTo(sender));
    }

    private void getByCode(final GetByCode command) {
        var sender = sender();
        var code = command.getCode();

        log.debug("Getting emoji with code " + code);
        Slick.source(DB_SESSION, GET_BY_CODE_SQL.apply(code), ROW_TO_EMOJI)
            .runWith(Sink.head(), materializer)
            .whenComplete(replyTo(sender));
    }

    private void deleteByCode(final DeleteByCode command) {
        var code = command.getCode();
        var deleteSink = Slick.sink(DB_SESSION, DELETE_BY_CODE_SQL);
        var sender = sender();

        log.info("Deleting emoji with code " + command.getCode());
        Source.from(List.of(code))
            .runWith(deleteSink, materializer)
            .whenComplete(replyTo(sender));
    }

    private void storeAtDate(final StoreAtDate command) {
        var insertUsageSink = Slick.sink(DB_SESSION, CREATE_USAGE_SQL);
        var usagePair = Pair.apply(command.getCode(), command.getDate());
        var sender = sender();

        log.info("Storing emoji with code {} at date {}", usagePair.first(), usagePair.second());
        Source.from(List.of(usagePair))
            .runWith(insertUsageSink, materializer)
            .whenComplete(replyTo(sender));
    }

    private void listRecent(final ListRecent command) {
        var sender = sender();
        long seconds = command.getSeconds();

        log.debug("Listing emoji usage of the last {} seconds", seconds);
        Slick.source(DB_SESSION, LIST_RECENT_SQL.apply(seconds), ROW_TO_EMOJI_USAGE)
            .runWith(Sink.seq(), materializer)
            .whenComplete(replyTo(sender));
    }

    private BiConsumer<Object, Throwable> replyTo(ActorRef sender) {
        return (done, fail) -> {
            Object response = fail != null ? fail : done;
            sender.tell(response, self());
        };
    }


}
