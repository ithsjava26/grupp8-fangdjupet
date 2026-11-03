package se.fangdjupet.sound;

import se.fangdjupet.combat.AttackStrategy;
import se.fangdjupet.combat.MagicAttack;
import se.fangdjupet.combat.MeleeAttack;
import se.fangdjupet.combat.RangedAttack;
import se.fangdjupet.game.GameEventListener;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

// Observer: lyssnar på spelets händelser som konsolen och loggfilen gör, och spelar musik och ljud till dem.
// Varje rum har sin låt, striderna har stridsmusik (Martin sin egen, StädHäxan en snabbare klibbig),
// och slagen låter efter hjältens attack. Samma regler som webbversionen (fangdjupet-web, index.html).
public final class SoundListener implements GameEventListener {

    static final List<String> ROOMS = List.of("Skoldörren", "Kafferummet", "Korridoren och städskrubben", "Lektionssalen");
    static final List<String> ROOM_SONGS = List.of("door", "coffee", "sticky", "lecture");   // städskrubben: klibbig

    private final Speaker speaker;
    private final Supplier<Optional<AttackStrategy>> heroAttack;   // magi och avstånd skriver båda "skjuter"
    private String roomSong = "title";
    private boolean inFight;
    private boolean expectHeroBar;   // efter fiendens slag kommer hjältens hp-mätare

    // "Robert  [###-------] 12/40"
    private static final java.util.regex.Pattern HP_BAR = java.util.regex.Pattern.compile("(.+?)\\s+\\[[#-]+\\]\\s+(\\d+)/(\\d+)\\s*");

    public SoundListener(Speaker speaker, Supplier<Optional<AttackStrategy>> heroAttack) {
        if (speaker == null) throw new IllegalArgumentException("speaker cannot be null");
        if (heroAttack == null) throw new IllegalArgumentException("heroAttack cannot be null");
        this.speaker = speaker;
        this.heroAttack = heroAttack;
        speaker.music(roomSong);   // huvudmenyn
    }

    // Spelaren tryckte Enter: ett litet blipp
    public void keyPressed() {
        speaker.sfx("blip");
    }

    @Override
    public void onEvent(String message) {
        if (message == null) return;
        String t = message.replaceAll("\u001B\\[[0-9;]*m", "");   // utan färgkoder (gult, rött)

        java.util.regex.Matcher bar = HP_BAR.matcher(t);
        boolean heroBar = expectHeroBar;
        expectHeroBar = false;
        if (bar.matches()) {
            // Hjältens hp-mätare efter fiendens slag: varna när en fjärdedel eller mindre är kvar
            int hp = Integer.parseInt(bar.group(2)), max = Integer.parseInt(bar.group(3));
            if (heroBar && hp > 0 && hp * 4 <= max) speaker.sfx("lowhp");
        } else if (t.startsWith("Hero: ")) {
            inFight = true;
            speaker.sfx(heroSound(t));
        } else if (t.startsWith("Enemy: ")) {
            inFight = true;
            expectHeroBar = true;
            speaker.sfx(t.contains(" missar ") ? "whoosh" : enemySound(t));
        } else if (t.contains("klämmer stressbollen") || t.startsWith("Du stoppar Stressboll")) {
            speaker.sfx("squeak");
        } else if (t.startsWith("Du har nu ") && t.contains(" i handen")) {
            speaker.sfx("clang");
        } else if (t.startsWith("Du tar på dig")) {
            speaker.sfx("clank");
        } else if (t.contains("kanelbulle") && t.matches(".*\\(\\+\\d+ hp\\).*")) {
            speaker.sfx("munch");
        } else if (t.endsWith(" vinner Fångdjupet!")) {
            inFight = false;
            roomSong = "title";
            speaker.jingle("final", "title");
        } else if (t.matches(".+ vinner mot .+!")) {
            inFight = false;
            speaker.jingle("win", roomSong);
        } else if (t.contains("UTTERLY EXPLAINED")) {
            speaker.music("bossFast");   // Martins fas 2: musiken går upp i tempo (samma låt spelas inte om)
        } else if (t.startsWith("Fiende ") && t.endsWith(" dyker upp!")) {
            speaker.sfx("alert");
            speaker.music(t.contains("Martin") ? "boss" : roomSong.equals("sticky") ? "stickyFight" : "battle");
        } else if (t.contains("besegrades av") || t.contains("dog av sin egen attack")) {
            inFight = false;
            roomSong = "title";
            speaker.jingle("lose", "title");   // sedan huvudmenyn
        } else if (t.contains("skrattar elakt")) {
            speaker.sfx("laugh");
        } else if (t.startsWith("Men vänta...") || t.contains("blir arg!")) {
            speaker.sfx("alert");
        } else if (ROOMS.contains(t)) {
            roomSong = ROOM_SONGS.get(ROOMS.indexOf(t));
            if (!inFight) speaker.music(roomSong);
        } else if (t.startsWith("Entrédörren slår igen")) {
            speaker.sfx("slam");
        } else if (t.matches(".*\\(\\+\\d+ hp\\).*") || t.contains("dricker") || t.contains("tar en ")) {
            speaker.sfx("gulp");
        } else if (t.startsWith("Du hittar") && !t.startsWith("Du hittar inget")) {
            speaker.sfx("find");
        } else if (t.startsWith("Du plockar upp:")) {
            speaker.sfx("pickup");
        } else {
            String other = otherSound(t);
            if (other != null) speaker.sfx(other);
        }
    }

