import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import javax.swing.JOptionPane;

class MainHandler {
    // private static Controller instance = new Controller();
    private static Model model = Model.getInstance();
    // private static NetWork server;
    private int round; // 到谁下棋了
    private int cnt;
    private Viewer v1, v2;
    private ArrayList<ObjectOutputStream> outs;
    private ArrayList<ObjectInputStream> ins;
    private ArrayList<Viewer> viewers;

    private ExecutorService exec = Executors.newCachedThreadPool();

    private ObjectOutputStream getObjectOutputStream(int index) {
        return outs.get(index);
    }

    public PutChessState sendPackageHandler(SendPackage pkg) throws Exception {
        getObjectOutputStream(pkg.getIndex()).writeObject(pkg);
        return PutChessState.NORMAL;
    }

    public void init() throws IOException {
        /** 如果想要变成单开，只要把相应的Oos和ois保存起来就好了，我这里为了方便直接用ArrayList来存 */
        cnt = -1;
        outs = new ArrayList<ObjectOutputStream>();
        ins = new ArrayList<ObjectInputStream>();
        viewers = new ArrayList<Viewer>();

        System.out.println("Server started!");

        Viewer v1 = new Viewer(Color.WHITE, 1129);
        Viewer v2 = new Viewer(Color.BLACK, 1129);
        ins.add(new ObjectInputStream(v1.getSocket().getInputStream()));
        outs.add(new ObjectOutputStream(v1.getSocket().getOutputStream()));
        viewers.add(v1);
        v1.setIndex(++cnt);
        viewers.add(v2);
        v2.setIndex(++cnt);
        ins.add(new ObjectInputStream(v2.getSocket().getInputStream()));
        outs.add(new ObjectOutputStream(v2.getSocket().getOutputStream()));

        ListenerForViewer listenerForPlayer1 = new ListenerForViewer(v1.getSocket(), v1, "W");
        ListenerForViewer listenerForPlayer2 = new ListenerForViewer(v2.getSocket(), v2, "B");

        exec.submit(listenerForPlayer1);
        exec.submit(listenerForPlayer2);
    }

    private class ListenerForViewer extends Thread {
        //private Socket s;
        private Viewer v;
        private ObjectInputStream ois;
        private String name;
        private String alias;

        private String getStringedWinType(Color color) {
            return "玩家" + (color == Color.WHITE ? "WHITE" : "BLACK") + "赢了！让我们恭喜TA :)";
        }

        private String getStringedRoundType(Color color) {
            return "轮到" + (color == Color.WHITE ? "白方" : "黑方");
        }

        private ObjectInputStream getObjectInputStream() {
            return ins.get(viewers.indexOf(this.v));
        }

        public ListenerForViewer(Socket s, Viewer v, String name) throws IOException {
            //this.s = s;
            this.v = v;
            this.name = name;
        }

        public synchronized void run() {
            System.out.println("客户端监听线程启动");
            while (true) {
                try {
                    ois = getObjectInputStream();
                    ReceptionPackage rpkg = (ReceptionPackage) ois.readObject();
                    v.setReady(true);
                    if (rpkg.getMessageType() == 0) {
                        if (rpkg.getState() == PutChessState.WIN) {
                            SendPackage pkg = new SendPackage();
                            pkg.setMessageType(5);;
                            v.sendPackageHandler(pkg);

                            /**
                             * If this process startst first, the thread will, unfortunately block in the way and the whold
                             * frame will also freeze.
                             */
                            
                            JOptionPane.showMessageDialog(null, getStringedWinType(rpkg.getColor()));
                        }
                        v.setRound(getStringedRoundType(rpkg.getRound()));

                        if (v.getColor() != rpkg.getRound()) {
                            v.resetTimerAndStop();
                        } else {
                            v.resetTimerAndStart();
                        }

                        v.chessBoard.setChesses(rpkg.getChesses());
                        v.chessBoard.repaint();
                        v.appendNewMessage(rpkg.getMessage(), 0, rpkg.getColor());
                    } else if (rpkg.getMessageType() == 1) {
                        v.appendNewMessage(rpkg.getMessage(), 1, rpkg.getColor());
                    } else if (rpkg.getMessageType() == 2) {
                        this.alias = rpkg.getAlias();
                        v.appendNewMessage(rpkg.getMessage(), 2, rpkg.getColor());
                    } else if (rpkg.getMessageType() == 4) {
                        v.drawFlowerImage();
                        v.appendNewMessage(rpkg.getMessage(), 4, rpkg.getColor());
                        // v.appendNewImage();
                    } else if (rpkg.getMessageType() == 5) {
                        v.chessBoard.setChesses(rpkg.getChesses());
                        v.chessBoard.repaint();
                    } else if (rpkg.getMessageType() == 6 && rpkg.getColor() != v.getColor()) {
                        v.appendNewMessage(rpkg.getMessage(), 6, rpkg.getColor());
                        String[] options = {"确认", "拒绝"};
                        int response = JOptionPane.showOptionDialog(v, "对方想要悔棋，你同意吗？", "悔棋", JOptionPane.YES_OPTION,  
                                                                    JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
                        SendPackage sendPackage = new SendPackage();
                        sendPackage.setMessageType(response == 0 ? 7 : 8);
                        sendPackageHandler(sendPackage);
                    } else if (rpkg.getMessageType() == 7)  {
                        v.appendNewMessage(rpkg.getMessage(), rpkg.getMessageType(), Color.NONE);
                        v.setRound(getStringedRoundType(rpkg.getRound()));
                        
                        if (v.getColor() != rpkg.getRound()) {
                            v.resetTimerAndStop();
                        } else {
                            v.resetTimerAndStart();
                        }

                        v.chessBoard.setChesses(rpkg.getChesses());
                        v.chessBoard.repaint();
                    } else if (rpkg.getMessageType() == 8) {
                        v.appendNewMessage(rpkg.getMessage(), rpkg.getMessageType(), Color.NONE);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}

public class Controller {
    private static MainHandler mh;

    public static PutChessState sendPackageHandler(SendPackage pkg, Viewer viewer) throws Exception {
        return mh.sendPackageHandler(pkg);
    }

    public static void main(String[] args) throws IOException {
        // server = new NetWork();
        mh = new MainHandler();
        mh.init();
    }
}
