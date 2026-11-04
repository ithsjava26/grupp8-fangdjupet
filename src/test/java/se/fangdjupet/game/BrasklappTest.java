package se.fangdjupet.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import se.fangdjupet.combat.Battle;
import se.fangdjupet.combat.BrasklappAttack;
import se.fangdjupet.combat.Frida;
import se.fangdjupet.combat.Hero;
import se.fangdjupet.combat.Martin;
import se.fangdjupet.items.Armor;
import se.fangdjupet.items.Brasklapp;
import se.fangdjupet.items.Item;
import se.fangdjupet.items.ItemType;

// Fridas brasklapp: DROP TABLE mot Martin, exakt 25 i skada, en enda gång
class BrasklappTest {

    @TempDir
    Path dir;

    private final Brasklapp note = Frida.create().brasklapp();

    private Hero heroWithBookAndNote() {
        Hero hero = GameEngine.newHero("Robin", 40);
        hero.pickUpJavaBook();
        hero.takeBrasklapp(note);
        return hero;
    }

    @Test
    void attackenFinnsBaraMedBrasklappenOchJavaBoken() {
        Hero hero = GameEngine.newHero("Robin", 40);
        hero.takeBrasklapp(note);
        assertFalse(AttackMenu.options(hero).contains(BrasklappAttack.DROP_TABLE), "ingen boken: inte Martin än");

        hero.pickUpJavaBook();
        assertTrue(AttackMenu.options(hero).contains(BrasklappAttack.DROP_TABLE));

        Hero without = GameEngine.newHero("Robin", 40);
        without.pickUpJavaBook();
        assertFalse(AttackMenu.options(without).contains(BrasklappAttack.DROP_TABLE));
    }

    @Test
    void dropTableTarExakt25OchMissarAldrig() {
        for (int seed = 0; seed < 50; seed++) {
            Hero hero = heroWithBookAndNote();
            List<String> log = new ArrayList<>();
            int[] round = {0};

            new Battle(log::add, h -> round[0]++ == 0 ? BrasklappAttack.DROP_TABLE : null, new Random(seed))
                    .fight(hero, List.of(Martin.create()));

            assertTrue(log.contains("Hero: Robin skriker boolean isAlive och läser upp DROP TABLE! "
                    + "Golvet skakar under Lektor Martin skada: 25"), "seed " + seed);
        }
    }

    @Test
    void brasklappenGarBaraAttAnvandaEnGang() {
        Hero hero = heroWithBookAndNote();
        int[] round = {0};

        new Battle(line -> { }, h -> round[0]++ == 0 ? BrasklappAttack.DROP_TABLE : null, new Random(1))
                .fight(hero, List.of(Martin.create()));

        assertTrue(hero.getBrasklapp().isEmpty());
        assertFalse(hero.getInventory().getAll().contains(note));
        assertFalse(AttackMenu.options(hero).contains(BrasklappAttack.DROP_TABLE));
        assertFalse(hero.getAttack() instanceof BrasklappAttack, "Enter = behåll får inte peka på en använd brasklapp");
    }

    @Test
    void sparfilenBeharVantenOchBrasklappen() throws IOException {
        GameSaveFileHandler handler = new GameSaveFileHandler(dir);
        List<Item> items = List.of(new Armor(ItemType.GLOVE, WorldBuilder.VANTE, 3, 1), note);
        GameState state = new GameState("Robin", 40, 12, 3, items);

        handler.save(state, 1);

        assertEquals(state, handler.load(1));
    }

    @Test
    void brasklappenHarEnBildOchSynsIMenyn() {
        assertTrue(AsciiArt.forItem(note).isPresent());
        assertEquals("  (25 i skada, missar aldrig – men bara en gång)", AttackMenu.effect(BrasklappAttack.DROP_TABLE));
    }
}
