package se.fangdjupet.combat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EnemyTest {

    // Fiende med en strategi som alltid gör 5 i skada
    private Enemy enemy(int level) {
        return new Enemy("Goblin", 15, (a, t) -> 5, level);
    }

    @Test
    void attackBonusEqualsLevel() {
        assertEquals(3, enemy(3).getAttackBonus());
    }

    @Test
    void defenseIsHalfLevel() {
        assertEquals(2, enemy(4).getDefense());
    }

    @Test
    void defenseRoundsDown() {
        assertEquals(0, enemy(1).getDefense());   // 1 / 2 = 0
        assertEquals(1, enemy(3).getDefense());   // 3 / 2 = 1
    }

    @Test
    void enemyStartsWithFullHp() {
        Enemy e = enemy(1);
        assertEquals(15, e.getHp());
        assertTrue(e.isAlive());
    }

    @Test
    void enemyNeedsPositiveMaxHp() {
        assertThrows(IllegalArgumentException.class,
                () -> new Enemy("Ghost", 0, (a, t) -> 5, 1));
    }

    // ── Repliken: det fienden säger när den dyker upp ──

    @Test
    void enemyHasNoLineByDefault() {
        assertTrue(new Enemy("Troll", 10, (a, t) -> 5, 1).getLine().isEmpty());
    }

    @Test
    void enemyCanHaveALine() {
        Enemy wizard = new Enemy("Trollkarl", 10, (a, t) -> 5, 1).withLine("Thou shall not pass!");
        assertEquals("Thou shall not pass!", wizard.getLine().orElseThrow());
    }

    @Test
    void withLineReturnsTheSameEnemy() {
        Enemy wizard = new Enemy("Trollkarl", 10, (a, t) -> 5, 1);
        assertSame(wizard, wizard.withLine("Thou shall not pass!"));
    }

    @Test
    void blankLineMeansNoLine() {
        assertTrue(new Enemy("Troll", 10, (a, t) -> 5, 1).withLine("  ").getLine().isEmpty());
        assertTrue(new Enemy("Troll", 10, (a, t) -> 5, 1).withLine(null).getLine().isEmpty());
    }
}
