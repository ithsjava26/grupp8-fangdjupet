package se.fangdjupet.game;

import se.fangdjupet.combat.Enemy;

import java.util.List;

public class WorldBuilder {

    public World build(
            Enemy doorEnemy,
            Enemy corridorEnemy,
            Enemy coffeeEnemy,
            Enemy finalEnemy

    ) {
        Room door = new Room(
                "Skoldörren",
                "Du står utanför skolan.",
                doorEnemy,
                List.of()
        );

        Room coffeeRoom = new Room(
                "Kafferummet",
                "Det luktar kaffe.",
                coffeeEnemy,
                List.of()
        );

        Room corridor = new Room(
                "På väg till lektionssalen",
                "Korridoren känns lång.",
                corridorEnemy,
                List.of()
        );

        Room classroom = new Room(
                "Lektionssalen",
                "Du har nått fram. Framför dig väntar dagens stora test.",
                finalEnemy,
                List.of()
        );

        return new World(List.of(door, coffeeRoom, corridor, classroom));
    }
}