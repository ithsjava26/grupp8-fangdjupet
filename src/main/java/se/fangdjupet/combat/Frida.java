package se.fangdjupet.combat;

import se.fangdjupet.items.ItemType;
import se.fangdjupet.items.Potion;

import java.util.List;

// Frida är den goda karaktären. Hon slåss inte (ingen Character), utan dyker upp
// när StädHäxan är besegrad och ger två hälsodrycker – bara en gång.
public final class Frida {
    // Två drycker à 20 hp räcker till full hälsa (40) även från 1 hp, innan man möter Martin
    static final int POTION_HEAL = 20;

    private boolean given;   // har hon redan gett sin gåva?

    private Frida() {}

    // Skapar Frida – WorldBuilder ställer henne i Korridoren och städskrubben
    public static Frida create() {
        return new Frida();
    }

    public String getName() {
        return "Frida";
    }

    // Det hon säger när hon dyker upp
    public String greeting() {
        return "Du ser trött ut! Ta de här två innan du möter Martin.";
    }

    // Första gången: två hälsodrycker. Därefter: ingenting.
    public List<Potion> give() {
        if (given) return List.of();
        given = true;
        return List.of(potion(), potion());
    }

    // true när gåvan redan är given
    public boolean hasGiven() {
        return given;
    }

    // En av hennes två hälsodrycker
    private static Potion potion() {
        return new Potion(ItemType.POTION, "Fridas hälsodryck", POTION_HEAL, 1);
    }
}
