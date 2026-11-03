package se.fangdjupet.game;

import java.io.IOException;

// Kastas när en sparplats inte går att läsa: den finns inte, saknar ett värde,
// har fel version eller innehåller trasiga data.
// Den ärver IOException, så kod som redan fångar IOException fångar även den här.
public class SaveFileException extends IOException {

    public SaveFileException(String message) {
        super(message);
    }

    // cause = det ursprungliga felet, t.ex. ett NumberFormatException när hp inte är ett tal
    public SaveFileException(String message, Throwable cause) {
        super(message, cause);
    }
}
