package se.fangdjupet.game;

import se.fangdjupet.combat.Enemy;
import se.fangdjupet.items.Item;

import java.util.ArrayList;
import java.util.List;

public class Room {
    private final String name;
    private final String description;
    private final Enemy enemy;
    private final List<Item> items;

    public Room(String name, String description, Enemy enemy, List<Item> items) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("description must not be blank");
        }

        this.name = name;
        this.description = description;
        this.enemy = enemy;
        this.items = new ArrayList<>(items);
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Enemy getEnemy() {
        return enemy;
    }

    public List<Item> getItems() {
        return List.copyOf(items);
    }

    public boolean hasLivingEnemy() {
        return enemy != null && enemy.isAlive();
    }

    public boolean removeItem(Item item) {
        return items.remove(item);
    }
}