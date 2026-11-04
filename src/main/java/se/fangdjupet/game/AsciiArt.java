package se.fangdjupet.game;

import se.fangdjupet.items.Item;
import se.fangdjupet.items.ItemType;

import java.util.List;
import java.util.Map;
import java.util.Optional;

// All ASCII-grafik på ett ställe, så att menyloopen inte fylls med stora textblock.
// Streck och tecken i ren ASCII (# - | / \), men texterna har å, ä och ö som resten av spelet.
// Grafiken skickas via en GameEventListener (t.ex. EventBus), aldrig direkt till System.out,
// så att testerna kan läsa den.
public final class AsciiArt {

    private AsciiArt() {}    // bara konstanter och statiska metoder

    // Text block ("""): radbrytningarna följer med. Backslash måste skrivas \\.
    public static final String TITLE = """
             _____  o                _  _                  _
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
                 /|   |\\     "Du ser trött ut,
                  |___|       ta de här!"
                  /   \\
            """;

    // Skolbyggnaden ovanför huvudmenyn (starta, fortsätt, avsluta) – bara där, inte i spelmenyn
    public static final String SCHOOL = """
                                        |>>>
                                        |
                                    ____|____
                                   /  .---.  \\
                                  /  ( XII )  \\
                                 /    '---'    \\
                   ______________/_______________\\______________
                   |                                           |
                   |     F Å N G D J U P E T S   S K O L A     |
                   |  _____   _____   _______   _____   _____  |
                   | |_|_|_| |_|_|_| |  ___  | |_|_|_| |_|_|_| |
                   | |_|_|_| |_|_|_| | |   | | |_|_|_| |_|_|_| |
                   |  _____   _____  | |   | |  _____   _____  |
                   | |_|_|_| |_|_|_| | |  o| | |_|_|_| |_|_|_| |
                   | |_|_|_| |_|_|_| | |   | | |_|_|_| |_|_|_| |
               ____|_________________|_|___|_|_________________|____
            """;

    // Entrédörren slår igen bakom hjälten: inlåst tills Martin är besegrad
    public static final String LOCKED_DOOR = """
                .-----------.
                | .-------. |      K L I C K !
                | |#######| |
                | |###o###| |      Dörren är låst.
                | |#######| |
                |_|_______|_|
            """;

    // ── Rummen i WorldBuilder. Namnet i bilden är utan å/ä/ö, annars blir det ? i Windows-konsolen. ──

    public static final String SCHOOL_DOOR = """
               _______________
              |    SKOLAN     |
              |_______________|
              |  ___________  |
              | |     |     | |
              | |     |   o | |      SKOLDÖRREN
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
            | /    |______|    \\ |   | [___] |    & STÄDSKRUBBEN
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

    // Entrédörren: en sur skoldörr som inte släpper in någon utan passerkort
    public static final String ENTRANCE_DOOR = """
                .---------.
                | o     o |     ENTRÉDÖRREN
                |    ^    |     "Har du passerkort?"
                |  \\___/  |
                |       o |
                |_________|
            """;

    public static final String JAVA_FLECK = """
                .--.
               ( :: )~      JAVAFLÄCKEN
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

    // Stressbollen från Lektionssalen: kläm den (+6 hp varje runda) eller kasta den på Martin
    public static final String STRESS_BALL = """
               .-''''-.
              / ~    ~ \\
             |  (o)(o)  |    STRESSBOLL
              \\  \\__/  /     kläm: +6 hp per runda
               '-....-'      kasta: 10 i skada
            """;

