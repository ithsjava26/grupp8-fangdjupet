package se.fangdjupet.items;

public class ItemFactory {

    private ItemFactory() {}

    public static Weapon createWeapon(ItemType type, String description, int level) {
        return switch (type) {
            case ItemType.SWORD -> new Weapon(ItemType.SWORD, WeaponRange.MELEE, description, generatePower(5, 10), level);
            case ItemType.SPEAR -> new Weapon(ItemType.SPEAR, WeaponRange.BOTH, description, generatePower(3, 8), level);
            case ItemType.BOW -> new Weapon(ItemType.BOW, WeaponRange.RANGED, description, generatePower(1, 5), level);
            default -> throw new IllegalArgumentException("Not a weapon: " + type);
        };
    }

    public static Armor createArmor(ItemType type, String description, int level) {
        return switch (type) {
            case ItemType.SHIELD -> new Armor(ItemType.SHIELD, description, generatePower(4, 12), level);
            case ItemType.HELMET -> new Armor(ItemType.HELMET, description, generatePower(1, 6), level);
            default -> throw new IllegalArgumentException("Not an armor: " + type);
        };
    }

    public static Potion createPotion(ItemType type, String description, int level) {
        return switch (type) {
            case ItemType.POTION -> new Potion(ItemType.POTION, description, generatePower(4, 9), level);
            default -> throw new IllegalArgumentException("Not a potion: " + type);
        };
    }

    private static int generatePower(int minPower, int maxPower) {
        return (int) (Math.random() * (maxPower - minPower + 1)) + minPower;
    }
}


import java.util.Locale;

public final class ItemFactory {

    private ItemFactory() {}    // bara statiska metoder, ska inte instansieras

    public static Item create(ItemType type, int level) {
        if (type == null) throw new IllegalArgumentException("type får inte vara null");
        if (level < 1) throw new IllegalArgumentException("level måste vara >= 1");
        // switch över en enum utan default: kompilatorn säger till om en ny ItemType inte hanteras
        return switch (type) {
            case SWORD  -> new Weapon("Svärd", level, 2 * level);
            case SPEAR  -> new Weapon("Spjut", level, 2 * level - 1);
            case BOW    -> new Weapon("Båge", level, level + 1);
            case SHIELD -> new Armor("Sköld", level, 2 * level);
            case HELMET -> new Armor("Hjälm", level, level);
            case POTION -> new Potion("Dryck", level, 5 * level);
        };
    }

    // Kontraktet med C: sparfilen innehåller t.ex. "sword;3"
    public static Item create(String type, int level) {
        if (type == null) throw new IllegalArgumentException("type får inte vara null");
        ItemType itemType;
        try {
            itemType = ItemType.valueOf(type.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Okänd föremålstyp: " + type, e);
        }
        return create(itemType, level);
    }
}
