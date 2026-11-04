package se.fangdjupet.game;

import se.fangdjupet.items.Item;

import java.util.List;

// items = ryggsäcken i den ordning hjälten fick sakerna. Tom i en gammal sparfil från före ryggsäcken.
public record GameState(String heroName, int heroMaxHp, int heroHp, int nextRoom, List<Item> items) {

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
        if (items == null) {
            throw new IllegalArgumentException("Föremålen får inte vara null");
        }
        items = List.copyOf(items);
    }

    // Utan föremål, som en gammal sparfil
    public GameState(String heroName, int heroMaxHp, int heroHp, int nextRoom) {
        this(heroName, heroMaxHp, heroHp, nextRoom, List.of());
    }
}
