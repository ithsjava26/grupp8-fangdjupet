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
        IO.println(menu(world == null ? null : world.getCurrentRoom()));
        IO.print("Välj ett alternativ: ");
    }

    // Två menyer. Utan äventyr (room == null): huvudmenyn, där man startar eller fortsätter.
    // I spelet: spelmenyn med rummets namn som rubrik – där ska man inte se "Starta nytt äventyr".
    static String menu(Room room) {
        if (room == null) {
            return """
                    Fangdjupet
                    ==========
                    1. Starta nytt äventyr
                    2. Fortsätt sparat äventyr
                    e. Avsluta
                    """;
        }
        return room.getName() + "\n"
                + "=".repeat(room.getName().length()) + "\n"
                + """
                1. Strid mot rummets fiende
                2. Gå till ett annat rum
                3. Visa hjältens utrustning
                e. Avsluta
                """;
    }

    // Ett menyval → rätt metod. Vilken meny som gäller beror på om ett äventyr pågår.
    private void handleChoice(String choice) {
        if (world == null) {
            switch (choice) {
                case "1" -> startNewAdventure();
                case "2" -> continueAdventure();
                case "e" -> exitGame();
                default -> output.onEvent("Ogiltigt val. Försök igen.");
            }
        } else {
            switch (choice) {
                case "1" -> fightRoomEnemy();
                case "2" -> moveToAnotherRoom();
                case "3" -> showEquipment();
                case "e" -> exitGame();
                default -> output.onEvent("Ogiltigt val. Försök igen.");
            }
        }
    }

    // Äventyret är slut (game over eller vinst): tillbaka till huvudmenyn
    private void endAdventure() {
        hero = null;
        world = null;
    }

    private Hero hero;
    private World world;

    // Huvudmenyn 1: skapar hjälten, fienderna och rummen och visar det första rummet
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

    // Huvudmenyn 2: inte klar – ska läsa in sparfilen
    private void continueAdventure() {
        output.onEvent("Fortsätt sparat äventyr.");
        // Läs in sparfilen här.
    }

    // Spelmenyn 3: inte klar – ska visa hjältens föremål
    private void showEquipment() {
        output.onEvent("Visa hjältens utrustning.");
        // Skriv ut hjältens föremål här.
    }

    // Spelmenyn 1: striden mot rummets fiender (en våg i taget).
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

        // Hjälten är död: game over och tillbaka till huvudmenyn, där 1 startar ett nytt äventyr
        if (!hero.isAlive()) {
            gameOver(hero, winner).forEach(output::onEvent);
            endAdventure();
            return;
        }

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
            endAdventure();   // spelet är vunnet: tillbaka till huvudmenyn
        }
    }

    // Får nästa våg hoppa fram (och kaffet delas ut)? Bara om hela vågen hjälten slogs mot är besegrad
    // och rummet har fler fiender. Vid rundtaket kan hjälten vinna på mest hp fast fiender lever –
    // då är vågen inte klar. (CodeRabbit-fynd i #18)
    static boolean nextWaveIsReady(List<Enemy> fought, Room room) {
        return fought.stream().noneMatch(Enemy::isAlive) && room.hasLivingEnemy();
    }

    // Raderna som visas när hjälten dör: GAME OVER i röda stora bokstäver, vem som vann, och hur man börjar om.
    // Dör båda i samma slag vinner den som slog – då kan hjälten "vinna" fast hen är död.
    static List<String> gameOver(Hero hero, se.fangdjupet.combat.Character winner) {
        String how = (winner == hero)
                ? hero.getName() + " vann striden men dog av sin egen attack."
                : hero.getName() + " besegrades av " + winner.getName() + ".";
        String art = AsciiArt.GAME_OVER.endsWith("\n")
                ? AsciiArt.GAME_OVER.substring(0, AsciiArt.GAME_OVER.length() - 1)
                : AsciiArt.GAME_OVER;
        return List.of(AsciiArt.red(art), how, "Välj 1 för att börja om, eller e för att avsluta.");
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

    // Spelmenyn 2: går till nästa rum (kollar inte om det finns fiender kvar)
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

    // Menyval e: avslutar loopen i run()
    private void exitGame() {
        output.onEvent("Tack för att du spelade!");
        running = false;
    }

    // Plockar upp allt i rummet och ger tillbaka det som togs. Ett vapen hamnar i handen (bonus i skada),
    // men attacken är kvar: pilbågen (avstånd 5) är svagare än närstrid (10), och den som bara trycker
    // Enter i attackmenyn ska inte få sämre attack. Bågen och staven väljs i attackmenyn.
    static List<Item> pickUpAll(Hero hero, Room room) {
        List<Item> items = room.getItems();   // en kopia – säker att ta bort ur rummet medan vi går igenom den
        for (Item item : items) {
            room.removeItem(item);
            if (item instanceof Weapon weapon) {
                AttackStrategy attack = hero.getAttack();
                hero.equip(weapon);       // equip byter attack efter vapnet ...
                hero.setAttack(attack);   // ... så vi lägger tillbaka den spelaren hade
            }
        }
        return items;
    }

    // Visar rummets namn, beskrivning och bild, plockar upp det som ligger där – och tar Java-boken i Lektionssalen
    private void showCurrentRoom() {
        Room room = world.getCurrentRoom();
        output.onEvent(room.getName());
        output.onEvent(room.getDescription());
        AsciiArt.forRoom(room.getName()).ifPresent(art -> AsciiArt.show(output, art));

        // Det som ligger i rummet plockar hjälten upp direkt, som Java-boken
        for (Item item : pickUpAll(hero, room)) {
            output.onEvent("Du hittar och plockar upp: " + item.description());
            showArt(item.description());
            if (item instanceof Weapon weapon) {
                output.onEvent("Du har nu " + weapon.description() + " i handen: +" + weapon.damageBonus() + " i skada.");
            }
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
