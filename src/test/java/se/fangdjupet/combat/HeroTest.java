package se.fangdjupet.combat;

import org.junit.jupiter.api.Test;
import se.fangdjupet.items.Armor;
import se.fangdjupet.items.Potion;
import se.fangdjupet.items.Weapon;

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
        h.equip(new Weapon("Sword", 1, 4));
        assertEquals(4, h.getAttackBonus());
    }

    @Test
    void armorGivesDefense() {
        Hero h = hero();
        h.equip(new Armor("Leather", 1, 2));
        assertEquals(2, h.getDefense());
    }

    @Test
    void newWeaponReplacesOld() {
        Hero h = hero();
        h.equip(new Weapon("Dagger", 1, 2));
        h.equip(new Weapon("Axe", 2, 6));
        assertEquals(6, h.getAttackBonus());
    }

    @Test
    void drinkHeals() {
        Hero h = hero();
        h.takeDamage(10);
        h.drink(new Potion("Small potion", 1, 5));
        assertEquals(25, h.getHp());
    }

    @Test
    void drinkCannotExceedMaxHp() {
        Hero h = hero();
        h.takeDamage(3);
        h.drink(new Potion("Big potion", 3, 50));
        assertEquals(30, h.getHp());
    }
}
