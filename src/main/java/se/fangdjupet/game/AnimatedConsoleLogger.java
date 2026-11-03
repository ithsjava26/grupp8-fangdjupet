package se.fangdjupet.game;

import se.fangdjupet.combat.AttackStrategy;
import se.fangdjupet.combat.MagicAttack;
import se.fangdjupet.combat.RangedAttack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Konsolen med rörelse (Observer, som ConsoleEventLogger): bilderna ritas fram rad för rad, repliker
// skrivs bokstav för bokstav, och i striden spelas små filmer upp på en rad innan raden skrivs:
// pilen flyger, magin sprakar, svärdet svingar, fienden smäller till och hp-mätaren tappar steg för steg.
// Filmerna ritas med \r (tillbaka till radens början), som IntelliJ och terminaler klarar – sedan
// suddas de, och den vanliga raden skrivs som förut. Av med -Dfangdjupet.animation=av (webben animerar själv).
public class AnimatedConsoleLogger implements GameEventListener {
    static final int LINE_MS = 22;    // paus efter varje rad i en bild
    static final int CHAR_MS = 14;    // paus efter varje bokstav i en replik
    static final int FRAME_MS = 38;   // paus mellan bilderna i en film
    static final int DOT_MS = 170;    // paus mellan prickarna när man letar
    static final int WIDTH = 44;      // så bred är filmen på raden

    private static final String YELLOW = "\u001B[33m", RED = "\u001B[31m", CYAN = "\u001B[36m",
            MAGENTA = "\u001B[35m", GREEN = "\u001B[32m", RESET = "\u001B[0m";
    private static final Pattern HP_BAR = Pattern.compile("(.+?)  \\[([#-]+)\\] (\\d+)/(\\d+)");

    private final Consumer<String> print;   // skriver utan radbrytning
    private final IntConsumer pause;        // väntar så många millisekunder
    private Supplier<Optional<AttackStrategy>> heroAttack = Optional::empty;   // magi eller pil? (båda "skjuter")
    private final Map<String, Integer> lastHp = new HashMap<>();              // för hp-mätarens film

    public AnimatedConsoleLogger() {
        this(IO::print, AnimatedConsoleLogger::sleep);
    }

    // För testerna: fånga utskriften och pauserna i stället för att vänta på riktigt
    AnimatedConsoleLogger(Consumer<String> print, IntConsumer pause) {
        if (print == null || pause == null) throw new IllegalArgumentException("print and pause must not be null");
        this.print = print;
        this.pause = pause;
    }

    // Vilken attack hjälten har i handen just nu (GameEngine.heroAttack): pilen och magin ser olika ut
    public AnimatedConsoleLogger followAttack(Supplier<Optional<AttackStrategy>> heroAttack) {
        if (heroAttack == null) throw new IllegalArgumentException("heroAttack must not be null");
        this.heroAttack = heroAttack;
        return this;
    }

    // Animation på, om den inte stängts av
    public static boolean enabled() {
        String off = System.getProperty("fangdjupet.animation", System.getenv("FANGDJUPET_ANIMATION"));
        return off == null || !off.equalsIgnoreCase("av");
    }

    @Override
    public void onEvent(String message) {
        if (message == null) return;
        String t = message.replaceAll("\u001B\\[[0-9;]*m", "");   // utan färgkoder, för att känna igen raden
        if (message.contains("\n")) {
            drawLineByLine(message);
            return;
        }
        if (t.matches("[^:\"]{1,30}: \".*\"")) {
            typeSpeech(message);
            return;
        }
        Matcher bar = HP_BAR.matcher(t);
        if (bar.matches()) {
            drainHpBar(bar.group(1), bar.group(2).length(), Integer.parseInt(bar.group(3)), Integer.parseInt(bar.group(4)));
        } else if (t.startsWith("Du letar i ") && t.endsWith(" ...")) {
            searchDots(message);
            return;
        } else if (t.startsWith("Hero: ")) {
            play(heroFilm(t));
        } else if (t.startsWith("Enemy: ")) {
            play(t.contains(" missar ") ? whoosh() : impact(enemyWord(t)));
        } else if (t.startsWith("Fiende ") && t.endsWith(" dyker upp!")) {
            play(alarm(t.contains("Martin")));
        } else if (t.matches(".+ vinner (mot .+|Fångdjupet)!")) {
            play(stars());
        } else if (t.startsWith("Du hittar") && !t.startsWith("Du hittar inget")) {
            play(sparkle());
        } else if (t.contains("klämmer stressbollen")) {
            play(squeeze());
        }
        print.accept(message + "\n");
    }

