package am.uno;

import java.io.Serial;
import java.io.Serializable;

// Opponents should not have access to player's cards
public class Opponent implements Serializable {
    @Serial
    private static final long serialVersionUID = 3L;

    String username;
    int id;
    int num_cards;
    int imageId;
    int leftSkipTurns;

    public Opponent(Player player) {
        this.id = player.getID();
        this.username = player.getUsername();
        this.num_cards = player.getNumCards();
        this.imageId = player.getImageId();
        this.leftSkipTurns = player.getLeftSkipTurns();
    }

    // SETTERS
    public void setPlayerAsOpponent(Player player) {
        this.id = player.getID();
        this.username = player.getUsername();
        this.num_cards = player.getNumCards();
        this.imageId = player.getImageId();
        this.leftSkipTurns = player.getLeftSkipTurns();
    }

    public void setOpponent(Opponent opponent) {
        this.id = opponent.getId();
        this.username = opponent.getUsername();
        this.num_cards = opponent.getNum_cards();
        this.imageId = opponent.getImageId();
        this.leftSkipTurns = opponent.getLeftSkipTurns();
    }

    public void setNum_cards(int num_cards) {
        this.num_cards = num_cards;
    }

    public void setLeftSkipTurns(int leftSkipTurns) {
        this.leftSkipTurns = leftSkipTurns;
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

    public int getLeftSkipTurns() {
        return this.leftSkipTurns;
    }
}
