package se.fangdjupet.items;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class InventoryTest {

    private Inventory inventory;

    @BeforeEach
    void setUp() {
        inventory = new Inventory(3);
    }

    @Test
    void addItem() {
        Potion potion = new Potion(ItemType.POTION, "Health potion", 20, 1);
        assertTrue(inventory.add(potion));
        assertEquals(1, inventory.size());
    }

    @Test
    void removeItem() {
        Potion potion = new Potion(ItemType.POTION, "Health potion", 20, 1);
        inventory.add(potion);
        assertTrue(inventory.remove(potion));
        assertEquals(0, inventory.size());
    }

    @Test
    void removeItemThatDoesNotExist() {
        Potion potion = new Potion(ItemType.POTION, "Health potion", 20, 1);
        assertFalse(inventory.remove(potion));
    }

    @Test
    void inventoryFull() {
        Potion potion1 = new Potion(ItemType.POTION, "Potion 1", 10, 1);
        Potion potion2 = new Potion(ItemType.POTION, "Potion 2", 20, 1);
        Potion potion3 = new Potion(ItemType.POTION, "Potion 3", 30, 1);
        Potion potion4 = new Potion(ItemType.POTION, "Potion 4", 40, 1);
        assertTrue(inventory.add(potion1));
        assertTrue(inventory.add(potion2));
        assertTrue(inventory.add(potion3));
        assertFalse(inventory.add(potion4));
        assertEquals(3, inventory.size());
    }

    @Test
    void addNullThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> inventory.add(null));
    }

    @Test
    void getAllReturnsAllItems() {
        Potion potion = new Potion(ItemType.POTION, "Health potion", 20, 1);
        inventory.add(potion);
        assertEquals(1, inventory.getAll().size());
        assertEquals(potion, inventory.getAll().get(0));
    }

    @Test
    void getAllIsImmutable() {
        Potion potion = new Potion(ItemType.POTION, "Health potion", 20, 1);
        inventory.add(potion);
        List<Item> items = inventory.getAll();
        assertThrows(UnsupportedOperationException.class, () -> items.add(potion));
    }

    @Test
    void getAllPotions() {
        Potion potion1 = new Potion(ItemType.POTION, "Potion 1", 20, 1);
        Potion potion2 = new Potion(ItemType.POTION, "Potion 2", 30, 1);
        inventory.add(potion1);
        inventory.add(potion2);
        List<Potion> potions = inventory.getAll(Potion.class);
        assertEquals(2, potions.size());
        assertTrue(potions.contains(potion1));
        assertTrue(potions.contains(potion2));
    }

    @Test
    void findByType() {
        Potion potion = new Potion(
                ItemType.POTION, "Health potion", 20, 1);
        inventory.add(potion);
        List<Item> result = inventory.findByType(ItemType.POTION);
        assertEquals(1, result.size());
        assertEquals(potion, result.get(0));
    }

    @Test
    void emptyInventory() {
        assertTrue(inventory.isEmpty());
        assertEquals(0, inventory.size());
    }

    @Test
    void isFullReturnsCorrectValue() {
        assertFalse(inventory.isFull());
        inventory.add(new Potion(ItemType.POTION, "Potion 1", 10, 1));
        inventory.add(new Potion(ItemType.POTION, "Potion 2", 20, 1));
        inventory.add(new Potion(ItemType.POTION, "Potion 3", 30, 1));
        assertTrue(inventory.isFull());
    }

    @Test
    void invalidCapacityThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> new Inventory(0));
    }
}