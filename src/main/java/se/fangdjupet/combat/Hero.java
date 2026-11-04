package se.fangdjupet.combat;

import se.fangdjupet.items.Potion;
import se.fangdjupet.items.Armor;
import se.fangdjupet.items.Brasklapp;
import se.fangdjupet.items.ItemType;
import se.fangdjupet.items.Inventory;
import se.fangdjupet.items.Item;
import se.fangdjupet.items.StressBall;
import se.fangdjupet.items.Weapon;
import se.fangdjupet.items.WeaponRange;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Random;

// Hero är spelaren. Ärver hp, attack, heal m.m. från Character.
public class Hero extends Character {
    private static final int WEAPON_STRENGTH = 5;   // grundstyrkan när vapnet väljer attack
    private static final int BACKPACK_SIZE = 10;    // spelet har fem saker som hamnar här (drycker dricks direkt), så ryggsäcken räcker

    private Weapon weapon;   // null = inget vapen
    private Armor armor;     // null = ingen rustning
    private boolean javaBook; //Plocka upp boken i sista rummet
    private StressBall stressBall;   // null = ingen stressboll i fickan
    private Brasklapp brasklapp;     // null = ingen brasklapp, eller redan använd
    private boolean usbStick = true; // USB-minnet med virus: ett försök mot Martin, sedan är det borta
    private final EnumSet<WeaponRange> pickedUp = EnumSet.noneOf(WeaponRange.class);   // räckvidderna hjälten har haft i handen
    private final JavaBookAttack bookAttack = new JavaBookAttack();   // citera ur boken: kapitel 18, sedan 20 ...
    private final Inventory inventory = new Inventory(BACKPACK_SIZE);   // ryggsäcken: allt hjälten bär med sig

    // Skickar vidare till Characters konstruktor
    public Hero(String name, int maxHp, AttackStrategy attack) {
        super(name, maxHp, attack);
    }

    // Tar på ett vapen (ersätter det gamla) och byter attack efter vapnets räckvidd:
    // båge = avståndsattack, staff = magi, svärd = närstrid, spjut (BOTH) behåller attacken man har.
    public void equip(Weapon weapon) {
        if (weapon == null)
            throw new IllegalArgumentException("weapon must not be null");
        this.weapon = weapon;
        keep(weapon);   // ett vapen som byts ut ligger kvar i ryggsäcken
        pickedUp.add(weapon.range());   // låser upp attacken: pilbågen avstånd, staven magi
        switch (weapon.range()) {
            case RANGED -> setAttack(new RangedAttack(WEAPON_STRENGTH, new Random()));
            case MELEE  -> setAttack(new MeleeAttack(WEAPON_STRENGTH));
            case MAGIC  -> setAttack(new MagicAttack(WEAPON_STRENGTH));
            case BOTH   -> { }
        }
    }

    // Tar på en rustning (ersätter den gamla)
    public void equip(Armor armor) {
        this.armor = armor;
        if (armor != null) keep(armor);
    }

    // Ryggsäcken med allt hjälten har plockat upp (visas i "Visa hjältens utrustning")
    public Inventory getInventory() {
        return inventory;
    }

    // Lägger föremålet i ryggsäcken, en gång – det som redan finns där läggs inte till igen.
    // Det hjälten bär ska alltid synas där, så en full ryggsäck växer i stället för att tappa föremålet.
    private void keep(Item item) {
        if (inventory.getAll().contains(item)) {
            return;
        }
        if (inventory.isFull()) {
            inventory.setCapacity(inventory.size() + 1);
        }
        inventory.add(item);
    }

    // Attackbonus kommer från vapnet – men bara när attacken passar vapnet:
    // pilbågen hjälper avståndsattacken, svärdet närstriden, spjutet båda. Knytnävarna blir inte bättre av en pilbåge.
    // 0 utan vapen. (Magi räknar inte bonus – staven gör magin starkare i AttackMenu i stället.)
    @Override
    public int getAttackBonus() {
        if (weapon == null || !suits(weapon.range(), getAttack())) {
            return 0;
        }
        return weapon.damageBonus();
    }

    // Passar attacken vapnets räckvidd?
    static boolean suits(WeaponRange range, AttackStrategy attack) {
        return switch (range) {
            case MELEE -> attack instanceof MeleeAttack;
            case RANGED -> attack instanceof RangedAttack;
            case MAGIC -> attack instanceof MagicAttack;
            case BOTH -> attack instanceof MeleeAttack || attack instanceof RangedAttack;
        };
    }

    // Håller hjälten ett vapen med den räckvidden? T.ex. pilbågen = RANGED
    public boolean holds(WeaponRange range) {
        return weapon != null && weapon.range() == range;
    }

    // Har hjälten någon gång haft ett vapen med den räckvidden i handen? Då finns attacken i attackmenyn,
    // även när ett annat vapen har tagit dess plats (fast svagare utan vapnet).
    public boolean hasPickedUp(WeaponRange range) {
        return pickedUp.contains(range);
    }