    // Allt utanför striden som inte har ett ljud ovan: gå, leta, spara, fel val, Frida, repliker ...
    static String otherSound(String t) {
        if (t.startsWith("Du går till ")) return "steps";
        if (t.startsWith("Du letar i ")) return "rummage";
        if (t.equals("Du hittar inget här.")) return "nope";
        if (t.startsWith("Äventyret är sparat")) return "save";
        if (t.startsWith("Ogiltigt val") || t.startsWith("Kunde inte spara") || t.startsWith("Sparfilen går inte")
                || t.equals("Starta ett äventyr först.") || t.startsWith("Det finns ingen") || t.startsWith("Det finns inget sparat")
                || t.startsWith("Du är redan i ")) return "buzz";
        if (t.endsWith(" dyker upp!")) return "sparkle";            // Frida (fienderna har fått sitt ljud ovan)
        if (t.equals("Äventyret börjar!") || t.startsWith("Välkommen tillbaka")) return "fanfare";
        if (t.startsWith("Du byter till ")) return "swap";
        if (t.contains(" är låst.") || t.contains(" står i vägen")) return "locked";
        if (t.startsWith("Du låter ")) return "drop";
        if (t.equals("Visa hjältens utrustning.")) return "bag";
        if (t.equals("Tack för att du spelade!")) return "bye";
        if (t.matches("[^:\"]{1,30}: \".*")) return t.startsWith(se.fangdjupet.combat.Martin.NAME) ? "blah" : "talk";   // Namn: "replik"
        return null;
    }

    // Hjältens rad: boken bläddrar (eller slinter), Dela skärm klingar, stressbollen säger boing – annars attackens ljud
    private String heroSound(String t) {
        if (t.contains("slinter på sidan")) return "slip";
        if (t.contains(" missar ") || t.contains("kastar stressbollen förbi")) return "whoosh";
        if (t.contains("delar skärmen")) return "chime";
        if (t.contains("kastar stressbollen på")) return "boing";
        if (t.contains(" citerar ")) return "page";
        return attackSound();
    }

    // Fiendens slag: varje fiende låter på sitt sätt. Okända fiender låter som en vanlig smäll.
    static String enemySound(String t) {
        if (t.startsWith("Enemy: Trollkarl")) return "zap";
        if (t.startsWith("Enemy: Troll")) return "club";
        if (t.contains("tvättsvamp")) return "splat";
        if (t.contains("Svinto")) return "hiss";
        if (t.contains("kemikalier")) return "fizz";
        if (t.startsWith("Enemy: " + se.fangdjupet.combat.Martin.NAME)) return "blah";
        return "hurt";
    }

    // Ljudet för hjältens slag: ett fult krossat "PJOOO" för pilbågen, magi, svärdshugg – eller kunskap mot Martin
    String attackSound() {
        return heroAttack.get().map(attack -> switch (attack) {
            case MagicAttack m -> "magic";
            case RangedAttack r -> "shoot";
            case MeleeAttack m -> "slash";
            default -> "smart";   // DoomScrolla, Java-boken ...
        }).orElse("slash");
    }

    String roomSong() {
        return roomSong;
    }
}
