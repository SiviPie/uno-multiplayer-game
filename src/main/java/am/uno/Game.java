package am.uno;

import java.util.ArrayList;
import java.util.Random;

public class Game {
    private final CardDeck cardDeck;

    /*** CONSTRUCTORS ***/

    public Game() {
        this.cardDeck = new CardDeck();

        // Populate
        cardDeck.populate();
    }

    /*** Other methods ***/

    public Card popRandomCard() {
        Random random = new Random();
        int randomIndex = random.nextInt(cardDeck.getSize());

        Card card = cardDeck.getCard(randomIndex);
        cardDeck.removeCardByName(card);

        return card;
    }

    public void addCard(Card card) {
        this.cardDeck.addCard(card);
    }

}