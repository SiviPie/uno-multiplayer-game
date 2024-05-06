package am.server;

import am.uno.Game;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import java.net.URL;
import java.util.ResourceBundle;

public class ServerController  implements Initializable {
    // LABELS
    @FXML
    private Label serverStatusLabel;
    @FXML
    private Label numberOfConnectionsLabel;

    // TEXTFIELDS
    @FXML
    private TextField portTextField;

    // BUTTONS
    @FXML
    private Button startServerButton;
    @FXML
    private Button stopServerButton;
    @FXML
    private Button greetPlayersButton;
    @FXML
    private Button startGameButton;

    // Server object
    private Server server;

    // Status variable
    private Boolean serverStarted = false;

    @FXML
    protected void startServerButtonClick() {
        try {
            // Get the port from the port text field
            int port = Integer.parseInt(portTextField.getText());

            // Create a new Server object and store it
            server = new Server(port);

            // Start listening for clients
            server.startListening(numberOfConnectionsLabel);

            // Update status variable
            serverStarted = true;

            // Update status label in GUI
            serverStatusLabel.setText("Server started on port " + port);

            // Create a new game
            server.setGame(new Game());

            // Make start server button inactive
            startServerButton.setDisable(true);
            // Make stop server button active
            stopServerButton.setDisable(false);
            // Make start game button active
            startGameButton.setDisable(false);
            // Make greet players button active
            greetPlayersButton.setDisable(false);

            // Set status in GUI
            serverStatusLabel.setText("Server started!");
        } catch (NumberFormatException e) {
            System.out.println("[ServerController]: Invalid input. Please enter a valid integer for port.");
            serverStatusLabel.setText("Invalid port.");
        }
    }

    @FXML
    private void stopServerButtonClick() {
        // Close the connections
        server.closeServer(numberOfConnectionsLabel);

        serverStarted = false;

        server.setGameStarted(false);

        // Make stop button inactive
        stopServerButton.setDisable(true);
        // make start server button active
        startServerButton.setDisable(false);
        // Make start game button inactive
        startGameButton.setDisable(true);
        // Make greet players button inactive
        greetPlayersButton.setDisable(true);

        // Update server status label
        serverStatusLabel.setText("Server closed!");
    }

    @FXML
    private void greetPlayersButtonClick() {
        // Send greetings to all the clients
        server.broadcastTextMessage("Hello!");

        System.out.println("[ServerController]: Greeted clients");
    }

    @FXML
    private void startGameButtonClick() {
        // Start game only if there are at least 2 players
        if (ClientHandler.clients.size() < 2) {
            System.out.println("[ServerController]: Not enough players to start the game.");
            return;
        }

        server.setGameStarted(true);

        // Set the cards
        server.setFirstCard();
        server.giveCardsToPlayers();

        // Start the turn
        server.setFirstPlayerTurn();

        startGameButton.setDisable(true);

        System.out.println("[ServerController]: Game started!");
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        portTextField.setOnKeyPressed(event -> {
            if (!serverStarted) {
                if (event.getCode() == KeyCode.ENTER) {
                    startServerButtonClick();
                }
            } else {
                System.out.println("[ServerController]: Server already started.");
            }
        });
    }
}