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
    private final Socket socket;

    private ObjectInputStream inputStream;
    private final ObjectOutputStream outputStream;

    boolean gameStarted = false;

    private final String username;
    protected Player player;
    private ArrayList<Opponent> opponents = new ArrayList<>();
    private Opponent playerTurn;

    private Card lastPlayedCard;
    private ArrayList<Card> cardsStack = new ArrayList<>();
    private boolean direction = true;

    private Color wildColor = null;

    boolean saidUno = false;

    public Client(Socket socket, String username) throws IOException {
        this.socket = socket;
        this.username = username;
        this.outputStream = new ObjectOutputStream(socket.getOutputStream());
    }

    public boolean isCardPlayable(Card card) {

        // TODO: also check if the last played card already applied to a previous player

        boolean isPlayable = false;


        switch (card.getType()) {
            case CardType.Number:
                if ((lastPlayedCard.getType() == CardType.Number && (lastPlayedCard.getNumber() == card.getNumber() || lastPlayedCard.getColor() == card.getColor()))
                        || ((lastPlayedCard.getType() == CardType.Skip || lastPlayedCard.getType() == CardType.Reverse) && (lastPlayedCard.getColor() == card.getColor()))
                        || ((lastPlayedCard.getType() == CardType.Wild) && (wildColor == card.getColor()))
                        || ((lastPlayedCard.getType() == CardType.Draw2) && cardsStack.isEmpty() && (lastPlayedCard.getColor() == card.getColor()))
                        || ((lastPlayedCard.getType() == CardType.WildDraw4) && cardsStack.isEmpty() && (wildColor == card.getColor()))) {
                    isPlayable = true;
                }

                break;

            case CardType.Skip:
                if (lastPlayedCard.getType() == CardType.Skip
                        || ((lastPlayedCard.getType() == CardType.Number || lastPlayedCard.getType() == CardType.Reverse) && (lastPlayedCard.getColor() == card.getColor()))
                        || ((lastPlayedCard.getType() == CardType.Wild) && (wildColor == card.getColor()))
                        || ((lastPlayedCard.getType() == CardType.Draw2) && cardsStack.isEmpty() && (lastPlayedCard.getColor() == card.getColor()))
                        || ((lastPlayedCard.getType() == CardType.WildDraw4) && cardsStack.isEmpty() && (wildColor == card.getColor()))) {
                    isPlayable = true;
                }

                break;

            case CardType.Reverse:
                if (lastPlayedCard.getType() == CardType.Reverse
                    || ((lastPlayedCard.getType() == CardType.Number || lastPlayedCard.getType() == CardType.Skip) && (lastPlayedCard.getColor() == card.getColor()))
                    || ((lastPlayedCard.getType() == CardType.Wild) && (wildColor == card.getColor()))
                    || ((lastPlayedCard.getType() == CardType.Draw2) && cardsStack.isEmpty() && (lastPlayedCard.getColor() == card.getColor()))
                    || ((lastPlayedCard.getType() == CardType.WildDraw4) && cardsStack.isEmpty() && (wildColor == card.getColor()))) {
                    isPlayable = true;
                }

                break;

            case CardType.Draw2:
                // TODO: Also check if a war started
                if (lastPlayedCard.getType() == CardType.Draw2
                        || lastPlayedCard.getType() == CardType.WildDraw4
                        || ((lastPlayedCard.getType() == CardType.Wild) && (wildColor == card.getColor()))
                        || ((lastPlayedCard.getType() == CardType.Number || lastPlayedCard.getType() == CardType.Skip || lastPlayedCard.getType() == CardType.Reverse)
                            && (lastPlayedCard.getColor() == card.getColor()))) {
                    isPlayable = true;
                }

                break;

            case CardType.Wild:
                // TODO: only if it's not a war
                isPlayable = cardsStack.isEmpty();

                break;

            case CardType.WildDraw4:
                // This one works on everything
                isPlayable = true;

                break;
        }

        return isPlayable;
    }

    public void sendTextMessage(String text) throws IOException {
        Message message = new Message(MessageType.TEXT);
        message.setSender(player.getUsername());
        message.setText(text);

        outputStream.writeObject(message);

        System.out.println("[CLIENT]: Sent TEXT message: " + text);

        ClientController.addTextMessageFromSelf(text, username);
    }

    public void sendUsername() throws IOException {
        Message message = new Message(MessageType.INIT);
        message.setText(this.username);

        outputStream.writeObject(message);

        System.out.println("[CLIENT]: Sent INIT message.");
    }

    public void playCard(Card card, Color selectedColor) throws IOException {
        if (!gameStarted) {
            return;
        }

        Message message = new Message(MessageType.GAME_CHOICE);
        message.setGameChoice(GameChoice.PLAY_CARD);
        message.setCard(card);
        message.setSaidUno(saidUno);

        if (selectedColor != null) {
            message.setColor(selectedColor);
        }

        player.popCard(card);

        outputStream.writeObject(message);

        System.out.println("[CLIENT]: Sent PLAY_CARD message.");
    }

    public void drawCard() throws IOException {
        if (!gameStarted) {
            return;
        }

        Message message = new Message(MessageType.GAME_CHOICE);
        message.setGameChoice(GameChoice.DRAW_CARD);

        outputStream.writeObject(message);

        System.out.println("[CLIENT]: Sent DRAW_CARD message.");
    }

    private void skipTurn() throws IOException {
        if (!gameStarted) {
            return;
        }

        Message message = new Message(MessageType.GAME_CHOICE);
        message.setGameChoice(GameChoice.SKIP_TURN);

        outputStream.writeObject(message);

        System.out.println("[CLIENT]: Sent SKIP_TURN message.");
    }

    public void sendDisconnectMessage() throws IOException {
        Message message = new Message(MessageType.CLIENT_DISCONNECT);
        message.setSender(player.getUsername());

        outputStream.writeObject(message);

        System.out.println("[CLIENT]: Sent DISCONNECT message.");

        this.closeEverything();
    }

    public void receiveMessageFromServer() {
        new Thread(() -> {
            try {
                inputStream = new ObjectInputStream(socket.getInputStream());

                Opponent opponent = null;
                int idOpponent;
                int num;
                Message message;

                Card card;

                while (socket.isConnected()) {
                    try {
                        message = (Message) inputStream.readObject();
                        switch (message.getType()) {
                            case TEXT:
                                // Handle text message
                                System.out.println("[CLIENT] Received TEXT message: " + message.getText());

                                // Add the message to the scrollpane
                                ClientController.addTextMessageFromOthers(message.getSender(), message.getText());
                                break;

                            case INIT:
                                player = message.getPlayer();
                                System.out.println("[CLIENT]: Received INIT Player object, I am " + player.getUsername() + " ID: " + player.getID());
                                break;

                            case GAME_CHOICE:
                                System.out.println("[CLIENT] Received GAME_CHOICE: " + message.getGameChoice());

                                switch (message.getGameChoice()) {
                                    case DRAW_CARD:
                                        card = message.getCard();

                                        player.addCard(card);

                                        ClientController.addCard(card);
                                        break;

                                    case PLAY_CARD:

                                        System.out.println("[CLIENT] Received PLAY_ADD:");
                                        lastPlayedCard = message.getCard();

                                        idOpponent = message.getIdPlayerToUpdate();
                                        num = message.getNum();

                                        for (int i = 0; i < opponents.size(); i++) {
                                            if (opponents.get(i).getId() == idOpponent) {
                                                opponent = opponents.get(i);
                                                opponent.setNum_cards(num);
                                                opponents.set(i, opponent);
                                                break;
                                            }
                                        }

                                        assert opponent != null;
                                        System.out.println(opponent.getUsername() + " new cards: " + opponent.getNum_cards());

                                        for (Opponent player : opponents) {
                                            if (player.getId() == opponent.getId()) {
                                                // Update player's properties with the properties of the copy
                                                player.setOpponent(opponent);


                                                // Also show in gui
                                                ClientController.setPlayerCards(opponent);

                                                break; // No need to continue searching once found
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
                                        System.out.println("[CLIENT] Received FIRST_CARD:");
                                        lastPlayedCard = message.getCard();

                                        gameStarted = true;

                                        // Add to GUI
                                        ClientController.setLastPlayedCard(lastPlayedCard);
                                        break;

                                    case CARD_ADD:
                                        card = message.getCard();

                                        System.out.println("[CLIENT] Received CARD_ADD: " + card.getName());

                                        // Add received card to deck
                                        player.addCard(card);

                                        // Add to GUI
                                        ClientController.addCard(card);

                                        break;

                                    case PLAYER_LIST_INIT:
                                        System.out.println("[CLIENT] Received PLAYER_LIST_INIT with " + message.getOpponents().size() + " players.");
                                        opponents = message.getOpponents();

                                        for (Opponent player : opponents) {
                                            ClientController.addPlayerToList(player);
                                        }
                                        break;

                                    case PLAYER_LIST_ADD:
                                        System.out.println("[CLIENT] Received PLAYER_LIST_ADD: " + message.getOpponent().getUsername());

                                        // Update  the list
                                        opponents.add(message.getOpponent());

                                        // Update in GUI
                                        ClientController.addPlayerToList(message.getOpponent());

                                        break;

                                    case PLAYER_LIST_REMOVE:
                                        System.out.println("[CLIENT] Received PLAYER_LIST_REMOVE: " + message.getOpponent().getUsername());

                                        // Remove player from GUI
                                        ClientController.removePlayerFromList(message.getOpponent().getId());

                                        // Remove player from opponent list
                                        opponents.remove(message.getOpponent());

                                        break;

                                    case PLAYER_CARDS_UPDATE:
                                        System.out.println("[CLIENT] Received PLAYER_LIST_UPDATE");

                                        idOpponent = message.getIdPlayerToUpdate();
                                        num = message.getNum();

                                        for (int i = 0; i < opponents.size(); i++) {
                                            if (opponents.get(i).getId() == idOpponent) {
                                                opponent= opponents.get(i);
                                                opponent.setNum_cards(num);
                                                opponents.set(i, opponent);
                                                break;
                                            }
                                        }

                                        assert opponent != null;
                                        System.out.println(opponent.getUsername() + " new cards: " + opponent.getNum_cards());

                                        for (Opponent player : opponents) {
                                            if (player.getId() == opponent.getId()) {
                                                // Update player's properties with the properties of the copy
                                                player.setOpponent(opponent);


                                                // Also show in gui
                                                ClientController.setPlayerCards(opponent);

                                                break; // No need to continue searching once found
                                            }
                                        }

                                        break;

                                    case PLAYER_TURN:
                                        saidUno = false;

                                        this.playerTurn = message.getOpponent();
                                        // Update in GUI

                                        for (int i = 0; i < opponents.size(); i++) {
                                            if (opponents.get(i).getId() == player.getID()) {
                                                opponent = opponents.get(i);
                                                break;
                                            }
                                        }

                                        if (opponent.getId() == player.getID() && opponent.getLeftSkipTurns() > 0) {
                                            skipTurn();
                                        } else {
                                            ClientController.setPlayerTurn(playerTurn.getUsername(), (playerTurn.getId() == player.getID()));
                                        }

                                        break;

                                    case PLAYER_SKIP_TURNS:
                                        System.out.println("[CLIENT] Received PLAYER_SKIP_TURNS");

                                        idOpponent = message.getIdPlayerToUpdate();
                                        num = message.getNum();

                                        for (int i = 0; i < opponents.size(); i++) {
                                            if (opponents.get(i).getId() == idOpponent) {
                                                opponent = opponents.get(i);
                                                opponent.setLeftSkipTurns(num);
                                                opponents.set(i, opponent);
                                                break;
                                            }
                                        }

                                        assert opponent != null;
                                        System.out.println("[Client]: " + opponent.getUsername() + " skip turns: " + opponent.getLeftSkipTurns());

                                        for (Opponent player : opponents) {
                                            if (player.getId() == opponent.getId()) {
                                                // Update player's properties with the properties of the copy
                                                player.setOpponent(opponent);

                                                // Also show in gui
                                                ClientController.setPlayerSkipTurns(opponent);

                                                break; // No need to continue searching once found
                                            }
                                        }

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
                                        card = message.getCard();
                                        cardsStack.add(card);

                                        // Add to GUI
                                        ClientController.addCardToStack(card);

                                        break;

                                    case CARDS_STACK_CLEAR:
                                        cardsStack.clear();

                                        // Clean cards stack from GUI
                                        ClientController.clearCardsStack();

                                        break;

                                    case DIRECTION_CHANGE:
                                        direction = message.getDirection();

                                        // Show in GUI
                                        ClientController.setDirection(direction);

                                        break;

                                    case STOP_GAME:
                                        // TODO
                                        break;
                                }
                                break;
                            case SERVER_SHUTDOWN:
                                System.out.println("[CLIENT]: Server is shutting down, exiting.");
                                closeEverything();

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
                System.out.println("[ClientHandler]: Socket closed by client.");
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

    public void closeEverything() {
        System.out.println("[Client]: Closing everything!");
        try {
            if (outputStream != null) {
                outputStream.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}