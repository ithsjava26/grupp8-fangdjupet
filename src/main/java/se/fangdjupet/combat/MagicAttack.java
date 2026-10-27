package se.fangdjupet.combat;

// Magi: en besvärjelse med fast kraft.
// Skada = kraften. Rustningen hjälper inte alls.
public class MagicAttack implements AttackStrategy {
    private final int power;   // Hur mycket skada magin gör, ändras aldrig

    // Kraften väljs när strategin skapas, t.ex. new MagicAttack(6) för en häxa
    public MagicAttack(int power) {
        this.power = power;
    }

    // Krävs av AttackStrategy. Magi ignorerar rustning och bonus – bara kraften räknas.
    @Override
    public int calculateDamage(Character attacker, Character target) {
        return power;   // skadan ÄR kraften – inget läggs till eller dras av
    }
}
