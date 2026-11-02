package se.fangdjupet.combat;

// Låter spelet (t.ex. menyn) välja hjältens attack inför varje runda.
// Returnerar null = behåll den attack hjälten redan har.
@FunctionalInterface
public interface AttackChooser {
    AttackStrategy choose(Hero hero);
}
