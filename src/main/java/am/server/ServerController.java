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

    private Server server;

    private Boolean serverStarted = false;

    @FXML
    protected void startServerButtonClick() {

        try {
            int port = Integer.parseInt(portTextField.getText());
            server = new Server(port);

            server.startListening(numberOfConnectionsLabel);

            serverStarted = true;

            serverStatusLabel.setText("Server started on port " + port);

            // CREATE GAME
            server.setGame(new Game());

            // make start button inactive
            startServerButton.setDisable(true);
            // make stopButton active
            stopServerButton.setDisable(false);

            // make client interraction buttons active
            greetPlayersButton.setDisable(false);
            startGameButton.setDisable(false);

            // make startButton inactive
            startServerButton.setDisable(true);

            // Set status in gui
            serverStatusLabel.setText("Server started!");

            // You can start the game or perform any other action with intValue
        } catch (NumberFormatException e) {
            // Handle the case when the text cannot be parsed to an integer
            System.out.println("Invalid input. Please enter a valid integer for port.");
            serverStatusLabel.setText("Invalid input.");
        }

    }

    @FXML
    private void stopServerButtonClick() {
        server.closeServer(numberOfConnectionsLabel);

        serverStarted = false;

        // make stopButton inactive
        stopServerButton.setDisable(true);

        // make client interraction buttons inactive
        greetPlayersButton.setDisable(true);
        startGameButton.setDisable(true);

        // make startButton active
        startServerButton.setDisable(false);

        // update server status label
        serverStatusLabel.setText("Server closed!");
    }

    @FXML
    private void greetPlayersButtonClick() {
        server.broadcastTextMessage("Hello!");
        System.out.println("[Server]: Greeted clients");
    }

    @FXML
    private void startGameButtonClick() {
        if (ClientHandler.clients.size() < 2) {
            System.out.println("Now enough players to start the game.");
            return;
        }

        server.setGameStarted(true);
        server.setFirstCard();
        server.giveCardsToPlayers();
        server.setFirstPlayerTurn();

        startGameButton.setDisable(true);

        System.out.println("GAME started!");
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        portTextField.setOnKeyPressed(event -> {
            if (!serverStarted) {
                if (event.getCode() == KeyCode.ENTER) {
                    startServerButtonClick();
                }
            } else {
                System.out.println("Server already started.");
            }
        });
    }
}