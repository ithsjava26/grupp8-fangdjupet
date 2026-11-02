package se.fangdjupet.game;

import org.junit.jupiter.api.Test;
import se.fangdjupet.combat.Enemy;
import se.fangdjupet.combat.MagicAttack;
import se.fangdjupet.combat.MeleeAttack;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class RoomTest {

    private Enemy enemy(String name) {
        return new Enemy(name, 10, new MeleeAttack(3), 1);
    }

    private Room coffeeRoom(Enemy... enemies) {
        return new Room("Kafferummet", "Det luktar kaffe.", List.of(enemies), List.of());
    }

    @Test
    void singleEnemyRoomStillWorks() {
        Enemy fleck = enemy("JavaFläcken");
        Room room = new Room("Kafferummet", "Det luktar kaffe.", fleck, List.of());
        assertSame(fleck, room.getEnemy());
        assertTrue(room.hasLivingEnemy());
    }

    @Test
    void roomWithoutEnemyHasNoEnemy() {
        Room room = new Room("Kafferummet", "Det luktar kaffe.", (Enemy) null, List.of());
        assertNull(room.getEnemy());
        assertFalse(room.hasLivingEnemy());
    }

    @Test
    void firstEnemyComesFirst() {
        Enemy fleck = enemy("JavaFläcken");
        Room room = coffeeRoom(fleck, enemy("Troll"), enemy("Trollkarl"));
        assertSame(fleck, room.getEnemy());
    }

    @Test
    void nextEnemyAppearsWhenTheFirstIsDefeated() {
        Enemy fleck = enemy("JavaFläcken");
        Enemy troll = enemy("Troll");
        Room room = coffeeRoom(fleck, troll, enemy("Trollkarl"));
        fleck.takeDamage(100);
        assertSame(troll, room.getEnemy());
        assertTrue(room.hasLivingEnemy());
    }

    @Test
    void enemiesComeInOrder() {
        Enemy fleck = enemy("JavaFläcken");
        Enemy troll = enemy("Troll");
        Enemy wizard = new Enemy("Trollkarl", 10, new MagicAttack(4), 1);
        Room room = coffeeRoom(fleck, troll, wizard);
        fleck.takeDamage(100);
        troll.takeDamage(100);
        assertSame(wizard, room.getEnemy());
    }

    @Test
    void roomIsClearWhenAllEnemiesAreDefeated() {
        Enemy fleck = enemy("JavaFläcken");
        Enemy troll = enemy("Troll");
        Room room = coffeeRoom(fleck, troll);
        fleck.takeDamage(100);
        troll.takeDamage(100);
        assertNull(room.getEnemy());
        assertFalse(room.hasLivingEnemy());
    }

    @Test
    void enemyListCannotBeChangedFromOutside() {
        java.util.ArrayList<Enemy> list = new java.util.ArrayList<>(List.of(enemy("JavaFläcken")));
        Room room = new Room("Kafferummet", "Det luktar kaffe.", list, List.of());
        list.clear();
        assertTrue(room.hasLivingEnemy());
    }

    private World world() {
        return new WorldBuilder().build(enemy("Entrédörren"), enemy("StädHäxan"),
                enemy("JavaFläcken"), enemy("Kapten Martin"));
    }

    @Test
    void coffeeRoomHasFleckFirst() {
        World world = world();
        world.moveForward();                                   // Skoldörren → Kafferummet
        Room room = world.getCurrentRoom();
        assertEquals("Kafferummet", room.getName());
        assertEquals(List.of(room.getEnemy()), room.getEnemies());
        assertEquals("JavaFläcken", room.getEnemy().getName());
    }

    @Test
    void trollAndWizardComeTogetherAfterTheFleck() {
        World world = world();
        world.moveForward();
        Room room = world.getCurrentRoom();
        room.getEnemy().takeDamage(1000);                      // JavaFläcken besegrad
        List<Enemy> together = room.getEnemies();
        assertEquals(2, together.size());
        assertEquals("Troll", together.get(0).getName());
        assertEquals("Trollkarl", together.get(1).getName());
        assertEquals(12, together.get(0).getMaxHp());
        assertEquals(16, together.get(1).getMaxHp());
        assertInstanceOf(MagicAttack.class, together.get(1).getAttack());   // trollkarlen använder magi
        assertEquals("Thou shall not pass!", together.get(1).getLine().orElseThrow());
    }

    @Test
    void groupStaysUntilBothAreDefeated() {
        Enemy troll = enemy("Troll");
        Enemy wizard = enemy("Trollkarl");
        Room room = coffeeRoom(enemy("JavaFläcken")).thenTogether(troll, wizard);
        room.getEnemy().takeDamage(1000);
        troll.takeDamage(1000);
        assertEquals(List.of(wizard), room.getEnemies());     // trollkarlen står kvar ensam
        wizard.takeDamage(1000);
        assertTrue(room.getEnemies().isEmpty());
        assertFalse(room.hasLivingEnemy());
    }

    @Test
    void coffeeIsWaitingInTheCoffeeRoom() {
        World world = world();
        world.moveForward();
        Room room = world.getCurrentRoom();
        Room.Refreshment coffee = room.takeRefreshment().orElseThrow();
        assertTrue(coffee.line().contains("kaffe"));
        assertEquals(30, coffee.potion().healAmount());
        assertTrue(room.takeRefreshment().isEmpty());          // kaffet går bara att dricka en gång
    }

    @Test
    void cinnamonBunWaitsWhenTheCoffeeRoomIsCleared() {
        World world = world();
        world.moveForward();
        Room room = world.getCurrentRoom();
        Room.Refreshment bun = room.takeReward().orElseThrow();
        assertTrue(bun.line().contains("kanelbulle"));
        assertEquals(35, bun.potion().healAmount());
        assertTrue(room.takeReward().isEmpty());               // bara en kanelbulle
    }

    @Test
    void coffeeAndBunAreDifferent() {
        World world = world();
        world.moveForward();
        Room room = world.getCurrentRoom();
        assertNotEquals(room.takeRefreshment().orElseThrow(), room.takeReward().orElseThrow());
    }

    // ── Java-boken i Lektionssalen ──

    @Test
    void classroomHasTheJavaBook() {
        World world = world();
        world.moveForward();
        world.moveForward();
        world.moveForward();                                   // → Lektionssalen
        assertEquals("Lektionssalen", world.getCurrentRoom().getName());
        assertTrue(world.getCurrentRoom().takeJavaBook());
    }

    @Test
    void theJavaBookCanOnlyBeTakenOnce() {
        Room room = coffeeRoom().withJavaBook();
        assertTrue(room.takeJavaBook());
        assertFalse(room.takeJavaBook());                      // andra gången är boken redan tagen
    }

    @Test
    void otherRoomsHaveNoJavaBook() {
        World world = world();
        assertFalse(world.getCurrentRoom().takeJavaBook());    // Skoldörren
        world.moveForward();
        assertFalse(world.getCurrentRoom().takeJavaBook());    // Kafferummet
    }

    @Test
    void otherRoomsHaveNoReward() {
        assertTrue(world().getCurrentRoom().takeReward().isEmpty());   // Skoldörren
    }

    @Test
    void otherRoomsHaveNoRefreshment() {
        World world = world();
        assertTrue(world.getCurrentRoom().takeRefreshment().isEmpty());   // Skoldörren
    }

    @Test
    void fridaWaitsInTheCorridorBeforeMartin() {
        World world = new WorldBuilder().build(enemy("Entrédörren"), enemy("StädHäxan"),
                enemy("JavaFläcken"), enemy("Kapten Martin"));
        assertTrue(world.getCurrentRoom().getFrida().isEmpty());          // Skoldörren
        world.moveForward();
        assertTrue(world.getCurrentRoom().getFrida().isEmpty());          // Kafferummet
        world.moveForward();
        assertTrue(world.getCurrentRoom().getFrida().isPresent());        // Korridoren och städskrubben
        world.moveForward();
        assertTrue(world.getCurrentRoom().getFrida().isEmpty());          // Lektionssalen
    }

    @Test
    void roomWithoutFridaHasNoFrida() {
        assertTrue(coffeeRoom(enemy("JavaFläcken")).getFrida().isEmpty());
    }

    @Test
    void cleaningWitchThreatensWithChemicals() {
        World world = new WorldBuilder().build(enemy("Entrédörren"), enemy("StädHäxan"),
                enemy("JavaFläcken"), enemy("Kapten Martin"));
        world.moveForward();
        world.moveForward();                                   // Kafferummet → Korridoren och städskrubben
        Room room = world.getCurrentRoom();
        assertEquals("StädHäxan", room.getEnemy().getName());
        assertTrue(room.getEnemy().getLine().orElseThrow().contains("kemikalier"));
    }
}
