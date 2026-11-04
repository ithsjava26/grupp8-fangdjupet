package se.fangdjupet.combat;

import org.junit.jupiter.api.Test;
import se.fangdjupet.game.AttackMenu;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class UsbVirusAttackTest {

    private Hero heroWithBook() {
        Hero h = new Hero("Robert", 40, new MeleeAttack(6));
        h.pickUpJavaBook();
        return h;
    }

    // Slumpen ger alltid samma tal: 0 = försöket misslyckas (under 80), 99 = det lyckas
    private static Random always(int value) {
        return new Random() { @Override public int nextInt(int bound) { return Math.min(value, bound - 1); } };
    }

    @Test
    void itSucceedsOneTimeInFive() {
        assertEquals(20, UsbVirusAttack.SUCCESS_PERCENT);
        assertEquals(80, UsbVirusAttack.PLUG_IN.missPercent());
        assertTrue(UsbVirusAttack.PLUG_IN.instantWin());
    }

    @Test
    void whenItWorksTheHeroWinsAtOnce() {
        Hero h = heroWithBook();
        Boss martin = Martin.create();
        List<String> log = new ArrayList<>();
        AttackChooser usb = hero -> hero.hasUsbStick() ? UsbVirusAttack.PLUG_IN : hero.getJavaBookAttack();
        assertSame(h, new Battle(log::add, usb, always(99)).fight(h, martin));
        assertEquals(0, martin.getHp());
        assertEquals(40, h.getHp(), "Martin hann inte förklara något");
        assertTrue(log.contains("Hero: Robert smyger in ett USB-minne med virus i datorn hos Lektor Martin skada: 100"), log.toString());
    }

    @Test
    void whenItFailsMartinTakesTheStick() {
        Hero h = heroWithBook();
        Boss martin = Martin.create();
        List<String> log = new ArrayList<>();
        int[] round = {0};
        AttackChooser usbOnce = hero -> round[0]++ == 0 ? UsbVirusAttack.PLUG_IN : null;   // sedan Enter = behåll
        new Battle(log::add, usbOnce, always(0)).fight(h, martin);
        assertTrue(log.contains("Hero: Robert försöker smyga in ett USB-minne, men blir påkommen av Lektor Martin!"), log.toString());
        assertFalse(h.hasUsbStick(), "ett försök – sedan är det borta");
        assertEquals(1, log.stream().filter(l -> l.contains("USB-minne")).count(), "Enter efteråt citerar ur boken i stället");
    }

    @Test
    void theMenuOffersItOnlyWhileTheHeroHasIt() {
        Hero h = heroWithBook();
        assertTrue(AttackMenu.options(h).contains(UsbVirusAttack.PLUG_IN));
        h.setAttack(UsbVirusAttack.PLUG_IN);
        h.loseUsbStick();
        assertFalse(AttackMenu.options(h).contains(UsbVirusAttack.PLUG_IN));
        assertInstanceOf(JavaBookAttack.class, h.getAttack(), "tillbaka till boken");
    }

    @Test
    void itHasItsOwnPicture() {
        assertTrue(AttackArt.forAttack(UsbVirusAttack.PLUG_IN).orElseThrow().contains("SMYG IN USB-MINNET"));
        assertTrue(AttackArt.USB_VIRUS.contains("20 % chans"));
    }
}
