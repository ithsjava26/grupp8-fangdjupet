package se.fangdjupet.game;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

// Skriver varje spelhändelse som en rad i en textfil (Observer, som ConsoleEventLogger).
// Main registrerar den på EventBus, så hela äventyret hamnar i fangdjupet.log.
public class FileLogger implements GameEventListener {
    // ANSI-färgkoder, t.ex. ESC[31m runt GAME OVER. De syns som skräptecken i en textfil.
    private static final String ANSI_COLOR = "\u001B\\[[0-9;]*m";

    private final Path file;
    private boolean broken;   // true efter ett skrivfel: då slutar loggern försöka

    public FileLogger(Path file) {
        if (file == null) {
            throw new IllegalArgumentException("file must not be null");
        }
        this.file = file;
    }

    // Lägger till raden sist i filen (skapar filen första gången). Färgkoderna tas bort.
    @Override
    public void onEvent(String message) {
        if (broken || message == null) return;
        try {
            Files.writeString(file, message.replaceAll(ANSI_COLOR, "") + System.lineSeparator(),
                    StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            // Spelet ska inte krascha för att loggfilen inte går att skriva. Säg till en gång och sluta sedan.
            broken = true;
            System.err.println("Loggfilen " + file + " går inte att skriva: " + e.getMessage());
        }
    }
}
