package se.fangdjupet.combat;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MartinTest {

    @Test
    void martinIsABoss() {
        Boss martin = Martin.create();
        assertEquals("Kapten Martin", martin.getName());
        assertEquals(40, martin.getMaxHp());
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
        martin.takeDamage(20);                                   // 20 av 40 kvar
        assertInstanceOf(KnowledgeAttack.class, martin.getAttack());
        assertEquals("Utterly explained", martin.getAttack().name());
    }

    @Test
    void phaseTwoHitsHarderThanPhaseOne() {
        Boss martin = Martin.create();
        Hero h = new Hero("Robert", 40, new MeleeAttack(10));
        int before = martin.attack(h);
        martin.takeDamage(20);
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
        first.takeDamage(40);
        assertTrue(Martin.create().isAlive());                   // en ny Martin har full hp
    }

    @Test
    void heroWithFullHpCanBeatMartinWithStackOverflow() {
        // Efter Fridas drycker har hjälten 40 hp. Med StackOverflow (6, kostar 1 hp) ska det gå att vinna.
        Hero h = new Hero("Robert", 40, new MeleeAttack(10));
        Boss martin = Martin.create();
        Battle b = new Battle(message -> { }, hero -> KnowledgeAttack.STACKOVERFLOW);
        assertSame(h, b.fight(h, martin));
    }

    @Test
    void martinWinsIfScreenSharingAlwaysGoesBadly() {
        // Dela skärm är 50/50. Går det dåligt varje gång (1 i skada) hinner Martin förklara hjälten i botten.
        Hero h = new Hero("Robert", 40, new MeleeAttack(10));
        Boss martin = Martin.create();
        KnowledgeAttack badLuck = new KnowledgeAttack("Dela skärm", 1, 6, new java.util.Random() {
            @Override
            public boolean nextBoolean() {
                return false;
            }
        });
        Battle b = new Battle(message -> { }, hero -> badLuck);
        assertSame(martin, b.fight(h, martin));
    }

    // ── Argare Martin när hjälten bär Städrustningen ──

    private final Hero dummy = new Hero("Robert", 40, new MeleeAttack(1));

    @Test
    void enragedMartinExplainsHarder() {
        Boss martin = Martin.create();
        Martin.enrage(martin);
        assertTrue(martin.isEnraged());
        assertEquals(4, martin.getAttack().calculateDamage(martin, dummy));        // fas 1: 4 i stället för 3
        martin.takeDamage(20);                                                      // halva livet: fas 2
        assertEquals(5, martin.getAttack().calculateDamage(martin, dummy));        // fas 2: kvar på 5
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
        martin.takeDamage(25);
        Martin.enrage(martin);
        assertEquals(5, martin.getAttack().calculateDamage(martin, dummy));
    }

    @Test
    void enrageHappensOnlyOnce() {
        Boss martin = Martin.create();
        Martin.enrage(martin);
        martin.enrage(new KnowledgeAttack("x", 99), new KnowledgeAttack("y", 99));   // ignoreras
        assertEquals(4, martin.getAttack().calculateDamage(martin, dummy));
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
