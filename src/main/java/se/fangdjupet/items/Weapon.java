package se.fangdjupet.items;

import java.util.logging.Level;

public record Weapon(ItemType type, WeaponRange range, String description, int damageBonus) implements Item {

    @Override
    public String info() {
        return "Description: " + description + ". Damage bonus: " + damageBonus;
    }
}

