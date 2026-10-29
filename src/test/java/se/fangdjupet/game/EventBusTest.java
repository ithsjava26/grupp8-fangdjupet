package se.fangdjupet.game;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EventBusTest {

    @Test
    void unregisteringSelfDoesNotSkipRemainingListeners() {
        EventBus bus = new EventBus();
        List<String> received = new ArrayList<>();
        GameEventListener selfRemoving = new GameEventListener() {
            @Override
            public void onEvent(String message) {
                received.add("self:" + message);
                bus.unregister(this);
            }
        };
        bus.register(selfRemoving);
        bus.register(message -> received.add("remaining:" + message));

        bus.publish("first");
        bus.publish("second");

        assertEquals(List.of("self:first", "remaining:first", "remaining:second"), received);
    }

    @Test
    void unregisteringAnotherListenerTakesEffectOnNextPublish() {
        EventBus bus = new EventBus();
        List<String> received = new ArrayList<>();
        GameEventListener removed = message -> received.add("removed:" + message);
        bus.register(message -> {
            received.add("first:" + message);
            bus.unregister(removed);
        });
        bus.register(removed);
        bus.register(message -> received.add("last:" + message));

        bus.publish("first");
        bus.publish("second");

        assertEquals(List.of("first:first", "removed:first", "last:first",
                "first:second", "last:second"), received);
    }

    @Test
    void registeringListenerTakesEffectOnNextPublish() {
        EventBus bus = new EventBus();
        List<String> received = new ArrayList<>();
        GameEventListener added = message -> received.add("added:" + message);
        bus.register(message -> {
            received.add("first:" + message);
            bus.register(added);
        });
        bus.register(message -> received.add("last:" + message));

        bus.publish("first");
        bus.publish("second");

        assertEquals(List.of("first:first", "last:first",
                "first:second", "last:second", "added:second"), received);
    }
}
