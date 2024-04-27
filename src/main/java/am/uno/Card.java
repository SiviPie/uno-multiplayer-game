package am.uno;

import java.io.Serial;
import java.io.Serializable;

public class Card implements Serializable {
    @Serial
    private static final long serialVersionUID = 3L;

    CardType type;
    Color color;
    int number;  // from 0 to 9 for Number cards, -1 for the rest

    /*** CONSTRUCTORS ***/

    public Card(CardType type, Color color, int number) {
        // for Number cards
        this.type = type;
        this.color = color;
        this.number = number;
    }

    public Card(CardType type, Color color) {
        // for Reverse, Skip, Draw2 cards
        this.type = type;
        this.color = color;
        this.number = -1;
    }

    public Card(CardType type) {
        // for Wild and WildDraw4 cards
        this.type = type;
        this.number = -1;
    }

    /*** SETTERS ***/

    public void setType(CardType type) {
        this.type = type;
    }

    public void setColor(Color color) {
        // only for wild cards
        this.color = color;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    /*** GETTERS ***/

    public CardType getType() {
        return this.type;
    }

    public Color getColor() {
        return this.color;
    }

    public int getNumber() {
        return this.number;
    }

    /*** OTHER METHODS ***/

    public void showInfo() {
        System.out.println( "Type: " + type + ((this.color != null) ? (" Color: " + color) : ("")) +   ((this.number >= 0) ? (" Number: " + number) : ("")));
    }
}