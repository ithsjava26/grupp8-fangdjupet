package se.fangdjupet.game;

import java.util.ArrayList;
import java.util.List;

// Skickar varje händelse vidare till alla registrerade lyssnare, t.ex. konsolen och loggfilen (Observer).
// Testerna finns i EventBusTest.
public class EventBus implements GameEventListener {
    private final List<GameEventListener> listeners = new ArrayList<>();

    public void register(GameEventListener listener) {
        listeners.add(listener);
    }

    public void unregister(GameEventListener listener) {
        listeners.remove(listener);
    }

    @Override
    public void onEvent(String message) {
        for (GameEventListener listener : new ArrayList<>(listeners)) {
            listener.onEvent(message);
        }
    }
}