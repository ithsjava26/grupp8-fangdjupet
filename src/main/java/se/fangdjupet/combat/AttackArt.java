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
               |  "Vad gör static?" |
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

    // Stressbollen flyger i en båge mot Martin – och är sedan borta
    public static final String STRESS_BALL_THROW = """
                      .  -  ~  -  .
                  . '               ' .
               o/                       (@)  SQUISH!
              /|                         |
              / \\                         KASTA STRESSBOLLEN
            """;

    // USB-minnet med virus som smygs in i Martins dator
    public static final String USB_VIRUS = """
                 ___________
                |  _______  |      [=USB=]>
                | | ERROR | |   ~~~~~~~~~~~~~~
                | | VIRUS | |  SMYG IN USB-MINNET
                | |_______| |   01001000 01001001
                |___________|   (20 %% chans)
                   _|___|_
            """.formatted();

    // Martins lavalampa i fas 2: den bubblar och ger honom hp varje runda, tills hjälten krossar den
    public static final String LAVA_LAMP = """
                   ___
                  /   \\        LAVALAMPA
                 / ( ) \\       blubb ... blubb ...
                |  o    |      +%d hp till Martin
                | (  )  |      varje runda
                |    () |
                 \\ (_) /       krossa den!
                  |___|
                 /_____\\
            """.formatted(Martin.LAVA_LAMP_HEAL);

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
               |  // ...och så en gång till...  |
               '--------------------------------'
                   blah  blah  blah  blah          MARTIN FÖRKLARAR
            """;

    public static final String MARTIN_UTTERLY = """
               .--------------------------------.
               | public static void main(...) { |
               |   // rad... för... rad...      |
               | }                              |
               '--------------------------------'
                 zzz      zzz      zzz             UTTERLY EXPLAINED
            """;

    public static final String MARTIN_PERSISTENTLY = """
               .--------------------------------.
               | // och en gang till...         |
               | // och en gang till...         |
               | // och en gang till...         |
               '--------------------------------'
                 igen!  igen!  igen!   PERSISTENTLY EXPLAINED
            """;

    public static final String MARTIN_ANGRILY = """
               .--------------------------------.
               | System.out.println("VARFOR");  |
               | throw new DuFattarInteError(); |
               | // HAR DU INTE LAST KAPITLET?! |
               '--------------------------------'
                 !!!!   !!!!   !!!!    ANGRILY EXPLAINED
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

    // DROP TABLE: golvet under Martin ger vika
    public static final String DROP_TABLE = """
                DROP TABLE martin;
                ___________    ___________
                |_|_|_|_|_/ \\/ \\_|_|_|_|_|
                         /  /\\  \\
                        /  /  \\  \\     GOLVET SKAKAR
            """;

    // Attackens namn (name()) -> bilden. Namnen måste vara exakt som i KnowledgeAttack, Martin och CleaningAttack.
    private static final Map<String, String> BY_NAME = Map.ofEntries(
            Map.entry(KnowledgeAttack.DOOMSCROLL.name(), DOOMSCROLL),
            Map.entry(KnowledgeAttack.QUIZ.name(), QUIZ),
            Map.entry(KnowledgeAttack.STACKOVERFLOW.name(), STACKOVERFLOW),
            Map.entry(KnowledgeAttack.SCREENSHARE.name(), SCREENSHARE),
            Map.entry(StressBallThrow.THROW.name(), STRESS_BALL_THROW),
            Map.entry(BrasklappAttack.DROP_TABLE.name(), DROP_TABLE),
            Map.entry(UsbVirusAttack.PLUG_IN.name(), USB_VIRUS),
            Map.entry("Thoroughly explained the Hello World Java to you", MARTIN_EXPLAINS),
            Map.entry("Utterly explained", MARTIN_UTTERLY),
            Map.entry("Persistently explained", MARTIN_PERSISTENTLY),
            Map.entry("Angrily explained", MARTIN_ANGRILY),
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
