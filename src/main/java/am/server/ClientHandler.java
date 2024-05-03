package am.server;

import am.message.Message;
import am.message.MessageType;
import am.uno.Card;
import am.uno.Player;
import am.uno.Opponent;
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
    protected static ArrayList<Opponent> opponents = new ArrayList<Opponent>();
    protected static ArrayList<Card> cardsStack = new ArrayList<>();
    protected static Opponent playerTurn;

    private ObjectInputStream inputStream = null;
    private ObjectOutputStream outputStream;

    Label numberOfConnectionsLabel;

    protected Player player;
    Opponent opponent;// opponent view, seen by others

    private static int last_id = 0;

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
            Message message = new Message(MessageType.PLAYER_LIST_INIT);
            message.setOpponents(opponents);

            outputStream.writeObject(message);

            System.out.println("[ClientHandler]: Sent opponent list containing " + message.getOpponents().size() + " players to client.");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendOpponentToClient(Opponent opponent, MessageType messageType) {
        try {
            if (outputStream == null) {
                // Handle or log the NullPointerException
                System.err.println("Output stream is null");
                return;
            }
            Message message = new Message(messageType);
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

            Message message = new Message(MessageType.CARD_ADD);
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

            Message message = new Message(MessageType.FIRST_CARD);
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

            Message message = new Message(MessageType.PLAY_CARD);
            message.setCard(card);

            message.setIdPlayerToUpdate(player.getId());
            message.setNum_cards(player.getNum_cards());


            outputStream.writeObject(message);

            System.out.println("[ClientHandler]: Sent PLAY_CARD " + card.getName() + " to client. Also opponent has " + player.getNum_cards() + " cards");

        } catch (IOException e) {
            throw new RuntimeException(e);
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

            Message message = new Message(MessageType.PLAYER_CARDS_UPDATE);

            message.setIdPlayerToUpdate(player.getId());
            message.setNum_cards(player.getNum_cards());

            System.out.println("[CLIENTHANDLER]: sent id " + player.getId() + "  " + player.getNum_cards() + " cards");

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
                Message message = new Message(MessageType.PLAYER_CARDS_UPDATE);

                message.setIdPlayerToUpdate(player.getId());
                message.setNum_cards(player.getNum_cards());

                System.out.println("[CLIENTHANDLER]: sent id " + player.getId() + "  " + player.getNum_cards() + " cards");

                outputStream.writeObject(message);
            }

            System.out.println("[ClientHandler]: Sent PLAYER_CARDS_UPDATE for all players.");

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
                client.sendOpponentToClient(opponent, MessageType.PLAYER_LIST_ADD);
            }
        }
    }

    public static void broadcastLastPlayedCard(Card card, Opponent opponent) {
        for (ClientHandler client : clients) {
            client.sendLastPlayedCard(card, opponent);
        }
    }

    public static void broadcastRemoveOpponentToOtherPlayers(Opponent opponent) {
        for (ClientHandler client : clients) {
            client.sendOpponentToClient(opponent, MessageType.PLAYER_LIST_REMOVE);
        }
    }

    public static void broadcastRemoveOpponentToOtherPlayersExcept(Opponent opponent, ClientHandler clientHandler) {
        for (ClientHandler client : clients) {
            if (client != clientHandler) {
                client.sendOpponentToClient(opponent, MessageType.PLAYER_LIST_REMOVE);
            }
        }
    }

    public static void broadcastPlayerCardsNumber(Opponent opponent) {
        for (ClientHandler client : clients) {
            client.sendPlayerCardsNumber(opponent);
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

                    switch (message.getType()) {
                        case TEXT:
                            // Handle text message
                            System.out.println("[ClientHandler]: Received TEXT: " + message.getText());
                            broadcastTextMessageFromClient(message.getText(), this);
                            break;
                        case STATUS_UPDATE:
                            // Handle status update
                            break;
                        case GAME_CHOICE:
                            // Handle game choice
                            switch (message.getGameChoice()) {
                                case DRAW_CARD:
                                    card = Server.game.popRandomCard();

                                    // Send card to player
                                    player.addCard(card);
                                    sendCardToClient(card);

                                    updateOpponent();

                                    // Broadcast new cards number for this player
                                    broadcastPlayerCardsNumber(opponent);
                                break;
                            }
                            break;
                        case PLAY_CARD:
                            lastPlayedCard = message.getCard();

                            player.showCardDeck();

                            player.popCard(lastPlayedCard);

                            // Send the lastPlayedCard to clients PLUS updated player info
                            opponent.setPlayerAsOpponent(player);
                            broadcastLastPlayedCard(lastPlayedCard, opponent);

                            if (lastPlayedCard.isStackable()) {
                                // TODO: Add to stack
                            } else {
                                Server.game.addCard(lastPlayedCard);
                            }

                            break;
                        case CLIENT_DISCONNECT:
                            System.out.println("[ClientHandler]: Received DISCONNECT message.");
                            this.isConnected = false;

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
                        default:
                            // Handle unknown message type
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