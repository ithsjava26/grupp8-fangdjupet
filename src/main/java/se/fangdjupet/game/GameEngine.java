package se.fangdjupet.game;

import se.fangdjupet.combat.*;

import se.fangdjupet.items.Armor;
import se.fangdjupet.items.Brasklapp;
import se.fangdjupet.items.Item;
import se.fangdjupet.items.ItemType;
import se.fangdjupet.items.Potion;
import se.fangdjupet.items.StressBall;
import se.fangdjupet.items.Weapon;
import se.fangdjupet.items.WeaponRange;

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
    private List<Enemy> currentFoes = List.of();   // fienderna i striden just nu (attackmenyn: Martins lavalampa)
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
    private Runnable onInput = () -> { };   // t.ex. ett blipp när spelaren trycker Enter (SoundListener)

    // Körs varje gång spelaren har svarat
    public void onInput(Runnable onInput) {
        this.onInput = onInput == null ? () -> { } : onInput;
    }

    // Hjältens attack just nu, om ett äventyr pågår – ljudet låter olika för magi, avstånd och närstrid
    // Hjältens namn, tomt innan äventyret har börjat (stridspanelen i konsolen)
    public Optional<String> heroName() {
        return Optional.ofNullable(hero).map(Hero::getName);
    }

    public Optional<AttackStrategy> heroAttack() {
        return Optional.ofNullable(hero).map(Hero::getAttack);
    }

    // All inmatning går hit: läser raden och säger till att spelaren har svarat
    private String readln() {
        String line = IO.readln();
        onInput.run();
        return line;
    }

    public void run() {
        running = true;

        while (running) {
            showMenu();
            String choice = readln().trim().toLowerCase();
            handleChoice(choice);
        }
    }

    private void showMenu() {
        if (world != null) {
            if (ConsoleStyle.enabled()) {   // statusraden: rummet, hp och attacken, bara i en riktig konsol
                IO.println(ConsoleStyle.status(world.getCurrentRoom().getName(), HpBar.status(hero, 10),
                        AttackMenu.label(hero.getAttack())));
            }
            IO.println(ConsoleStyle.panel(ConsoleStyle.map(roomMap(world))));   // kartan syns hela tiden i spelet
        } else {
            IO.println(AsciiArt.SCHOOL.stripTrailing());   // skolbyggnaden, bara ovanför huvudmenyn
        }
        IO.println(ConsoleStyle.panel(ConsoleStyle.menu(menu(world == null ? null : world.getCurrentRoom()))));   // färger bara i en riktig konsol
        IO.print(ConsoleStyle.prompt("Välj ett alternativ: "));
    }

    // Två menyer. Utan äventyr (room == null): huvudmenyn, där man startar eller fortsätter.
    // I spelet: spelmenyn med rummets namn som rubrik – där ska man inte se "Starta nytt äventyr".
    static String menu(Room room) {
        if (room == null) {
            return """
                    Fångdjupet
                    ==========
                    1. Starta nytt äventyr
                    2. Fortsätt sparat äventyr
                    e. Avsluta
                    """;
        }
        return room.getName() + "\n"
                + "=".repeat(room.getName().length()) + "\n"
                + fightOption(room) + "\n"
                + "2. Gå till ett annat rum\n"
                + "3. Visa hjältens utrustning\n"
                + "4. Spara äventyret\n"
                + searchOption(room) + "\n"
                + "e. Avsluta\n";
    }

    // Menyval 1. Lever en fiende i rummet blir det gulmarkerat med namnet, t.ex. "(Entrédörren väntar!)".
    static String fightOption(Room room) {
        List<Enemy> enemies = room.getEnemies();
        if (enemies.isEmpty()) return "1. Strid mot rummets fiende";
        String names = enemies.stream().map(Enemy::getName).collect(Collectors.joining(" och "));
        return AsciiArt.yellow("1. Strid mot rummets fiende  (" + names + " väntar!)");
    }

    // Menyval 5. Finns något att hitta i rummet blir det gulmarkerat, så att spelaren ser det.
    static String searchOption(Room room) {
        return room.hasSomethingToFind()
                ? AsciiArt.yellow("5. Leta i rummet  (något finns här!)")
                : "5. Leta i rummet";
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
                case "5" -> searchRoom();
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
        IO.print(ConsoleStyle.prompt("Vad heter hjälten? "));
        String heroName = readln().trim();
        if (heroName.isBlank()) heroName = "Hjälten";   // ett tomt namn går inte att spara

        hero = newHero(heroName, HERO_MAX_HP);
        world = newWorld(random);

        output.onEvent("Äventyret börjar!");
        output.onEvent("Du har " + SWORD + " i handen: +" + AttackMenu.SWORD_BONUS + " i närstrid.");
        showCurrentRoom();
    }

    // Hjälten börjar med ett svärd i handen och bara närstrid. Magi och avstånd kommer med staven och pilbågen.
    static final String SWORD = "Svärd";

    static Hero newHero(String name, int maxHp) {
        Hero hero = new Hero(name, maxHp, new MeleeAttack(AttackMenu.MELEE));
        equipAndAim(hero, new Weapon(ItemType.SWORD, WeaponRange.MELEE, SWORD, AttackMenu.SWORD_BONUS, 1));
        return hero;
    }

    // Fienderna och de fyra rummen, likadana varje gång – både för ett nytt och ett sparat äventyr
    static World newWorld(Random random) {
        Enemy doorEnemy = new Enemy("Entrédörren", 20, new MeleeAttack(6), 1);   // tung dörr: fyra svärdshugg, och den slår igen hårt
        Enemy corridorEnemy = CleaningWitch.create(random);   // kastar tvättsvamp och Svinto, sedan sprayar hon kemikalier
        Enemy coffeeEnemy = new Enemy("JavaFläcken", 24, new MeleeAttack(7), 2);   // seg fläck: två pilar, och den slår tillbaka
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
        if (!loadedHero.isAlive() || !resume(loadedHero, loadedWorld, state)) {
            output.onEvent("Sparfilen går inte att använda: hjälten är död eller rummet finns inte.");
            return;
        }

        hero = loadedHero;
        world = loadedWorld;
        output.onEvent("Välkommen tillbaka, " + hero.getName() + "!");
        output.onEvent(HpBar.status(hero, 10));
        showCurrentRoom();
    }

    // Spelmenyn 4: sparar hjältens namn, hp, rummet och ryggsäcken på sparplats 1 (en gammal sparning skrivs över)
    private void saveAdventure() {
        Room room = world.getRooms().get(world.getFurthestRoomIndex());   // där hjälten fortsätter
        try {
            saves.save(stateOf(hero, world), SAVE_SLOT);
            output.onEvent("Äventyret är sparat: " + room.getName() + ", " + hero.getHp() + "/" + hero.getMaxHp() + " hp.");
            output.onEvent("Fortsätter du senare börjar du från början av " + room.getName() + ".");
        } catch (IOException e) {
            output.onEvent("Kunde inte spara: " + e.getMessage());
        }
    }

    // Det som sparas: namn, maxHp, hp, ryggsäcken och numret på rummet längst fram som hjälten har nått (0 = Skoldörren).
    // Har hjälten gått tillbaka till Kafferummet från Korridoren fortsätter hen ändå i Korridoren –
    // rummen före är rensade, annars hade hjälten inte kommit dit.
    static GameState stateOf(Hero hero, World world) {
        return new GameState(hero.getName(), hero.getMaxHp(), hero.getHp(), world.getFurthestRoomIndex(),
                hero.getInventory().getAll());
    }

    // Hjälten från en sparning: svärdet som i ett nytt äventyr, det hp hen hade och allt i ryggsäcken
    static Hero heroFrom(GameState state) {
        Hero hero = newHero(state.heroName(), state.heroMaxHp());
        hero.takeDamage(state.heroMaxHp() - state.heroHp());
        state.items().forEach(item -> giveBack(hero, item));
        return hero;
    }

    // Föremålen kommer i den ordning hjälten fick dem: varje vapen låser upp sin attack, och det sista
    // hamnar i handen – samma vapen som när det sparades, eftersom man inte kan byta tillbaka till ett gammalt.
    private static void giveBack(Hero hero, Item item) {
        switch (item) {
            case Weapon weapon -> equipAndAim(hero, weapon);
            case Armor armor -> hero.equip(armor);
            case StressBall ball -> hero.pickUp(ball);
            case Brasklapp note -> hero.takeBrasklapp(note);
            case Potion potion -> { }   // drycker dricks direkt och ligger aldrig i ryggsäcken
        }
    }

    // Går till det sparade rummet. Rummen före är avklarade, så de rensas: världen byggs ju om från början.
    // Det sparade rummet börjar om. Det hjälten redan har tas bort ur världen, så Städrustningen inte går att
    // hitta två gånger, och det hen lämnade ligger kvar i rummen. Svärdet finns alltid i ryggsäcken, så en
    // sparning utan föremål är en gammal sparfil: då hämtas vapnen från rummen före (fastForward).
    static boolean resume(Hero hero, World world, GameState state) {
        if (state.items().isEmpty()) {
            return fastForward(hero, world, state.nextRoom());
        }
        for (Item item : hero.getInventory().getAll()) {
            world.getRooms().forEach(room -> room.forget(item));
        }
        while (world.getCurrentRoomIndex() < state.nextRoom()) {
            world.getCurrentRoom().markCleared();
            if (!world.moveForward()) return false;
        }
        return true;
    }

    // En gammal sparfil, utan föremål: går fram till det sparade rummet. Rummen före är redan avklarade:
    // de rensas, men vapnen som låg där tar hjälten med sig. false om rummet inte finns i världen.
    static boolean fastForward(Hero hero, World world, int roomIndex) {
        while (world.getCurrentRoomIndex() < roomIndex) {
            pickUpAll(hero, world.getCurrentRoom());
            world.getCurrentRoom().markCleared();
            if (!world.moveForward()) return false;
        }
        return true;
    }

    // Spelmenyn 3: hp, vapen, rustning, attack, Java-boken och ryggsäcken
    private void showEquipment() {
        output.onEvent("Visa hjältens utrustning.");
        equipment(hero).forEach(output::onEvent);
        IO.print(ConsoleStyle.gearEnd());   // stänger panelen i en riktig konsol
        // Bilder på allt hjälten bär: vapnen (svärdet, pilbågen, staven), rustningen (t.ex. Städrustningen),
        // Java-boken och stressbollen
        weaponArt(hero).forEach(art -> AsciiArt.show(output, art));
        hero.getArmor().flatMap(AsciiArt::forItem).ifPresent(art -> AsciiArt.show(output, art));
        if (hero.hasJavaBook()) AsciiArt.show(output, AsciiArt.JAVA_BOOK);
        hero.getStressBall().flatMap(AsciiArt::forItem).ifPresent(art -> AsciiArt.show(output, art));
    }

    // Bilderna på vapnen i ryggsäcken, inte bara det i handen. Mot Martin hjälper bara kunskap,
    // men hjälten bär pilbågen och staven ändå.
    static List<String> weaponArt(Hero hero) {
        return hero.getInventory().getAll(Weapon.class).stream()
                .flatMap(weapon -> AsciiArt.forItem(weapon).stream())
                .toList();
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
        hero.getStressBall().ifPresent(ball -> lines.add("Stressboll: i fickan (+" + ball.hpPerTurn()
                + " hp per runda, eller kasta den på Martin)"));
        List<Item> backpack = hero.getInventory().getAll();
        if (!backpack.isEmpty()) {
            lines.add("Ryggsäck: " + backpack.stream().map(Item::description).collect(Collectors.joining(", ")));
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
            // Har hjälten stressbollen (Martin) eller Städrustningen blir bossen lite svårare (bara en gång)
            if (enemy instanceof Boss boss && !boss.isEnraged()) {
                if (Martin.NAME.equals(boss.getName()) && hero.getStressBall().isPresent()) {
                    Martin.enrage(boss, Martin.STRESS_BALL_LINE);
                    output.onEvent(Martin.STRESS_BALL_ANGRY);
                } else if (wearsCleaningArmor(hero)) {
                    reactToCleaningArmor(boss);
                }
            }
            showArt(enemy.getName());                                  // t.ex. Martins porträtt
            output.onEvent("Fiende " + enemy.getName() + " dyker upp!");
            enemy.getLine().ifPresent(line -> output.onEvent(enemy.getName() + ": \"" + line + "\""));   // fiendens replik
            if (Martin.NAME.equals(enemy.getName()) && hero.getKnowledgeDefense() > 0) {
                output.onEvent("Du drar på dig ullvantarna: Martins förklaringar tar " + hero.getKnowledgeDefense() + " mindre.");
            }
        }

        // Battle sköter striden och loggen; spelaren väljer attack inför varje runda
        // fullt namn på Character: java.lang.Character finns också
        currentFoes = enemies;                                // attackmenyn ser Martins lavalampa
        se.fangdjupet.combat.Character winner = new Battle(output, this::chooseAttack, random)
                .withAttackArt()                              // bilder för attackerna (AttackArt)
                .fight(hero, enemies);                        // med slump: miss och ±1

        // Hjälten är död: game over och tillbaka till huvudmenyn, där 1 startar ett nytt äventyr
        if (!hero.isAlive()) {
            gameOver(hero, winner).forEach(output::onEvent);
            endAdventure();
            return;
        }

        // Bara den sista bossen (Martin i Lektionssalen) ger vinst i hela spelet
        Room room = world.getCurrentRoom();
        if (winner == hero && !room.hasLivingEnemy()) {
            room.takeReward().ifPresent(r -> offerRefreshment(room, r));   // rummet rensat: t.ex. en kanelbulle
            room.getFrida().ifPresent(this::meetFrida);           // rummet rensat: Frida kan dyka upp
        }
        if (winner == hero && nextWaveIsReady(enemies, room)) {
            room.takeRefreshment().ifPresent(r -> offerRefreshment(room, r));   // t.ex. kaffe innan nästa våg
            String next = room.getEnemies().stream().map(Enemy::getName).collect(Collectors.joining(" och "));
            output.onEvent("Men vänta... " + next + " har gömt sig i " + room.getName() + " och hoppar fram!");
        }
        if (winner == hero && world.isAtFinalRoom() && !room.hasLivingEnemy()) {
            AsciiArt.show(output, AsciiArt.VICTORY);
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

    // En paus i rummet vid rätt ögonblick: repliken och bilden gulmarkerade, hjältens hp, och spelaren väljer (j/n).
    // Säger man nej står kaffet/kanelbullen kvar i rummet (Leta i rummet, eller när man kommer tillbaka) –
    // men nästa våg hoppar fram direkt, så den som tackar nej kan dö.
    private void offerRefreshment(Room room, Room.Refreshment refreshment) {
        Potion potion = refreshment.potion();
        output.onEvent(AsciiArt.yellow(refreshment.line()));
        showArt(potion.description());                 // kaffekoppen eller kanelbullen
        output.onEvent(HpBar.status(hero, 10));
        IO.print(ConsoleStyle.tint(AsciiArt.yellow(takeQuestion(potion))));
        if (wantsIt(readln())) {
            drinkRefreshment(refreshment);
        } else {
            room.leaveItem(potion);
            output.onEvent(leftBehind(potion));
        }
    }

    // Frågan för något i rummet: "Ta en kopp kaffe (+30 hp)? (j/n): " eller "Plocka upp Pilbåge? (j/n): "
    static String takeQuestion(Item item) {
        return item instanceof Potion p
                ? "Ta " + p.description() + " (+" + p.healAmount() + " hp)? (j/n): "
                : "Plocka upp " + item.description() + "? (j/n): ";
    }

    // När spelaren säger nej
    static String leftBehind(Item item) {
        if (WorldBuilder.ULLVANTAR.equals(item.description())) {   // dörren slår igen: hit kommer man inte tillbaka
            return "Du låter " + item.description() + " ligga kvar. När Entrédörren slår igen bakom dig är de borta.";
        }
        return item instanceof Potion
                ? "Du låter " + item.description() + " stå kvar. Den finns kvar om du letar i rummet eller kommer tillbaka."
                : "Du låter " + item.description() + " ligga kvar. Den finns kvar om du kommer tillbaka.";
    }

    // Hjälten dricker/äter (t.ex. kaffe +30 hp, kanelbulle +35 hp)
    private void drinkRefreshment(Room.Refreshment refreshment) {
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
        output.onEvent(frida.getName() + ": \"" + Frida.SURPRISE + "\"");
        Brasklapp note = frida.brasklapp();
        AsciiArt.forItem(note).ifPresent(art -> AsciiArt.show(output, art));
        output.onEvent(Frida.NOTE_GIVEN);
        hero.takeBrasklapp(note);
    }

    // Spelmenyn 2: går till nästa rum – men bara när rummets fiender är besegrade
    private void moveToAnotherRoom() {
        if (world == null) {
            output.onEvent("Starta ett äventyr först.");
            return;
        }

        // Spelaren väljer själv: kartan och rummen med siffror, Enter = stanna kvar
        IO.println(ConsoleStyle.panel(ConsoleStyle.map(roomMap(world))));
        IO.println(ConsoleStyle.panel(ConsoleStyle.menu(roomChoices(world)), "Rum"));
        IO.print(ConsoleStyle.prompt("Välj rum (Enter = stanna kvar): "));
        goTo(readln().trim());
    }

    // Svaret på "Välj rum": en siffra i listan, eller tomt för att stanna
    private void goTo(String answer) {
        Room here = world.getCurrentRoom();
        if (answer.isEmpty()) {
            output.onEvent("Du stannar i " + here.getName() + ".");
            return;
        }
        int index;
        try {
            index = Integer.parseInt(answer) - 1;
        } catch (NumberFormatException e) {
            output.onEvent("Ogiltigt val. Försök igen.");
            return;
        }
        if (index < 0 || index >= world.getRooms().size()) {
            output.onEvent("Ogiltigt val. Försök igen.");
        } else if (index == world.getCurrentRoomIndex()) {
            output.onEvent("Du är redan i " + here.getName() + ".");
        } else if (moveAndLock(index)) {
            showCurrentRoom();
        } else {
            output.onEvent(lockedMessage(world, index));
        }
    }

    static final String LOCKED_IN =
            "Du är nu inlåst i Fångdjupet och behöver besegra slutbossen innan du kan ta dig ut.";

    // Går till rummet. Första gången hjälten går in i skolan slår Entrédörren igen bakom hen.
    private boolean moveAndLock(int index) {
        boolean wasLocked = world.isEntranceLocked();
        if (!world.moveTo(index)) return false;
        output.onEvent("Du går till " + world.getCurrentRoom().getName() + ".");
        if (!wasLocked && world.isEntranceLocked()) {
            AsciiArt.show(output, AsciiArt.LOCKED_DOOR);
            output.onEvent("Entrédörren slår igen bakom dig!");
            output.onEvent(LOCKED_IN);
        }
        return true;
    }

    // Varför ett rum är låst: rummet längst fram har en fiende som lever, eller rummet ligger längre bort
    static String lockedMessage(World world, int index) {
        String name = world.getRooms().get(index).getName();
        if (index == 0 && world.isEntranceLocked()) {
            return name + " är låst. " + LOCKED_IN;
        }
        Room front = world.getRooms().get(world.getFurthestRoomIndex());
        if (index == world.getFurthestRoomIndex() + 1) {
            String blocker = blockedBy(front).map(m -> m.substring(0, m.indexOf(" står i vägen"))).orElse("någon");
            return name + " är låst. " + blocker + " står i vägen i " + front.getName() + " – besegra rummets fiender först.";
        }
        return name + " är låst. Du har inte hittat vägen dit än.";
    }

    // ── Kartan över rummen ──
    // Två rader som en slinga: Skoldörren → Kafferummet överst, Korridoren → Lektionssalen nederst (åt vänster).
    //   KARTA  @ du är här  x rensat  ! fiender  ? nytt  # låst
    //   [x] Skoldorren -- [@] Kafferummet
    //                       |
    //   [#] Lektionssalen -- [?] Korridoren
    // Bara ren ASCII, som all annan grafik.
    static final String MAP_LEGEND = "KARTA  @ du är här  x rensat  ! fiender  ? nytt  # låst";

    static String roomMap(World world) {
        List<Room> rooms = world.getRooms();
        int n = rooms.size();
        int half = (n + 1) / 2;
        List<String> cells = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            cells.add("[" + mapSymbol(world, i) + "] " + shortName(rooms.get(i).getName()));
        }
        int width = cells.stream().mapToInt(String::length).max().orElse(0);
        String joiner = " -- ";

        StringBuilder top = new StringBuilder();
        StringBuilder bottom = new StringBuilder();
        for (int col = 0; col < half; col++) {
            if (col > 0) top.append(joiner);
            top.append(pad(cells.get(col), width));

            int below = 2 * half - 1 - col;               // nedre raden går tillbaka åt vänster
            boolean filled = below < n;
            boolean prevFilled = col > 0 && 2 * half - col < n;
            if (col > 0) bottom.append(filled && prevFilled ? joiner : " ".repeat(joiner.length()));
            bottom.append(pad(filled ? cells.get(below) : "", width));
        }

        StringBuilder map = new StringBuilder(MAP_LEGEND).append('\n').append(top.toString().stripTrailing());
        if (n > half) {
            int column = (half - 1) * (width + joiner.length()) + 1;   // under symbolen i sista rummet överst
            map.append('\n').append(" ".repeat(column)).append('|')
               .append('\n').append(bottom.toString().stripTrailing());
        }
        return map.toString();
    }

    // @ här, # låst, ? öppet men nytt, ! varit där och fiender lever, x rensat
    static char mapSymbol(World world, int index) {
        if (index == world.getCurrentRoomIndex()) return '@';
        if (!world.isOpen(index)) return '#';
        if (!world.hasVisited(index)) return '?';
        return world.getRooms().get(index).hasLivingEnemy() ? '!' : 'x';
    }

    // Listan i "Gå till ett annat rum", t.ex. "2. Kafferummet  (du är här)"
    static String roomChoices(World world) {
        StringBuilder sb = new StringBuilder("Vart vill du gå?");
        List<Room> rooms = world.getRooms();
        for (int i = 0; i < rooms.size(); i++) {
            String status = switch (mapSymbol(world, i)) {
                case '@' -> "du är här";
                case '#' -> "låst";
                case '?' -> "nytt rum";
                case '!' -> "fiender kvar";
                default -> "rensat";
            };
            sb.append('\n').append(i + 1).append(". ").append(rooms.get(i).getName()).append("  (").append(status).append(')');
        }
        return sb.toString();
    }

    // Kort namn till kartan: "Korridoren och städskrubben" → "Korridoren", "Skoldörren" blir kvar
    static String shortName(String roomName) {
        String first = roomName.split(" och ")[0];
        return first;
    }

    private static String pad(String s, int width) {
        return s + " ".repeat(Math.max(0, width - s.length()));
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

    // Plockar upp allt i rummet och ger tillbaka det som togs. Ett vapen hamnar i handen och hjälten
    // byter till vapnets attack (equipAndAim): pilbågen och staven slår mycket hårdare än svärdet.
    static List<Item> pickUpAll(Hero hero, Room room) {
        List<Item> items = room.getItems();   // en kopia – säker att ta bort ur rummet medan vi går igenom den
        for (Item item : items) {
            room.removeItem(item);
            if (item instanceof Weapon weapon) {
                equipAndAim(hero, weapon);
            }
        }
        return items;
    }

    // Vapnet i handen, och hjälten byter till vapnets attack: pilbågen skjuter, staven trollar, svärdet slår.
    // Med Java-boken (bara kunskap mot Martin) eller ett spjut (passar båda) behålls attacken.
    static void equipAndAim(Hero hero, Weapon weapon) {
        AttackStrategy attack = hero.getAttack();
        hero.equip(weapon);       // equip byter attack efter vapnet ...
        hero.setAttack(attack);   // ... med grundstyrkan, så vi lägger tillbaka den spelaren hade ...
        AttackMenu.forWeapon(hero).ifPresent(hero::setAttack);   // ... och tar menyns attack för vapnet
    }

    // Bossen ser Städrustningen: Martin blir arg och förklarar hårdare, StädHäxan skrattar elakt
    // (det är ju hennes rustning) och hotar med Vanish. Andra bossar bryr sig inte.
    private void reactToCleaningArmor(Boss boss) {
        switch (boss.getName()) {
            case Martin.NAME -> {
                Martin.enrage(boss);
                output.onEvent(Martin.angryLine(random));   // en Java-sak hjälten inte kan, slumpad
            }
            case CleaningWitch.NAME -> {
                CleaningWitch.enrage(boss);
                output.onEvent(CleaningWitch.LAUGH);
            }
            default -> { }
        }
    }

    // Bär hjälten Städrustningen från städskrubben?
    static boolean wearsCleaningArmor(Hero hero) {
        return hero.getArmor().map(a -> WorldBuilder.STADRUSTNING.equals(a.description())).orElse(false);
    }

    // Spelmenyn 5: letar i rummet. Det som var gömt (Städrustningen i städskrubben) tas på eller används direkt.
    private void searchRoom() {
        Room room = world.getCurrentRoom();
        output.onEvent("Du letar i " + room.getName() + " ...");
        boolean lying = offerItems(room);          // det man lämnat kvar kan man ta nu
        List<Item> found = room.search();          // det gömda (Städrustningen i städskrubben)
        if (found.isEmpty() && !lying) {
            output.onEvent("Du hittar inget här.");
            return;
        }
        for (Item item : found) {
            output.onEvent("Du hittar: " + item.description() + "!");
            AsciiArt.forItem(item).ifPresent(art -> AsciiArt.show(output, art));
            take(item);   // man letade själv – det man hittar tar man
        }
    }

    // Hjälten tar föremålet: vapnet i handen, rustningen på sig, drycken dricks direkt
    private void take(Item item) {
        switch (item) {   // Item är sealed: alla sorter måste stå här, så inget default behövs
            case Brasklapp note -> {
                hero.takeBrasklapp(note);
                output.onEvent("Du stoppar " + note.description() + " i fickan: den går att läsa upp mot Martin, en gång.");
            }
            case Armor armor -> {
                hero.equip(armor);
                output.onEvent("Du tar på dig " + armor.description() + ": +" + armor.defense() + " i försvar.");
                if (armor.type() == ItemType.GLOVE) {
                    hero.warmUp();
                    output.onEvent("Ullvantarna värmer: du har nu " + hero.getHp() + "/" + hero.getMaxHp() + " hp.");
                }
            }
            case Weapon weapon -> {
                equipAndAim(hero, weapon);
                output.onEvent("Du har nu " + weapon.description() + " i handen: +" + weapon.damageBonus() + " i skada.");
                output.onEvent("Du byter till " + AttackMenu.label(hero.getAttack()) + ".");
            }
            case Potion potion -> {
                int before = hero.getHp();
                hero.drink(potion);
                output.onEvent("Du dricker " + potion.description() + " (+" + (hero.getHp() - before) + " hp).");
            }
            case StressBall ball -> {
                hero.pickUp(ball);
                output.onEvent("Du stoppar " + ball.description() + " i fickan: i striden klämmer du den, +"
                        + ball.hpPerTurn() + " hp varje runda.");
                output.onEvent("Du kan också kasta den på Martin (" + StressBallThrow.POWER
                        + " i skada om den träffar) – men då är den borta.");
            }
        }
    }

    // Raden när något ligger i rummet, t.ex. "Här ligger: Pilbåge (+3 i skada)"
    static String itemHere(Item item) {
        String bonus = switch (item) {
            case Weapon w -> "+" + w.damageBonus() + " i skada";
            case Armor a -> "+" + a.defense() + " i försvar";
            case Potion p -> "+" + p.healAmount() + " hp";
            case StressBall b -> "+" + b.hpPerTurn() + " hp per runda";
            case Brasklapp n -> n.power() + " i skada mot Martin";
        };
        return "Här ligger: " + item.description() + " (" + bonus + ")";
    }

    // Svaret på "Plocka upp ...? (j/n)": j, ja eller y. Enter eller något annat = nej.
    static boolean wantsIt(String answer) {
        if (answer == null) return false;
        String a = answer.trim().toLowerCase();
        return a.equals("j") || a.equals("ja") || a.equals("y") || a.equals("yes");
    }

    // Visar rummets namn, beskrivning och bild, plockar upp det som ligger där – och tar Java-boken i Lektionssalen
    private void showCurrentRoom() {
        Room room = world.getCurrentRoom();
        output.onEvent(room.getName());
        output.onEvent(room.getDescription());
        AsciiArt.forRoom(room.getName()).ifPresent(art -> AsciiArt.show(output, art));

        offerItems(room);

        // I Lektionssalen tar hjälten upp Java-boken direkt: mot Martin hjälper bara kunskap
        if (room.takeJavaBook()) {
            output.onEvent("Du hittar en Java-bok på katedern!");
            showArt("Java-bok");
            hero.pickUpJavaBook();
            output.onEvent("Mot Martin hjälper bara kunskap. Du kan nu bara använda Knowledge attacks.");
        }
    }

    // Det som ligger i rummet gulmarkeras, och spelaren väljer själv om hen plockar upp det (j/n).
    // Säger man nej ligger det kvar – frågan kommer igen när man kommer tillbaka eller letar (menyval 5).
    // true om något låg i rummet.
    private boolean offerItems(Room room) {
        List<Item> items = room.getItems();
        for (Item item : items) {
            output.onEvent(AsciiArt.yellow(itemHere(item)));
            AsciiArt.forItem(item).ifPresent(art -> AsciiArt.show(output, art));   // på namnet, annars på sorten
            IO.print(ConsoleStyle.tint(AsciiArt.yellow(takeQuestion(item))));
            if (wantsIt(readln())) {
                room.removeItem(item);
                if (!(item instanceof Potion)) output.onEvent("Du plockar upp: " + item.description());
                take(item);
            } else {
                output.onEvent(leftBehind(item));
            }
        }
        return !items.isEmpty();
    }

    // Visar bilden för en fiende, sak eller person – om det finns någon
    private void showArt(String name) {
        AsciiArt.forName(name).ifPresent(art -> AsciiArt.show(output, art));
    }

    // null = behåll nuvarande attack (vapnet väljer: båge = avstånd, staff = magi, svärd = närstrid)
    // Menyn kommer från AttackMenu: med Java-boken bara Martins fyra, annars närstrid/magi/avstånd
    private AttackStrategy chooseAttack(Hero hero) {
        Enemy foe = currentFoes.stream().filter(Enemy::isAlive).findFirst().orElse(null);
        List<AttackStrategy> options = AttackMenu.options(hero, foe);
        IO.println(ConsoleStyle.prompt("Välj attack (Enter = behåll):"));
        // pilbågens och stavens attack gulmarkerade
        IO.println(ConsoleStyle.panel(ConsoleStyle.menu(String.join("\n", AttackMenu.lines(hero, options))), "Attacker"));
        IO.print(ConsoleStyle.prompt("Ditt val: "));
        return AttackMenu.choose(options, readln());
    }
}
