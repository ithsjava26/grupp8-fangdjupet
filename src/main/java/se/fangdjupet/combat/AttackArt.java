package se.fangdjupet.combat;

import java.util.Map;
import java.util.Optional;

// ASCII-bilder för attackerna: DoomScrolla, StackOverflow, Svinto, Martins förklaringar m.fl.
// Battle visar bilden när spelaren väljer en attack, och första gången en boss använder en attack
// (se Battle.withAttackArt()). Bara ren ASCII och högst 60 tecken breda – å, ä och ö blir ofta ?
// i Windows-konsolen, därför står namnen i bilderna utan prickar.
public final class AttackArt {

    private AttackArt() {}    // bara konstanter och statiska metoder

    // ── Hjältens vanliga attacker ──

    public static final String MELEE = """
                   |\\
              o====|=>================-      NÄRSTRID
                   |/
            """;

    public static final String MAGIC = """
                 .   *   .
               *   \\ | /   *
              - - -( * )- - -        MAGI
               *   / | \\   *
                 '   *   '
            """;

    public static final String RANGED = """
                |\\
                | \\
              >>|--)------------>    AVSTÅNDSATTACK
                | /
                |/
            """;

    // ── Kunskapsattackerna mot Martin ──

    public static final String DOOMSCROLL = """
               .---------.
               | ~~~~~~~ |      DOOMSCROLLA
               | [##] == |      scroll... scroll...
               | ~~~~~~~ |      ...scroll...
               | [##] == |      (kostar 1 hp)
               |    o    |
               '---------'
            """;

    public static final String QUIZ = """
               .--------------------.
               |  JAVAQUIZ      ?   |
               |  [A]   [B]   [C]   |    CITERA FRÅN JAVAQUIZET
               |  "Vad gor static?" |
               '--------------------'
            """;

    public static final String STACKOVERFLOW = """
                       __
                     _/_/
                   _/_/          CITERA STACKOVERFLOW
                  /_/            "Closed as duplicate."
               | ======= |
               |_________|
            """;

    public static final String SCREENSHARE = """
               .---------.       .---------.
               | > java  |  ==>  | > java  |
               | Hello!_ |       | Hello!_ |
               '----+----'       '----+----'
                   _|_               _|_      DELA SKÄRM
            """;

    // Java-boken uppslagen på kapitel 18: en Animal som kan vara både Dog och Cat
    public static final String POLYMORPHISM = """
                ______________ ______________
               | KAP. 18      |              |
               | Polymorphism | Animal a =   |
               |              |  new Dog();  |
               | Animal       | a.speak();   |
               |  /    \\      |  -> "Voff!"  |
               | Dog   Cat    |              |
               |______________|______________|    POLYMORPHISM
            """;

    // Nästa citat: kapitel 20, ett interface som flera klasser implementerar (som AttackStrategy i spelet)
    public static final String INTERFACE = """
                ______________ ________________
               | KAP. 20      | interface      |
               | Interface    |  Attack {      |
               |              |  int hit();    |
               |  <<Attack>>  | }              |
               |   /     \\    | class Bow      |
               | Bow    Staff |  implements .. |
               |______________|________________|    INTERFACE
            """;

    // Java-bokens bild för kapitlet som står på tur (JavaBookAttack bläddrar vidare efter varje citat)
    static String forChapter(String chapter) {
        return chapter.contains("Interface") ? INTERFACE : POLYMORPHISM;
    }

    // ── Lektor Martins förklaringar (fas 1 och fas 2) ──

    public static final String MARTIN_EXPLAINS = """
               .--------------------------------.
               | System.out.println("Hello");   |
               |  // ...och sa en gang till...  |
               '--------------------------------'
                   blah  blah  blah  blah          MARTIN FÖRKLARAR
            """;

    public static final String MARTIN_UTTERLY = """
               .--------------------------------.
               | public static void main(...) { |
               |   // rad... for... rad...      |
               | }                              |
               '--------------------------------'
                 zzz      zzz      zzz             UTTERLY EXPLAINED
            """;

    // ── StädHäxans kast ──

    public static final String SPONGE = """
                .--------.
               ( o  o  o  )  ~ ~ >     TVÄTTSVAMP
                '--------'
            """;

    public static final String SVINTO = """
                .--------.
                | SVINTO |  * * * >    SVINTO
                '--------'
            """;

    public static final String CHEMICALS = """
                 _
                |=|____
                |  ____]>  ~ ~ ~ ~ ~   KEMIKALIER
                |  |
                |__|
            """;

    // Vanish i ansiktet – när StädHäxan ser sin Städrustning på hjälten (CleaningWitch.enrage)
    public static final String VANISH = """
                 ____
                |=|_____
                |VANISH_]>  ~*~*~*~*~   VANISH
                |    |                  (rakt i ansiktet!)
                |____|
            """;

    // Attackens namn (name()) -> bilden. Namnen måste vara exakt som i KnowledgeAttack, Martin och CleaningAttack.
    private static final Map<String, String> BY_NAME = Map.ofEntries(
            Map.entry(KnowledgeAttack.DOOMSCROLL.name(), DOOMSCROLL),
            Map.entry(KnowledgeAttack.QUIZ.name(), QUIZ),
            Map.entry(KnowledgeAttack.STACKOVERFLOW.name(), STACKOVERFLOW),
            Map.entry(KnowledgeAttack.SCREENSHARE.name(), SCREENSHARE),
            Map.entry("Thoroughly explained the Hello World Java to you", MARTIN_EXPLAINS),
            Map.entry("Utterly explained", MARTIN_UTTERLY),
            Map.entry(CleaningAttack.SPONGE.name(), SPONGE),
            Map.entry(CleaningAttack.SVINTO.name(), SVINTO),
            Map.entry(CleaningAttack.CHEMICALS.name(), CHEMICALS),
            Map.entry(CleaningAttack.VANISH.name(), VANISH));

    // Bilden för en attack, eller tomt om den saknar bild. Först på namnet (kunskap, Martin, StädHäxan),
    // sedan på typen (närstrid, magi, avstånd). CleaningAttack ärver RangedAttack men hittas på namnet först.
    public static Optional<String> forAttack(AttackStrategy attack) {
        if (attack == null) return Optional.empty();
        String art = BY_NAME.get(attack.name());
        if (art != null) return Optional.of(art);
        return switch (attack) {
            case JavaBookAttack book -> Optional.of(forChapter(book.nextChapter()));   // bilden visas före citatet
            case MeleeAttack m -> Optional.of(MELEE);
            case MagicAttack m -> Optional.of(MAGIC);
            case RangedAttack r -> Optional.of(RANGED);
            default -> Optional.empty();
        };
    }
}
