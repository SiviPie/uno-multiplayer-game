package am.uno;

import java.io.Serial;
import java.io.Serializable;

public class Player implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L; // ???

    int id;
    String username;
    CardDeck cards;

    public Player(String username, int id) {
        this.username = username;
        this.cards = new CardDeck();
        this.id = id;
    }

    public void addCard(Card card) {
        cards.addCard(card);
    }

    public void showCardDeck() {
        this.cards.printCards();
        this.cards.printNumberOfCards();
    }

    // GETTER
    public String getUsername() {
        return this.username;
    }

    public int getID() {
        return this.id;
    }
}