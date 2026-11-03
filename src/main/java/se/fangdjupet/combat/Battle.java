package se.fangdjupet.combat;

import se.fangdjupet.game.GameEventListener;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

// En strid mellan hjälten och en eller flera fiender. fight(...) kör runda efter runda tills
// någon sida är död, och returnerar vinnaren. Varje slag skickas som text till listenern
// (Observer) – Battle skriver aldrig själv till konsolen. Attackerna är strategier (Strategy),
// så Battle bryr sig inte om hur skadan räknas, bara att den räknas.
public class Battle {
    private static final int MAX_ROUNDS = 100;    // rundtak: en strid får aldrig fastna

    private final GameEventListener listener;     // dit loggen skickas, t.ex. konsolen eller ett test

    private final AttackChooser chooser;   // frågas före varje hjältens slag
    private final Random random;           // null = ingen slump: skadan blir exakt (testerna)
    private boolean attackArt;             // visa AttackArt-bilder? Av som standard, så loggen blir kort i testerna

    public Battle(GameEventListener listener) {
        this(listener, hero -> null);      // utan val: hjälten behåller sin attack
    }

    // Spelaren väljer attack varje runda, men utan slump – skadan blir exakt (så kör testerna)
    public Battle(GameEventListener listener, AttackChooser chooser) {
        this(listener, chooser, false, null);   // utan slump
    }

    // Med slump: ibland en miss, och ±1 i skada. Så kör GameEngine striderna.
    public Battle(GameEventListener listener, AttackChooser chooser, Random random) {
        this(listener, chooser, true, random);
    }

    // Alla konstruktorer hamnar här. withLuck = true kräver en Random.
    private Battle(GameEventListener listener, AttackChooser chooser, boolean withLuck, Random random) {
        if (listener == null)
            throw new IllegalArgumentException("listener cannot be null"); //Samma princip som character
        if (chooser == null)
            throw new IllegalArgumentException("chooser cannot be null");
        if (withLuck && random == null)
            throw new IllegalArgumentException("random cannot be null");
        this.listener = listener;
        this.chooser = chooser;
        this.random = random;
    }

    // Slår på bilderna: när spelaren väljer en attack (DoomScrolla, StackOverflow ...) och första gången
    // en boss använder en attack (Svinto, Martins förklaringar ...). Returnerar striden, så att man kan
    // kedja: new Battle(output, chooser, random).withAttackArt().fight(hero, enemies)
    public Battle withAttackArt() {
        this.attackArt = true;
        return this;
    }

    // Kör hela striden, berättar vem som vann och returnerar vinnaren
    public Character fight(Hero hero, Enemy enemy) {
        return fight(hero, List.of(enemy));                // en vanlig strid = en grupp med en fiende
    }

    // Strid mot flera fiender samtidigt, t.ex. trollet och trollkarlen i kafferummet
    public Character fight(Hero hero, List<Enemy> enemies) {
        if (enemies == null || enemies.isEmpty())
            throw new IllegalArgumentException("enemies cannot be empty");
        Character result = runFight(hero, enemies);                        // själva striden
        String losers = (result == hero)
                ? enemies.stream().map(Character::getName).collect(Collectors.joining(" och "))
                : hero.getName();
        listener.onEvent(result.getName() + " vinner mot " + losers + "!");   // slutmeddelandet, loggas bara en gång
        return result;
    }

