package se.fangdjupet.combat;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class CleaningWitchTest {

    @Test
    void sheIsABossCalledStadHaxan() {
        Boss witch = CleaningWitch.create(new Random());
        assertEquals("StädHäxan", witch.getName());
        assertEquals(40, witch.getMaxHp());
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
        witch.takeDamage(20);                                   // 40 → 20, halva livet
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
        witch.takeDamage(20);                                   // halva livet: fas 2
        assertSame(CleaningAttack.VANISH, witch.getAttack());
    }

    @Test
    void enragedInPhaseTwoSwitchesToVanishAtOnce() {
        Boss witch = CleaningWitch.create(new Random(1));
        witch.takeDamage(20);
        CleaningWitch.enrage(witch);
        assertSame(CleaningAttack.VANISH, witch.getAttack());
    }

    // En hjälte i Städrustningen (+20 försvar) och en StädHäxa på nivå 2 (bonus 2, halva = 1)
    private Hero inArmor() {
        Hero h = new Hero("Robert", 40, new MeleeAttack(6));
        h.equip(new se.fangdjupet.items.Armor(se.fangdjupet.items.ItemType.SHIELD, "Städrustning", 20, 2));
        return h;
    }

    // Slumpen 99 = ingen miss
    private static Random noMiss() {
        return new Random() { @Override public int nextInt(int bound) { return bound - 1; } };
    }

    @Test
    void vanishGoesStraightThroughTheArmor() {
        Boss witch = CleaningWitch.create(new Random(1));
        CleaningAttack vanish = new CleaningAttack("Vanish", "sprejar Vanish i ansiktet på", 10, noMiss(), true);
        assertTrue(CleaningAttack.VANISH.throughArmor());
        assertEquals(11, vanish.calculateDamage(witch, inArmor()), "10 + halva nivån, försvaret räknas inte");
    }

    @Test
    void herThrowsBounceOffTheArmor() {
        Boss witch = CleaningWitch.create(new Random(1));
        CleaningAttack chemicals = new CleaningAttack("Kemikalier", "sprayar kemikalier på", 11, noMiss());
        assertFalse(CleaningAttack.CHEMICALS.throughArmor());
        assertEquals(0, chemicals.calculateDamage(witch, inArmor()), "11 + 1 − 20 → 0");
    }

    @Test
    void sheHitsHarderThanBefore() {
        Boss witch = CleaningWitch.create(new Random(1));
        Hero h = new Hero("Robert", 40, new MeleeAttack(6));
        assertEquals(12, new CleaningAttack("Kemikalier", "x", 11, noMiss()).calculateDamage(witch, h));
        assertEquals(40, CleaningWitch.HP);
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
        assertTrue(Martin.angryLine(new Random(1)).contains("förklarar hårdare"));
    }
}
