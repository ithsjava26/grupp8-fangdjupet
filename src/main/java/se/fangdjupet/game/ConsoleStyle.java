package se.fangdjupet.game;

import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Tydligare grafik i konsolen: färger, fet stil och riktiga staplar. Bara utseendet ändras –
// spelets rader är desamma (webben läser dem som text, och loggfilen sparar dem utan färg).
// Används bara när spelet körs i en riktig konsol (AnimatedConsoleLogger och GameEngines menyer när enabled är på).
public final class ConsoleStyle {
    private ConsoleStyle() {}

    static final String RESET = "\u001B[0m", BOLD = "\u001B[1m", DIM = "\u001B[2m",
            RED = "\u001B[91m", GREEN = "\u001B[92m", YELLOW = "\u001B[93m", BLUE = "\u001B[94m",
            MAGENTA = "\u001B[95m", CYAN = "\u001B[96m", WHITE = "\u001B[97m";

    private static final Pattern HP_BAR = Pattern.compile("(.+?)  \\[([#-]+)\\] (\\d+)/(\\d+)");
    private static final Pattern OPTION = Pattern.compile("([0-9]+|e)\\. (.*)");
    private static final Pattern DAMAGE = Pattern.compile(" skada: (\\d+)$");
    private static final Pattern SPEECH = Pattern.compile("([^:\"]{1,30}): (\".*\")");
    private static final Pattern MARK = Pattern.compile("\\[([@x!?#])\\]");
    private static final Pattern GEAR = Pattern.compile("(Vapen|Rustning|Attack|Java-boken|Stressboll|Ryggsäck): (.+)");
    private static final String GEAR_TITLE = "Visa hjältens utrustning.";
    private static final int GEAR_WIDTH = 60;
    // Ikonerna är tecken med en kolumns bredd: emojis blir olika breda i olika terminaler och förstör raderna
    private static final Map<String, String> GEAR_ICONS = Map.of("Vapen", "►", "Rustning", "■", "Attack", "»",
            "Java-boken", "≡", "Stressboll", "●", "Ryggsäck", "♦");

    // Webbens färger (--accent, --red, --ok, --you, --magic, --ink, --dim och --art i fangdjupet-web).
    // Alla färgkoder i spelet byts mot dem i tint, så resten av koden behöver bara de 16 vanliga.
    private static final String ACCENT = rgb(233, 178, 94), SOFT_RED = rgb(255, 107, 91), OK = rgb(127, 211, 166),
            YOU = rgb(159, 195, 238), MAGIC = rgb(183, 156, 255), INK = rgb(238, 243, 248), MUTED = rgb(163, 175, 188),
            ART = rgb(240, 215, 164);
    private static final String PANEL_BACKGROUND = "\u001B[48;2;23;28;36m";   // --panel2
    private static final Map<String, String> TRUE_COLORS = Map.ofEntries(
            Map.entry("\u001B[31m", SOFT_RED), Map.entry(RED, SOFT_RED),
            Map.entry("\u001B[32m", OK), Map.entry(GREEN, OK),
            Map.entry("\u001B[33m", ACCENT), Map.entry(YELLOW, ACCENT),
            Map.entry("\u001B[35m", MAGIC), Map.entry(MAGENTA, MAGIC),
            Map.entry("\u001B[36m", YOU), Map.entry(CYAN, YOU), Map.entry(BLUE, YOU),
            Map.entry(WHITE, INK), Map.entry(DIM, MUTED));
    private static final java.util.List<String> ROOMS =
            java.util.List.of("Skoldörren", "Kafferummet", "Korridoren och städskrubben", "Lektionssalen");

    // Påslaget i Main när konsolen animeras – webben kör spelet utan, och får vanlig text
    private static boolean enabled;
    private static boolean trueColor;   // webbens färger i stället för de 16 vanliga

    public static void enable(boolean on) {
        enabled = on;
        if (!on) trueColor = false;
    }

    public static void trueColor(boolean on) {
        trueColor = on;
    }

    // Klarar terminalen 24-bitarsfärg? setting är -Dfangdjupet.farger: 24 eller 16 bestämmer, annars läser vi
    // miljön. IntelliJs Kör-fönster syns inte där, så där sätter man -Dfangdjupet.farger=24 själv.
    public static boolean supportsTrueColor(UnaryOperator<String> env, String setting) {
        if ("24".equals(setting)) return true;
        if ("16".equals(setting)) return false;
        String colorTerm = env.apply("COLORTERM");
        return "truecolor".equalsIgnoreCase(colorTerm) || "24bit".equalsIgnoreCase(colorTerm)
                || env.apply("WT_SESSION") != null                         // Windows Terminal
                || "vscode".equals(env.apply("TERM_PROGRAM"))
                || "JetBrains-JediTerm".equals(env.apply("TERMINAL_EMULATOR"));   // IntelliJs terminal
    }

