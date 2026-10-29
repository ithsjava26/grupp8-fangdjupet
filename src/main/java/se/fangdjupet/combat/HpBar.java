package se.fangdjupet.combat;

// HP-mätare i ren ASCII för stridsloggen, t.ex. "Aragorn  [#######---] 28/40".
// Bygger bara på Character, så den fungerar för Hero, Enemy och en kommande Boss.
public final class HpBar {

    private HpBar() {}    // bara statiska metoder, ska inte instansieras

    // Själva stapeln: width tecken mellan hakparenteserna, "#" för liv och "-" för tappat liv.
    public static String render(Character c, int width) {
        if (c == null) throw new IllegalArgumentException("character must not be null");
        if (width < 1) throw new IllegalArgumentException("width must be at least 1");

        int filled = (int) ((long) c.getHp() * width / c.getMaxHp());
        if (c.isAlive() && filled == 0) filled = 1;     // lite liv kvar ska synas, inte se dött ut
        return "[" + "#".repeat(filled) + "-".repeat(width - filled) + "]";
    }

    // En hel rad med namn, stapel och siffror – det som skickas till loggen.
    public static String status(Character c, int width) {
        return c.getName() + "  " + render(c, width) + " " + c.getHp() + "/" + c.getMaxHp();
    }
}
