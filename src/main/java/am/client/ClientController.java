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


    // GAMEVIEW


    Client client;

    // Injected FXMLLoader for loading the second window
    private FXMLLoader gameLoader;

    // Controller for the second window
    private static GameController gameController;

    static String previousTextMessageSender = "";

    @FXML
    protected void connectButtonClick() {
        // READ THE LABELS
        try {
            if (usernameTextField.getText().isEmpty() || portTextField.getText().isEmpty() || ipTextField.getText().isEmpty()) {
                connectionStatusLabel.setText("Fill in all the fields!");
                return;
            }

            String ip = ipTextField.getText();
            int port = Integer.parseInt(portTextField.getText());

            // handleChangeWindow();

            client = new Client(new Socket(ip, port), usernameTextField.getText());

            //handleChangeFXMLButtonAction(new ActionEvent());

            client.receiveMessageFromServer();
            client.sendUsername();

            System.out.println("[CLIENTCONTROLLER]: Created client");


            openGameWindow();


            // Make connect button inactive
            connectButton.setDisable(true);

            // Make disconnect button active
            disconnectButton.setDisable(false);

            // Set status
            connectionStatusLabel.setText("Connected!");

        } catch (NumberFormatException e) {
            // Handle the case when the text cannot be parsed to an integer
            System.out.println("[CLIENTCONTROLLER]: Invalid input. Please enter a valid integer.");

            // Set status
            connectionStatusLabel.setText("Invalid PORT.");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void addPlayerToList(Opponent player) {
        // TODO

        // VBOX: Spacing 10, Alignment Center
        // - ImageView : fitHeight: 80px
        // - Label : Color white
        // - HBox: Alignment Center
        // -- ImageView : fitHeight: 50, fitWidth: 34.5
        // -- Label: " : number"

        VBox vbox = new VBox();
        vbox.setId("player_" + player.getId());
        vbox.setAlignment(Pos.CENTER);
        vbox.setSpacing(10);

        Image pfpImage = new Image(Objects.requireNonNull(ClientController.class.getResourceAsStream("/am/client/cats/cat" + player.getImageId() + ".jpeg")));
        ImageView pfpImageView = new ImageView(pfpImage);
        pfpImageView.setFitWidth(80);
        pfpImageView.setFitHeight(80);

        // Create a circular clipping mask for the ImageView
        Circle clippingCircle = new Circle(50); // Radius of 40 (half of the desired width/height)
        clippingCircle.setFill(new ImagePattern(pfpImage));
        clippingCircle.setStyle("-fx-stroke:  #52489C;" +
                                "-fx-stroke-width: 2px;");

        Label usernameLabel = new Label(player.getUsername());
        usernameLabel.setFont(Font.font("Arial", 16));
        usernameLabel.setTextFill(Color.WHITE);
        // TODO: Make white

        HBox hbox = new HBox();
        hbox.setAlignment(Pos.CENTER);

        Image cardsImage = new Image(Objects.requireNonNull(ClientController.class.getResourceAsStream("/am/client/img/Deck.png")));
        ImageView cardsImageView = new ImageView(cardsImage);
        cardsImageView.setFitWidth(34.5);
        cardsImageView.setFitHeight(50);

        Label cardsLabel = new Label(" : " + player.getNum_cards());
        cardsLabel.setFont(Font.font("Arial", 16));
        cardsLabel.setTextFill(Color.WHITE);
        // TODO: Make white

        hbox.getChildren().add(cardsImageView);
        hbox.getChildren().add(cardsLabel);

        //vbox.getChildren().add(pfpImageView);
        vbox.getChildren().add(clippingCircle);
        vbox.getChildren().add(usernameLabel);
        vbox.getChildren().add(hbox);

        Platform.runLater(() -> gameController.playerListHbox.getChildren().add(vbox));

        System.out.println("[CLIENTCONTROLLER]: Added player to GUI");
    }

    public static void setPlayerCards(Opponent opponent) {

        Platform.runLater(() -> gameController.setPlayerCards(opponent.getId(), opponent.getNum_cards()));

        System.out.println("SET PLAYER CARDS");
    }

    public static void removePlayerFromList(int id) {
        // TODO
        Platform.runLater(() -> {
            String vboxIdPrefix = "player_" + id;
            // Iterate through children of playerListHbox


            for (Node node : new ArrayList<>(gameController.playerListHbox.getChildren())) {
                // Assuming each child is a VBox
                if (node instanceof VBox) {
                    VBox vbox = (VBox) node;
                    // Check if the ID of the VBox matches the prefix
                    if (vbox.getId() != null && vbox.getId().startsWith(vboxIdPrefix)) {
                        gameController.playerListHbox.getChildren().remove(vbox);
                        break; // Stop iterating once the player is removed
                    }
                }
            }
        });
    }

    public static void addTextMessageFromOthers(String username, String message) {
        /*
        VBOX:
            - Hbox cu username
            - Hbox cu mesajul
         */

        VBox vbox = new VBox();

        HBox hbox_username = new HBox();
        if (!previousTextMessageSender.equals(username)) {
            hbox_username.setAlignment(Pos.CENTER_LEFT);
            hbox_username.setPadding(new Insets(5, 5, 5, 10));

            Text textUsername = new Text(username);
            textUsername.setFont(Font.font("Arial", 14));
            TextFlow usernameFlow = new TextFlow(textUsername);
            usernameFlow.setPadding(new Insets(5, 10, 0, 10));

            hbox_username.getChildren().add(usernameFlow);
        }

        HBox hbox_message = new HBox();
        hbox_message.setAlignment(Pos.CENTER_LEFT);
        hbox_message.setPadding(new Insets(0, 10, 5, 10));

        Text textMessage = new Text(message);
        textMessage.setFont(Font.font("Arial", 16));
        textMessage.setFill(Color.color((double) 82 /255, (double) 72 /255, (double) 156 / 255));
        TextFlow textFlow = new TextFlow(textMessage);
        textFlow.setStyle("-fx-background-color: rgb(233, 233, 235); " +
                          "-fx-background-radius: 20px;");
        textFlow.setPadding(new Insets(8, 14, 8, 14));

        hbox_message.getChildren().add(textFlow);

        if (!previousTextMessageSender.equals(username)) {
            vbox.getChildren().add(hbox_username);
        }
        vbox.getChildren().add(hbox_message);

        previousTextMessageSender = username;

        Platform.runLater(() -> gameController.chatVbox.getChildren().add(vbox));
    }

    public static void addTextMessageFromSelf(String message, String username) {
        previousTextMessageSender = username;

        HBox hbox = new HBox();
        hbox.setAlignment(Pos.CENTER_RIGHT);
        hbox.setPadding(new Insets(5, 10, 5, 10));

        Text text = new Text(message);
        text.setFont(Font.font("Arial", 16));
        text.setFill(Color.color(0.934, 0.945, 0.996)); // change text color

        TextFlow textFlow = new TextFlow(text); // allows us to style and wrap

        textFlow.setStyle("-fx-background-color: #52489C; " +
                          "-fx-background-radius: 20px;");

        textFlow.setPadding(new Insets(8, 14, 8, 14));

        hbox.getChildren().add(textFlow);

        Platform.runLater(() -> gameController.chatVbox.getChildren().add(hbox));
    }

    public static void addCard(Card card) {
        System.out.println("[CLIENTCONTROLLER]: Card name: " + card.getName());

        Image image = new Image(Objects.requireNonNull(ClientController.class.getResourceAsStream("/am/client/img/" + card.getName() + ".png")));
        ImageViewWithCard imageView = new ImageViewWithCard(card, image);
        imageView.setFitWidth(117.3);
        imageView.setFitHeight(170);

        imageView.setCursor(Cursor.HAND);

        // Attach event handler to the ImageView
        imageView.setOnMouseClicked(event -> gameController.handleImageViewClick(imageView));

        Platform.runLater(() -> gameController.cardListFlowPane.getChildren().add(imageView));
    }

    public static void setLastPlayedCard(Card card) {
        System.out.println("[CLIENTCONTROLLER]: Card name: " + card.getName());

        Image image = new Image(Objects.requireNonNull(ClientController.class.getResourceAsStream("/am/client/img/" + card.getName() + ".png")));
        ImageViewWithCard imageView = new ImageViewWithCard(card, image);
        imageView.setFitWidth(117.3);
        imageView.setFitHeight(170);

        Platform.runLater(() -> gameController.setLastPlayedCard(imageView));
    }

    public static void deselectCard() {
        Platform.runLater(() -> gameController.deselectCard());
    }

    @FXML
    private AnchorPane mainClientAnchorPane;

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

            // Add Listener to the text field
            gameController.textMessageField.setOnKeyPressed(event -> {
                if (event.getCode() == KeyCode.ENTER) {
                    try {
                        gameController.sendMessageButtonClick();
                    } catch (IOException e) {
                        e.printStackTrace(); // Handle the exception appropriately
                    }
                }
            });

            // Set properties
            gameController.chatVbox.heightProperty().addListener((observableValue, number, t1) -> gameController.chatScrollPane.setVvalue((Double) t1));


            // Get the screen dimensions
            double screenWidth = Screen.getPrimary().getVisualBounds().getWidth();
            double screenHeight = Screen.getPrimary().getVisualBounds().getHeight();

            // Set the window size to half of the screen size
            double windowWidth = 5 * screenWidth / 6;
            double windowHeight = 5 * screenHeight / 6;

            // Set the window position to the left half of the screen
            // double windowX = 0;
            // double windowY = 0;

            // Create a new stage for the game window
            Stage gameStage = new Stage();
            gameStage.setTitle("Game Window");
            gameStage.setScene(new Scene(root));

            // gameStage.setX(windowX);
            // gameStage.setY(windowY);
            gameStage.setWidth(windowWidth);
            gameStage.setHeight(windowHeight);
            //gameStage.setFullScreen(true);

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
        connectionStatusLabel.setText("Disconnected!");
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Add Listener to the text fields

        ipTextField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                connectButtonClick();
            }
        });
        // Add Listener to the text fields
        portTextField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                connectButtonClick();
            }
        });
        // Add Listener to the text fields
        usernameTextField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                connectButtonClick();
            }
        });

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
