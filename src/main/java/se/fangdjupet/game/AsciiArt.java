package se.fangdjupet.game;

import java.util.List;

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
                 /|   |\\     "You look tired,
                  |___|       take this!"
                  /   \\
            """;

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
