package am.server;

import am.message.GameUpdate;
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
    private ServerSocket serverSocket;

    private boolean isRunning = true;

    private boolean gameStarted = false;

    protected static Game game;

    Timer timer;

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
                    if (!isRunning || gameStarted) {
                        break; // Check if server is closing before handling new client
                    }
                    Socket clientSocket = serverSocket.accept(); // Accept incoming client connections

                    ClientHandler clientHandler = new ClientHandler(clientSocket, numberOfConnectionsLabel);

                    ClientHandler.clients.add(clientHandler);

                    updateNumberOfConnectionsLabel(numberOfConnectionsLabel);
                    clientHandler.start(); // Start handling client in a separate thread
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
        }, 0, 5000); // Scan every 5 seconds
    }

    public void setGame(Game game) {
        this.game = game;
    }

    private void scanForClientHandlers(Label numberOfConnectionsLabel) {
        updateNumberOfConnectionsLabel(numberOfConnectionsLabel);
    }

    public void giveCardsToPlayers() {
        for (int i = 0; i < 5; i++) {
            for (ClientHandler client : ClientHandler.clients) {
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

        Card card = game.popRandomCard();

        // Check if card is WILD
        while ((card.getType() == CardType.Wild) || (card.getType() == CardType.WildDraw4)) {
            // Add card back to pile
            game.addCard(card);
            // Try again
            card = game.popRandomCard();
        }

        // TODO: Handle Draw2

        for (ClientHandler client : ClientHandler.clients) {
            client.sendFirstPlayedCard(card);
        }
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

    public void setGameStarted(Boolean gameStarted) {
        this.gameStarted = gameStarted;
    }

    public void closeServer(Label numberOfConnectionsLabel) {
        isRunning = false; // Set the flag to stop accepting new clients

        if (this.timer != null) {
            this.timer.cancel(); // Cancel the timer task
        }

        try {
            System.out.println("[Server]: Closing client by client");

            while (!ClientHandler.clients.isEmpty()) {
                closeOneConnection(numberOfConnectionsLabel);
            }

            if (serverSocket != null) {
                serverSocket.close(); // Close the server socket to interrupt accept()
            }

            updateNumberOfConnectionsLabel(numberOfConnectionsLabel);

            System.out.println("[Server]: Server closed!");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("[Server]: Error closing server socket.");
        }
    }

    public void closeOneConnection(Label numberOfConnectionsLabel) throws IOException {
        if (ClientHandler.clients.isEmpty()) {
            System.out.println("[Server]: Can't close connection, client list empty!");
            return;
        }

        ClientHandler client = ClientHandler.clients.getFirst();
        client.sendShutdownMessage();

        updateNumberOfConnectionsLabel(numberOfConnectionsLabel);
    }

    public void setFirstPlayerTurn() {
        ClientHandler.playerTurn = ClientHandler.opponents.getFirst();

        ClientHandler.broadcastPlayerTurn();
    }

}