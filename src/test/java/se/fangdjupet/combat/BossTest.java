package se.fangdjupet.combat;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BossTest {

    // Två attacker som går att känna igen: fas 1 gör 3, fas 2 gör 9
    private final AttackStrategy phaseOne = (a, t) -> 3;
    private final AttackStrategy phaseTwo = (a, t) -> 9;
    private final AttackStrategy rage = (a, t) -> 12;

    // En boss med 80 hp – halva livet är 40
    @Test
    void rageMustComeAfterPhaseTwoAndHighestHpFirst() {
        assertThrows(IllegalArgumentException.class, () -> boss().withRage(40, rage));
        assertThrows(IllegalArgumentException.class, () -> boss().withRage(0, rage));
        assertThrows(IllegalArgumentException.class, () -> boss().withRage(20, null));
        assertThrows(IllegalArgumentException.class, () -> boss().withRage(10, rage).withRage(20, rage));
    }

    @Test
    void rageReplacesPhaseTwoOnlyAtItsHp() {
        Boss b = boss().withRage(20, rage);

        b.takeDamage(40);
        assertSame(phaseTwo, b.getAttack());

        b.takeDamage(20);
        assertSame(rage, b.getAttack());
    }

    private Boss boss() {
        return new Boss("Martin", 80, phaseOne, phaseTwo, 5);
    }

    @Test
    void bossIsAnEnemy() {
        assertInstanceOf(Enemy.class, boss());      // Boss extends Enemy – går att skicka till Battle
    }

    @Test
    void bossStartsInPhaseOne() {
        Boss b = boss();
        assertSame(phaseOne, b.getAttack());
        assertFalse(b.isInPhaseTwo());
    }

    @Test
    void bossStaysInPhaseOneAboveHalfHp() {
        Boss b = boss();
        b.takeDamage(39);                           // 41 kvar – mer än hälften
        assertSame(phaseOne, b.getAttack());
        assertFalse(b.isInPhaseTwo());
    }

    @Test
    void bossSwitchesToPhaseTwoAtHalfHp() {
        Boss b = boss();
        b.takeDamage(40);                           // exakt 40 kvar = halva livet
        assertSame(phaseTwo, b.getAttack());
        assertTrue(b.isInPhaseTwo());
    }

    @Test
    void bossSwitchesAfterSeveralSmallHits() {
        Boss b = boss();
        b.takeDamage(20);
        b.takeDamage(20);                           // 40 kvar efter två slag
        assertTrue(b.isInPhaseTwo());
    }

    @Test
    void bossSwitchesOnlyOnce() {
        Boss b = boss();
        b.takeDamage(40);                           // fas 2
        AttackStrategy other = (a, t) -> 1;
        b.setAttack(other);                         // någon byter attacken efteråt
        b.takeDamage(5);                            // ett nytt slag får inte byta tillbaka till fas 2-attacken
        assertSame(other, b.getAttack());
    }

    @Test
    void healingDoesNotGoBackToPhaseOne() {
        Boss b = boss();
        b.takeDamage(50);
        b.heal(50);                                 // full hp igen
        assertSame(phaseTwo, b.getAttack());        // men förklaringarna blir inte lättare
    }

    @Test
    void bossNeedsAPhaseTwoAttack() {
        assertThrows(IllegalArgumentException.class,
                () -> new Boss("Martin", 80, phaseOne, null, 5));
    }
}