    private static String rgb(int red, int green, int blue) {
        return "\u001B[38;2;" + red + ";" + green + ";" + blue + "m";
    }

    // Sista steget före utskrift: byter de vanliga färgerna mot webbens. Utan 24-bitarsfärg blir texten som den är.
    public static String tint(String text) {
        if (!trueColor || text == null) return text;
        for (Map.Entry<String, String> color : TRUE_COLORS.entrySet()) {
            text = text.replace(color.getKey(), color.getValue());
        }
        return text;
    }

    // Texten utan färgkoder: så bred är den på skärmen
    static String plain(String text) {
        return text.replaceAll("\u001B\\[[0-9;]*m", "");
    }

    public static boolean enabled() {
        return enabled;
    }

    // ── En rad från spelet (EventBus) ──

    public static String line(String t) {
        if (t == null || t.isEmpty() || t.contains("\u001B[")) return t;   // redan färgad (gult, rött): som den är
        Matcher bar = HP_BAR.matcher(t);
        if (bar.matches()) return hpBar(bar.group(1), bar.group(2), bar.group(3), bar.group(4));
        if (t.equals(GEAR_TITLE)) return top("UTRUSTNING", GEAR_WIDTH, "");
        Matcher gear = GEAR.matcher(t);
        if (gear.matches()) return gearRow(gear.group(1), gear.group(2));
        if (t.startsWith("Hero: ")) return GREEN + BOLD + "» " + RESET + WHITE + damage(t.substring(6)) + RESET;
        if (t.startsWith("Enemy: ")) return RED + BOLD + "« " + RESET + RED + damage(t.substring(7)) + RESET;
        if (t.startsWith("Fiende ") && t.endsWith(" dyker upp!")) return RED + BOLD + "!! " + t + RESET;
        if (t.matches(".+ vinner (mot .+|Fångdjupet)!")) return GREEN + BOLD + "★ " + t + " ★" + RESET;
        if (t.contains("besegrades av") || t.contains("dog av sin egen attack")) return RED + BOLD + t + RESET;
        if (t.contains("blir arg!") || t.contains("skrattar elakt")) return MAGENTA + BOLD + t + RESET;
        if (t.startsWith("Du hittar") && !t.startsWith("Du hittar inget")) return YELLOW + BOLD + "✦ " + t + RESET;
        if (t.contains(" klämmer stressbollen") || t.matches(".*\\(\\+\\d+ hp\\).*")) return GREEN + t + RESET;
        if (t.startsWith("Lavalampan bubblar") || t.contains("tar fram en lavalampa")) return MAGENTA + BOLD + "~ " + t + RESET;
        Matcher say = SPEECH.matcher(t);
        if (say.matches()) return MAGENTA + BOLD + say.group(1) + ":" + RESET + " " + WHITE + say.group(2) + RESET;
        if (t.startsWith("Du går till ")) return CYAN + "→ " + t + RESET;
        if (ROOMS.contains(t)) return CYAN + BOLD + "▌ " + t.toUpperCase() + RESET;   // rummets namn: en rubrik
        return t;
    }

    // "Robert  [#####-----] 20/40" → namnet fett, en färgad stapel av block och siffrorna
    static String hpBar(String name, String bar, String cur, String max) {
        int full = (int) bar.chars().filter(c -> c == '#').count();
        int pct = Integer.parseInt(max) == 0 ? 0 : Integer.parseInt(cur) * 100 / Integer.parseInt(max);
        String color = pct > 50 ? GREEN : pct > 25 ? YELLOW : RED;
        return BOLD + WHITE + name + RESET + "  " + color + "█".repeat(full) + RESET + DIM + "░".repeat(bar.length() - full) + RESET
                + "  " + BOLD + color + cur + RESET + DIM + "/" + max + RESET;
    }

    // Hjälten och fienden sida vid sida, som stridspanelen på webben
    public static String battle(String hero, String enemy) {
        return onPanel(CYAN + "▌ " + RESET + line(hero) + DIM + "   vs   " + RESET + line(enemy));
    }

    // Raden ovanför kartan: rummet, hjältens hp och attacken
    public static String status(String room, String hp, String attack) {
        return tint(onPanel(CYAN + BOLD + "▌ " + room.toUpperCase() + RESET + "   " + line(hp) + "   " + DIM + attack + RESET));
    }

    private static String gearRow(String label, String value) {
        return DIM + "│ " + RESET + YELLOW + GEAR_ICONS.get(label) + " " + BOLD + String.format("%-11s", label) + RESET
                + WHITE + value + RESET;
    }

