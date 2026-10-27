package se.fangdjupet.combat;

// abstract = det går inte att skriva new Character(...). Man skapar en Hero eller Enemy,
// som ärver allt här och fyller i de två abstrakta metoderna längst ner.
public abstract class Character {
    // private = bara klassen själv kommer åt fälten (inkapsling). Andra går via metoderna.
    private final String name;          // final = sätts en gång i konstruktorn, ändras aldrig
    private final int maxHp;            // taket för hp
    private int hp;                     // ändras i striden – därför inte final
    private AttackStrategy attack;      // karaktären HAR en strategi (komposition, inte arv)

    // protected = bara subklasserna anropar den, via super(name, maxHp, attack).
    protected Character(String name, int maxHp, AttackStrategy attack) {
        // maxHp måste vara större än 0 – annars IllegalArgumentException
        if (maxHp <= 0)
            throw new IllegalArgumentException("maxHp must be greater than 0");

        this.name = name;
        this.maxHp = maxHp;
        this.hp = maxHp;                                   // ny karaktär = fullt liv
        this.attack = attack;
    }

    // Getters: läsa fälten utifrån. Inga setters för name/maxHp – de ska inte ändras.
    public String getName() {
        return name;
    }

    public int getHp() {
        return hp;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public boolean isAlive() {
        // Lever så länge hp är över 0
        return hp > 0;
    }

    public void takeDamage(int amount) {
        // Dra av – hp aldrig under 0, negativ skada räknas som 0
        amount = Math.max(0, amount); //Begränsa skada till 0
        hp = Math.max(0, hp - amount);
    }

    public void heal(int amount) {
        // (övning 1.3): lägg till – hp aldrig över maxHp
        amount = Math.max(0, amount);
        hp = Math.min(maxHp, hp + amount);
    }

    public void setAttack(AttackStrategy attack) {
        // byt strategi mitt i striden
        this.attack = attack;
    }

    // Karaktären frågar sin strategi om skadan och returnerar den – den drar inte av hp själv.
    public int attack(Character target) {
        // låt strategin räkna ut skadan – vem är anfallaren?
        return attack.calculateDamage(this, target);
    }

    // Abstrakta metoder: ingen kropp, varje subklass MÅSTE skriva dem – annars kompilerar det inte.
    // Här kopplas B:s föremål in i A:s strid.
    public abstract int getAttackBonus();   // Hero: vapnet. Enemy: nivån.

    public abstract int getDefense();       // Hero: rustningen. Enemy: nivån.
}
