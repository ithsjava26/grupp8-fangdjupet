package se.fangdjupet.combat;

import org.junit.jupiter.api.Test;
import se.fangdjupet.items.Armor;
import se.fangdjupet.items.ItemType;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class KnowledgeAttackTest {

    // Hjälte utan vapen eller rustning – bonus 0, försvar 0
    private Hero student() {
        return new Hero("Robin", 30, new MeleeAttack(3));
    }

    // Fiende på nivå 10: bonus 10, försvar 10 / 2 = 5
    private Enemy martin() {
        return new Enemy("Martin", 50, new MagicAttack(4), 10);
    }

    @Test
    void damageEqualsPower() {
        Hero h = student();
        h.setAttack(new KnowledgeAttack("Testattack", 7));
        assertEquals(7, h.attack(martin()));                   // bara kraften räknas
    }

    @Test
    void knowledgeIgnoresDefense() {
        Enemy m = martin();
        Hero h = student();
        h.setAttack(new KnowledgeAttack("Testattack", 7));
        assertEquals(7, h.attack(m));                          // Martins försvar 5 dras inte av
    }

    @Test
    void knowledgeIgnoresArmor() {
        Enemy m = martin();
        m.setAttack(new KnowledgeAttack("Testattack", 7));
        Hero h = student();
        int withoutArmor = m.attack(h);
        h.equip(new Armor(ItemType.SHIELD, "Plate", 20, 5));   // jättetjock rustning
        int withArmor = m.attack(h);
        assertEquals(withoutArmor, withArmor);                 // samma skada ändå
    }

    @Test
    void attackerBonusDoesNotCount() {
        Enemy m = martin();                                    // bonus 10
        m.setAttack(new KnowledgeAttack("Testattack", 7));
        assertEquals(7, m.attack(student()));                  // 7, inte 17
    }

    @Test
    void nameReturnsGivenName() {
        // Hade fångat buggen där name() anropade sig själv (StackOverflowError)
        assertEquals("Testattack", new KnowledgeAttack("Testattack", 7).name());
    }

    @Test
    void twoArgConstructorHasNoSelfDamage() {
        assertEquals(0, new KnowledgeAttack("Testattack", 7).selfDamage());
    }

    @Test
    void threeArgConstructorKeepsSelfDamage() {
        assertEquals(2, new KnowledgeAttack("Testattack", 7, 2).selfDamage());
    }

    @Test
    void attackDoesNotChangeHp() {
        Hero h = student();
        Enemy m = martin();
        h.setAttack(KnowledgeAttack.DOOMSCROLL);
        h.attack(m);
        assertEquals(50, m.getHp());                           // attack() returnerar bara skadan
        assertEquals(30, h.getHp());                           // självskadan dras av i Battle, inte här
    }

    @Test
    void doomscrollCostsOneHp() {
        assertEquals(5, KnowledgeAttack.DOOMSCROLL.calculateDamage(student(), martin()));
        assertEquals(1, KnowledgeAttack.DOOMSCROLL.selfDamage());
    }

    @Test
    void quizHasNoSelfDamage() {
        assertEquals(4, KnowledgeAttack.QUIZ.calculateDamage(student(), martin()));
        assertEquals(0, KnowledgeAttack.QUIZ.selfDamage());
    }

    @Test
    void stackOverflowCostsOneHp() {
        assertEquals(6, KnowledgeAttack.STACKOVERFLOW.calculateDamage(student(), martin()));
        assertEquals(1, KnowledgeAttack.STACKOVERFLOW.selfDamage());
    }

    @Test
    void screenshareHasNoSelfDamage() {
        assertEquals(0, KnowledgeAttack.SCREENSHARE.selfDamage());
    }

    // ── Dela skärm: 50/50 – ibland går det bra (6), ibland dåligt (1) ──

    // En Random som alltid svarar samma sak på nextBoolean()
    private Random coin(boolean heads) {
        return new Random() {
            @Override
            public boolean nextBoolean() {
                return heads;
            }
        };
    }

    @Test
    void screenshareIsEitherHighOrLow() {
        for (int i = 0; i < 50; i++) {
            int damage = KnowledgeAttack.SCREENSHARE.calculateDamage(student(), martin());
            assertTrue(damage == 1 || damage == 6, "skada: " + damage);
        }
    }

    @Test
    void gambleGoesWellOnHeads() {
        KnowledgeAttack share = new KnowledgeAttack("Dela skärm", 1, 6, coin(true));
        assertEquals(6, share.calculateDamage(student(), martin()));
    }

    @Test
    void gambleGoesBadlyOnTails() {
        KnowledgeAttack share = new KnowledgeAttack("Dela skärm", 1, 6, coin(false));
        assertEquals(1, share.calculateDamage(student(), martin()));
    }

    @Test
    void gambleNeedsARandom() {
        assertThrows(IllegalArgumentException.class, () -> new KnowledgeAttack("Dela skärm", 1, 6, null));
    }

    @Test
    void ordinaryKnowledgeAttackIsNotAGamble() {
        for (int i = 0; i < 20; i++) {
            assertEquals(4, KnowledgeAttack.QUIZ.calculateDamage(student(), martin()));
        }
    }

    @Test
    void otherStrategiesHaveNoSelfDamageByDefault() {
        // default-metoden i AttackStrategy ger 0 – gamla attacker och lambdor påverkas inte
        assertEquals(0, new MeleeAttack(3).selfDamage());
        assertEquals(0, new MagicAttack(6).selfDamage());
        AttackStrategy lambda = (a, t) -> 5;
        assertEquals(0, lambda.selfDamage());
    }

}
