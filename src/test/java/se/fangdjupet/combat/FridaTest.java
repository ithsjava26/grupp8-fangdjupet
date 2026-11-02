package se.fangdjupet.combat;

import org.junit.jupiter.api.Test;
import se.fangdjupet.items.ItemType;
import se.fangdjupet.items.Potion;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FridaTest {

    @Test
    void fridaHasANameAndAGreeting() {
        Frida frida = Frida.create();
        assertEquals("Frida", frida.getName());
        assertFalse(frida.greeting().isBlank());
    }

    @Test
    void fridaGivesTwoHealthPotions() {
        List<Potion> gift = Frida.create().give();
        assertEquals(2, gift.size());
        assertTrue(gift.stream().allMatch(p -> p.type() == ItemType.POTION));
    }

    @Test
    void fridaGivesOnlyOnce() {
        Frida frida = Frida.create();
        frida.give();
        assertTrue(frida.give().isEmpty());
        assertTrue(frida.hasGiven());
    }

    @Test
    void bothPotionsHealTheHeroToFullHealth() {
        // Hjälten har bara 1 hp kvar efter StädHäxan – Fridas två drycker räcker ändå hela vägen
        Hero hero = new Hero("Robert", 40, new MeleeAttack(10));
        hero.takeDamage(39);
        Frida.create().give().forEach(hero::drink);
        assertEquals(40, hero.getHp());
    }

    @Test
    void potionsNeverHealOverMaxHp() {
        Hero hero = new Hero("Robert", 40, new MeleeAttack(10));
        hero.takeDamage(5);
        Frida.create().give().forEach(hero::drink);
        assertEquals(40, hero.getHp());
    }

    @Test
    void eachNewFridaHasHerOwnGift() {
        Frida first = Frida.create();
        first.give();
        assertEquals(2, Frida.create().give().size());
    }
}
