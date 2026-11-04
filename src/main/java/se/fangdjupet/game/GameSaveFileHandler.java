package se.fangdjupet.game;

import se.fangdjupet.items.Armor;
import se.fangdjupet.items.Item;
import se.fangdjupet.items.ItemType;
import se.fangdjupet.items.Potion;
import se.fangdjupet.items.StressBall;
import se.fangdjupet.items.Weapon;
import se.fangdjupet.items.WeaponRange;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;

// Sparar och läser in äventyret: en .properties-fil per sparplats, save-<nr>.properties i sparmappen.
// GameEngine sparar på plats 1 (spelmenyn 4) och läser in den igen (huvudmenyn 2).
public class GameSaveFileHandler {

    // Föremålen är valfria nycklar (item.0, item.1 ...), så gamla sparfiler har samma version och går att läsa
    private static final String SAVE_VERSION = "1";
    private static final String ITEM_KEY = "item.";
    private static final int ITEM_FIELDS = 6;
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
        for (int i = 0; i < state.items().size(); i++) {
            properties.setProperty(ITEM_KEY + i, encode(state.items().get(i)));
        }

        // Skriv först till en temporär fil, så att den gamla sparfilen finns kvar
        // ända tills den nya är färdigskriven.
        Path temp = Files.createTempFile(saveDirectory, "save-" + slot + "-", ".tmp");
        try {
            try (OutputStream out = Files.newOutputStream(temp)) {
                properties.store(out, "Sparfil för Fangdjupet");
            }
            // ATOMIC_MOVE byter sparplatsen i ett enda steg: antingen finns den gamla
            // filen eller den nya, aldrig en halvskriven. Den temporära filen ligger i
            // samma mapp, så det fungerar på vanliga filsystem. Går det ändå inte kastas
            // ett IOException och den gamla sparningen finns kvar – hellre det än en
            // icke-atomär ersättning som kan lämna en trasig fil.
            Files.move(temp, file, StandardCopyOption.ATOMIC_MOVE);
        } finally {
            Files.deleteIfExists(temp);
        }
    }

    // Läser in sparplatsen. Finns den inte, eller är filen trasig, kastas SaveFileException.
    public GameState load(int slot) throws IOException {
        Path file = getSaveFile(slot);

        if (!Files.exists(file)) {
            throw new SaveFileException("Sparplats " + slot + " finns inte");
        }

        Properties properties = new Properties();

        try (InputStream in = Files.newInputStream(file)) {
            properties.load(in);
        } catch (IllegalArgumentException e) {
            // Properties.load kastar detta vid en trasig unicode-escape i filen
            throw new SaveFileException("Sparfilen innehåller ogiltiga data", e);
        }

        String version = required(properties, "version");
        if (!SAVE_VERSION.equals(version)) {
            throw new SaveFileException("Sparversionen stöds inte: " + version);
        }

        try {
            return new GameState(
                    required(properties, "heroName"),
                    Integer.parseInt(required(properties, "heroMaxHp")),
                    Integer.parseInt(required(properties, "heroHp")),
                    Integer.parseInt(required(properties, "nextRoom")),
                    readItems(properties)
            );
        } catch (IllegalArgumentException e) {
            throw new SaveFileException("Sparfilen innehåller ogiltiga data", e);
        }
    }

    // item.0, item.1 ... tills numret saknas. En gammal sparfil har inga, och då blir listan tom.
    // Finns fler item-nycklar än vi läste har filen en lucka, och då är den trasig.
    private List<Item> readItems(Properties properties) {
        List<Item> items = new ArrayList<>();
        for (int i = 0; properties.getProperty(ITEM_KEY + i) != null; i++) {
            items.add(decode(properties.getProperty(ITEM_KEY + i)));
        }
        long saved = properties.stringPropertyNames().stream().filter(key -> key.startsWith(ITEM_KEY)).count();
        if (saved != items.size()) {
            throw new IllegalArgumentException("Ett föremål saknas i sparfilen");
        }
        return items;
    }

    // En rad per föremål, t.ex. weapon;BOW;RANGED;4;1;Pilbåge: sort, typ, räckvidd (- utan räckvidd), styrka,
    // nivå och namn. Bonusen sparas eftersom den slumpas. Namnet ligger sist, så det får innehålla semikolon.
    private static String encode(Item item) {
        return switch (item) {
            case Weapon w -> line("weapon", w, w.range().name(), w.damageBonus());
            case Armor a -> line("armor", a, "-", a.defense());
            case Potion p -> line("potion", p, "-", p.healAmount());
            case StressBall b -> line("stressball", b, "-", b.hpPerTurn());
        };
    }

    private static String line(String kind, Item item, String range, int power) {
        return String.join(";", kind, item.type().name(), range, Integer.toString(power),
                Integer.toString(item.level()), item.description());
    }

    // Tvärtom mot encode. En trasig rad ger IllegalArgumentException, som load gör om till SaveFileException.
    private static Item decode(String line) {
        String[] fields = line.split(";", ITEM_FIELDS);
        if (fields.length != ITEM_FIELDS) {
            throw new IllegalArgumentException("Trasigt föremål: " + line);
        }

        ItemType type = ItemType.valueOf(fields[1]);
        int power = Integer.parseInt(fields[3]);
        int level = Integer.parseInt(fields[4]);
        String description = fields[5];

        return switch (fields[0]) {
            case "weapon" -> new Weapon(type, WeaponRange.valueOf(fields[2]), description, power, level);
            case "armor" -> new Armor(type, description, power, level);
            case "potion" -> new Potion(type, description, power, level);
            case "stressball" -> new StressBall(type, description, power, level);
            default -> throw new IllegalArgumentException("Okänd sorts föremål: " + fields[0]);
        };
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
            throw new SaveFileException("Sparfilen saknar: " + key);
        }

        return value;
    }
}