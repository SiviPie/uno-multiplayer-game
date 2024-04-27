package am.client;

import am.message.Message;
import am.message.MessageType;
import am.uno.Player;
import am.uno.Opponent;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

/****
 * THE CLIENT SHOULD KNOW
 * - All info about their own person
 * - Who the other players are
 * - How many cards the other players have
 * - What cards have been played
 * - Whose round it is
 */

public class Client {
    private final Socket socket;

    private ObjectInputStream inputStream;
    private final ObjectOutputStream outputStream;

    private ArrayList<Opponent> opponents = new ArrayList<Opponent>();
    private final String username;
    private Player player;

    public Client(Socket socket, String username) throws IOException {
        this.socket = socket;
        this.username = username;
        this.outputStream = new ObjectOutputStream(socket.getOutputStream());
    }


    public void sendTextMessage(String text) throws IOException {
        Message message = new Message(MessageType.TEXT);
        message.setSender(player.getUsername());
        message.setText(text);

        outputStream.writeObject(message);

        System.out.println("[Client]: Sent TEXT message: " + text);

        ClientController.addTextMessageFromSelf(text);

    }

    public void sendUsername() throws IOException {
        Message message = new Message(MessageType.INIT);
        message.setText(this.username);

        outputStream.writeObject(message);

        System.out.println("[Client]: Sent INIT message.");
    }

    public void sendDisconnectMessage() throws IOException {
        Message message = new Message(MessageType.CLIENT_DISCONNECT);
        message.setSender(player.getUsername());

        outputStream.writeObject(message);

        System.out.println("[Client]: Sent DISCONNECT message.");

        this.closeEverything();
    }

    public void receiveMessageFromServer() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    inputStream = new ObjectInputStream(socket.getInputStream());
                    while (socket.isConnected()) {
                        // MESSAGE VERSION
                        try {
                            Message message = (Message) inputStream.readObject();
                            switch (message.getType()) {
                                case TEXT:
                                    // Handle text message
                                    System.out.println("[CLIENT] Received message: " + message.getText());

                                    // Add the message to the scrollpane
                                    ClientController.addTextMessageFromOthers(message.getSender(), message.getText());
                                    break;
                                case PLAYER_LIST_INIT:
                                    System.out.println("[CLIENT] Received Player List INIT with " + message.getOpponents().size() + " players.");
                                    opponents = message.getOpponents();

                                    for (Opponent opponent : opponents) {
                                        ClientController.addPlayerToList(opponent);
                                    }
                                    break;
                                case PLAYER_LIST_ADD:
                                    System.out.println("[CLIENT] Received Player List ADD: " + message.getOpponent().getUsername());

                                    // Update  the list
                                    opponents.add(message.getOpponent());

                                    // Update in GUI
                                    ClientController.addPlayerToList(message.getOpponent());

                                    break;
                                case PLAYER_LIST_REMOVE:
                                    System.out.println("[CLIENT] Received Player List REMOVE: " + message.getOpponent().getUsername());

                                    // TODO

                                    // Remove player from GUI
                                    ClientController.removePlayerFromList(message.getOpponent().getId());

                                    // Remove player from opponent list
                                    opponents.remove(message.getOpponent());
                                    break;
                                case STATUS_UPDATE:
                                    // Handle status update
                                    break;
                                case GAME_CHOICE:
                                    // Handle game choice
                                    break;
                                case INIT:
                                    player = message.getPlayer();
                                    System.out.println("[CLIENT] Received Player object, I am " + player.getUsername() + " ID: " + player.getID());
                                    break;
                                case SERVER_SHUTDOWN:
                                    System.out.println("[Client]: Server is shutting down, exiting.");
                                    closeEverything();
                                    break;
                                default:
                                    // Handle unknown message type
                                    break;
                            }
                        } catch (SocketException se) {
                            // Handle socket closed
                            System.out.println("[ClientHandler]: Socket closed by client.");
                            break;
                        } catch (IOException e) {
                            // Log the exception and exit the loop
                            System.out.println("[Client]: Error sending DISCONNECT message: " + e.getMessage());
                            // Close the resources and exit the loop
                            break;
                        }
                    }
                } catch (SocketException se) {
                    // Handle socket closed
                    System.out.println("[ClientHandler]: Socket closed by client.");
                } catch (IOException | ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
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