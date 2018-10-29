package reactivestack.actor;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import reactivestack.model.ActorCommand.*;
import reactivestack.model.Emoji;
import reactivestack.model.EmojiUsage;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static reactivestack.model.CategoryType.SMILEYS_AND_EMOTION;

public class EmojiActor extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(getContext().getSystem(), this);

    public static Props props() {
        return Props.create(EmojiActor.class);
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
        log.debug("Listing all emojis");
        var e1 = new Emoji("1", SMILEYS_AND_EMOTION, "xxx", 1);
        var e2 = new Emoji("2", SMILEYS_AND_EMOTION, "yyy", 2);
        var response = List.of(e1, e2);
        sender().tell(response, self());
    }

    private void create(final Create command) {
        log.info("Creating emoji");
        sender().tell(new Emoji("3", SMILEYS_AND_EMOTION, "zzz", 3), self());
    }

    private void getByCode(final GetByCode command) {
        log.debug("Getting emoji having code " + command.getCode());
        sender().tell(new Emoji(command.getCode(), SMILEYS_AND_EMOTION, "zzz", 3), self());
    }

    private void deleteByCode(final DeleteByCode command) {
        log.info("Deleting emoji having code " + command.getCode());
        sender().tell(true, self());
    }

    private void storeAtDate(final StoreAtDate command) {
        log.info("Storing emoji with code {} at date ", command.getCode(), command.getDate());
        sender().tell(new EmojiUsage(command.getCode(), command.getDate()), self());
    }

    private void listRecent(final ListRecent command) {
        log.debug("Listing emoji usage of the last {} seconds", command.getSeconds());
        var u1 = new EmojiUsage("1", Instant.now());
        var u2 = new EmojiUsage("1", Instant.now().minus(1, ChronoUnit.HOURS));
        var response = List.of(u1, u2);
        sender().tell(response, self());
    }


}
