package se.fangdjupet.combat;

import java.util.Random;

// StädHäxans attacker: hon kastar och sprayar städgrejer på avstånd.
// Det är en RangedAttack – samma uträkning som pilbågen (20 % miss, annars
// styrka + halva bonusen − målets försvar) – men varje attack har eget namn och verb i loggen,
// t.ex. "Enemy: StädHäxan kastar en tvättsvamp på Robert skada: 7".
public class CleaningAttack extends RangedAttack {

    public static final CleaningAttack SPONGE =
            new CleaningAttack("Tvättsvamp", "kastar en tvättsvamp på", 7, new Random());
    public static final CleaningAttack SVINTO =
            new CleaningAttack("Svinto", "kastar Svinto på", 9, new Random());
    // Kemikalierna (spolarvätska) – det hårdaste, tas fram vid halva livet
    public static final CleaningAttack CHEMICALS =
            new CleaningAttack("Kemikalier", "sprayar kemikalier på", 11, new Random());
    // Kemikalieangreppet när hon ser sin Städrustning på hjälten: Vanish rakt i ansiktet.
    // Förklädet skyddar inte ansiktet – Vanish går rakt igenom rustningen (försvaret räknas inte).
    public static final CleaningAttack VANISH =
            new CleaningAttack("Vanish", "sprejar Vanish i ansiktet på", 10, new Random(), true);

    private final String name;
    private final String verb;
    private final boolean throughArmor;   // true = försvaret hjälper inte (Vanish i ansiktet)

    // Uträkningen och missen ärvs från RangedAttack – här läggs bara namn och verb till
    public CleaningAttack(String name, String verb, int strength, Random random) {
        this(name, verb, strength, random, false);
    }

    public CleaningAttack(String name, String verb, int strength, Random random, boolean throughArmor) {
        super(strength, random);
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("name must not be blank");
        if (verb == null || verb.isBlank())
            throw new IllegalArgumentException("verb must not be blank");
        if (random == null)
            throw new IllegalArgumentException("random must not be null");
        this.name = name;
        this.verb = verb;
        this.throughArmor = throughArmor;
    }

    // Går attacken rakt igenom rustningen?
    public boolean throughArmor() {
        return throughArmor;
    }

    // Vanish i ansiktet: rustningens försvar räknas inte
    @Override
    protected int defenseOf(Character target) {
        return throughArmor ? 0 : super.defenseOf(target);
    }

    // Namnet i stället för klassnamnet, t.ex. "Svinto"
    @Override
    public String name() {
        return name;
    }

    // Ordet i loggen: "StädHäxan <verb> Robert skada: 9"
    @Override
    public String verb() {
        return verb;
    }
}