    // Raden som stänger panelen i "Visa hjältens utrustning"
    public static String gearEnd() {
        return enabled ? tint(DIM + "└" + "─".repeat(GEAR_WIDTH) + RESET + "\n") : "";
    }

    // Panelens färg bakom raden. Varje RESET stänger av den, så den slås på igen efter dem.
    private static String onPanel(String row) {
        return trueColor ? PANEL_BACKGROUND + row.replace(RESET, RESET + PANEL_BACKGROUND) + RESET : row;
    }

    // "... skada: 14" → skadan i fet gul
    private static String damage(String t) {
        Matcher m = DAMAGE.matcher(t);
        if (!m.find()) return t;
        return t.substring(0, m.start()) + "  " + YELLOW + BOLD + "−" + m.group(1) + RESET;
    }

    // ── Menyerna och kartan (GameEngine skriver dem direkt) ──

    // Varje rad i en meny: "1. Strid" med siffran i fet gul (ingen ruta), rubriken och "=====" i cyan
    public static String menu(String block) {
        if (!enabled || block == null) return block;
        StringBuilder out = new StringBuilder();
        String[] lines = block.split("\n", -1);
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            boolean header = i + 1 < lines.length && lines[i + 1].matches("=+");
            Matcher opt = OPTION.matcher(line);
            if (line.matches("=+")) out.append(CYAN).append(line).append(RESET);
            else if (header) out.append(CYAN).append(BOLD).append(line).append(RESET);
            else if (opt.matches()) out.append(YELLOW).append(BOLD).append(opt.group(1)).append(".").append(RESET)
                    .append(" ").append(opt.group(2));
            else out.append(line);
            if (i + 1 < lines.length) out.append("\n");
        }
        return tint(out.toString());
    }

    // Ram runt en meny. Rubriken och "=====" blir ramens titel. Webben får texten som den är.
    public static String panel(String block) {
        if (!enabled || block == null) return block;
        List<String> lines = block.stripTrailing().lines().toList();
        boolean titled = lines.size() > 1 && plain(lines.get(1)).matches("=+");
        return titled ? box(plain(lines.getFirst()), lines.subList(2, lines.size())) : box("", lines);
    }

    // Ram med en egen titel, t.ex. KARTA
    public static String panel(String block, String title) {
        if (!enabled || block == null) return block;
        return box(title, block.stripTrailing().lines().toList());
    }

    private static String box(String title, List<String> lines) {
        int width = Math.max(title.length() + 4, lines.stream().mapToInt(line -> plain(line).length()).max().orElse(0) + 2);
        StringBuilder out = new StringBuilder(top(title.toUpperCase(), width, "┐")).append("\n");
        for (String line : lines) {
            String padding = " ".repeat(width - 2 - plain(line).length());
            out.append(onPanel(DIM + "│ " + RESET + line + padding + DIM + " │" + RESET)).append("\n");
        }
        return tint(out.append(DIM).append("└").append("─".repeat(width)).append("┘").append(RESET).toString());
    }

    private static String top(String title, int width, String corner) {
        if (title.isEmpty()) return DIM + "┌" + "─".repeat(width) + corner + RESET;
        return DIM + "┌─ " + RESET + CYAN + BOLD + title + RESET + DIM + " " + "─".repeat(width - title.length() - 3) + corner + RESET;
    }

    // Kartan: @ du är här i gult, x rensat i grönt, ! fiender i rött, ? nytt i cyan, # låst nedtonat
    public static String map(String map) {
        if (!enabled || map == null) return map;
        Matcher m = MARK.matcher(map);
        StringBuilder out = new StringBuilder();
        while (m.find()) {
            String color = switch (m.group(1)) {
                case "@" -> YELLOW + BOLD;
                case "x" -> GREEN + BOLD;
                case "!" -> RED + BOLD;
                case "?" -> CYAN + BOLD;
                default -> DIM;
            };
            m.appendReplacement(out, Matcher.quoteReplacement(color + "[" + m.group(1) + "]" + RESET));
        }
        m.appendTail(out);
        return tint(out.toString());
    }

    // Frågan längst ner: fet, så man ser var man skriver
    public static String prompt(String text) {
        if (!enabled || text == null) return text;
        return tint(BOLD + WHITE + text + RESET);
    }

    // Bilderna (ASCII): fet stil så att linjerna syns tydligt, och webbens bildfärg om terminalen klarar den.
    // En bild med egen färg, t.ex. den röda GAME OVER, behåller sin.
    public static String art(String line) {
        if (line == null || line.isBlank()) return line;
        return BOLD + (trueColor ? ART : "") + line + RESET;
    }
}
