package com.satdev.rockpaperscisors.domain;

public class RockPapersScissors {
    // Static variables
    public Player p1;
    public Player p2;
    public Player[] players = new Player[2];


    static String msg1 = "Enter choice: ";

    static int limit = 3; // number of rounds
    static boolean stop = false; // set to true to stop game

    // Checks all win conditions.
    public boolean isWin(Player p1, Player p2) {
        if(p1.getChoice().equals("rock")) {
            if(p2.getChoice().equals("scissors")) {
                return true;
            } else if(p2.getChoice().equals("paper")) {
                return false;
            }
        } else if(p1.getChoice().equals("paper")) {
            if(p2.getChoice().equals("rock")) {
                return true;
            } else if(p2.getChoice().equals("scissors")) {
                return false;
            }
        } else if(p1.getChoice().equals("scissors")) {
            if(p2.getChoice().equals("paper")) {
                return true;
            } else if(p2.getChoice().equals("rock")) {
                return false;
            }
        }
        return false;
    }
}
