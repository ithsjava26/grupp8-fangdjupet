package se.fangdjupet.game;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AsciiArtTest {

    // Sant om alla tecken är vanlig ASCII (0–127) – annars kan Windows-konsolen visa ?
    private boolean isPlainAscii(String s) {
        return s.chars().allMatch(ch -> ch < 128);
    }

    @Test
    void allArtIsPlainAscii() {
        assertTrue(isPlainAscii(AsciiArt.TITLE));
        assertTrue(isPlainAscii(AsciiArt.MARTIN));
        assertTrue(isPlainAscii(AsciiArt.FRIDA));
    }

    @Test
    void martinAndFridaHaveTheirNames() {
        assertTrue(AsciiArt.MARTIN.contains("MARTIN"));
        assertTrue(AsciiArt.FRIDA.contains("FRIDA"));
    }

    @Test
    void bannerFramesText() {
        assertEquals("""
                +------+
                | Hall |
                +------+""", AsciiArt.banner("Hall"));
    }

    @Test
    void bannerPadsLinesToSameWidth() {
        assertEquals("""
                +-----------+
                | Sword     |
                | Damage: 4 |
                +-----------+""", AsciiArt.banner("Sword\nDamage: 4"));
    }

    @Test
    void showSendsArtToListener() {
        List<String> screen = new ArrayList<>();
        AsciiArt.show(screen::add, AsciiArt.MARTIN);
        assertEquals(1, screen.size());
        assertTrue(screen.getFirst().contains("MARTIN"));
        assertFalse(screen.getFirst().endsWith("\n"));    // sista radbrytningen är borttagen
    }

    @Test
    void showRemovesOnlyOneTerminalNewline() {
        List<String> screen = new ArrayList<>();
        AsciiArt.show(screen::add, "art \t\n\n");
        AsciiArt.show(screen::add, "art \t\n");
        AsciiArt.show(screen::add, "art\n \t");
        AsciiArt.show(screen::add, "");
        assertEquals(List.of("art \t\n", "art \t", "art\n \t", ""), screen);
    }

    @Test
    void bannerRejectsNull() {
        assertThrows(IllegalArgumentException.class, () -> AsciiArt.banner(null));
    }
}
