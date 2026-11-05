package se.fangdjupet.items;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class Inventory {

    private final List<Item> items;
    private int capacity;

    public Inventory(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be positive");
        }

        this.items = new ArrayList<>();
        this.capacity = capacity;
    }

    public boolean add(Item item) {
        if (item == null) {
            throw new IllegalArgumentException("Item cannot be null");
        }

        if (isFull()) {
            return false;
        }

        return items.add(item);
    }

    public boolean remove(Item item) {
        return items.remove(item);
    }

    public List<Item> getAll() {
        return List.copyOf(items);
    }

    public <T extends Item> List<T> getAll(Class<T> kind) {
        if (kind == null) {
            throw new IllegalArgumentException("Kind cannot be null");
        }

        return items.stream()
                .filter(kind::isInstance)
                .map(kind::cast)
                .toList();
    }

    public int size() {
        return items.size();
    }

    public void setCapacity(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be positive");
        }

        if (capacity < items.size()) {
            throw new IllegalArgumentException("Capacity cannot be less than the number of items");
        }

        this.capacity = capacity;
    }

    public boolean isFull() {
        return items.size() >= capacity;
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public List<Item> findByType(ItemType type) {
        if (type == null) {
            throw new IllegalArgumentException("Type cannot be null");
        }

        return items.stream()
                .filter(item -> item.type() == type)
                .toList();
    }

    public Optional<Weapon> strongestWeapon() {
        return items.stream()
                .filter(Weapon.class::isInstance)
                .map(Weapon.class::cast)
                .max(Comparator.comparingInt(Weapon::damageBonus));
    }
}