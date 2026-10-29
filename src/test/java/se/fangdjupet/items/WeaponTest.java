package se.fangdjupet.items;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class WeaponTest {
    @Test
    public void testWeaponInfo() {
        Weapon weapon = new Weapon(ItemType.SWORD, WeaponRange.MELEE, "A steel sword",7, 42);
        Item item = weapon;
        assertEquals(42, item.level());
        assertEquals("Description: A steel sword. Damage bonus: 7",weapon.info());
    }
}
