package se.fangdjupet.game;

import se.fangdjupet.combat.*;
import se.fangdjupet.items.WeaponRange;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

// Attackmenyn i en strid: vilka attacker spelaren får välja, hur menyn ser ut och vad ett val betyder.
// Ingen inmatning här – GameEngine skriver ut lines(...) och skickar in det spelaren skrev till choose(...).
// Därför går allt att testa utan tangentbord.
public final class AttackMenu {
    private AttackMenu() {}

    // Med Java-boken: Martins fyra KnowledgeAttack och citat ur boken. Annars närstrid, magi och avstånd.
    public static List<AttackStrategy> options(Hero hero) {
        if (hero.hasJavaBook()) {
            List<AttackStrategy> knowledge = new ArrayList<>(Martin.heroChoices());
            knowledge.add(hero.getJavaBookAttack());   // 5. Citera från Java-boken, kapitel 18 / 20 ...
            return List.copyOf(knowledge);
        }
        int magic = hero.holds(WeaponRange.MAGIC)
                ? STAFF_MAGIC + hero.getWeapon().orElseThrow().damageBonus()   // staven: magin blir mycket starkare
                : MAGIC;
        int ranged = hero.holds(WeaponRange.RANGED) ? BOW_RANGED : RANGED;   // pilbågen: skjuter på riktigt
        return List.of(new MeleeAttack(MELEE), new MagicAttack(magic), new RangedAttack(ranged, new Random()));
    }

    // Menyns attack för vapnet hjälten håller: pilbågen = avstånd, staven = magi, svärdet = närstrid.
    // Tomt med Java-boken (bara kunskap mot Martin), utan vapen, eller med spjutet (passar båda).
    public static Optional<AttackStrategy> forWeapon(Hero hero) {
        if (hero.hasJavaBook() || hero.getWeapon().isEmpty()) return Optional.empty();
        List<AttackStrategy> options = options(hero);
        return switch (hero.getWeapon().get().range()) {
            case MELEE -> Optional.of(options.get(0));
            case MAGIC -> Optional.of(options.get(1));
            case RANGED -> Optional.of(options.get(2));
            case BOTH -> Optional.empty();
        };
    }

    // Styrkorna i attackmenyn. Utan vapen är närstriden bäst; med pilbågen eller staven är deras attack mycket bättre.
    // Pilbågen lägger dessutom till halva sin bonus (RangedAttack), och avstånd missar 20 % av skotten.
    // Magi går rakt igenom rustningen och missar aldrig (bara Battles vanliga miss).
    static final int MELEE = 7;          // knytnävarna (bonus bara med svärd eller spjut)
    static final int MAGIC = 4;          // magi utan stav
    static final int RANGED = 4;         // kasta saker utan pilbåge
    static final int STAFF_MAGIC = 8;    // magi med Trollstaven: 8 + stavens bonus
    static final int BOW_RANGED = 13;    // pil och båge: 11 + halva bågens bonus

    // Menyraderna med nummer, t.ex. "1. Närstrid" eller "1. DoomScrolla"
    public static List<String> lines(List<AttackStrategy> options) {
        List<String> lines = new ArrayList<>();
        for (int i = 0; i < options.size(); i++) {
            lines.add((i + 1) + ". " + label(options.get(i)));   // numret är positionen + 1
        }
        return lines;
    }

    // Attackens namn på svenska, t.ex. "Närstrid". Används i menyn och i "Visa hjältens utrustning".
    public static String label(AttackStrategy attack) {
        return switch (attack) {
            case MeleeAttack m -> "Närstrid";
            case MagicAttack m -> "Magi";
            case RangedAttack r -> "Avståndsattack";
            default -> attack.name();             // KnowledgeAttack visar sin egen text
        };
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
