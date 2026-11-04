package se.fangdjupet.combat;

import se.fangdjupet.items.Brasklapp;
import se.fangdjupet.items.ItemType;
import se.fangdjupet.items.Potion;

import java.util.List;

// Frida är den goda karaktären. Hon slåss inte (ingen Character), utan dyker upp
// när StädHäxan är besegrad och ger två hälsodrycker – bara en gång.
public final class Frida {
    // Två drycker à 20 hp räcker till full hälsa (40) även från 1 hp, innan man möter Martin
    static final int POTION_HEAL = 20;

    // Efter dryckerna hittar hon något i fickan
    public static final String SURPRISE = "Nä men vad är det här?";
    public static final String NOTE_GIVEN = "Frida ger dig en brasklapp med en Java-referensvariabel som du kan använda mot Martin.";

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

    // Brasklappen: DROP TABLE mot Martin, en gång (BrasklappAttack)
    public Brasklapp brasklapp() {
        return new Brasklapp(ItemType.NOTE, "Brasklapp", BrasklappAttack.POWER, 3);
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
