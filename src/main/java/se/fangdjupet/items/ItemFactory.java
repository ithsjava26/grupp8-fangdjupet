package se.fangdjupet.items;

// Factory (Tanaz): skapar föremålen, så ingen annan behöver veta hur kraften slumpas
public class ItemFactory {

    // Ingen ska skapa en ItemFactory – bara anropa de statiska metoderna
    private ItemFactory() {}

    // Ett vapen. Typen väljer räckvidd: svärd närstrid, spjut båda, båge avstånd, staff magi.
    public static Weapon createWeapon(ItemType type, String description, int level) {
        return switch (type) {
            case ItemType.SWORD -> new Weapon(ItemType.SWORD, WeaponRange.MELEE, description, generatePower(5, 10), level);
            case ItemType.SPEAR -> new Weapon(ItemType.SPEAR, WeaponRange.BOTH, description, generatePower(3, 8), level);
            case ItemType.BOW -> new Weapon(ItemType.BOW, WeaponRange.RANGED, description, generatePower(1, 5), level);
            case ItemType.STAFF -> new Weapon(ItemType.STAFF, WeaponRange.MAGIC, description, generatePower(2, 7), level);
            default -> throw new IllegalArgumentException("Not a weapon: " + type);
        };
    }

    // En rustning: sköld eller hjälm
    public static Armor createArmor(ItemType type, String description, int level) {
        return switch (type) {
            case ItemType.SHIELD -> new Armor(ItemType.SHIELD, description, generatePower(4, 12), level);
            case ItemType.HELMET -> new Armor(ItemType.HELMET, description, generatePower(1, 6), level);
            default -> throw new IllegalArgumentException("Not an armor: " + type);
        };
    }

    // En hälsodryck
    public static Potion createPotion(ItemType type, String description, int level) {
        return switch (type) {
            case ItemType.POTION -> new Potion(ItemType.POTION, description, generatePower(4, 9), level);
            default -> throw new IllegalArgumentException("Not a potion: " + type);
        };
    }

    // Ett slumptal från minPower till och med maxPower
    private static int generatePower(int minPower, int maxPower) {
        return (int) (Math.random() * (maxPower - minPower + 1)) + minPower;
    }
}
