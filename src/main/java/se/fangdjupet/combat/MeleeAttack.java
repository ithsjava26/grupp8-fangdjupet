package se.fangdjupet.combat;

// Närstrid: ett slag med svärd eller klubba.
// Skada = styrka + anfallarens bonus − målets försvar, aldrig under 0.
public class MeleeAttack implements AttackStrategy {
    private final int strength;   // hur hårt slaget är, ändras aldrig

    // Styrkan väljs när strategin skapas, t.ex. new MeleeAttack(10) för ett troll
    public MeleeAttack(int strength) {
        this.strength = strength;
    }

    // Krävs av AttackStrategy. Bonus: vapnet (Hero) eller nivån (Enemy).
    // Försvar: rustningen (Hero) eller halva nivån (Enemy). Math.max gör att skadan aldrig blir negativ.
    @Override
    public int calculateDamage(Character attacker, Character target) {
        return Math.max(0, strength + attacker.getAttackBonus() - target.getDefense());
    }
}
