package se.fangdjupet.game;

import se.fangdjupet.combat.Enemy;
import se.fangdjupet.combat.Frida;
import se.fangdjupet.items.Item;
import se.fangdjupet.items.Potion;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// Ett rum i spelet: namn, beskrivning, fiender i vågor och föremål.
// Med with...-metoderna läggs extra saker till: kaffe, kanelbulle, Frida och Java-boken.
public class Room {
    private final String name;
    private final String description;
    // Fienderna kommer i vågor: nästa våg dyker upp när den förra är besegrad.
    // En våg kan vara en fiende eller flera samtidigt (trollet och trollkarlen).
    private final List<List<Enemy>> waves = new ArrayList<>();
    private final List<Item> items;
    private final List<Item> hidden = new ArrayList<>();   // hittas bara om man letar (spelmenyn 5)
    private Frida frida;                 // dyker upp när rummets fiender är besegrade, null = ingen
    private Refreshment refreshment;     // något att dricka mellan vågorna, null = inget (eller redan drucket)
    private Refreshment reward;          // något att äta när rummet är rensat, null = inget (eller redan uppätet)
    private boolean javaBook;            //true = boken ligger kvar i rummet

    // Det man hittar i rummet, t.ex. kaffe: en replik och en dryck som helar
    public record Refreshment(String line, Potion potion) {
    }

    // Ett rum med en fiende (eller ingen, null)
    public Room(String name, String description, Enemy enemy, List<Item> items) {
        this(name, description, enemy == null ? List.of() : List.of(enemy), items);
    }

    // Ett rum med flera fiender i rad, t.ex. JavaFläcken, sedan Troll, sedan Trollkarl
    public Room(String name, String description, List<Enemy> enemies, List<Item> items) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("description must not be blank");
        }

        this.name = name;
        this.description = description;
        for (Enemy enemy : enemies) {
            waves.add(List.of(enemy));              // varje fiende i listan är en egen våg
        }
        this.items = new ArrayList<>(items);
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    // Lägger till en våg med flera fiender som slåss samtidigt
    public Room thenTogether(Enemy... together) {
        waves.add(List.of(together));
        return this;
    }

    // Fienderna som lever i den första vågen som inte är besegrad – tom när rummet är rensat
    public List<Enemy> getEnemies() {
        for (List<Enemy> wave : waves) {
            List<Enemy> alive = wave.stream().filter(Enemy::isAlive).toList();
            if (!alive.isEmpty()) return alive;
        }
        return List.of();
    }

    // Första fienden som lever, eller null när rummet är rensat
    public Enemy getEnemy() {
        List<Enemy> current = getEnemies();
        return current.isEmpty() ? null : current.get(0);
    }

    // Något att dricka när första vågen är besegrad, t.ex. kaffet i Kafferummet
    public Room withRefreshment(String line, Potion potion) {
        this.refreshment = new Refreshment(line, potion);
        return this;
    }

    // Belöningen när alla fiender i rummet är besegrade, t.ex. en kanelbulle
    public Room withReward(String line, Potion potion) {
        this.reward = new Refreshment(line, potion);
        return this;
    }

    // Lägger Java-boken i rummet (Lektionssalen)
    public Room withJavaBook() {
        this.javaBook = true;
        return this;
    }

    // Tar Java-boken: true första gången, sedan false – boken är redan tagen
    public boolean takeJavaBook() {
        boolean wasHere = javaBook;   // spara först...
        javaBook = false;             // ...sedan är boken tagen
        return wasHere;
    }

    // Tar belöningen – bara en gång, sedan är den slut
    public Optional<Refreshment> takeReward() {
        Optional<Refreshment> taken = Optional.ofNullable(reward);
        reward = null;
        return taken;
    }

    // Tar förfriskningen – bara en gång, sedan är den slut
    public Optional<Refreshment> takeRefreshment() {
        Optional<Refreshment> taken = Optional.ofNullable(refreshment);
        refreshment = null;
        return taken;
    }

    // Gömmer ett föremål i rummet. Det plockas inte upp när man kommer in – man måste leta (spelmenyn 5).
    public Room withHiddenItem(Item item) {
        if (item == null) throw new IllegalArgumentException("item must not be null");
        hidden.add(item);
        return this;
    }

    // Letar i rummet: ger allt som var gömt, bara en gång – sedan finns inget mer att hitta
    public List<Item> search() {
        List<Item> found = List.copyOf(hidden);
        hidden.clear();
        return found;
    }

    // Finns det något att hitta här? Något som ligger kvar, eller något gömt (då gulmarkeras "Leta i rummet")
    public boolean hasSomethingToFind() {
        return !items.isEmpty() || !hidden.isEmpty();
    }

    // En kopia – listan i rummet ändras bara via removeItem
    public List<Item> getItems() {
        return List.copyOf(items);
    }

    // true så länge någon våg har en fiende som lever
    public boolean hasLivingEnemy() {
        return getEnemy() != null;
    }

    // Ställer Frida i rummet, t.ex. new Room(...).withFrida(Frida.create())
    public Room withFrida(Frida frida) {
        this.frida = frida;
        return this;
    }

    // Frida om hon finns i rummet, annars tomt
    public Optional<Frida> getFrida() {
        return Optional.ofNullable(frida);
    }

    // Lämnar något i rummet, t.ex. kaffet som hjälten tackade nej till – det erbjuds igen senare
    public void leaveItem(Item item) {
        if (item == null) throw new IllegalArgumentException("item must not be null");
        items.add(item);
    }

    // Hjälten har redan föremålet från en sparning, så det ska varken ligga här eller vara gömt här.
    // Vi jämför namnet: bonusen slumpas när världen byggs, så det nya föremålet är inte lika med det sparade.
    public void forget(Item item) {
        items.removeIf(here -> here.description().equals(item.description()));
        hidden.removeIf(here -> here.description().equals(item.description()));
    }

    // Tar bort föremålet från rummet – true om det fanns där
    public boolean removeItem(Item item) {
        return items.remove(item);
    }
}