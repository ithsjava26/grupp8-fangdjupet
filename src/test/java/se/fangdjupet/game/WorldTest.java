package se.fangdjupet.game;

import org.junit.jupiter.api.Test;
import se.fangdjupet.combat.Enemy;
import se.fangdjupet.combat.MeleeAttack;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WorldTest {

    private Room room(String name) {
        return new Room(name, "Ett rum.", new Enemy("Troll", 10, new MeleeAttack(3), 1), List.of());
    }

    private World fourRooms() {
        return new World(List.of(room("A"), room("B"), room("C"), room("D")));
    }

    private void clear(Room room) {
        while (room.hasLivingEnemy()) room.getEnemies().forEach(e -> e.takeDamage(999));
    }

    @Test
    void startsInTheFirstRoomWithEverythingElseLocked() {
        World world = fourRooms();
        assertTrue(world.isOpen(0));
        assertFalse(world.isOpen(1), "fienden i A lever");
        assertFalse(world.isOpen(3));
    }

    @Test
    void theNextRoomOpensWhenTheFrontRoomIsCleared() {
        World world = fourRooms();
        clear(world.getCurrentRoom());
        assertTrue(world.isOpen(1));
        assertFalse(world.isOpen(2), "man kan inte hoppa över ett rum");
    }

    @Test
    void canGoBackToVisitedRooms() {
        World world = fourRooms();
        clear(world.getCurrentRoom());
        assertTrue(world.moveTo(1));
        assertTrue(world.moveTo(0));
        assertEquals(0, world.getCurrentRoomIndex());
        assertEquals(1, world.getFurthestRoomIndex(), "längst fram är fortfarande B");
        assertTrue(world.hasVisited(1));
    }

    @Test
    void cannotMoveToALockedRoomOrTheSameRoom() {
        World world = fourRooms();
        assertFalse(world.moveTo(1));
        assertFalse(world.moveTo(0));
        assertFalse(world.moveTo(-1));
        assertFalse(world.moveTo(4));
        assertEquals(0, world.getCurrentRoomIndex());
    }

    @Test
    void goingBackDoesNotOpenMoreRooms() {
        World world = fourRooms();
        clear(world.getCurrentRoom());
        world.moveTo(1);          // B har en levande fiende
        world.moveTo(0);
        assertFalse(world.isOpen(2));
    }

    @Test
    void moveForwardAlsoCountsAsVisited() {
        World world = fourRooms();
        world.moveForward();
        world.moveForward();
        assertEquals(2, world.getFurthestRoomIndex());
        assertTrue(world.moveTo(0));
    }

    @Test
    void roomsListCannotBeChanged() {
        assertThrows(UnsupportedOperationException.class, () -> fourRooms().getRooms().clear());
    }
}
