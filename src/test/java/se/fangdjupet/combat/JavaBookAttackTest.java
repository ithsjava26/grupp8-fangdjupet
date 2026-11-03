package se.fangdjupet.combat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JavaBookAttackTest {

    private final Hero hero = new Hero("Robert", 40, new MeleeAttack(7));
    private final Boss martin = Martin.create();

    @Test
    void firstChapter18ThenChapter20ThenFromTheStart() {
        JavaBookAttack book = new JavaBookAttack();
        assertEquals("Citera från Java-boken, kapitel 18: Polymorphism", book.name());
        assertEquals(14, book.calculateDamage(hero, martin));
        assertEquals("citerar kapitel 18: Polymorphism för", book.verb(), "loggen visar kapitlet som just lästes");
        assertEquals("Citera från Java-boken, kapitel 20: Interface", book.name(), "menyn visar nästa kapitel");
        book.calculateDamage(hero, martin);
        assertEquals("citerar kapitel 20: Interface för", book.verb());
        assertEquals("Citera från Java-boken, kapitel 18: Polymorphism", book.name());
    }

    @Test
    void itIsTheBestButYouCanSlipOnThePage() {
        JavaBookAttack book = new JavaBookAttack();
        assertTrue(JavaBookAttack.POWER > KnowledgeAttack.STACKOVERFLOW.power(), "boken ska vara bäst mot Martin");
        assertEquals(30, book.missPercent());
        assertTrue(book.missPercent() > AttackStrategy.USUAL_MISS_PERCENT);
        assertEquals("slinter på sidan och missar", book.missVerb());
    }

    @Test
    void itCostsNothingAndGoesThroughDefense() {
        JavaBookAttack book = new JavaBookAttack();
        assertEquals(0, book.selfDamage());
        hero.equip(new se.fangdjupet.items.Armor(se.fangdjupet.items.ItemType.SHIELD, "Sköld", 20, 1));
        assertEquals(JavaBookAttack.POWER, book.calculateDamage(martin, hero));
    }

    @Test
    void theArtFollowsTheChapter() {
        JavaBookAttack book = new JavaBookAttack();
        assertTrue(AttackArt.forAttack(book).orElseThrow().contains("Polymorphism"));
        book.calculateDamage(hero, martin);
        assertTrue(AttackArt.forAttack(book).orElseThrow().contains("Interface"));
    }

    @Test
    void everyHeroHasTheirOwnBookInTheMenu() {
        Hero other = new Hero("Fredrik", 40, new MeleeAttack(7));
        hero.getJavaBookAttack().calculateDamage(hero, martin);
        assertNotSame(hero.getJavaBookAttack(), other.getJavaBookAttack());
        assertEquals("Citera från Java-boken, kapitel 18: Polymorphism", other.getJavaBookAttack().name());
    }
}
