package se.fangdjupet.game;

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

    private void startNewAdventure() {
        output.onEvent("Starta nytt äventyr.");
        // Skapa hjälte och starta äventyret här.
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
        output.onEvent("Strid mot rummets fiende.");
        // Anropa stridslogiken här.
    }

    private void moveToAnotherRoom() {
        output.onEvent("Gå till ett annat rum.");
        // Flytta till nästa rum här.
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
}

