package se.fangdjupet.game;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import se.fangdjupet.combat.Enemy;
import se.fangdjupet.combat.Hero;
import se.fangdjupet.combat.MeleeAttack;
import se.fangdjupet.items.Armor;
import se.fangdjupet.items.ItemType;
import se.fangdjupet.items.Potion;
import se.fangdjupet.items.Weapon;
import se.fangdjupet.items.WeaponRange;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class GameEngineTest {

    private Enemy enemy(String name) {
        return new Enemy(name, 10, new MeleeAttack(3), 1);
    }

    // Kafferummet i liten skala: JavaFläcken först, sedan trollet och trollkarlen tillsammans
    private Room coffeeRoom(Enemy fleck, Enemy troll, Enemy wizard) {
        return new Room("Kafferummet", "Det luktar kaffe.", fleck, List.of()).thenTogether(troll, wizard);
    }

    @Test
    void nextWaveComesWhenTheWaveIsDefeated() {
        Enemy fleck = enemy("JavaFläcken");
        Room room = coffeeRoom(fleck, enemy("Troll"), enemy("Trollkarl"));
        fleck.takeDamage(100);
        assertTrue(GameEngine.nextWaveIsReady(List.of(fleck), room));
    }

    @Test
    void noNextWaveWhileTheFoughtWaveLives() {
        // Rundtaket: hjälten kan vinna på mest hp fast JavaFläcken lever – då ska inget kaffe delas ut
        Enemy fleck = enemy("JavaFläcken");
        Room room = coffeeRoom(fleck, enemy("Troll"), enemy("Trollkarl"));
        fleck.takeDamage(5);
        assertFalse(GameEngine.nextWaveIsReady(List.of(fleck), room));
    }

    @Test
    void noNextWaveWhenOneOfAGroupLives() {
        Enemy fleck = enemy("JavaFläcken");
        Enemy troll = enemy("Troll");
        Enemy wizard = enemy("Trollkarl");
        Room room = coffeeRoom(fleck, troll, wizard).thenTogether(enemy("Extra"));
        fleck.takeDamage(100);
        troll.takeDamage(100);                                // trollkarlen lever kvar
        assertFalse(GameEngine.nextWaveIsReady(List.of(troll, wizard), room));
    }

    @Test
    void noNextWaveWhenTheRoomIsCleared() {
        Enemy fleck = enemy("JavaFläcken");
        Enemy troll = enemy("Troll");
        Enemy wizard = enemy("Trollkarl");
        Room room = coffeeRoom(fleck, troll, wizard);
        fleck.takeDamage(100);
        troll.takeDamage(100);
        wizard.takeDamage(100);
        assertFalse(GameEngine.nextWaveIsReady(List.of(troll, wizard), room));
    }

    // ── Game over när hjälten dör ──

    private Hero robert() {
        return new Hero("Robert", 40, new MeleeAttack(10));
    }

    @Test
    void gameOverIsBigRedLetters() {
        String art = GameEngine.gameOver(robert(), enemy("StädHäxan")).get(0);
        assertTrue(art.startsWith("\u001B[31m"), "börjar rött");
        assertTrue(art.endsWith("\u001B[0m"), "slutar med vanlig färg");
        assertTrue(art.contains(AsciiArt.GAME_OVER.lines().findFirst().orElseThrow()));
    }

    @Test
    void gameOverSaysWhoWon() {
        assertTrue(GameEngine.gameOver(robert(), enemy("StädHäxan")).contains("Robert besegrades av StädHäxan."));
    }

    @Test
    void gameOverWhenTheHeroDiesOfTheirOwnAttack() {
        // Dör båda i samma slag vinner den som slog – hjälten "vinner" men är död
        Hero robert = robert();
        assertTrue(GameEngine.gameOver(robert, robert).contains("Robert vann striden men dog av sin egen attack."));
    }

    @Test
    void gameOverTellsHowToPlayAgain() {
        assertTrue(GameEngine.gameOver(robert(), enemy("Troll")).stream().anyMatch(line -> line.contains("1")));
    }


    // ── Föremål plockas upp automatiskt när hjälten kommer in i rummet ──

    private final Weapon bow = new Weapon(ItemType.BOW, WeaponRange.RANGED, "Pilbåge", 3, 1);

    @Test
    void everythingInTheRoomIsPickedUp() {
        Room room = new Room("Kafferummet", "Det luktar kaffe.", enemy("JavaFläcken"), List.of(bow));
        assertEquals(List.of(bow), GameEngine.pickUpAll(robert(), room));
        assertTrue(room.getItems().isEmpty());
    }

    @Test
    void anEmptyRoomGivesNothing() {
        Room room = new Room("Skoldörren", "Du står utanför skolan.", enemy("Entrédörren"), List.of());
        assertTrue(GameEngine.pickUpAll(robert(), room).isEmpty());
    }

    @Test
    void aWeaponEndsUpInTheHeroesHand() {
        Hero robert = robert();
        GameEngine.pickUpAll(robert, new Room("Kafferummet", "Det luktar kaffe.", enemy("JavaFläcken"), List.of(bow)));
        assertEquals(3, robert.getAttackBonus());
    }

    @Test
    void aWeaponSwitchesToItsAttack() {
        // Pilbågen är mycket bättre än knytnävarna – den som tar den börjar skjuta (kan bytas i attackmenyn)
        Hero robert = robert();
        var melee = robert.getAttack();
        GameEngine.pickUpAll(robert, new Room("Kafferummet", "Det luktar kaffe.", enemy("JavaFläcken"), List.of(bow)));
        assertInstanceOf(se.fangdjupet.combat.RangedAttack.class, robert.getAttack());
        assertNotSame(melee, robert.getAttack());
    }

    @Test
    void aPotionIsPickedUpButNotEquipped() {
        Hero robert = robert();
        Potion potion = new Potion(ItemType.POTION, "Hälsodryck", 10, 1);
        assertEquals(List.of(potion), GameEngine.pickUpAll(robert, new Room("Korridoren", "Lång.", enemy("StädHäxan"), List.of(potion))));
        assertEquals(0, robert.getAttackBonus());
    }

    // ── Två menyer: huvudmenyn utan äventyr, spelmenyn i rummet ──

    @Test
    void mainMenuStartsOrContinuesAnAdventure() {
        String menu = GameEngine.menu(null);
        assertTrue(menu.contains("1. Starta nytt äventyr"));
        assertTrue(menu.contains("2. Fortsätt sparat äventyr"));
        assertTrue(menu.contains("e. Avsluta"));
        assertFalse(menu.contains("Strid"), "ingen strid utan äventyr");
    }

    @Test
    void gameMenuDoesNotOfferANewAdventure() {
        String menu = GameEngine.menu(new Room("Kafferummet", "Det luktar kaffe.", enemy("JavaFläcken"), List.of()));
        assertFalse(menu.contains("Starta nytt äventyr"));
        assertTrue(menu.contains("1. Strid mot rummets fiende"));
        assertTrue(menu.contains("2. Gå till ett annat rum"));
        assertTrue(menu.contains("3. Visa hjältens utrustning"));
        assertTrue(menu.contains("e. Avsluta"));
    }

    @Test
    void gameMenuShowsWhereTheHeroIs() {
        String menu = GameEngine.menu(new Room("Kafferummet", "Det luktar kaffe.", enemy("JavaFläcken"), List.of()));
        assertTrue(menu.startsWith("Kafferummet\n==========="), menu);
    }

    @Test
    void gameMenuCanSave() {
        String menu = GameEngine.menu(new Room("Kafferummet", "Det luktar kaffe.", enemy("JavaFläcken"), List.of()));
        assertTrue(menu.contains("4. Spara äventyret"));
    }

    // ── Gå till nästa rum: inte förbi en levande fiende ──

    @Test
    void aLivingEnemyBlocksTheWay() {
        Room room = new Room("Skoldörren", "Du står utanför skolan.", enemy("Entrédörren"), List.of());
        assertEquals("Entrédörren står i vägen! Besegra rummets fiender innan du går vidare.",
                GameEngine.blockedBy(room).orElseThrow());
    }

    @Test
    void theWayIsFreeWhenTheRoomIsCleared() {
        Enemy door = enemy("Entrédörren");
        Room room = new Room("Skoldörren", "Du står utanför skolan.", door, List.of());
        door.takeDamage(100);
        assertTrue(GameEngine.blockedBy(room).isEmpty());
    }

    @Test
    void theNextWaveBlocksTooAndAllItsEnemiesAreNamed() {
        Enemy fleck = enemy("JavaFläcken");
        Room room = coffeeRoom(fleck, enemy("Troll"), enemy("Trollkarl"));
        fleck.takeDamage(100);
        assertTrue(GameEngine.blockedBy(room).orElseThrow().startsWith("Troll och Trollkarl står i vägen!"));
    }

    @Test
    void aRoomWithoutEnemiesIsFree() {
        assertTrue(GameEngine.blockedBy(new Room("Hallen", "Tom.", (Enemy) null, List.of())).isEmpty());
    }

    // ── Visa hjältens utrustning ──

    @Test
    void equipmentWithoutAnything() {
        assertEquals(List.of("Robert  [##########] 40/40", "Vapen: inget", "Rustning: ingen", "Attack: Närstrid"),
                GameEngine.equipment(robert()));
    }

    @Test
    void equipmentShowsWeaponArmorAndHp() {
        Hero robert = robert();
        GameEngine.pickUpAll(robert, new Room("Kafferummet", "Det luktar kaffe.", enemy("JavaFläcken"), List.of(bow)));
        robert.equip(new Armor(ItemType.SHIELD, "Läderrustning", 2, 1));
        robert.takeDamage(10);
        List<String> lines = GameEngine.equipment(robert);
        assertEquals("Robert  [#######---] 30/40", lines.get(0));   // 7,5 avrundas nedåt
        assertTrue(lines.contains("Vapen: Pilbåge (+3 i skada)"), lines.toString());
        assertTrue(lines.contains("Rustning: Läderrustning (försvar 2)"), lines.toString());
    }

    @Test
    void equipmentShowsTheJavaBook() {
        Hero robert = robert();
        robert.pickUpJavaBook();
        List<String> lines = GameEngine.equipment(robert);
        assertTrue(lines.contains("Attack: Citera StackOverflow. Ställ jobbig fråga."), lines.toString());
        assertTrue(lines.stream().anyMatch(line -> line.startsWith("Java-boken: ja")), lines.toString());
    }

    // ── Spara och fortsätta ──

    @Test
    void theSaveHoldsNameHpAndRoom() {
        Hero robert = robert();
        robert.takeDamage(15);
        World world = GameEngine.newWorld(new Random(1));
        world.moveForward();                                   // Kafferummet
        assertEquals(new GameState("Robert", 40, 25, 1), GameEngine.stateOf(robert, world));
    }

    @Test
    void aLoadedHeroHasTheSavedHp() {
        Hero hero = GameEngine.heroFrom(new GameState("Robert", 40, 25, 1));
        assertEquals("Robert", hero.getName());
        assertEquals(25, hero.getHp());
        assertEquals(40, hero.getMaxHp());
        assertTrue(hero.getAttack() instanceof MeleeAttack);
    }

    @Test
    void fastForwardGoesToTheSavedRoomAndKeepsTheWeapons() {
        Hero hero = robert();
        World world = GameEngine.newWorld(new Random(1));
        assertTrue(GameEngine.fastForward(hero, world, 2));
        assertEquals("Korridoren och städskrubben", world.getCurrentRoom().getName());
        assertEquals("Pilbåge", hero.getWeapon().orElseThrow().description());   // från Kafferummet
        assertTrue(world.getCurrentRoom().hasLivingEnemy(), "StädHäxan väntar – rummet börjar om");
    }

    @Test
    void fastForwardToTheFirstRoomStaysThere() {
        World world = GameEngine.newWorld(new Random(1));
        assertTrue(GameEngine.fastForward(robert(), world, 0));
        assertEquals("Skoldörren", world.getCurrentRoom().getName());
    }

    @Test
    void fastForwardToARoomThatDoesNotExistFails() {
        assertFalse(GameEngine.fastForward(robert(), GameEngine.newWorld(new Random(1)), 99));
    }

    @Test
    void saveAndLoadThroughTheFileGivesTheSameAdventure(@TempDir Path dir) throws IOException {
        Hero robert = robert();
        robert.takeDamage(7);
        World world = GameEngine.newWorld(new Random(1));
        world.moveForward();
        world.moveForward();
        GameSaveFileHandler saves = new GameSaveFileHandler(dir);
        saves.save(GameEngine.stateOf(robert, world), GameEngine.SAVE_SLOT);

        GameState loaded = saves.load(GameEngine.SAVE_SLOT);
        Hero hero = GameEngine.heroFrom(loaded);
        World loadedWorld = GameEngine.newWorld(new Random(1));
        assertTrue(GameEngine.fastForward(hero, loadedWorld, loaded.nextRoom()));
        assertEquals(33, hero.getHp());
        assertEquals("Korridoren och städskrubben", loadedWorld.getCurrentRoom().getName());
    }

    // ── Leta i rummet (spelmenyn 5) och Städrustningen ──

    @Test
    void gameMenuCanSearchTheRoom() {
        String menu = GameEngine.menu(new Room("Kafferummet", "Det luktar kaffe.", enemy("JavaFläcken"), List.of()));
        assertTrue(menu.contains("5. Leta i rummet"));
    }

    @Test
    void cleaningArmorIsHiddenInTheCleaningCupboard() {
        World world = GameEngine.newWorld(new Random(1));
        world.moveForward();                                   // Kafferummet
        world.moveForward();                                   // Korridoren och städskrubben
        List<se.fangdjupet.items.Item> found = world.getCurrentRoom().search();
        Armor armor = (Armor) found.getFirst();
        assertEquals(WorldBuilder.STADRUSTNING, armor.description());
        assertEquals(20, armor.defense());
    }

    @Test
    void otherRoomsHaveNothingHidden() {
        World world = GameEngine.newWorld(new Random(1));
        assertTrue(world.getCurrentRoom().search().isEmpty());   // Skoldörren
        world.moveForward();
        assertTrue(world.getCurrentRoom().search().isEmpty());   // Kafferummet
    }

    @Test
    void cleaningArmorGivesTwentyDefense() {
        Hero hero = robert();
        hero.equip(new Armor(ItemType.SHIELD, WorldBuilder.STADRUSTNING, 20, 2));
        assertEquals(20, hero.getDefense());
    }

    @Test
    void equipKeepingAttackKeepsTheChosenAttack() {
        Hero hero = robert();
        var attack = hero.getAttack();
        GameEngine.equipKeepingAttack(hero, new Weapon(ItemType.BOW, WeaponRange.RANGED, "Pilbåge", 3, 1));
        assertSame(attack, hero.getAttack());
        assertEquals(3, hero.getAttackBonus());
    }

    @Test
    void cleaningArmorIsRecognised() {
        Hero hero = robert();
        assertFalse(GameEngine.wearsCleaningArmor(hero));
        hero.equip(new Armor(ItemType.HELMET, "Cykelhjälm", 2, 1));
        assertFalse(GameEngine.wearsCleaningArmor(hero));
        hero.equip(new Armor(ItemType.SHIELD, WorldBuilder.STADRUSTNING, 20, 2));
        assertTrue(GameEngine.wearsCleaningArmor(hero));
    }
}
