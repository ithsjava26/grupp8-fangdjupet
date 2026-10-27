package se.fangdjupet.combat;

import org.junit.jupiter.api.Test;
import se.fangdjupet.items.Armor;

import static org.junit.jupiter.api.Assertions.*;

class MagicAttackTest {

    // Häxa på nivå 2 (bonus 2) som kastar magi med kraft 6
    private Enemy witch() {
        return new Enemy("Witch", 20, new MagicAttack(6), 2);
    }

    @Test
    void damageEqualsPower() {
        Hero h = new Hero("Robin", 30, new MeleeAttack(3));
        assertEquals(6, witch().attack(h));                    // bara kraften, bonusen räknas inte
    }

    @Test
    void magicIgnoresArmor() {
        Enemy w = witch();
        Hero h = new Hero("Robin", 30, new MeleeAttack(3));
        int withoutArmor = w.attack(h);
        h.equip(new Armor("Plate", 5, 20));                    // jättetjock rustning
        int withArmor = w.attack(h);
        assertEquals(withoutArmor, withArmor);                 // samma skada ändå
    }

    @Test
    void changingStrategyChangesDamage() {
        Hero h = new Hero("Robin", 30, new MeleeAttack(3));
        Enemy troll = new Enemy("Troll", 40, new MeleeAttack(10), 1);   // försvar 1 / 2 = 0
        int melee = h.attack(troll);                           // 3 + 0 − 0 = 3
        h.setAttack(new MagicAttack(9));
        int magic = h.attack(troll);                           // 9
        assertEquals(3, melee);
        assertEquals(9, magic);
    }
}
