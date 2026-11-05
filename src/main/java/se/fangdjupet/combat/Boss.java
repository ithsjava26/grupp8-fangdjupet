package se.fangdjupet.combat;

import java.util.ArrayDeque;
import java.util.Deque;

// Boss är en fiende som byter till en tyngre attack vid halva livet (bara en gång).
// Den ärver allt från Enemy (hp, nivå, attack, repliken) och lägger bara till fasbytet
// och de hårdare steg som kan komma efter det (withRage).
public class Boss extends Enemy {
    private AttackStrategy phaseTwoAttack;         // den tyngre attacken, sparas tills bossen har halva livet kvar
    private boolean phaseTwo;                      // false tills bytet skett – då blir den true och stannar så
    private boolean enraged;                       // true när bossen har blivit argare (se enrage)
    private int lavaLampHeal;                      // 0 = ingen lavalampa; annars hp varje runda i fas 2 (Martin)
    private boolean lavaLampBroken;                // true när hjälten har krossat lampan
    private final Deque<Rage> rages = new ArrayDeque<>();   // stegen som är kvar, närmast först

    // Vid så här lite hp byter bossen till attacken
    private record Rage(int hp, AttackStrategy attack) { }

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

    // Gör bossen argare: tyngre attacker i båda faserna, bara en gång.
    // T.ex. Martin när hjälten kommer i Städrustningen (se Martin.enrage).
    public void enrage(AttackStrategy phaseOne, AttackStrategy phaseTwo) {
        if (phaseOne == null || phaseTwo == null)
            throw new IllegalArgumentException("attacks must not be null");
        if (enraged) return;
        enraged = true;
        phaseTwoAttack = phaseTwo;
        setAttack(this.phaseTwo ? phaseTwo : phaseOne);   // står bossen redan i fas 2 gäller den nya fas 2-attacken
    }

    public boolean isEnraged() {
        return enraged;
    }

    // Ett hårdare steg efter fas 2: vid hp eller mindre byter bossen till attacken. Lägg till högsta hp först.
    public Boss withRage(int hp, AttackStrategy attack) {
        if (attack == null) throw new IllegalArgumentException("attack must not be null");
        if (hp <= 0 || hp >= getMaxHp() / 2) throw new IllegalArgumentException("hp must be between 0 and half the life");
        if (!rages.isEmpty() && hp >= rages.getLast().hp()) throw new IllegalArgumentException("add the highest hp first");
        rages.addLast(new Rage(hp, attack));
        return this;
    }

    // I fas 2 ställer bossen fram en lavalampa som ger den så här mycket hp varje runda – tills hjälten krossar den
    public Boss withLavaLamp(int healPerRound) {
        if (healPerRound <= 0) throw new IllegalArgumentException("healPerRound must be positive");
        this.lavaLampHeal = healPerRound;
        return this;
    }

    // Står lampan på katedern? Bara i fas 2, bara om bossen har en, och bara tills den är krossad
    public boolean lavaLampStanding() {
        return phaseTwo && lavaLampHeal > 0 && !lavaLampBroken && isAlive();
    }

    public int lavaLampHeal() {
        return lavaLampHeal;
    }

    // Hjälten krossar lampan. true om den stod där, false om det inte fanns någon att krossa
    public boolean smashLavaLamp() {
        if (!lavaLampStanding()) return false;
        lavaLampBroken = true;
        return true;
    }

    // Skadan dras av som vanligt. Vid halva livet byter bossen till fas 2-attacken – bara en gång.
    @Override
    public void takeDamage(int amount) {
        super.takeDamage(amount);                          // Character drar av skadan (aldrig under 0)
        if (!phaseTwo && getHp() <= getMaxHp() / 2) {      // halva livet kvar, och inte redan bytt
            setAttack(phaseTwoAttack);                     // Strategy-mönstret: byt attack mitt i striden
            phaseTwo = true;                               // så att bytet aldrig görs igen (inte ens efter heal)
        }
        while (!rages.isEmpty() && getHp() <= rages.getFirst().hp()) {   // ett hårt slag kan hoppa över ett steg
            setAttack(rages.removeFirst().attack());       // stegen försvinner: en heal gör inte bossen lugnare
        }
    }
}
