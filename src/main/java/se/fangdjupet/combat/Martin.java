package se.fangdjupet.combat;

import java.util.List;

// Fabrik för den sista bossen, Kapten Martin, och hjältens attacker mot honom.
// Bara statiska metoder – som ItemFactory och Frida skapar man inga Martin-objekt.
public final class Martin {
    private Martin() {}                 // ingen ska skriva new Martin()

    // En ny Martin varje gång: 40 hp, nivå 3. Han förklarar med KnowledgeAttack och
    // byter till den tyngre förklaringen vid halva livet (Boss sköter bytet).
    public static Boss create() {
        Boss martin = new Boss("Kapten Martin", 40,
                new KnowledgeAttack("Thoroughly explained the Hello World Java to you", 3),   // fas 1
                new KnowledgeAttack("Utterly explained", 5),                                  // fas 2
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
