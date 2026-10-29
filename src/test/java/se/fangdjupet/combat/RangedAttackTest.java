package se.fangdjupet.combat;

import org.junit.jupiter.api.Test;
import se.fangdjupet.items.Armor;
import se.fangdjupet.items.ItemType;
import se.fangdjupet.items.Weapon;
import se.fangdjupet.items.WeaponRange;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class RangedAttackTest {

    // Låtsas-Random: "slår" alltid samma tal, så testet bestämmer om det blir träff eller miss
    private static Random alwaysRolls(int value) {
        return new Random() {
            @Override
            public int nextInt(int bound) {
                return value;
            }
        };
    }

    // Hjälte med pilbåge (styrka 5) och ett svärd som ger bonus 4
    private Hero archer(Random random) {
        Hero h = new Hero("Robin", 30, new RangedAttack(5, random));
        h.equip(new Weapon(ItemType.SWORD, WeaponRange.MELEE, "Sword", 4, 1));
        return h;
    }

    // Mål med rustning: försvar 2
    private Hero target() {
        Hero t = new Hero("Target", 30, new MeleeAttack(1));
        t.equip(new Armor(ItemType.SHIELD, "Leather", 2, 1));
        return t;
    }

    @Test
    void rollUnder20IsAMiss() {
        assertEquals(0, archer(alwaysRolls(5)).attack(target()));
    }

    @Test
    void rollOf20OrMoreHitsWithHalfBonus() {
        assertEquals(5, archer(alwaysRolls(50)).attack(target()));   // 5 + 4/2 − 2
        assertEquals(5, archer(alwaysRolls(20)).attack(target()));   // 20 är precis en träff
    }

    @Test
    void hitDamageIsNeverNegative() {
        Hero t = new Hero("Tank", 30, new MeleeAttack(1));
        t.equip(new Armor(ItemType.SHIELD, "Plate", 20, 5));                          // försvar 20
        assertEquals(0, archer(alwaysRolls(50)).attack(t));          // 5 + 2 − 20 → 0
    }

    @Test
    void sameSeedGivesSameDamage() {
        Hero a = archer(new Random(42));
        Hero b = archer(new Random(42));
        Hero t = target();
        for (int i = 0; i < 20; i++) {
            assertEquals(a.attack(t), b.attack(t));                  // samma frö → samma serie
        }
    }

    @Test
    void manyShotsGiveBothHitsAndMisses() {
        Hero a = archer(new Random(42));
        Hero t = target();
        int misses = 0;
        for (int i = 0; i < 1000; i++) {
            if (a.attack(t) == 0) {
                misses++;
            }
        }
        // runt 20 % miss – fast frö gör att talet blir detsamma varje körning
        assertTrue(misses > 100 && misses < 300, "missar: " + misses);
    }
}
