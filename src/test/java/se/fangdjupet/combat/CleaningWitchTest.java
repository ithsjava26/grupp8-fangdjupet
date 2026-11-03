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

    // ── Städrustningen: hennes egen rustning på hjälten ──

    @Test
    void enragedSheThreatensWithVanishAndKeepsThrowing() {
        Boss witch = CleaningWitch.create(new Random(1));
        AttackStrategy phaseOne = witch.getAttack();
        CleaningWitch.enrage(witch);
        assertTrue(witch.isEnraged());
        assertSame(phaseOne, witch.getAttack(), "fas 1: Svinto och tvättsvampar som vanligt");
        assertEquals(CleaningWitch.ENRAGED_LINE, witch.getLine().orElseThrow());
        assertTrue(CleaningWitch.ENRAGED_LINE.contains("Svinto") && CleaningWitch.ENRAGED_LINE.contains("Vanish"));
        witch.takeDamage(15);                                   // halva livet: fas 2
        assertSame(CleaningAttack.VANISH, witch.getAttack());
    }

    @Test
    void enragedInPhaseTwoSwitchesToVanishAtOnce() {
        Boss witch = CleaningWitch.create(new Random(1));
        witch.takeDamage(15);
        CleaningWitch.enrage(witch);
        assertSame(CleaningAttack.VANISH, witch.getAttack());
    }

    @Test
    void vanishIsAsHardAsTheChemicalsAndHasArt() {
        assertEquals("Vanish", CleaningAttack.VANISH.name());
        assertTrue(CleaningAttack.VANISH.verb().contains("ansiktet"));
        assertTrue(AttackArt.forAttack(CleaningAttack.VANISH).orElseThrow().contains("VANISH"));
    }

    @Test
    void sheLaughsInsteadOfExplaining() {
        assertTrue(CleaningWitch.LAUGH.contains("skrattar elakt"));
        assertFalse(CleaningWitch.LAUGH.contains("förklarar"));
        assertTrue(Martin.ANGRY.contains("förklarar hårdare"));
    }
}