    public static final String BOW = """
               |\\
               | \\
               |--)---->    PILBÅGE
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

    // Städrustningen som ligger gömd i städskrubben: gummiförkläde, hink som hjälm och moppen
    public static final String BRASKLAPP = """
                 ______________________
                |  boolean isAlive;    |    BRASKLAPP
                |  DROP TABLE martin;  |    25 i skada,
                |______________________|    en enda gang
            """;

    public static final String MITTEN = """
                  .---.
                 /     \\__        ULLVANTAR
                |       |  \\      +5 i forsvar, +10 hp
                |       |__/
                |       |          kvarglomda pa
                '-------'          Entredorren
                |_______|
            """;

    public static final String CLEANING_ARMOR = """
                 _______
                |_HINK__|
                 (o   o)          STÄDRUSTNING
               .-'-----'-.        +20 i forsvar
              /|  _____  |\\
             | | |GUMMI| | |#
             | | |_____| | |#     gummiforklade
               |_________| #     och moppen
                |   |   |  #
                |___|___| _#_
            """;

    // Föremålen per sort (ItemType), för vapen och rustningar som inte har en egen bild på namnet
    public static final String SWORD = """
                   /\\
                   ||
                   ||
                   ||        SVÄRD
                 o=||=o
                   ##
                   ()
            """;

    public static final String SPEAR = """
                   /\\
                  /  \\
                  \\  /
                   ||        SPJUT
                   ||
                   ||
                   ||
            """;

    public static final String SHIELD = """
               .--------.
               | \\    / |
               |  \\  /  |    SKÖLD
               |   ><   |
                \\  /\\  /
                 '----'
            """;

    public static final String HELMET = """
                .------.
               /  ____  \\
              |  |____|  |   HJÄLM
              |__________|
            """;

    public static final String POTION = """
                  _
                 |=|
                .' '.
               | ~~~ |       DRYCK
               |_____|
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

    // ── Vinst: Martin är besegrad ──

    public static final String VICTORY = """
                  ___________
                 '._==_==_=_.'
                 .-\\:      /-.        D U   V A N N !
                | (|:.     |) |
                 '-|:.     |-'        Martin är besegrad och
                   \\::.    /          Fångdjupet är klarat.
                    '::. .'
                      ) (
                    _.' '._
                   '-------'
            """;

    // ANSI-färgkoder: ESC[31m gör texten röd, ESC[0m går tillbaka till vanlig färg.
    // Fungerar i IntelliJ och Windows Terminal. Koderna är också ren ASCII.
    public static final String RED = "\u001B[31m";
    public static final String RESET = "\u001B[0m";
    public static final String YELLOW = "\u001B[33m";   // gult: något man kan plocka upp

    // Gör texten gul, t.ex. "Här ligger: Pilbåge (+3 i skada)"
    public static String yellow(String text) {
        if (text == null) {
            throw new IllegalArgumentException("text must not be null");
        }
        return YELLOW + text + RESET;
    }

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
            Map.entry("Entrédörren", ENTRANCE_DOOR),
            Map.entry("JavaFläcken", JAVA_FLECK),
            Map.entry("Troll", TROLL),
            Map.entry("Trollkarl", WIZARD),
            Map.entry("StädHäxan", WITCH),
            Map.entry("Lektor Martin", MARTIN),
            Map.entry("Frida", FRIDA),
            Map.entry("en kopp kaffe", COFFEE_CUP),
            Map.entry("en kanelbulle", CINNAMON_BUN),
            Map.entry("Java-bok", JAVA_BOOK),
            Map.entry("Pilbåge", BOW),
            Map.entry("Trollstav", STAFF),
            Map.entry(WorldBuilder.STRESSBOLL, STRESS_BALL),
            Map.entry(WorldBuilder.STADRUSTNING, CLEANING_ARMOR),
            Map.entry(WorldBuilder.VANTE, MITTEN),
            Map.entry("Brasklapp", BRASKLAPP));

    // Bilden för en fiende, ett föremål eller en person, eller tomt om den saknar bild:
    //   AsciiArt.forName(enemy.getName()).ifPresent(art -> AsciiArt.show(output, art));
    public static Optional<String> forName(String name) {
        if (name == null) return Optional.empty();
        return Optional.ofNullable(THINGS.get(name));
    }

    // Sorten -> bilden, när föremålet inte har en egen bild på namnet
    private static final Map<ItemType, String> ITEM_TYPES = Map.of(
            ItemType.SWORD, SWORD,
            ItemType.SPEAR, SPEAR,
            ItemType.BOW, BOW,
            ItemType.STAFF, STAFF,
            ItemType.SHIELD, SHIELD,
            ItemType.HELMET, HELMET,
            ItemType.GLOVE, MITTEN,
            ItemType.NOTE, BRASKLAPP,
            ItemType.POTION, POTION,
            ItemType.STRESSBALL, STRESS_BALL);

    // Bilden för ett föremål: först på namnet (t.ex. "Pilbåge", "en kopp kaffe"), annars på sorten
    // (ett svärd från ItemFactory får svärdet). Tomt för null.
    public static Optional<String> forItem(Item item) {
        if (item == null) return Optional.empty();
        return forName(item.description()).or(() -> Optional.ofNullable(ITEM_TYPES.get(item.type())));
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
