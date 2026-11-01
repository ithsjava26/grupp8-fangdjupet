package se.fangdjupet.game;

public record GameState(String heroName, int heroMaxHp, int heroHp, int nextRoom) {


    public GameState {
        if (heroName == null || heroName.isBlank()) {
            throw new IllegalArgumentException("Hjältens namn får inte vara tomt");
        }
        if (heroMaxHp <= 0) {
            throw new IllegalArgumentException("Hjältens maxhälsa måste vara större än 0");
        }
        if (heroHp < 0 || heroHp > heroMaxHp) {
            throw new IllegalArgumentException(
                    "Hjältens hälsa måste vara mellan 0 och hjältens maxhälsa");
        }
        if (nextRoom < 0) {
            throw new IllegalArgumentException("Nästa rum får inte vara ett negativt tal");
        }
    }
}
