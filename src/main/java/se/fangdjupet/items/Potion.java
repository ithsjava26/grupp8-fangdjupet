package se.fangdjupet.items;

public record Potion(ItemType type, String description, int healAmount, int level) implements Item {

    @Override
    public String info() {
        return "Description: " + description + ". Heal amount: " + healAmount;
    }
}

