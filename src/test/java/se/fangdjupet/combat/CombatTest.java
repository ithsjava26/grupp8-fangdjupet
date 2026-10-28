package se.fangdjupet.combat;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CombatTest {

    // Låtsaskaraktär: strategin gör alltid 5 i skada, ingen bonus eller försvar
    private Character dummy(int maxHp) {
        return new Character("Dummy", maxHp, (a, t) -> 5) {
            @Override public int getAttackBonus() { return 0; }
            @Override public int getDefense()     { return 0; }
        };
    }

    @Test
    void characterNeedsPositiveMaxHp() {
        assertThrows(IllegalArgumentException.class, () -> dummy(0));
    }

    @Test
    void newCharacterIsAlive() {
        assertTrue(dummy(10).isAlive());
    }

    @Test
    void characterWithZeroHpIsDead() {
        Character c = dummy(10);
        c.takeDamage(15);
        assertEquals(0, c.getHp());
        assertFalse(c.isAlive());
    }

    @Test
    void negativeDamageDoesNothing() {
        Character c = dummy(10);
        c.takeDamage(-5);
        assertEquals(10, c.getHp());
    }

    @Test
    void healAddsHp() {
        Character c = dummy(10);
        System.out.println("Start: " + c.getHp());
        c.takeDamage(6);
        System.out.println("Efter skada: " + c.getHp());
        c.heal(3);
        System.out.println("Efter heal: " + c.getHp());
        assertEquals(7, c.getHp());
    }

    @Test
    void healCannotExceedMaxHp() {
        Character c = dummy(10);
        c.takeDamage(6);
        c.heal(20);
        assertEquals(10, c.getHp());
    }

    @Test
    void attackReturnsStrategyDamage() {
        Character attacker = dummy(10);
        Character target = dummy(10);
        assertEquals(5, attacker.attack(target));
        assertEquals(10, target.getHp()); // attack drar inte av hp själv
    }

    @Test
    void setAttackChangesStrategy() {
        Character attacker = dummy(10);
        attacker.setAttack((a, t) -> 12);
        assertEquals(12, attacker.attack(dummy(10)));
    }
}
