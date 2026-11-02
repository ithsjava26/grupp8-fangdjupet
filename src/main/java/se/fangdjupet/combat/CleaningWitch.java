package se.fangdjupet.combat;

import java.util.Random;

// StädHäxan i Korridoren och städskrubben. En Boss med två faser, som Martin:
// fas 1 kastar hon tvättsvamp eller Svinto (slumpas varje slag),
// vid halva livet tar hon fram kemikalierna och sprayar dem. Alla tre är avståndsattacker.
public final class CleaningWitch {

    // Ingen ska skapa en CleaningWitch – bara anropa create()
    private CleaningWitch() {
    }

    // Random styr vilket kast hon väljer i fas 1 – testerna kan skicka in sin egen
    public static Boss create(Random random) {
        if (random == null)
            throw new IllegalArgumentException("random must not be null");
        Boss witch = new Boss("StädHäxan", 30,
                new RandomAttack(random, CleaningAttack.SPONGE, CleaningAttack.SVINTO),
                CleaningAttack.CHEMICALS, 2);
        witch.withLine("Ett steg till mot lektionssalen och jag tar fram kemikalierna!");
        return witch;
    }
}
