package se.fangdjupet.items;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.junit.jupiter.api.Assertions.*;

class ItemFactoryTest {

    @Test
    void swordIsWeaponWithBonusFromLevel() {
        Item item = ItemFactory.create(ItemType.SWORD, 3);
        Weapon sword = assertInstanceOf(Weapon.class, item);
        assertEquals(3, sword.level());
        assertEquals(6, sword.damageBonus());
    }

    @Test
    void shieldIsArmor() {
        Armor shield = assertInstanceOf(Armor.class, ItemFactory.create(ItemType.SHIELD, 2));
        assertEquals(4, shield.defense());
    }

    @Test
    void potionHealsMoreAtHigherLevel() {
        Potion low = assertInstanceOf(Potion.class, ItemFactory.create(ItemType.POTION, 1));
        Potion high = assertInstanceOf(Potion.class, ItemFactory.create(ItemType.POTION, 4));
        assertTrue(high.healAmount() > low.healAmount());
    }

    // Varje typ ska gå att skapa och få rätt nivå
    @ParameterizedTest
    @EnumSource(ItemType.class)
    void everyTypeCanBeCreated(ItemType type) {
        Item item = ItemFactory.create(type, 5);
        assertNotNull(item.name());
        assertEquals(5, item.level());
    }

    @Test
    void stringVersionMatchesSaveFormat() {
        // sparfilen innehåller "sword;3"
        assertEquals(ItemFactory.create(ItemType.SWORD, 3), ItemFactory.create("sword", 3));
        assertEquals(ItemFactory.create(ItemType.BOW, 2), ItemFactory.create(" BOW ", 2));
    }

    @Test
    void unknownTypeThrows() {
        assertThrows(IllegalArgumentException.class, () -> ItemFactory.create("banana", 1));
    }

    @Test
    void levelMustBePositive() {
        assertThrows(IllegalArgumentException.class, () -> ItemFactory.create(ItemType.SWORD, 0));
        assertThrows(IllegalArgumentException.class, () -> ItemFactory.create("sword", -1));
    }

    @Test
    void nullTypeThrows() {
        assertThrows(IllegalArgumentException.class, () -> ItemFactory.create((ItemType) null, 1));
        assertThrows(IllegalArgumentException.class, () -> ItemFactory.create((String) null, 1));
    }
}
