package se.fangdjupet.combat;

// Brasklappen från Frida: hjälten skriker ut boolean isAlive och läser upp DROP TABLE, och golvet under
// Martin ger vika. Exakt 25 i skada, missar aldrig och går bara att använda en gång.
// Finns i attackmenyn bara när hjälten har brasklappen (AttackMenu.options).
public final class BrasklappAttack implements AttackStrategy {
    public static final int POWER = 25;
    public static final BrasklappAttack DROP_TABLE = new BrasklappAttack();

    private BrasklappAttack() {}

    @Override
    public int calculateDamage(Character attacker, Character target) {
        return POWER;
    }

    @Override
    public String name() {
        return "Läs upp brasklappen: DROP TABLE";
    }

    // "Robert skriker boolean isAlive och läser upp DROP TABLE! Golvet skakar under Lektor Martin skada: 25"
    @Override
    public String verb() {
        return "skriker boolean isAlive och läser upp DROP TABLE! Golvet skakar under";
    }

    @Override
    public int missPercent() {
        return 0;
    }

    @Override
    public boolean exactDamage() {
        return true;
    }

    @Override
    public void used(Hero hero) {
        hero.useBrasklapp();
    }
}
