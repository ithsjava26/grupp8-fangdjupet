package se.fangdjupet.sound;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import se.fangdjupet.combat.AttackStrategy;
import se.fangdjupet.combat.KnowledgeAttack;
import se.fangdjupet.combat.MagicAttack;
import se.fangdjupet.combat.MeleeAttack;
import se.fangdjupet.combat.RangedAttack;
import se.fangdjupet.game.AsciiArt;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class SoundListenerTest {

    // Antecknar vad som skulle ha spelats, i stället för att spela det
    static final class NotingSpeaker implements Speaker {
        final List<String> played = new ArrayList<>();
        public void music(String song) { played.add("music " + song); }
        public void jingle(String name, String then) { played.add("jingle " + name + " -> " + then); }
        public void sfx(String name) { played.add("sfx " + name); }
        String last() { return played.getLast(); }
    }

    private NotingSpeaker speaker;
    private AttackStrategy attack;
    private SoundListener sound;

    @BeforeEach
    void setUp() {
        speaker = new NotingSpeaker();
        attack = new MeleeAttack(5);
        sound = new SoundListener(speaker, () -> Optional.ofNullable(attack));
    }

    @Test
    void theMainMenuHasTheTitleSong() {
        assertEquals(List.of("music title"), speaker.played);
    }

    @Test
    void everyRoomHasItsOwnSongAndTheCleaningClosetIsSticky() {
        sound.onEvent("Skoldörren");
        assertEquals("music door", speaker.last());
        sound.onEvent("Kafferummet");
        assertEquals("music coffee", speaker.last());
        sound.onEvent("Korridoren och städskrubben");
        assertEquals("music sticky", speaker.last());
        sound.onEvent("Lektionssalen");
        assertEquals("music lecture", speaker.last());
    }

    @Test
    void fightsHaveBattleMusicTheWitchAStickyOneAndMartinHisOwn() {
        sound.onEvent("Kafferummet");
        sound.onEvent("Fiende Troll dyker upp!");
        assertEquals("music battle", speaker.last());
        sound.onEvent("Korridoren och städskrubben");
        sound.onEvent("Fiende StädHäxan dyker upp!");
        assertEquals("music stickyFight", speaker.last());
        sound.onEvent("Lektionssalen");
        sound.onEvent("Fiende Lektor Martin dyker upp!");
        assertEquals("music boss", speaker.last());
    }

    @Test
    void afterAWinTheRoomSongComesBack() {
        sound.onEvent("Korridoren och städskrubben");
        sound.onEvent("Fiende StädHäxan dyker upp!");
        sound.onEvent("Hero: Robert slår StädHäxan skada: 6");
        sound.onEvent("Robert vinner mot StädHäxan!");
        assertEquals("jingle win -> sticky", speaker.last());
    }

    @Test
    void theHerosHitSoundsLikeTheAttack() {
        sound.onEvent("Hero: Robert slår Troll skada: 6");
        assertEquals("sfx slash", speaker.last());
        attack = new RangedAttack(13, new Random(1));
        sound.onEvent("Hero: Robert skjuter Troll skada: 15");
        assertEquals("sfx shoot", speaker.last());       // pilbågen: det fula "PJOOO"
        attack = new MagicAttack(8);
        sound.onEvent("Hero: Robert skjuter StädHäxan skada: 14");
        assertEquals("sfx magic", speaker.last());       // samma verb, men magi
        attack = KnowledgeAttack.DOOMSCROLL;
        sound.onEvent("Hero: Robert DoomScrollar Lektor Martin skada: 9");
        assertEquals("sfx smart", speaker.last());
    }

    @Test
    void missesWhooshAndHitsOnTheHeroHurt() {
        sound.onEvent("Hero: Robert missar Troll!");
        assertEquals("sfx whoosh", speaker.last());
        sound.onEvent("Enemy: Entrédörren slår Robert skada: 7");
        assertEquals("sfx hurt", speaker.last());
        sound.onEvent("Enemy: Troll missar Robert!");
        assertEquals("sfx whoosh", speaker.last());
    }

    @Test
    void gameOverAndVictoryHaveJingles() {
        sound.onEvent("Robert besegrades av Troll.");
        assertEquals("jingle lose -> title", speaker.last());
        sound.onEvent("Robert vinner Fångdjupet!");
        assertEquals("jingle final -> title", speaker.last());
    }

    @Test
    void thingsThatHappenHaveSounds() {
        sound.onEvent("Entrédörren slår igen bakom dig!");
        assertEquals("sfx slam", speaker.last());
        sound.onEvent("StädHäxan skrattar elakt åt Städrustningen!");
        assertEquals("sfx laugh", speaker.last());
        sound.onEvent("Du plockar upp: Pilbåge");
        assertEquals("sfx pickup", speaker.last());
        sound.onEvent("Du dricker kaffet. (+30 hp)");
        assertEquals("sfx gulp", speaker.last());
        sound.onEvent(AsciiArt.yellow("Du hittar Städrustningen!"));   // färgkoderna stör inte
        assertEquals("sfx find", speaker.last());
    }

    @Test
    void everyEnemySoundsDifferent() {
        sound.onEvent("Enemy: Troll slår Robert skada: 7");
        assertEquals("sfx club", speaker.last());
        sound.onEvent("Enemy: Trollkarl skjuter Robert skada: 6");
        assertEquals("sfx zap", speaker.last());
        sound.onEvent("Enemy: StädHäxan kastar en tvättsvamp på Robert skada: 6");
        assertEquals("sfx splat", speaker.last());
        sound.onEvent("Enemy: StädHäxan kastar Svinto på Robert skada: 8");
        assertEquals("sfx hiss", speaker.last());
        sound.onEvent("Enemy: StädHäxan sprayar kemikalier på Robert skada: 11");
        assertEquals("sfx fizz", speaker.last());
        sound.onEvent("Enemy: Lektor Martin slår Robert skada: 10");
        assertEquals("sfx blah", speaker.last());
    }

    @Test
    void theHerosNewThingsHaveSounds() {
        attack = KnowledgeAttack.SCREENSHARE;
        sound.onEvent("Hero: Robert citerar kapitel 18: Polymorphism för Lektor Martin skada: 14");
        assertEquals("sfx page", speaker.last());
        sound.onEvent("Hero: Robert slinter på sidan och missar Lektor Martin!");
        assertEquals("sfx slip", speaker.last());
        sound.onEvent("Hero: Robert delar skärmen och tar över lektionen: +10 hp, ingen skada på Lektor Martin");
        assertEquals("sfx chime", speaker.last());
        sound.onEvent("Hero: Robert kastar stressbollen på Lektor Martin skada: 10");
        assertEquals("sfx boing", speaker.last());
        sound.onEvent("Hero: Robert kastar stressbollen förbi Lektor Martin!");
        assertEquals("sfx whoosh", speaker.last());
        sound.onEvent("Robert klämmer stressbollen: +5 hp");
        assertEquals("sfx squeak", speaker.last());
    }

    @Test
    void pickingThingsUpSoundsLikeTheThing() {
        sound.onEvent("Du har nu Pilbåge i handen: +4 i skada.");
        assertEquals("sfx clang", speaker.last());
        sound.onEvent("Du tar på dig Städrustning: +20 i försvar.");
        assertEquals("sfx clank", speaker.last());
        sound.onEvent("Du äter en kanelbulle (+35 hp).");
        assertEquals("sfx munch", speaker.last());
        sound.onEvent("Du stoppar Stressboll i fickan: i striden klämmer du den, +5 hp varje runda.");
        assertEquals("sfx squeak", speaker.last());
    }

    @Test
    void lowHpAfterAnEnemyHitBeeps() {
        sound.onEvent("Enemy: Troll slår Robert skada: 7");
        sound.onEvent("Robert  [##--------] 9/40");
        assertEquals("sfx lowhp", speaker.last());
        int before = speaker.played.size();
        sound.onEvent("Troll  [#---------] 2/12");     // fiendens mätare: ingen varning
        sound.onEvent("Enemy: Troll slår Robert skada: 7");
        sound.onEvent("Robert  [######----] 25/40");   // mycket kvar: ingen varning
        assertEquals(before + 1, speaker.played.size());
    }

    @Test
    void everythingOutsideTheFightHasASoundToo() {
        String[][] lines = {
                {"Du går till Kafferummet.", "steps"},
                {"Du letar i Lektionssalen ...", "rummage"},
                {"Du hittar inget här.", "nope"},
                {"Äventyret är sparat: Skoldörren, 40/40 hp.", "save"},
                {"Ogiltigt val. Försök igen.", "buzz"},
                {"Du är redan i Kafferummet.", "buzz"},
                {"Frida dyker upp!", "sparkle"},
                {"Äventyret börjar!", "fanfare"},
                {"Välkommen tillbaka, Robert!", "fanfare"},
                {"Du byter till Avståndsattack.", "swap"},
                {"Lektionssalen är låst. Du har inte hittat vägen dit än.", "locked"},
                {"Entrédörren står i vägen! Besegra rummets fiender innan du går vidare.", "locked"},
                {"Du låter Pilbåge ligga kvar. Den finns kvar om du kommer tillbaka.", "drop"},
                {"Visa hjältens utrustning.", "bag"},
                {"Tack för att du spelade!", "bye"},
                {"Frida: \"Du ser trött ut!\"", "talk"},
                {"Lektor Martin: \"Let me explain Hello World to you...\"", "blah"},
        };
        for (String[] line : lines) {
            sound.onEvent(line[0]);
            assertEquals("sfx " + line[1], speaker.last(), line[0]);
        }
    }

    @Test
    void anOrdinaryDescriptionIsSilent() {
        int before = speaker.played.size();
        sound.onEvent("Du står utanför skolan. Dörren är tung.");
        assertEquals(before, speaker.played.size());
    }

    @Test
    void enterGivesABlip() {
        sound.keyPressed();
        assertEquals("sfx blip", speaker.last());
    }

    @Test
    void otherLinesAreQuiet() {
        int before = speaker.played.size();
        sound.onEvent("Du står utanför skolan.");
        sound.onEvent("Du hittar inget mer här.");
        sound.onEvent(null);
        assertEquals(before, speaker.played.size());
    }

    @Test
    void theSpeakerAndTheAttackAreNeeded() {
        assertThrows(IllegalArgumentException.class, () -> new SoundListener(null, Optional::empty));
        assertThrows(IllegalArgumentException.class, () -> new SoundListener(speaker, null));
    }
}
