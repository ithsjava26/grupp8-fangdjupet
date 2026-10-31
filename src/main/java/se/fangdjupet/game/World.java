package se.fangdjupet.game;

import java.util.List;

public class World {
    private final List<Room> rooms;
    private int currentRoomIndex;

    public World(List<Room> rooms) {
        if (rooms == null || rooms.isEmpty()) {
            throw new IllegalArgumentException("World måste ha minst ett rum");
        }

        this.rooms = List.copyOf(rooms);
        this.currentRoomIndex = 0;
    }

    public Room getCurrentRoom() {
        return rooms.get(currentRoomIndex);
    }

    public boolean canMoveForward() {
        return currentRoomIndex < rooms.size() - 1;
    }

    public boolean moveForward() {
        if (!canMoveForward()) {
            return false;
        }

        currentRoomIndex++;
        return true;
    }

    public boolean isAtFinalRoom() {
        return currentRoomIndex == rooms.size() - 1;
    }

    public int getCurrentRoomIndex() {
        return currentRoomIndex;
    }
}