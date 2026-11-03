package se.fangdjupet.combat;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

// Dela skärm (hp i stället för skada) och Java-boken (man kan slinta på sidan) i en riktig strid
class BattleAgainstMartinTest {

    // En Random där varje slag missar (nextInt ger 0 < alla misschanser) – utom Dela skärm, som aldrig missar
    private static Random alwaysMiss() {
        return new Random() {
            @Override
            public int nextInt(int bound) {
                return 0;
            }
        };
    }

    @Test
    void screenSharingHealsTheHeroAndDoesNotHurtMartin() {
        List<String> log = new ArrayList<>();
        Hero hero = new Hero("Robert", 40, new MeleeAttack(1));
        hero.takeDamage(25);                                    // 15 hp kvar
        Boss martin = Martin.create();
        int[] round = {0};
        new Battle(log::add, h -> round[0]++ == 0 ? KnowledgeAttack.SCREENSHARE : h.getJavaBookAttack()).fight(hero, martin);
        assertTrue(log.contains("Hero: Robert delar skärmen och tar över lektionen: +10 hp, ingen skada på Lektor Martin"), log.toString());
        assertTrue(log.contains("Robert  [######----] 25/40"), "15 + 10 = 25: " + log);
    }

    @Test
    void screenSharingNeverMissesEvenWhenEverythingElseDoes() {
        List<String> log = new ArrayList<>();
        Hero hero = new Hero("Robert", 40, new MeleeAttack(1));
        new Battle(log::add, h -> KnowledgeAttack.SCREENSHARE, alwaysMiss()).fight(hero, Martin.create());
        assertTrue(log.stream().anyMatch(l -> l.startsWith("Hero: Robert delar skärmen")), log.toString());
        assertTrue(log.stream().noneMatch(l -> l.startsWith("Hero: Robert missar")), log.toString());
    }

    @Test
    void theJavaBookCanSlipOnThePage() {
        List<String> log = new ArrayList<>();
        Hero hero = new Hero("Robert", 40, new MeleeAttack(1));
        hero.pickUpJavaBook();
        new Battle(log::add, Hero::getJavaBookAttack, alwaysMiss()).fight(hero, Martin.create());
        assertTrue(log.contains("Hero: Robert slinter på sidan och missar Lektor Martin!"), log.toString());
    }

    @Test
    void theBookMissesMoreOftenThanOrdinaryHits() {
        // Med en riktig slump: 30 % mot 10 %, räknat över många slag
        Random random = new Random(7);
        int slips = 0, n = 20000;
        JavaBookAttack book = new JavaBookAttack();
        for (int i = 0; i < n; i++) if (random.nextInt(100) < book.missPercent()) slips++;
        assertEquals(0.30, slips / (double) n, 0.02);
    }
}