    // ── Bilder och repliker ──

    private void drawLineByLine(String art) {
        for (String line : art.split("\n", -1)) {
            print.accept(line + "\n");
            pause.accept(LINE_MS);
        }
    }

    private void typeSpeech(String message) {
        int start = message.indexOf('"') + 1;
        print.accept(message.substring(0, start));
        for (int i = start; i < message.length() - 1; i++) {
            print.accept(String.valueOf(message.charAt(i)));
            pause.accept(CHAR_MS);
        }
        print.accept("\"\n");
    }

    private void searchDots(String message) {
        print.accept(message.substring(0, message.length() - 4) + " ");
        for (int i = 0; i < 3; i++) {
            pause.accept(DOT_MS);
            print.accept(".");
        }
        print.accept("\n");
    }

    // ── Filmerna: en lista med bilder som ritas på samma rad ──

    private void play(List<String> frames) {
        if (frames.isEmpty()) return;
        for (String frame : frames) {
            print.accept("\r" + frame + RESET);
            pause.accept(FRAME_MS);
        }
        print.accept("\r" + " ".repeat(WIDTH + 12) + "\r");   // sudda filmen, sedan kommer den vanliga raden
    }

    // Hjältens slag: filmen efter vad som hände
    List<String> heroFilm(String t) {
        if (t.contains("slinter på sidan")) return slip();   // före missen: raden säger också "missar"
        if (t.contains(" missar ") || t.contains("kastar stressbollen förbi")) return whoosh();
        if (t.contains("delar skärmen")) return screenShare();
        if (t.contains("kastar stressbollen")) return ballThrow();
        if (t.contains(" citerar ")) return flying(CYAN, "{;}", "  RTFM!");
        if (t.contains(" skjuter ")) {
            AttackStrategy attack = heroAttack.get().orElse(null);
            if (attack instanceof MagicAttack) return magic();
            if (attack instanceof RangedAttack) return flying(YELLOW, ">>---->", "  THWACK!");
            return flying(CYAN, "?!", "  !!");   // kunskap mot Martin (DoomScroll, quiz, StackOverflow)
        }
        return swordSwing();
    }

    // Något som flyger från vänster till höger, och ett ord när det träffar
    static List<String> flying(String color, String thing, String hit) {
        List<String> frames = new ArrayList<>();
        for (int x = 0; x + thing.length() <= WIDTH; x += 4) frames.add(color + " ".repeat(x) + thing);
        frames.add(color + " ".repeat(WIDTH - thing.length()) + thing + hit);
        frames.add(color + " ".repeat(WIDTH - thing.length()) + thing + hit);
        return frames;
    }

    static List<String> magic() {
        List<String> frames = new ArrayList<>();
        String[] sparks = {"*", "+*+", "*+*+*", "~*+*+*~", "*~*+*+*~*", "+*~*+*+*~*+"};
        for (int i = 0; i < sparks.length; i++) frames.add(MAGENTA + " ".repeat(WIDTH / 2 - i) + sparks[i]);
        frames.add(MAGENTA + " ".repeat(WIDTH / 2 - 6) + "*  Z A P !  *");
        frames.add(MAGENTA + " ".repeat(WIDTH / 2 - 6) + "*  Z A P !  *");
        return frames;
    }

    static List<String> swordSwing() {
        String at = " ".repeat(WIDTH / 2);
        return List.of(YELLOW + at + "  /", YELLOW + at + "  |", YELLOW + at + "  \\", YELLOW + at + "o==|====>",
                YELLOW + at + "o==|====>  SLASH!", YELLOW + at + "o==|====>  SLASH!");
    }

