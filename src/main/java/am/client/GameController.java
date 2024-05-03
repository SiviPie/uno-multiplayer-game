package am.client;

import am.server.ClientHandler;
import am.uno.CardType;
import am.uno.Color;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.IOException;

public class GameController {

    public interface WindowCloseListener {
        void onWindowClose();
    }

    private WindowCloseListener windowCloseListener;

    public void setWindowCloseListener(WindowCloseListener listener) {
        this.windowCloseListener = listener;
    }

    // LABELS
    @FXML
    public Label connectionStatusLabel;

    // TEXTFIELDS
    @FXML
    protected TextField textMessageField;

    // BUTTONS
    @FXML
    private Button sendMessageButton;

    @FXML
    private Button redColorButton;
    @FXML
    private Button greenColorButton;
    @FXML
    private Button yellowColorButton;
    @FXML
    private Button blueColorButton;


    // FLOWPANE
    @FXML
    protected FlowPane cardListFlowPane;

    // SCROLLPANE
    @FXML
    protected ScrollPane chatScrollPane;

    // HBOX
    @FXML
    protected HBox playerListHbox;

    @FXML
    protected HBox colorPickerHbox;

    // VBOX
    @FXML
    protected VBox chatVbox;

    // IMAGEVIEW

    @FXML
    private ImageView playedCardImage;
    private ImageViewWithCard selectedImageView = null;
    private Color selectedColor = null;

    private Client client;

    // Method to close the game window
    public void closeWindow() {
        if (windowCloseListener != null) {
            windowCloseListener.onWindowClose();
        }
        Stage stage = (Stage) chatVbox.getScene().getWindow();
        stage.close();
    }

    public void setClient(Client client) {
        this.client = client;
    }

    @FXML
    public void sendMessageButtonClick() throws IOException {
        // Get text from label only if it's not empty
        if (!textMessageField.getText().isEmpty()) {
            client.sendTextMessage(textMessageField.getText());
            textMessageField.clear();
        }

    }

    public void setPlayerCards(int id, int num_cards) {

        VBox vbox = (VBox) playerListHbox.lookup("#player_" + id);

        // Selectam label-ul
        HBox hbox = (HBox) vbox.getChildren().get(2);
        Label cardsLabel = (Label) hbox.getChildren().get(1);

        // Modificam label-ul
        cardsLabel.setText(" : " + num_cards);
    }

    @FXML
    public void playCardButtonClick() throws IOException {
        if (selectedImageView == null) {
            return;
        }

        client.playCard(selectedImageView.getCard());

        // Remove card from here
        cardListFlowPane.getChildren().remove(selectedImageView);

        selectedImageView = null;
        selectedColor = null;
    }

    @FXML
    public void drawCardButtonClick() throws IOException {
        client.drawCard();
    }

    public void deselectCard() {
        if (selectedImageView != null) {
            selectedImageView.setFitWidth(117.3); // Reset fit width
            selectedImageView.setFitHeight(170); // Reset fit height
        }

        selectedImageView = null;
    }

    protected void handleImageViewClick(ImageViewWithCard imageView) {
        // First check if it's our turn
        // TODO

        // Ckeck if the card is valid based on the last played card
        if (!client.isCardPlayable(imageView.getCard())) {
            return;
        }

        if (imageView.getCard().getType() == CardType.Wild || imageView.getCard().getType() == CardType.WildDraw4) {
            colorPickerHbox.setVisible(true);
        }

        // If the clicked ImageView is already selected, do nothing
        if (imageView == selectedImageView) {
            return;
        }

        // Deselect the previously selected ImageView
        deselectCard();

        // Select the clicked ImageView
        selectedImageView = imageView;

        // Modify size
        selectedImageView.setFitWidth(138); // Set fit width a little bigger
        selectedImageView.setFitHeight(200); // Set fit height a little bigger
    }

    protected void setLastPlayedCard(ImageViewWithCard imageView) {
        playedCardImage.setImage(imageView.getImage());
    }

    @FXML
    public void chooseColorRed() {
        chooseColor(Color.Red);
    }

    @FXML
    public void chooseColorGreen() {
        chooseColor(Color.Green);
    }

    @FXML
    public void chooseColorBlue() {
        chooseColor(Color.Blue);
    }

    @FXML
    public void chooseColorYellow() {
        chooseColor(Color.Yellow);
    }

    public void deselectColor() {
        this.selectedColor = null;
    }

    public void chooseColor(Color color) {
        selectedColor = color;
        System.out.println("Selected color: " + color);
        colorPickerHbox.setVisible(false);
    }

    @FXML
    public void disconnectButtonClick() throws IOException {
        if (client != null) {
            client.sendDisconnectMessage();
            client.closeEverything();
        }

        this.closeWindow();
    }
}
