package se.fangdjupet.combat;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class RandomAttackTest {

    private Hero robert() {
        return new Hero("Robert", 40, new MeleeAttack(10));
    }

    private Enemy witch() {
        return new Enemy("StädHäxan", 26, new MeleeAttack(8), 2);
    }

    // En Random som svarar med talen i tur och ordning
    private Random rolls(int... values) {
        return new Random() {
            private int next;

            @Override
            public int nextInt(int bound) {
                return values[next++ % values.length];
            }
        };
    }

    // Två attacker som aldrig missar: svampen gör 6, Svinto 9
    private final CleaningAttack sponge = new CleaningAttack("Tvättsvamp", "kastar en tvättsvamp på", 5, rolls(99));
    private final CleaningAttack svinto = new CleaningAttack("Svinto", "kastar Svinto på", 8, rolls(99));

    @Test
    void picksTheAttackTheRandomPoints() {
        RandomAttack mix = new RandomAttack(rolls(1), sponge, svinto);
        assertEquals(9, mix.calculateDamage(witch(), robert()));
    }

    @Test
    void picksANewAttackEveryTime() {
        RandomAttack mix = new RandomAttack(rolls(0, 1), sponge, svinto);
        assertEquals(6, mix.calculateDamage(witch(), robert()));
        assertEquals(9, mix.calculateDamage(witch(), robert()));
    }

    @Test
    void verbFollowsTheLastPick() {
        // Battle räknar först skadan och skriver sedan verbet – då ska verbet höra till samma attack
        RandomAttack mix = new RandomAttack(rolls(0, 1), sponge, svinto);
        mix.calculateDamage(witch(), robert());
        assertEquals("kastar en tvättsvamp på", mix.verb());
        mix.calculateDamage(witch(), robert());
        assertEquals("kastar Svinto på", mix.verb());
    }

    @Test
    void beforeAnyPickTheFirstAttackIsShown() {
        RandomAttack mix = new RandomAttack(rolls(1), sponge, svinto);
        assertEquals("Tvättsvamp", mix.name());
    }

    @Test
    void needsAtLeastOneAttack() {
        assertThrows(IllegalArgumentException.class, () -> new RandomAttack(new Random()));
    }

    @Test
    void needsARandom() {
        assertThrows(IllegalArgumentException.class, () -> new RandomAttack(null, sponge));
    }
}
