package se.fangdjupet.game;

import java.util.List;
import java.util.Map;
import java.util.Optional;

// All ASCII-grafik på ett ställe, så att menyloopen inte fylls med stora textblock.
// Bara ren ASCII: tecken som █ och å blir ofta ? i Windows-konsolen.
// Grafiken skickas via en GameEventListener (t.ex. EventBus), aldrig direkt till System.out,
// så att testerna kan läsa den.
public final class AsciiArt {

    private AsciiArt() {}    // bara konstanter och statiska metoder

    // Text block ("""): radbrytningarna följer med. Backslash måste skrivas \\.
    public static final String TITLE = """
             _____                   _  _                  _
            |  ___|_ _ _ __   __ _  __| |(_)_   _ _ __   ___| |_
            | |_ / _` | '_ \\ / _` |/ _` || | | | | '_ \\ / _ \\ __|
            |  _| (_| | | | | (_| | (_| || | |_| | |_) |  __/ |_
            |_|  \\__,_|_| |_|\\__, |\\__,_|/ |\\__,_| .__/ \\___|\\__|
                             |___/     |__/      |_|
            """;

    public static final String MARTIN = """
                  .-----.
                 / o   o \\      MARTIN
                |    ^    |     "Let me explain
                 \\  '-'  /       Hello World to you..."
                  '-----'
                  /|=|=|\\
            """;

    public static final String FRIDA = """
                  .---.
                 ( ^_^ )     FRIDA
                 /|   |\\     "Du ser trott ut,
                  |___|       ta de har!"
                  /   \\
            """;

    // ── Rummen i WorldBuilder. Namnet i bilden är utan å/ä/ö, annars blir det ? i Windows-konsolen. ──

    public static final String SCHOOL_DOOR = """
               _______________
              |    SKOLAN     |
              |_______________|
              |  ___________  |
              | |     |     | |
              | |     |   o | |      SKOLDORREN
              | |_____|_____| |
              |_______________|
            """;

    // Kafferummet: kaffeautomaten med JAVA-skylten, och en kopp på bänken
    public static final String COFFEE_ROOM = """
               ______________________
              |    ~  J A V A  ~     |
              |______________________|
              |  .----------------.  |
              |  |  KAFFE    [ON] |  |        ( (
              |  |____________.___|  |         ) )
              |   (1)  (2)  (3)  o   |       .-----.
              |                      |       |     |]    KAFFERUMMET
              |        _|  |_        |       '-----'
              |       |      |       |
            __|_______|______|_______|___________________
            """;

    public static final String CORRIDOR = """
             ____________________     _______
            |\\                  /|   |   |   |
            | \\    ________    / |   |   |   |
            |  |  |        |  |  |   |  /|\\  |    KORRIDOREN
            | /    |______|    \\ |   | [___] |    & STADSKRUBBEN
            |/__________________\\|   |_______|
            """;

    public static final String CLASSROOM = """
              ________________________
             | public static void     |
             |  main(String[] args)   |
             |________________________|
                |                  |        LEKTIONSSALEN
             [____]  [____]  [____]  [____]
            """;

    // ── Fienderna ──

    public static final String JAVA_FLECK = """
                .--.
               ( :: )~      JAVAFLACKEN
                '--'
            """;

    public static final String TROLL = """
                ,   ,
               (o . o)
               /( - )\\      TROLL
                /   \\
            """;

    public static final String WIZARD = """
                  /\\
                 /**\\
                (o  o)      TROLLKARL
                /|~~|\\      "Thou shall
                 /  \\        not pass!"
            """;

    // StädHäxan: elak, med häxhatt, långt hår, arga ögonbryn och huggtänder. Svinto i ena handen, sprayflaskan i den andra.
    public static final String WITCH = """
                      /\\
                     /  \\
                 ___/____\\___
                  ////||\\\\\\\\
                 ///\\    /\\\\\\      STADHAXAN
                 ||  O  O  ||
                 ||   >    ||      tvattsvamp, Svinto
                 ||  \\/\\/  ||      och kemikalier
                  \\\\  __  //
              _     _/|##|\\_
             |S|===/  |##|  \\===<]=-  ~ ~ ~
             |_|      |##|
                     /    \\
                    /_    _\\
            """;

    // ── Saker man hittar ──

    public static final String COFFEE_CUP = """
                ( (
                 ) )
               .-----.
               |     |]     KAFFE
               `-----'
            """;

    public static final String CINNAMON_BUN = """
               .-~~~-.
              ( @ @ @ )     KANELBULLE
               '-...-'
            """;

