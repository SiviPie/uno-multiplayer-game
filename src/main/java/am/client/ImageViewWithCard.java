package am.client;

import am.uno.Card;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

class ImageViewWithCard extends ImageView {
    private final Card card;

    public ImageViewWithCard(Card card, Image image) {
        super(image);
        this.card = card;
    }

    // GETTERS
    public Card getCard() {
        return card;
    }
}