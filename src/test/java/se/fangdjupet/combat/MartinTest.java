package se.fangdjupet.combat;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MartinTest {

    @Test
    void martinIsABoss() {
        Boss martin = Martin.create();
        assertEquals("Lektor Martin", martin.getName());
        assertEquals(100, martin.getMaxHp());
    }

    @Test
    void martinExplainsWithKnowledgeAttacks() {
        Boss martin = Martin.create();
        assertInstanceOf(KnowledgeAttack.class, martin.getAttack());
        assertEquals("Thoroughly explained the Hello World Java to you", martin.getAttack().name());
    }

    @Test
    void martinExplainsHarderAtHalfHp() {
        Boss martin = Martin.create();
        martin.takeDamage(50);                                   // 50 av 100 kvar: halva livet
        assertInstanceOf(KnowledgeAttack.class, martin.getAttack());
        assertEquals("Utterly explained", martin.getAttack().name());
    }

    @Test
    void phaseTwoHitsHarderThanPhaseOne() {
        Boss martin = Martin.create();
        Hero h = new Hero("Robert", 40, new MeleeAttack(10));
        int before = martin.attack(h);
        martin.takeDamage(50);
        assertTrue(martin.attack(h) > before);
    }

    @Test
    void martinGreetsTheHero() {
        assertTrue(Martin.create().getLine().orElseThrow().contains("Hello World"));
    }

    @Test
    void heroChoicesAreTheFourKnowledgeAttacks() {
        List<KnowledgeAttack> choices = Martin.heroChoices();
        assertEquals(List.of(KnowledgeAttack.DOOMSCROLL, KnowledgeAttack.QUIZ,
                KnowledgeAttack.STACKOVERFLOW, KnowledgeAttack.SCREENSHARE), choices);
    }

    @Test
    void eachCreateGivesANewMartin() {
        Boss first = Martin.create();
        first.takeDamage(Martin.HP);
        assertTrue(Martin.create().isAlive());                   // en ny Martin har full hp
    }

    @Test
    void theJavaBookAloneIsNotEnoughAgainst100Hp() {
        // Utan slump: boken (14) behöver 8 citat mot 100 hp, och Martin hinner förklara 7 + 7 + 7 + 15 + 15 = 51 > 40
        Hero h = new Hero("Robert", 40, new MeleeAttack(10));
        h.pickUpJavaBook();
        assertNotSame(h, new Battle(message -> { }, Hero::getJavaBookAttack).fight(h, Martin.create()));
    }

    @Test
    void theBookTheStressBallAndScreenSharingBeatMartin() {
        // Utan slump: boken, stressbollen (+6 varje runda) och Dela skärm (+12) när hp är 15 eller lägre.
        // Bollen och skärmen läker 18 – lite mer än Martins 15 i fas 2 – så hjälten hinner citera klart.
        Hero h = new Hero("Robert", 40, new MeleeAttack(10));
        h.pickUpJavaBook();
        h.pickUp(new se.fangdjupet.items.StressBall(se.fangdjupet.items.ItemType.STRESSBALL, "Stressboll", 6, 3));
        AttackChooser tactic = hero -> hero.getHp() <= 15 ? KnowledgeAttack.SCREENSHARE : hero.getJavaBookAttack();
        assertSame(h, new Battle(message -> { }, tactic).fight(h, Martin.create()));
    }

    @Test
    void stackOverflowAloneIsTooWeakAgainstMartin() {
        // 11 i skada men kostar 3 hp: Martin hinner förklara hjälten i botten
        Hero h = new Hero("Robert", 40, new MeleeAttack(10));
        Boss martin = Martin.create();
        Battle b = new Battle(message -> { }, hero -> KnowledgeAttack.STACKOVERFLOW);
        assertSame(martin, b.fight(h, martin));
    }

    @Test
    void screenSharingGivesTimeButNeverWins() {
        // Dela skärm ger +10 hp men gör ingen skada: vid rundtaket har Martin fortfarande full hp
        Hero h = new Hero("Robert", 40, new MeleeAttack(10));
        Boss martin = Martin.create();
        Battle b = new Battle(message -> { }, hero -> KnowledgeAttack.SCREENSHARE);
        assertSame(martin, b.fight(h, martin));
        assertEquals(Martin.HP, martin.getHp());
    }

    // ── Argare Martin när hjälten bär Städrustningen ──

    private final Hero dummy = new Hero("Robert", 40, new MeleeAttack(1));

    @Test
    void enragedMartinExplainsHarder() {
        Boss martin = Martin.create();
        Martin.enrage(martin);
        assertTrue(martin.isEnraged());
        assertEquals(8, martin.getAttack().calculateDamage(martin, dummy));        // fas 1: 8 i stället för 7
        martin.takeDamage(50);                                                      // halva livet: fas 2
        assertEquals(15, martin.getAttack().calculateDamage(martin, dummy));       // fas 2: kvar på 15
    }

    @Test
    void enragedMartinKeepsTheNamesSoTheArtStays() {
        Boss martin = Martin.create();
        String before = martin.getAttack().name();
        Martin.enrage(martin);
        assertEquals(before, martin.getAttack().name());
        assertTrue(AttackArt.forAttack(martin.getAttack()).isPresent());
    }

    @Test
    void enragedMartinHasANewLine() {
        Boss martin = Martin.create();
        Martin.enrage(martin);
        assertEquals(Martin.ENRAGED_LINE, martin.getLine().orElseThrow());
    }

    @Test
    void enrageInPhaseTwoUsesTheNewPhaseTwoAttack() {
        Boss martin = Martin.create();
        martin.takeDamage(60);
        Martin.enrage(martin);
        assertEquals(15, martin.getAttack().calculateDamage(martin, dummy));
    }

    @Test
    void enrageHappensOnlyOnce() {
        Boss martin = Martin.create();
        Martin.enrage(martin);
        martin.enrage(new KnowledgeAttack("x", 99), new KnowledgeAttack("y", 99));   // ignoreras
        assertEquals(8, martin.getAttack().calculateDamage(martin, dummy));
    }

    @Test
    void enrageRejectsNullAttacks() {
        assertThrows(IllegalArgumentException.class, () -> Martin.create().enrage(null, KnowledgeAttack.QUIZ));
    }

    @Test
    void angryMartinSeesSomethingInJavaYouCannotDo() {
        String line = Martin.angryLine(new java.util.Random(1));
        assertTrue(line.startsWith("Lektor Martin ser att du ") && line.endsWith(" och blir arg! Han förklarar hårdare."), line);
        assertFalse(line.contains("Städrustning"), "det handlar om Java, inte rustningen");
        assertTrue(Martin.CANT.stream().anyMatch(line::contains));
    }

    @Test
    void theAngryLineVaries() {
        java.util.Random random = new java.util.Random(3);
        java.util.Set<String> seen = new java.util.HashSet<>();
        for (int i = 0; i < 200; i++) seen.add(Martin.angryLine(random));
        assertEquals(Martin.CANT.size(), seen.size(), "alla varianter kommer någon gång");
        assertTrue(Martin.CANT.contains("inte kan skriva en konstruktor"));
        assertThrows(IllegalArgumentException.class, () -> Martin.angryLine(null));
    }

    @Test
    void theStressBallMakesHimAngryWithItsOwnLine() {
        Boss martin = Martin.create();
        Martin.enrage(martin, Martin.STRESS_BALL_LINE);
        assertTrue(martin.isEnraged());
        assertEquals(Martin.STRESS_BALL_LINE, martin.getLine().orElseThrow());
        assertTrue(Martin.STRESS_BALL_ANGRY.contains("ser stressbollen och blir arg!"));
        assertEquals(8, martin.getAttack().calculateDamage(martin, dummy), "lika arg som i rustningen");
    }

    @Test
    void normalMartinIsNotEnraged() {
        assertFalse(Martin.create().isEnraged());
    }

    @Test
    void martinAsksAJavaQuestionEveryRoundInTurn() {
        Boss martin = Martin.create();
        assertEquals(Martin.QUESTIONS.get(0), martin.nextTaunt().orElseThrow());
        assertEquals(Martin.QUESTIONS.get(1), martin.nextTaunt().orElseThrow());
        assertTrue(Martin.QUESTIONS.get(0).contains("första normalformen"));
        for (int i = 2; i < Martin.QUESTIONS.size(); i++) martin.nextTaunt();
        assertEquals(Martin.QUESTIONS.get(0), martin.nextTaunt().orElseThrow(), "sedan från början");
    }

    @Test
    void theQuestionsComeBeforeHisExplanationInTheFight() {
        java.util.List<String> log = new java.util.ArrayList<>();
        Hero hero = new Hero("Robert", 40, new MeleeAttack(1));
        hero.pickUpJavaBook();
        new Battle(log::add, Hero::getJavaBookAttack).fight(hero, Martin.create());
        int question = log.indexOf("Lektor Martin: \"" + Martin.QUESTIONS.get(0) + "\"");
        assertTrue(question >= 0, log.toString());
        assertTrue(log.get(question + 1).startsWith("Enemy: Lektor Martin"), log.toString());
    }

    @Test
    void ordinaryEnemiesSayNothingInTheFight() {
        assertTrue(new Enemy("Troll", 10, new MeleeAttack(1), 1).nextTaunt().isEmpty());
    }
}
