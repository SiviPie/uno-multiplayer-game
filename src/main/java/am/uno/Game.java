package am.uno;

import java.util.ArrayList;
import java.util.Random;

public class Game {
    private ArrayList<Player> players;
    private Player currentPlayer;
    private Boolean direction; // True = iterate players from 0 to n, False = reverse
    private CardDeck cardDeck;
    private ArrayList<Card> currentCards; // Last (relevant) played cards

    /*** CONSTRUCTORS ***/

    public Game() {
        this.players = new ArrayList<Player>();
        this.currentPlayer = null;
        this.direction = true;
        this.cardDeck = new CardDeck();
        this.currentCards = new ArrayList<Card>();

        // Populate
        cardDeck.populate();
        currentCards.add(this.popRandomCard());

        // If wild, choose random color
        if ((this.getLastCardPlayed().getType() == CardType.Wild) || (this.getLastCardPlayed().getType() == CardType.WildDraw4)) {
            // Generate a random index
            Random random = new Random();
            int randomIndex = random.nextInt(Color.values().length);

            // Get the random color
            Color randomColor = Color.values()[randomIndex];

            // Set the random color
            currentCards.get(0).setColor(randomColor);
        }
    }

    /*** SETTERS ***/

    public void setPlayers(ArrayList<Player> players) {
        this.players = players;
    }

    public void setCardDeck(CardDeck cardDeck) {
        this.cardDeck = cardDeck;
    }

    /*** GETTERS ***/

    public ArrayList<Player> getPlayers() {
        return this.players;
    }

    public Player getPlayer(String username) {
        // TODO
        throw new UnsupportedOperationException("Method not yet implemented");

    }

    public Player getPlayer(int index) {
        // TODO
        throw new UnsupportedOperationException("Method not yet implemented");
    }

    public int getNumberOrPlayers() {
        return this.players.size();
    }

    public CardDeck getCardDeck() {
        return this.cardDeck;
    }

    public Card getLastCardPlayed() {
        return this.currentCards.get(this.currentCards.size() - 1);
    }

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

    public void showCardDeck() {
        this.cardDeck.printCards();
        this.cardDeck.printNumberOfCards();
    }

    public void addPlayer(Player player) {
        players.add(player);
    }

    public void removePlayer(Player player) {
        players.remove(player);
    }

    public void giveRandomCardToPlayer(Player player) {
        Card card = this.popRandomCard();
        player.addCard(card);
    }

}