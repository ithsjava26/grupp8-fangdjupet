package se.fangdjupet.combat;

// Ett interface = ett löfte om metoder, utan kod. Varje sorts attack (melee, magi …)
// blir en egen klass som implementerar det. Strategy-mönstret: attacken går att byta ut.
public interface AttackStrategy {
    // Räknar ut hur mycket skada anfallaren gör på målet. Ingen kropp här –
    // varje strategi skriver sin egen. Den enda metoden ⇒ en lambda räcker som strategi.
    int calculateDamage(Character attacker, Character target);

    // default = metoden HAR en kropp, så strategierna behöver inte skriva den.
    // Ger klassens namn, t.ex. "MeleeAttack" – bra i stridsloggen.
    default String name() { return getClass().getSimpleName(); }
}
