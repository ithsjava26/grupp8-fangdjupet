package se.fangdjupet.combat;

import org.junit.jupiter.api.Test;
import se.fangdjupet.items.Armor;
import se.fangdjupet.items.ItemType;
import se.fangdjupet.items.Potion;
import se.fangdjupet.items.Weapon;
import se.fangdjupet.items.WeaponRange;

import static org.junit.jupiter.api.Assertions.*;

class HeroTest {

    // Hjälte med en strategi som alltid gör 5 i skada
    private Hero hero() {
        return new Hero("Robin", 30, (a, t) -> 5);
    }

    @Test
    void heroWithoutGearHasNoBonusOrDefense() {
        Hero h = hero();
        assertEquals(0, h.getAttackBonus());
        assertEquals(0, h.getDefense());
    }

    @Test
    void weaponGivesAttackBonus() {
        Hero h = hero();
        h.equip(new Weapon(ItemType.SWORD, WeaponRange.MELEE, "Sword", 4, 1));
        assertEquals(4, h.getAttackBonus());
    }

    @Test
    void armorGivesDefense() {
        Hero h = hero();
        h.equip(new Armor(ItemType.SHIELD, "Leather", 2, 1));
        assertEquals(2, h.getDefense());
    }

    @Test
    void newWeaponReplacesOld() {
        Hero h = hero();
        h.equip(new Weapon(ItemType.SWORD, WeaponRange.MELEE, "Dagger", 2, 1));
        h.equip(new Weapon(ItemType.SWORD, WeaponRange.MELEE, "Axe", 6, 2));
        assertEquals(6, h.getAttackBonus());
    }

    @Test
    void bowGivesRangedAttack() {
        Hero h = hero();
        h.equip(new Weapon(ItemType.BOW, WeaponRange.RANGED, "Bow", 3, 1));
        assertInstanceOf(RangedAttack.class, h.getAttack());
    }

    @Test
    void swordGivesMeleeAttack() {
        Hero h = hero();
        h.equip(new Weapon(ItemType.SWORD, WeaponRange.MELEE, "Sword", 4, 1));
        assertInstanceOf(MeleeAttack.class, h.getAttack());
    }

    @Test
    void spearKeepsCurrentAttack() {
        Hero h = hero();
        AttackStrategy before = h.getAttack();
        h.equip(new Weapon(ItemType.SPEAR, WeaponRange.BOTH, "Spear", 5, 1));
        assertSame(before, h.getAttack());
    }

    @Test
    void switchingFromBowToSwordGivesMeleeAttack() {
        Hero h = hero();
        h.equip(new Weapon(ItemType.BOW, WeaponRange.RANGED, "Bow", 3, 1));
        h.equip(new Weapon(ItemType.SWORD, WeaponRange.MELEE, "Sword", 4, 1));
        assertInstanceOf(MeleeAttack.class, h.getAttack());
    }

    @Test
    void equipNullWeaponThrows() {
        assertThrows(IllegalArgumentException.class, () -> hero().equip((Weapon) null));
    }

    @Test
    void drinkHeals() {
        Hero h = hero();
        h.takeDamage(10);
        h.drink(new Potion(ItemType.POTION, "Small potion", 5, 1));
        assertEquals(25, h.getHp());
    }

    @Test
    void drinkCannotExceedMaxHp() {
        Hero h = hero();
        h.takeDamage(3);
        h.drink(new Potion(ItemType.POTION, "Big potion", 50, 3));
        assertEquals(30, h.getHp());
    }
}
