package am.uno;

import java.io.Serial;
import java.io.Serializable;

public class Player implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L; // ???

    int id;
    int imageId;
    String username;
    CardDeck cards;
    int leftSkipTurns; // Number of turns the player should skip

    public Player(String username, int id, int imageId) {
        this.username = username;
        this.imageId = imageId;
        this.cards = new CardDeck();
        this.id = id;
        leftSkipTurns = 0;
    }

    public void addCard(Card card) {
        cards.addCard(card);
    }

    public void showCardDeck() {
        this.cards.printCards();
        this.cards.printNumberOfCards();
    }

    public void popCard(Card card) {
        this.cards.removeCardByName(card);
    }

    public void setLeftSkipTurns(int leftSkipTurns) { this.leftSkipTurns = leftSkipTurns; }

    // GETTER
    public String getUsername() {
        return this.username;
    }

    public int getID() {
        return this.id;
    }

    public int getImageId() {
        return this.imageId;
    }

    public CardDeck getCards() {
        return this.cards;
    }

    public int getNumCards() {
        return this.cards.getSize();
    }

    public int getLeftSkipTurns() { return this.leftSkipTurns; }
}