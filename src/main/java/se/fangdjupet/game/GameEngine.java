package se.fangdjupet.game;

import se.fangdjupet.combat.*;

import se.fangdjupet.items.Item;
import se.fangdjupet.items.Potion;
import se.fangdjupet.items.Weapon;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;


// Spelets huvudloop (Fredrik): menyn, rummen och vad varje val gör.
// Striderna lämnas över till Battle, och allt som skrivs ut går via output (Observer).
public class GameEngine {
    private final GameEventListener output;
    private final Random random = new Random();   // slumpen i striderna
    private boolean running;

    // output = dit allt skrivs, i spelet en EventBus med konsolen
    public GameEngine(GameEventListener output) {
        if (output == null) {
            throw new IllegalArgumentException("output must not be null");
        }
        this.output = output;
    }

    // Visar menyn och kör valen tills spelaren väljer e
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

    // Ett menyval → rätt metod
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

    // Menyval 1: skapar hjälten, fienderna och rummen och visar det första rummet
    private void startNewAdventure() {
        output.onEvent("Starta nytt äventyr.");
        IO.print("Vad heter hjälten? ");
        String heroName = IO.readln().trim();

        hero = new Hero(heroName, 40, new MeleeAttack(10));

        Enemy doorEnemy = new Enemy("Entrédörren", 15, new MeleeAttack(5), 1);   // liten fiende
        Enemy corridorEnemy = CleaningWitch.create(random);   // kastar tvättsvamp och Svinto, sedan sprayar hon kemikalier
        Enemy coffeeEnemy = new Enemy("JavaFläcken", 15, new MeleeAttack(4), 1);   // liten fiende
        Enemy finalEnemy = Martin.create();   // bossen: byter till tyngre förklaringar vid halva livet

        world = new WorldBuilder().build(
                doorEnemy, corridorEnemy, coffeeEnemy, finalEnemy);

        output.onEvent("Äventyret börjar!");
        showCurrentRoom();
    }

    // Menyval 2: inte klar – ska läsa in sparfilen
    private void continueAdventure() {
        output.onEvent("Fortsätt sparat äventyr.");
        // Läs in sparfilen här.
    }

    // Menyval 3: inte klar – ska visa hjältens föremål
    private void showEquipment() {
        output.onEvent("Visa hjältens utrustning.");
        // Skriv ut hjältens föremål här.
    }

    // Menyval 4: striden mot rummets fiender (en våg i taget).
    // Efter en vunnen strid: kaffet mellan vågorna, kanelbullen och Frida när rummet är rensat,
    // och "vinner Fångdjupet!" när Martin i sista rummet är besegrad.
    private void fightRoomEnemy() {
        if (hero == null || world == null) {
            output.onEvent("Starta ett äventyr först.");
            return;
        }

        List<Enemy> enemies = world.getCurrentRoom().getEnemies();   // en fiende, eller flera samtidigt

        if (enemies.isEmpty()) {
            output.onEvent("Det finns ingen fiende att slåss mot.");
            return;
        }

        for (Enemy enemy : enemies) {
            showArt(enemy.getName());                                  // t.ex. Martins porträtt
            output.onEvent("Fiende " + enemy.getName() + " dyker upp!");
            enemy.getLine().ifPresent(line -> output.onEvent(enemy.getName() + ": \"" + line + "\""));   // fiendens replik
        }

        // Battle sköter striden och loggen; spelaren väljer attack inför varje runda
        // fullt namn på Character: java.lang.Character finns också
        se.fangdjupet.combat.Character winner = new Battle(output, this::chooseAttack, random).fight(hero, enemies);   // med slump: miss och ±1

        // Bara den sista bossen (Martin i Lektionssalen) ger vinst i hela spelet
        Room room = world.getCurrentRoom();
        if (winner == hero && !room.hasLivingEnemy()) {
            room.takeReward().ifPresent(this::drinkRefreshment);   // rummet rensat: t.ex. en kanelbulle
            room.getFrida().ifPresent(this::meetFrida);           // rummet rensat: Frida kan dyka upp
        }
        if (winner == hero && nextWaveIsReady(enemies, room)) {
            room.takeRefreshment().ifPresent(this::drinkRefreshment);   // t.ex. kaffe innan nästa våg
            String next = room.getEnemies().stream().map(Enemy::getName).collect(Collectors.joining(" och "));
            output.onEvent("Men vänta... " + next + " har gömt sig i " + room.getName() + " och hoppar fram!");
        }
        if (winner == hero && world.isAtFinalRoom() && !room.hasLivingEnemy()) {
            output.onEvent(hero.getName() + " vinner Fångdjupet!");
        }
    }

