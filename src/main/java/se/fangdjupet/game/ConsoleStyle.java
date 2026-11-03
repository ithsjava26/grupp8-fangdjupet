package se.fangdjupet.game;

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
    private static final java.util.List<String> ROOMS =
            java.util.List.of("Skoldörren", "Kafferummet", "Korridoren och städskrubben", "Lektionssalen");

    // Påslaget i Main när konsolen animeras – webben kör spelet utan, och får vanlig text
    private static boolean enabled;

    public static void enable(boolean on) {
        enabled = on;
    }

    public static boolean enabled() {
        return enabled;
    }

    // ── En rad från spelet (EventBus) ──

    public static String line(String t) {
        if (t == null || t.isEmpty() || t.contains("\u001B[")) return t;   // redan färgad (gult, rött): som den är
        Matcher bar = HP_BAR.matcher(t);
        if (bar.matches()) return hpBar(bar.group(1), bar.group(2), bar.group(3), bar.group(4));
        if (t.startsWith("Hero: ")) return GREEN + BOLD + "» " + RESET + WHITE + damage(t.substring(6)) + RESET;
        if (t.startsWith("Enemy: ")) return RED + BOLD + "« " + RESET + RED + damage(t.substring(7)) + RESET;
        if (t.startsWith("Fiende ") && t.endsWith(" dyker upp!")) return RED + BOLD + "!! " + t + RESET;
        if (t.matches(".+ vinner (mot .+|Fångdjupet)!")) return GREEN + BOLD + "★ " + t + " ★" + RESET;
        if (t.contains("besegrades av") || t.contains("dog av sin egen attack")) return RED + BOLD + t + RESET;
        if (t.contains("blir arg!") || t.contains("skrattar elakt")) return MAGENTA + BOLD + t + RESET;
        if (t.startsWith("Du hittar") && !t.startsWith("Du hittar inget")) return YELLOW + BOLD + "✦ " + t + RESET;
        if (t.contains(" klämmer stressbollen") || t.matches(".*\\(\\+\\d+ hp\\).*")) return GREEN + t + RESET;
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

    // "... skada: 14" → skadan i fet gul
    private static String damage(String t) {
        Matcher m = DAMAGE.matcher(t);
        if (!m.find()) return t;
        return t.substring(0, m.start()) + "  " + YELLOW + BOLD + "−" + m.group(1) + RESET;
    }

    // ── Menyerna och kartan (GameEngine skriver dem direkt) ──

    // Varje rad i en meny: "1. Strid" → "[1] Strid" med siffran i gult, rubriken och "=====" i cyan
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
            else if (opt.matches()) out.append(YELLOW).append(BOLD).append("[").append(opt.group(1)).append("]").append(RESET)
                    .append(" ").append(opt.group(2));
            else out.append(line);
            if (i + 1 < lines.length) out.append("\n");
        }
        return out.toString();
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
        return out.toString();
    }

    // Frågan längst ner: fet, så man ser var man skriver
    public static String prompt(String text) {
        if (!enabled || text == null) return text;
        return BOLD + WHITE + text + RESET;
    }

    // Bilderna (ASCII): fet stil så att linjerna syns tydligt, färgen lämnas som den är
    public static String art(String line) {
        if (line == null || line.isBlank()) return line;
        return BOLD + line + RESET;
    }
}
