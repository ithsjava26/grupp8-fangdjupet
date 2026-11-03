package se.fangdjupet.game;

import se.fangdjupet.combat.*;

import se.fangdjupet.items.Item;
import se.fangdjupet.items.Potion;
import se.fangdjupet.items.Weapon;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;


// Spelets huvudloop (Fredrik): menyn, rummen och vad varje val gör.
// Striderna lämnas över till Battle, och allt som skrivs ut går via output (Observer).
public class GameEngine {
    static final int SAVE_SLOT = 1;               // spelet använder en sparplats
    private static final int HERO_MAX_HP = 40;

    private final GameEventListener output;
    private final GameSaveFileHandler saves;      // sparar och läser in äventyret
    private final Random random = new Random();   // slumpen i striderna
    private boolean running;

    // output = dit allt skrivs, i spelet en EventBus med konsolen. Sparfilerna hamnar i mappen saves.
    public GameEngine(GameEventListener output) {
        this(output, new GameSaveFileHandler(Path.of("saves")));
    }

    // Med en egen sparhanterare, t.ex. en tillfällig mapp i testerna
    public GameEngine(GameEventListener output, GameSaveFileHandler saves) {
        if (output == null) {
            throw new IllegalArgumentException("output must not be null");
        }
        if (saves == null) {
            throw new IllegalArgumentException("saves must not be null");
        }
        this.output = output;
        this.saves = saves;
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
                4. Spara äventyret
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
                case "4" -> saveAdventure();
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
        if (heroName.isBlank()) heroName = "Hjälten";   // ett tomt namn går inte att spara

        hero = new Hero(heroName, HERO_MAX_HP, new MeleeAttack(10));
        world = newWorld(random);

        output.onEvent("Äventyret börjar!");
        showCurrentRoom();
    }

    // Fienderna och de fyra rummen, likadana varje gång – både för ett nytt och ett sparat äventyr
    static World newWorld(Random random) {
        Enemy doorEnemy = new Enemy("Entrédörren", 15, new MeleeAttack(5), 1);   // liten fiende
        Enemy corridorEnemy = CleaningWitch.create(random);   // kastar tvättsvamp och Svinto, sedan sprayar hon kemikalier
        Enemy coffeeEnemy = new Enemy("JavaFläcken", 15, new MeleeAttack(4), 1);   // liten fiende
        Enemy finalEnemy = Martin.create();   // bossen: byter till tyngre förklaringar vid halva livet

        return new WorldBuilder().build(
                doorEnemy, corridorEnemy, coffeeEnemy, finalEnemy);
    }

    // Huvudmenyn 2: läser in sparplats 1 och fortsätter i början av det sparade rummet
    private void continueAdventure() {
        output.onEvent("Fortsätt sparat äventyr.");
        GameState state;
        try {
            if (!saves.listSaveSlots().contains(SAVE_SLOT)) {
                output.onEvent("Det finns inget sparat äventyr. Välj 1 för att starta ett nytt.");
                return;
            }
            state = saves.load(SAVE_SLOT);
        } catch (IOException e) {   // även SaveFileException: trasig fil, fel version, saknat värde
            output.onEvent("Sparfilen går inte att läsa: " + e.getMessage());
            return;
        }

        Hero loadedHero = heroFrom(state);
        World loadedWorld = newWorld(random);
        if (!loadedHero.isAlive() || !fastForward(loadedHero, loadedWorld, state.nextRoom())) {
            output.onEvent("Sparfilen går inte att använda: hjälten är död eller rummet finns inte.");
            return;
        }

        hero = loadedHero;
        world = loadedWorld;
        output.onEvent("Välkommen tillbaka, " + hero.getName() + "!");
        output.onEvent(HpBar.status(hero, 10));
        showCurrentRoom();
    }

    // Spelmenyn 4: sparar hjältens namn, hp och rummet på sparplats 1 (en gammal sparning skrivs över)
    private void saveAdventure() {
        Room room = world.getCurrentRoom();
        try {
            saves.save(stateOf(hero, world), SAVE_SLOT);
            output.onEvent("Äventyret är sparat: " + room.getName() + ", " + hero.getHp() + "/" + hero.getMaxHp() + " hp.");
            output.onEvent("Fortsätter du senare börjar du från början av " + room.getName() + ".");
        } catch (IOException e) {
            output.onEvent("Kunde inte spara: " + e.getMessage());
        }
    }

    // Det som sparas: namn, maxHp, hp och numret på rummet hjälten är i (0 = Skoldörren)
    static GameState stateOf(Hero hero, World world) {
        return new GameState(hero.getName(), hero.getMaxHp(), hero.getHp(), world.getCurrentRoomIndex());
    }

    // Hjälten från en sparning: närstrid som i ett nytt äventyr, och det hp hen hade när det sparades
    static Hero heroFrom(GameState state) {
        Hero hero = new Hero(state.heroName(), state.heroMaxHp(), new MeleeAttack(10));
        hero.takeDamage(state.heroMaxHp() - state.heroHp());
        return hero;
    }

    // Går fram till det sparade rummet. Rummen före är redan avklarade: fienderna där hoppas över,
    // men vapnen som låg där tar hjälten med sig. false om rummet inte finns i världen.
    static boolean fastForward(Hero hero, World world, int roomIndex) {
        while (world.getCurrentRoomIndex() < roomIndex) {
            pickUpAll(hero, world.getCurrentRoom());
            if (!world.moveForward()) return false;
        }
        return true;
    }

    // Spelmenyn 3: hp, vapen, rustning, attack och Java-boken
    private void showEquipment() {
        output.onEvent("Visa hjältens utrustning.");
        equipment(hero).forEach(output::onEvent);
    }

    // Raderna i "Visa hjältens utrustning", t.ex. "Vapen: Pilbåge (+4 i skada)"
    static List<String> equipment(Hero hero) {
        List<String> lines = new ArrayList<>();
        lines.add(HpBar.status(hero, 10));
        lines.add("Vapen: " + hero.getWeapon()
                .map(w -> w.description() + " (+" + w.damageBonus() + " i skada)")
                .orElse("inget"));
        lines.add("Rustning: " + hero.getArmor()
                .map(a -> a.description() + " (försvar " + a.defense() + ")")
                .orElse("ingen"));
        lines.add("Attack: " + AttackMenu.label(hero.getAttack()));
        if (hero.hasJavaBook()) {
            lines.add("Java-boken: ja – mot Martin hjälper bara kunskap");
        }
        return lines;
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

    // Spelmenyn 2: går till nästa rum – men bara när rummets fiender är besegrade
    private void moveToAnotherRoom() {
        if (world == null) {
            output.onEvent("Starta ett äventyr först.");
            return;
        }

        Optional<String> blocked = blockedBy(world.getCurrentRoom());
        if (blocked.isPresent()) {
            output.onEvent(blocked.get());
            return;
        }

        if (world.moveForward()) {
            showCurrentRoom();
        } else {
            output.onEvent("Det finns inga fler rum.");
        }
    }

    // Står någon i vägen? Lever en fiende i rummet får hjälten inte gå förbi den.
    // Tomt = vägen är fri. Även efter rundtaket, där hjälten kan vinna fast fienden lever.
    static Optional<String> blockedBy(Room room) {
        List<Enemy> enemies = room.getEnemies();
        if (enemies.isEmpty()) return Optional.empty();
        String names = enemies.stream().map(Enemy::getName).collect(Collectors.joining(" och "));
        return Optional.of(names + " står i vägen! Besegra rummets fiender innan du går vidare.");
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
