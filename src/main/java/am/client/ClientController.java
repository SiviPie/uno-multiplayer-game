package am.client;

import am.uno.Player;
import am.uno.Opponent;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

public class ClientController implements GameController.WindowCloseListener  {
    // LABELS
    @FXML
    private Label titleLabel;
    @FXML
    private Label connectionStatusLabel;

    // TEXTFIELDS
    @FXML
    private TextField ipTextField;
    @FXML
    private TextField portTextField;
    @FXML
    private TextField usernameTextField;

    // BUTTONS
    @FXML
    private Button connectButton;
    @FXML
    private Button disconnectButton;


    Client client;

    // Injected FXMLLoader for loading the second window
    private FXMLLoader gameLoader;

    // Controller for the second window
    private static GameController gameController;

    Player player;

    @FXML
    protected void connectButtonClick() {
        // READ THE LABELS
        try {
            if (usernameTextField.getText().isEmpty() || portTextField.getText().isEmpty() || ipTextField.getText().isEmpty()) {
                connectionStatusLabel.setText("Please fill the fields!");
                return;
            }

            String ip = ipTextField.getText();
            int port = Integer.parseInt(portTextField.getText());

            client = new Client(new Socket(ip, port), usernameTextField.getText());

            client.receiveMessageFromServer();
            client.sendUsername();

            System.out.println("Created client");

            openGameWindow();

            // Make connect button inactive
            connectButton.setDisable(true);

            // Make disconnect button active
            disconnectButton.setDisable(false);

            // Set status
            connectionStatusLabel.setText("Connected!");

        } catch (NumberFormatException e) {
            // Handle the case when the text cannot be parsed to an integer
            System.out.println("Invalid input. Please enter a valid integer.");

            // Set status
            connectionStatusLabel.setText("Invalid input (port must be a number).");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void addPlayerToList(Opponent player) {
        // TODO
        VBox vbox = new VBox();
        vbox.setAlignment(Pos.CENTER);
        vbox.setSpacing(20);

        Label username = new Label(player.getUsername());
        Label cards = new Label(String.valueOf(player.getNum_cards()));
        Button uno_button = new Button("Uno!");

        vbox.getChildren().add(username);
        vbox.getChildren().add(cards);
        vbox.getChildren().add(uno_button);

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Platform.runLater(() -> gameController.playerListHbox.getChildren().add(vbox));
            }
        });

        System.out.println("Added player to GUI");
    }

    public static void removePlayerFromList(int id) {
        // TODO
    }

    public static void addTextMessageFromOthers(String username, String message) {
        /*
        VBOX:
            - Hbox cu username
            - Hbox cu mesajul
         */
        VBox vbox = new VBox();

        HBox hbox_username = new HBox();
        hbox_username.setAlignment(Pos.CENTER_LEFT);
        hbox_username.setPadding(new Insets(5, 5, 5, 10));

        Text textUsername = new Text(username);
        TextFlow usernameFlow = new TextFlow(textUsername);
        usernameFlow.setPadding(new Insets(5, 10, 5, 10));

        hbox_username.getChildren().add(textUsername);

        HBox hbox_message = new HBox();
        hbox_message.setAlignment(Pos.CENTER_LEFT);
        hbox_message.setPadding(new Insets(5, 5, 5, 10));

        Text textMessage = new Text(message);
        TextFlow textFlow = new TextFlow(textMessage);
        textFlow.setStyle("-fx-background-color: rgb(233, 233, 235); " +
                "-fx-background-radius: 20px;");
        textFlow.setPadding(new Insets(5, 10, 5, 10));

        hbox_message.getChildren().add(textFlow);

        vbox.getChildren().add(hbox_username);
        vbox.getChildren().add(hbox_message);

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                gameController.chatVbox.getChildren().add(vbox);
            }
        });
    }

    public static void addTextMessageFromSelf(String message) {
        HBox hbox = new HBox();
        hbox.setAlignment(Pos.CENTER_RIGHT);
        hbox.setPadding(new Insets(5, 5, 5, 10));

        Text text = new Text(message);
        TextFlow textFlow = new TextFlow(text); // allows us to style and wrap

        textFlow.setStyle("-fx-color: rgb(239, 242, 255); " +
                "-fx-background-color: rgb(15, 25, 242); " +
                "-fx-background-radius: 20px;");

        textFlow.setPadding(new Insets(2, 10, 5, 10));
        text.setFill(Color.color(0.934, 0.945, 0.996)); // change text color

        hbox.getChildren().add(textFlow);

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                gameController.chatVbox.getChildren().add(hbox);
            }
        });
    }

    private void openGameWindow() {
        try {
            // Load the FXML file for the game window
            gameLoader = new FXMLLoader(getClass().getResource("game-view.fxml"));
            Parent root = gameLoader.load();

            // Get the controller of the second window
            gameController = gameLoader.getController();

            //gameController.updateGameStatus(player.getUsername());
            gameController.setClient(client);
            gameController.setWindowCloseListener(this);

            // Set properties
            gameController.chatVbox.heightProperty().addListener(new ChangeListener<Number>() {
                @Override
                public void changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
                    gameController.chatScrollPane.setVvalue((Double) t1);
                }
            });

            // Create a new stage for the game window
            Stage gameStage = new Stage();
            gameStage.setTitle("Game Window");
            gameStage.setScene(new Scene(root));

            // Show the game window
            gameStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onWindowClose() {
        // Disable the disconnect button when the game window is closed
        // Make connect button active
        connectButton.setDisable(false);
        disconnectButton.setDisable(true);
    }

    @FXML
    protected void disconnectButtonClick() {
        client.closeEverything();

        // Close the second window
        gameController.closeWindow();

        // Make connect button active
        connectButton.setDisable(false);

        // Make disconnect button inactive
        disconnectButton.setDisable(true);

        // Set status
        connectionStatusLabel.setText("Disconnected!");
    }
}
