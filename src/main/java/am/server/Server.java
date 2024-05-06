package am.server;

import am.uno.Card;
import am.uno.CardType;
import am.uno.Game;
import javafx.application.Platform;
import javafx.scene.control.Label;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import java.util.Timer;
import java.util.TimerTask;


public class Server {
    // Connection variables
    private ServerSocket serverSocket;

    // Timer used to scan for connections
    Timer timer;

    // Game variables
    protected static Game game;
    private boolean isRunning = true;
    private boolean gameStarted = false;

    public Server(int port) {
        try {
            this.serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("[Server]: Error creating server socket.");
        }
    }

    public void startListening(Label numberOfConnectionsLabel) {
        new Thread(() -> {
            try {
                while (true) {
                    // Check if server is closing before handling new client
                    if (!isRunning) {
                        break;
                    }

                    // Accept incoming client connections
                    Socket clientSocket = serverSocket.accept();

                    if (!gameStarted) {
                        // Create ClientHandler object
                        ClientHandler clientHandler = new ClientHandler(clientSocket, numberOfConnectionsLabel);

                        // Add it to the list of clientHandlers
                        ClientHandler.clients.add(clientHandler);

                        // Update the number of connections label (GUI)
                        updateNumberOfConnectionsLabel(numberOfConnectionsLabel);

                        // Start handling client in a separate thread
                        clientHandler.start();
                    }
                }
            } catch (SocketException se) {
                // Socket closed, server is shutting down
                System.out.println("[Server]: Server socket closed.");
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("[Server]: Error accepting client connection.");
            }
        }).start();

        // Schedule a task to scan for client handlers every few seconds
        this.timer = new Timer();

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                scanForClientHandlers(numberOfConnectionsLabel);
            }
        }, 0, 10000); // Scan every 10 seconds
    }

    public void setGame(Game game) {
        Server.game = game;
    }

    public void setGameStarted(Boolean gameStarted) {
        this.gameStarted = gameStarted;
    }

    private void scanForClientHandlers(Label numberOfConnectionsLabel) {
        updateNumberOfConnectionsLabel(numberOfConnectionsLabel);
    }

    public void setFirstPlayerTurn() {
        ClientHandler.playerTurn = ClientHandler.opponents.getFirst();

        ClientHandler.broadcastPlayerTurn();
    }

    public void giveCardsToPlayers() {
        // Give 5 cards to each player
        for (int i = 0; i < 5; i++) {
            for (ClientHandler client : ClientHandler.clients) {
                // Pop card from game's card deck
                Card card = game.popRandomCard();

                // Add card to player info
                client.player.addCard(card);

                // Send to client
                client.sendCardToClient(card);
            }
        }

        ClientHandler.updateOpponentsForAllClientHandlers();
        broadcastAllPlayersCardsNumber();
    }

    public void setFirstCard() {
        // Pop card from game's card deck
        Card card = game.popRandomCard();

        // Check if card is WILD or DRAW, and if it is, discard it
        while ((card.getType() == CardType.Wild) || (card.getType() == CardType.WildDraw4)) {
            // Add card back to pile
            game.addCard(card);
            // Try again
            card = game.popRandomCard();
        }

        // If the card is Draw2, effects apply - let players know
        if (card.getType() == CardType.Draw2) {

            ClientHandler.cardsStack.add(card);
            ClientHandler.broadcastAddCardToStack(card);
        }

        // Send the card to the players
        ClientHandler.broadcastSendFirstPlayedCard(card);
    }

    public void broadcastAllPlayersCardsNumber() {
        ClientHandler.broadcastAllPlayerCardsNumber();
    }

    public void broadcastTextMessage(String message){
        ClientHandler.broadcastTextMessageFromServer(message);
    }

    private void updateNumberOfConnectionsLabel(Label numberOfConnectionsLabel) {
        // Update the label with the current number of connections
        Platform.runLater(() -> numberOfConnectionsLabel.setText(String.valueOf(ClientHandler.clients.size())));
    }

    public void closeServer(Label numberOfConnectionsLabel) {
        // Set the flag to stop accepting new clients
        isRunning = false;

        // Cancel the timer task
        if (this.timer != null) {
            this.timer.cancel();
        }

        try {
            System.out.println("[Server]: Closing client by client");

            // Close all client connections
            while (!ClientHandler.clients.isEmpty()) {
                closeOneConnection(numberOfConnectionsLabel);
            }

            // Close the server socket to interrupt accept()
            if (serverSocket != null) {
                serverSocket.close();
            }

            // Update number of connections in GUI
            updateNumberOfConnectionsLabel(numberOfConnectionsLabel);

            System.out.println("[Server]: Server closed!");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("[Server]: Error closing server socket.");
        }
    }

    public void closeOneConnection(Label numberOfConnectionsLabel) throws IOException {
        // Check if clients list is empty - if it is, abort
        if (ClientHandler.clients.isEmpty()) {
            System.out.println("[Server]: Can't close connection, client list empty!");
            return;
        }

        // Close the first connection in clients list
        ClientHandler client = ClientHandler.clients.getFirst();
        client.sendShutdownMessage();

        // Update number of connections in GUI
        updateNumberOfConnectionsLabel(numberOfConnectionsLabel);
    }

}