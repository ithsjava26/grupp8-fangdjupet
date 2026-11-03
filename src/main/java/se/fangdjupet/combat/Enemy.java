package se.fangdjupet.combat;

import java.util.List;
import java.util.Optional;

// Enemy är en fiende. Ärver hp, attack, heal m.m. från Character.
public class Enemy extends Character {
    private final int level;   // fiendens nivå, ändras aldrig
    private String line;       // det fienden säger när den dyker upp, null = ingenting
    private List<String> taunts = List.of();   // det fienden säger i striden, en per runda
    private int nextTaunt;

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

    // Repliker i striden, en per runda innan fienden slår, i tur och ordning (sedan från början).
    // T.ex. Martins Java-frågor. Ingen lista = fienden säger inget i striden.
    public Enemy withTaunts(List<String> taunts) {
        if (taunts == null) throw new IllegalArgumentException("taunts must not be null");
        this.taunts = List.copyOf(taunts);
        this.nextTaunt = 0;
        return this;
    }

    // Nästa replik i striden, tom om fienden inte har några
    public Optional<String> nextTaunt() {
        if (taunts.isEmpty()) return Optional.empty();
        String taunt = taunts.get(nextTaunt);
        nextTaunt = (nextTaunt + 1) % taunts.size();
        return Optional.of(taunt);
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
