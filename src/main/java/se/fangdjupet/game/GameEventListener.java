package se.fangdjupet.game;

// Observer-mönstret: den som vill veta vad som händer i spelet implementerar det här
// och registrerar sig. Den som skickar vet inte vem som lyssnar – skärm, fil eller test.
public interface GameEventListener {
    // Anropas för varje händelse, t.ex. "Hjälten gör 7 i skada".
    void onEvent(String message);
}
