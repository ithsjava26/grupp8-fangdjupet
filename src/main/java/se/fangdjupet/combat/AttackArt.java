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
              o====|=>================-      NARSTRID
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
              >>|--)------------>    AVSTANDSATTACK
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
               |  [A]   [B]   [C]   |    CITERA FRAN JAVAQUIZET
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
                   _|_               _|_      DELA SKARM
            """;

    // ── Kapten Martins förklaringar (fas 1 och fas 2) ──

    public static final String MARTIN_EXPLAINS = """
               .--------------------------------.
               | System.out.println("Hello");   |
               |  // ...och sa en gang till...  |
               '--------------------------------'
                   blah  blah  blah  blah          MARTIN FORKLARAR
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
               ( o  o  o  )  ~ ~ >     TVATTSVAMP
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
            Map.entry(CleaningAttack.CHEMICALS.name(), CHEMICALS));

    // Bilden för en attack, eller tomt om den saknar bild. Först på namnet (kunskap, Martin, StädHäxan),
    // sedan på typen (närstrid, magi, avstånd). CleaningAttack ärver RangedAttack men hittas på namnet först.
    public static Optional<String> forAttack(AttackStrategy attack) {
        if (attack == null) return Optional.empty();
        String art = BY_NAME.get(attack.name());
        if (art != null) return Optional.of(art);
        return switch (attack) {
            case MeleeAttack m -> Optional.of(MELEE);
            case MagicAttack m -> Optional.of(MAGIC);
            case RangedAttack r -> Optional.of(RANGED);
            default -> Optional.empty();
        };
    }
}
