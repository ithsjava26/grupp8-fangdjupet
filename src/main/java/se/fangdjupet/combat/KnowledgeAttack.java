package se.fangdjupet.combat;

import java.util.Random;

// Kunskap som vapen – det enda som biter på Martin i Lektionssalen.
// Skadan är en fast kraft som ignorerar försvaret (som MagicAttack). Vissa attacker kostar
// anfallaren hp (selfDamage), och Dela skärm är en chansning mellan två värden.
// Martin själv förklarar också med KnowledgeAttack (se Martin.create()).
public class KnowledgeAttack implements AttackStrategy {

    //Konstanter för olika kunskapsangrepp mot bossen Martin
    public static final KnowledgeAttack DOOMSCROLL = new KnowledgeAttack("DoomScrolla", 5, 1);
    public static final KnowledgeAttack QUIZ = new KnowledgeAttack("Citera från JavaQuizet. Ställ en jobbig fråga.", 4);
    public static final KnowledgeAttack STACKOVERFLOW = new KnowledgeAttack("Citera StackOverflow. Ställ jobbig fråga.", 6, 1);
    // Dela skärm är ett chanstagande: 50/50 att det går bra (6) eller dåligt (1)
    public static final KnowledgeAttack SCREENSHARE = new KnowledgeAttack("Dela skärm och ta över lektionen", 1, 6, new Random());

    private final String name;       // syns i attackmenyn och loggen
    private final int power;         // skadan (den höga, om det är en chansning)
    private final int selfDamage;    // vad attacken kostar anfallaren, t.ex. 1 för DoomScrolla
    private final int lowPower;      // skadan när chansen går dåligt (samma som power om det inte är en chansning)
    private final Random coin;       // null = ingen chansning, skadan är alltid power

    // Vanlig kunskapsattack som kostar anfallaren hp, t.ex. StackOverflow (6 i skada, kostar 1)
    public KnowledgeAttack(String name, int power, int selfDamage) {
        this.name = name;
        this.power = power;
        this.selfDamage = selfDamage;
        this.lowPower = power;
        this.coin = null;
    }

    // Chansning: 50/50 mellan lowPower och highPower varje gång, t.ex. Dela skärm (1 eller 6)
    public KnowledgeAttack(String name, int lowPower, int highPower, Random coin) {
        if (coin == null)
            throw new IllegalArgumentException("coin must not be null");
        this.name = name;
        this.power = highPower;
        this.lowPower = lowPower;
        this.selfDamage = 0;
        this.coin = coin;
    }

    // Kunskapsattack som inte kostar något, t.ex. JavaQuizet
    public KnowledgeAttack(String name, int power) {
        this(name, power, 0);
    }

    // Krävs av AttackStrategy. Försvaret räknas inte – kunskap går rakt igenom.
    @Override
    public int calculateDamage(Character attacker, Character target) {
        if (coin != null) {
            return coin.nextBoolean() ? power : lowPower;   // krona = det gick bra, klave = det gick dåligt
        }
        return power;
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
}
