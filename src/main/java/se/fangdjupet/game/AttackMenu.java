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

    // Med Java-boken: Martins fyra KnowledgeAttack och citat ur boken. Annars närstrid – och magi och
    // avstånd först när hjälten har plockat upp staven eller pilbågen. I början finns bara svärdet.
    public static List<AttackStrategy> options(Hero hero) {
        if (hero.hasJavaBook()) {
            List<AttackStrategy> knowledge = new ArrayList<>(Martin.heroChoices());
            knowledge.add(hero.getJavaBookAttack());   // 5. Citera från Java-boken, kapitel 18 / 20 ...
            if (hero.getStressBall().isPresent()) knowledge.add(StressBallThrow.THROW);   // 6. Kasta stressbollen
            return List.copyOf(knowledge);
        }
        int magic = hero.holds(WeaponRange.MAGIC)
                ? STAFF_MAGIC + hero.getWeapon().orElseThrow().damageBonus()   // staven: magin blir mycket starkare
                : MAGIC;
        int ranged = hero.holds(WeaponRange.RANGED) ? BOW_RANGED : RANGED;   // pilbågen: skjuter på riktigt
        List<AttackStrategy> options = new ArrayList<>();
        options.add(new MeleeAttack(MELEE));
        if (hero.hasPickedUp(WeaponRange.MAGIC)) options.add(new MagicAttack(magic));
        if (hero.hasPickedUp(WeaponRange.RANGED)) options.add(new RangedAttack(ranged, new Random()));
        return List.copyOf(options);
    }

    // Menyns attack för vapnet hjälten håller: pilbågen = avstånd, staven = magi, svärdet = närstrid.
    // Tomt med Java-boken (bara kunskap mot Martin), utan vapen, eller med spjutet (passar båda).
    public static Optional<AttackStrategy> forWeapon(Hero hero) {
        if (hero.hasJavaBook() || hero.getWeapon().isEmpty()) return Optional.empty();
        WeaponRange range = hero.getWeapon().get().range();
        return options(hero).stream().filter(attack -> belongsTo(range, attack)).findFirst();
    }

    // Hör attacken till vapnet? Pilbågen = avstånd, staven = magi, svärdet = närstrid (spjutet: ingen egen)
    static boolean belongsTo(WeaponRange range, AttackStrategy attack) {
        return switch (range) {
            case MELEE -> attack instanceof MeleeAttack;
            case MAGIC -> attack instanceof MagicAttack;
            case RANGED -> attack instanceof RangedAttack;
            case BOTH -> false;
        };
    }

    // Gulmarkeras attacken i menyn? Ja för pilbågens och stavens attack när hjälten håller vapnet –
    // de slår mycket hårdare än svärdet, och utan dem klarar man inte trollen och StädHäxan.
    static boolean highlighted(Hero hero, AttackStrategy attack) {
        return hero.getWeapon()
                .filter(w -> w.range() == WeaponRange.MAGIC || w.range() == WeaponRange.RANGED)
                .map(w -> belongsTo(w.range(), attack))
                .orElse(false);
    }

    // Styrkorna i attackmenyn. Svärdet (närstrid) är svagt; med pilbågen eller staven är deras attack mycket bättre.
    // Pilbågen lägger dessutom till halva sin bonus (RangedAttack), och avstånd missar 20 % av skotten.
    // Magi går rakt igenom rustningen och missar aldrig (bara Battles vanliga miss).
    // Magi och avstånd finns i menyn först när staven eller pilbågen har plockats upp.
    static final int MELEE = 5;          // närstrid: 5 + svärdets bonus (SWORD_BONUS)
    static final int SWORD_BONUS = 1;    // startsvärdet: +1 i närstrid
    static final int MAGIC = 4;          // magi när staven inte är i handen längre
    static final int RANGED = 4;         // avstånd när pilbågen inte är i handen längre
    static final int STAFF_MAGIC = 8;    // magi med Trollstaven: 8 + stavens bonus
    static final int BOW_RANGED = 13;    // pil och båge: 11 + halva bågens bonus

    // Menyraderna för hjälten: pilbågens och stavens attack gulmarkerade när vapnet är i handen
    public static List<String> lines(Hero hero, List<AttackStrategy> options) {
        List<String> lines = lines(options);
        for (int i = 0; i < options.size(); i++) {
            if (highlighted(hero, options.get(i))) lines.set(i, AsciiArt.yellow(lines.get(i)));
        }
        return lines;
    }

    // Menyraderna med nummer, t.ex. "1. Närstrid" eller "1. DoomScrolla"
    public static List<String> lines(List<AttackStrategy> options) {
        List<String> lines = new ArrayList<>();
        for (int i = 0; i < options.size(); i++) {
            lines.add((i + 1) + ". " + label(options.get(i)) + effect(options.get(i)));   // numret är positionen + 1
        }
        return lines;
    }

    // Vad attacken gör mot Martin, i menyn: "(14 i skada, 30 % risk att du slinter på sidan)", "(+10 hp till dig, ingen skada)"
    static String effect(AttackStrategy attack) {
        return switch (attack) {
            case KnowledgeAttack k when k.selfHeal() > 0 -> "  (+" + k.selfHeal() + " hp till dig, ingen skada, missar aldrig)";
            case KnowledgeAttack k when k.lowPower() != k.power() -> "  (" + k.lowPower() + " eller " + k.power() + " i skada)";
            case KnowledgeAttack k -> "  (" + k.power() + " i skada" + (k.selfDamage() > 0 ? ", kostar dig " + k.selfDamage() + " hp" : "") + ")";
            case JavaBookAttack b -> "  (" + JavaBookAttack.POWER + " i skada, " + JavaBookAttack.SLIP_PERCENT + " % risk att du slinter på sidan)";
            case StressBallThrow t -> "  (" + StressBallThrow.POWER + " i skada om den träffar – sedan är bollen borta: ingen +5 hp per runda)";
            default -> "";
        };
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
