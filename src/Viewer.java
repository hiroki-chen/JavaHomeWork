
/**
 * @author: Mark Chen
 * @version: 1.0
 * 
 * This file is used to create the GUI body of the gobang game.
 */

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.Timer;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import javazoom.jl.player.*;

/**
 * Main body of the {@link Viewer} class.
 */

public class Viewer extends JFrame {
    private static final String BACKGROUND_PATH = "resources/img/background.jpg";
    private static final String FLOWER_PATH = "resources/img/flower.gif";
    private static final String DEFAULT_SONG_PATH = "resources/music/t+pazolite - Oshama Scramble!.mp3";
    private static final String DEFAULT_SONG_CHOOSER_PATH = "resources/music";
    private static final String DEFAULT_IP_ADDRESS = "127.0.0.1";
    private static final String CLAPPING_SOUND_PATH = "resources/sound/clap.mp3";

    private static final String CONNECT_TO_SERVER_SUSSCESSD = "Connected to Server!!!!";
    private static final String CONNECT_TO_SERVER_FAILED = "Failed to connect to the server.";

    /** Indicates whether the opponent is ready. */
    private boolean isReady;
    private int port;
    private int index;

    private Socket s;
    private Color color;
    private CounterThread timerThread;

    private ImageIcon flower;

    private LoadingPanel loadingPanel;

    private JButton flowerButton;
    private JButton undoButton;
    private JComboBox songList;
    private JPanel flowerPanel;
    private JLabel who;
    private JLabel round;
    private JTextField showAlias;
    private JTextField timeText;
    private JTextPane chatter;
    private JTextArea inputArea;
    private PlayBackgroundMusicThread musicThread;

    private String alias;

    public ChessBoard chessBoard;

    private ExecutorService exec = Executors.newCachedThreadPool();

    public ArrayList<SendPackage> chessItems = new ArrayList<SendPackage>();

    private class CounterThread extends Thread implements ActionListener {
        private static final long MAX_ALLOWED_TIME = 30000;
        private long remainingTime;

        private Timer timer;
        private JTextField timeText = null;
        private SimpleDateFormat df;

        @Override
        public void actionPerformed(ActionEvent e) {
            df = new SimpleDateFormat("ss");
            remainingTime -= 1000;
            timeText.setText(df.format(remainingTime)); // 1970.1.1 0:00:00

            if (remainingTime == 0) {
                SendPackage sendPackage = new SendPackage();
                sendPackage.setMessageType(3);
                sendPackage.setColor(color);
                try {
                    sendPackageHandler(sendPackage);
                } catch (Exception err) {
                    err.printStackTrace();
                }
            }
        }

        public CounterThread(JTextField timeText) {
            this.timeText = timeText;
            this.remainingTime = MAX_ALLOWED_TIME;
            timer = new Timer(1000, this);
        }

        public void run() {
            timer.start();
        }

        /**
         * This method is reserved for {@link Controller}, which calls it for stopping
         * the timer and reset the {@link remainingTime}.
         */
        public void resetTimer() {
            timer.stop();
            remainingTime = MAX_ALLOWED_TIME;
            timeText.setText(df.format(remainingTime));
        }

        public void startTimer() {
            timer.start();
        }
    }