    // Ullvantarna från Entrédörren: så mycket mindre tar Martins förklaringar. De räknas från ryggsäcken,
    // så de hjälper även när Städrustningen har tagit rustningens plats.
    public static final int GLOVE_VS_KNOWLEDGE = 2;

    // Så mycket mer hälsa ullvantarna ger: 40 blir 50
    public static final int GLOVE_HP = 10;

    // Bara när ullvantarna plockas upp. En sparfil har redan det höjda maxHp, så equip får inte göra det här.
    public void warmUp() {
        raiseMaxHp(GLOVE_HP);
    }

    @Override
    public int getKnowledgeDefense() {
        return inventory.findByType(ItemType.GLOVE).isEmpty() ? 0 : GLOVE_VS_KNOWLEDGE;
    }

    // Försvar kommer från rustningen, 0 utan rustning
    @Override
    public int getDefense() {
        if (armor == null) {
            return 0;
        }
        return armor.defense();
    }

    // Dricker en dryck och helar så mycket den ger (aldrig över max)
    public void drink(Potion potion) {
        heal(potion.healAmount());
    }

    // Vapnet i handen, tomt utan vapen (visas i "Visa hjältens utrustning")
    public Optional<Weapon> getWeapon() {
        return Optional.ofNullable(weapon);
    }

    // Rustningen, tom utan rustning
    public Optional<Armor> getArmor() {
        return Optional.ofNullable(armor);
    }

    // Har hjälten tagit upp Java-boken? Då väljer hen bara bland KnowledgeAttack mot Martin.
    // Attacken ur Java-boken (bläddrar vidare för varje citat). Finns i attackmenyn när hjälten har boken.
    public JavaBookAttack getJavaBookAttack() {
        return bookAttack;
    }

    public boolean hasJavaBook() {
        return javaBook;
    }

    // Brasklappen från Frida, tills den är använd mot Martin
    public void takeBrasklapp(Brasklapp note) {
        if (note == null)
            throw new IllegalArgumentException("note must not be null");
        brasklapp = note;
        keep(note);
    }

    public Optional<Brasklapp> getBrasklapp() {
        return Optional.ofNullable(brasklapp);
    }

    // DROP TABLE är uppläst: brasklappen är borta. Nästa runda (Enter = behåll) citerar hjälten ur boken i stället.
    void useBrasklapp() {
        if (brasklapp != null) inventory.remove(brasklapp);
        brasklapp = null;
        if (getAttack() instanceof BrasklappAttack) setAttack(javaBook ? bookAttack : KnowledgeAttack.STACKOVERFLOW);
    }

    // Stressbollen i fickan (från Leta i Lektionssalen)
    public void pickUp(StressBall ball) {
        if (ball == null)
            throw new IllegalArgumentException("ball must not be null");
        stressBall = ball;
        keep(ball);
    }

    public Optional<StressBall> getStressBall() {
        return Optional.ofNullable(stressBall);
    }

    // Battle klämmer bollen i början av varje runda: +hpPerTurn hp (aldrig över maxHp). Ger hur mycket som kom tillbaka.
    public int squeezeStressBall() {
        if (stressBall == null || !isAlive()) return 0;
        int before = getHp();
        heal(stressBall.hpPerTurn());
        return getHp() - before;
    }

    // Har hjälten kvar USB-minnet? Ett försök per äventyr (UsbVirusAttack)
    public boolean hasUsbStick() {
        return usbStick;
    }

    // Försöket är gjort: USB-minnet är borta. Nästa runda (Enter = behåll) citerar hjälten ur boken i stället.
    // Efter en attack som bara används en gång (lavalampan): tillbaka till boken, eller StackOverflow utan bok
    void stopUsing(AttackStrategy attack) {
        if (getAttack() == attack) setAttack(javaBook ? bookAttack : KnowledgeAttack.STACKOVERFLOW);
    }

    void loseUsbStick() {
        usbStick = false;
        if (getAttack() instanceof UsbVirusAttack) setAttack(javaBook ? bookAttack : KnowledgeAttack.STACKOVERFLOW);
    }

    // Bollen är kastad på Martin: borta ur fickan. Nästa runda (Enter = behåll) citerar hjälten ur boken i stället.
    void throwAwayStressBall() {
        if (stressBall != null) inventory.remove(stressBall);
        stressBall = null;
        if (getAttack() instanceof StressBallThrow) setAttack(javaBook ? bookAttack : KnowledgeAttack.STACKOVERFLOW);
    }

    // Tar upp Java-boken i Lektionssalen och börjar med StackOverflow – som när en båge
    // byter till RangedAttack. Två gånger gör ingen skillnad: fältet är redan true.
    public void pickUpJavaBook() {
        javaBook = true;
        setAttack(KnowledgeAttack.STACKOVERFLOW);
    }
}
