package se.fangdjupet.combat;

import se.fangdjupet.items.Potion;
import se.fangdjupet.items.Armor;
import se.fangdjupet.items.Weapon;

import java.util.Random;

// Hero är spelaren. Ärver hp, attack, heal m.m. från Character.
public class Hero extends Character {
    private static final int WEAPON_STRENGTH = 5;   // grundstyrkan när vapnet väljer attack

    private Weapon weapon;   // null = inget vapen
    private Armor armor;     // null = ingen rustning

    // Skickar vidare till Characters konstruktor
    public Hero(String name, int maxHp, AttackStrategy attack) {
        super(name, maxHp, attack);
    }

    // Tar på ett vapen (ersätter det gamla) och byter attack efter vapnets räckvidd:
    // båge = avståndsattack, svärd = närstrid, spjut (BOTH) behåller attacken man har.
    public void equip(Weapon weapon) {
        if (weapon == null)
            throw new IllegalArgumentException("weapon must not be null");
        this.weapon = weapon;
        switch (weapon.range()) {
            case RANGED -> setAttack(new RangedAttack(WEAPON_STRENGTH, new Random()));
            case MELEE  -> setAttack(new MeleeAttack(WEAPON_STRENGTH));
            case BOTH   -> { }
        }
    }

    // Tar på en rustning (ersätter den gamla)
    public void equip(Armor armor) {
        this.armor = armor;
    }

    // Attackbonus kommer från vapnet, 0 utan vapen
    @Override
    public int getAttackBonus() {
        if (weapon == null) {
            return 0;
        }
        return weapon.damageBonus();
    }

    // Försvar kommer från rustningen, 0 utan rustning
    @Override
    public int getDefense() {
        if (armor == null) {
            return 0;
        }
        return armor.defense();
    }

    public void drink(Potion potion) {
        heal(potion.healAmount());
    }
}
