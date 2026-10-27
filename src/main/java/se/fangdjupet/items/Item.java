package se.fangdjupet.items;

// sealed … permits = BARA de tre uppräknade typerna får vara ett Item. En fjärde stoppas av
// kompilatorn, och en switch över Item vet att den täcker alla fall.
public sealed interface Item permits Weapon, Armor, Potion {
    String name();      // alla föremål har ett namn …
    int level();        // … och en nivå (avgör hur bra det är och var det dyker upp)
}