    // Får nästa våg hoppa fram (och kaffet delas ut)? Bara om hela vågen hjälten slogs mot är besegrad
    // och rummet har fler fiender. Vid rundtaket kan hjälten vinna på mest hp fast fiender lever –
    // då är vågen inte klar. (CodeRabbit-fynd i #18)
    static boolean nextWaveIsReady(List<Enemy> fought, Room room) {
        return fought.stream().noneMatch(Enemy::isAlive) && room.hasLivingEnemy();
    }

    // En paus i rummet: repliken, sedan dricker/äter hjälten (t.ex. kaffe +30 hp, kanelbulle +35 hp)
    private void drinkRefreshment(Room.Refreshment refreshment) {
        output.onEvent(refreshment.line());
        showArt(refreshment.potion().description());   // kaffekoppen eller kanelbullen
        int before = hero.getHp();
        hero.drink(refreshment.potion());
        output.onEvent(hero.getName() + " tar " + refreshment.potion().description()
                + " (+" + (hero.getHp() - before) + " hp)");   // det hjälten faktiskt fick, aldrig över max
        output.onEvent(HpBar.status(hero, 10));
    }

    // Frida ger två hälsodrycker (bara en gång) och hjälten dricker dem direkt
    private void meetFrida(Frida frida) {
        if (frida.hasGiven()) return;
        showArt(frida.getName());
        output.onEvent(frida.getName() + " dyker upp!");
        output.onEvent(frida.getName() + ": \"" + frida.greeting() + "\"");
        for (Potion potion : frida.give()) {
            int before = hero.getHp();
            hero.drink(potion);
            output.onEvent(hero.getName() + " dricker " + potion.description() + " (+" + (hero.getHp() - before) + " hp)");
            output.onEvent(HpBar.status(hero, 10));
        }
    }

    // Menyval 5: går till nästa rum (kollar inte om det finns fiender kvar)
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

    // Menyval 6: plockar upp allt i rummet. Ett vapen tas fram direkt och byter attack.
    private void pickUpItem() {
        if (hero == null || world == null) {
            output.onEvent("Starta ett äventyr först.");
            return;
        }
        Room room = world.getCurrentRoom();
        if (room.getItems().isEmpty()) {
            output.onEvent("Det finns inget att plocka upp här.");
            return;
        }
        for (Item item : room.getItems()) {
            room.removeItem(item);
            output.onEvent("Du plockar upp: " + item.description());
            showArt(item.description());
            if (item instanceof Weapon weapon) {
                hero.equip(weapon);   // vapnet byter attack: båge = avstånd, staff = magi
                output.onEvent("Du tar fram vapnet. Attack: " + hero.getAttack().name());
            }
        }
    }

    // Menyval 7: inte klar – ska visa dialog
    private void talkToSomeone() {
        output.onEvent("Prata med någon.");
        // Visa dialog här.
    }

    // Menyval e: avslutar loopen i run()
    private void exitGame() {
        output.onEvent("Tack för att du spelade!");
        running = false;
    }

    // Visar rummets namn, beskrivning och bild, det som ligger där – och tar Java-boken i Lektionssalen
    private void showCurrentRoom() {
        Room room = world.getCurrentRoom();
        output.onEvent(room.getName());
        output.onEvent(room.getDescription());
        AsciiArt.forRoom(room.getName()).ifPresent(art -> AsciiArt.show(output, art));

        // Det som ligger i rummet syns i grafiken – plocka upp med menyval 6
        for (Item item : room.getItems()) {
            output.onEvent("Här ligger: " + item.description());
            showArt(item.description());
        }

        // I Lektionssalen tar hjälten upp Java-boken direkt: mot Martin hjälper bara kunskap
        if (room.takeJavaBook()) {
            output.onEvent("Du hittar en Java-bok på katedern!");
            showArt("Java-bok");
            hero.pickUpJavaBook();
            output.onEvent("Mot Martin hjälper bara kunskap. Du kan nu bara använda Knowledge attacks.");
        }
    }

    // Visar bilden för en fiende, sak eller person – om det finns någon
    private void showArt(String name) {
        AsciiArt.forName(name).ifPresent(art -> AsciiArt.show(output, art));
    }

    // null = behåll nuvarande attack (vapnet väljer: båge = avstånd, staff = magi, svärd = närstrid)
    // Menyn kommer från AttackMenu: med Java-boken bara Martins fyra, annars närstrid/magi/avstånd
    private AttackStrategy chooseAttack(Hero hero) {
        List<AttackStrategy> options = AttackMenu.options(hero);
        IO.println("Välj attack (Enter = behåll):");
        AttackMenu.lines(options).forEach(IO::println);
        IO.print("Ditt val: ");
        return AttackMenu.choose(options, IO.readln());
    }
}
