package se.fangdjupet.items;

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
