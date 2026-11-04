package se.fangdjupet.items;

public sealed interface Item permits Weapon, Armor, Potion, StressBall, Brasklapp {

    ItemType type();

    String description();

    int level();

    String info();
}
