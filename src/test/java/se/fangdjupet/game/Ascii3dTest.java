package se.fangdjupet.game;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class Ascii3dTest {
    private static final List<String> FILM = Ascii3d.film();

    private static String plain(String frame) {
        return frame.replaceAll("\u001B\\[[0-9;]*m", "");
    }

    @Test
    void everyFrameFitsAnEightyColumnConsole() {
        assertEquals(Ascii3d.FRAMES, FILM.size());
        for (String frame : FILM) {
            String[] rows = plain(frame).split("\n", -1);
            assertEquals(Ascii3d.HEIGHT, rows.length);
            for (String row : rows) {
                assertEquals(Ascii3d.SCHOOL_WIDTH + Ascii3d.TROPHY_WIDTH, row.length());
                assertTrue(row.length() < 80, "en full rad skulle bryta i en vanlig konsol");
            }
        }
    }

    @Test
    void theFilmIsPureAscii() {
        assertTrue(FILM.stream().allMatch(frame -> plain(frame).chars().allMatch(c -> c == '\n' || c >= 32 && c < 127)));
    }

    @Test
    void theShapesTurnFromFrameToFrame() {
        assertTrue(new HashSet<>(FILM).size() > Ascii3d.FRAMES / 2);
        assertNotEquals(plain(FILM.get(40)), plain(FILM.get(41)));
    }

    @Test
    void itGrowsFromNothingAndEndsWithBothShapesInPlace() {
        long first = plain(FILM.getFirst()).chars().filter(c -> "@%#*+=-:.".indexOf(c) >= 0).count();
        long last = plain(FILM.getLast()).chars().filter(c -> "@%#*+=-:.".indexOf(c) >= 0).count();
        assertTrue(first < last / 10, "filmen börjar nästan tom");
        String middle = plain(FILM.getLast()).split("\n")[10];
        assertFalse(middle.substring(0, Ascii3d.SCHOOL_WIDTH).isBlank(), "skolan till vänster");
        assertFalse(middle.substring(Ascii3d.SCHOOL_WIDTH).isBlank(), "pokalen till höger");
    }

    @Test
    void theSameFilmEveryTime() {
        assertEquals(FILM, Ascii3d.film());
    }
}
