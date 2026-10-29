package se.fangdjupet.combat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HpBarTest {

    // En fiende med 40 hp och en strategi som aldrig gör skada – bara livet spelar roll här.
    private Enemy troll() {
        return new Enemy("Troll", 40, (attacker, target) -> 0, 1);
    }

    @Test
    void fullHpGivesFullBar() {
        assertEquals("[##########]", HpBar.render(troll(), 10));
    }

    @Test
    void halfHpGivesHalfBar() {
        Enemy t = troll();
        t.takeDamage(20);
        assertEquals("[#####-----]", HpBar.render(t, 10));
    }

    @Test
    void deadGivesEmptyBar() {
        Enemy t = troll();
        t.takeDamage(1000);
        assertEquals("[----------]", HpBar.render(t, 10));
    }

    @Test
    void littleHpLeftStillShowsOneMark() {
        Enemy t = troll();
        t.takeDamage(39);                               // 1/40 hp, avrundas annars till 0
        assertEquals("[#---------]", HpBar.render(t, 10));
    }

    @Test
    void statusShowsNameBarAndNumbers() {
        Enemy t = troll();
        t.takeDamage(12);
        assertEquals("Troll  [#######---] 28/40", HpBar.status(t, 10));
    }

    @Test
    void badArgumentsThrow() {
        assertThrows(IllegalArgumentException.class, () -> HpBar.render(troll(), 0));
        assertThrows(IllegalArgumentException.class, () -> HpBar.render(null, 10));
    }
}
