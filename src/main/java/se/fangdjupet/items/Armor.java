package se.fangdjupet.items;

public record Armor(ItemType type, String description, int defense) implements Item {

    @Override
    public String info() {
        return "Description: " + description + ". Defense: " + defense;
    }
}
