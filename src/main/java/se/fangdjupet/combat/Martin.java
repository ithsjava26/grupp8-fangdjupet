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

    // Det hjälten kan välja mellan mot Martin (efter att ha tagit upp Java-boken)
    public static List<KnowledgeAttack> heroChoices() {
        return List.of(KnowledgeAttack.DOOMSCROLL, KnowledgeAttack.QUIZ,
                KnowledgeAttack.STACKOVERFLOW, KnowledgeAttack.SCREENSHARE);
    }
}
