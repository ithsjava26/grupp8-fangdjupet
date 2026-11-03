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
    void theStressBallIsHiddenInTheClassroom() {
        World world = GameEngine.newWorld(new Random(1));
        world.moveForward();
        world.moveForward();
        world.moveForward();                                   // Lektionssalen
        se.fangdjupet.items.StressBall ball = (se.fangdjupet.items.StressBall) world.getCurrentRoom().search().getFirst();
        assertEquals(WorldBuilder.STRESSBOLL, ball.description());
        assertEquals(5, ball.hpPerTurn());
        assertTrue(AsciiArt.forItem(ball).isPresent(), "stressbollen har en bild");
    }

    @Test
    void equipmentShowsTheStressBallAndTheMenuCanThrowIt() {
        Hero robert = robert();
        robert.pickUpJavaBook();
        robert.pickUp(new se.fangdjupet.items.StressBall(ItemType.STRESSBALL, WorldBuilder.STRESSBOLL, 5, 3));
        assertTrue(GameEngine.equipment(robert).contains("Stressboll: i fickan (+5 hp per runda, eller kasta den på Martin)"));
        List<String> menu = AttackMenu.lines(AttackMenu.options(robert));
        assertEquals("6. Kasta stressbollen  (10 i skada om den träffar – sedan är bollen borta: ingen +5 hp per runda)", menu.get(5));
        assertEquals("Här ligger: Stressboll (+5 hp per runda)",
                GameEngine.itemHere(new se.fangdjupet.items.StressBall(ItemType.STRESSBALL, "Stressboll", 5, 3)));
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
    void pickingUpTheBowSwitchesToShooting() {
        Hero hero = robert();
        GameEngine.equipAndAim(hero, new Weapon(ItemType.BOW, WeaponRange.RANGED, "Pilbåge", 3, 1));
        assertInstanceOf(se.fangdjupet.combat.RangedAttack.class, hero.getAttack());
        assertEquals(3, hero.getAttackBonus(), "bågens bonus hjälper skotten");
    }

    @Test
    void theStaffSwitchesToMagicButTheJavaBookKeepsKnowledge() {
        Hero hero = robert();
        GameEngine.equipAndAim(hero, new Weapon(ItemType.STAFF, WeaponRange.MAGIC, "Trollstav", 4, 1));
        assertEquals(AttackMenu.STAFF_MAGIC + 4,
                hero.getAttack().calculateDamage(hero, new Enemy("Docka", 50, new MeleeAttack(1), 1)));
        Hero reader = robert();
        reader.pickUpJavaBook();
        var knowledge = reader.getAttack();
        GameEngine.equipAndAim(reader, new Weapon(ItemType.STAFF, WeaponRange.MAGIC, "Trollstav", 4, 1));
        assertSame(knowledge, reader.getAttack());
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

    // ── Kartan och "Gå till ett annat rum" ──

    private void clearCurrent(World world) {
        Room room = world.getCurrentRoom();
        while (room.hasLivingEnemy()) room.getEnemies().forEach(e -> e.takeDamage(999));
    }

    @Test
    void mapAtTheStart() {
        World world = GameEngine.newWorld(new Random(1));
        assertEquals("""
                KARTA  @ du är här  x rensat  ! fiender  ? nytt  # låst
                [@] Skoldörren    -- [#] Kafferummet
                                      |
                [#] Lektionssalen -- [#] Korridoren""", GameEngine.roomMap(world));
    }

    @Test
    void mapShowsClearedNewAndCurrentRooms() {
        World world = GameEngine.newWorld(new Random(1));
        clearCurrent(world);                       // Entrédörren besegrad
        assertTrue(GameEngine.roomMap(world).contains("[@] Skoldörren    -- [?] Kafferummet"));
        world.moveTo(1);                           // Kafferummet – dörren ut slår igen
        clearCurrent(world);                       // alla vågor i Kafferummet
        world.moveTo(2);                           // Korridoren, StädHäxan lever
        world.moveTo(1);                           // tillbaka till Kafferummet
        String map = GameEngine.roomMap(world);
        assertTrue(map.contains("[#] Skoldörren"), map);
        assertTrue(map.contains("[@] Kafferummet"), map);
        assertTrue(map.contains("[!] Korridoren"), map);
    }

    @Test
    void mapIsPlainAsciiAndFitsTheConsole() {
        String map = GameEngine.roomMap(GameEngine.newWorld(new Random(1)));
        assertTrue(map.chars().allMatch(c -> c < 128 || "åäöÅÄÖé".indexOf(c) >= 0));
        assertTrue(map.lines().allMatch(l -> l.length() <= 60));
    }

    @Test
    void roomChoicesShowWhereYouAreAndWhatIsLocked() {
        World world = GameEngine.newWorld(new Random(1));
        clearCurrent(world);
        assertEquals("""
                Vart vill du gå?
                1. Skoldörren  (du är här)
                2. Kafferummet  (nytt rum)
                3. Korridoren och städskrubben  (låst)
                4. Lektionssalen  (låst)""", GameEngine.roomChoices(world));
    }

    @Test
    void visitedRoomIsCleared() {
        World world = GameEngine.newWorld(new Random(1));
        clearCurrent(world);
        world.moveTo(1);
        clearCurrent(world);
        world.moveTo(2);
        String choices = GameEngine.roomChoices(world);
        assertTrue(choices.contains("2. Kafferummet  (rensat)"), choices);
        assertTrue(choices.contains("1. Skoldörren  (låst)"), choices);
    }

    @Test
    void lockedMessageNamesWhoIsInTheWay() {
        World world = GameEngine.newWorld(new Random(1));
        assertEquals("Kafferummet är låst. Entrédörren står i vägen i Skoldörren – besegra rummets fiender först.",
                GameEngine.lockedMessage(world, 1));
        assertEquals("Lektionssalen är låst. Du har inte hittat vägen dit än.", GameEngine.lockedMessage(world, 3));
    }

    @Test
    void shortNamesAreAsciiWithoutTheSecondPart() {
        assertEquals("Skoldörren", GameEngine.shortName("Skoldörren"));
        assertEquals("Korridoren", GameEngine.shortName("Korridoren och städskrubben"));
    }

    @Test
    void saveKeepsTheFurthestRoomWhenYouWentBack() {
        World world = GameEngine.newWorld(new Random(1));
        clearCurrent(world);
        world.moveTo(1);
        clearCurrent(world);
        world.moveTo(2);
        world.moveTo(1);
        assertEquals(2, GameEngine.stateOf(robert(), world).nextRoom());
    }

    // ── Inlåst: Entrédörren slår igen när hjälten går in ──

    @Test
    void theEntranceLocksWhenYouGoInside() {
        World world = GameEngine.newWorld(new Random(1));
        clearCurrent(world);
        assertFalse(world.isEntranceLocked(), "ute på skolgården är dörren inte låst än");
        world.moveTo(1);
        assertTrue(world.isEntranceLocked());
        assertFalse(world.moveTo(0));
        assertEquals("Skoldörren är låst. Du är nu inlåst i Fångdjupet och behöver besegra slutbossen innan du kan ta dig ut.",
                GameEngine.lockedMessage(world, 0));
    }

    @Test
    void theEntranceOpensWhenTheFinalBossIsDefeated() {
        World world = GameEngine.newWorld(new Random(1));
        clearCurrent(world);
        world.moveTo(1);
        world.getRooms().getLast().getEnemies().forEach(e -> e.takeDamage(999));   // Martin besegrad
        assertFalse(world.isEntranceLocked());
        assertTrue(world.isOpen(0));
    }

    @Test
    void loadingASaveAlsoLocksTheEntrance() {
        World world = GameEngine.newWorld(new Random(1));
        assertTrue(GameEngine.fastForward(robert(), world, 1));
        assertTrue(world.isEntranceLocked());
    }

    // ── Föremål i rummet: gulmarkerat, spelaren väljer ──

    @Test
    void coffeeAndBunAreOfferedWithTheirHp() {
        Potion coffee = new Potion(ItemType.POTION, "en kopp kaffe", 30, 1);
        assertEquals("Ta en kopp kaffe (+30 hp)? (j/n): ", GameEngine.takeQuestion(coffee));
        assertTrue(GameEngine.leftBehind(coffee).contains("letar i rummet"));
        assertEquals("Plocka upp Pilbåge? (j/n): ",
                GameEngine.takeQuestion(bow));
    }

    @Test
    void itemHereShowsTheBonus() {
        assertEquals("Här ligger: Pilbåge (+3 i skada)",
                GameEngine.itemHere(new Weapon(ItemType.BOW, WeaponRange.RANGED, "Pilbåge", 3, 1)));
        assertEquals("Här ligger: Städrustning (+20 i försvar)",
                GameEngine.itemHere(new Armor(ItemType.SHIELD, "Städrustning", 20, 2)));
        assertEquals("Här ligger: en kopp kaffe (+30 hp)",
                GameEngine.itemHere(new Potion(ItemType.POTION, "en kopp kaffe", 30, 1)));
    }

    @Test
    void wantsItOnlyOnYes() {
        assertTrue(GameEngine.wantsIt("j"));
        assertTrue(GameEngine.wantsIt(" Ja "));
        assertTrue(GameEngine.wantsIt("y"));
        assertFalse(GameEngine.wantsIt("n"));
        assertFalse(GameEngine.wantsIt(""));
        assertFalse(GameEngine.wantsIt(null));
    }

    // ── Gulmarkerade menyval: fiende kvar och något att hitta ──

    @Test
    void fightOptionIsYellowWithTheEnemyName() {
        Room room = new Room("Kafferummet", "Det luktar kaffe.", enemy("JavaFläcken"), List.of());
        assertEquals(AsciiArt.yellow("1. Strid mot rummets fiende  (JavaFläcken väntar!)"), GameEngine.fightOption(room));
    }

    @Test
    void fightOptionIsPlainWhenTheRoomIsCleared() {
        Room room = new Room("Kafferummet", "Det luktar kaffe.", enemy("JavaFläcken"), List.of());
        room.getEnemy().takeDamage(999);
        assertEquals("1. Strid mot rummets fiende", GameEngine.fightOption(room));
    }

    @Test
    void searchOptionIsYellowWhenSomethingIsHidden() {
        Room room = new Room("Korridoren", "Lång.", enemy("StädHäxan"), List.of())
                .withHiddenItem(new Armor(ItemType.SHIELD, "Städrustning", 20, 2));
        assertEquals(AsciiArt.yellow("5. Leta i rummet  (något finns här!)"), GameEngine.searchOption(room));
        room.search();
        assertEquals("5. Leta i rummet", GameEngine.searchOption(room));
    }

    @Test
    void searchOptionIsYellowWhenSomethingWasLeftBehind() {
        Room room = new Room("Kafferummet", "Det luktar kaffe.", enemy("JavaFläcken"),
                List.of(new Weapon(ItemType.BOW, WeaponRange.RANGED, "Pilbåge", 3, 1)));
        assertTrue(GameEngine.searchOption(room).startsWith(AsciiArt.YELLOW));
        assertTrue(GameEngine.menu(room).contains("(något finns här!)"));
    }
}
