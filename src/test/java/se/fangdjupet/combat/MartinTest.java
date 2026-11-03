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
}
