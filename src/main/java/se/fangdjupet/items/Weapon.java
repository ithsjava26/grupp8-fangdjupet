package se.fangdjupet.items;

public record Weapon(ItemType type, WeaponRange range, String description, int damageBonus, int level) implements Item {

    @Override
    public String info() {
        return "Description: " + description + ". Damage bonus: " + damageBonus;
    }
}

