package com.satdev.rockpaperscisors.domain;

public class Player {
    private String name;
    private String choice;
    private int roundsWon;

    public Player(String n) {
        name = n;
        roundsWon = 0;
    }

    // Get Player name.
    public String getName() {
        return name;
    }

    // Get Player choice.
    public String getChoice() {
        return choice;
    }

    // Get roundsWon.
    public int getRoundsWon() {
        return roundsWon;
    }

    // Set Player name.
    public void setName(String n) {
        name = n.toLowerCase();
    }

    // Set Player choice.
    public void setChoice(String c) {
        choice = c.toLowerCase();
    }

    // Set roundsWon.
    public void setRoundsWon(int rw) {
        roundsWon = rw;
    }

    // Increment RoundsWon.
    public void incrementRoundsWon() {
        roundsWon++;
    }



}
