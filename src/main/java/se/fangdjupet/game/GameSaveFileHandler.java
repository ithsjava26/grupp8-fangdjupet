package se.fangdjupet.game;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;

public class GameSaveFileHandler {

    private static final String SAVE_VERSION = "1";
    private final Path saveDirectory;

    public GameSaveFileHandler(Path saveDirectory) {
        if (saveDirectory == null) {
            throw new IllegalArgumentException("Sparmappen får inte vara null");
        }
        this.saveDirectory = saveDirectory;
    }

    public void save(GameState state, int slot) throws IOException {
        if (state == null) {
            throw new IllegalArgumentException("GameState får inte vara null");
        }

        Path file = getSaveFile(slot);
        Files.createDirectories(saveDirectory);

        Properties properties = new Properties();
        properties.setProperty("version", SAVE_VERSION);
        properties.setProperty("heroName", state.heroName());
        properties.setProperty("heroMaxHp", Integer.toString(state.heroMaxHp()));
        properties.setProperty("heroHp", Integer.toString(state.heroHp()));
        properties.setProperty("nextRoom", Integer.toString(state.nextRoom()));

        // Skriv först till en temporär fil, så att den gamla sparfilen finns kvar
        // ända tills den nya är färdigskriven.
        Path temp = Files.createTempFile(saveDirectory, "save-" + slot + "-", ".tmp");
        try {
            try (OutputStream out = Files.newOutputStream(temp)) {
                properties.store(out, "Sparfil för Fangdjupet");
            }
            try {
                Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temp);
        }
    }

    public GameState load(int slot) throws IOException {
        Path file = getSaveFile(slot);

        if (!Files.exists(file)) {
            throw new IOException("Sparplats " + slot + " finns inte");
        }

        Properties properties = new Properties();

        try (InputStream in = Files.newInputStream(file)) {
            properties.load(in);
        } catch (IllegalArgumentException e) {
            // Properties.load kastar detta vid en trasig unicode-escape i filen
            throw new IOException("Sparfilen innehåller ogiltiga data", e);
        }

        String version = required(properties, "version");
        if (!SAVE_VERSION.equals(version)) {
            throw new IOException("Sparversionen stöds inte: " + version);
        }

        try {
            return new GameState(
                    required(properties, "heroName"),
                    Integer.parseInt(required(properties, "heroMaxHp")),
                    Integer.parseInt(required(properties, "heroHp")),
                    Integer.parseInt(required(properties, "nextRoom"))
            );
        } catch (IllegalArgumentException e) {
            throw new IOException("Sparfilen innehåller ogiltiga data", e);
        }
    }

    public List<Integer> listSaveSlots() throws IOException {
        if (!Files.exists(saveDirectory)) {
            return List.of();
        }

        try (Stream<Path> files = Files.list(saveDirectory)) {
            return files
                    .map(path -> path.getFileName().toString())
                    .filter(this::isSaveFileName)
                    .flatMap(this::extractSlotNumber)
                    .sorted()
                    .toList();
        }
    }

    private Path getSaveFile(int slot) {
        if (slot < 1) {
            throw new IllegalArgumentException("Sparplatsen måste vara 1 eller högre");
        }

        return saveDirectory.resolve("save-" + slot + ".properties");
    }

    private boolean isSaveFileName(String name) {
        // Bara positiva tal utan inledande nollor – samma form som getSaveFile skapar
        return name.matches("save-[1-9]\\d*\\.properties");
    }

    private Stream<Integer> extractSlotNumber(String name) {
        String number = name
                .substring("save-".length(), name.length() - ".properties".length());
        try {
            return Stream.of(Integer.parseInt(number));
        } catch (NumberFormatException e) {
            // För stort för en int: hoppa över filen i stället för att stoppa hela listningen
            return Stream.empty();
        }
    }

    private String required(Properties properties, String key) throws IOException {
        String value = properties.getProperty(key);

        if (value == null || value.isBlank()) {
            throw new IOException("Sparfilen saknar: " + key);
        }

        return value;
    }
}