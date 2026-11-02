package se.fangdjupet.game;

import se.fangdjupet.combat.*;

import java.util.Random;


public class GameEngine {
    private final GameEventListener output;
    private boolean running;

    public GameEngine(GameEventListener output) {
        if (output == null) {
            throw new IllegalArgumentException("output must not be null");
        }
        this.output = output;
    }

    public void run() {
        running = true;

        while (running) {
            showMenu();
            String choice = IO.readln().trim().toLowerCase();
            handleChoice(choice);
        }
    }

    private void showMenu() {
        IO.println("""
                Fangdjupet
                ==========
                1. Starta nytt äventyr
                2. Fortsätt sparat äventyr
                3. Visa hjältens utrustning
                4. Strid mot rummets fiende
                5. Gå till ett annat rum
                6. Plocka upp föremål
                7. Prata med någon
                e. Avsluta
                """);
        IO.print("Välj ett alternativ: ");
    }

    private void handleChoice(String choice) {
        switch (choice) {
            case "1" -> startNewAdventure();
            case "2" -> continueAdventure();
            case "3" -> showEquipment();
            case "4" -> fightRoomEnemy();
            case "5" -> moveToAnotherRoom();
            case "6" -> pickUpItem();
            case "7" -> talkToSomeone();
            case "e" -> exitGame();
            default -> output.onEvent("Ogiltigt val. Försök igen.");
        }
    }

    private Hero hero;
    private World world;

    private void startNewAdventure() {
        output.onEvent("Starta nytt äventyr.");
        IO.print("Vad heter hjälten? ");
        String heroName = IO.readln().trim();

        hero = new Hero(heroName, 40, new MeleeAttack(10));

        Enemy doorEnemy = new Enemy("Dörrjäveln!", 20, new MeleeAttack(8), 1);
        Enemy corridorEnemy = new Enemy("StädHäxan", 30, new MeleeAttack(8), 2);
        Enemy coffeeEnemy = new Enemy("JavaFläcken", 25, new MeleeAttack(7), 1);
        Enemy finalEnemy = new Enemy("Kapten Martin", 40, new MeleeAttack(10), 3);

        world = new WorldBuilder().build(
                doorEnemy, corridorEnemy, coffeeEnemy, finalEnemy);

        output.onEvent("Äventyret börjar!");
        showCurrentRoom();
    }

    private void continueAdventure() {
        output.onEvent("Fortsätt sparat äventyr.");
        // Läs in sparfilen här.
    }

    private void showEquipment() {
        output.onEvent("Visa hjältens utrustning.");
        // Skriv ut hjältens föremål här.
    }

    private void fightRoomEnemy() {
        if (hero == null || world == null) {
            output.onEvent("Starta ett äventyr först.");
            return;
        }

        Enemy enemy = world.getCurrentRoom().getEnemy();

        if (enemy == null || !enemy.isAlive()) {
            output.onEvent("Det finns ingen fiende att slåss mot.");
            return;
        }

        output.onEvent("Fiende " + enemy.getName() + " dyker upp!");

        while (hero.isAlive() && enemy.isAlive()) {
            AttackStrategy attack = chooseAttack();
            int damage = attack.calculateDamage(hero, enemy);
            enemy.takeDamage(damage);

            if (damage == 0) {
                output.onEvent("Attacken träffade inte eller gjorde ingen skada.");
            } else {
                output.onEvent(hero.getName() + " gör " + damage
                        + " i skada på " + enemy.getName());
            }

            if (!enemy.isAlive()) {
                output.onEvent("Hjälten besegrade " + enemy.getName() + ".");
                break;
            }

            damage = enemy.attack(hero);
            hero.takeDamage(damage);
            output.onEvent(enemy.getName() + " gör " + damage
                    + " i skada på " + hero.getName()
                    + " (" + hero.getHp() + "/" + hero.getMaxHp() + ")");
        }

        if (!hero.isAlive()) {
            output.onEvent("Hjälten har blivit besegrad.");
        }
    }

    private void moveToAnotherRoom() {
        if (world == null) {
            output.onEvent("Starta ett äventyr först.");
            return;
        }

        if (world.moveForward()) {
            showCurrentRoom();
        } else {
            output.onEvent("Det finns inga fler rum.");
        }
    }

    private void pickUpItem() {
        output.onEvent("Plocka upp föremål.");
        // Lägg rummets föremål i inventariet här.
    }

    private void talkToSomeone() {
        output.onEvent("Prata med någon.");
        // Visa dialog här.
    }

    private void exitGame() {
        output.onEvent("Tack för att du spelade!");
        running = false;
    }

    private void showCurrentRoom() {
        Room room = world.getCurrentRoom();
        output.onEvent(room.getName());
        output.onEvent(room.getDescription());
    }

    private AttackStrategy chooseAttack() {
        IO.println("Välj attack:");
        IO.println("1. Närstrid");
        IO.println("2. Magi");
        IO.println("3. Avståndsattack");
        IO.print("Ditt val: ");

        String choice = IO.readln().trim();

        return switch (choice) {
            case "2" -> new MagicAttack(6);
            case "3" -> new RangedAttack(5, new Random());
            default -> new MeleeAttack(10);
        };
}

}




