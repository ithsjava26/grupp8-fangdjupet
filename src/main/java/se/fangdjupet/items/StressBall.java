package se.fangdjupet.items;

// Stressbollen som ligger gömd i Lektionssalen (Leta i rummet, spelmenyn 5).
// Hjälten har den i fickan och klämmer på den i striden: hpPerTurn hp tillbaka varje runda.
// Den går också att kasta på Martin (combat/StressBallThrow) – men då är den borta.
public record StressBall(ItemType type, String description, int hpPerTurn, int level) implements Item {

    public StressBall {
        if (hpPerTurn <= 0)
            throw new IllegalArgumentException("hpPerTurn must be positive");
    }

    @Override
    public String info() {
        return "Description: " + description + ". Heal per turn: " + hpPerTurn;
    }
}
