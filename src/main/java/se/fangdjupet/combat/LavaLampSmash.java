package se.fangdjupet.combat;

// Krossa Lektor Martins lavalampa. I fas 2 ställer Martin fram en lavalampa som ger honom hp varje runda
// (Boss.withLavaLamp). Den här attacken gör ingen skada på Martin – den kostar hjältens tur, men sedan
// bubblar lampan inte mer. Finns i attackmenyn bara när lampan står (AttackMenu), gulmarkerad.
public final class LavaLampSmash implements AttackStrategy {
    public static final LavaLampSmash SMASH = new LavaLampSmash();

    private LavaLampSmash() {}

    // Ingen skada på Martin: det är lampan som går sönder (Battle sköter det)
    @Override
    public int calculateDamage(Character attacker, Character target) {
        return 0;
    }

    // Lampan står still på katedern: man träffar alltid
    @Override
    public int missPercent() {
        return 0;
    }

    // Lampan är krossad: nästa runda (Enter = behåll) citerar hjälten ur boken igen
    @Override
    public void used(Hero hero) {
        hero.stopUsing(this);
    }

    @Override
    public String name() {
        return "Krossa lavalampan";
    }

    @Override
    public String verb() {
        return "krossar lavalampan hos";
    }
}
