package am.client;

import am.message.GameChoice;
import am.message.Message;
import am.message.MessageType;
import am.uno.*;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

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

    public boolean isCardPlayable(Card card) {
        // Decide if we can select the card based on the card on the table

        // Check if game hasn't started yet
        if (lastPlayedCard == null) {
            return false;
        }

        boolean isPlayable = false;

        switch (card.getType()) {
            case CardType.Number:
                isPlayable =  ((lastPlayedCard.getType() == CardType.Number && (lastPlayedCard.getNumber() == card.getNumber() || lastPlayedCard.getColor() == card.getColor()))
                        || ((lastPlayedCard.getType() == CardType.Skip) && cardsStack.isEmpty() && (lastPlayedCard.getColor() == card.getColor()))
                        || ((lastPlayedCard.getType() == CardType.Reverse) && (lastPlayedCard.getColor() == card.getColor()))
                        || ((lastPlayedCard.getType() == CardType.Wild) && (wildColor == card.getColor()))
                        || ((lastPlayedCard.getType() == CardType.Draw2) && cardsStack.isEmpty() && (lastPlayedCard.getColor() == card.getColor()))
                        || ((lastPlayedCard.getType() == CardType.WildDraw4) && cardsStack.isEmpty() && (wildColor == card.getColor())));

                break;

            case CardType.Skip:
                isPlayable = (lastPlayedCard.getType() == CardType.Skip
                        || ((lastPlayedCard.getType() == CardType.Number || lastPlayedCard.getType() == CardType.Reverse) && (lastPlayedCard.getColor() == card.getColor()))
                        || ((lastPlayedCard.getType() == CardType.Wild) && (wildColor == card.getColor()))
                        || ((lastPlayedCard.getType() == CardType.Draw2) && cardsStack.isEmpty() && (lastPlayedCard.getColor() == card.getColor()))
                        || ((lastPlayedCard.getType() == CardType.WildDraw4) && cardsStack.isEmpty() && (wildColor == card.getColor())));

                break;

            case CardType.Reverse:
                isPlayable =  (lastPlayedCard.getType() == CardType.Reverse
                        || ((lastPlayedCard.getType() == CardType.Skip) && cardsStack.isEmpty() && (lastPlayedCard.getColor() == card.getColor()))
                        || ((lastPlayedCard.getType() == CardType.Number) && (lastPlayedCard.getColor() == card.getColor()))
                        || ((lastPlayedCard.getType() == CardType.Wild) && (wildColor == card.getColor()))
                        || ((lastPlayedCard.getType() == CardType.Draw2) && cardsStack.isEmpty() && (lastPlayedCard.getColor() == card.getColor()))
                        || ((lastPlayedCard.getType() == CardType.WildDraw4) && cardsStack.isEmpty() && (wildColor == card.getColor())));

                break;

            case CardType.Draw2:
                isPlayable = (lastPlayedCard.getType() == CardType.Draw2
                        || (lastPlayedCard.getType() == CardType.WildDraw4 && (!cardsStack.isEmpty() || (wildColor == card.getColor())))
                        || ((lastPlayedCard.getType() == CardType.Wild) && (wildColor == card.getColor()))
                        || ((lastPlayedCard.getType() == CardType.Skip) && cardsStack.isEmpty() && (lastPlayedCard.getColor() == card.getColor()))
                        || ((lastPlayedCard.getType() == CardType.Number || lastPlayedCard.getType() == CardType.Reverse)
                            && (lastPlayedCard.getColor() == card.getColor())));

                break;

            case CardType.Wild:
                isPlayable = cardsStack.isEmpty();

                break;

            case CardType.WildDraw4:
                isPlayable = true;

                break;
        }

        return isPlayable;
    }

    public void sendUsername() throws IOException {
        // Send username to Server in order to be assigned a player

        try {
            // Create Message object
            Message message = new Message(MessageType.INIT);
            message.setText(this.username);

            // Send the message
            outputStream.writeObject(message);

            System.out.println("[CLIENT]: Sent INIT message.");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendTextMessage(String text) {
        // Send text message to the other players

        try {
            // Create Message object
            Message message = new Message(MessageType.TEXT);
            message.setSender(player.getUsername());
            message.setText(text);

            // Send the message
            outputStream.writeObject(message);

            // Add to GUI
            ClientController.addTextMessageFromSelf(text, username);

            System.out.println("[CLIENT]: Sent TEXT message: " + text);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void playCard(Card card, Color selectedColor) {

        if (!gameStarted) {
            return;
        }

        try {
            // Create Message object
            Message message = new Message(MessageType.GAME_CHOICE);
            message.setGameChoice(GameChoice.PLAY_CARD);
            message.setCard(card);
            message.setSaidUno(saidUno);

            // Handle Wild cards
            if (selectedColor != null) {
                message.setColor(selectedColor);
            }

            // Remove card from player's card deck
            player.popCard(card);

            // Send the message
            outputStream.writeObject(message);

            System.out.println("[CLIENT]: Sent PLAY_CARD message.");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void drawCard() {

        if (!gameStarted) {
            return;
        }

        try {
            // Create Message object
            Message message = new Message(MessageType.GAME_CHOICE);
            message.setGameChoice(GameChoice.DRAW_CARD);

            // Send the message
            outputStream.writeObject(message);

            System.out.println("[CLIENT]: Sent DRAW_CARD message.");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void skipTurn() throws IOException {

        if (!gameStarted) {
            return;
        }

        try {
            // Create message object
            Message message = new Message(MessageType.GAME_CHOICE);
            message.setGameChoice(GameChoice.SKIP_TURN);

            // Send the message
            outputStream.writeObject(message);

            System.out.println("[CLIENT]: Sent SKIP_TURN message.");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendDisconnectMessage() {
        try {
            // Create Message object
            Message message = new Message(MessageType.CLIENT_DISCONNECT);
            message.setSender(player.getUsername());

            // Send the message
            outputStream.writeObject(message);

            System.out.println("[CLIENT]: Sent DISCONNECT message.");

            this.closeEverything();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

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
                                            ClientController.addPlayerToList(player);
                                        }

                                        System.out.println("[CLIENT] Received PLAYER_LIST_INIT with " + opponents.size() + " players.");

                                        break;

                                    case PLAYER_LIST_ADD:
                                        // Get the opponent from the Server
                                        opponent = message.getOpponent();

                                        // Update opponents list
                                        opponents.add(opponent);

                                        // Update in GUI
                                        ClientController.addPlayerToList(opponent);

                                        System.out.println("[CLIENT] Received PLAYER_LIST_ADD: " + opponent.getUsername());

                                        break;

                                    case PLAYER_LIST_REMOVE:
                                        // Get the opponent from the Server
                                        opponent = message.getOpponent();

                                        // Remove player from GUI
                                        ClientController.removePlayerFromList(opponent.getId());

                                        // Remove player from opponents list
                                        opponents.remove(opponent);

                                        System.out.println("[CLIENT] Received PLAYER_LIST_REMOVE: " + message.getOpponent().getUsername());

                                        break;

                                    case PLAYER_CARDS_UPDATE:
                                        // Get the id of the player
                                        idOpponent = message.getIdPlayerToUpdate();
                                        // Get that player's updated number of cards
                                        num = message.getNum();

                                        // Select the player from the opponents list
                                        for (int i = 0; i < opponents.size(); i++) {
                                            if (opponents.get(i).getId() == idOpponent) {
                                                opponent= opponents.get(i);
                                                opponent.setNum_cards(num);
                                                opponents.set(i, opponent);
                                                break;
                                            }
                                        }

                                        // Make sure the getUsername() method doesn't throw a NullPointerException
                                        assert opponent != null;

                                        System.out.println(opponent.getUsername() + " new cards: " + opponent.getNum_cards());

                                        for (Opponent player : opponents) {
                                            if (player.getId() == opponent.getId()) {
                                                // Update the opponent
                                                player.setOpponent(opponent);

                                                // Show in GUI
                                                ClientController.setPlayerCards(opponent);

                                                break;
                                            }
                                        }

                                        System.out.println("[CLIENT] Received PLAYER_LIST_UPDATE message.");

                                        break;

                                    case PLAYER_TURN:
                                        // Since it's a new turn, the player did not say UNO
                                        saidUno = false;

                                        // Get the player whore turn it is
                                        this.playerTurn = message.getOpponent();

                                        // Check if the player is this client's player
                                        // If it is, check if the player should skip turn
                                        if (playerTurn.getId() == player.getID() && playerTurn.getLeftSkipTurns() > 0) {
                                            skipTurn();
                                        } else {
                                            ClientController.setPlayerTurn(playerTurn.getUsername(), (playerTurn.getId() == player.getID()));
                                        }

                                        System.out.println("[CLIENT] Received PLAYER_TURN message.");
                                        break;

                                    case PLAYER_SKIP_TURNS:
                                        // Get the id of the player to be updated
                                        idOpponent = message.getIdPlayerToUpdate();
                                        // Get the number of skips we should add
                                        num = message.getNum();

                                        // Select the player from opponents list
                                        for (int i = 0; i < opponents.size(); i++) {
                                            if (opponents.get(i).getId() == idOpponent) {
                                                opponent = opponents.get(i);
                                                opponent.setLeftSkipTurns(num);
                                                opponents.set(i, opponent);
                                                break;
                                            }
                                        }

                                        // Make sure the getUsername() method doesn't throw a NullPointerException
                                        assert opponent != null;

                                        System.out.println("[Client]: " + opponent.getUsername() + " skip turns: " + opponent.getLeftSkipTurns());

                                        for (Opponent player : opponents) {
                                            if (player.getId() == opponent.getId()) {
                                                // Update the opponent's info
                                                player.setOpponent(opponent);

                                                // Show in GUI
                                                ClientController.setPlayerSkipTurns(opponent);

                                                break;
                                            }
                                        }

                                        System.out.println("[CLIENT] Received PLAYER_SKIP_TURNS message");

                                        break;

                                    case WILD_COLOR_SET:
                                        // Set the wild color
                                        wildColor = message.getColor();

                                        // Show in GUI
                                        ClientController.setWildColor(wildColor);

                                        break;

                                    case WILD_COLOR_CLEAR:
                                        // Remove the wild color
                                        if (wildColor != null) {
                                            wildColor = null;

                                            // Remove from GUI
                                            ClientController.removeWildColor();
                                        }

                                        break;

                                    case CARDS_STACK_ADD:
                                        // Get card from Server
                                        card = message.getCard();
                                        // Add to cards stack list
                                        cardsStack.add(card);

                                        // Add to GUI
                                        ClientController.addCardToStack(card);

                                        break;

                                    case CARDS_STACK_CLEAR:
                                        // Remove all cards from card stack
                                        cardsStack.clear();

                                        // Clean cards stack from GUI
                                        ClientController.clearCardsStack();

                                        break;

                                    case DIRECTION_CHANGE:
                                        // Get direction from Server
                                        direction = message.getDirection();

                                        // Show in GUI
                                        ClientController.setDirection(direction);

                                        break;

                                    case STOP_GAME:
                                        // Set game as not started
                                        gameStarted = false;

                                        break;
                                }
                                break;
                            case SERVER_SHUTDOWN:
                                closeEverything();
                                System.out.println("[CLIENT]: Server is shutting down, exiting.");

                                break;
                            default:
                                break;
                        }
                    } catch (SocketException se) {
                        System.out.println("[CLIENT]: Socket closed by client.");
                        break;
                    } catch (IOException e) {
                        System.out.println("[CLIENT]: Error sending DISCONNECT message: " + e.getMessage());
                        break;
                    }
                }
            } catch (SocketException se) {
                System.out.println("[CLIENT]: Socket closed by client.");
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    public void closeEverything() {
        System.out.println("[CLIENT]: Closing everything!");

        try {
            if (outputStream != null) {
                outputStream.close();
            }
            if (inputStream != null) {
                inputStream.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}