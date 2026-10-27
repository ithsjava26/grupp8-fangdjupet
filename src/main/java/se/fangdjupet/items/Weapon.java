package se.fangdjupet.items;

// record = en oföränderlig dataklass. Java skriver konstruktor, getters (name(), level() …),
// equals, hashCode och toString åt dig. name() och level() uppfyller interfacet automatiskt.
public record Weapon(String name, int level, int damageBonus) implements Item {}   // + skada
