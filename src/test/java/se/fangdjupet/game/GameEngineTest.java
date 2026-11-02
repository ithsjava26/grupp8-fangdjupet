package se.fangdjupet.game;

import org.junit.jupiter.api.Test;
import se.fangdjupet.combat.Enemy;
import se.fangdjupet.combat.MeleeAttack;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GameEngineTest {

    private Enemy enemy(String name) {
        return new Enemy(name, 10, new MeleeAttack(3), 1);
    }

    // Kafferummet i liten skala: JavaFläcken först, sedan trollet och trollkarlen tillsammans
    private Room coffeeRoom(Enemy fleck, Enemy troll, Enemy wizard) {
        return new Room("Kafferummet", "Det luktar kaffe.", fleck, List.of()).thenTogether(troll, wizard);
    }

    @Test
    void nextWaveComesWhenTheWaveIsDefeated() {
        Enemy fleck = enemy("JavaFläcken");
        Room room = coffeeRoom(fleck, enemy("Troll"), enemy("Trollkarl"));
        fleck.takeDamage(100);
        assertTrue(GameEngine.nextWaveIsReady(List.of(fleck), room));
    }

    @Test
    void noNextWaveWhileTheFoughtWaveLives() {
        // Rundtaket: hjälten kan vinna på mest hp fast JavaFläcken lever – då ska inget kaffe delas ut
        Enemy fleck = enemy("JavaFläcken");
        Room room = coffeeRoom(fleck, enemy("Troll"), enemy("Trollkarl"));
        fleck.takeDamage(5);
        assertFalse(GameEngine.nextWaveIsReady(List.of(fleck), room));
    }

    @Test
    void noNextWaveWhenOneOfAGroupLives() {
        Enemy fleck = enemy("JavaFläcken");
        Enemy troll = enemy("Troll");
        Enemy wizard = enemy("Trollkarl");
        Room room = coffeeRoom(fleck, troll, wizard).thenTogether(enemy("Extra"));
        fleck.takeDamage(100);
        troll.takeDamage(100);                                // trollkarlen lever kvar
        assertFalse(GameEngine.nextWaveIsReady(List.of(troll, wizard), room));
    }

    @Test
    void noNextWaveWhenTheRoomIsCleared() {
        Enemy fleck = enemy("JavaFläcken");
        Enemy troll = enemy("Troll");
        Enemy wizard = enemy("Trollkarl");
        Room room = coffeeRoom(fleck, troll, wizard);
        fleck.takeDamage(100);
        troll.takeDamage(100);
        wizard.takeDamage(100);
        assertFalse(GameEngine.nextWaveIsReady(List.of(troll, wizard), room));
    }
}
