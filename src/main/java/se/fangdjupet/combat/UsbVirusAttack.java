package se.fangdjupet.combat;

// Smyga in ett USB-minne med virus i Lektor Martins dator. Bara 20 % chans att det lyckas –
// men då tar viruset över datorn och hjälten vinner direkt (all hans hp i skada, ingen slump i skadan).
// Misslyckas det blir hjälten påkommen och Martin tar USB-minnet: ett försök per strid.
// Finns i attackmenyn bara så länge hjälten har USB-minnet kvar (AttackMenu.options).
public final class UsbVirusAttack implements AttackStrategy {
    public static final int SUCCESS_PERCENT = 20;
    public static final UsbVirusAttack PLUG_IN = new UsbVirusAttack();

    private UsbVirusAttack() {}

    // Lyckas det är skadan precis allt hp målet har kvar
    @Override
    public int calculateDamage(Character attacker, Character target) {
        return target.getHp();
    }

    // Battle varierar inte skadan med ±1 för den här attacken: målet ska alltid falla
    @Override
    public boolean instantWin() {
        return true;
    }

    // 80 % av gångerna blir hjälten påkommen
    @Override
    public int missPercent() {
        return 100 - SUCCESS_PERCENT;
    }

    // Efter försöket – lyckat eller inte – är USB-minnet borta
    @Override
    public void used(Hero hero) {
        hero.loseUsbStick();
    }

    @Override
    public String name() {
        return "Smyg in USB-minnet";
    }

    // "Robert smyger in ett USB-minne med virus i datorn hos Lektor Martin skada: 87"
    @Override
    public String verb() {
        return "smyger in ett USB-minne med virus i datorn hos";
    }

    // "Robert försöker smyga in ett USB-minne, men blir påkommen av Lektor Martin!"
    @Override
    public String missVerb() {
        return "försöker smyga in ett USB-minne, men blir påkommen av";
    }
}
