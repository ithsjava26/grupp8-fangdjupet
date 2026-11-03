package se.fangdjupet.combat;

import org.junit.jupiter.api.Test;
import se.fangdjupet.items.ItemType;
import se.fangdjupet.items.StressBall;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// Stressbollen från Lektionssalen: kläm den (+5 hp varje runda) eller kasta den på Martin (10 i skada, sedan borta)
class StressBallTest {

    private static final StressBall BALL = new StressBall(ItemType.STRESSBALL, "Stressboll", 5, 3);

    private Hero heroWithBall() {
        Hero hero = new Hero("Robert", 40, new MeleeAttack(1));
        hero.pickUpJavaBook();
        hero.pickUp(BALL);
        return hero;
    }

    @Test
    void squeezingGivesFiveHpButNeverMoreThanMax() {
        Hero hero = heroWithBall();
        hero.takeDamage(12);
        assertEquals(5, hero.squeezeStressBall());
        assertEquals(33, hero.getHp());
        hero.heal(5);                                 // 38
        assertEquals(2, hero.squeezeStressBall());   // 38 → 40, inte över max
        assertEquals(0, hero.squeezeStressBall());
    }

    @Test
    void withoutABallNothingHappens() {
        Hero hero = new Hero("Robert", 40, new MeleeAttack(1));
        hero.takeDamage(10);
        assertEquals(0, hero.squeezeStressBall());
        assertTrue(hero.getStressBall().isEmpty());
    }

    @Test
    void battleSqueezesTheBallEveryRound() {
        List<String> log = new ArrayList<>();
        Hero hero = heroWithBall();
        hero.takeDamage(20);
        new Battle(log::add, Hero::getJavaBookAttack).fight(hero, Martin.create());
        assertTrue(log.contains("Robert klämmer stressbollen: +5 hp"), log.toString());
        long squeezes = log.stream().filter(l -> l.contains("klämmer stressbollen")).count();
        assertTrue(squeezes >= 3, "en gång per runda: " + log);
    }

    @Test
    void throwingTheBallHitsForTenAndThenItIsGone() {
        List<String> log = new ArrayList<>();
        Hero hero = heroWithBall();
        hero.takeDamage(20);                           // 20 hp: klämmer första rundan
        Boss martin = Martin.create();
        int[] round = {0};
        new Battle(log::add, h -> round[0]++ == 0 ? StressBallThrow.THROW : null).fight(hero, martin);
        assertTrue(log.contains("Hero: Robert kastar stressbollen på Lektor Martin skada: 10"), log.toString());
        assertTrue(hero.getStressBall().isEmpty(), "bollen är borta efter kastet");
        assertEquals(1, log.stream().filter(l -> l.contains("klämmer stressbollen")).count(), "bara före kastet: " + log);
        // Enter efter kastet: hjälten citerar ur boken i stället för att kasta en boll hen inte har
        assertTrue(log.stream().anyMatch(l -> l.startsWith("Hero: Robert citerar")), log.toString());
    }

    @Test
    void aMissedThrowBouncesBack() {
        StressBallThrow attack = StressBallThrow.THROW;
        assertEquals("kastar stressbollen förbi", attack.missVerb());
        Hero hero = heroWithBall();
        assertTrue(hero.getStressBall().isPresent());   // en miss räknar aldrig ut skadan: bollen är kvar
    }

    @Test
    void theThrowIsStrongButTheBookIsBetter() {
        assertEquals(10, StressBallThrow.POWER);
        assertTrue(JavaBookAttack.POWER > StressBallThrow.POWER);
    }

    @Test
    void aBallMustHeal() {
        assertThrows(IllegalArgumentException.class, () -> new StressBall(ItemType.STRESSBALL, "Platt boll", 0, 1));
        assertThrows(IllegalArgumentException.class, () -> heroWithBall().pickUp(null));
    }
}
