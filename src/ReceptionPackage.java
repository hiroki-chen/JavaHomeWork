import java.io.*;

import javax.swing.ImageIcon;

public class ReceptionPackage implements Serializable {
    private static final long serialVersionUID = 2392489384982943L;
    private Color color;
    private boolean isReady;
    private PutChessState state;
    private int row, col;
    private Color round;
    private Color chesses[][];
    private int messageType;
    private String message;
    private String alias;
    private String time;
    private ImageIcon image;

    public ReceptionPackage() {
    }

    public ReceptionPackage(Color color, Color round, PutChessState state, int row, int col, Color[][] chesses) {
        this.color = color;
        this.round = round;
        this.state = state;
        this.row = row;
        this.col = col;
        this.chesses = new Color[19][19];
        for (int i = 0; i < 19; i++) {
            for (int j = 0; j < 19; j++) {
                this.chesses[i][j] = chesses[i][j];
            }
        }
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public void setImage(ImageIcon image) {
        this.image = image;
    }

    public void setReady(boolean isReady) {
        this.isReady = isReady;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setChesses(Color[][] chesses) {
        this.chesses = chesses;
    }

    public void setRound(Color round) {
        this.round = round;
    }

    public int getCol() {
        return col;
    }

    public Color getColor() {
        return color;
    }

    public PutChessState getState() {
        return state;
    }

    public int getRow() {
        return row;
    }

    public Color[][] getChesses() {
        return chesses;
    }

    public Color getRound() {
        return round;
    }

    public String getMessage() {
        return message;
    }

    public int getMessageType() {
        return messageType;
    }

    public boolean getReady() {
        return isReady;
    }

    public String getAlias() {
        return alias;
    }

    public ImageIcon getImage() {
        return image;
    }

    public String getTime() {
        return time;
    }
}
