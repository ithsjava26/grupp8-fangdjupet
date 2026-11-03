package se.fangdjupet.game;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AnimatedConsoleLoggerTest {
    private final StringBuilder out = new StringBuilder();
    private final List<Integer> pauses = new ArrayList<>();
    private final AnimatedConsoleLogger logger = new AnimatedConsoleLogger(out::append, pauses::add);

    @Test
    void ordinaryLinesArePrintedAtOnce() {
        logger.onEvent("Du går till Kafferummet.");
        assertEquals("Du går till Kafferummet.\n", out.toString());
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
    }
}
