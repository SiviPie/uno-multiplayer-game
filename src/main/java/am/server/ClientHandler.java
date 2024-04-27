package am.server;

import am.message.Message;
import am.message.MessageType;
import am.uno.Player;
import am.uno.Opponent;
import javafx.application.Platform;
import javafx.scene.control.Label;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

public class ClientHandler extends Thread {
    private final Socket clientSocket;
    protected static final ArrayList<ClientHandler> clients = new ArrayList<ClientHandler>();

    protected static ArrayList<Opponent> opponents = new ArrayList<Opponent>();

    private ObjectInputStream inputStream = null;
    private ObjectOutputStream outputStream = null;

    Label numberOfConnectionsLabel;

    private Player player;

    private static int last_id = 0;

    Boolean isConnected;

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
            Message message = new Message(MessageType.PLAYER_LIST_INIT);
            message.setOpponents(opponents);

            outputStream.writeObject(message);

            System.out.println("[ClientHandler]: Sent opponent list containing " + message.getOpponents().size() + " players to client.");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendOpponentToClient(Opponent opponent) {
        try {
            Message message = new Message(MessageType.PLAYER_LIST_ADD);
            message.setOpponent(opponent);

            outputStream.writeObject(message);

            System.out.println("[ClientHandler]: Sent opponent " + opponent.getUsername() + " to client.");

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendShutdownMessage() {
        try {
            Message message = new Message(MessageType.SERVER_SHUTDOWN);
            message.setSender("Server");

            outputStream.writeObject(message);

            System.out.println("[ClientHandler]: Sent SERVER_SHUTDOWN message: ");

            this.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void broadcastTextMessageFromServer(String message) {
        for (ClientHandler client : clients) {
            client.sendTextMessageToClient("server", message);
        }
    }

    public void broadcastTextMessageFromServerExcept(String message, ClientHandler clientHandler) {
        for (ClientHandler client : clients) {
            if (client != clientHandler) {
                client.sendTextMessageToClient("server", message);
            }
        }
    }

    public void broadcastTextMessageFromClient(String message, ClientHandler sender) {
        // Broadcast from everyone except sender
        for (ClientHandler client : clients) {
            if (client != sender) {
                client.sendTextMessageToClient(sender.player.getUsername(), message);
            }
        }
    }

    public void broadcastNewOpponentToOtherPlayers(Opponent opponent, ClientHandler exception) {
        for (ClientHandler client : clients) {
            if (client != exception) {
                client.sendOpponentToClient(opponent);
            }
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

            while (clientSocket.isConnected()) {
                if (!this.isConnected) {
                    break;
                }

                try {
                    Message message = (Message) inputStream.readObject();

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
                            break;
                        case CLIENT_DISCONNECT:
                            System.out.println("[ClientHandler]: Received DISCONNECT message.");
                            this.isConnected = false;

                            // TODO: Notify the other users
                            break;
                        case INIT:
                            // TODO: Assign player to client
                            // Init comes with a usename

                            System.out.println("[ClientHandler]: Received INIT message");
                            String username = message.getText();
                            player = new Player(username, last_id);
                            last_id++;

                            // SEND Player to client
                            sendPlayerToClient(player);

                            Opponent opponent = new Opponent(player);

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
        }catch (SocketException se) {
            // Handle socket closed
            System.out.println("[ClientHandler]: Socket closed by client.");
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            close();
        }
    }

    /*
    private static void removePlayerFromOpponentList(int id) {
        for (int i = 0; i < opponents.size(); i++) {
            if (opponents.get(i).getId() == id) {
                opponents.remove(i);
                return;
            }
        }
    }

     */

    public void close() {
        try {
            System.out.println("[ClientHandler]: Trying to close reader");

            if (inputStream != null) {
                inputStream.close();
            }

            if (outputStream != null) {
                outputStream.close();
            }

            if (clientSocket != null) {
                clientSocket.close();
            }

            // TODO: Notify all the remaining clients of the departure

            // Remove this client handler from the list
            clients.remove(this);

            // Remove player from opponent list
            //removePlayerFromOpponentList(player.getID());

            updateNumberOfConnectionsLabel(numberOfConnectionsLabel);

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("[ClientHandler]: Error closing client connection.");
        }
    }
}