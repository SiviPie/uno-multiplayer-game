package am.client;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
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
    @FXML
    private Label gameUsernameLabel;

    // TEXTFIELDS
    @FXML
    private TextField textMessageField;

    // BUTTONS
    @FXML
    private Button sendMessageButton;

    // SCROLLPANE
    @FXML
    protected ScrollPane chatScrollPane;

    // HBOX
    @FXML
    protected HBox playerListHbox;

    // VBOX
    @FXML
    protected VBox chatVbox;

    private Client client;

    // Method to update the text of the label
    public void updateGameStatus(String status) {
        gameUsernameLabel.setText(status);
    }

    // Method to close the game window
    public void closeWindow() {
        if (windowCloseListener != null) {
            windowCloseListener.onWindowClose();
        }
        Stage stage = (Stage) gameUsernameLabel.getScene().getWindow();
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
        }

    }

    @FXML
    public void disconnectButtonClick() throws IOException {
        client.sendDisconnectMessage();
        client.closeEverything();
        this.closeWindow();
    }
}