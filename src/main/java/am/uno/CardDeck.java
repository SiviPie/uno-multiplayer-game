package am.uno;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;

public class CardDeck implements Serializable {
    @Serial
    private static final long serialVersionUID = 2L;

    private ArrayList<Card> cards;

    public CardDeck() {
        cards = new ArrayList<Card>();
    }

    public void addCard(Card card) {
        this.cards.add(card);
    }

    public void populate() {

        for (Color color : Color.values()) {

            // add Number cards - 19 number cards (1 zero and 2 each of one through nine)
            // add 1 zero card
            this.addCard(new Card(CardType.Number, color, 0));

            // add 2 of each other number
            for (int i = 1; i <= 9; i++) {
                this.addCard(new Card(CardType.Number, color, i));
                this.addCard(new Card(CardType.Number, color, i));
            }

            // add Reverse cards - 2 for each color
            this.addCard(new Card(CardType.Reverse, color));
            this.addCard(new Card(CardType.Reverse, color));

            // add Skip cards - 2 for each color
            this.addCard(new Card(CardType.Skip, color));
            this.addCard(new Card(CardType.Skip, color));

            // add Draw2 cards - 2 for each color
            this.addCard(new Card(CardType.Draw2, color));
            this.addCard(new Card(CardType.Draw2, color));
        }

        // add Wild card - 4 in total
        for (int i = 0; i < 4; i++) {
            this.addCard(new Card(CardType.Wild));
        }

        // add Wild Draw4 card - 4 in total
        for (int i = 0; i < 4; i++) {
            this.addCard(new Card(CardType.WildDraw4));
        }
    }

    public Card getCard(int index) {
        return this.cards.get(index);
    }

    public int getSize() {
        return this.cards.size();
    }

    public void removeCardByName(Card card) {
        for (Card card_aux : cards) {
            if (card_aux.getName().equals(card.getName())) {
                this.cards.remove(card_aux);
                break;
            }
        }

    }

    public void printCards() {
        for (Card card : cards) {
            card.showInfo();
        }
    }

    public void printNumberOfCards() {
        System.out.println(cards.size());
    }
}