    public static final String JAVA_BOOK = """
               ________
              | JAVA   |
              | for    |    JAVA-BOKEN
              | dummies|
              |________|
            """;

    public static final String BOW = """
               |\\
               | \\
               |--)---->    PILBAGE
               | /
               |/
            """;

    public static final String STAFF = """
                 *
                (o)
                 |          TROLLSTAV
                 |
                 |
            """;

    // ── Game over ──

    // Visas i rött när hjälten dör – se red()
    public static final String GAME_OVER = """
               ____    _    __  __ _____    _____     _______ ____
              / ___|  / \\  |  \\/  | ____|  / _ \\ \\   / / ____|  _ \\
             | |  _  / _ \\ | |\\/| |  _|   | | | \\ \\ / /|  _| | |_) |
             | |_| |/ ___ \\| |  | | |___  | |_| |\\ V / | |___|  _ <
              \\____/_/   \\_\\_|  |_|_____|  \\___/  \\_/  |_____|_| \\_\\
            """;

    // ANSI-färgkoder: ESC[31m gör texten röd, ESC[0m går tillbaka till vanlig färg.
    // Fungerar i IntelliJ och Windows Terminal. Koderna är också ren ASCII.
    public static final String RED = "\u001B[31m";
    public static final String RESET = "\u001B[0m";

    // Gör texten röd, t.ex. red(GAME_OVER)
    public static String red(String text) {
        if (text == null) {
            throw new IllegalArgumentException("text must not be null");
        }
        return RED + text + RESET;
    }

    // Namnet på en fiende, ett föremål eller en person -> bilden.
    // Namnen måste vara exakt som i GameEngine/WorldBuilder (getName() eller description()).
    private static final Map<String, String> THINGS = Map.ofEntries(
            Map.entry("JavaFläcken", JAVA_FLECK),
            Map.entry("Troll", TROLL),
            Map.entry("Trollkarl", WIZARD),
            Map.entry("StädHäxan", WITCH),
            Map.entry("Kapten Martin", MARTIN),
            Map.entry("Frida", FRIDA),
            Map.entry("en kopp kaffe", COFFEE_CUP),
            Map.entry("en kanelbulle", CINNAMON_BUN),
            Map.entry("Java-bok", JAVA_BOOK),
            Map.entry("Pilbåge", BOW),
            Map.entry("Trollstav", STAFF));

    // Bilden för en fiende, ett föremål eller en person, eller tomt om den saknar bild:
    //   AsciiArt.forName(enemy.getName()).ifPresent(art -> AsciiArt.show(output, art));
    public static Optional<String> forName(String name) {
        if (name == null) return Optional.empty();
        return Optional.ofNullable(THINGS.get(name));
    }

    // Rumsnamnet (Room.getName()) -> bilden. Namnen måste vara exakt som i WorldBuilder.
    private static final Map<String, String> ROOMS = Map.of(
            "Skoldörren", SCHOOL_DOOR,
            "Kafferummet", COFFEE_ROOM,
            "Korridoren och städskrubben", CORRIDOR,
            "Lektionssalen", CLASSROOM);

    // Bilden för ett rum, eller tomt om rummet saknar bild. Används så här i menyn:
    //   AsciiArt.forRoom(room.getName()).ifPresent(art -> AsciiArt.show(output, art));
    public static Optional<String> forRoom(String roomName) {
        if (roomName == null) return Optional.empty();    // Map.of kastar NullPointerException på get(null)
        return Optional.ofNullable(ROOMS.get(roomName));
    }

    // Ram runt valfri text, t.ex. ett rumsnamn eller ett föremåls beskrivning.
    // Tar en String, så den fungerar oavsett hur föremålen ser ut (name() eller info()).
    public static String banner(String text) {
        if (text == null) throw new IllegalArgumentException("text must not be null");
        List<String> lines = text.lines().toList();
        int width = lines.stream().mapToInt(String::length).max().orElse(0);

        String edge = "+" + "-".repeat(width + 2) + "+";
        StringBuilder sb = new StringBuilder(edge).append('\n');
        for (String line : lines) {
            sb.append("| ").append(line).append(" ".repeat(width - line.length())).append(" |\n");
        }
        return sb.append(edge).toString();
    }

    // Skickar en bild till lyssnaren, utan sista radbrytningen från text blocket.
    public static void show(GameEventListener out, String art) {
        out.onEvent(art.endsWith("\n") ? art.substring(0, art.length() - 1) : art);
    }
}
