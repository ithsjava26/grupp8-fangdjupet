package se.fangdjupet.game;

import java.util.List;

// Rummen i ordning: Skoldörren → Kafferummet → Korridoren och städskrubben → Lektionssalen.
// Hjälten får gå tillbaka till alla rum hen har varit i, och vidare till nästa nya rum
// när rummet längst fram är rensat. Ett rum längre bort är låst.
public class World {
    private final List<Room> rooms;
    private int currentRoomIndex;
    private int furthestRoomIndex;   // det rum längst fram som hjälten har nått
    private boolean entranceLocked;  // dörren ut (första rummet) slår igen när hjälten går in i skolan

    public World(List<Room> rooms) {
        if (rooms == null || rooms.isEmpty()) {
            throw new IllegalArgumentException("World måste ha minst ett rum");
        }

        this.rooms = List.copyOf(rooms);
        this.currentRoomIndex = 0;
        this.furthestRoomIndex = 0;
    }

    public Room getCurrentRoom() {
        return rooms.get(currentRoomIndex);
    }

    // Alla rum i ordning (listan går inte att ändra)
    public List<Room> getRooms() {
        return rooms;
    }

    public boolean canMoveForward() {
        return currentRoomIndex < rooms.size() - 1;
    }

    // Ett steg fram, utan att titta på fienderna (används när en sparning läses in)
    public boolean moveForward() {
        if (!canMoveForward()) {
            return false;
        }

        currentRoomIndex++;
        furthestRoomIndex = Math.max(furthestRoomIndex, currentRoomIndex);
        entranceLocked = true;
        return true;
    }

    // Inlåst: när hjälten har gått in i skolan går det inte att gå ut genom första rummet (Skoldörren)
    // förrän slutbossen i sista rummet är besegrad
    public boolean isEntranceLocked() {
        return entranceLocked && rooms.getLast().hasLivingEnemy();
    }

    // Är rummet öppet? Alla rum hjälten har varit i, och nästa nya rum om rummet längst fram är rensat.
    // Undantag: första rummet är låst när hjälten är inlåst.
    public boolean isOpen(int index) {
        if (index < 0 || index >= rooms.size()) return false;
        if (index == 0 && isEntranceLocked()) return false;
        if (index <= furthestRoomIndex) return true;
        return index == furthestRoomIndex + 1 && !rooms.get(furthestRoomIndex).hasLivingEnemy();
    }

    // Har hjälten varit i rummet?
    public boolean hasVisited(int index) {
        return index >= 0 && index <= furthestRoomIndex;
    }

    // Går till ett valfritt öppet rum (spelmenyn 2). false om rummet är låst, inte finns eller är det man står i.
    public boolean moveTo(int index) {
        if (index == currentRoomIndex || !isOpen(index)) {
            return false;
        }
        currentRoomIndex = index;
        furthestRoomIndex = Math.max(furthestRoomIndex, index);
        if (index > 0) entranceLocked = true;   // in i skolan: dörren slår igen bakom hjälten
        return true;
    }

    public boolean isAtFinalRoom() {
        return currentRoomIndex == rooms.size() - 1;
    }

    public int getCurrentRoomIndex() {
        return currentRoomIndex;
    }

    public int getFurthestRoomIndex() {
        return furthestRoomIndex;
    }
}
