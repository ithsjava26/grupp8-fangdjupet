package se.fangdjupet.items;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ArmorTest {

    @Test
    public void testArmorInfo() {
        Armor armor = new Armor(ItemType.SHIELD, "A wooden shield", 7);
        assertEquals("Description: A wooden shield. Defense: 7", armor.info());
    }
}