    /**
     * @deprecated: The thread will continuously check if the opponent is ready,
     *              otherwise it will block until the opponent is online. Or the
     *              chess game is single-usered.
     */
    private void tryStartTimer() {
        exec.submit(new Thread() {
            @Override
            public void run() {
                System.out.println(isReady);
                try {
                    while (!isReady) {
                        SendPackage sendPackage = new SendPackage();
                        sendPackage.setReady(true);
                        sendPackage.setColor(color);
                        sendPackage.setMessageType(3);
                        sendPackageHandler(sendPackage);
                    }
                    // If ready, thene simply create a new thread to handle this.
                    new CounterThread(timeText);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * The method will play the music in the background in loop mode, the default
     * path is {@code resources/music/bgm2.mp3}.
     * 
     * @author: Mark Chen
     * @param path the path of the music file.
     */
    private void playMusic(String path) {
        musicThread = new PlayBackgroundMusicThread(path);
        musicThread.startMusic();
    }

    /**
     * This method is the initializer for the main body {@link Viewer}, to draw all
     * the components within it, such as {@link Viewer#inputArea}, which is for the
     * user to type the words, as well as {@link ChessBoard}, which is for drawing a
     * chessboard on the middle of the screen.
     * 
     * @Notice: This method contains nothing, it simply calls other private methods
     *          in the {@link Viewer}. Any rewritting this method should first
     *          implement all the needed methods.
     * 
     * @author: Mark Chen
     * @throws Exception
     * @version: 2.0
     */
    private void init() throws Exception {
        // 连接一下。
        this.connect();

        // 画出main Frame.
        this.drawMainFrame();

        // 设置背景图片
        this.drawBackgroundPicture(BACKGROUND_PATH);

        // 画出五子棋盘
        this.drawChessBoard();

        // 提示标签
        this.drawHintLabels();

        // 聊天信息
        this.drawChatterMainFrame();

        // 播放音乐
        this.playMusic(DEFAULT_SONG_PATH);

        // 画出时钟
        this.drawClock();

        // 画出花朵按钮
        this.drawFlowerButton();

        // 画出悔棋按钮
        this.drawUndoButton();

        // 画出花朵所在的层
        this.drawFlowerPanel();

        // 画出选歌器
        this.drawSongList();

        // 画出（不可见）的加载画面，很卡
        // this.drawLoadingPanel();

        // 开始计时
        this.startTimer();

    }

    /**
     * This method is used for the {@link Viewer} to connect itself to the
     * {@link NetWork}.
     * 
     * @throws IOException
     */
    private void connect() throws IOException {
        // Default approach.
        s = new Socket(DEFAULT_IP_ADDRESS, port);
    }

    /**
     * @Note: This method is an overloaded method of {@link Viewer#connect()}, but
     *        reserved.
     * @param address
     * @throws IOException
     */
    private void connect(String address) throws IOException {
        s = new Socket(address, port);
    }

    /**
     * This method draws the main Frame.
     */
    private void drawMainFrame() {
        this.setTitle("五子棋游戏");
        this.setVisible(true);
        this.setSize(new Dimension(800, 600));
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    /**
     * This method draws the background picture of the game body, using the
     * {@link String} path.
     * 
     * @param path
     */
    private void drawBackgroundPicture(String path) {
        ImageIcon bg = new ImageIcon(path);
        JLabel label = new JLabel(bg);
        label.setBounds(0, 0, bg.getIconWidth(), bg.getIconHeight());
        JPanel imagePanel = (JPanel) this.getContentPane();
        imagePanel.setOpaque(true);
        this.setLayout(new FlowLayout());
        this.getLayeredPane().add(label, -1);
        this.setSize(bg.getIconWidth(), bg.getIconHeight());
        JOptionPane.showMessageDialog(this, this.getStringedColor(this.color));
    }

    /**
     * This method draws a {@link ChessBoard}.
     */
    private void drawChessBoard() {
        chessBoard = new ChessBoard();
        chessBoard.setBounds(this.getWidth() / 2 - 250, this.getHeight() / 2 - 250, 494, 494); // 把棋盘放在最中间的位置
        chessBoard.setBorder(new TextBubbleBorder(java.awt.Color.BLUE));
        chessBoard.addMouseListener(new PutChessListener(color, this));
        chessBoard.addMouseMotionListener(new WantToPutChessListener());

        // A layered pane is neccessary.
        this.getLayeredPane().add(chessBoard, 0);
    }

    /**
     * This methods puts a button on the right of the {@link Viewer#inputArea}. If
     * the user wants to send an egg picture, for exmaple, or whatever the user
     * likes, the method can also be reused.
     * 
     * @see {@link JButton}
     */
    private void drawFlowerButton() {
        flowerButton = new JButton();
        flowerButton.setHorizontalAlignment(JButton.CENTER);
        flowerButton.setText("发送鲜花");
        flowerButton.setBounds(this.getWidth() / 2 + 550, this.getHeight() / 2 + 150, 100, 20);
        flowerButton.addActionListener(new SendFlowerListener(this));
        this.getLayeredPane().add(flowerButton, 2);
    }

    private void drawUndoButton() {
        undoButton = new JButton();
        undoButton.setHorizontalAlignment(JButton.CENTER);
        undoButton.setText("悔棋");
        undoButton.setBounds(this.getWidth() / 2 + 450, this.getHeight() / 2 + 150, 100, 20);
        undoButton.addActionListener(new UndoListener(this));
        this.getLayeredPane().add(undoButton, 2);
    }

    /**
     * @Notice: The user wants to know who's round, and the role played by him /
     *          her, so some clues are necessary.
     * @see: {@link Viewer#setAlias(String)}
     * @see: {@link Viewer#setName(String)}
     */
    private void drawHintLabels() {
        who = new JLabel("您是" + this.getStringedColor(this.color), JLabel.CENTER);
        who.setBorder(BorderFactory.createLineBorder(java.awt.Color.PINK));
        who.setBounds(20, 20, 100, 20);
        who.setBackground(java.awt.Color.WHITE);
        who.setOpaque(true);
        this.getLayeredPane().add(who, 1);

        round = new JLabel("轮到" + "黑方", JLabel.CENTER);
        round.setBorder(BorderFactory.createLineBorder(java.awt.Color.PINK));
        round.setBounds(20, 50, 100, 20);
        round.setBackground(java.awt.Color.WHITE);
        round.setOpaque(true);
        this.getLayeredPane().add(round, 1);

        alias = this.getStringedColor(this.color);
        showAlias = new JTextField("你好，" + alias);
        showAlias.setHorizontalAlignment(JTextField.CENTER);
        showAlias.setBorder(BorderFactory.createLineBorder(java.awt.Color.PINK));
        showAlias.setBounds(20, 80, 100, 20);
        showAlias.setBackground(java.awt.Color.WHITE);
        showAlias.setOpaque(true);
        showAlias.addKeyListener(new ChangeAliasListener(this));
        this.getLayeredPane().add(showAlias, 1);
    }

    /**
     * This method draws a chatter body, which is a {@link JTextPane}, because it is
     * able to set variable {@link SimpleAttributeSet}.
     * 
     * @throws BadLocationException
     * @throws IOException
     * @Notice: {@link Viewer#chatter} is integrated to a {@link JScrollPane}.
     */
    private void drawChatterMainFrame() throws BadLocationException, IOException {
        chatter = new JTextPane();
        chatter.setBorder(BorderFactory.createTitledBorder(new CustomBorder(java.awt.Color.BLUE, 15), "聊天信息"));
        chatter.setEditable(false);
        StyledDocument doc = chatter.getStyledDocument();
        // doc.insertString(0, "聊天室启动\n", new SimpleAttributeSet());
        JScrollPane jsb1 = new JScrollPane(chatter);
        jsb1.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        jsb1.setBounds(this.getWidth() / 2 + 250, this.getHeight() / 2 - 250, 400, 400);
        this.getLayeredPane().add(jsb1, 1);

        inputArea = new JTextArea();
        inputArea.setBorder(BorderFactory.createBevelBorder(1));
        JScrollPane jsb2 = new JScrollPane(inputArea);
        jsb2.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        jsb2.setBounds(this.getWidth() / 2 + 250, this.getHeight() / 2 + 170, 400, 74);
        inputArea.addKeyListener(new sendMessageListener(this));
        this.getLayeredPane().add(jsb2, 1);
    }

    /**
     * This methods draws a {@link JLabel} as a clock hint.
     */
    private void drawClock() {
        JLabel hint = new JLabel("剩余时间", JLabel.CENTER);
        hint.setBorder(BorderFactory.createLineBorder(java.awt.Color.PINK));
        hint.setOpaque(true);
        hint.setBackground(java.awt.Color.WHITE);
        hint.setBounds(20, 110, 100, 20);
        timeText = new JTextField();
        timeText.setText("sad");
        timeText.setHorizontalAlignment(JTextField.CENTER);
        timeText.setBorder(BorderFactory.createLineBorder(java.awt.Color.PINK));
        timeText.setEditable(true);
        timeText.setBackground(java.awt.Color.WHITE);
        timeText.setOpaque(true);
        timeText.setBounds(20, 130, 100, 20);
        this.getLayeredPane().add(timeText, 1);
        this.getLayeredPane().add(hint, 1);
    }

    /**
     * This method draws a {@link JPanel} containing a flower.gif, which is called
     * by the constructor.
     */
    private void drawFlowerPanel() {
        flowerPanel = new JPanel();
        flowerPanel.setOpaque(true);
        flowerPanel.add(new JLabel(flower));
        flowerPanel.setBounds(this.getWidth() / 2 - flower.getIconWidth(),
                this.getHeight() / 2 - flower.getIconHeight(), flower.getIconWidth(), flower.getIconHeight());
        flowerPanel.setVisible(false);
        flowerPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                ((JPanel) e.getSource()).setVisible(false);
            }
        });
        this.getLayeredPane().add(flowerPanel, Integer.MAX_VALUE);
    }

    private void drawLoadingPanel() {
        loadingPanel = new LoadingPanel();
        loadingPanel.setBackground(java.awt.Color.WHITE);
        loadingPanel.setBounds(this.getWidth() / 2 - 250, this.getHeight() / 2 - 250, 494, 494);
        loadingPanel.show();
        loadingPanel.setVisible(false);
        this.getLayeredPane().add(loadingPanel, 1);
    }

    /**
     * This method draws the {@link JComboBox} for the user to choose a song in the
     * resource file.
     */
    private void drawSongList() {
        File file = new File(DEFAULT_SONG_CHOOSER_PATH);
        File[] files = file.listFiles();
        ArrayList<MusicItem> musicItems = new ArrayList<MusicItem>();

        for (int i = 0; i < files.length; i++) {
            if (files[i].getName().endsWith(".mp3")) {
                musicItems.add(new MusicItem(files[i]));
            }
        }
        System.out.println(musicItems.size());

        songList = new JComboBox<MusicItem>(musicItems.toArray(new MusicItem[0]));
        songList.setBounds(20, 160, 100, 20);
        songList.setOpaque(false);
        songList.setBackground(java.awt.Color.WHITE);
        songList.addItemListener(new SetBackgroundMusicListener(this));
        this.getLayeredPane().add(songList, 1);
    }

    /**
     * @deprecated Not yet supported, because {@link Controller} has something
     *             wrong?
     * @throws IOException
     * @throws Exception
     */
    private void sendReadyInformation() throws IOException, Exception {
        SendPackage sendPackage = new SendPackage();
        sendPackage.setReady(true);
        sendPackageHandler(sendPackage);
    }

    /**
     * This method starts the timer.
     */
    private void startTimer() {
        timerThread = new CounterThread(this.timeText);
        exec.submit(timerThread);
    }

    /**
     * Constructor of the {@link Viewer}.
     * 
     * @param color The color of the {@link ChessBoard}; in other words, the color
     *              of the user.
     * @param port  The port for connection.
     */
    public Viewer(Color color, int port) {
        this.isReady = false;
        this.color = color;
        this.port = port;
        
        this.flower = new ImageIcon(FLOWER_PATH);
        // 先把socket互相连接上，把基本的框架画出来
        try {
            this.init();
            System.out.println(CONNECT_TO_SERVER_SUSSCESSD);
        } catch (Exception e) {
            System.out.println(CONNECT_TO_SERVER_FAILED);
        }
    }

    /**
     * This method is public, mainly because the {@link Controller} should receive
     * the {@link ReceptionPackage} and try to draw a flower image on the screen.
     * The method simply alters the visibility of the {@link ImageIcon} flower.
     */
    public void drawFlowerImage() {
        if (flowerPanel.isVisible() == false) {
            flowerPanel.setVisible(true);
        }

        /* Plays the sound effect of clapping! */
        exec.submit(new Thread() {
            public void run() {
                try {
                    Player player = new Player(new FileInputStream(CLAPPING_SOUND_PATH));
                    player.play();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        /* End. */
    }

    public void setReady(boolean isReady) {
        this.isReady = isReady;
    }

    /**
     * Because we need to get the {@link String} of the color to edit the
     * {@link JTextPane}, i.e. {@link Viewer#chatter}, we have to convert the
     * customized {@link Color} (not a {@link java.awt.Color}) to a {@link String}.
     * 
     * @param: Color color
     * @return: String
     */
    private String getStringedColor(Color color) {
        return color == Color.BLACK ? "黑方" : "白方";
    }

    public Socket getSocket() {
        return s;
    }

    /**
     * This method sets the hint label, called by the {@link Controller}, which
     * receives a {@link ReceptionPackage}.
     * 
     * @param text The content.
     */
    public void setRound(String text) {
        this.round.setText(text);
    }

    public Color getColor() {
        return color;
    }

    public String getAlias() {
        return alias;
    }

    public PlayBackgroundMusicThread getMusicThread() {
        return musicThread;
    }

    public int getIndex() {
        return index;
    }

    /**
     * This method is a handler to send a {@link SendPackage} to the server side,
     * and because we cannot directly communicate with the server side, so we
     * dispatch the package to the {@link Controller}.
     * 
     * @param pkg The {@link SendPackage} we want to send.
     * @return {@link PutChessState}
     * @throws Exception
     */
    public PutChessState sendPackageHandler(SendPackage sendPackage) throws Exception {
        sendPackage.setIndex(this.index);
        return Controller.sendPackageHandler(sendPackage, this);
    }

    /**
     * This method is reserved for inserting an image into the
     * {@link Viewer#chatter}.
     * 
     * @param image The image user wants to insert.
     */
    public void appendNewImage(ImageIcon image) {
        this.chatter.insertIcon(image);
    }

    /**
     * Invoked by the {@code Controller}.
     * 
     * This method appends a new message at the back of the previous text of the
     * {@link Viewer#chatter}, which is realized by get the {@link StyledDocument}
     * of the {@link Viewer#chatter}, and set new {@link SimpleAttributeSet}, or
     * {@link JTextPane#setParagraphAttributes(javax.swing.text.AttributeSet, boolean)}.
     * Furthermore, the method firstly judges whether the message is a chatting
     * message or a information message to notify users if a chess is put, and then
     * different {@link javax.swing.text.AttributeSet.ParagraphAttribute}s are set.
     * 
     * @Notice: This method may contain insidious bugs. Please use with care, or the
     *          main thread would get stuck, especially with "\n".<br>
     *          <br>
     *          Also, Please do not type your words too quickly, otherwise the
     *          thread will face a deadlock, because java.swing is <b>not
     *          thread-safe</b>.
     * @author: Mark Chen
     * @version: 3.4
     * 
     * @param message
     * @param messageType
     * @param color
     */
    public void appendNewMessage_old(String message, int messageType, Color color) {
        try {
            SimpleAttributeSet keyWord = new SimpleAttributeSet();
            SimpleAttributeSet align = new SimpleAttributeSet();
            StyleConstants.setAlignment(align, StyleConstants.ALIGN_LEFT);

            if (messageType == 1) {
                StyleConstants.setAlignment(align,
                        color == this.color ? StyleConstants.ALIGN_RIGHT : StyleConstants.ALIGN_LEFT);
                StyleConstants.setForeground(keyWord, color == this.color ? java.awt.Color.BLUE : java.awt.Color.BLACK);
            } else if (messageType == 2) {
                StyleConstants.setAlignment(align, StyleConstants.ALIGN_CENTER);
                StyleConstants.setItalic(keyWord, true);
                StyleConstants.setForeground(keyWord, java.awt.Color.GRAY);
                if (color == this.color) {
                    message = "您" + message;
                } else {
                    message = this.getStringedColor(color) + message;
                }
            } else if (messageType == 4) {
                StyleConstants.setAlignment(align, StyleConstants.ALIGN_CENTER);
            } else {
                StyleConstants.setItalic(keyWord, true);
                StyleConstants.setForeground(keyWord, java.awt.Color.GRAY);
                StyleConstants.setAlignment(align, StyleConstants.ALIGN_CENTER);
            }

            StyledDocument doc = this.chatter.getStyledDocument();
            int pos = doc.getLength();
            message += "\n";

            doc.insertString(doc.getLength(), message, keyWord);
            doc.setParagraphAttributes(pos, message.length() + 1, align, false);

        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    public void appendNewMessage(String message, int messageType, Color color) {
        try {
            SimpleAttributeSet keyWord = new SimpleAttributeSet();

            if (messageType == 1) {
                StyleConstants.setForeground(keyWord, color == this.color ? java.awt.Color.BLUE : java.awt.Color.BLACK);
            } else if (messageType == 2) {
                StyleConstants.setItalic(keyWord, true);
                StyleConstants.setForeground(keyWord, java.awt.Color.GRAY);
                if (color == this.color) {
                    message = "您" + message;
                } else {
                    message = this.getStringedColor(color) + message;
                }
            } else {
                StyleConstants.setItalic(keyWord, true);
                StyleConstants.setForeground(keyWord, java.awt.Color.GRAY);
            }

            StyledDocument doc = this.chatter.getStyledDocument();
            message += "\n";

            doc.insertString(doc.getLength(), message, keyWord);
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    /**
     * Reserved for the {@link Controller}.
     */
    public void resetTimerAndStop() {
        this.timerThread.resetTimer();
    }

    /**
     * Reserved for the {@link Controller}.
     */
    public void resetTimerAndStart() {
        this.timerThread.resetTimer();
        this.timerThread.startTimer();
    }
}

/**
 * An extended JPanel, used to draw a chessBaord on the center of the frame.
 */
class ChessBoard extends JPanel {
    /** The size of the chessboard. */
    public static final int WIDTH = 19;
    public static final int OFFSET = 25;
    private int pendingRow, pendingCol;

    private Color chesses[][];
    public Image boardImg = Toolkit.getDefaultToolkit().getImage("resources/img/chess.jpg");


    public ChessBoard() {
        // this.setBackground(java.awt.Color.WHITE);
        chesses = new Color[WIDTH][WIDTH];
        for (int i = 0; i < WIDTH; i++) {
            for (int j = 0; j < WIDTH; j++) {
                chesses[i][j] = Color.NONE;
            }
        }
        pendingRow = pendingCol = -1;
    }

    /**
     * @param Color[][] chesses This method uses a chessBoard to reset the private
     *                  chessBoard.
     */
    public void setChesses(Color[][] chesses) {
        this.chesses = chesses;
    }

    /**
     * This method clears the chessboard of the parent class.
     */
    public void clear() {
        for (int i = 0; i < WIDTH; i++) {
            for (int j = 0; j < WIDTH; j++) {
                chesses[i][j] = Color.NONE;
            }
        }
    }

    /**
     * This method set the magenet position.
     * @param row
     * @param col
     */
    public void setPendingPosition(int row, int col) {
        this.pendingRow = row;
        this.pendingCol = col;
        this.repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int imgWidth = boardImg.getWidth(this);// 图片的宽度
        int imgHeight = boardImg.getHeight(this);// 图片的高度

        int FWidth = getWidth();// 容器的宽度
        int FHeight = getHeight();// 容器的高度

        int x = (FWidth - imgWidth) / 2;
        int y = (FHeight - imgHeight) / 2;
        g.drawImage(boardImg, x, y, null);// 画出图片

        int span_x = imgWidth / (WIDTH + 1);// 单元格的宽度
        int span_y = imgHeight / (WIDTH + 1);// 单元格的高度
        // 画横线
        for (int i = 0; i <= WIDTH; i++) {
            g.drawLine(x + span_x, y + (i + 1) * span_y, FWidth - x - span_x, y + (i + 1) * span_y);
        }
        // 画竖线
        for (int i = 0; i <= WIDTH; i++) {
            g.drawLine(x + (i + 1) * span_x, y + span_y, x + (i + 1) * span_x, FHeight - y - span_y);
        }

        for (int i = 0; i < WIDTH; i++) {
            for (int j = 0; j < WIDTH; j++) {
                if (chesses[i][j] == Color.BLACK) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setColor(java.awt.Color.BLACK);
                    g2.fillOval(i * OFFSET + OFFSET / 2, j * OFFSET + OFFSET / 2, WIDTH, WIDTH);
                } else if (chesses[i][j] == Color.WHITE) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setColor(java.awt.Color.BLACK);
                    g2.drawOval(i * OFFSET + OFFSET / 2, j * OFFSET + OFFSET / 2, WIDTH, WIDTH);
                    g2.setColor(java.awt.Color.WHITE);
                    g2.fillOval(i * OFFSET + OFFSET / 2, j * OFFSET + OFFSET / 2, WIDTH, WIDTH);
                }
            }
        }

        Graphics2D g2 = (Graphics2D) g;
        g2.setColor(new java.awt.Color(127, 127, 127, 127));
        g2.fillOval(pendingRow * OFFSET + OFFSET / 2, pendingCol * OFFSET + OFFSET / 2, WIDTH, WIDTH);
    }
}

/**
 * This class is a definition of MusicItem, which is the item of the {@link JComboBox} songlist.
 */

class MusicItem implements Serializable {
    private static final long serialVersionUID = 4929249248942924L;
    private String name;
    private String songPath;

    public MusicItem(File file) {
        this.name = file.getName();
        this.songPath = file.getPath();
    }

    @Override
    public String toString() {
        return name;
    }

    public String getSongPath() {
        return songPath;
    }
}

/**
 * This class is extended from {@link Thread}, in order to play the background
 * music. As we want to change the music and stop the music, So the class should
 * be implemented not as an anonymous class, but a defined class.
 * 
 * @author: Mark Chen
 * @version: 1.0
 */
class PlayBackgroundMusicThread extends Thread {
    private String path;
    private Player player;
    private boolean isPlay;

    public void stopMusic() {
        isPlay = false;
        player.close();
    }

    public void startMusic() {
        this.run();
    }

    @Override
    public void run() {
        new Thread() {
            @Override
            public void run() {
                try {
                    while (isPlay) {
                        FileInputStream in = new FileInputStream(path);
                        player = new Player(in);
                        player.play();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
        Thread.yield();
    }

    public void changeMusic(String newPath) {
        this.path = newPath;
        this.stopMusic();
        this.isPlay = true;
        this.startMusic();
    }

    public PlayBackgroundMusicThread(String path) {
        this.path = path;
        this.isPlay = true;
    }
}

/**
 * This class will listen to the {@code MouseEvent} attachted to the class
 * {@link ChessBoard}, if any valid click is captured, it will send a package
 * containing the {@code Color}, {@code Row}, {@code Col} to the server.
 */
class PutChessListener extends MouseAdapter {
    private Color color;
    private Viewer viewer;

    @Override
    public void mouseClicked(MouseEvent e) {
        // 获取鼠标按下时的坐标。
        int posX = e.getX();
        int posY = e.getY();
        System.out.println(posX + ", " + posY);
        // 计算出具体的行列值
        int row = posX / 25, col = posY / 25;

        SendPackage sendPackage = new SendPackage(color, row, col);
        sendPackage.setAlias(viewer.getAlias());
        sendPackage.setMessageType(0);
        try {
            if (PutChessState.NORMAL == viewer.sendPackageHandler(sendPackage)) {
                System.out.println("发送成功！");
                // viewer.chessItems.add(sendPackage);
                System.out.println(row + ", " + col);
                // chessBoard.repaint();
                return;
            } else {
                JOptionPane.showMessageDialog(viewer, "有问题", "", JOptionPane.WARNING_MESSAGE);
            }
        } catch (Exception exp) {
            exp.printStackTrace();
        }
    }

    public PutChessListener(Color color, Viewer viewer) {
        this.color = color;
        this.viewer = viewer;
    }
}

class WantToPutChessListener extends MouseMotionAdapter {
    @Override
    public void mouseMoved(MouseEvent e) {
        int posX = e.getX();
        int posY = e.getY();
        int row = posX / 25, col = posY / 25;
        ChessBoard instance = (ChessBoard)e.getSource();
        instance.setPendingPosition(row, col);
    }
}

/**
 * This class is an KeyAdapter used to listen to the trigger event caused by the
 * InputArea. If any new messages is sent by the combination keycode CTRL +
 * ENTER, the listenner will invoke
 * {@link Viewer#sendPackageHandler(SendPackage)} method to send a package to
 * the server side.
 */
class sendMessageListener extends KeyAdapter {
    private Viewer viewer;

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getModifiersEx() == KeyEvent.CTRL_DOWN_MASK && e.getKeyCode() == KeyEvent.VK_ENTER) {
            try {
                JTextArea inputArea = (JTextArea) e.getSource();
                String message = inputArea.getText();
                inputArea.setText("");
                SendPackage sendPackage = new SendPackage(viewer.getColor(), -1, -1); // 行列没有意义
                sendPackage.setMessage(message);
                sendPackage.setMessageType(1);
                sendPackage.setDate(new Date());
                sendPackage.setAlias(viewer.getAlias());
                viewer.sendPackageHandler(sendPackage);
            } catch (Exception err) {
                err.printStackTrace();
            }
        }
    }

    public sendMessageListener(Viewer viewer) {
        this.viewer = viewer;
    }
}

class ChangeAliasListener extends KeyAdapter {
    private Viewer viewer;

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            try {
                SendPackage sendPackage = new SendPackage();
                JTextField showAlias = (JTextField) e.getSource();

                String input = showAlias.getText();
                System.out.println(input);
                if (input != null) {
                    viewer.setAlias(input);
                    sendPackage.setAlias(showAlias.getText());
                    sendPackage.setMessageType(2);
                    sendPackage.setColor(viewer.getColor());
                    viewer.sendPackageHandler(sendPackage);
                }
            } catch (Exception err) {
                err.printStackTrace();
            }
        }
    }

    public ChangeAliasListener(Viewer viewer) {
        this.viewer = viewer;
    }
}

class SendFlowerListener implements ActionListener {
    private Viewer viewer;

    @Override
    public void actionPerformed(ActionEvent e) {
        SendPackage sendPackage = new SendPackage();
        sendPackage.setMessageType(4);
        sendPackage.setAlias(viewer.getAlias());

        try {
            viewer.sendPackageHandler(sendPackage);
        } catch (Exception err) {
            err.printStackTrace();
        }
    }

    public SendFlowerListener(Viewer viewer) {
        this.viewer = viewer;
    }
}

class UndoListener implements ActionListener {
    private Viewer viewer;

    @Override
    public void actionPerformed(ActionEvent e) {
        SendPackage sendPackage = new SendPackage();
        sendPackage.setMessageType(6); // want to undo.
        sendPackage.setColor(viewer.getColor());
        sendPackage.setIndex(viewer.getIndex());

        try {
            viewer.sendPackageHandler(sendPackage);
        } catch (Exception err) {
            err.printStackTrace();
        }
    }

    public UndoListener(Viewer viewer) {
        this.viewer = viewer;
    }
}

class SetBackgroundMusicListener implements ItemListener {
    private Viewer viewer;

    @Override
    public void itemStateChanged(ItemEvent e) {
        JComboBox<MusicItem> list = (JComboBox<MusicItem>) e.getSource();
        MusicItem item = (MusicItem) list.getSelectedItem();
        this.viewer.getMusicThread().changeMusic(item.getSongPath());
    }

    public SetBackgroundMusicListener(Viewer viewer) {
        this.viewer = viewer;
    }
}