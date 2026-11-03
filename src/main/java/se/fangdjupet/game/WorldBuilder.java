package se.fangdjupet.game;

import se.fangdjupet.items.Armor;
import se.fangdjupet.items.ItemFactory;
import se.fangdjupet.items.ItemType;
import se.fangdjupet.items.Potion;
import se.fangdjupet.items.StressBall;

import se.fangdjupet.combat.Enemy;
import se.fangdjupet.combat.Frida;
import se.fangdjupet.combat.MagicAttack;
import se.fangdjupet.combat.MeleeAttack;

import java.util.List;

// Bygger spelvärlden (Fredrik): de fyra rummen i rad, med fiender, föremål och extra saker.
// Skoldörren → Kafferummet → Korridoren och städskrubben → Lektionssalen.
public class WorldBuilder {

    // Rustningen som ligger gömd i städskrubben – namnet används också för bilden i AsciiArt
    public static final String STADRUSTNING = "Städrustning";
    public static final String STRESSBOLL = "Stressboll";

    public World build(
            Enemy doorEnemy,
            Enemy corridorEnemy,
            Enemy coffeeEnemy,
            Enemy finalEnemy

    ) {
        // StädHäxan hotar när man är på väg mot lektionssalen
        if (corridorEnemy != null && corridorEnemy.getLine().isEmpty()) {
            corridorEnemy.withLine("Ett steg till mot lektionssalen och jag tar fram kemikalierna!");
        }

        Room door = new Room(
                "Skoldörren",
                "Du står utanför skolan.",
                doorEnemy,
                List.of()
        );

        // De riktiga fienderna: trollet och trollkarlen gömmer sig i kafferummet och
        // hoppar fram tillsammans när JavaFläcken är besegrad. Trollkarlen använder magi.
        Enemy troll = new Enemy("Troll", 12, new MeleeAttack(7), 2);
        Enemy wizard = new Enemy("Trollkarl", 16, new MagicAttack(6), 2)
                .withLine("Thou shall not pass!");
        Room coffeeRoom = new Room(
                "Kafferummet",
                "Det luktar kaffe.",
                coffeeEnemy,
                List.of(ItemFactory.createWeapon(ItemType.BOW, "Pilbåge", 1))
        )
                // Efter JavaFläcken: en kaffe (+30 hp), sedan trollet och trollkarlen samtidigt
                .withRefreshment("Du ser dig om efter en kaffe... Där! Kannan på bänken är fortfarande varm.",
                        new Potion(ItemType.POTION, "en kopp kaffe", 30, 1))
                .thenTogether(troll, wizard)
                // När trollet och trollkarlen är besegrade: en kanelbulle (+35 hp) inför StädHäxan
                .withReward("På bänken bredvid kannan ligger en kanelbulle. Den har ingen tagit!",
                        new Potion(ItemType.POTION, "en kanelbulle", 35, 1));

        Room corridor = new Room(
                "Korridoren och städskrubben",
                "Korridoren känns lång.",
                corridorEnemy,
                List.of(ItemFactory.createWeapon(ItemType.STAFF, "Trollstav", 1))
        ).withFrida(Frida.create())    // Frida dyker upp när StädHäxan är besegrad
         // Gömd i städskrubben: hittas bara om man letar (spelmenyn 5). ItemType har ingen egen
         // rustningssort, så SHIELD används. +20 försvar: närstrid och StädHäxans kast gör nästan
         // ingen skada – men Martins kunskap går rakt igenom rustningen.
         .withHiddenItem(new Armor(ItemType.SHIELD, STADRUSTNING, 20, 2));

        Room classroom = new Room(
                "Lektionssalen",
                "Du har nått fram. Framför dig väntar dagens stora test.",
                finalEnemy,
                List.of()
        ).withJavaBook()    // hjälten tar upp Java-boken när hen kommer in
         // Gömd i Lektionssalen: hittas bara om man letar (spelmenyn 5). +5 hp varje runda i striden mot Martin,
         // eller kasta den på honom (10 i skada) – men då är den borta.
         .withHiddenItem(new StressBall(ItemType.STRESSBALL, STRESSBOLL, 5, 3));

        return new World(List.of(door, coffeeRoom, corridor, classroom));
    }
}