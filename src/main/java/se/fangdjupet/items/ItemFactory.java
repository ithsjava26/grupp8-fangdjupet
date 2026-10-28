package se.fangdjupet.items;

public class ItemFactory {

    private ItemFactory() {}

    public static Weapon createWeapon(ItemType type, String description) {
        return switch (type) {
            case ItemType.SWORD -> new Weapon(ItemType.SWORD, WeaponRange.MELEE, description, generatePower(5, 10));
            case ItemType.SPEAR -> new Weapon(ItemType.SPEAR, WeaponRange.BOTH, description, generatePower(3, 8));
            case ItemType.BOW -> new Weapon(ItemType.BOW, WeaponRange.RANGED, description, generatePower(1, 5));
            default -> throw new IllegalArgumentException("Not a weapon: " + type);
        };
    }

    public static Armor createArmor(ItemType type, String description) {
        return switch (type) {
            case ItemType.SHIELD -> new Armor(ItemType.SHIELD, description, generatePower(4, 12));
            case ItemType.HELMET -> new Armor(ItemType.HELMET, description, generatePower(1, 6));
            default -> throw new IllegalArgumentException("Not an armor: " + type);
        };
    }

    public static Potion createPotion(ItemType type, String description) {
        return switch (type) {
            case ItemType.POTION -> new Potion(ItemType.POTION, description, generatePower(4, 9));
            default -> throw new IllegalArgumentException("Not a potion: " + type);
        };
    }

    private static int generatePower(int minPower, int maxPower) {
        return (int) (Math.random() * (maxPower - minPower + 1)) + minPower;
    }
}


