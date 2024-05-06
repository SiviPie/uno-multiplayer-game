package am.client;

import am.uno.Card;
import am.uno.Opponent;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;
import java.util.ResourceBundle;

public class ClientController implements GameController.WindowCloseListener, Initializable {
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

    // Client object
    Client client;

    // Controller for the second (game) window
    private static GameController gameController;

    private static String previousTextMessageSender = "";

    public static void setDirection(boolean direction) {
        Platform.runLater(() -> gameController.setDirection(direction));
    }

    @FXML
    protected void connectButtonClick() {
        // Read the labels
        try {
            // Check if the fields have been filled
            if (usernameTextField.getText().isEmpty() || portTextField.getText().isEmpty() || ipTextField.getText().isEmpty()) {
                connectionStatusLabel.setText("Fill in all the fields!");
                return;
            }

            // Get the data
            String ip = ipTextField.getText();
            int port = Integer.parseInt(portTextField.getText());

            // Create the client object
            client = new Client(new Socket(ip, port), usernameTextField.getText());

            System.out.println("[CLIENTCONTROLLER]: Created client");

            // Start listening to the server
            client.receiveMessageFromServer();

            // Send username in order to be assigned a player object
            client.sendUsername();

            // Load the FXML data for the game window
            openGameWindow();

            // Make connect button inactive
            connectButton.setDisable(true);

            // Make disconnect button active
            disconnectButton.setDisable(false);

            // Set status label
            connectionStatusLabel.setText("Connected!");

        } catch (NumberFormatException e) {
            // Handle the case when the text cannot be parsed to an integer
            System.out.println("[CLIENTCONTROLLER]: The port must be an integer.");

            // Set status
            connectionStatusLabel.setText("Invalid PORT.");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void addPlayerToList(Opponent player) {
        // Structure:
        // Vbox
        // - Circle
        // - Username Label
        // - HBox
        // -- ImageView
        // -- Cards Label
        // - Skip Label

        // Create the VBox
        VBox vbox = new VBox();
        vbox.setId("player_" + player.getId());
        vbox.setAlignment(Pos.CENTER);
        vbox.setSpacing(10);

        // Create the Image for the profile picture
        Image pfpImage = new Image(Objects.requireNonNull(ClientController.class.getResourceAsStream("/am/client/img/cats/cat" + player.getImageId() + ".jpeg")));
        ImageView pfpImageView = new ImageView(pfpImage);

        // Set image dimensions
        pfpImageView.setFitWidth(80);
        pfpImageView.setFitHeight(80);

        // Create a circular clipping mask for the ImageView
        Circle clippingCircle = new Circle(50);
        clippingCircle.setFill(new ImagePattern(pfpImage));
        clippingCircle.setStyle("-fx-stroke:  #52489C;" +
                                "-fx-stroke-width: 2px;");

        // Create the Username Label
        Label usernameLabel = new Label(player.getUsername());
        usernameLabel.setFont(Font.font("Arial", 16));
        usernameLabel.setTextFill(Color.WHITE);

        // Create the HBox
        HBox hbox = new HBox();
        hbox.setAlignment(Pos.CENTER);

        // Create the card image
        Image cardsImage = new Image(Objects.requireNonNull(ClientController.class.getResourceAsStream("/am/client/img/cards/Deck.png")));
        ImageView cardsImageView = new ImageView(cardsImage);

        // Set image dimensions
        cardsImageView.setFitWidth(34.5);
        cardsImageView.setFitHeight(50);

        // Create the Cards Label (number of cards the player holds)
        Label cardsLabel = new Label(" : " + player.getNum_cards());
        cardsLabel.setFont(Font.font("Arial", 16));
        cardsLabel.setTextFill(Color.WHITE);

        // Add Card Image and Cards Label to HBox
        hbox.getChildren().add(cardsImageView);
        hbox.getChildren().add(cardsLabel);

        // Create Skip Label (number of turns the player must skip)
        Label skipLabel = new Label("Skip: " + player.getLeftSkipTurns());
        skipLabel.setFont(Font.font("Arial", 16));
        skipLabel.setTextFill(Color.WHITE);

        // Add elements to VBox
        vbox.getChildren().add(clippingCircle);
        vbox.getChildren().add(usernameLabel);
        vbox.getChildren().add(hbox);
        vbox.getChildren().add(skipLabel);

        // Apply the change to game GUI
        Platform.runLater(() -> gameController.playerListHbox.getChildren().add(vbox));

        System.out.println("[CLIENTCONTROLLER]: Added player to GUI");
    }

    public static void setPlayerCards(Opponent opponent) {
        // Set the number of cards a player holds
        Platform.runLater(() -> gameController.setPlayerCards(opponent.getId(), opponent.getNum_cards()));

        System.out.println("[CLIENTCONTROLLER]: Set Player " + opponent.getUsername() + " cards");
    }

    public static void setPlayerSkipTurns(Opponent opponent) {
        // Set the number of turns a player has to skip
        Platform.runLater(() -> gameController.setPlayerSkipTurns(opponent.getId(), opponent.getLeftSkipTurns()));

        System.out.println("[CLIENTCONTROLLER]: Set Player " + opponent.getUsername() + " skip turns");
    }

    public static void removePlayerFromList(int id) {
        // Remove a player's info from GUI based on player ID
        Platform.runLater(() -> gameController.removePlayer(id));

        System.out.println("[CLIENTCONTROLLER]: Removed player with ID " + id);
    }

    public static void setPlayerTurn(String username, boolean isMyTurn) {
        // Update the GUI based on whose turn it is
        Platform.runLater(() -> gameController.setPlayerTurn(username, isMyTurn));

        System.out.println("[CLIENTCONTROLLER]: Set player turn: " + username + ". Is that me? " + isMyTurn);
    }

    public static void addTextMessageFromOthers(String username, String message) {
        // Structure:
        // VBox:
        // - HBox username (optional)
        // -- TextFlow containing the username
        // - HBox text message
        // -- TextFlow containing the text message

        // Create the VBox object
        VBox vbox = new VBox();

        // Declare the HBox object for username
        HBox hbox_username = new HBox();

        // Add the username label only if the previous message has not been sent by the same player
        if (!previousTextMessageSender.equals(username)) {
            // Create the HBox object
            hbox_username.setAlignment(Pos.CENTER_LEFT);
            hbox_username.setPadding(new Insets(5, 5, 5, 10));

            // Create the Text object
            Text textUsername = new Text(username);
            textUsername.setFont(Font.font("Arial", 14));
            // Create the TextFlow object
            TextFlow usernameFlow = new TextFlow(textUsername);
            usernameFlow.setPadding(new Insets(5, 10, 0, 10));

            // Add TextFlow to HBox
            hbox_username.getChildren().add(usernameFlow);
        }

        // Create the HBox object for the message
        HBox hbox_message = new HBox();
        hbox_message.setAlignment(Pos.CENTER_LEFT);
        hbox_message.setPadding(new Insets(0, 10, 5, 10));

        // Create the Text object
        Text textMessage = new Text(message);
        textMessage.setFont(Font.font("Arial", 16));
        textMessage.setFill(Color.color((double) 82 /255, (double) 72 /255, (double) 156 / 255));
        // Create the TextFlow object
        TextFlow textFlow = new TextFlow(textMessage);
        textFlow.setStyle("-fx-background-color: rgb(233, 233, 235); " +
                          "-fx-background-radius: 20px;");
        textFlow.setPadding(new Insets(8, 14, 8, 14));

        // Add the TextFlow to the HBox
        hbox_message.getChildren().add(textFlow);

        // Add the HBox with the username only if it applies
        if (!previousTextMessageSender.equals(username)) {
            vbox.getChildren().add(hbox_username);
        }

        // Add the HBox to VBox
        vbox.getChildren().add(hbox_message);

        // Update the previous sender variable
        previousTextMessageSender = username;

        // Apply the changes in game window
        Platform.runLater(() -> gameController.chatVbox.getChildren().add(vbox));

        System.out.println("[CLIENTCONTROLLER]: Added text message from other player to GUI.");
    }

    public static void addTextMessageFromSelf(String message, String username) {
        // Structure:
        // HBox
        // - TextFlow

        // Create the HBox object
        HBox hbox = new HBox();
        hbox.setAlignment(Pos.CENTER_RIGHT);
        hbox.setPadding(new Insets(5, 10, 5, 10));

        // Create Text object
        Text text = new Text(message);
        text.setFont(Font.font("Arial", 16));
        text.setFill(Color.color(0.934, 0.945, 0.996)); // change text color

        // Create TextFlow object
        TextFlow textFlow = new TextFlow(text); // allows us to style and wrap
        textFlow.setStyle("-fx-background-color: #52489C; " +
                          "-fx-background-radius: 20px;");
        textFlow.setPadding(new Insets(8, 14, 8, 14));

        // Add TextFlow to HBox
        hbox.getChildren().add(textFlow);

        // Apply the changes
        Platform.runLater(() -> gameController.chatVbox.getChildren().add(hbox));

        System.out.println("[CLIENTCONTROLLER]: Added text message from self to GUI.");

        // Update the previous sender variable
        previousTextMessageSender = username;
    }

    public static void addCard(Card card) {
        // Create Image object
        Image image = new Image(Objects.requireNonNull(ClientController.class.getResourceAsStream("/am/client/img/cards/" + card.getName() + ".png")));

        // Create ImageViewWithCard object based on Image and Card
        ImageViewWithCard imageView = new ImageViewWithCard(card, image);

        // Set dimensions
        imageView.setFitWidth(117.3);
        imageView.setFitHeight(170);

        // Set cursor
        imageView.setCursor(Cursor.HAND);

        // Attach event handler to the ImageView
        imageView.setOnMouseClicked(event -> gameController.handleImageViewClick(imageView));

        // Apply the changes
        Platform.runLater(() -> gameController.cardListFlowPane.getChildren().add(imageView));

        System.out.println("[CLIENTCONTROLLER]: Added card " + card.getName() + " to GUI");
    }

    public static void setLastPlayedCard(Card card) {
        // Create Image object
        Image image = new Image(Objects.requireNonNull(ClientController.class.getResourceAsStream("/am/client/img/cards/" + card.getName() + ".png")));

        // Create ImageViewWithCard object based on Image and Card
        ImageViewWithCard imageView = new ImageViewWithCard(card, image);

        // Set dimensions
        imageView.setFitWidth(117.3);
        imageView.setFitHeight(170);

        // Apply changes
        Platform.runLater(() -> gameController.setLastPlayedCard(imageView));

        System.out.println("[CLIENTCONTROLLER]: Added last played card " + card.getName() + " to GUI");
    }

    public static void deselectCard() {
        // Run the method
        Platform.runLater(() -> gameController.deselectCard());
    }

    private void openGameWindow() {
        try {
            // Inject FXMLLoader for loading the second window
            FXMLLoader gameLoader = new FXMLLoader(getClass().getResource("game-view.fxml"));
            // Load the FXML file for the game window
            Parent root = gameLoader.load();

            // Get the controller of the second window
            gameController = gameLoader.getController();

            // Share the client object
            gameController.setClient(client);

            // Set Window Listener for close event
            gameController.setWindowCloseListener(this);

            // Add Listener to the message text field (pressing ENTER = calling method for sending the message)
            gameController.textMessageField.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ENTER) {
                    try {
                        gameController.sendMessageButtonClick();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

            // Make sure the chat container scrolls to the bottom when adding a new text message
            gameController.chatVbox.heightProperty().addListener((observableValue, number, t1) -> gameController.chatScrollPane.setVvalue((Double) t1));

            // Get the screen dimensions
            double screenWidth = Screen.getPrimary().getVisualBounds().getWidth();
            double screenHeight = Screen.getPrimary().getVisualBounds().getHeight();

            // Set the window size to half of the screen size
            double windowWidth = 5 * screenWidth / 6;
            double windowHeight = 5 * screenHeight / 6;

            // Create a new stage for the game window
            Stage gameStage = new Stage();
            gameStage.setTitle("Game Window");
            gameStage.setScene(new Scene(root));

            gameStage.setWidth(windowWidth);
            gameStage.setHeight(windowHeight);
            gameStage.setTitle("UNO Game");

            // Show the game window
            gameStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void addCardToStack(Card card) {
        // Create Image object
        Image image = new Image(Objects.requireNonNull(ClientController.class.getResourceAsStream("/am/client/img/cards/" + card.getName() + ".png")));

        // Create ImageViewWithCard object based on Image and Card
        ImageViewWithCard imageView = new ImageViewWithCard(card, image);

        // Set dimensions
        imageView.setFitWidth(55.2);
        imageView.setFitHeight(80);

        // Apply the changes
        Platform.runLater(() -> gameController.cardsStackHbox.getChildren().add(imageView));
    }

    public static void clearCardsStack() {
        // Call the method created in Game Controller
        Platform.runLater(() -> gameController.cardsStackHbox.getChildren().clear());
    }

    public static void setWildColor(am.uno.Color color) {
        // Call the method created in Game Controller
        Platform.runLater(() -> gameController.setWildColor(color));
    }

    public static void removeWildColor() {
        // Call the method created in Game Controller
        Platform.runLater(() -> gameController.clearWildColor());
    }

    @Override
    public void onWindowClose() {
        // Make connect button active
        connectButton.setDisable(false);

        // Disable the disconnect button
        disconnectButton.setDisable(true);

        // Update connection status Label
        connectionStatusLabel.setText("Disconnected!");
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Add Listener to the text fields, pressing ENTER = trying to connect

        ipTextField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                connectButtonClick();
            }
        });

        portTextField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                connectButtonClick();
            }
        });

        usernameTextField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                connectButtonClick();
            }
        });

    }

    @FXML
    protected void disconnectButtonClick() {
        // Close the connection
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
