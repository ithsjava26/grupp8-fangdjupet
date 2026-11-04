package se.fangdjupet.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.Test;

import se.fangdjupet.combat.Enemy;
import se.fangdjupet.combat.Hero;
import se.fangdjupet.combat.Martin;
import se.fangdjupet.combat.MeleeAttack;
import se.fangdjupet.items.Armor;
import se.fangdjupet.items.ItemType;

// Ullvantarna på Entrédörren: +5 i försvar, +10 hp, syns i ryggsäcken och gör Martin lite lättare
class VanteTest {

    private static final Armor VANTE = new Armor(ItemType.GLOVE, WorldBuilder.VANTE, 5, 1);
    private static final Armor STADRUSTNING = new Armor(ItemType.SHIELD, WorldBuilder.STADRUSTNING, 20, 2);

    private final Hero hero = GameEngine.newHero("Robin", 40);

    @Test
    void vantenLiggerVidEntredorrenInnanStriden() {
        Room door = GameEngine.newWorld(new Random(1)).getCurrentRoom();

        assertEquals("Skoldörren", door.getName());
        assertEquals(List.of(VANTE), door.getItems());
        assertEquals("Här ligger: Ullvantar (+5 i försvar)", GameEngine.itemHere(VANTE));
        assertEquals("Plocka upp Ullvantar? (j/n): ", GameEngine.takeQuestion(VANTE));
        assertTrue(AsciiArt.forItem(VANTE).isPresent(), "vanten har en egen bild");
        assertTrue(GameEngine.leftBehind(VANTE).contains("slår igen"), "säger man nej går den inte att hämta senare");
    }

    @Test
    void vantenSkyddarMotEntredorrenOchSynsIRyggsacken() {
        Enemy door = new Enemy("Entrédörren", 20, new MeleeAttack(6), 1);
        int bare = door.attack(hero);

        hero.equip(VANTE);

        assertEquals(bare - 5, door.attack(hero));
        assertTrue(hero.getInventory().getAll().contains(VANTE));
    }

    @Test
    void utanVantenGarMartinRaktIgenomRustningen() {
        hero.equip(STADRUSTNING);

        assertEquals(7, Martin.create().attack(hero));
    }

    @Test
    void vantenDamparMartinsForklaringar() {
        hero.equip(VANTE);

        assertEquals(5, Martin.create().attack(hero));
    }

    @Test
    void vantenHjalperMotMartinAvenUnderStadrustningen() {
        hero.equip(VANTE);
        hero.equip(STADRUSTNING);

        assertEquals(20, hero.getDefense(), "Städrustningen sitter på");
        assertEquals(5, Martin.create().attack(hero), "vanten i ryggsäcken hjälper ändå");
    }

    @Test
    void ullvantarnaGer50Hp() {
        hero.warmUp();

        assertEquals(50, hero.getMaxHp());
        assertEquals(50, hero.getHp());
    }

    @Test
    void hogreTaketGarAttHelaUppTill() {
        hero.warmUp();
        hero.takeDamage(30);

        hero.heal(100);

        assertEquals(50, hero.getHp());
    }

    @Test
    void sparfilenGerInte10HpTill() {
        hero.equip(VANTE);
        hero.warmUp();
        hero.takeDamage(8);

        Hero loaded = GameEngine.heroFrom(GameEngine.stateOf(hero, GameEngine.newWorld(new Random(1))));

        assertEquals(50, loaded.getMaxHp());
        assertEquals(42, loaded.getHp());
    }

    @Test
    void stadrustningenSitterPaNarBadaKommerTillbaka() {
        Hero loaded = GameEngine.newHero("Robin", 40);

        List.of(VANTE, STADRUSTNING).forEach(loaded::equip);

        assertEquals(20, loaded.getDefense());
        assertEquals(2, loaded.getKnowledgeDefense());
    }
}
