package am.uno;

import java.io.Serializable;

public class Opponent implements Serializable {
    // Opponents should not have access to my player data

    int id;
    String username;
    int num_cards;

    public Opponent(Player player) {
        this.id = player.getID();
        this.username = player.getUsername();
        this.num_cards = player.cards.getSize();
    }

    // SETTERS
    public void setPlayerAsOpponent(Player player) {
        this.id = player.getID();
        this.username = player.getUsername();
        this.num_cards = player.cards.getSize();
    }

    // GETTERS
    public int getId() {
        return this.id;
    }

    public String getUsername() {
        return this.username;
    }

    public int getNum_cards() {
        return this.num_cards;
    }
}
