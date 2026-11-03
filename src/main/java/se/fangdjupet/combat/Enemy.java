package se.fangdjupet.combat;

import java.util.Optional;

// Enemy är en fiende. Ärver hp, attack, heal m.m. från Character.
public class Enemy extends Character {
    private final int level;   // fiendens nivå, ändras aldrig
    private String line;       // det fienden säger när den dyker upp, null = ingenting

    // Skickar name, maxHp och attack till Character. Nivån sparas här.
    public Enemy(String name, int maxHp, AttackStrategy attack, int level) {
        super(name, maxHp, attack);
        this.level = level;
    }

    // Ger fienden en replik, t.ex. new Enemy(...).withLine("Thou shall not pass!"). Tom text = ingen replik.
    public Enemy withLine(String line) {
        this.line = (line == null || line.isBlank()) ? null : line;
        return this;
    }

    // Fiendens replik, tom om den inte säger något
    public Optional<String> getLine() {
        return Optional.ofNullable(line);
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
