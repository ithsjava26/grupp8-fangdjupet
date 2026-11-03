package se.fangdjupet.combat;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class CleaningWitchTest {

    @Test
    void sheIsABossCalledStadHaxan() {
        Boss witch = CleaningWitch.create(new Random());
        assertEquals("StädHäxan", witch.getName());
        assertEquals(30, witch.getMaxHp());
        assertEquals(2, witch.getAttackBonus());              // nivå 2
    }

    @Test
    void sheStartsWithSpongeOrSvinto() {
        Boss witch = CleaningWitch.create(new Random());
        assertInstanceOf(RandomAttack.class, witch.getAttack());
        assertFalse(witch.isInPhaseTwo());
    }

    @Test
    void atHalfLifeSheSpraysChemicals() {
        Boss witch = CleaningWitch.create(new Random());
        witch.takeDamage(15);                                   // 30 → 15, halva livet
        assertTrue(witch.isInPhaseTwo());
        assertSame(CleaningAttack.CHEMICALS, witch.getAttack());
    }

    @Test
    void sheThreatensWithChemicals() {
        Boss witch = CleaningWitch.create(new Random());
        assertTrue(witch.getLine().orElseThrow().contains("kemikalier"));
    }

    @Test
    void randomMustNotBeNull() {
        assertThrows(IllegalArgumentException.class, () -> CleaningWitch.create(null));
    }
}
