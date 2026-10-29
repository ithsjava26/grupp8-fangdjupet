package se.fangdjupet.items;

public sealed interface Item permits Weapon, Armor, Potion {

    ItemType type();

    String description();

    int level();

    String info();
}
