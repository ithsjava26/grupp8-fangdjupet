package se.fangdjupet.game;

import org.junit.jupiter.api.Test;
import se.fangdjupet.combat.*;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AttackMenuTest {

    private Hero hero() {
        return new Hero("Robert", 40, new MeleeAttack(10));
    }

    private Hero heroWithBook() {
        Hero h = hero();
        h.pickUpJavaBook();
        return h;
    }

    // ── Vilka attacker man kan välja ──

    @Test
    void withoutTheBookTheHeroChoosesMeleeMagicOrRanged() {
        List<AttackStrategy> options = AttackMenu.options(hero());
        assertEquals(3, options.size());
        assertInstanceOf(MeleeAttack.class, options.get(0));
        assertInstanceOf(MagicAttack.class, options.get(1));
        assertInstanceOf(RangedAttack.class, options.get(2));
    }

    @Test
    void withTheBookOnlyMartinsFourAreOffered() {
        List<AttackStrategy> options = AttackMenu.options(heroWithBook());
        assertEquals(List.copyOf(Martin.heroChoices()), options);
    }

    // ── Hur menyn ser ut ──

    @Test
    void menuLinesWithoutTheBook() {
        assertEquals(List.of("1. Närstrid", "2. Magi", "3. Avståndsattack"),
                AttackMenu.lines(AttackMenu.options(hero())));
    }

    @Test
    void menuLinesWithTheBookShowTheKnowledgeAttackNames() {
        List<String> lines = AttackMenu.lines(AttackMenu.options(heroWithBook()));
        assertEquals(4, lines.size());
        assertEquals("1. " + KnowledgeAttack.DOOMSCROLL.name(), lines.get(0));
        assertEquals("4. " + KnowledgeAttack.SCREENSHARE.name(), lines.get(3));
    }

    @Test
    void labelGivesTheSwedishName() {
        assertEquals("Närstrid", AttackMenu.label(new MeleeAttack(10)));
        assertEquals("Magi", AttackMenu.label(new MagicAttack(6)));
        assertEquals("Avståndsattack", AttackMenu.label(new RangedAttack(5, new java.util.Random(1))));
        assertEquals(KnowledgeAttack.QUIZ.name(), AttackMenu.label(KnowledgeAttack.QUIZ));
    }

    // ── Spelarens val ──

    @Test
    void numberPicksThatOption() {
        List<AttackStrategy> options = AttackMenu.options(heroWithBook());
        assertSame(KnowledgeAttack.DOOMSCROLL, AttackMenu.choose(options, "1"));
        assertSame(KnowledgeAttack.SCREENSHARE, AttackMenu.choose(options, "4"));
    }

    @Test
    void spacesAroundTheNumberAreOk() {
        List<AttackStrategy> options = AttackMenu.options(heroWithBook());
        assertSame(KnowledgeAttack.QUIZ, AttackMenu.choose(options, " 2 "));
    }

    @Test
    void enterKeepsTheCurrentAttack() {
        assertNull(AttackMenu.choose(AttackMenu.options(hero()), ""));   // null = behåll
    }

    @Test
    void numberOutsideTheMenuKeepsTheCurrentAttack() {
        List<AttackStrategy> options = AttackMenu.options(hero());
        assertNull(AttackMenu.choose(options, "0"));
        assertNull(AttackMenu.choose(options, "4"));             // utan boken finns bara 1–3
    }

    @Test
    void textKeepsTheCurrentAttack() {
        assertNull(AttackMenu.choose(AttackMenu.options(hero()), "hej"));
        assertNull(AttackMenu.choose(AttackMenu.options(hero()), null));
    }
}