    static List<String> ballThrow() {
        List<String> frames = new ArrayList<>();
        String[] ball = {".", "o", "O", "o", "O", "o", "O", "o", "."};
        for (int i = 0; i < ball.length; i++) frames.add(YELLOW + " ".repeat(4 + i * 4) + ball[i]);
        frames.add(YELLOW + " ".repeat(36) + "(@)  SQUISH!");
        frames.add(YELLOW + " ".repeat(36) + "(@)  SQUISH!");
        return frames;
    }

    static List<String> screenShare() {
        String[] typed = {"> ", "> j", "> ja", "> jav", "> java", "> java Hello!", "> java Hello!  +hp"};
        List<String> frames = new ArrayList<>();
        for (String s : typed) frames.add(GREEN + "  [" + s + " ".repeat(Math.max(0, 20 - s.length())) + "]");
        return frames;
    }

    static List<String> slip() {
        return List.of(YELLOW + "  [KAP]", YELLOW + "    [KAP]/", YELLOW + "       \\[KAP]", YELLOW + "          ~wiiiuuu~",
                YELLOW + "                 ...duns.", YELLOW + "                 ...duns.");
    }

    static List<String> whoosh() {
        List<String> frames = new ArrayList<>();
        for (int x = 0; x < WIDTH - 10; x += 6) frames.add(CYAN + " ".repeat(x) + "~ ~ ~");
        frames.add(CYAN + " ".repeat(WIDTH - 10) + "~  MISS!");
        frames.add(CYAN + " ".repeat(WIDTH - 10) + "~  MISS!");
        return frames;
    }

    // Fiendens smäll: växer fram och blir ett ord
    static List<String> impact(String word) {
        String at = " ".repeat(WIDTH / 2 - 4);
        return List.of(RED + at + "   *", RED + at + "  ***", RED + at + " *****", RED + at + "* " + word + " *",
                RED + at + "* " + word + " *", RED + at + "  " + word);
    }

    // Varje fiende smäller på sitt sätt (samma som SoundListener.enemySound)
    static String enemyWord(String t) {
        if (t.startsWith("Enemy: Trollkarl")) return "ZAP!";
        if (t.startsWith("Enemy: Troll")) return "BONK!";
        if (t.contains("tvättsvamp")) return "SPLAT!";
        if (t.contains("Svinto")) return "PSSST!";
        if (t.contains("kemikalier") || t.contains("Vanish")) return "FZZZ!";
        if (t.contains("Martin")) return "BLA BLA BLA!";
        return "BAM!";
    }

    static List<String> alarm(boolean boss) {
        String color = boss ? RED : YELLOW;
        String word = boss ? "!!  B O S S  !!" : "!!!";
        return List.of(color + "  !", color + "  ! !", color + "  ! ! !", "", color + "  " + word, "", color + "  " + word);
    }

    static List<String> stars() {
        return List.of(YELLOW + "  *   .   *   .   *", YELLOW + "  .   *   .   *   .", YELLOW + "  *   .   *   .   *",
                YELLOW + "  .   *   .   *   .", YELLOW + "  * + * + * + * + *");
    }

    static List<String> sparkle() {
        return List.of(YELLOW + "  .", YELLOW + "  . o", YELLOW + "  . o O", YELLOW + "  . o O *", YELLOW + "  . o O * !");
    }

    static List<String> squeeze() {
        return List.of(GREEN + "  ( o )", GREEN + "  (-o-)", GREEN + "  )-o-(", GREEN + "  (-o-)  iiik!", GREEN + "  ( o )  +hp");
    }

    // Hp-mätaren tappar (eller fylls på) steg för steg från förra värdet
    private void drainHpBar(String name, int width, int hp, int max) {
        Integer before = lastHp.put(name, hp);
        if (before == null || before == hp || max <= 0) return;
        List<String> frames = new ArrayList<>();
        int steps = Math.min(8, Math.abs(before - hp));
        for (int i = 1; i < steps; i++) {
            int now = before + (hp - before) * i / steps;
            frames.add((hp < before ? RED : GREEN) + name + "  " + bar(now, max, width) + " " + now + "/" + max);
        }
        play(frames);
    }

    // Samma stapel som HpBar.render
    static String bar(int hp, int max, int width) {
        int filled = (int) ((long) hp * width / max);
        if (hp > 0 && filled == 0) filled = 1;
        return "[" + "#".repeat(filled) + "-".repeat(width - filled) + "]";
    }

    private static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
