package se.fangdjupet.game;

import java.util.ArrayList;
import java.util.List;

//Todo: Tester
public class EventBus {

    private final List<GameEventListener> listeners = new ArrayList<>();

    public void register(GameEventListener listener) {
        listeners.add(listener);
    }

    public void unregister(GameEventListener listener) {
        listeners.remove(listener);
    }

    public void publish(String message) {
        for (GameEventListener listener : new ArrayList<>(listeners)) {
            listener.onEvent(message);
        }
    }
}
