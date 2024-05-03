package am.uno;

import java.io.Serializable;

public class Opponent implements Serializable {
    // Opponents should not have access to my player data

    int id;
    String username;
    int num_cards;
    int imageId;

    public Opponent(Player player) {
        this.id = player.getID();
        this.username = player.getUsername();
        this.num_cards = player.getNumCards();
        this.imageId = player.getImageId();
    }

    // SETTERS
    public void setPlayerAsOpponent(Player player) {
        this.id = player.getID();
        this.username = player.getUsername();
        this.num_cards = player.getNumCards();
        this.imageId = player.getImageId();
    }

    public void setOpponent(Opponent opponent) {
        this.id = opponent.getId();
        this.username = opponent.getUsername();
        this.num_cards = opponent.getNum_cards();
        this.imageId = opponent.getImageId();
    }

    public void setNum_cards(int num_cards) {
        this.num_cards = num_cards;
    }

    // GETTERS
    public int getId() {
        return this.id;
    }

    public int getImageId() {
        return this.imageId;
    }

    public String getUsername() {
        return this.username;
    }

    public int getNum_cards() {
        return this.num_cards;
    }
}
