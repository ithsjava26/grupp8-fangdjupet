package se.fangdjupet;

import se.fangdjupet.game.AsciiArt;
import se.fangdjupet.game.ConsoleEventLogger;
import se.fangdjupet.game.EventBus;
import se.fangdjupet.game.FileLogger;
import se.fangdjupet.game.GameEngine;

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
        game.run();
    }
}
