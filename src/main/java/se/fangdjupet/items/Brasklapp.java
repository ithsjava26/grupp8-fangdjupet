package se.fangdjupet.items;

// Brasklappen som Frida ger hjälten efter StädHäxan. Den ligger i ryggsäcken tills den används
// mot Martin (combat/BrasklappAttack), en enda gång.
public record Brasklapp(ItemType type, String description, int power, int level) implements Item {

    public Brasklapp {
        if (power <= 0)
            throw new IllegalArgumentException("power must be positive");
    }

    @Override
    public String info() {
        return "Description: " + description + ". Damage: " + power;
    }
}
