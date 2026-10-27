package se.fangdjupet.combat;

import java.util.Random;

public class RangedAttack implements AttackStrategy {
    private final int strength;
    private final Random random;

    public RangedAttack(int strength, Random random) {
        this.strength = strength;
        this.random = random;
    }

    @Override
    public int calculateDamage(Character attacker, Character target) {
        if (random.nextInt(100) < 20) { //slå ett tal. Är det under 20 blir det miss och metoden avslutas direkt med 0. Annars fortsätter koden till nästa rad och räknar ut träffskadan.
            return 0;
        }
        return Math.max(0, strength + attacker.getAttackBonus() / 2 - target.getDefense());
    }
}
