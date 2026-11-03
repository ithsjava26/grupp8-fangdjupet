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

        if (t.startsWith("Hero: ")) {
            inFight = true;
            speaker.sfx(t.contains(" missar ") ? "whoosh" : attackSound());
        } else if (t.startsWith("Enemy: ")) {
            inFight = true;
            speaker.sfx(t.contains(" missar ") ? "whoosh" : "hurt");
        } else if (t.endsWith(" vinner Fångdjupet!")) {
            inFight = false;
            roomSong = "title";
            speaker.jingle("final", "title");
        } else if (t.matches(".+ vinner mot .+!")) {
            inFight = false;
            speaker.jingle("win", roomSong);
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
        }
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
