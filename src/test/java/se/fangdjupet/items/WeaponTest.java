package se.fangdjupet.items;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class WeaponTest {
    @Test
    public void testWeaponInfo() {
        Weapon weapon = new Weapon(ItemType.SWORD, WeaponRange.MELEE, "A steel sword",7);
        assertEquals("Description: A steel sword. Damage bonus: 7",weapon.info());
    }
}
