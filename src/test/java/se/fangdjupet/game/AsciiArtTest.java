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

    // ── Bilder för fiender, saker och personer (namnen i GameEngine och WorldBuilder) ──

    private static final List<String> THING_NAMES = List.of(
            "JavaFläcken", "Troll", "Trollkarl", "StädHäxan", "Kapten Martin", "Frida",
            "en kopp kaffe", "en kanelbulle", "Java-bok", "Pilbåge", "Trollstav");

    @Test
    void everyEnemyItemAndPersonHasArt() {
        for (String name : THING_NAMES) {
            assertTrue(AsciiArt.forName(name).isPresent(), "saknar bild: " + name);
        }
    }

    @Test
    void thingArtIsPlainAsciiAndFitsTheConsole() {
        for (String name : THING_NAMES) {
            String art = AsciiArt.forName(name).orElseThrow();
            assertTrue(isPlainAscii(art), "ASCII: " + name);
            assertTrue(art.lines().allMatch(line -> line.length() <= 60), "för bred: " + name);
        }
    }

    @Test
    void martinAndFridaUseTheirPortraits() {
        assertEquals(AsciiArt.MARTIN, AsciiArt.forName("Kapten Martin").orElseThrow());
        assertEquals(AsciiArt.FRIDA, AsciiArt.forName("Frida").orElseThrow());
    }

    @Test
    void eachThingHasItsOwnPicture() {
        long distinct = THING_NAMES.stream().map(n -> AsciiArt.forName(n).orElseThrow()).distinct().count();
        assertEquals(THING_NAMES.size(), distinct);
    }

    @Test
    void cinnamonBunAndBookShowTheirLabels() {
        assertTrue(AsciiArt.forName("en kanelbulle").orElseThrow().contains("KANELBULLE"));
        assertTrue(AsciiArt.forName("Java-bok").orElseThrow().contains("JAVA-BOKEN"));
    }

    @Test
    void unknownThingHasNoArt() {
        assertTrue(AsciiArt.forName("Något som inte finns").isEmpty());
        assertTrue(AsciiArt.forName(null).isEmpty());
    }

    // ── Rumsbilderna (rummen i WorldBuilder) ──

    private static final List<String> ROOM_NAMES =
            List.of("Skoldörren", "Kafferummet", "Korridoren och städskrubben", "Lektionssalen");

    @Test
    void everyRoomHasArt() {
        for (String name : ROOM_NAMES) {
            assertTrue(AsciiArt.forRoom(name).isPresent(), "saknar bild: " + name);
        }
    }

    @Test
    void roomArtIsPlainAsciiAndFitsTheConsole() {
        for (String name : ROOM_NAMES) {
            String art = AsciiArt.forRoom(name).orElseThrow();
            assertTrue(isPlainAscii(art), "ASCII: " + name);
            assertTrue(art.lines().allMatch(line -> line.length() <= 60), "för bred: " + name);
        }
    }

    @Test
    void eachRoomShowsItsOwnLabel() {
        assertTrue(AsciiArt.SCHOOL_DOOR.contains("SKOLDORREN"));
        assertTrue(AsciiArt.COFFEE_ROOM.contains("KAFFERUMMET"));
        assertTrue(AsciiArt.CORRIDOR.contains("KORRIDOREN"));
        assertTrue(AsciiArt.CORRIDOR.contains("STADSKRUBBEN"));
        assertTrue(AsciiArt.CLASSROOM.contains("LEKTIONSSALEN"));
    }

    @Test
    void fourRoomsHaveFourDifferentPictures() {
        long distinct = ROOM_NAMES.stream().map(n -> AsciiArt.forRoom(n).orElseThrow()).distinct().count();
        assertEquals(4, distinct);
    }

    @Test
    void unknownRoomHasNoArt() {
        assertTrue(AsciiArt.forRoom("Rum som inte finns").isEmpty());
        assertTrue(AsciiArt.forRoom(null).isEmpty());
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

    // ── Game over: stora bokstäver i rött ──

    @Test
    void gameOverIsPlainAsciiAndFitsTheConsole() {
        assertTrue(isPlainAscii(AsciiArt.GAME_OVER));
        assertTrue(AsciiArt.GAME_OVER.lines().allMatch(line -> line.length() <= 70));
        assertTrue(AsciiArt.GAME_OVER.lines().count() >= 4);   // stora bokstäver, inte en rad text
    }

    @Test
    void redWrapsTextInAnsiColour() {
        // ESC[31m = röd text, ESC[0m = tillbaka till vanlig färg
        assertEquals("\u001B[31mGAME OVER\u001B[0m", AsciiArt.red("GAME OVER"));
    }

    @Test
    void redRejectsNull() {
        assertThrows(IllegalArgumentException.class, () -> AsciiArt.red(null));
    }
}
