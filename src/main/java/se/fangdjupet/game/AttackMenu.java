package se.fangdjupet.game;

import se.fangdjupet.combat.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// Attackmenyn i en strid: vilka attacker spelaren får välja, hur menyn ser ut och vad ett val betyder.
// Ingen inmatning här – GameEngine skriver ut lines(...) och skickar in det spelaren skrev till choose(...).
// Därför går allt att testa utan tangentbord.
public final class AttackMenu {
    private AttackMenu() {}

    // Med Java-boken: bara Martins fyra KnowledgeAttack. Annars närstrid, magi och avstånd.
    public static List<AttackStrategy> options(Hero hero) {
        if (hero.hasJavaBook()) {
            return List.copyOf(Martin.heroChoices());
        }
        return List.of(new MeleeAttack(10), new MagicAttack(6), new RangedAttack(5, new Random()));
    }

    // Menyraderna med nummer, t.ex. "1. Närstrid" eller "1. DoomScrolla"
    public static List<String> lines(List<AttackStrategy> options) {
        List<String> lines = new ArrayList<>();
        for (int i = 0; i < options.size(); i++) {
            AttackStrategy attack = options.get(i);
            String label = switch (attack) {
                case MeleeAttack m -> "Närstrid";
                case MagicAttack m -> "Magi";
                case RangedAttack r -> "Avståndsattack";
                default -> attack.name();             // KnowledgeAttack visar sin egen text
            };
            lines.add((i + 1) + ". " + label);       // numret är positionen + 1
        }
        return lines;
    }

    // Spelarens val: ett nummer i menyn ger den attacken. Enter, text eller fel nummer ger null = behåll.
    public static AttackStrategy choose(List<AttackStrategy> options, String input) {
        if (input == null || input.isBlank()) return null;   // Enter
        int number;
        try {
            number = Integer.parseInt(input.trim());
        } catch (NumberFormatException e) {
            return null;                                     // inte ett tal
        }
        if (number < 1 || number > options.size()) return null;   // utanför menyn
        return options.get(number - 1);
    }
}
