import java.io.*;
import java.util.*;

import javax.swing.ImageIcon;

public class SendPackage implements Serializable {
    private static final long serialVersionUID = 32392489384982943L;
    //记录的状态可能要更多一些？
    private boolean isReady;
    private Color color; //谁在下棋
    private int row;
    private int col;
    private int index;
    private int messageType; //0下棋，1聊天 
    private String message;
    private String alias;
    private Date date;
    private ImageIcon image;

    public SendPackage(Color clr, int r, int c) {
        color = clr;
        row = r;
        col = c;
    }

    public SendPackage() {
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public void setDate(Date date) {
        this.date = date;
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

    public boolean getReady() {
        return isReady;
    }

    public Color getColor() {
        return color;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public int getMessageType() {
        return messageType;
    }

    public String getMessage() {
        return message;
    }

    public Date getDate() {
        return date;
    }

    public String getAlias() {
        return alias;
    }

    public ImageIcon getImage() {
        return image;
    }

    public int getIndex() {
        return index;
    }
}
