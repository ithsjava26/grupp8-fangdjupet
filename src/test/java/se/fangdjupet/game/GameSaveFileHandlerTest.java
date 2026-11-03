package se.fangdjupet.game;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GameSaveFileHandlerTest {

    @TempDir
    Path dir;

    @Test
    void saveAndLoadGivesSameState() throws IOException {
        GameSaveFileHandler handler = new GameSaveFileHandler(dir);
        GameState state = new GameState("Aria", 30, 12, 3);

        handler.save(state, 1);

        assertEquals(state, handler.load(1));
    }

    @Test
    void overwritingSlotReplacesOldSaveAndLeavesNoTempFiles() throws IOException {
        GameSaveFileHandler handler = new GameSaveFileHandler(dir);
        handler.save(new GameState("Aria", 30, 30, 1), 1);
        handler.save(new GameState("Aria", 30, 5, 4), 1);

        assertEquals(new GameState("Aria", 30, 5, 4), handler.load(1));
        try (var files = Files.list(dir)) {
            assertEquals(List.of("save-1.properties"),
                    files.map(p -> p.getFileName().toString()).toList());
        }
    }

    @Test
    void malformedUnicodeEscapeGivesIOException() throws IOException {
        Files.writeString(dir.resolve("save-1.properties"), "version=1\nheroName=\\u12\n");

        assertThrows(IOException.class, () -> new GameSaveFileHandler(dir).load(1));
    }

    @Test
    void missingSlotGivesSaveFileException() {
        SaveFileException e = assertThrows(SaveFileException.class, () -> new GameSaveFileHandler(dir).load(1));
        assertEquals("Sparplats 1 finns inte", e.getMessage());
    }

    @Test
    void missingValueGivesSaveFileException() throws IOException {
        Files.writeString(dir.resolve("save-1.properties"), "version=1\nheroName=Aria\n");

        SaveFileException e = assertThrows(SaveFileException.class, () -> new GameSaveFileHandler(dir).load(1));
        assertEquals("Sparfilen saknar: heroMaxHp", e.getMessage());
    }

    @Test
    void wrongVersionGivesSaveFileException() throws IOException {
        Files.writeString(dir.resolve("save-1.properties"), "version=99\n");

        assertThrows(SaveFileException.class, () -> new GameSaveFileHandler(dir).load(1));
    }

    @Test
    void hpThatIsNotANumberGivesSaveFileException() throws IOException {
        Files.writeString(dir.resolve("save-1.properties"),
                "version=1\nheroName=Aria\nheroMaxHp=30\nheroHp=mycket\nnextRoom=1\n");

        SaveFileException e = assertThrows(SaveFileException.class, () -> new GameSaveFileHandler(dir).load(1));
        assertInstanceOf(NumberFormatException.class, e.getCause());
    }

    @Test
    void saveFileExceptionIsAnIOException() {
        // Kod som redan fångar IOException fångar även SaveFileException
        assertInstanceOf(IOException.class, new SaveFileException("trasig"));
    }

    @Test
    void listSaveSlotsSkipsFilesThatAreNotValidSlots() throws IOException {
        GameSaveFileHandler handler = new GameSaveFileHandler(dir);
        handler.save(new GameState("Aria", 30, 30, 1), 2);
        handler.save(new GameState("Aria", 30, 30, 1), 10);
        Files.createFile(dir.resolve("save-0.properties"));
        Files.createFile(dir.resolve("save-01.properties"));
        Files.createFile(dir.resolve("save-2147483648.properties"));
        Files.createFile(dir.resolve("notes.txt"));

        assertEquals(List.of(2, 10), handler.listSaveSlots());
    }
}
