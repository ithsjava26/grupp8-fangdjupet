package se.fangdjupet.items;

public sealed interface Item permits Weapon, Armor, Potion, StressBall {

    ItemType type();

    String description();

    int level();

    String info();
}
