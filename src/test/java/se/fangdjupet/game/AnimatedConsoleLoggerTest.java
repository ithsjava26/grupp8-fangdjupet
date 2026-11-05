package se.fangdjupet.game;

import org.junit.jupiter.api.Test;
import se.fangdjupet.combat.MagicAttack;
import se.fangdjupet.combat.RangedAttack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class AnimatedConsoleLoggerTest {
    private final StringBuilder out = new StringBuilder();
    private final List<Integer> pauses = new ArrayList<>();
    private final AnimatedConsoleLogger logger = new AnimatedConsoleLogger(out::append, pauses::add);

    @Test
    void winningInATerminalPlaysTheFilmAndGivesTheCursorBack() {
        logger.redrawing().onEvent("Robert vinner Fångdjupet!");
        String film = out.toString();
        assertTrue(film.startsWith("\u001B[?25l"), "markören göms");
        assertEquals(Ascii3d.FRAMES - 1, film.split("\u001B\\[" + Ascii3d.HEIGHT + "A", -1).length - 1);
        assertTrue(film.contains("\u001B[?25h"), "markören kommer tillbaka");
        assertTrue(film.endsWith("Robert vinner Fångdjupet!\n"), "raden står kvar efter filmen");
    }

    @Test
    void winningWithoutATerminalShowsOnlyTheLastPicture() {
        logger.onEvent("Robert vinner Fångdjupet!");
        String film = out.toString();
        assertTrue(film.contains(Ascii3d.film().getLast()));
        assertFalse(film.contains("\u001B[" + Ascii3d.HEIGHT + "A"), "IntelliJ kan inte hoppa upp");
        assertFalse(film.contains("\u001B[?25l"));
    }

    @Test
    void winningAFightGetsNoTrophy() {
        logger.redrawing().onEvent("Robert vinner mot Troll!");
        assertFalse(out.toString().contains("\u001B[" + Ascii3d.HEIGHT + "A"));
    }

    @Test
    void shallNotPassGetsLightningAndBigWords() {
        logger.onEvent("Trollkarl: \"Thou shall not pass!\"");
        String film = out.toString();
        assertTrue(film.contains("\u001B[7m"), "blixten");
        assertTrue(film.contains("Trollkarl: \"Thou shall not pass!\""), "repliken skrivs som vanligt");
        assertTrue(film.contains("T H O U   S H A L L   N O T   P A S S !"), "orden stort");
    }

    @Test
    void otherSpeechGetsNoLightning() {
        logger.onEvent("Frida: \"Du ser trött ut!\"");
        assertFalse(out.toString().contains("\u001B[7m"));
        assertFalse(out.toString().contains("T H O U"));
    }

    @Test
    void ordinaryLinesArePrintedAtOnce() {
        logger.onEvent("Det luktar kaffe.");
        assertEquals("Det luktar kaffe.\n", out.toString());
        assertTrue(pauses.isEmpty());
    }

    @Test
    void artIsDrawnLineByLine() {
        logger.onEvent(AsciiArt.STRESS_BALL.stripTrailing());
        assertEquals(AsciiArt.STRESS_BALL.stripTrailing() + "\n", out.toString());
        assertEquals(AsciiArt.STRESS_BALL.stripTrailing().lines().count(), pauses.size());
        assertTrue(pauses.stream().allMatch(p -> p == AnimatedConsoleLogger.LINE_MS));
    }

    @Test
    void speechIsTypedLetterByLetter() {
        String line = "Lektor Martin: \"Varför är String immutable?\"";
        logger.onEvent(line);
        assertEquals(line + "\n", out.toString(), "samma text, bara långsammare");
        assertEquals("Varför är String immutable?".length(), pauses.size());
    }

    @Test
    void nullIsIgnoredAndNothingIsMissing() {
        logger.onEvent(null);
        assertEquals("", out.toString());
        assertThrows(IllegalArgumentException.class, () -> new AnimatedConsoleLogger(null, ms -> { }));
        assertThrows(IllegalArgumentException.class, () -> logger.followAttack(null));
    }

    // Efter filmen står den vanliga raden kvar, oförändrad, på en egen rad
    private void assertLineComesLast(String line) {
        assertTrue(out.toString().endsWith("\r" + line + "\n"), out.toString());
    }

    @Test
    void theArrowFliesBeforeTheHit() {
        logger.followAttack(() -> Optional.of(new RangedAttack(11, new Random(1))));
        logger.onEvent("Hero: Robert skjuter Troll skada: 13");
        assertTrue(out.toString().contains(">>---->"), "pilen");
        assertTrue(out.toString().contains("THWACK!"));
        assertLineComesLast("Hero: Robert skjuter Troll skada: 13");
        assertTrue(pauses.size() > 5 && pauses.stream().allMatch(ms -> ms == AnimatedConsoleLogger.FRAME_MS));
    }

    @Test
    void magicSparklesAndTheSwordSwings() {
        logger.followAttack(() -> Optional.of(new MagicAttack(8)));
        logger.onEvent("Hero: Robert skjuter Trollkarl skada: 9");
        assertTrue(out.toString().contains("Z A P !"));
        out.setLength(0);
        logger.onEvent("Hero: Robert slår Troll skada: 6");
        assertTrue(out.toString().contains("SLASH!"));
        assertLineComesLast("Hero: Robert slår Troll skada: 6");
    }

    @Test
    void theBookTheBallAndTheMissHaveTheirOwnFilms() {
        logger.onEvent("Hero: Robert citerar kapitel 18: Polymorphism för Lektor Martin skada: 14");
        assertTrue(out.toString().contains("{;}"));
        logger.onEvent("Hero: Robert kastar stressbollen på Lektor Martin skada: 10");
        assertTrue(out.toString().contains("SQUISH!"));
        logger.onEvent("Hero: Robert slinter på sidan och missar Lektor Martin!");
        assertTrue(out.toString().contains("duns"));
        logger.onEvent("Enemy: Troll missar Robert!");
        assertTrue(out.toString().contains("MISS!"));
        assertLineComesLast("Enemy: Troll missar Robert!");
    }

    @Test
    void everyEnemyHitsWithItsOwnWord() {
        assertEquals("BONK!", AnimatedConsoleLogger.enemyWord("Enemy: Troll slår Robert skada: 7"));
        assertEquals("ZAP!", AnimatedConsoleLogger.enemyWord("Enemy: Trollkarl skjuter Robert skada: 6"));
        assertEquals("SPLAT!", AnimatedConsoleLogger.enemyWord("Enemy: StädHäxan kastar en tvättsvamp på Robert skada: 6"));
        assertEquals("BLA BLA BLA!", AnimatedConsoleLogger.enemyWord("Enemy: Lektor Martin slår Robert skada: 10"));
        logger.onEvent("Enemy: Troll slår Robert skada: 7");
        assertTrue(out.toString().contains("BONK!"));
        assertLineComesLast("Enemy: Troll slår Robert skada: 7");
    }

    @Test
    void theHpBarDrainsStepByStep() {
        logger.onEvent("Robert  [##########] 40/40");     // första gången: ingen film
        assertTrue(pauses.isEmpty());
        logger.onEvent("Robert  [#####-----] 20/40");
        assertTrue(out.toString().contains("Robert  [#######---] 30/40"), "ett steg på vägen: " + out);
        assertLineComesLast("Robert  [#####-----] 20/40");
    }

    @Test
    void theDotsTickWhenSearching() {
        logger.onEvent("Du letar i Lektionssalen ...");
        assertEquals("Du letar i Lektionssalen ...\n", out.toString());
        assertEquals(3, pauses.size());
    }

    @Test
    void theFilmIsWipedBeforeTheLine() {
        logger.onEvent("Robert vinner mot Troll!");
        String s = out.toString();
        int wipe = s.lastIndexOf("\r" + " ".repeat(AnimatedConsoleLogger.WIDTH + 12) + "\r");
        assertTrue(wipe > 0 && s.indexOf("Robert vinner mot Troll!") > wipe, s);
    }

    @Test
    void theDamageRisesOutOfTheFilm() {
        List<String> film = AnimatedConsoleLogger.withDamage(List.of("smäll"), "Hero: Robert skjuter Troll skada: 14");
        assertEquals("smäll", film.getFirst());
        assertTrue(film.getLast().contains("−14"), film.toString());
        assertEquals(List.of("smäll"), AnimatedConsoleLogger.withDamage(List.of("smäll"), "Hero: Robert missar Troll!"));
    }

    @Test
    void theHitFlashesAndTheWordShakes() {
        List<String> film = AnimatedConsoleLogger.impact("BAM!");
        assertTrue(film.getFirst().contains("\u001B[7m"), "raden blinkar");
        assertTrue(film.stream().filter(frame -> frame.contains("* BAM! *")).distinct().count() > 2, "ordet skakar");
    }

    @Test
    void walkingKnockOutAndHealingHaveTheirOwnFilms() {
        assertTrue(AnimatedConsoleLogger.wipe().stream().anyMatch(frame -> frame.contains("▓")));
        assertTrue(AnimatedConsoleLogger.knockOut().getLast().contains("K  .  O"));
        assertTrue(AnimatedConsoleLogger.glow().getLast().contains("hp!"));
    }

    @Test
    void theBarIsTheSameAsHpBar() {
        assertEquals("[#####-----]", AnimatedConsoleLogger.bar(20, 40, 10));
        assertEquals("[#---------]", AnimatedConsoleLogger.bar(1, 40, 10), "lite liv kvar syns");
        assertEquals("[----------]", AnimatedConsoleLogger.bar(0, 40, 10));
    }

    @Test
    void theUsbStickHasItsOwnFilm() {
        logger.onEvent("Hero: Robert smyger in ett USB-minne med virus i datorn hos Lektor Martin skada: 100");
        assertTrue(out.toString().contains("VIRUS!"));
        logger.onEvent("Hero: Robert försöker smyga in ett USB-minne, men blir påkommen av Lektor Martin!");
        assertTrue(out.toString().contains("PÅKOMMEN!"));
        assertLineComesLast("Hero: Robert försöker smyga in ett USB-minne, men blir påkommen av Lektor Martin!");
    }

    // Panelen med riktig stil: bara det som skrivs efter den sista raden räknas, utan färgkoder
    private final StringBuilder styled = new StringBuilder();
    private final AnimatedConsoleLogger panel = new AnimatedConsoleLogger(styled::append, ms -> { }, true)
            .followHero(() -> java.util.Optional.of("Robert"));

    private String lastPanelRow(String line) {
        styled.setLength(0);
        panel.onEvent(line);
        return styled.toString().replaceAll("\u001B\\[[0-9;]*m", "");
    }

    @Test
    void panelenVisarFiendenSomSlar() {
        panel.onEvent("Fiende Troll dyker upp!");
        panel.onEvent("Fiende Trollkarl dyker upp!");
        panel.onEvent("Robert  [##########] 40/40");
        panel.onEvent("Troll  [#####-----] 6/12");
        panel.onEvent("Trollkarl  [#####-----] 12/24");

        panel.onEvent("Enemy: Troll slår Robert skada: 7");
        assertTrue(lastPanelRow("Robert  [########--] 33/40").contains("6/12"), "Trollet slog: dess rad syns");

        panel.onEvent("Enemy: Trollkarl skjuter Robert skada: 6");
        String row = lastPanelRow("Robert  [######----] 27/40");
        assertTrue(row.contains("12/24"), "Trollkarlen slog: dess rad syns");
        assertFalse(row.contains("6/12"), "inte Trollets gamla rad");
    }

    @Test
    void okandFiendeVisarBaraHjalten() {
        panel.onEvent("Fiende Troll dyker upp!");
        panel.onEvent("Fiende Trollkarl dyker upp!");
        panel.onEvent("Robert  [##########] 40/40");
        panel.onEvent("Troll  [#####-----] 6/12");

        panel.onEvent("Enemy: Trollkarl skjuter Robert skada: 6");
        String row = lastPanelRow("Robert  [########--] 34/40");

        assertTrue(row.contains("34/40"));
        assertFalse(row.contains("vs"), "ingen gammal fiende bredvid hjälten");
    }
}
