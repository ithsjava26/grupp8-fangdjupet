package se.fangdjupet.combat;

import org.junit.jupiter.api.Test;
import se.fangdjupet.items.Armor;
import se.fangdjupet.items.ItemType;
import se.fangdjupet.items.Weapon;
import se.fangdjupet.items.WeaponRange;

import static org.junit.jupiter.api.Assertions.*;

class MeleeAttackTest {

    // Hjälte utan utrustning: bonus 0, försvar 0
    private Hero hero(AttackStrategy attack) {
        return new Hero("Robin", 30, attack);
    }

    @Test
    void damageIsStrengthPlusBonusMinusDefense() {
        Enemy troll = new Enemy("Troll", 40, new MeleeAttack(10), 4);   // bonus 4
        Hero h = hero(new MeleeAttack(3));
        h.equip(new Armor(ItemType.SHIELD, "Leather", 2, 1));                            // försvar 2
        assertEquals(12, troll.attack(h));                              // 10 + 4 − 2
    }

    @Test
    void armorReducesMeleeDamage() {
        Enemy troll = new Enemy("Troll", 40, new MeleeAttack(10), 1);
        Hero h = hero(new MeleeAttack(3));
        int withoutArmor = troll.attack(h);                             // 10 + 1 − 0 = 11
        h.equip(new Armor(ItemType.SHIELD, "Chainmail", 3, 2));
        int withArmor = troll.attack(h);                                // 10 + 1 − 3 = 8
        assertEquals(11, withoutArmor);
        assertEquals(8, withArmor);
    }

    @Test
    void weaponIncreasesMeleeDamage() {
        Hero h = hero(new MeleeAttack(3));
        Enemy goblin = new Enemy("Goblin", 15, new MeleeAttack(2), 1);  // försvar 1 / 2 = 0
        h.equip(new Weapon(ItemType.SWORD, WeaponRange.MELEE, "Sword", 4, 1));
        h.setAttack(new MeleeAttack(3));                                // equip ger styrka 5 – testet vill ha 3
        assertEquals(7, h.attack(goblin));                              // 3 + 4 − 0
    }

    @Test
    void damageIsNeverNegative() {
        Enemy rat = new Enemy("Rat", 5, new MeleeAttack(1), 0);         // bonus 0
        Hero h = hero(new MeleeAttack(3));
        h.equip(new Armor(ItemType.SHIELD, "Plate", 20, 5));                             // försvar 20
        assertEquals(0, rat.attack(h));                                 // 1 + 0 − 20 → 0, inte −19
    }
}
