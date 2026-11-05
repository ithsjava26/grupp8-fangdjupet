package se.fangdjupet;

import se.fangdjupet.game.AnimatedConsoleLogger;
import se.fangdjupet.game.AsciiArt;
import se.fangdjupet.game.ConsoleEventLogger;
import se.fangdjupet.game.EventBus;
import se.fangdjupet.game.FileLogger;
import se.fangdjupet.game.GameEngine;
import se.fangdjupet.game.GameEventListener;
import se.fangdjupet.sound.RetroSpeaker;
import se.fangdjupet.sound.SoundListener;

import java.nio.file.Path;

// Startpunkten: visar startbilden och kör spelet.
public class Main {
    public static void main(String[] args) {
        // Konsolen: bilderna ritas fram rad för rad, repliker skrivs bokstav för bokstav och striden får små
        // filmer på en rad – om inte animationen är avstängd (-Dfangdjupet.animation=av, som webben gör)
        AnimatedConsoleLogger animated = AnimatedConsoleLogger.enabled() ? new AnimatedConsoleLogger() : null;
        se.fangdjupet.game.ConsoleStyle.enable(animated != null);   // tydligare grafik med färger, bara i en riktig konsol
        // Webbens färger om terminalen klarar dem, annars de 16 vanliga (-Dfangdjupet.farger=24 eller 16 bestämmer)
        se.fangdjupet.game.ConsoleStyle.trueColor(animated != null && se.fangdjupet.game.ConsoleStyle.supportsTrueColor(
                System::getenv, System.getProperty("fangdjupet.farger")));
        GameEventListener console = animated != null ? animated : new ConsoleEventLogger();
        AsciiArt.show(console, AsciiArt.TITLE);

        // EventBus skickar varje händelse vidare till alla lyssnare – ljudet, konsolen och loggfilen (Observer)
        EventBus eventBus = new EventBus();
        GameEngine game = new GameEngine(eventBus);

        //Observer är grundidén: spelet skickar bara text med onEvent(...) och vet inte vem som lyssnar. Därför kan testerna läsa samma händelser som konsolen visar.

        // Musik och ljud: först på bussen, så att smällen hörs när filmen i konsolen börjar.
        // Finns inget ljud (eller -Dfangdjupet.ljud=av) blir det tyst.
        RetroSpeaker.open().ifPresent(speaker -> {
            SoundListener sound = new SoundListener(speaker, game::heroAttack);
            eventBus.register(sound);
            game.onInput(sound::keyPressed);
        });
        if (animated != null) animated.followAttack(game::heroAttack).followHero(game::heroName);   // pilen, magin och stridspanelen
        eventBus.register(console);
        eventBus.register(new FileLogger(Path.of("fangdjupet.log")));
        game.run();
    }
}
