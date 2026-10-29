package se.fangdjupet.game;

public class ConsoleEventLogger implements GameEventListener {
    // Tar emot spelhändelser och skriver det till konsolen.
    @Override
    public void onEvent(String message) {
        IO.println(message);
    }
}
