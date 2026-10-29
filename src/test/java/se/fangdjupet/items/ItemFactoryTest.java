package se.fangdjupet.items;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ItemFactoryTest {

    @Test
    public void testCreateWeaponSpear() {
        Weapon weapon = ItemFactory.createWeapon(ItemType.SPEAR, "The celestial point", 42);
        assertNotNull(weapon);
        assertEquals(42, weapon.level());
        assertEquals(ItemType.SPEAR, weapon.type());
        assertEquals("The celestial point", weapon.description());
        assertEquals(WeaponRange.BOTH, weapon.range());
        assertTrue(3 <= weapon.damageBonus() && 8 >= weapon.damageBonus());
    }

    @Test
    public void testCreateWeaponSword() {
        Weapon weapon = ItemFactory.createWeapon(ItemType.SWORD, "Big sword", 42);
        assertNotNull(weapon);
        assertEquals(42, weapon.level());
        assertEquals(ItemType.SWORD, weapon.type());
        assertEquals("Big sword", weapon.description());
        assertEquals(WeaponRange.MELEE, weapon.range());
        assertTrue(5 <= weapon.damageBonus() && 10 >= weapon.damageBonus());
    }

    @Test
    public void testCreateWeaponBow() {
        Weapon weapon = ItemFactory.createWeapon(ItemType.BOW, "Magic bow", 42);
        assertNotNull(weapon);
        assertEquals(42, weapon.level());
        assertEquals(ItemType.BOW, weapon.type());
        assertEquals("Magic bow", weapon.description());
        assertEquals(WeaponRange.RANGED, weapon.range());
        assertTrue(1 <= weapon.damageBonus() && 5 >= weapon.damageBonus());
    }

    @Test
    public void shouldThrowWhenNotAWeapon() {
        assertThrows(IllegalArgumentException.class, () -> ItemFactory.createWeapon(ItemType.SHIELD, "Normal shield", 42));
    }

    @Test
    public void testCreateArmorShield() {
        Armor armor = ItemFactory.createArmor(ItemType.SHIELD, "The golden shield", 42);
        assertNotNull(armor);
        assertEquals(42, armor.level());
        assertEquals(ItemType.SHIELD, armor.type());
        assertEquals("The golden shield", armor.description());
        assertTrue(4 <= armor.defense() && 12 >= armor.defense());
    }

    @Test
    public void testCreateArmorHelmet() {
        Armor armor = ItemFactory.createArmor(ItemType.HELMET, "Viking helmet", 42);
        assertNotNull(armor);
        assertEquals(42, armor.level());
        assertEquals(ItemType.HELMET, armor.type());
        assertEquals("Viking helmet", armor.description());
        assertTrue(1 <= armor.defense() && 6 >= armor.defense());
    }

    @Test
    public void shouldThrowWhenNotAArmor() {
        assertThrows(IllegalArgumentException.class, () -> ItemFactory.createArmor(ItemType.POTION, "A herbal potion", 42));
    }

    @Test
    public void testCreatePotion() {
        Potion potion = ItemFactory.createPotion(ItemType.POTION, "A herbal potion", 42);
        assertNotNull(potion);
        assertEquals(42, potion.level());
        assertEquals(ItemType.POTION, potion.type());
        assertEquals("A herbal potion", potion.description());
        assertTrue(4 <= potion.healAmount() && 9 >= potion.healAmount());
    }

    @Test
    public void shouldThrowWhenNotAPotion() {
        assertThrows(IllegalArgumentException.class, () -> ItemFactory.createPotion(ItemType.BOW, "Magic bow", 42));
    }
}
