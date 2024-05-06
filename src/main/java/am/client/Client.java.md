## Uno Client Code Documentation
### Table of Contents
- [Overview](#overview)
- [Initialization](#initialization)
- [Receiving Messages](#receiving-messages)
- [Sending Messages](#sending-messages)
- [Game Logic](#game-logic)
- [Utility Methods](#utility-methods)
- [Example Usage](#example-usage)

### Overview

The `Client` class represents a client that connects to a Uno server and allows a user to play the game. It handles communication with the server, updates the game state, and provides a simple interface for the user to interact with the game.

### Initialization

The `Client` class is initialized with a `Socket` and a `String` representing the username of the player.

```java
import java.net.Socket;
import java.net.SocketException;
import java.io.*;

public class Client {
    // Connection variables
    private final Socket socket;
    private ObjectInputStream inputStream;
    private final ObjectOutputStream outputStream;

    // Players info variables
    private final String username;
    protected Player player;
    private ArrayList<Opponent> opponents = new ArrayList<>();
    private Opponent playerTurn; // Whose turn it is now
    boolean saidUno = false;

    // Game variables
    boolean gameStarted = false;
    private Card lastPlayedCard = null;
    private final ArrayList<Card> cardsStack = new ArrayList<>(); // history of cards that can be stacked
                                                                  // skip, draw2, wilddraw4
    private boolean direction = true; // true = left to right
                                      // false = right to left

    private Color wildColor = null;

    public Client(Socket socket, String username) throws IOException {
        this.socket = socket;
        this.username = username;
        this.outputStream = new ObjectOutputStream(socket.getOutputStream());
    }
}
```

### Receiving Messages

The `Client` class uses a separate thread to listen for messages from the server. The thread is started when the `receiveMessageFromServer()` method is called.

```java
public void receiveMessageFromServer() {
    new Thread(() -> {
        try {
            inputStream = new ObjectInputStream(socket.getInputStream());

            // Auxiliary variables
            Message message;
            Opponent opponent = null;
            Card card;
            int idOpponent;
            int num;

            while (socket.isConnected()) {
                try {
                    // Get message
                    message = (Message) inputStream.readObject();

                    switch (message.getType()) {
                        case TEXT:
                            // Add to GUI
                            ClientController.addTextMessageFromOthers(message.getSender(), message.getText());

                            System.out.println("[CLIENT] Received TEXT message: " + message.getText());
                            break;

                        case INIT:
                            // Store the assigned Player object
                            player = message.getPlayer();

                            System.out.println("[CLIENT]: Received INIT Player object. I am " + player.getUsername() + " ID: " + player.getID());
                            break;

                        case GAME_CHOICE:
                            System.out.println("[CLIENT] Received GAME_CHOICE: " + message.getGameChoice());

                            switch (message.getGameChoice()) {
                                case DRAW_CARD:
                                    // Get card from Server
                                    card = message.getCard();

                                    // Add card to cards deck
                                    player.addCard(card);

                                    // Add to GUI
                                    ClientController.addCard(card);
                                    break;

                                case PLAY_CARD:
                                    System.out.println("[CLIENT] Received PLAY_ADD:");

                                    // Update the card on the table
                                    lastPlayedCard = message.getCard();

                                    // Get the id of the player who played the card
                                    idOpponent = message.getIdPlayerToUpdate();
                                    // Get the number of cards that player has after playing the card
                                    num = message.getNum();

                                    // Select the player from the opponents list
                                    for (int i = 0; i < opponents.size(); i++) {
                                        if (opponents.get(i).getId() == idOpponent) {
                                            opponent = opponents.get(i);
                                            opponent.setNum_cards(num);
                                            opponents.set(i, opponent);
                                            break;
                                        }
                                    }

                                    // Make sure the getUsername() method doesn't throw a NullPointerException
                                    assert opponent != null;

                                    System.out.println("[CLIENT]: " + opponent.getUsername() + " new cards: " + opponent.getNum_cards());

                                    for (Opponent player : opponents) {
                                        if (player.getId() == opponent.getId()) {
                                            // Update the opponent's info
                                            player.setOpponent(opponent);

                                            // Show in GUI
                                            ClientController.setPlayerCards(opponent);

                                            break;
                                        }
                                    }

                                    // Add to GUI
                                    ClientController.setLastPlayedCard(lastPlayedCard);

                                    // Also deselect any selected card if there were any
                                    ClientController.deselectCard();

                                    break;
                            }
                            break;


                        case GAME_UPDATE:
                            switch (message.getGameUpdate()) {
                                case FIRST_CARD:
                                    // Update the card on the table
                                    lastPlayedCard = message.getCard();

                                    // Set the game as started
                                    gameStarted = true;

                                    // Add card to GUI
                                    ClientController.setLastPlayedCard(lastPlayedCard);

                                    System.out.println("[CLIENT] Received FIRST_CARD:");
                                    break;

                                case CARD_ADD:
                                    card = message.getCard();

                                    System.out.println("[CLIENT] Received CARD_ADD: " + card.getName());

                                    // Add card to player's deck
                                    player.addCard(card);

                                    // Add card to GUI
                                    ClientController.addCard(card);

                                    break;

                                case PLAYER_LIST_INIT:
                                    // Get the opponents list
                                    opponents = message.getOpponents();

                                    // Add opponents to GUI
                                    for (Opponent player : opponents) {