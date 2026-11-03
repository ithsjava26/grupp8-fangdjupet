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
        // Konsolen: bilderna ritas fram rad för rad och repliker skrivs bokstav för bokstav,
        // om inte animationen är avstängd (-Dfangdjupet.animation=av, som webben gör)
        GameEventListener console = AnimatedConsoleLogger.enabled() ? new AnimatedConsoleLogger() : new ConsoleEventLogger();
        AsciiArt.show(console, AsciiArt.TITLE);

        // EventBus skickar varje händelse vidare till alla lyssnare – konsolen och loggfilen (Observer)
        EventBus eventBus = new EventBus();
        eventBus.register(console);
        eventBus.register(new FileLogger(Path.of("fangdjupet.log")));
        GameEngine game = new GameEngine(eventBus);

        // Musik och ljud: en lyssnare till på samma EventBus. Finns inget ljud (eller -Dfangdjupet.ljud=av) blir det tyst.
        RetroSpeaker.open().ifPresent(speaker -> {
            SoundListener sound = new SoundListener(speaker, game::heroAttack);
            eventBus.register(sound);
            game.onInput(sound::keyPressed);
        });
        game.run();
    }
}
