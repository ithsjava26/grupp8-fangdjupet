package se.fangdjupet.combat;

import java.util.List;
import java.util.Random;

// Fabrik för den sista bossen, Lektor Martin, och hjältens attacker mot honom.
// Bara statiska metoder – som ItemFactory och Frida skapar man inga Martin-objekt.
public final class Martin {
    private Martin() {}                 // ingen ska skriva new Martin()

    public static final String NAME = "Lektor Martin";
    // När han blir arg (hjälten kommer i Städrustningen) ser han att hjälten inte kan något i Java – en slumpas fram
    public static final List<String> CANT = List.of(
            "inte kan skriva en konstruktor",
            "inte vet vad static betyder",
            "jämför strängar med ==",
            "inte kan skilja på ett interface och en abstrakt klass",
            "glömmer break i switchen",
            "har gjort alla fält public",
            "fångar Exception och gör ingenting",
            "inte vet vad en NullPointerException är",
            "inte kan överskugga equals() utan hashCode()",
            "skriver main-metoden utan static");

    // "Lektor Martin ser att du inte kan skriva en konstruktor och blir arg! Han förklarar hårdare."
    public static String angryLine(Random random) {
        if (random == null)
            throw new IllegalArgumentException("random must not be null");
        return NAME + " ser att du " + CANT.get(random.nextInt(CANT.size())) + " och blir arg! Han förklarar hårdare.";
    }

    // Martins styrka: hp och förklaringarna (fas 1, och fas 2 från halva livet). Arg = i Städrustningen.
    public static final int HP = 100;
    static final int EXPLAIN = 7, EXPLAIN_HARDER = 14;        // fas 2: dubbelt så hårt som fas 1
    static final int ANGRY_EXPLAIN = 8, ANGRY_EXPLAIN_HARDER = 14;

    // En ny Martin varje gång: 100 hp, nivå 3. Han förklarar med KnowledgeAttack och
    // byter till den tyngre förklaringen vid halva livet (Boss sköter bytet).
    public static Boss create() {
        Boss martin = new Boss(NAME, HP,
                new KnowledgeAttack("Thoroughly explained the Hello World Java to you", EXPLAIN),   // fas 1
                new KnowledgeAttack("Utterly explained", EXPLAIN_HARDER),                           // fas 2
                3);
        // withLine returnerar en Enemy, inte en Boss – därför sparas bossen i en variabel först
        martin.withLine("Let me explain Hello World to you...");
        martin.withTaunts(QUESTIONS);
        return martin;
    }

    // Martin förhör hjälten: en fråga per runda innan han förklarar (Battle skriver ut den), sedan från början
    public static final List<String> QUESTIONS = List.of(
            "Kan du första normalformen? Inga upprepade grupper i tabellen!",
            "Vad är skillnaden mellan == och equals()?",
            "Varför är String immutable?",
            "Vad händer om du glömmer break i en switch?",
            "Vad betyder static – egentligen?",
            "Interface eller abstrakt klass? Motivera!",
            "Andra normalformen då? Inga partiella beroenden!",
            "Vad är en NullPointerException – och varför är det ditt fel?",
            "Checked eller unchecked exception?",
            "ArrayList eller LinkedList? Vad blir tidskomplexiteten?",
            "Varför ska fälten vara private?",
            "Tredje normalformen? Inga transitiva beroenden!");

    // Kommer hjälten i Städrustningen blir Martin lite svårare: fas 1 gör 8 i skada i stället för 7 (fas 2 är kvar på 14).
    // Kunskap går ändå rakt igenom rustningen, så försvaret hjälper inte mot honom.
    // Förklaringarna heter likadant, så AttackArt visar samma bilder.
    public static final String ENRAGED_LINE = "Städrustning? Den implementerar inte Shield-interfacet – kunskap kompilerar rakt igenom!";

    // Har hjälten stressbollen i fickan blir han också arg – en gång, och med en egen replik
    public static final String STRESS_BALL_ANGRY = NAME + " ser stressbollen och blir arg! Han förklarar hårdare.";
    public static final String STRESS_BALL_LINE = "En stressboll? Stress är en checked exception – den får du hantera själv!";

    public static void enrage(Boss martin) {
        enrage(martin, ENRAGED_LINE);
    }

    // Samma ilska, men med en annan replik (stressbollen i stället för rustningen)
    public static void enrage(Boss martin, String line) {
        if (martin == null || martin.isEnraged()) return;
        martin.enrage(
                new KnowledgeAttack("Thoroughly explained the Hello World Java to you", ANGRY_EXPLAIN),
                new KnowledgeAttack("Utterly explained", ANGRY_EXPLAIN_HARDER));
        martin.withLine(line);
    }

    // Det hjälten kan välja mellan mot Martin (efter att ha tagit upp Java-boken)
    public static List<KnowledgeAttack> heroChoices() {
        return List.of(KnowledgeAttack.DOOMSCROLL, KnowledgeAttack.QUIZ,
                KnowledgeAttack.STACKOVERFLOW, KnowledgeAttack.SCREENSHARE);
    }
}