    // En runda = hjälten slår den första fienden som lever, sedan slår varje levande fiende.
    // Går tills hjälten eller alla fiender är döda, eller rundtaket nås.
    private Character runFight(Hero hero, List<Enemy> enemies) {
        int round = 0;
        Set<String> bossAttacksShown = new HashSet<>();   // varje bossattack visas med bild bara en gång per strid
        // Så länge hjälten och minst en fiende lever och taket inte nåtts – slåss
        while (hero.isAlive() && anyAlive(enemies) && round < MAX_ROUNDS) {
            round++;
            int squeezed = hero.squeezeStressBall();        // stressbollen i fickan: +6 hp varje runda
            if (squeezed > 0) {
                listener.onEvent(hero.getName() + " klämmer stressbollen: +" + squeezed + " hp");
                listener.onEvent(HpBar.status(hero, 10));
            }
            AttackStrategy chosen = chooser.choose(hero);   // spelaren väljer attack för den här rundan
            if (chosen != null) {
                hero.setAttack(chosen);
                showArt(chosen);                            // bilden för det spelaren valde, t.ex. DoomScrolla
            }
            Enemy target = firstAlive(enemies);            // hjälten slår den första fienden som lever
            AttackStrategy attack = hero.getAttack();
            int damage = 0;
            if (attack.selfHeal() > 0) {
                // T.ex. Dela skärm: hjälten tar över lektionen och får tid – hp i stället för skada, missar aldrig
                int before = hero.getHp();
                hero.heal(attack.selfHeal());
                listener.onEvent("Hero: " + hero.getName() + " " + attack.verb() + ": +" + (hero.getHp() - before)
                        + " hp, ingen skada på " + target.getName());
                listener.onEvent(HpBar.status(hero, 10));
            } else {
                boolean heroMisses = misses(attack.missPercent());   // slumpen: ibland missar man (Java-boken oftare)
                // bara skadan räknas ut här, hp ändras inte än. USB-minnet (instantWin) varieras inte: allt hp, exakt
                damage = heroMisses ? 0 : attack.instantWin() ? hero.attack(target) : vary(hero.attack(target));
                if (heroMisses) {
                    listener.onEvent("Hero: " + hero.getName() + " " + attack.missVerb() + " " + target.getName() + "!");
                } else {
                    listener.onEvent("Hero: " + hero.getName() + " " + hit(attack, target, damage));
                }
                target.takeDamage(damage);                          // nu dras skadan av fienden
                listener.onEvent(HpBar.status(target, 10));         // grafik: hp-mätare för fienden
                attack.used(hero);                                  // t.ex. USB-minnet: ett försök, sedan är det borta
            }
            hero.takeDamage(hero.getAttack().selfDamage());   // attacken kan kosta hjälten hp
            if(hero.getAttack().selfDamage() > 0) {
                listener.onEvent(HpBar.status(hero, 10));// grafik: hp-mätare för hjälten, 10 tecken bred
            }
            if (!anyAlive(enemies) || !hero.isAlive()) break;   // ingen av de döda slår mer
            for (Enemy enemy : enemies) {                     // fiendernas tur, en i taget
                if (!enemy.isAlive()) continue;                // döda fiender slår inte
                enemy.nextTaunt().ifPresent(taunt -> listener.onEvent(enemy.getName() + ": \"" + taunt + "\""));   // t.ex. Martins Java-fråga
                boolean enemyMisses = misses(enemy.getAttack().missPercent());
                damage = enemyMisses ? 0 : vary(enemy.attack(hero));   // fiendens tur: skadan räknas ut
                // Efter attack(): då vet RandomAttack vilket kast det blev (svamp eller Svinto)
                if (enemy instanceof Boss && bossAttacksShown.add(enemy.getAttack().name())) {
                    showArt(enemy.getAttack());
                }
                if (enemyMisses) {
                    listener.onEvent("Enemy: " + enemy.getName() + " missar " + hero.getName() + "!");
                } else {
                    listener.onEvent("Enemy: " + enemy.getName() + " " + hit(enemy.getAttack(), hero, damage));
                }
                hero.takeDamage(damage);                                     // hjälten tar skadan
                listener.onEvent(HpBar.status(hero, 10));          // grafik: hp-mätare för hjälten, 10 tecken bred
                enemy.takeDamage(enemy.getAttack().selfDamage());            // fiendens attack kan kosta fienden hp
                if (enemy.getAttack().selfDamage() > 0) {                    // mätaren bara om attacken kostade något
                    listener.onEvent(HpBar.status(enemy, 10));
                }
                if(!hero.isAlive()) return enemy;                            // hjälten dog: den fienden vinner direkt
            }
        }
        return winner(hero, enemies);   // fienderna dog, eller rundtaket nåddes: winner() avgör
    }

    // Alla fiender döda: hjälten vinner, även om hen dog av sin egen attack i samma slag (valet B).
    // Hjälten dog: den första levande fienden vinner. Rundtaket nåddes: hjälten vinner bara med
    // mer hp än fienderna har tillsammans – vid lika vinner fienden, hjälten hann inte besegra den.
    private Character winner(Hero hero, List<Enemy> enemies) {
        if (!anyAlive(enemies)) return hero;
        if (!hero.isAlive()) return firstAlive(enemies);
        int enemyHp = enemies.stream().mapToInt(Character::getHp).sum();
        return hero.getHp() > enemyHp ? hero : firstAlive(enemies);
    }

    // Skickar attackens bild till loggen, utan text blockets sista radbrytning – om bilderna är på
    private void showArt(AttackStrategy attack) {
        if (!attackArt) return;
        AttackArt.forAttack(attack).ifPresent(art -> listener.onEvent(art.stripTrailing()));
    }

    // Slumpen (bara med Random): percent % av slagen missar – oftast 10 % (AttackStrategy.USUAL_MISS_PERCENT)
    private boolean misses(int percent) {
        return random != null && random.nextInt(100) < percent;
    }

    // Slagets rad i loggen. 0 i skada (bomkast eller för bra försvar) skrivs som en miss.
    private static String hit(AttackStrategy attack, Character target, int damage) {
        if (damage == 0) {
            return attack.verb() + " " + target.getName() + " men missar!";
        }
        return attack.verb() + " " + target.getName() + " skada: " + damage;
    }

    // Slumpen (bara med Random): skadan blir 1 lägre, samma eller 1 högre – aldrig under 0.
    // En miss (0) förblir en miss.
    private int vary(int damage) {
        if (random == null || damage == 0) return damage;
        return Math.max(0, damage + random.nextInt(3) - 1);
    }

    // Lever minst en av fienderna?
    private static boolean anyAlive(List<Enemy> enemies) {
        return enemies.stream().anyMatch(Character::isAlive);
    }

    // Hjälten slår alltid den första fienden som lever
    private static Enemy firstAlive(List<Enemy> enemies) {
        return enemies.stream().filter(Character::isAlive).findFirst().orElseThrow();
    }
}
