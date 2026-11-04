package se.fangdjupet.combat;

import org.junit.jupiter.api.Test;
import se.fangdjupet.game.AttackMenu;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// Martins lavalampa i fas 2: +3 hp varje runda tills hjälten krossar den
class LavaLampTest {

    private static Hero robert() {
        Hero h = new Hero("Robert", 40, new MeleeAttack(1));
        h.pickUpJavaBook();
        return h;
    }

    @Test
    void theLampOnlyStandsInPhaseTwo() {
        Boss martin = Martin.create();
        assertFalse(martin.lavaLampStanding(), "fas 1: ingen lampa");
        martin.takeDamage(50);
        assertTrue(martin.lavaLampStanding(), "fas 2: lampan står på katedern");
        assertEquals(Martin.LAVA_LAMP_HEAL, martin.lavaLampHeal());
        assertEquals(3, Martin.LAVA_LAMP_HEAL);
    }

    @Test
    void smashingTheLampWorksOnce() {
        Boss martin = Martin.create();
        assertFalse(martin.smashLavaLamp(), "ingen lampa i fas 1");
        martin.takeDamage(50);
        assertTrue(martin.smashLavaLamp());
        assertFalse(martin.lavaLampStanding());
        assertFalse(martin.smashLavaLamp(), "redan krossad");
    }

    @Test
    void otherBossesHaveNoLamp() {
        Boss witch = CleaningWitch.create(new java.util.Random(1));
        witch.takeDamage(witch.getMaxHp() / 2);
        assertFalse(witch.lavaLampStanding());
        assertThrows(IllegalArgumentException.class, () -> witch.withLavaLamp(0));
    }

    @Test
    void martinBringsOutTheLampAndItHealsHimEveryRound() {
        List<String> log = new ArrayList<>();
        Hero hero = robert();
        Boss martin = Martin.create();
        martin.takeDamage(49);                                       // 51 hp: nästa slag tar honom till fas 2
        int[] round = {0};
        new Battle(log::add, h -> round[0]++ < 3 ? KnowledgeAttack.SCREENSHARE : null).fight(hero, martin);
        assertTrue(log.stream().noneMatch(l -> l.contains("lavalampa")), "fas 1 hela striden: ingen lampa " + log);

        log.clear();
        Hero hero2 = robert();
        Boss martin2 = Martin.create();
        martin2.takeDamage(50);                                      // 50 hp: fas 2 direkt
        int[] r2 = {0};
        new Battle(log::add, h -> r2[0]++ == 0 ? KnowledgeAttack.SCREENSHARE : null).fight(hero2, martin2);
        assertEquals(1, log.stream().filter(l -> l.contains("tar fram en lavalampa")).count(), "tas fram en gång: " + log);
        assertTrue(log.contains("Lavalampan bubblar: Lektor Martin +3 hp"), log.toString());
        assertTrue(log.contains("Lektor Martin  [#####-----] 53/100"), "50 + 3 = 53: " + log);
    }

    @Test
    void smashingTheLampStopsTheHealingAndGoesBackToTheBook() {
        List<String> log = new ArrayList<>();
        Hero hero = robert();
        Boss martin = Martin.create();
        martin.takeDamage(50);
        int[] round = {0};
        List<AttackStrategy> held = new ArrayList<>();
        new Battle(log::add, h -> {
            held.add(h.getAttack());
            return round[0]++ == 0 ? LavaLampSmash.SMASH : null;
        }).fight(hero, martin);
        assertTrue(log.contains("Hero: Robert krossar lavalampan! Lavan rinner ut över katedern – Lektor Martin får inga fler hp av den."), log.toString());
        assertTrue(log.stream().noneMatch(l -> l.startsWith("Lavalampan bubblar")), "krossad före Martins första tur: " + log);
        assertInstanceOf(JavaBookAttack.class, held.get(1), "nästa runda: boken igen");
    }

    @Test
    void smashingWithoutALampDoesNothing() {
        List<String> log = new ArrayList<>();
        int[] round = {0};
        new Battle(log::add, h -> round[0]++ == 0 ? LavaLampSmash.SMASH : null).fight(robert(), Martin.create());
        assertTrue(log.contains("Hero: Robert letar efter en lavalampa att krossa, men det finns ingen."), log.toString());
    }

    @Test
    void theMenuShowsTheLampInYellowOnlyWhileItStands() {
        Hero hero = robert();
        Boss martin = Martin.create();
        assertFalse(AttackMenu.options(hero, martin).contains(LavaLampSmash.SMASH), "fas 1");
        martin.takeDamage(50);
        List<AttackStrategy> options = AttackMenu.options(hero, martin);
        assertSame(LavaLampSmash.SMASH, options.getLast(), "sist i menyn");
        String line = AttackMenu.lines(hero, options).getLast();
        assertTrue(line.startsWith("\u001B["), "gulmarkerad: " + line);
        assertTrue(line.contains(options.size() + ". Krossa lavalampan  (Martin får +3 hp varje runda så länge den står"), line);
        martin.smashLavaLamp();
        assertFalse(AttackMenu.options(hero, martin).contains(LavaLampSmash.SMASH), "krossad: borta ur menyn");
    }

    @Test
    void theLampHasAPicture() {
        assertTrue(AttackArt.LAVA_LAMP.contains("LAVALAMPA"));
        assertTrue(AttackArt.LAVA_LAMP.contains("+3 hp till Martin"));
    }
}
