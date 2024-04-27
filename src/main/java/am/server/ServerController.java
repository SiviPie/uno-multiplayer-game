package am.server;

import am.uno.Game;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.ServerSocket;

public class ServerController {
    // LABELS
    @FXML
    private Label titleLabel;
    @FXML
    private Label serverStatusLabel;
    @FXML
    private Label connectionsLabel;
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
    private Button giveCardsButton;
    @FXML
    private Button printFirstPlayerCardsButton;
    @FXML
    private Button updatePlayerListButtonC;


    private Server server;
    private Game game;

    @FXML
    protected void startServerButtonClick() {

        try {
            int port = Integer.parseInt(portTextField.getText());
            server = new Server(port);

            server.startListening(numberOfConnectionsLabel);

            serverStatusLabel.setText("Server started on port " + port);



            // CREATE GAME
            game = new Game();

            // make start button inactive
            startServerButton.setDisable(true);
            // make stopButton active
            stopServerButton.setDisable(false);

            // make client interraction buttons active
            greetPlayersButton.setDisable(false);
            giveCardsButton.setDisable(false);

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
    private void stopServerButtonClick(ActionEvent event) throws IOException {
        server.closeServer(numberOfConnectionsLabel);

        // make stopButton inactive
        stopServerButton.setDisable(true);

        // make client interraction buttons inactive
        greetPlayersButton.setDisable(true);
        giveCardsButton.setDisable(true);

        // make startButton active
        startServerButton.setDisable(false);

        // update server status label
        serverStatusLabel.setText("Server closed!");
    }

    @FXML
    private void greetPlayersButtonClick(ActionEvent event) throws IOException {
        server.broadcastTextMessage("hehehe");
        System.out.println("[Server]: Greeted clients");
    }

    @FXML
    private void giveCardsButtonClick(ActionEvent event) throws IOException {
        server.giveCardsToPlayers();
        System.out.println("[Server]: Gave cards to players!");
    }

    @FXML
    private void printFirstPlayerCardsButtonClick(ActionEvent event) throws  IOException {
        server.printFirstPlayerCards();
    }


    @FXML
    private void updatePlayerListButtonClick(ActionEvent event) throws IOException {
        server.broadcastPlayerList();
    }


    @FXML
    private void startGameButtonClick(ActionEvent event) throws  IOException {
        server.setGameStarted(true);
        System.out.println("GAME started!");
    }

}