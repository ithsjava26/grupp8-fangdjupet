package se.fangdjupet.combat;

// Ett interface = ett löfte om metoder, utan kod. Varje sorts attack (melee, magi …)
// blir en egen klass som implementerar det. Strategy-mönstret: attacken går att byta ut.
public interface AttackStrategy {
    // Så ofta ett slag missar när Battle kör med slump
    int USUAL_MISS_PERCENT = 10;

    // Räknar ut hur mycket skada anfallaren gör på målet. Ingen kropp här –
    // varje strategi skriver sin egen. Den enda metoden ⇒ en lambda räcker som strategi.
    int calculateDamage(Character attacker, Character target);

    // default = metoden HAR en kropp, så strategierna behöver inte skriva den.
    // Ger klassens namn, t.ex. "MeleeAttack" – bra i stridsloggen.
    default String name() { return getClass().getSimpleName(); }
    default int selfDamage() { return 0; } //Vad attackerna kostar en själv
    default String verb(){
        return "slår";
    }

    // Hp som attacken ger anfallaren i stället för att skada, t.ex. Dela skärm (+10). Den missar aldrig.
    default int selfHeal() { return 0; }

    // Hur ofta attacken missar (i procent) när Battle kör med slump. Java-boken missar oftare: man slinter på sidan.
    default int missPercent() { return USUAL_MISS_PERCENT; }

    // Ordet i loggen när attacken missar: "Robert missar Troll!"
    default String missVerb() { return "missar"; }
    // Vinner attacken direkt när den lyckas (USB-minnet)? Då varierar Battle inte skadan, och målet faller alltid.
    default boolean instantWin() { return false; }
    // Anropas efter hjältens försök, lyckat eller inte – t.ex. är USB-minnet borta efteråt
    default void used(Hero hero) { }
}
