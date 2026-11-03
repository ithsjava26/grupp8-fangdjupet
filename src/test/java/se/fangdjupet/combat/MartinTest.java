package se.fangdjupet.combat;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MartinTest {

    @Test
    void martinIsABoss() {
        Boss martin = Martin.create();
        assertEquals("Lektor Martin", martin.getName());
        assertEquals(50, martin.getMaxHp());
    }

    @Test
    void martinExplainsWithKnowledgeAttacks() {
        Boss martin = Martin.create();
        assertInstanceOf(KnowledgeAttack.class, martin.getAttack());
        assertEquals("Thoroughly explained the Hello World Java to you", martin.getAttack().name());
    }

    @Test
    void martinExplainsHarderAtHalfHp() {
        Boss martin = Martin.create();
        martin.takeDamage(25);                                   // 25 av 50 kvar
        assertInstanceOf(KnowledgeAttack.class, martin.getAttack());
        assertEquals("Utterly explained", martin.getAttack().name());
    }

    @Test
    void phaseTwoHitsHarderThanPhaseOne() {
        Boss martin = Martin.create();
        Hero h = new Hero("Robert", 40, new MeleeAttack(10));
        int before = martin.attack(h);
        martin.takeDamage(25);
        assertTrue(martin.attack(h) > before);
    }

    @Test
    void martinGreetsTheHero() {
        assertTrue(Martin.create().getLine().orElseThrow().contains("Hello World"));
    }

    @Test
    void heroChoicesAreTheFourKnowledgeAttacks() {
        List<KnowledgeAttack> choices = Martin.heroChoices();
        assertEquals(List.of(KnowledgeAttack.DOOMSCROLL, KnowledgeAttack.QUIZ,
                KnowledgeAttack.STACKOVERFLOW, KnowledgeAttack.SCREENSHARE), choices);
    }

    @Test
    void eachCreateGivesANewMartin() {
        Boss first = Martin.create();
        first.takeDamage(50);
        assertTrue(Martin.create().isAlive());                   // en ny Martin har full hp
    }

    @Test
    void heroWithFullHpBeatsMartinWithTheJavaBook() {
        // Efter Fridas drycker har hjälten 40 hp. Boken (14) utan slump: 4 citat mot Martins 50 hp.
        Hero h = new Hero("Robert", 40, new MeleeAttack(10));
        h.pickUpJavaBook();
        Boss martin = Martin.create();
        Battle b = new Battle(message -> { }, hero -> hero.getJavaBookAttack());
        assertSame(h, b.fight(h, martin));
        assertEquals(40 - 7 - 10 - 10, h.getHp());               // Martin hann förklara 3 gånger: 7, sedan 10 från halva livet
    }

    @Test
    void stackOverflowAloneIsTooWeakAgainstMartin() {
        // 11 i skada men kostar 3 hp: Martin hinner förklara hjälten i botten
        Hero h = new Hero("Robert", 40, new MeleeAttack(10));
        Boss martin = Martin.create();
        Battle b = new Battle(message -> { }, hero -> KnowledgeAttack.STACKOVERFLOW);
        assertSame(martin, b.fight(h, martin));
    }

    @Test
    void screenSharingGivesTimeButNeverWins() {
        // Dela skärm ger +10 hp men gör ingen skada: vid rundtaket har Martin fortfarande full hp
        Hero h = new Hero("Robert", 40, new MeleeAttack(10));
        Boss martin = Martin.create();
        Battle b = new Battle(message -> { }, hero -> KnowledgeAttack.SCREENSHARE);
        assertSame(martin, b.fight(h, martin));
        assertEquals(Martin.HP, martin.getHp());
    }

    // ── Argare Martin när hjälten bär Städrustningen ──

    private final Hero dummy = new Hero("Robert", 40, new MeleeAttack(1));

    @Test
    void enragedMartinExplainsHarder() {
        Boss martin = Martin.create();
        Martin.enrage(martin);
        assertTrue(martin.isEnraged());
        assertEquals(8, martin.getAttack().calculateDamage(martin, dummy));        // fas 1: 8 i stället för 7
        martin.takeDamage(25);                                                      // halva livet: fas 2
        assertEquals(10, martin.getAttack().calculateDamage(martin, dummy));       // fas 2: kvar på 10
    }

    @Test
    void enragedMartinKeepsTheNamesSoTheArtStays() {
        Boss martin = Martin.create();
        String before = martin.getAttack().name();
        Martin.enrage(martin);
        assertEquals(before, martin.getAttack().name());
        assertTrue(AttackArt.forAttack(martin.getAttack()).isPresent());
    }

    @Test
    void enragedMartinHasANewLine() {
        Boss martin = Martin.create();
        Martin.enrage(martin);
        assertEquals(Martin.ENRAGED_LINE, martin.getLine().orElseThrow());
    }

    @Test
    void enrageInPhaseTwoUsesTheNewPhaseTwoAttack() {
        Boss martin = Martin.create();
        martin.takeDamage(30);
        Martin.enrage(martin);
        assertEquals(10, martin.getAttack().calculateDamage(martin, dummy));
    }

    @Test
    void enrageHappensOnlyOnce() {
        Boss martin = Martin.create();
        Martin.enrage(martin);
        martin.enrage(new KnowledgeAttack("x", 99), new KnowledgeAttack("y", 99));   // ignoreras
        assertEquals(8, martin.getAttack().calculateDamage(martin, dummy));
    }

    @Test
    void enrageRejectsNullAttacks() {
        assertThrows(IllegalArgumentException.class, () -> Martin.create().enrage(null, KnowledgeAttack.QUIZ));
    }

    @Test
    void normalMartinIsNotEnraged() {
        assertFalse(Martin.create().isEnraged());
    }
}
