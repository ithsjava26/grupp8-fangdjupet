package se.fangdjupet.combat;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class BattleTest {

    // Lyssnare som inte gör något – loggningen testas senare
    private Battle battle() {
        return new Battle(message -> { });
    }

    // Lambdor som strategier: skadan blir alltid exakt samma, så striden blir förutsägbar
    private Hero hero(int maxHp, int damage) {
        return new Hero("Robin", maxHp, (a, t) -> damage);
    }

    private Enemy enemy(int maxHp, int damage) {
        return new Enemy("Troll", maxHp, (a, t) -> damage, 0);
    }

    // En attack som kostar anfallaren hp (som DoomScrolla). En lambda räcker inte här:
    // den kan bara ersätta den abstrakta metoden, inte skriva över selfDamage().
    private AttackStrategy costly(int damage, int cost) {
        return new AttackStrategy() {
            @Override
            public int calculateDamage(Character attacker, Character target) {
                return damage;
            }

            @Override
            public int selfDamage() {
                return cost;
            }
        };
    }

    // Lyssnare som sparar varje meddelande i listan – så kan testet läsa vad Battle loggade.
    // log::add är en metodreferens: "skicka varje meddelande till log.add".
    private Battle battleWithLog(List<String> log) {
        return new Battle(log::add);
    }

    @Test
    void listenerMustNotBeNull() {
        assertThrows(IllegalArgumentException.class, () -> new Battle(null));
    }

    @Test
    void strongerHeroWins() {
        Hero h = hero(20, 5);
        Enemy e = enemy(10, 3);
        assertSame(h, battle().fight(h, e));          // assertSame = exakt samma objekt
    }

    @Test
    void strongerEnemyWins() {
        Hero h = hero(10, 1);
        Enemy e = enemy(20, 5);
        assertSame(e, battle().fight(h, e));
    }

    @Test
    void winnerIsAliveAndLoserHasZeroHp() {
        Hero h = hero(20, 5);
        Enemy e = enemy(10, 3);
        battle().fight(h, e);
        assertTrue(h.isAlive());
        assertEquals(0, e.getHp());
    }

    @Test
    void heroAttacksFirst() {
        // Båda dödar med ett slag – den som slår först vinner
        Hero h = hero(10, 100);
        Enemy e = enemy(10, 100);
        assertSame(h, battle().fight(h, e));
        assertEquals(10, h.getHp());                  // fienden hann aldrig slå
    }

    @Test
    void deadEnemyDoesNotStrikeBack() {
        // Runda 1: troll 10 → 5, Robin 20 → 17. Runda 2: troll 5 → 0 → break.
        Hero h = hero(20, 5);
        Enemy e = enemy(10, 3);
        battle().fight(h, e);
        assertEquals(17, h.getHp());                  // 14 om det döda trollet slog en gång till
    }

    @Test
    void fightTakesExpectedNumberOfRounds() {
        // Räknar hur många gånger hjälten anfaller. int[] för att lambdan ska kunna ändra värdet.
        int[] heroAttacks = {0};
        Hero h = new Hero("Robin", 100, (a, t) -> {
            heroAttacks[0]++;
            return 3;
        });
        Enemy e = enemy(10, 1);
        battle().fight(h, e);
        assertEquals(4, heroAttacks[0]);              // 3 + 3 + 3 + 3 ≥ 10
    }

    @Test
    void heroThatDealsNoDamageLoses() {
        // Hjälten gör 0 men fienden gör skada – striden tar ändå slut
        Hero h = hero(10, 0);
        Enemy e = enemy(10, 2);
        assertSame(e, battle().fight(h, e));
        assertEquals(0, h.getHp());
        assertEquals(10, e.getHp());
    }

    // ── Attacker som kostar hp (selfDamage) ──

    @Test
    void heroCanDieFromOwnAttack() {
        // Robin har 1 hp och attacken kostar 1 hp. Trollet har 10 hp och gör ingen skada.
        Hero h = new Hero("Robin", 1, costly(1, 1));
        Enemy e = enemy(10, 0);
        assertSame(e, battle().fight(h, e));          // trollet vinner utan att slå ett enda slag
        assertEquals(0, h.getHp());
    }

    @Test
    void heroWinsEvenIfOwnAttackKillsBothAtOnce() {
        // Slaget dödar trollet (100 skada) och kostar Robin sitt sista hp. Fienden dog, så hjälten vinner.
        Hero h = new Hero("Robin", 1, costly(100, 1));
        Enemy e = enemy(10, 5);
        assertSame(h, battle().fight(h, e));
        assertEquals(0, h.getHp());
        assertEquals(0, e.getHp());
    }

    @Test
    void selfDamageIsDeductedEveryRound() {
        // Tre slag à 4 skada dödar trollet (10 hp). Varje slag kostar 1 hp: 20 − 3 = 17.
        Hero h = new Hero("Robin", 20, costly(4, 1));
        battle().fight(h, enemy(10, 0));
        assertEquals(17, h.getHp());
    }

    @Test
    void enemyCanDieFromOwnAttack() {
        // Trollet har 1 hp och slår 5, men slaget kostar det 1 hp. Robin gör ingen skada alls.
        Hero h = hero(10, 0);
        Enemy e = new Enemy("Troll", 1, costly(5, 1), 0);
        assertSame(h, battle().fight(h, e));
        assertEquals(0, e.getHp());                   // trollet dog av sitt eget slag
        assertEquals(5, h.getHp());                   // men hann slå en gång: 10 − 5
    }

    @Test
    void enemyWinsEvenIfOwnAttackKillsBothAtOnce() {
        // Spegelbilden av hjältens fall: slaget dödar Robin (100 skada) och kostar trollet sitt sista hp.
        // Hjälten dog av trollets slag, så trollet vinner.
        Hero h = hero(10, 0);
        Enemy e = new Enemy("Troll", 1, costly(100, 1), 0);
        assertSame(e, battle().fight(h, e));
        assertEquals(0, h.getHp());
        assertEquals(0, e.getHp());
    }

    @Test
    void enemySelfDamageIsDeductedEveryRound() {
        // Trollet har 6 hp och gör ingen skada, men varje slag kostar 2 hp: dör efter 3 slag.
        Hero h = hero(1000, 0);
        Enemy e = new Enemy("Troll", 6, costly(0, 2), 0);
        assertSame(h, battle().fight(h, e));
        assertEquals(0, e.getHp());
    }

    // ── Loggen till GameEventListener ──

    @Test
    void heroAttackIsLogged() {
        List<String> log = new ArrayList<>();
        Hero h = hero(20, 5);
        Enemy e = enemy(10, 3);
        battleWithLog(log).fight(h, e);
        assertFalse(log.isEmpty());                   // minst ett meddelande
        String first = log.get(0);                    // hjälten slår först
        assertTrue(first.contains("Robin"));          // vem slår
        assertTrue(first.contains("Troll"));          // vem blir slagen
        assertTrue(first.contains("5"));              // hur mycket skada
    }

    // Slumpen som aldrig ger miss (RangedAttack missar när talet är under 20), så loggen blir förutsägbar
    private Random neverMisses() {
        return new Random() {
            @Override
            public int nextInt(int bound) {
                return 99;
            }
        };
    }

    // Första loggraden från en strid där hjälten använder strategin mot ett troll som inte gör skada
    private String firstLogLine(AttackStrategy strategy) {
        List<String> log = new ArrayList<>();
        Hero h = new Hero("Robin", 20, strategy);
        battleWithLog(log).fight(h, enemy(10, 0));
        return log.get(0);
    }

    @Test
    void meleeAttackIsLoggedAsHit() {
        assertTrue(firstLogLine(new MeleeAttack(5)).contains("slår"));
    }

    @Test
    void rangedAttackIsLoggedAsShot() {
        String line = firstLogLine(new RangedAttack(5, neverMisses()));
        assertTrue(line.contains("skjuter"));
        assertFalse(line.contains("slår"));
    }

    @Test
    void magicAttackIsLoggedAsShot() {
        String line = firstLogLine(new MagicAttack(5));
        assertTrue(line.contains("skjuter"));
        assertFalse(line.contains("slår"));
    }

    @Test
    void enemyAttackIsLogged() {
        // Robin slår 5, trollet slår 3. Fiendens slag är raden som säger "skada: 3" – var i loggen den
        // står spelar ingen roll, så testet tål att hp-rader läggs in mellan slagen.
        List<String> log = new ArrayList<>();
        Hero h = hero(20, 5);
        Enemy e = enemy(10, 3);
        battleWithLog(log).fight(h, e);
        String line = log.stream().filter(l -> l.contains("skada: 3")).findFirst().orElseThrow();
        assertTrue(line.contains("Troll"));           // vem slår
        assertTrue(line.contains("Robin"));           // vem blir slagen
    }

    // ── HP-raden: efter varje slag visas den träffades liv som ASCII-mätare (HpBar.status, bredd 10) ──

    private boolean logHas(List<String> log, String text) {
        return log.stream().anyMatch(line -> line.contains(text));
    }

    @Test
    void enemyHpBarIsLoggedAfterHeroHit() {
        // Robin slår 4 per runda, trollet har 10 hp: 6 kvar efter första slaget (6/10 = 6 tecken #)
        List<String> log = logOf(hero(20, 4), enemy(10, 3));
        assertTrue(logHas(log, "Troll  [######----] 6/10"));
    }

    @Test
    void heroHpBarIsLoggedAfterEnemyHit() {
        // Trollet slår 3 per runda, Robin har 20 hp: 17 kvar efter första slaget (17 * 10 / 20 = 8 tecken #)
        List<String> log = logOf(hero(20, 4), enemy(10, 3));
        assertTrue(logHas(log, "Robin  [########--] 17/20"));
    }

    @Test
    void deadEnemyShowsEmptyBar() {
        // Tredje slaget dödar trollet: 0 hp ger en tom mätare
        List<String> log = logOf(hero(20, 4), enemy(10, 3));
        assertTrue(logHas(log, "Troll  [----------] 0/10"));
    }

    @Test
    void deadHeroShowsEmptyBar() {
        // Trollet slår 5 per runda mot Robins 10 hp: dör i runda 2. Den sista mätaren ska visa 0/10.
        List<String> log = logOf(hero(10, 1), enemy(20, 5));
        assertTrue(logHas(log, "Robin  [----------] 0/10"));
    }

    @Test
    void enemyBarIsLoggedAfterOwnSelfDamage() {
        // Trollet har 6 hp, gör ingen skada och förlorar 2 hp per slag: 4, 2, 0. Sista mätaren ska vara tom.
        List<String> log = logOf(hero(1000, 0), new Enemy("Troll", 6, costly(0, 2), 0));
        assertTrue(logHas(log, "Troll  [----------] 0/6"));
    }

    @Test
    void heroBarIsLoggedAfterOwnSelfDamage() {
        // Robins attack kostar 1 hp per slag: första mätaren efter egen skada är 19/20 (19 * 10 / 20 = 9 tecken #)
        List<String> log = logOf(new Hero("Robin", 20, costly(4, 1)), enemy(10, 0));
        assertTrue(logHas(log, "Robin  [#########-] 19/20"));
    }

    @Test
    void heroBarIsNotRepeatedWhenHeroTakesNoSelfDamage() {
        // Robin: vanlig attack utan kostnad. Fienden hinner slå 2 gånger (runda 1 och 2), dör i runda 3.
        // Mätaren för Robin ska alltså visas exakt 2 gånger, bara efter fiendens slag.
        List<String> log = logOf(hero(20, 4), enemy(10, 3));
        long heroBars = log.stream().filter(line -> line.startsWith("Robin  [")).count();
        assertEquals(2, heroBars);
    }

    @Test
    void enemyBarIsNotRepeatedWhenEnemyTakesNoSelfDamage() {
        // Trollet har en vanlig attack utan kostnad. Robin slår 3 gånger (runda 1–3), så trollets mätare
        // visas exakt 3 gånger, bara efter hjältens slag.
        List<String> log = logOf(hero(20, 4), enemy(10, 3));
        long enemyBars = log.stream().filter(line -> line.startsWith("Troll  [")).count();
        assertEquals(3, enemyBars);
    }

    // ── Slutmeddelandet: sista raden i loggen säger vem som vann (och innehåller ordet "vinner") ──

    // Kör en strid och ger hela loggen
    private List<String> logOf(Hero h, Enemy e) {
        List<String> log = new ArrayList<>();
        battleWithLog(log).fight(h, e);
        return log;
    }

    // Hur många loggrader som innehåller "vinner"
    private long announcements(List<String> log) {
        return log.stream().filter(line -> line.contains("vinner")).count();
    }

    @Test
    void heroVictoryIsAnnounced() {
        List<String> log = logOf(hero(20, 5), enemy(10, 3));
        String last = log.get(log.size() - 1);
        assertTrue(last.contains("vinner"));
        assertTrue(last.contains("Robin"));
    }

    @Test
    void enemyVictoryIsAnnounced() {
        // Här dör hjälten av fiendens slag – fight() lämnar loopen med en tidig return
        List<String> log = logOf(hero(10, 1), enemy(20, 5));
        String last = log.get(log.size() - 1);
        assertTrue(last.contains("vinner"));
        assertTrue(last.contains("Troll"));
    }

    @Test
    void heroVictoryNamesTheEnemy() {
        // En vanlig strid vinns mot fienden, inte hela Fångdjupet
        List<String> log = logOf(hero(20, 5), enemy(10, 3));
        assertEquals("Robin vinner mot Troll!", log.get(log.size() - 1));
    }

    @Test
    void enemyVictoryNamesTheHero() {
        List<String> log = logOf(hero(10, 1), enemy(20, 5));
        assertEquals("Troll vinner mot Robin!", log.get(log.size() - 1));
    }

    @Test
    void ordinaryFightDoesNotWinFangdjupet() {
        List<String> log = logOf(hero(20, 5), enemy(10, 3));
        assertTrue(log.stream().noneMatch(line -> line.contains("Fångdjupet")));
    }

    @Test
    void roundLimitWinnerIsAnnounced() {
        // Ingen gör skada, så striden slutar på rundtaket. Robin har mest hp.
        List<String> log = logOf(hero(20, 0), enemy(10, 0));
        String last = log.get(log.size() - 1);
        assertTrue(last.contains("vinner"));
        assertTrue(last.contains("Robin"));
    }

    @Test
    void victoryIsAnnouncedExactlyOnce() {
        // En gång när hjälten vinner, och en gång när fienden vinner (tidig return i loopen)
        assertEquals(1, announcements(logOf(hero(20, 5), enemy(10, 3))));
        assertEquals(1, announcements(logOf(hero(10, 1), enemy(20, 5))));
    }

    // ── Attackval varje runda: Battle frågar en "attackväljare" innan hjälten slår ──

    @Test
    void chooserIsAskedEveryRound() {
        // Robin slår 4 mot trollets 10 hp: runda 1, 2 och 3 (då trollet dör)
        int[] asked = {0};
        Battle b = new Battle(message -> { }, h -> {
            asked[0]++;
            return null;                                  // null = behåll nuvarande attack
        });
        b.fight(hero(20, 4), enemy(10, 0));
        assertEquals(3, asked[0]);
    }

    @Test
    void chosenAttackIsUsed() {
        // Hjältens egen attack gör 0, men den valda gör 10 – trollet dör på första slaget
        Hero h = hero(20, 0);
        Enemy e = enemy(10, 0);
        Battle b = new Battle(message -> { }, hero -> (a, t) -> 10);
        assertSame(h, b.fight(h, e));
        assertEquals(0, e.getHp());
    }

    @Test
    void attackCanChangeFromRoundToRound() {
        // Runda 1 väljs en attack som gör 1, därefter en som gör 100 – trollet (50 hp) dör i runda 2
        int[] round = {0};
        List<String> log = new ArrayList<>();
        Battle b = new Battle(log::add, hero -> {
            round[0]++;
            int damage = round[0] == 1 ? 1 : 100;
            return (a, t) -> damage;
        });
        Enemy e = enemy(50, 0);
        b.fight(hero(20, 0), e);
        assertEquals(2, round[0]);
        assertEquals(0, e.getHp());
    }

    @Test
    void nullChoiceKeepsCurrentAttack() {
        Hero h = hero(20, 5);
        AttackStrategy before = h.getAttack();
        Battle b = new Battle(message -> { }, hero -> null);
        b.fight(h, enemy(10, 0));
        assertSame(before, h.getAttack());                // rörde inte attacken
    }

    @Test
    void chosenAttackStaysOnTheHero() {
        Hero h = hero(20, 0);
        AttackStrategy chosen = (a, t) -> 10;
        new Battle(message -> { }, hero -> chosen).fight(h, enemy(10, 0));
        assertSame(chosen, h.getAttack());                // den valda attacken är hjältens nya attack
    }

    @Test
    void chosenAttackSelfDamageCountsToo() {
        // Den valda attacken kostar 1 hp, trots att hjältens egen attack inte gör det
        Hero h = hero(20, 0);
        new Battle(message -> { }, hero -> costly(100, 1)).fight(h, enemy(10, 0));
        assertEquals(19, h.getHp());
    }

    // ── Rundtaket: ingen av dem gör skada, så båda lever när taket (100 rundor) nås ──

    @Test
    void fightStopsAtRoundLimit() {
        int[] heroAttacks = {0};
        Hero h = new Hero("Robin", 10, (a, t) -> {
            heroAttacks[0]++;
            return 0;
        });
        battle().fight(h, enemy(10, 0));
        assertEquals(100, heroAttacks[0]);            // MAX_ROUNDS, inte en runda mer
    }

    @Test
    void atRoundLimitHeroWinsWithMoreHp() {
        Hero h = hero(20, 0);
        Enemy e = enemy(10, 0);
        assertSame(h, battle().fight(h, e));
    }

    @Test
    void atRoundLimitEnemyWinsWithMoreHp() {
        Hero h = hero(10, 0);
        Enemy e = enemy(20, 0);
        assertSame(e, battle().fight(h, e));
    }

    @Test
    void atRoundLimitEnemyWinsOnTie() {
        Hero h = hero(10, 0);
        Enemy e = enemy(10, 0);
        assertSame(e, battle().fight(h, e));          // lika hp ⇒ fienden
    }

    @Test
    void woundedHeroKeepsHpBetweenFights() {
        // Hp återställs inte efter striden – nästa strid börjar med det som är kvar
        Hero h = hero(20, 5);
        battle().fight(h, enemy(10, 3));              // Robin 20 → 17
        battle().fight(h, enemy(10, 3));              // Robin 17 → 14
        assertEquals(14, h.getHp());
    }

    @Test
    void worksWithRealStrategies() {
        // Riktiga strategier i stället för lambdor: kunskap mot närstrid
        Hero h = new Hero("Robin", 30, KnowledgeAttack.QUIZ);         // 7 skada, ignorerar försvar
        Enemy e = new Enemy("Troll", 20, new MeleeAttack(2), 2);      // 2 + 2 − 0 = 4 skada
        // Robin: 30 / 4 → behöver 8 slag på sig. Trollet: 20 / 7 → dör på 3:e slaget.
        assertSame(h, battle().fight(h, e));
        assertEquals(22, h.getHp());                  // trollet hann slå 2 gånger: 30 − 8
    }

    // ── Flera fiender samtidigt (trollet och trollkarlen i kafferummet) ──

    private Enemy named(String name, int maxHp, int damage) {
        return new Enemy(name, maxHp, (a, t) -> damage, 0);
    }

    @Test
    void heroCanBeatTwoEnemiesAtOnce() {
        Hero h = hero(40, 10);
        Enemy troll = named("Troll", 12, 2);
        Enemy wizard = named("Trollkarl", 20, 3);
        assertSame(h, battle().fight(h, List.of(troll, wizard)));
        assertFalse(troll.isAlive());
        assertFalse(wizard.isAlive());
    }

    @Test
    void heroHitsTheFirstLivingEnemyFirst() {
        List<String> log = new ArrayList<>();
        battleWithLog(log).fight(hero(40, 10), List.of(named("Troll", 12, 0), named("Trollkarl", 20, 0)));
        List<String> heroHits = log.stream().filter(l -> l.startsWith("Hero:")).toList();
        assertTrue(heroHits.get(0).contains("Troll skada"));      // runda 1 och 2: trollet
        assertTrue(heroHits.get(1).contains("Troll skada"));
        assertTrue(heroHits.get(2).contains("Trollkarl"));        // sedan trollkarlen
    }

    @Test
    void everyLivingEnemyAttacksEachRound() {
        // Runda 1: hjälten dödar trollet, bara trollkarlen slår (3). Runda 2: trollkarlen dör.
        Hero h = hero(10, 100);
        battle().fight(h, List.of(named("Troll", 12, 2), named("Trollkarl", 20, 3)));
        assertEquals(7, h.getHp());
    }

    @Test
    void bothEnemiesHitInTheSameRound() {
        List<String> log = new ArrayList<>();
        battleWithLog(log).fight(hero(40, 1), List.of(named("Troll", 50, 2), named("Trollkarl", 50, 3)));
        // Rad 0: hjältens slag, rad 1: trollets mätare – sedan slår trollet och därefter trollkarlen
        assertTrue(log.get(2).startsWith("Enemy: Troll "));
        assertTrue(log.get(4).startsWith("Enemy: Trollkarl "));
        assertTrue(log.get(6).startsWith("Hero:"));                // först därefter runda 2
    }

    @Test
    void theEnemyThatKillsTheHeroWins() {
        // Runda 1: 10 − 2 − 3 = 5. Runda 2: trollet tar 2 (3 kvar), trollkarlen tar sista 3.
        Hero h = hero(10, 1);
        Enemy troll = named("Troll", 50, 2);
        Enemy wizard = named("Trollkarl", 50, 3);
        assertSame(wizard, battle().fight(h, List.of(troll, wizard)));
    }

    @Test
    void victoryOverTwoEnemiesNamesBoth() {
        List<String> log = new ArrayList<>();
        battleWithLog(log).fight(hero(40, 10), List.of(named("Troll", 12, 0), named("Trollkarl", 20, 0)));
        assertEquals("Robin vinner mot Troll och Trollkarl!", log.get(log.size() - 1));
    }

    @Test
    void enemyVictoryInGroupFightNamesTheHero() {
        List<String> log = new ArrayList<>();
        battleWithLog(log).fight(hero(10, 1), List.of(named("Troll", 50, 2), named("Trollkarl", 50, 3)));
        assertEquals("Trollkarl vinner mot Robin!", log.get(log.size() - 1));
    }

    @Test
    void groupFightNeedsAtLeastOneEnemy() {
        assertThrows(IllegalArgumentException.class, () -> battle().fight(hero(10, 1), List.of()));
    }

    @Test
    void oneEnemyInAListIsAnOrdinaryFight() {
        Hero h = hero(20, 5);
        Enemy e = enemy(10, 3);
        assertSame(h, battle().fight(h, List.of(e)));
        assertEquals(17, h.getHp());                     // trollet hann slå en gång innan det dog
    }

    // ── Slumpen: miss ibland och ±1 i skada (bara när Battle får en Random) ──

    // En Random som svarar med talen i tur och ordning, om och om igen.
    // Battle frågar först nextInt(100) – under 10 = miss – och sedan nextInt(3) för -1, 0 eller +1.
    private Random rolls(int... values) {
        return new Random() {
            private int next;

            @Override
            public int nextInt(int bound) {
                int value = values[next % values.length];
                next++;
                return Math.min(value, bound - 1);
            }
        };
    }

    private Battle luckyBattle(List<String> log, Random random) {
        return new Battle(log::add, hero -> null, random);
    }

    @Test
    void aMissDealsNoDamage() {
        List<String> log = new ArrayList<>();
        Enemy e = enemy(10, 0);
        luckyBattle(log, rolls(0)).fight(hero(20, 5), e);          // 0 < 10: alla slag missar
        assertEquals(10, e.getHp());
        assertTrue(log.contains("Hero: Robin missar Troll!"));
    }

    @Test
    void theEnemyCanMissToo() {
        List<String> log = new ArrayList<>();
        Hero h = hero(20, 5);
        luckyBattle(log, rolls(0)).fight(h, enemy(10, 3));
        assertEquals(20, h.getHp());
        assertTrue(log.contains("Enemy: Troll missar Robin!"));
    }

    @Test
    void aHitCanBeOneMore() {
        // 99 = träff, 2 = +1: hjälten gör 6 i stället för 5 – trollet (12 hp) dör på två slag
        List<String> log = new ArrayList<>();
        luckyBattle(log, rolls(99, 2)).fight(hero(20, 5), enemy(12, 1));   // fienden gör 1, så den drar också sina tal ur Random
        assertEquals(2, log.stream().filter(l -> l.startsWith("Hero:")).count());
        assertTrue(log.contains("Hero: Robin slår Troll skada: 6"));
    }

    @Test
    void aHitCanBeOneLess() {
        // 99 = träff, 0 = -1: hjälten gör 4 – trollet (12 hp) behöver tre slag
        List<String> log = new ArrayList<>();
        luckyBattle(log, rolls(99, 0)).fight(hero(20, 5), enemy(12, 1));   // fienden gör 1, så den drar också sina tal ur Random
        assertEquals(3, log.stream().filter(l -> l.startsWith("Hero:")).count());
    }

    @Test
    void damageIsNeverNegative() {
        Hero h = hero(20, 5);
        luckyBattle(new ArrayList<>(), rolls(99, 0)).fight(h, enemy(10, 0));   // fienden gör 0 – minus 1 blir ändå 0
        assertEquals(20, h.getHp());
    }

    @Test
    void withoutRandomTheFightIsExact() {
        List<String> log = new ArrayList<>();
        battleWithLog(log).fight(hero(20, 5), enemy(10, 1));
        assertTrue(log.contains("Hero: Robin slår Troll skada: 5"));
        assertTrue(log.stream().noneMatch(l -> l.contains("missar")));
    }

    @Test
    void randomMustNotBeNull() {
        assertThrows(IllegalArgumentException.class, () -> new Battle(message -> { }, hero -> null, null));
    }

    // ── Ingen skada = en miss, även när attacken själv missar (t.ex. ett bomkast) ──

    @Test
    void zeroDamageStaysZeroWithLuck() {
        // 99 = Battle missar inte, 2 = +1 – men 0 i skada ska inte bli 1
        Hero h = hero(20, 5);
        luckyBattle(new ArrayList<>(), rolls(99, 2)).fight(h, enemy(10, 0));
        assertEquals(20, h.getHp());
    }

    // ── Bilder för attackerna (withAttackArt) ──

    @Test
    void noAttackArtUnlessTurnedOn() {
        List<String> log = new ArrayList<>();
        new Battle(log::add, hero -> KnowledgeAttack.DOOMSCROLL).fight(hero(50, 5), enemy(10, 1));
        assertFalse(log.contains(AttackArt.DOOMSCROLL.stripTrailing()));
    }

    @Test
    void chosenAttackIsShownBeforeTheHit() {
        List<String> log = new ArrayList<>();
        new Battle(log::add, hero -> KnowledgeAttack.DOOMSCROLL).withAttackArt().fight(hero(50, 5), enemy(10, 1));
        int art = log.indexOf(AttackArt.DOOMSCROLL.stripTrailing());
        assertTrue(art >= 0, log.toString());
        assertTrue(log.get(art + 1).startsWith("Hero: Robin"), "bilden kommer före slaget");
    }

    @Test
    void enterKeepsAttackWithoutArt() {
        List<String> log = new ArrayList<>();
        new Battle(log::add, hero -> null).withAttackArt().fight(hero(50, 5), enemy(10, 1));
        assertTrue(log.stream().noneMatch(l -> l.contains("NARSTRID") || l.contains("DOOMSCROLLA")), log.toString());
    }

    @Test
    void ordinaryEnemyAttacksHaveNoArt() {
        List<String> log = new ArrayList<>();
        new Battle(log::add).withAttackArt()
                .fight(hero(50, 5), new Enemy("Troll", 10, new MeleeAttack(3), 1));
        assertFalse(log.contains(AttackArt.MELEE.stripTrailing()));
    }

    @Test
    void eachBossAttackIsShownOncePerFight() {
        // Martin: 40 hp, hjälten gör 5 per runda = 8 rundor. Fas 1 och fas 2 visas en gång var.
        List<String> log = new ArrayList<>();
        new Battle(log::add).withAttackArt().fight(hero(500, 5), Martin.create());
        assertEquals(1, log.stream().filter(AttackArt.MARTIN_EXPLAINS.stripTrailing()::equals).count());
        assertEquals(1, log.stream().filter(AttackArt.MARTIN_UTTERLY.stripTrailing()::equals).count());
        assertEquals(1, log.stream().filter(AttackArt.MARTIN_PERSISTENTLY.stripTrailing()::equals).count());
        assertEquals(1, log.stream().filter(AttackArt.MARTIN_ANGRILY.stripTrailing()::equals).count());
    }

    @Test
    void withAttackArtReturnsTheSameBattle() {
        Battle b = battle();
        assertSame(b, b.withAttackArt());
    }

    @Test
    void zeroDamageIsLoggedAsAMiss() {
        List<String> log = new ArrayList<>();
        battleWithLog(log).fight(hero(20, 5), enemy(10, 0));
        assertTrue(log.contains("Enemy: Troll slår Robin men missar!"), log.toString());
        assertTrue(log.stream().noneMatch(l -> l.contains("skada: 0")));
    }
}
