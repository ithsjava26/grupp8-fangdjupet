package se.fangdjupet.game;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ConsoleStyleTest {

    private static String plain(String s) {
        return s.replaceAll("\u001B\\[[0-9;]*m", "");
    }

    @AfterEach
    void off() {
        ConsoleStyle.enable(false);
    }

    @Test
    void theHpBarBecomesColouredBlocks() {
        String bar = ConsoleStyle.line("Robert  [#####-----] 20/40");
        assertEquals("Robert  █████░░░░░  20/40", plain(bar));
        assertTrue(bar.contains(ConsoleStyle.YELLOW), "halva livet: gult");
        assertTrue(ConsoleStyle.line("Robert  [#########-] 36/40").contains(ConsoleStyle.GREEN));
        assertTrue(ConsoleStyle.line("Robert  [#---------] 4/40").contains(ConsoleStyle.RED), "lite kvar: rött");
    }

    @Test
    void heroAndEnemyLinesGetArrowsAndTheDamageStandsOut() {
        assertEquals("» Robert skjuter Troll  −14", plain(ConsoleStyle.line("Hero: Robert skjuter Troll skada: 14")));
        assertEquals("« Troll slår Robert  −11", plain(ConsoleStyle.line("Enemy: Troll slår Robert skada: 11")));
        assertTrue(ConsoleStyle.line("Enemy: Troll slår Robert skada: 11").startsWith(ConsoleStyle.RED));
    }

    @Test
    void winsAppearancesAndSpeechAreColoured() {
        assertEquals("★ Robert vinner mot Troll! ★", plain(ConsoleStyle.line("Robert vinner mot Troll!")));
        assertEquals("!! Fiende Troll dyker upp!", plain(ConsoleStyle.line("Fiende Troll dyker upp!")));
        assertEquals("Lektor Martin: \"Hej\"", plain(ConsoleStyle.line("Lektor Martin: \"Hej\"")));
        assertNotEquals("Lektor Martin: \"Hej\"", ConsoleStyle.line("Lektor Martin: \"Hej\""));
    }

    @Test
    void ordinaryAndAlreadyColouredLinesStayTheSame() {
        assertEquals("Det luktar kaffe.", ConsoleStyle.line("Det luktar kaffe."));
        assertEquals("▌ KAFFERUMMET", plain(ConsoleStyle.line("Kafferummet")), "rummets namn blir en rubrik");
        String yellow = AsciiArt.yellow("Plocka upp Pilbåge? (j/n): ");
        assertEquals(yellow, ConsoleStyle.line(yellow));
        assertNull(ConsoleStyle.line(null));
    }

    @Test
    void menusMapsAndPromptsAreOnlyStyledInARealConsole() {
        String menu = "Kafferummet\n===========\n1. Strid mot rummets fiende\ne. Avsluta";
        assertEquals(menu, ConsoleStyle.menu(menu), "av: webben får vanlig text");
        assertEquals("[@] Kafferummet", ConsoleStyle.map("[@] Kafferummet"));
        ConsoleStyle.enable(true);
        String styled = ConsoleStyle.menu(menu);
        assertEquals(menu, plain(styled), "samma text, utan ruta runt siffrorna");
        assertTrue(styled.contains(ConsoleStyle.YELLOW + ConsoleStyle.BOLD + "1." + ConsoleStyle.RESET), "siffran i fet gul");
        assertTrue(ConsoleStyle.map("[@] Kafferummet -- [#] Korridoren").contains(ConsoleStyle.YELLOW + ConsoleStyle.BOLD + "[@]"));
        assertEquals("Ditt val: ", plain(ConsoleStyle.prompt("Ditt val: ")));
    }

    @Test
    void trueColorSwapsTheColoursForTheWebsitesAndOnlyThen() {
        String yellow = AsciiArt.yellow("Plocka upp?");
        assertEquals(yellow, ConsoleStyle.tint(yellow), "16 färger: som den är");
        ConsoleStyle.enable(true);
        ConsoleStyle.trueColor(true);
        assertEquals("\u001B[38;2;233;178;94mPlocka upp?" + AsciiArt.RESET, ConsoleStyle.tint(yellow));
        assertEquals("Plocka upp?", plain(ConsoleStyle.tint(yellow)));
        assertNull(ConsoleStyle.tint(null));
    }

    @Test
    void theTerminalDecidesTheColoursUnlessTheSettingDoes() {
        java.util.function.UnaryOperator<String> nothing = key -> null;
        assertFalse(ConsoleStyle.supportsTrueColor(nothing, null));
        assertTrue(ConsoleStyle.supportsTrueColor(nothing, "24"));
        assertTrue(ConsoleStyle.supportsTrueColor(key -> key.equals("COLORTERM") ? "truecolor" : null, null));
        assertTrue(ConsoleStyle.supportsTrueColor(key -> key.equals("WT_SESSION") ? "abc" : null, null));
        assertFalse(ConsoleStyle.supportsTrueColor(key -> key.equals("WT_SESSION") ? "abc" : null, "16"));
    }

    @Test
    void aPanelIsAFrameOnlyInARealConsole() {
        String menu = "Kafferummet\n===========\n1. Strid\ne. Avsluta\n";
        assertEquals(menu, ConsoleStyle.panel(menu), "av: webben får vanlig text");
        assertEquals(menu, ConsoleStyle.panel(menu, "Karta"));
        ConsoleStyle.enable(true);
        assertEquals("""
                ┌─ KAFFERUMMET ─┐
                │ 1. Strid      │
                │ e. Avsluta    │
                └───────────────┘""", plain(ConsoleStyle.panel(menu)));
    }

    @Test
    void everyRowInAPanelIsEquallyWideEvenWithColours() {
        ConsoleStyle.enable(true);
        ConsoleStyle.trueColor(true);
        String panel = ConsoleStyle.panel(AsciiArt.yellow("1. Strid") + "\nen längre rad än den första", "Karta");
        assertEquals(1, plain(panel).lines().mapToInt(String::length).distinct().count(), plain(panel));
    }

    @Test
    void theGearRowsGetIconsAndTheSameTextStays() {
        assertEquals("│ ► Vapen      Pilbåge (+4 i skada)", plain(ConsoleStyle.line("Vapen: Pilbåge (+4 i skada)")));
        assertEquals("│ ♦ Ryggsäck   Svärd, Pilbåge", plain(ConsoleStyle.line("Ryggsäck: Svärd, Pilbåge")));
        assertTrue(plain(ConsoleStyle.line("Visa hjältens utrustning.")).startsWith("┌─ UTRUSTNING ─"));
        assertEquals("", ConsoleStyle.gearEnd(), "av: webben får ingen extra rad");
        ConsoleStyle.enable(true);
        assertTrue(plain(ConsoleStyle.gearEnd()).startsWith("└─"));
    }

    @Test
    void theStatusRowShowsRoomHpAndAttack() {
        assertEquals("▌ KAFFERUMMET   Robert  █████░░░░░  20/40   Avståndsattack",
                plain(ConsoleStyle.status("Kafferummet", "Robert  [#####-----] 20/40", "Avståndsattack")));
    }

    @Test
    void theHeroAndTheEnemyStandSideBySideInTheFight() {
        StringBuilder out = new StringBuilder();
        AnimatedConsoleLogger styled = new AnimatedConsoleLogger(out::append, ms -> { }, true)
                .followHero(() -> java.util.Optional.of("Robert"));
        styled.onEvent("Robert  [#####-----] 20/40");
        assertEquals("Robert  █████░░░░░  20/40\n", plain(out.toString()), "ingen fiende än: raden som den är");
        out.setLength(0);
        styled.onEvent("Troll  [##########] 12/12");
        assertEquals("▌ Robert  █████░░░░░  20/40   vs   Troll  ██████████  12/12\n", plain(out.toString()));
        out.setLength(0);
        styled.onEvent("Robert vinner mot Troll!");
        out.setLength(0);
        styled.onEvent("Robert  [#####-----] 20/40");
        assertEquals("Robert  █████░░░░░  20/40\n", plain(out.toString()), "efter striden: ingen fiende kvar");
    }

    @Test
    void aDefeatedEnemyIsNotInTheNextPanel() {
        StringBuilder out = new StringBuilder();
        AnimatedConsoleLogger styled = new AnimatedConsoleLogger(out::append, ms -> { }, true)
                .followHero(() -> java.util.Optional.of("Robert"));
        styled.onEvent("Robert  [#####-----] 20/40");
        styled.onEvent("Troll  [----------] 0/12");
        out.setLength(0);
        styled.onEvent("Robert  [####------] 16/40");
        assertFalse(out.toString().contains("Troll"), out.toString());
    }

    @Test
    void theHpRowInTheGearPanelHasARail() {
        StringBuilder out = new StringBuilder();
        AnimatedConsoleLogger styled = new AnimatedConsoleLogger(out::append, ms -> { }, true);
        styled.onEvent("Visa hjältens utrustning.");
        out.setLength(0);
        styled.onEvent("Robert  [#####-----] 20/40");
        assertEquals("│ Robert  █████░░░░░  20/40\n", plain(out.toString()));
    }

    @Test
    void theAnimatedConsoleUsesTheStyleButTheTextIsTheSame() {
        StringBuilder out = new StringBuilder();
        List<Integer> pauses = new ArrayList<>();
        AnimatedConsoleLogger styled = new AnimatedConsoleLogger(out::append, pauses::add, true);
        styled.onEvent("Hero: Robert skjuter Troll skada: 14");
        assertTrue(plain(out.toString()).endsWith("» Robert skjuter Troll  −14\n"), plain(out.toString()));
        out.setLength(0);
        styled.onEvent("Lektor Martin: \"Hej\"");
        assertEquals("Lektor Martin: \"Hej\"\n", plain(out.toString()));
    }
}
