package se.fangdjupet.combat;

// Enemy är en fiende. Ärver hp, attack, heal m.m. från Character.
public class Enemy extends Character {
    private final int level;   // fiendens nivå, ändras aldrig

    // Skickar name, maxHp och attack till Character. Nivån sparas här.
    public Enemy(String name, int maxHp, AttackStrategy attack, int level) {
        super(name, maxHp, attack);
        this.level = level;
    }

    // Attackbonus = nivån. Högre nivå slår hårdare.
    @Override
    public int getAttackBonus() {
        return level;
    }

    // Försvar = halva nivån (avrundas nedåt). Växer långsammare än attacken.
    @Override
    public int getDefense() {
        return level / 2;
    }
}
