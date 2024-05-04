package am.client;

import am.uno.CardType;
import am.uno.Color;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;

import java.io.IOException;

public class GameController {

    // LABELS
    @FXML
    private Label connectionStatusLabel;
    @FXML
    private Label playerTurnLabel;

    // TEXTFIELDS
    @FXML
    protected TextField textMessageField;

    // BUTTONS
    @FXML
    private Button sendMessageButton;
    @FXML
    private Button playCardButton;
    @FXML
    private Button drawButton;
    @FXML
    private Button unoButton;

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
    protected HBox cardsStackHbox;
    @FXML
    protected HBox colorPickerHbox;

    // VBOX
    @FXML
    protected VBox chatVbox;

    // CIRCLE
    @FXML
    protected Circle wildColorCircle;

    // IMAGEVIEW
    @FXML
    private ImageView playedCardImage;
    private ImageViewWithCard selectedImageView = null;
    private Color selectedColor = null;

    // SVGPATH
    @FXML
    private SVGPath directionLeft;
    @FXML
    private SVGPath directionRight;

    private Client client;

    public interface WindowCloseListener {
        void onWindowClose();
    }

    private WindowCloseListener windowCloseListener;

    public void setWindowCloseListener(WindowCloseListener listener) {
        this.windowCloseListener = listener;
    }


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

    public void setPlayerSkipTurns(int id, int leftSkipTurns) {
        VBox vbox = (VBox) playerListHbox.lookup("#player_" + id);

        // Selectam label-ul
        Label skipLabel = (Label) vbox.getChildren().get(3);

        // Modificam label-ul
        skipLabel.setText("Skip: " + leftSkipTurns);
    }

    public void setPlayerTurn(String username, boolean isMyTurn) {
        playerTurnLabel.setText(username);

        if (isMyTurn) {
            playCardButton.setDisable(false);
            drawButton.setDisable(false);
            unoButton.setDisable(false);
        } else {
            playCardButton.setDisable(true);
            drawButton.setDisable(true);
            unoButton.setDisable(true);
        }
    }

    @FXML
    public void playCardButtonClick() throws IOException {
        if (selectedImageView == null) {
            return;
        }

        client.playCard(selectedImageView.getCard(), selectedColor);

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

        // Ckeck if the card is valid based on the last played card
        if (!client.isCardPlayable(imageView.getCard())) {
            return;
        }

        if (imageView.getCard().getType() == CardType.Wild || imageView.getCard().getType() == CardType.WildDraw4) {
            colorPickerHbox.setVisible(true);
        } else {
            deselectColor();
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

    public void setWildColor(Color color) {
        switch (color) {
            case Red:
                wildColorCircle.setFill(javafx.scene.paint.Color.rgb(239, 91, 91));
            break;

            case Blue:
                wildColorCircle.setFill(javafx.scene.paint.Color.rgb(86, 207, 225));
            break;

            case Green:
                wildColorCircle.setFill(javafx.scene.paint.Color.rgb(105, 194, 141));
            break;

            case Yellow:
                wildColorCircle.setFill(javafx.scene.paint.Color.rgb(225, 213, 86));
            break;
        }

        wildColorCircle.setVisible(true);
    }

    public void clearWildColor() {
        wildColorCircle.setVisible(false);
    }

    public void setDirection(boolean direction) {
        if (direction) {
            directionLeft.setFill(javafx.scene.paint.Color.BLACK);
            directionRight.setFill(javafx.scene.paint.Color.rgb(72, 227, 193));
        } else {
            directionLeft.setFill(javafx.scene.paint.Color.rgb(72, 227, 193));
            directionRight.setFill(javafx.scene.paint.Color.BLACK);
        }
    }

    @FXML
    public void unoButtonClick() {
        if (client.player.getCards().getSize() == 2) {
            client.saidUno = true;
        } else {
            System.out.println("[GameController]: Sorry, you can't press UNO as you have more than 2 cards.");
        }
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
