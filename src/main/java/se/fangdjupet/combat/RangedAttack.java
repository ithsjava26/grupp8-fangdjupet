package se.fangdjupet.combat;

import java.util.Random;

// Avståndsattack: pil och båge.
// 20 % miss = 0. Annars skada = styrka + halva anfallarens bonus − målets försvar, aldrig under 0.
public class RangedAttack implements AttackStrategy {
    private final int strength;   // hur hårt skottet är, ändras aldrig
    private final Random random;  // slumpen skickas in – testet kan då styra den

    // Styrkan och slumpen väljs när strategin skapas, t.ex. new RangedAttack(5, new Random())
    public RangedAttack(int strength, Random random) {
        this.strength = strength;
        this.random = random;
    }

    // Krävs av AttackStrategy
    @Override
    public int calculateDamage(Character attacker, Character target) {
        // Slå ett tal 0–99. Under 20 = miss → 0 direkt. Annars räknas träffskadan ut nedanför.
        if (random.nextInt(100) < 20) {
            return 0;
        }
        return Math.max(0, strength + attacker.getAttackBonus() / 2 - target.getDefense());
    }
}
