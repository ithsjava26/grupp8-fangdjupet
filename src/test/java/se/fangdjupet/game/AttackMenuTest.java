package se.fangdjupet.game;

import org.junit.jupiter.api.Test;
import se.fangdjupet.combat.*;

import se.fangdjupet.items.ItemType;
import se.fangdjupet.items.Weapon;
import se.fangdjupet.items.WeaponRange;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AttackMenuTest {

    private Hero hero() {
        return new Hero("Robert", 40, new MeleeAttack(10));
    }

    private static final Weapon BOW = new Weapon(ItemType.BOW, WeaponRange.RANGED, "Pilbåge", 4, 1);
    private static final Weapon STAFF = new Weapon(ItemType.STAFF, WeaponRange.MAGIC, "Trollstav", 4, 1);

    private Hero heroWithBook() {
        Hero h = hero();
        h.pickUpJavaBook();
        return h;
    }

    // ── Vilka attacker man kan välja ──

    @Test
    void atTheStartOnlyMeleeCanBeChosen() {
        List<AttackStrategy> options = AttackMenu.options(GameEngine.newHero("Robert", 40));
        assertEquals(1, options.size());
        assertInstanceOf(MeleeAttack.class, options.get(0));
    }

    @Test
    void theBowUnlocksRangedAndTheStaffUnlocksMagic() {
        Hero hero = GameEngine.newHero("Robert", 40);
        hero.equip(BOW);
        List<AttackStrategy> withBow = AttackMenu.options(hero);
        assertEquals(2, withBow.size());
        assertInstanceOf(RangedAttack.class, withBow.get(1));

        hero.equip(STAFF);                                // staven tar pilbågens plats i handen ...
        List<AttackStrategy> options = AttackMenu.options(hero);
        assertEquals(3, options.size());                  // ... men avståndsattacken finns kvar
        assertInstanceOf(MeleeAttack.class, options.get(0));
        assertInstanceOf(MagicAttack.class, options.get(1));
        assertInstanceOf(RangedAttack.class, options.get(2));
    }

    @Test
    void theHeroStartsWithAWeakSword() {
        Hero hero = GameEngine.newHero("Robert", 40);
        assertEquals(GameEngine.SWORD, hero.getWeapon().orElseThrow().description());
        assertInstanceOf(MeleeAttack.class, hero.getAttack());
        se.fangdjupet.combat.Character dummy = new Hero("Docka", 99, new MeleeAttack(1));
        assertEquals(AttackMenu.MELEE + AttackMenu.SWORD_BONUS, hero.getAttack().calculateDamage(hero, dummy));
    }

    @Test
    void theWeaponInHandsAttackIsHighlightedInYellow() {
        Hero hero = GameEngine.newHero("Robert", 40);
        assertEquals(List.of("1. Närstrid"), AttackMenu.lines(hero, AttackMenu.options(hero)));   // svärdet: inget gult
        hero.equip(BOW);
        hero.equip(STAFF);
        List<String> lines = AttackMenu.lines(hero, AttackMenu.options(hero));
        assertEquals("1. Närstrid", lines.get(0));
        assertEquals(AsciiArt.yellow("2. Magi"), lines.get(1));    // staven i handen
        assertEquals("3. Avståndsattack", lines.get(2));           // pilbågen är inte i handen längre
    }

    @Test
    void withTheBookMartinsFourAndTheJavaBookAreOffered() {
        List<AttackStrategy> options = AttackMenu.options(heroWithBook());
        assertEquals(Martin.heroChoices(), options.subList(0, 4));
        assertInstanceOf(JavaBookAttack.class, options.get(4));
    }

    // ── Hur menyn ser ut ──

    @Test
    void menuLinesWithoutTheBook() {
        Hero hero = hero();
        hero.equip(STAFF);
        hero.equip(BOW);
        assertEquals(List.of("1. Närstrid", "2. Magi", "3. Avståndsattack"),
                AttackMenu.lines(AttackMenu.options(hero)));
    }

    @Test
    void menuLinesWithTheBookShowTheKnowledgeAttackNames() {
        List<String> lines = AttackMenu.lines(AttackMenu.options(heroWithBook()));
        assertEquals(6, lines.size());
        assertEquals("1. " + KnowledgeAttack.DOOMSCROLL.name() + "  (9 i skada, kostar dig 2 hp)", lines.get(0));
        assertEquals("2. " + KnowledgeAttack.QUIZ.name() + "  (7 i skada)", lines.get(1));
        assertEquals("4. " + KnowledgeAttack.SCREENSHARE.name() + "  (+10 hp till dig, ingen skada, missar aldrig)", lines.get(3));
        assertEquals("5. Citera från Java-boken, kapitel 18: Polymorphism  (14 i skada, 30 % risk att du slinter på sidan)", lines.get(4));
        assertEquals("6. Smyg in USB-minnet  (20 % chans: viruset tar över datorn och du vinner direkt – annars tar Martin USB-minnet)", lines.get(5));
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
        assertNull(AttackMenu.choose(options, "2"));             // i början finns bara 1. Närstrid
    }

    @Test
    void textKeepsTheCurrentAttack() {
        assertNull(AttackMenu.choose(AttackMenu.options(hero()), "hej"));
        assertNull(AttackMenu.choose(AttackMenu.options(hero()), null));
    }

    @Test
    void theBowAndTheStaffMakeTheirAttacksMuchStronger() {
        Hero hero = GameEngine.newHero("Robert", 40);
        se.fangdjupet.combat.Character dummy = new Hero("Docka", 99, new MeleeAttack(1));
        hero.equip(STAFF);
        assertEquals(AttackMenu.STAFF_MAGIC + 4, AttackMenu.options(hero).get(1).calculateDamage(hero, dummy));
        hero.equip(BOW);   // staven ur handen: magin finns kvar men blir svag
        assertEquals(AttackMenu.MAGIC, AttackMenu.options(hero).get(1).calculateDamage(hero, dummy));
        int sword = AttackMenu.MELEE + AttackMenu.SWORD_BONUS;
        assertTrue(AttackMenu.BOW_RANGED > sword + 4, "pilbågen ska vara mycket bättre än svärdet");
        assertTrue(AttackMenu.STAFF_MAGIC > sword, "staven ska vara bättre än svärdet");
        assertEquals(6, sword);
    }
}
