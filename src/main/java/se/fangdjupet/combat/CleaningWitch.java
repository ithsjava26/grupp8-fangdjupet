package se.fangdjupet.combat;

import java.util.Random;

// StädHäxan i Korridoren och städskrubben. En Boss med två faser, som Martin:
// fas 1 kastar hon tvättsvamp eller Svinto (slumpas varje slag),
// vid halva livet tar hon fram kemikalierna och sprayar dem. Alla tre är avståndsattacker.
public final class CleaningWitch {

    public static final String NAME = "StädHäxan";
    public static final int HP = 40;

    // När hjälten kommer i hennes egen Städrustning skrattar hon elakt och hotar med kemikalieangrepp
    public static final String LAUGH = "StädHäxan ser sin Städrustning på dig och skrattar elakt: \"Mwahahaha!\"";
    public static final String ENRAGED_LINE = "Jaså, min städrustning? Akta dig så jag inte kastar Svinto på dig – och tvättsvampar! "
            + "Och sen blir det kemikalieangrepp: jag sprejar dig i ansiktet med Vanish!";

    // Ingen ska skapa en CleaningWitch – bara anropa create()
    private CleaningWitch() {
    }

    // Random styr vilket kast hon väljer i fas 1 – testerna kan skicka in sin egen
    public static Boss create(Random random) {
        if (random == null)
            throw new IllegalArgumentException("random must not be null");
        Boss witch = new Boss(NAME, HP,
                new RandomAttack(random, CleaningAttack.SPONGE, CleaningAttack.SVINTO),
                CleaningAttack.CHEMICALS, 2);
        witch.withLine("Ett steg till mot lektionssalen och jag tar fram kemikalierna!");
        return witch;
    }

    // Hjälten bär Städrustningen: hon kastar som vanligt (Svinto och tvättsvampar – de studsar mot förklädet), men vid halva
    // livet blir kemikalierna Vanish i ansiktet. Den går rakt igenom rustningen, så hon är farlig även i hennes egen rustning.
    // Bara en gång. Står hon redan i fas 2 byts attacken direkt till Vanish.
    public static void enrage(Boss witch) {
        if (witch == null || witch.isEnraged()) return;
        witch.enrage(witch.isInPhaseTwo() ? CleaningAttack.VANISH : witch.getAttack(), CleaningAttack.VANISH);
        witch.withLine(ENRAGED_LINE);
    }
}
