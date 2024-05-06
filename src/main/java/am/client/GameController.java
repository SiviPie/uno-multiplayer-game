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
    private Label playerTurnLabel;

    // TEXTFIELDS
    @FXML
    protected TextField textMessageField;

    // BUTTONS
    @FXML
    private Button playCardButton;
    @FXML
    private Button drawButton;
    @FXML
    private Button unoButton;

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

    // Client object
    private Client client;

    // Listener variable
    private WindowCloseListener windowCloseListener;

    public interface WindowCloseListener {
        void onWindowClose();
    }

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

    public void removePlayer(int id) {
        // Select the corresponding vbox
        VBox vbox = (VBox) playerListHbox.lookup("#player_" + id);

        // Remove it from player list hbox
        playerListHbox.getChildren().remove(vbox);
    }

    @FXML
    public void sendMessageButtonClick() throws IOException {
        // Get text from label only if it's not empty
        if (!textMessageField.getText().isEmpty()) {
            // Send the message
            client.sendTextMessage(textMessageField.getText());

            // Clear the text field
            textMessageField.clear();
        }

    }

    public void setPlayerCards(int id, int num_cards) {
        // Select the corresponding vbox
        VBox vbox = (VBox) playerListHbox.lookup("#player_" + id);

        // Select the player's hbox
        HBox hbox = (HBox) vbox.getChildren().get(2);

        // Select the label containing the number of cards
        Label cardsLabel = (Label) hbox.getChildren().get(1);

        // Update the label text
        cardsLabel.setText(" : " + num_cards);
    }

    public void setPlayerSkipTurns(int id, int leftSkipTurns) {
        // Select the player's vbox
        VBox vbox = (VBox) playerListHbox.lookup("#player_" + id);

        // Select the label containing the number of skips
        Label skipLabel = (Label) vbox.getChildren().get(3);

        // Update the label
        skipLabel.setText("Skip: " + leftSkipTurns);
    }

    public void setPlayerTurn(String username, boolean isMyTurn) {
        // Update the player turn Label
        playerTurnLabel.setText(username);

        // Enable or disable buttons based on if it's this client's player's turn
        if (isMyTurn) {
            // Enable all the buttons
            playCardButton.setDisable(false);
            drawButton.setDisable(false);
            unoButton.setDisable(false);
        } else {
            // Disable all the buttons
            playCardButton.setDisable(true);
            drawButton.setDisable(true);
            unoButton.setDisable(true);
        }
    }

    @FXML
    public void playCardButtonClick() {
        // If nothing has been selected, return
        if (selectedImageView == null) {
            return;
        }

        // Play the selected card
        client.playCard(selectedImageView.getCard(), selectedColor);

        // Remove card from player's card deck (GUI)
        cardListFlowPane.getChildren().remove(selectedImageView);

        // Forget the image and/or color selected, if any
        selectedImageView = null;
        selectedColor = null;
    }

    @FXML
    public void drawCardButtonClick() {
        // Call the client's method to draw card
        client.drawCard();
    }

    public void deselectCard() {
        // Make sure all the cards images have the right dimensions
        if (selectedImageView != null) {
            selectedImageView.setFitWidth(117.3); // Reset fit width
            selectedImageView.setFitHeight(170); // Reset fit height
        }

        selectedImageView = null;
    }

    protected void handleImageViewClick(ImageViewWithCard imageView) {
        // Ckeck if the card can be playes based on the last played card
        if (!client.isCardPlayable(imageView.getCard())) {
            return;
        }

        // Show color pop-up only if we selected a wild card
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

        // Change dimensions
        selectedImageView.setFitWidth(138); // Set fit width a little bigger
        selectedImageView.setFitHeight(200); // Set fit height a little bigger
    }

    protected void setLastPlayedCard(ImageViewWithCard imageView) {
        // Update the last played card
        playedCardImage.setImage(imageView.getImage());
    }

    public void chooseColor(Color color) {
        selectedColor = color;
        System.out.println("Selected color: " + color);
        colorPickerHbox.setVisible(false);
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

    public void setWildColor(Color color) {
        // Fill color circle based on the chosen color
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

        // Make color circle visible
        wildColorCircle.setVisible(true);
    }

    public void clearWildColor() {
        // Hide color circle
        wildColorCircle.setVisible(false);
    }

    public void setDirection(boolean direction) {
        // Color the direction triangles based on direction
        if (direction) {
            // Left to right
            directionLeft.setFill(javafx.scene.paint.Color.BLACK);
            directionRight.setFill(javafx.scene.paint.Color.rgb(72, 227, 193));
        } else {
            // Right to left
            directionLeft.setFill(javafx.scene.paint.Color.rgb(72, 227, 193));
            directionRight.setFill(javafx.scene.paint.Color.BLACK);
        }
    }

    @FXML
    public void unoButtonClick() {
        // Can say UNO only if after playing the card the player has only 1 card left
        if (client.player.getCards().getSize() == 2) {
            client.saidUno = true;
        } else {
            System.out.println("[GameController]: Sorry, you can't press UNO as you have more than 2 cards.");
        }
    }

    @FXML
    public void disconnectButtonClick() {
        // Close the connection
        if (client != null) {
            client.sendDisconnectMessage();
            client.closeEverything();
        }

        // Close the game window
        this.closeWindow();
    }
}
