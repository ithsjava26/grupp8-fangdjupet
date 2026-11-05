package se.fangdjupet.combat;

import java.util.Random;

// Kunskap som vapen – det enda som biter på Martin i Lektionssalen.
// Skadan är en fast kraft som ignorerar försvaret (som MagicAttack). Vissa attacker kostar
// anfallaren hp (selfDamage). Dela skärm gör ingen skada alls: den ger hjälten tid att andas (+12 hp)
// och missar aldrig. En chansning mellan två värden går också (konstruktorn med Random).
// Martin själv förklarar också med KnowledgeAttack (se Martin.create()).
public class KnowledgeAttack implements AttackStrategy {

    //Konstanter för olika kunskapsangrepp mot bossen Martin
    public static final KnowledgeAttack DOOMSCROLL = new KnowledgeAttack("DoomScrolla", 9, 2);
    public static final KnowledgeAttack QUIZ = new KnowledgeAttack("Citera från JavaQuizet. Ställ en jobbig fråga.", 7);
    public static final KnowledgeAttack STACKOVERFLOW = new KnowledgeAttack("Citera StackOverflow. Ställ jobbig fråga.", 11, 3);
    // Dela skärm: hjälten tar över lektionen och får tid – +12 hp, ingen skada på Martin, missar aldrig
    public static final int SCREENSHARE_HEAL = 12;
    public static final KnowledgeAttack SCREENSHARE = healing("Dela skärm och ta över lektionen", SCREENSHARE_HEAL);

    private final String name;       // syns i attackmenyn och loggen
    private final int power;         // skadan (den höga, om det är en chansning)
    private final int selfDamage;    // vad attacken kostar anfallaren, t.ex. 2 för DoomScrolla
    private final int lowPower;      // skadan när chansen går dåligt (samma som power om det inte är en chansning)
    private final Random coin;       // null = ingen chansning, skadan är alltid power
    private final int selfHeal;      // hp till anfallaren i stället för skada (Dela skärm)

    // Alla konstruktorer hamnar här
    private KnowledgeAttack(String name, int power, int lowPower, int selfDamage, int selfHeal, Random coin) {
        this.name = name;
        this.power = power;
        this.lowPower = lowPower;
        this.selfDamage = selfDamage;
        this.selfHeal = selfHeal;
        this.coin = coin;
    }

    // Vanlig kunskapsattack som kostar anfallaren hp, t.ex. StackOverflow (11 i skada, kostar 3)
    public KnowledgeAttack(String name, int power, int selfDamage) {
        this(name, power, power, selfDamage, 0, null);
    }

    // Chansning: 50/50 mellan lowPower och highPower varje gång
    public KnowledgeAttack(String name, int lowPower, int highPower, Random coin) {
        if (coin == null)
            throw new IllegalArgumentException("coin must not be null");
        this(name, highPower, lowPower, 0, 0, coin);
    }

    // Kunskapsattack som inte kostar något, t.ex. JavaQuizet
    public KnowledgeAttack(String name, int power) {
        this(name, power, 0);
    }

    // Kunskap som ger anfallaren hp i stället för att skada, t.ex. Dela skärm (+10)
    public static KnowledgeAttack healing(String name, int heal) {
        if (heal <= 0)
            throw new IllegalArgumentException("heal must be positive");
        return new KnowledgeAttack(name, 0, 0, 0, heal, null);
    }

    // Krävs av AttackStrategy. Försvaret räknas inte – kunskap går rakt igenom. Bara ullvantarna dämpar den.
    @Override
    public int calculateDamage(Character attacker, Character target) {
        int damage = coin == null || coin.nextBoolean() ? power : lowPower;   // krona = det gick bra, klave = det gick dåligt
        return Math.max(0, damage - target.getKnowledgeDefense());
    }

    // Namnet i stället för klassnamnet, så menyn visar "DoomScrolla" och inte "KnowledgeAttack"
    @Override
    public String name() {
        return name;
    }

    // Battle drar av det här från anfallaren efter slaget
    @Override
    public int selfDamage() {
        return selfDamage;
    }

    // Battle ger anfallaren det här i stället för att slå (Dela skärm)
    @Override
    public int selfHeal() {
        return selfHeal;
    }

    // Den som ger sig själv hp kan inte missa – det finns inget att träffa
    @Override
    public int missPercent() {
        return selfHeal > 0 ? 0 : AttackStrategy.super.missPercent();
    }

    // I loggen: "Robert delar skärmen och tar över lektionen: +12 hp, ingen skada på Lektor Martin"
    @Override
    public String verb() {
        return selfHeal > 0 ? "delar skärmen och tar över lektionen" : AttackStrategy.super.verb();
    }

    // Skadan, för attackmenyn (den höga, om det är en chansning)
    public int power() {
        return power;
    }

    // Skadan när chansningen går dåligt (samma som power annars)
    public int lowPower() {
        return lowPower;
    }
}
