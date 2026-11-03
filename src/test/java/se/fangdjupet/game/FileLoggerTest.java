package se.fangdjupet.game;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FileLoggerTest {

    @TempDir
    Path dir;

    @Test
    void eachEventBecomesALine() throws IOException {
        Path log = dir.resolve("spel.log");
        FileLogger logger = new FileLogger(log);
        logger.onEvent("Äventyret börjar!");
        logger.onEvent("Hero: Robert slår Troll skada: 8");
        assertEquals(List.of("Äventyret börjar!", "Hero: Robert slår Troll skada: 8"), Files.readAllLines(log));
    }

    @Test
    void anOldLogIsKeptAndAddedTo() throws IOException {
        Path log = dir.resolve("spel.log");
        Files.writeString(log, "Förra spelet" + System.lineSeparator());
        new FileLogger(log).onEvent("Nytt spel");
        assertEquals(List.of("Förra spelet", "Nytt spel"), Files.readAllLines(log));
    }

    @Test
    void colorCodesAreRemoved() throws IOException {
        Path log = dir.resolve("spel.log");
        new FileLogger(log).onEvent(AsciiArt.red("GAME OVER"));
        assertEquals(List.of("GAME OVER"), Files.readAllLines(log));
    }

    @Test
    void worksOnTheEventBus() throws IOException {
        Path log = dir.resolve("spel.log");
        EventBus bus = new EventBus();
        bus.register(new FileLogger(log));
        bus.onEvent("Robert vinner Fångdjupet!");
        assertEquals(List.of("Robert vinner Fångdjupet!"), Files.readAllLines(log));
    }

    @Test
    void aFileThatCannotBeWrittenDoesNotCrashTheGame() {
        // Mappen finns inte – skrivningen misslyckas, men onEvent kastar inget
        FileLogger logger = new FileLogger(dir.resolve("finns-inte").resolve("spel.log"));
        assertDoesNotThrow(() -> logger.onEvent("Hej"));
        assertDoesNotThrow(() -> logger.onEvent("Hej igen"));
    }

    @Test
    void fileMustNotBeNull() {
        assertThrows(IllegalArgumentException.class, () -> new FileLogger(null));
    }
}
