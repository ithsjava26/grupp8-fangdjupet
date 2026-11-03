package se.fangdjupet.game;

import java.util.function.Consumer;
import java.util.function.IntConsumer;

// Konsolen med lite rörelse (Observer, som ConsoleEventLogger): bilderna ritas fram rad för rad,
// och repliker – t.ex. Martins Java-frågor – skrivs ut bokstav för bokstav.
// Av med -Dfangdjupet.animation=av eller FANGDJUPET_ANIMATION=av (webben animerar själv).
public class AnimatedConsoleLogger implements GameEventListener {
    static final int LINE_MS = 22;   // paus efter varje rad i en bild
    static final int CHAR_MS = 14;   // paus efter varje bokstav i en replik

    private final Consumer<String> print;   // skriver utan radbrytning
    private final IntConsumer pause;        // väntar så många millisekunder

    public AnimatedConsoleLogger() {
        this(IO::print, AnimatedConsoleLogger::sleep);
    }

    // För testerna: fånga utskriften och pauserna i stället för att vänta på riktigt
    AnimatedConsoleLogger(Consumer<String> print, IntConsumer pause) {
        if (print == null || pause == null) throw new IllegalArgumentException("print and pause must not be null");
        this.print = print;
        this.pause = pause;
    }

    // Animation på, om den inte stängts av
    public static boolean enabled() {
        String off = System.getProperty("fangdjupet.animation", System.getenv("FANGDJUPET_ANIMATION"));
        return off == null || !off.equalsIgnoreCase("av");
    }

    @Override
    public void onEvent(String message) {
        if (message == null) return;
        if (message.contains("\n")) {
            // En bild: rad för rad, uppifrån och ned
            for (String line : message.split("\n", -1)) {
                print.accept(line + "\n");
                pause.accept(LINE_MS);
            }
        } else if (message.matches("[^:\"]{1,30}: \".*\"")) {
            // En replik: Namn: "text" – namnet direkt, texten bokstav för bokstav
            int start = message.indexOf('"') + 1;
            print.accept(message.substring(0, start));
            for (int i = start; i < message.length() - 1; i++) {
                print.accept(String.valueOf(message.charAt(i)));
                pause.accept(CHAR_MS);
            }
            print.accept("\"\n");
        } else {
            print.accept(message + "\n");
        }
    }

    private static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
