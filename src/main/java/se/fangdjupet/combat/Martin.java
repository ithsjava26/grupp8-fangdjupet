package se.fangdjupet.combat;

import java.util.List;

// Fabrik för den sista bossen, Lektor Martin, och hjältens attacker mot honom.
// Bara statiska metoder – som ItemFactory och Frida skapar man inga Martin-objekt.
public final class Martin {
    private Martin() {}                 // ingen ska skriva new Martin()

    public static final String NAME = "Lektor Martin";
    public static final String ANGRY = NAME + " ser Städrustningen och blir arg! Han förklarar hårdare.";

    // Martins styrka: hp och förklaringarna (fas 1, och fas 2 från halva livet). Arg = i Städrustningen.
    public static final int HP = 50;
    static final int EXPLAIN = 7, EXPLAIN_HARDER = 10;
    static final int ANGRY_EXPLAIN = 8, ANGRY_EXPLAIN_HARDER = 10;

    // En ny Martin varje gång: 50 hp, nivå 3. Han förklarar med KnowledgeAttack och
    // byter till den tyngre förklaringen vid halva livet (Boss sköter bytet).
    public static Boss create() {
        Boss martin = new Boss(NAME, HP,
                new KnowledgeAttack("Thoroughly explained the Hello World Java to you", EXPLAIN),   // fas 1
                new KnowledgeAttack("Utterly explained", EXPLAIN_HARDER),                           // fas 2
                3);
        // withLine returnerar en Enemy, inte en Boss – därför sparas bossen i en variabel först
        martin.withLine("Let me explain Hello World to you...");
        return martin;
    }

    // Kommer hjälten i Städrustningen blir Martin lite svårare: fas 1 gör 4 i skada i stället för 3 (fas 2 är kvar på 5).
    // Vid fullt hp vinner hjälten då ungefär 78 % i stället för 89 % (simulerat med StackOverflow).
    // Kunskap går ändå rakt igenom rustningen, så försvaret hjälper inte mot honom.
    // Förklaringarna heter likadant, så AttackArt visar samma bilder.
    public static final String ENRAGED_LINE = "Jaså, städrustning? Kunskap går rakt igenom gummi!";

    public static void enrage(Boss martin) {
        if (martin == null || martin.isEnraged()) return;
        martin.enrage(
                new KnowledgeAttack("Thoroughly explained the Hello World Java to you", 4),
                new KnowledgeAttack("Utterly explained", 5));
        martin.withLine(ENRAGED_LINE);
    }

    // Det hjälten kan välja mellan mot Martin (efter att ha tagit upp Java-boken)
    public static List<KnowledgeAttack> heroChoices() {
        return List.of(KnowledgeAttack.DOOMSCROLL, KnowledgeAttack.QUIZ,
                KnowledgeAttack.STACKOVERFLOW, KnowledgeAttack.SCREENSHARE);
    }
}
