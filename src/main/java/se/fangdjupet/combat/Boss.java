package se.fangdjupet.combat;

// Boss är en fiende som byter till en tyngre attack vid halva livet (bara en gång).
// Den ärver allt från Enemy (hp, nivå, attack, repliken) och lägger bara till fasbytet.
public class Boss extends Enemy {
    private final AttackStrategy phaseTwoAttack;   // den tyngre attacken, sparas tills bossen har halva livet kvar
    private boolean phaseTwo;                      // false tills bytet skett – då blir den true och stannar så

    // Bossen börjar med fas 1-attacken – den skickas vidare till Enemy som vanlig attack
    public Boss(String name, int maxHp, AttackStrategy phaseOneAttack, AttackStrategy phaseTwoAttack, int level) {
        super(name, maxHp, phaseOneAttack, level);   // super(...) måste stå först i en konstruktor
        if (phaseTwoAttack == null)
            throw new IllegalArgumentException("phaseTwoAttack must not be null");   // samma princip som Character
        this.phaseTwoAttack = phaseTwoAttack;        // används först i takeDamage nedan
    }

    // Har bossen bytt till fas 2? (t.ex. för att visa en replik när det händer)
    public boolean isInPhaseTwo() {
        return phaseTwo;
    }

    // Skadan dras av som vanligt. Vid halva livet byter bossen till fas 2-attacken – bara en gång.
    @Override
    public void takeDamage(int amount) {
        super.takeDamage(amount);                          // Character drar av skadan (aldrig under 0)
        if (!phaseTwo && getHp() <= getMaxHp() / 2) {      // halva livet kvar, och inte redan bytt
            setAttack(phaseTwoAttack);                     // Strategy-mönstret: byt attack mitt i striden
            phaseTwo = true;                               // så att bytet aldrig görs igen (inte ens efter heal)
        }
    }
}
