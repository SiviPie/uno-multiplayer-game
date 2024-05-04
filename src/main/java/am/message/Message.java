package am.message;

import am.uno.Card;
import am.uno.Color;
import am.uno.Player;
import am.uno.Opponent;

import java.io.Serializable;
import java.util.ArrayList;

public class Message implements Serializable {
    private MessageType type;

    private GameChoice gameChoice;
    private GameUpdate gameUpdate;

    private String sender;
    private String text;

    private Card card;
    private Color color;

    private Player player;
    private ArrayList<Opponent> opponents = new ArrayList<Opponent>();
    private Opponent opponent;

    private int idPlayerToUpdate;
    private int num;
    private boolean saidUno;
    private boolean direction = true;

    public Message(MessageType type) {
        this.type = type;
    }

    // GETTERS
    public MessageType getType() {
        return type;
    }

    public String getSender() {
        return sender;
    }

    public String getText() {
        return text;
    }

    public Card getCard() {
        return this.card;
    }

    public GameChoice getGameChoice() {
        return gameChoice;
    }

    public Player getPlayer() {
        return player;
    }

    public ArrayList<Opponent> getOpponents() {
        return this.opponents;
    }

    public Opponent getOpponent() {
        return this.opponent;
    }

    public int getIdPlayerToUpdate() {
        return  this.idPlayerToUpdate;
    }

    public int getNum() {
        return this.num;
    }

    public GameUpdate getGameUpdate() {
        return gameUpdate;
    }

    public boolean getSaidUno() {
        return saidUno;
    }

    public boolean getDirection() {
        return direction;
    }

    public Color getColor() {
        return color;
    }

    // SETTERS
    public void setType(MessageType type) {
        this.type = type;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setCard(Card card) {
        this.card = card;
    }

    public void setGameChoice(GameChoice gameChoice) {
        this.gameChoice = gameChoice;
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public void setOpponents (ArrayList<Opponent> opponents) {
        this.opponents = opponents;
    }

    public void setOpponent(Opponent opponent) {
        this.opponent = opponent;
    }

    public void setIdPlayerToUpdate(int idPlayerToUpdate) {
        this.idPlayerToUpdate = idPlayerToUpdate;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public void setGameUpdate(GameUpdate gameUpdate) {
        this.gameUpdate = gameUpdate;
    }

    public void setSaidUno(boolean saidUno) {
        this.saidUno = saidUno;
    }

    public void setDirection(boolean direction) {
        this.direction = direction;
    }

    public void setColor(Color color) {
        this.color = color;
    }
}
