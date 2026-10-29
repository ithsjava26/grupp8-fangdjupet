package se.fangdjupet.items;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PotionTest {

    @Test
    public void testPotionInfo () {
        Potion potion = new Potion(ItemType.POTION, "A herbal potion", 8, 42);
        Item item = potion;
        assertEquals(42, item.level());
        assertEquals("Description: A herbal potion. Heal amount: 8", potion.info());
    }
}
