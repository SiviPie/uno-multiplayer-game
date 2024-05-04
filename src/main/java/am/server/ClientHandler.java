package am.server;

import am.message.GameChoice;
import am.message.GameUpdate;
import am.message.Message;
import am.message.MessageType;
import am.uno.*;
import javafx.application.Platform;
import javafx.scene.control.Label;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Random;

public class ClientHandler extends Thread {
    private final Socket clientSocket;
    protected static final ArrayList<ClientHandler> clients = new ArrayList<ClientHandler>();

    private ObjectInputStream inputStream = null;
    private ObjectOutputStream outputStream;

    protected Player player;
    Opponent opponent;// opponent view, seen by others
    protected static ArrayList<Opponent> opponents = new ArrayList<Opponent>();
    protected static Opponent playerTurn;

    private static int last_id = 0;

    private static boolean direction = true;

    protected static ArrayList<Card> cardsStack = new ArrayList<>();

    Label numberOfConnectionsLabel;

    Boolean isConnected;
    Boolean closedByServer = false;

    Card lastPlayedCard;

    public ClientHandler(Socket clientSocket, Label numberOfConnectionsLabel) {
        this.clientSocket = clientSocket;
        this.numberOfConnectionsLabel = numberOfConnectionsLabel;

        try {
            this.outputStream = new ObjectOutputStream(clientSocket.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.isConnected = true;

        System.out.println("[ClientHandler]: Created client handler");

    }

    public void sendTextMessageToClient(String username, String text)  {
        try {
            if (outputStream == null && this.isConnected) {
                // Handle or log the NullPointerException
                System.err.println("Output stream is null");
                return;
            }
            Message message = new Message(MessageType.TEXT);
            message.setSender(username);
            message.setText(text);

            outputStream.writeObject(message);

            System.out.println("[ClientHandler]: Sent TEXT message: " + text);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendPlayerToClient(Player player) {
        try {
            if (outputStream == null) {
                // Handle or log the NullPointerException
                System.err.println("Output stream is null");
                return;
            }
            Message message = new Message(MessageType.INIT);
            message.setPlayer(player);

            outputStream.writeObject(message);

            System.out.println("[ClientHandler]: Sent Player object");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendOpponentListToClient() {
        try {
            if (outputStream == null) {
                // Handle or log the NullPointerException
                System.err.println("Output stream is null");
                return;
            }
            Message message = new Message(MessageType.GAME_UPDATE);
            message.setGameUpdate(GameUpdate.PLAYER_LIST_INIT);
            message.setOpponents(opponents);

            outputStream.writeObject(message);

            System.out.println("[ClientHandler]: Sent opponent list containing " + message.getOpponents().size() + " players to client.");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendOpponentToClient(Opponent opponent, GameUpdate gameUpdate) {
        try {
            if (outputStream == null) {
                // Handle or log the NullPointerException
                System.err.println("Output stream is null");
                return;
            }
            Message message = new Message(MessageType.GAME_UPDATE);
            message.setGameUpdate(gameUpdate);
            message.setOpponent(opponent);
            outputStream.writeObject(message);

            System.out.println("[ClientHandler]: Sent opponent " + opponent.getUsername() + " to client.");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendCardToClient(Card card) {
        try {
            if (outputStream == null) {
                // Handle or log the NullPointerException
                System.err.println("Output stream is null");
                return;
            }

            Message message = new Message(MessageType.GAME_UPDATE);
            message.setGameUpdate(GameUpdate.CARD_ADD);
            message.setCard(card);

            outputStream.writeObject(message);

            System.out.println("[ClientHandler]: Sent card " + card.getName() + " to client.");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendFirstPlayedCard(Card card) {
        try {
            if (outputStream == null) {
                // Handle or log the NullPointerException
                System.err.println("Output stream is null");
                return;
            }

            Message message = new Message(MessageType.GAME_UPDATE);
            message.setGameUpdate(GameUpdate.FIRST_CARD);
            message.setCard(card);

            outputStream.writeObject(message);

            System.out.println("[ClientHandler]: Sent FIRST_CARD " + card.getName() + " to client");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendLastPlayedCard(Card card, Opponent player) {
        try {
            if (outputStream == null) {
                // Handle or log the NullPointerException
                System.err.println("Output stream is null");
                return;
            }

            Message message = new Message(MessageType.GAME_CHOICE);
            message.setGameChoice(GameChoice.PLAY_CARD);

            message.setCard(card);

            message.setIdPlayerToUpdate(player.getId());
            message.setNum(player.getNum_cards());


            outputStream.writeObject(message);

            System.out.println("[ClientHandler]: Sent PLAY_CARD " + card.getName() + " to client. Also opponent has " + player.getNum_cards() + " cards");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendCardToStack(Card card) {
        try {
            if (outputStream == null) {
                // Handle or log the NullPointerException
                System.err.println("Output stream is null");
                return;
            }

            Message message = new Message(MessageType.GAME_UPDATE);
            message.setGameUpdate(GameUpdate.CARDS_STACK_ADD);
            message.setCard(card);

            outputStream.writeObject(message);

            System.out.println("[ClientHandler]: Sent CLEAR_CARD message");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void clearCardsStack() {
        try {
            if (outputStream == null) {
                // Handle or log the NullPointerException
                System.err.println("Output stream is null");
                return;
            }

            Message message = new Message(MessageType.GAME_UPDATE);
            message.setGameUpdate(GameUpdate.CARDS_STACK_CLEAR);

            outputStream.writeObject(message);

            System.out.println("[ClientHandler]: Sent CLEAR_CARD message");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void broadcastAddCardToStack(Card card) {
        for (ClientHandler client : ClientHandler.clients) {
            client.sendCardToStack(card);
        }
    }

    public static void broadcastClearCardsStack() {
        for (ClientHandler client : ClientHandler.clients) {
            client.clearCardsStack();
        }
    }

    public void updateOpponent() {
        this.opponent.setPlayerAsOpponent(player);
    }

    public static void updateOpponentsForAllClientHandlers() {
        for (ClientHandler client : clients) {
            client.updateOpponent();
        }
    }

    public void sendPlayerCardsNumber(Opponent player) {
        try {
            if (outputStream == null) {
                // Handle or log the NullPointerException
                System.err.println("Output stream is null");
                return;
            }

            Message message = new Message(MessageType.GAME_UPDATE);
            message.setGameUpdate(GameUpdate.PLAYER_CARDS_UPDATE);

            message.setIdPlayerToUpdate(player.getId());
            message.setNum(player.getNum_cards());

            System.out.println("[CLIENTHANDLER]: sent id " + player.getId() + "  " + player.getNum_cards() + " cards");

            outputStream.writeObject(message);


            System.out.println("[ClientHandler]: Sent PLAYER_CARDS_UPDATE for all players.");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendPlayerSkipTurns(Opponent opponent) {
        try {
            if (outputStream == null) {
                // Handle or log the NullPointerException
                System.err.println("Output stream is null");
                return;
            }

            Message message = new Message(MessageType.GAME_UPDATE);
            message.setGameUpdate(GameUpdate.PLAYER_SKIP_TURNS);

            message.setIdPlayerToUpdate(opponent.getId());
            message.setNum(opponent.getLeftSkipTurns());

            System.out.println("[CLIENTHANDLER]: sent id " + opponent.getId() + "  " + opponent.getLeftSkipTurns() + " skip turns");

            outputStream.writeObject(message);


            System.out.println("[ClientHandler]: Sent PLAYER_CARDS_UPDATE for all players.");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendAllPlayersCardsNumber() {
        try {
            if (outputStream == null) {
                // Handle or log the NullPointerException
                System.err.println("Output stream is null");
                return;
            }

            for (Opponent player : opponents) {
                Message message = new Message(MessageType.GAME_UPDATE);
                message.setGameUpdate(GameUpdate.PLAYER_CARDS_UPDATE);

                message.setIdPlayerToUpdate(player.getId());
                message.setNum(player.getNum_cards());

                System.out.println("[CLIENTHANDLER]: sent id " + player.getId() + "  " + player.getNum_cards() + " cards");

                outputStream.writeObject(message);
            }

            System.out.println("[ClientHandler]: Sent PLAYER_CARDS_UPDATE for all players.");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendWildColor(Color color) {
        try {
            if (outputStream == null) {
                // Handle or log the NullPointerException
                System.err.println("Output stream is null");
                return;
            }

            Message message = new Message(MessageType.GAME_UPDATE);
            message.setGameUpdate(GameUpdate.WILD_COLOR_SET);
            message.setColor(color);

            outputStream.writeObject(message);

            System.out.println("[CLIENTHANDLER]: sent WILD_COLOR_SET message");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendRemoveWildColor() {
        try {
            if (outputStream == null) {
                // Handle or log the NullPointerException
                System.err.println("Output stream is null");
                return;
            }

            Message message = new Message(MessageType.GAME_UPDATE);
            message.setGameUpdate(GameUpdate.WILD_COLOR_CLEAR);

            outputStream.writeObject(message);

            System.out.println("[CLIENTHANDLER]: sent WILD_COLOR_CLEAR message");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void changeDirection(boolean direction) {
        try {
            if (outputStream == null) {
                System.err.println("Output stream is null");
                return;
            }

            Message message = new Message(MessageType.GAME_UPDATE);
            message.setGameUpdate(GameUpdate.DIRECTION_CHANGE);
            message.setDirection(direction);

            outputStream.writeObject(message);

            System.out.println("[CLIENTHANDLER]: sent DIRECTION_CHANGE message");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void stopGame() {
        try {
            if (outputStream == null) {
                System.err.println("Output stream is null");
                return;
            }

            Message message = new Message(MessageType.GAME_UPDATE);
            message.setGameUpdate(GameUpdate.STOP_GAME);

            outputStream.writeObject(message);

            System.out.println("[CLIENTHANDLER]: sent DIRECTION_CHANGE message");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendShutdownMessage() {
        try {
            if (outputStream == null) {
                // Handle or log the NullPointerException
                System.err.println("Output stream is null");
                return;
            }

            Message message = new Message(MessageType.SERVER_SHUTDOWN);
            message.setSender("Server");

            outputStream.writeObject(message);

            System.out.println("[ClientHandler]: Sent SERVER_SHUTDOWN message: ");

            this.closedByServer = true;
            this.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (IllegalStateException e) {
            // Handle or log the IllegalStateException
            e.printStackTrace();
        } catch (SecurityException e) {
            // Handle or log the SecurityException
            e.printStackTrace();
        }
    }

    public static void broadcastAllPlayerCardsNumber() {
        for (ClientHandler client : clients) {
            client.sendAllPlayersCardsNumber();
        }
    }

    public static void broadcastTextMessageFromServer(String message) {
        for (ClientHandler client : clients) {
            client.sendTextMessageToClient("server", message);
        }
    }

    public static void broadcastTextMessageFromServerExcept(String message, ClientHandler clientHandler) {
        for (ClientHandler client : clients) {
            if (client != clientHandler) {
                client.sendTextMessageToClient("server", message);
            }
        }
    }

    public static void broadcastTextMessageFromClient(String message, ClientHandler sender) {
        // Broadcast from everyone except sender
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendTextMessageToClient(sender.player.getUsername(), message);
            }
        }
    }

    public static void broadcastNewOpponentToOtherPlayers(Opponent opponent, ClientHandler exception) {
        for (ClientHandler client : clients) {
            if (client != exception) {
                client.sendOpponentToClient(opponent, GameUpdate.PLAYER_LIST_ADD);
            }
        }
    }

    public static void broadcastLastPlayedCard(Card card, Opponent opponent) {
        for (ClientHandler client : clients) {
            client.sendLastPlayedCard(card, opponent);
        }
    }

    public static void broadcastRemoveOpponentToOtherPlayersExcept(Opponent opponent, ClientHandler clientHandler) {
        for (ClientHandler client : clients) {
            if (client != clientHandler) {
                client.sendOpponentToClient(opponent, GameUpdate.PLAYER_LIST_REMOVE);
            }
        }
    }

    public static void broadcastPlayerCardsNumber(Opponent opponent) {
        for (ClientHandler client : clients) {
            client.sendPlayerCardsNumber(opponent);
        }
    }

    private void broadcastPlayerSkipTurns(Opponent opponent) {
        for (ClientHandler client : clients) {
            client.sendPlayerSkipTurns(opponent);
        }
    }

    public static void broadcastPlayerTurn() {
        for (ClientHandler client : ClientHandler.clients) {
            client.sendOpponentToClient(ClientHandler.playerTurn, GameUpdate.PLAYER_TURN);
        }
    }

    public static void broadcastSetWildColor(Color color) {
        for (ClientHandler client : ClientHandler.clients) {
            client.sendWildColor(color);
        }
    }

    public static void broadcastRemoveWildColor() {
        for (ClientHandler client : ClientHandler.clients) {
            client.sendRemoveWildColor();
        }
    }

    public static void broadcastChangeDirection(boolean direction) {
        for (ClientHandler client : ClientHandler.clients) {
            client.changeDirection(direction);
        }
    }

    private void broadcastStopGame() {
        for (ClientHandler client : ClientHandler.clients) {
            client.stopGame();
        }
    }

    private void updateNumberOfConnectionsLabel(Label numberOfConnectionsLabel) {
        // Update the label with the current number of connections
        Platform.runLater(() -> numberOfConnectionsLabel.setText(String.valueOf(ClientHandler.clients.size())));
    }

    @Override
    public void run() {
        try {
            this.inputStream = new ObjectInputStream(clientSocket.getInputStream());
            Message message;
            Card card;

            while (clientSocket.isConnected()) {
                if (!this.isConnected) {
                    break;
                }

                try {
                    if (inputStream == null) {
                        break;
                    }

                    message = (Message) inputStream.readObject();

                    int cardsToDraw;
                    int index;

                    switch (message.getType()) {

                        case TEXT:
                            System.out.println("[ClientHandler]: Received TEXT: " + message.getText());
                            broadcastTextMessageFromClient(message.getText(), this);
                            break;

                        case INIT:
                            System.out.println("[ClientHandler]: Received INIT message");
                            String username = message.getText();

                            // Create an instance of Random class
                            Random random = new Random();

                            // Generate a random number between 0 (inclusive) and 42 (exclusive)
                            int randomNumber = random.nextInt(19);

                            player = new Player(username, last_id, randomNumber);
                            last_id++;

                            // SEND Player to client
                            sendPlayerToClient(player);

                            opponent = new Opponent(player);

                            opponents.add(opponent);

                            // Greet player
                            this.sendTextMessageToClient("server", "Welcome to the server, " + player.getUsername() + "!");

                            // TODO: send list of players to this player
                            this.sendOpponentListToClient();

                            // TODO: notify the others that player joined
                            broadcastTextMessageFromServerExcept(username + " connected to the server!", this);
                            broadcastNewOpponentToOtherPlayers(opponent, this);

                            break;

                        case GAME_CHOICE:
                            switch (message.getGameChoice()) {
                                case DRAW_CARD:

                                    // Check if the stacked cards apply to player
                                    if (!cardsStack.isEmpty()) {
                                        // Check what type of war it was
                                        if (cardsStack.getFirst().getType() == CardType.Skip) {
                                            System.out.println("[ClientHandler]: Set player " + cardsStack.size() + " skips");
                                            player.setLeftSkipTurns(cardsStack.size());

                                            broadcastPlayerSkipTurns(opponent);

                                        } else {
                                            cardsToDraw = 0;

                                            for (Card cardFromStack : cardsStack) {
                                                if (cardFromStack.getType() == CardType.Draw2) {
                                                    cardsToDraw += 2;
                                                } else if (cardFromStack.getType() == CardType.WildDraw4) {
                                                    cardsToDraw += 4;
                                                }
                                            }

                                            System.out.println("[ClientHandler]: Player is going to draw " + cardsToDraw + " cards.");

                                            for (int i = 0; i < cardsToDraw; i++) {
                                                card = Server.game.popRandomCard();

                                                // Send card to player
                                                player.addCard(card);
                                                sendCardToClient(card);
                                            }
                                        }

                                        // Add the cards back to the game
                                        for (int i = 0; i < cardsStack.size() - 1; i++) {
                                            Server.game.addCard(cardsStack.get(i));
                                        }

                                        cardsStack.clear();

                                        // Let everyone know they should not worry about consequences anymore
                                        broadcastClearCardsStack();
                                    } else {
                                        card = Server.game.popRandomCard();

                                        // Send card to player
                                        player.addCard(card);
                                        sendCardToClient(card);
                                    }

                                    updateOpponent();

                                    // Broadcast new cards number for this player
                                    broadcastPlayerCardsNumber(opponent);


                                    // Go to the next player
                                    if (direction) {
                                        playerTurn = opponents.get((opponents.indexOf(playerTurn) + 1) % opponents.size());
                                    } else {
                                        index = opponents.indexOf(playerTurn) - 1;
                                        if (index < 0) {
                                            index += opponents.size(); // Wrap around to the end of the list
                                        }
                                        playerTurn = opponents.get(index);
                                    }

                                    broadcastPlayerTurn();
                                break;

                                case PLAY_CARD:
                                    lastPlayedCard = message.getCard();

                                    player.popCard(lastPlayedCard);

                                    // Check if player won
                                    if (player.getCards().getSize() == 0) {
                                        broadcastTextMessageFromServer("Congrats! " + player.getUsername() + " won the game!");
                                        broadcastStopGame();
                                        break;
                                    }

                                    // Check if player said uno when they had 2 cards
                                    if (player.getCards().getSize() == 1) {
                                        if (message.getSaidUno() == true) {
                                            broadcastTextMessageFromServer(player.getUsername() + " said UNO!");
                                        } else {
                                            broadcastTextMessageFromServer(player.getUsername() + " forgot to say UNO! +4 cards punishment.");

                                            for (int i = 0; i < 4; i++) {
                                                card = Server.game.popRandomCard();

                                                // Send card to player
                                                player.addCard(card);
                                                sendCardToClient(card);
                                            }
                                        }
                                    }

                                    // Send the lastPlayedCard to clients PLUS updated player info
                                    updateOpponent();

                                    broadcastLastPlayedCard(lastPlayedCard, opponent);

                                    // Send the wild color if it's been set
                                    if (message.getColor() != null) {
                                        broadcastSetWildColor(message.getColor());
                                    } else {
                                        broadcastRemoveWildColor();
                                    }

                                    if(lastPlayedCard.getType() == CardType.Reverse) {
                                        direction = !direction;

                                        broadcastChangeDirection(direction);
                                    }

                                    if (lastPlayedCard.isStackable()) {
                                        cardsStack.add(lastPlayedCard);

                                        // Broadcast stack
                                        broadcastAddCardToStack(lastPlayedCard);
                                    } else {
                                        Server.game.addCard(lastPlayedCard);
                                    }

                                    // Go to the next player
                                    if (direction) {
                                        playerTurn = opponents.get((opponents.indexOf(playerTurn) + 1) % opponents.size());
                                    } else {
                                        index = opponents.indexOf(playerTurn) - 1;
                                        if (index < 0) {
                                            index += opponents.size(); // Wrap around to the end of the list
                                        }
                                        playerTurn = opponents.get(index);
                                    }

                                    broadcastPlayerTurn();

                                    break;

                                case SKIP_TURN:
                                    player.setLeftSkipTurns(player.getLeftSkipTurns() - 1);

                                    opponent.setPlayerAsOpponent(player);

                                    broadcastPlayerSkipTurns(opponent);

                                    // Go to the next player
                                    if (direction) {
                                        playerTurn = opponents.get((opponents.indexOf(playerTurn) + 1) % opponents.size());
                                    } else {
                                        index = opponents.indexOf(playerTurn) - 1;
                                        if (index < 0) {
                                            index += opponents.size(); // Wrap around to the end of the list
                                        }
                                        playerTurn = opponents.get(index);
                                    }

                                    broadcastPlayerTurn();

                                    break;
                            }
                            break;

                        case CLIENT_DISCONNECT:
                            System.out.println("[ClientHandler]: Received DISCONNECT message.");
                            this.isConnected = false;

                            break;

                        default:
                            break;
                    }
                } catch (EOFException eof) {
                    // Handle client disconnection
                    System.out.println("[ClientHandler]: Client disconnected");
                    break; // Exit the loop when client disconnects
                } catch (SocketException se) {
                    // Handle socket closed
                    System.out.println("[ClientHandler]: Socket closed by client.");
                    break; // Exit the loop when socket is closed
                } catch (IOException | ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (EOFException eof) {
            // Handle client disconnection
            System.out.println("[ClientHandler]: Client disconnected");
        } catch (SocketException se) {
            // Handle socket closed
            System.out.println("[ClientHandler]: Socket closed by client.");
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            close();
        }
    }



    public void close() {
        try {
            System.out.println("[ClientHandler]: Trying to close reader");

            if (!closedByServer) {
                broadcastTextMessageFromServerExcept(player.getUsername() + " left the server.", this);
                broadcastRemoveOpponentToOtherPlayersExcept(opponent, this);
            }

            if (inputStream != null) {
                inputStream.close();
            }

            if (outputStream != null) {
                outputStream.close();
            }

            if (clientSocket != null) {
                clientSocket.close();
            }

            // Remove this client handler from the list
            clients.remove(this);
            opponents.remove(opponent);  // DO I HAVE TO ???????

            // Notify all the remaining clients of the departure

            updateNumberOfConnectionsLabel(numberOfConnectionsLabel);

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("[ClientHandler]: Error closing client connection.");
        }
    }
}