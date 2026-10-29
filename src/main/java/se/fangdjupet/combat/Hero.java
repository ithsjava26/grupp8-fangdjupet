package se.fangdjupet.combat;

import se.fangdjupet.items.Potion;
import se.fangdjupet.items.Armor;
import se.fangdjupet.items.Weapon;

// Hero är spelaren. Ärver hp, attack, heal m.m. från Character.
public class Hero extends Character {
    private Weapon weapon;   // null = inget vapen
    private Armor armor;     // null = ingen rustning

    // Skickar vidare till Characters konstruktor
    public Hero(String name, int maxHp, AttackStrategy attack) {
        super(name, maxHp, attack);
    }

    // Tar på ett vapen (ersätter det gamla)
    public void equip(Weapon weapon) {
        this.weapon = weapon;
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
