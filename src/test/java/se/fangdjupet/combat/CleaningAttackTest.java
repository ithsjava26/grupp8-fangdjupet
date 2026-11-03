package se.fangdjupet.combat;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class CleaningAttackTest {

    // Hjälte utan rustning – försvar 0
    private Hero robert() {
        return new Hero("Robert", 40, new MeleeAttack(10));
    }

    // StädHäxan på nivå 2 – bonus 2
    private Enemy witch() {
        return new Enemy("StädHäxan", 26, new MeleeAttack(8), 2);
    }

    // RangedAttack slår nextInt(100): under 20 = miss
    private Random neverMisses() {
        return new Random() {
            @Override
            public int nextInt(int bound) {
                return 99;
            }
        };
    }

    private Random alwaysMisses() {
        return new Random() {
            @Override
            public int nextInt(int bound) {
                return 0;
            }
        };
    }

    // Högsta skadan på 50 kast – konstanterna har riktig slump, så en miss ibland gör inget
    private int best(CleaningAttack attack) {
        int best = 0;
        for (int i = 0; i < 50; i++) {
            best = Math.max(best, attack.calculateDamage(witch(), robert()));
        }
        return best;
    }

    @Test
    void cleaningIsARangedAttack() {
        assertInstanceOf(RangedAttack.class, CleaningAttack.SPONGE);
        assertInstanceOf(RangedAttack.class, CleaningAttack.SVINTO);
        assertInstanceOf(RangedAttack.class, CleaningAttack.CHEMICALS);
    }

    @Test
    void damageIsCountedLikeRanged() {
        // styrka 8 + halva bonusen 1 − försvar 0 = 9
        CleaningAttack svinto = new CleaningAttack("Svinto", "kastar Svinto på", 8, neverMisses());
        assertEquals(9, svinto.calculateDamage(witch(), robert()));
    }

    @Test
    void aThrowCanMiss() {
        CleaningAttack svinto = new CleaningAttack("Svinto", "kastar Svinto på", 8, alwaysMisses());
        assertEquals(0, svinto.calculateDamage(witch(), robert()));
    }

    @Test
    void nameAndVerbAreUsedInTheLog() {
        CleaningAttack svinto = new CleaningAttack("Svinto", "kastar Svinto på", 8, neverMisses());
        assertEquals("Svinto", svinto.name());
        assertEquals("kastar Svinto på", svinto.verb());
    }

    @Test
    void theSpongeIsThrown() {
        assertEquals("kastar en tvättsvamp på", CleaningAttack.SPONGE.verb());
    }

    @Test
    void svintoIsThrown() {
        assertEquals("kastar Svinto på", CleaningAttack.SVINTO.verb());
    }

    @Test
    void chemicalsAreSprayed() {
        assertEquals("sprayar kemikalier på", CleaningAttack.CHEMICALS.verb());
    }

    @Test
    void chemicalsHitHardest() {
        int sponge = best(CleaningAttack.SPONGE);
        int svinto = best(CleaningAttack.SVINTO);
        int chemicals = best(CleaningAttack.CHEMICALS);
        assertTrue(sponge < svinto && svinto < chemicals, sponge + " " + svinto + " " + chemicals);
    }

    @Test
    void cleaningCostsNothing() {
        assertEquals(0, CleaningAttack.CHEMICALS.selfDamage());
    }

    @Test
    void nameAndVerbMustNotBeBlank() {
        assertThrows(IllegalArgumentException.class, () -> new CleaningAttack(" ", "kastar", 8, neverMisses()));
        assertThrows(IllegalArgumentException.class, () -> new CleaningAttack("Svinto", null, 8, neverMisses()));
    }

    @Test
    void randomMustNotBeNull() {
        assertThrows(IllegalArgumentException.class, () -> new CleaningAttack("Svinto", "kastar", 8, null));
    }
}
