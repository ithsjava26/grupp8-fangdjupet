package se.fangdjupet;

import se.fangdjupet.game.AsciiArt;
import se.fangdjupet.game.ConsoleEventLogger;
import se.fangdjupet.game.EventBus;
import se.fangdjupet.game.FileLogger;
import se.fangdjupet.game.GameEngine;
import se.fangdjupet.sound.RetroSpeaker;
import se.fangdjupet.sound.SoundListener;

import java.nio.file.Path;

// Startpunkten: visar startbilden och kör spelet.
public class Main {
    public static void main(String[] args) {
        // Startbilden i ren ASCII – "å" blir "?" i Windows-konsolen
        AsciiArt.show(new ConsoleEventLogger(), AsciiArt.TITLE);

        // EventBus skickar varje händelse vidare till alla lyssnare – konsolen och loggfilen (Observer)
        EventBus eventBus = new EventBus();
        eventBus.register(new ConsoleEventLogger());
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
