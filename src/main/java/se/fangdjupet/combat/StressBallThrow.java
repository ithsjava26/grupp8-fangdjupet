package se.fangdjupet.combat;

// Kasta stressbollen på Martin: 10 i skada om den träffar, rakt igenom försvaret. Men sedan är bollen borta –
// hjälten klämmer den inte längre och får inte +6 hp varje runda. Missar kastet studsar bollen tillbaka.
// Finns i attackmenyn bara när hjälten har bollen (AttackMenu.options).
public final class StressBallThrow implements AttackStrategy {
    public static final int POWER = 10;
    public static final StressBallThrow THROW = new StressBallThrow();

    private StressBallThrow() {}

    // Träffar: bollen är borta efter kastet (Hero.throwAwayStressBall)
    @Override
    public int calculateDamage(Character attacker, Character target) {
        if (attacker instanceof Hero hero) hero.throwAwayStressBall();
        return POWER;
    }

    @Override
    public String name() {
        return "Kasta stressbollen";
    }

    // "Robert kastar stressbollen på Lektor Martin skada: 10"
    @Override
    public String verb() {
        return "kastar stressbollen på";
    }

    // "Robert kastar stressbollen förbi Lektor Martin!" – den studsar tillbaka, hjälten har den kvar
    @Override
    public String missVerb() {
        return "kastar stressbollen förbi";
    }
}
