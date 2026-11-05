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
    static final int FILM_MS = 65;    // paus mellan bilderna i vinstfilmen

    private static final String YELLOW = "\u001B[33m", RED = "\u001B[31m", CYAN = "\u001B[36m",
            MAGENTA = "\u001B[35m", GREEN = "\u001B[32m", RESET = "\u001B[0m";
    private static final String FLASH = "\u001B[7m";   // omvända färger: hela raden blinkar till
    private static final String HIDE_CURSOR = "\u001B[?25l", SHOW_CURSOR = "\u001B[?25h";
    private static final String GEAR_TITLE = "Visa hjältens utrustning.";
    private static final Pattern HP_BAR = Pattern.compile("(.+?)  \\[([#-]+)\\] (\\d+)/(\\d+)");
    private static final Pattern GEAR_ROW = Pattern.compile("(Vapen|Rustning|Attack|Java-boken|Stressboll|Ryggsäck): .+");
    private static final Pattern DAMAGE = Pattern.compile(" skada: (\\d+)$");

    private final Consumer<String> print;   // skriver utan radbrytning
    private final IntConsumer pause;        // väntar så många millisekunder
    private Supplier<Optional<AttackStrategy>> heroAttack = Optional::empty;   // magi eller pil? (båda "skjuter")
    private final Map<String, Integer> lastHp = new HashMap<>();              // för hp-mätarens film
    private final boolean style;            // tydligare grafik med färger och block (ConsoleStyle) – av i testerna
    private Supplier<Optional<String>> heroName = Optional::empty;   // vems hp-mätare som är hjältens
    private String heroBar;                 // stridspanelen: hjältens och fiendens senaste hp-rad
    private final Map<String, String> enemyBars = new HashMap<>();   // senaste hp-raden för varje fiende
    private String foe;   // fienden i panelen: den som slogs sist
    private boolean inGearPanel;            // mellan rubriken och sista raden i "Visa hjältens utrustning"
    private boolean redraw;                 // går det att hoppa upp och rita om rader (en riktig terminal)?

    public AnimatedConsoleLogger() {
        this(IO::print, AnimatedConsoleLogger::sleep, true);
        java.io.Console console = System.console();
        redraw = console != null && console.isTerminal();
    }

    // För testerna: fånga utskriften och pauserna i stället för att vänta på riktigt
    AnimatedConsoleLogger(Consumer<String> print, IntConsumer pause) {
        this(print, pause, false);
    }

    // style = true: raderna färgas och hp-mätarna ritas med block (ConsoleStyle)
    AnimatedConsoleLogger(Consumer<String> print, IntConsumer pause, boolean style) {
        if (print == null || pause == null) throw new IllegalArgumentException("print and pause must not be null");
        this.print = style ? text -> print.accept(ConsoleStyle.tint(text)) : print;
        this.pause = pause;
        this.style = style;
    }

    // För testerna: låtsas att konsolen kan rita om rader
    AnimatedConsoleLogger redrawing() {
        redraw = true;
        return this;
    }

    // Hjältens namn (GameEngine.heroName): då hamnar hjälten till vänster i stridspanelen
    public AnimatedConsoleLogger followHero(Supplier<Optional<String>> heroName) {
        if (heroName == null) throw new IllegalArgumentException("heroName must not be null");
        this.heroName = heroName;
        return this;
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
        Matcher bar = HP_BAR.matcher(t);
        inGearPanel = t.equals(GEAR_TITLE) || inGearPanel && (bar.matches() || GEAR_ROW.matcher(t).matches());
        if (message.contains("\n")) {
            drawLineByLine(message);
            return;
        }
        if (t.matches("[^:\"]{1,30}: \".*\"")) {
            boolean shallNotPass = t.endsWith("\"" + WorldBuilder.SHALL_NOT_PASS + "\"");
            if (shallNotPass) play(lightning());
            typeSpeech(message);
            if (shallNotPass) staffStrike();
            return;
        }
        if (bar.matches()) {
            drainHpBar(bar.group(1), bar.group(2).length(), Integer.parseInt(bar.group(3)), Integer.parseInt(bar.group(4)));
            if (style) {
                print.accept(hpRow(t, bar.group(1), bar.group(3)) + "\n");
                return;
            }
        } else if (t.startsWith("Du går till ")) {
            play(wipe());
        } else if (t.startsWith("Du letar i ") && t.endsWith(" ...")) {
            searchDots(message);
            return;
        } else if (t.startsWith("Hero: ")) {
            play(withDamage(heroFilm(t), t));
        } else if (t.startsWith("Enemy: ")) {
            foe = attacker(t);
            play(t.contains(" missar ") ? whoosh() : withDamage(impact(enemyWord(t)), t));
        } else if (t.startsWith("Fiende ") && t.endsWith(" dyker upp!")) {
            forgetFight();
            play(alarm(t.contains("Martin")));
        } else if (t.endsWith(" vinner Fångdjupet!")) {
            forgetFight();
            spinTrophy();
        } else if (t.matches(".+ vinner mot .+!")) {
            forgetFight();
            play(stars());
        } else if (t.contains("besegrades av")) {
            forgetFight();
            play(knockOut());
        } else if (t.matches(".*\\(\\+\\d+ hp\\).*")) {
            play(glow());
        } else if (t.startsWith("Du hittar") && !t.startsWith("Du hittar inget")) {
            play(sparkle());
        } else if (t.contains("klämmer stressbollen")) {
            play(squeeze());
        }
        print.accept((style ? ConsoleStyle.line(message) : message) + "\n");
    }

    // ── Stridspanelen ──

    // En hp-rad: i striden hjälten och fienden sida vid sida, med den andras senaste värde. Annars raden som den är.
    private String hpRow(String line, String name, String hp) {
        if (inGearPanel) return ConsoleStyle.DIM + "│ " + ConsoleStyle.RESET + ConsoleStyle.line(line);
        if (heroName.get().filter(name::equals).isPresent()) heroBar = line;
        else {
            enemyBars.put(name, line);
            foe = name;
        }
        String enemyBar = foe == null ? null : enemyBars.get(foe);
        String row = heroBar == null || enemyBar == null ? ConsoleStyle.line(line) : ConsoleStyle.battle(heroBar, enemyBar);
        if (hp.equals("0") && name.equals(foe)) {   // fienden är borta: inte kvar i nästa panel
            enemyBars.remove(foe);
            foe = null;
        }
        return row;
    }

    // Fienden som slår nu. Saknar den hp-rad visar panelen bara hjälten, inte en annan fiendes rad.
    private String attacker(String line) {
        String rest = line.substring("Enemy: ".length());
        return enemyBars.keySet().stream().filter(name -> rest.startsWith(name + " ")).findFirst().orElse(null);
    }

    private void forgetFight() {
        heroBar = null;
        enemyBars.clear();
        foe = null;
    }

    // ── Bilder och repliker ──

    private void drawLineByLine(String art) {
        for (String line : art.split("\n", -1)) {
            print.accept((style ? ConsoleStyle.art(line) : line) + "\n");
            pause.accept(LINE_MS);
        }
    }

    private void typeSpeech(String message) {
        int start = message.indexOf('"') + 1;
        String who = message.substring(0, start);              // "Lektor Martin: \""
        if (style) who = ConsoleStyle.MAGENTA + ConsoleStyle.BOLD + who.substring(0, who.length() - 3) + ":"
                + ConsoleStyle.RESET + " " + ConsoleStyle.WHITE + "\"";
        print.accept(who);
        for (int i = start; i < message.length() - 1; i++) {
            print.accept(String.valueOf(message.charAt(i)));
            pause.accept(CHAR_MS);
        }
        print.accept("\"" + (style ? ConsoleStyle.RESET : "") + "\n");
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
            // Fyll ut raden, så att inget ligger kvar från en bredare bild
            int rest = Math.max(0, WIDTH + 12 - ConsoleStyle.plain(frame).length());
            print.accept("\r" + frame + RESET + " ".repeat(rest));
            pause.accept(FRAME_MS);
        }
        print.accept("\r" + " ".repeat(WIDTH + 12) + "\r");   // sudda filmen, sedan kommer den vanliga raden
    }

    // Vinsten: skolan och pokalen snurrar i 3D (Ascii3d). Filmen ritar om flera rader, och det klarar en
    // terminal men inte IntelliJs konsol. Där visas bara sista bilden, och den står kvar i båda fallen.
    private void spinTrophy() {
        List<String> frames = Ascii3d.film();
        if (!redraw) {
            print.accept(frames.getLast() + "\n");
            return;
        }
        print.accept(HIDE_CURSOR);
        for (int i = 0; i < frames.size(); i++) {
            if (i > 0) print.accept("\u001B[" + Ascii3d.HEIGHT + "A\r");   // tillbaka upp till bildens första rad
            print.accept(frames.get(i) + "\n");
            pause.accept(FILM_MS);
        }
        print.accept(SHOW_CURSOR);
    }

    // Hjältens slag: filmen efter vad som hände
    List<String> heroFilm(String t) {
        if (t.contains("blir påkommen av")) return flying(RED, "[USB]", "  PÅKOMMEN!");        // USB-minnet: Martin ser det
        if (t.contains("krossar lavalampan")) return flying(MAGENTA, "o==[]", "  KRASCH!");      // lavalampan går sönder
        if (t.contains("smyger in ett USB-minne")) return flying(GREEN, "[USB]>", "  VIRUS!");  // viruset tar över datorn
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

    // Fiendens smäll: raden blinkar till, smällen växer fram och ordet skakar
    static List<String> impact(String word) {
        String at = " ".repeat(WIDTH / 2 - 4);
        String hit = "* " + word + " *";
        return List.of(flash(RED), RED + at + "   *", RED + at + "  ***", RED + at + " *****", RED + at + hit,
                RED + at.substring(3) + hit, RED + at + "   " + hit, RED + at.substring(2) + hit, RED + at + " " + hit,
                RED + at + hit, RED + at + "  " + word);
    }

    // Hela raden blinkar till i färgen
    static String flash(String color) {
        return color + FLASH + " ".repeat(WIDTH + 12);
    }

    // Skadan stiger ur filmen, som siffrorna på webben: "−14"
    static List<String> withDamage(List<String> film, String t) {
        Matcher damage = DAMAGE.matcher(t);
        if (!damage.find()) return film;
        List<String> frames = new ArrayList<>(film);
        String number = "−" + damage.group(1);
        String at = " ".repeat(WIDTH / 2);
        frames.add(YELLOW + at + ".  " + number);
        frames.add(YELLOW + at + "   " + number + "  .");
        frames.add(YELLOW + at + "      " + number);
        return frames;
    }

    // Ett svep över raden när hjälten går till ett annat rum
    static List<String> wipe() {
        List<String> frames = new ArrayList<>();
        for (int x = 0; x <= WIDTH; x += 8) frames.add(CYAN + "░".repeat(x) + "▒▓");
        for (int x = 0; x <= WIDTH; x += 11) frames.add(CYAN + " ".repeat(x) + "░".repeat(WIDTH - x));
        return frames;
    }

    // Hjälten faller: raden blinkar, och bokstäverna glider isär
    static List<String> knockOut() {
        return List.of(flash(RED), RED + "  K.O.", RED + "  K . O .", RED + "  K  .  O  .", RED + "  K  .  O  .");
    }

    // Hp kommer tillbaka: något grönt stiger
    static List<String> glow() {
        return List.of(GREEN + "  +", GREEN + "  + +", GREEN + "  + + +", GREEN + "  + + +  hp!");
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

    // Trollkarlens replik: blixten slår ner två gånger. Bara två blinkningar, så att det inte flimrar.
    static List<String> lightning() {
        String white = FLASH + " ".repeat(WIDTH + 12);
        String bolt = YELLOW + "    __/\\  /\\__/\\  /\\__/\\  /\\__/\\  /\\__";
        return List.of(white, white, bolt, bolt, "", "", "", "", "", "", white, white, bolt, bolt, "", "");
    }

    // Staven slår i golvet: en våg rullar ut från mitten
    static List<String> shockwave() {
        List<String> frames = new ArrayList<>();
        for (int i = 0; i <= 12; i++) {
            frames.add(CYAN + " ".repeat(24 - i) + "(".repeat(i) + "|" + ")".repeat(i));
        }
        return frames;
    }

    // Efter repliken: vågen, och sedan står orden kvar stort. Loggfilen och webben får bara repliken.
    private void staffStrike() {
        play(shockwave());
        print.accept(RED + "  ==========================================\n"
                + "    T H O U   S H A L L   N O T   P A S S !\n"
                + "  ==========================================" + RESET + "\n");
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
            String frame = name + "  " + bar(now, max, width) + " " + now + "/" + max;
            frames.add(style ? ConsoleStyle.line(frame) : (hp < before ? RED : GREEN) + frame);
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
