package se.fangdjupet;

import se.fangdjupet.game.AsciiArt;
import se.fangdjupet.game.ConsoleEventLogger;
import se.fangdjupet.game.GameEngine;

public class Main {
    public static void main(String[] args) {
        // Startbilden i ren ASCII – "å" blir "?" i Windows-konsolen
        AsciiArt.show(new ConsoleEventLogger(), AsciiArt.TITLE);
        // C byter till: new Game().run();

        GameEngine game = new GameEngine(System.out::println);
        game.run();
    }
}
