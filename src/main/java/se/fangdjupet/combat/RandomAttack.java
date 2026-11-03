package se.fangdjupet.combat;

import java.util.List;
import java.util.Random;

// En strategi som består av andra strategier: varje slag slumpar den fram en av dem.
// name(), verb() och selfDamage() hör till den attack som valdes senast – Battle räknar först
// skadan och skriver sedan verbet, så loggen stämmer med slaget.
public class RandomAttack implements AttackStrategy {

    private final Random random;
    private final List<AttackStrategy> attacks;
    private AttackStrategy last;     // senast valda attack (den första innan något slag)

    public RandomAttack(Random random, AttackStrategy... attacks) {
        if (random == null)
            throw new IllegalArgumentException("random must not be null");
        if (attacks == null || attacks.length == 0)
            throw new IllegalArgumentException("needs at least one attack");
        this.random = random;
        this.attacks = List.of(attacks);
        this.last = this.attacks.getFirst();
    }

    // Slumpar fram en av attackerna och kommer ihåg den – sedan räknas skadan som för den
    @Override
    public int calculateDamage(Character attacker, Character target) {
        last = attacks.get(random.nextInt(attacks.size()));
        return last.calculateDamage(attacker, target);
    }

    // name, verb och selfDamage hör till den senast valda attacken
    @Override
    public String name() {
        return last.name();
    }

    @Override
    public String verb() {
        return last.verb();
    }

    @Override
    public int selfDamage() {
        return last.selfDamage();
    }
}
