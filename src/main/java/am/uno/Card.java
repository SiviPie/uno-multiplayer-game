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

    public String getName() {
        // Returns a string with the card name
        // Number cards: Color_number
        // Skip cards: Color_skip
        // Reverse cards: Color_reverse
        // Draw2: Color_Draw2
        // Wild cards: Wild
        // Wild_Draw cards: Wild_Draw

        if (this.type == CardType.Wild || this.type == CardType.WildDraw4) {
            return String.valueOf(this.type);
        }

        String name = this.color + "_";

        if (this.type == CardType.Number) {
            name = name + this.number;
        } else {
            name = name + this.type;
        }

        return name;
    }

    /*** OTHER METHODS ***/

    public void showInfo() {
        System.out.println( "Type: " + type + ((this.color != null) ? (" Color: " + color) : ("")) +   ((this.number >= 0) ? (" Number: " + number) : ("")));
    }

    public Boolean isStackable() {
        return (this.type == CardType.Skip) || (this.type == CardType.Draw2) || (this.type == CardType.WildDraw4);
    }
}