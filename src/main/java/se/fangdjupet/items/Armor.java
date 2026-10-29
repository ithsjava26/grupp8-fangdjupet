package se.fangdjupet.items;

public record Armor(ItemType type, String description, int defense, int level) implements Item {

    @Override
    public String info() {
        return "Description: " + description + ". Defense: " + defense;
    }
}
