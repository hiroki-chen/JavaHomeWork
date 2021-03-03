import java.io.*;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import java.text.SimpleDateFormat;
import java.net.*;

/**
 * This class is the main class of the server. <br>
 * 1. It hanldes connection from clients and starts a new thread to handle it;
 * <br>
 * 2. It try to start the timer; <br>
 * 3. It creates I/O between host and clients. <br>
 * 
 * @author: Mark Chen
 * @version: 1.1
 * @see: {@link Runnable}
 */
public class NetWork implements Runnable {
    private int cnt;

    private ServerSocket ss;

    private ArrayList<ObjectOutputStream> outs;
    private ArrayList<ObjectInputStream> ins;
    private ArrayList<Socket> clients;
    private ExecutorService exec = Executors.newCachedThreadPool();

    private Color whosRound;
    private PutChessState state;
    private Model model = Model.getInstance();

    private int lastRow;
    private int lastCol;

    /**
     * Constructor of the {@link NetWork}, it creates a list of clients and starts
     * with a new port. <br>
     * Moreover, a new accepting thread is started.
     */
    public NetWork() {
        try {
            ss = new ServerSocket(1129);
            cnt = -1;
            outs = new ArrayList<ObjectOutputStream>();
            ins = new ArrayList<ObjectInputStream>();
            clients = new ArrayList<Socket>();
            exec.submit(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        new NetWork();
    }

    @Override
    public void run() {
        try {
            System.out.println("Server runs");

            while (true) {
                Socket s = ss.accept();
                
                cnt += 1;
                clients.add(s);
                outs.add(new ObjectOutputStream(s.getOutputStream()));
                ins.add(new ObjectInputStream(s.getInputStream()));

                lastCol = lastRow = -1;

                System.out.println("new connected.");
                exec.submit(new ServiceClientsThread(s, cnt));
                /** Things all done, allow CPU to do other things, especially listening to clients. */
                Thread.yield();
            }
        } catch (EOFException e) {
            System.out.println("Closing...");
            try {
                for (Socket client : clients) {
                    client.close();
                }
                outs.clear();
                ins.clear();
            } catch (IOException err) {
                err.printStackTrace();
            }
        } catch (IOException e) {
            // TODO
        }
    }

    /**
     * This is a thread to handle each newly connected {@code Socket} which is
     * activated by a new client. It has a method to read {@code SendPackage} sent
     * either by the {@code Controller} or the {@code Viewer}, and a method to
     * {@code writeToAll}, as to synchronize the two clients.
     * 
     * @author: Mark Chen
     * @version: 1.2
     * @see: {@link Thread}
     */
    private class ServiceClientsThread extends Thread {
        private int serial;

        private Socket s;

        /**
         * This method gets a stringified time format, which then serves as a prefix of
         * the {@code message}.
         * 
         * @param date
         * @return {@link String}
         */
        private String getStringedTime(Date date) {
            SimpleDateFormat df = new SimpleDateFormat("[HH:mm:ss]");
            return df.format(date);
        }

        /**
         * This method gets the String of the Color.
         * 
         * @param color
         * @return String
         */
        private String getStringedColor(Color color) {
            return color == Color.BLACK ? "黑方" : "白方";
        }

        /**
         * This method gets the opponent.
         * 
         * @param color
         * @return {@link Color}
         */
        private Color getReversedColor(Color color) {
            return color != Color.BLACK ? Color.BLACK : Color.WHITE;
        }

        /**
         * This method gets the String of the hint information.
         * 
         * @param color
         * @param row
         * @param col
         * @return {@link String}
         */
        private String getStringedInformation(Color color, int row, int col) {
            return "提示信息：" + this.getStringedColor(color) + "在(" + row + ", " + col + ")处下了棋！";
        }

        private String getStringedChattingInformation(Date date, String alias, Color color, String message) {
            return this.getStringedTime(date) + alias + "(" + this.getStringedColor(color) + ")" + "：" + message;
        }

        private String getStringedWinInformation(Color color) {
            return this.getStringedColor(color) + "胜利！";
        }

        private String getStringedSetAliasInformation(String alias) {
            return "将昵称设置为" + "“" + alias + "”";
        }

        private String getStringedSendFlowerInformation(String alias) {
            return "“" + alias + "”" + "给对方送了一朵鲜花！";
        }

        private String getStringedUndoRequestInformation(Color color) {
            return getStringedColor(color) + "想要悔棋，同意吗？";
        }

        private String getStringedUndoRequestAcceptedInformation() {
            return "悔棋成功！";
        }

        private String getStringedUndoRequesDeclinedInformation() {
            return "悔棋拒绝！";
        }
        /**
         * Constructor of the {@link ServiceClientsThread}. It creates a new
         * {@link ObjectInputStream}.
         * 
         * @param s
         * @throws IOException
         */
        public ServiceClientsThread(Socket s, int serial) throws IOException {
            this.s = s;
            this.serial = serial;
        }

        /**
         * This method writes to all the clients by sending {@link ReceptionPackage}
         * back.
         * 
         * @param rpkg the package sent back.
         * @throws IOException
         * @throws Exception
         */
        private void writeToAll(ReceptionPackage rpkg) throws IOException, Exception {
            for (ObjectOutputStream oos : outs) {
                synchronized (oos) {
                    oos.writeObject(rpkg);
                    oos.flush();
                }
                
            }
        }

        /**
         * This method tries to fetch a {@link SendPackage} from the
         * {@link ObjectInputStream} generated by the thread. However, as multithreaded
         * application may try to read or write to all the Objects passed by the
         * handler, it is a must to make sure that the fetch process is synchronized, or
         * the {@code readObject()} method will throw an Exception, indicating that the
         * {@link ObjectInputStream} is somehow corrupted, and that is caused by two (or
         * more) threads simultaneously alters the {@link ObjectInputStream}, and tries
         * to fetch informations from it, after which the stream will accidentally move
         * a bit forward.
         * 
         * <br> Synchronizing issue is addressed by implementing the {@link ExecutorService}
         * whereby the generation of threadpool is possible.
         * 
         * @author: Mark Chen
         * @version: 1.3
         * @see <a href="https://stackoverflow.com/a/23580843/14875612">
         *          An synchronized issue
         *      </a>
         * 
         * @return {@link SendPackage}
         * @throws NullPointerException
         * @throws Exception
         * @throws EOFException
         */
        private SendPackage getSendPackage() throws NullPointerException,
                                                                 EOFException,
                                                                 Exception {
            ObjectInputStream ois = ins.get(this.serial);
            Object pkg = ois.readObject();
            if (pkg instanceof SendPackage) {
                return (SendPackage) pkg;
            }

            return null;
        }

        /**
         * Asynchronous reading method, but {@code writeToAll} is synchronuous. This
         * method creates a appropriate {@link ReceptionPackage}, and invokes the write
         * method.
         * 
         * 
         * @author: Mark Chen
         * @version: 2.1
         * @see: {@link ServiceClientsThread#getSendPackage()}
         * 
         * @throws Exception
         * @throws EOFException
         * @throws IOException
         * 
         */
        private void readFromAll() throws IOException,
                                          EOFException,
                                          Exception {
            while (true) {
                SendPackage pkg = this.getSendPackage();
                if (pkg == null) {
                    continue;
                }

                if (pkg.getMessageType() == 0) {
                    PutChessState state = model.putChess(pkg);
                    whosRound = model.getRound();
                    ReceptionPackage rpkg = new ReceptionPackage(pkg.getColor(), model.getRound(), state, pkg.getRow(),
                            pkg.getCol(), model.getChessBoard());
                    rpkg.setMessageType(0);

                    if (state == PutChessState.WIN) {
                        model.clear();
                        rpkg.setMessage(this.getStringedWinInformation(pkg.getColor()));
                    } else if (state != PutChessState.NORMAL && state != PutChessState.WIN) {
                        continue;
                    } else {
                        rpkg.setMessage(this.getStringedInformation(pkg.getColor(), pkg.getRow(), pkg.getCol()));
                    }

                    lastRow = pkg.getRow();
                    lastCol = pkg.getCol();
                    writeToAll(rpkg);
                } else if (pkg.getMessageType() == 1) {
                    ReceptionPackage rpkg = new ReceptionPackage();
                    rpkg.setMessageType(1);
                    rpkg.setMessage(this.getStringedChattingInformation(pkg.getDate(), pkg.getAlias(), pkg.getColor(),
                            pkg.getMessage()));
                    rpkg.setColor(pkg.getColor());
                    writeToAll(rpkg);
                } else if (pkg.getMessageType() == 2) {
                    ReceptionPackage rpkg = new ReceptionPackage();
                    rpkg.setMessageType(2);
                    rpkg.setColor(pkg.getColor());
                    rpkg.setAlias(pkg.getAlias());
                    rpkg.setMessage(this.getStringedSetAliasInformation(pkg.getAlias()));
                    writeToAll(rpkg);
                } else if (pkg.getMessageType() == 4) {
                    ReceptionPackage rpkg = new ReceptionPackage();
                    rpkg.setMessageType(4);
                    rpkg.setMessage(this.getStringedSendFlowerInformation(pkg.getAlias()));
                    writeToAll(rpkg);
                } else if (pkg.getMessageType() == 5) {
                    ReceptionPackage rpkg = new ReceptionPackage();
                    rpkg.setChesses(model.getChessBoard());
                    writeToAll(rpkg);
                } else if (pkg.getMessageType() == 6) {
                    // 你下棋的时候不能悔棋。

                    if (pkg.getColor() != model.getRound()) {
                        ReceptionPackage rpkg = new ReceptionPackage();
                        rpkg.setMessageType(6);
                        rpkg.setMessage(getStringedUndoRequestInformation(pkg.getColor()));
                        rpkg.setColor(pkg.getColor());
                        writeToAll(rpkg);
                    } else {
                        // TODO
                    }
                    
                } else if (pkg.getMessageType() == 7) {
                    ReceptionPackage rpkg = new ReceptionPackage();
                    rpkg.setMessageType(7);
                    rpkg.setMessage(getStringedUndoRequestAcceptedInformation());
                    rpkg.setColor(pkg.getColor());
                    rpkg.setRound(getReversedColor(pkg.getColor()));
                    model.reverseRound();

                    if (lastRow != -1 && lastCol != -1) {
                        model.setToNone(lastRow, lastCol);
                    }
                    rpkg.setChesses(model.getChessBoard());
                    
                    writeToAll(rpkg);
                } else if (pkg.getMessageType() == 8) {
                    ReceptionPackage rpkg = new ReceptionPackage();
                    rpkg.setMessageType(8);
                    rpkg.setMessage(getStringedUndoRequesDeclinedInformation());
                    rpkg.setColor(pkg.getColor());
                    writeToAll(rpkg);
                }
            }
        }

        /**
         * This method does cleaning jobs and clear all the {@link ArrayList}s.
         * @throws Exception
         */
        private void clearAllClients() throws Exception {
            clients.get(serial).close();
            clients.remove(serial);
            ins.remove(serial);
            outs.remove(serial);
            
            if (--cnt == -1) {
                model.clear();
                lastCol = lastRow = -1;
                model.setLastColor(Color.WHITE);
            }
        }

        @Override
        public void run() {
            try {
                this.readFromAll();
            } catch (Exception e) {
                try {
                    this.clearAllClients();
                } catch (Exception err) {
                    err.printStackTrace();
                }
            }
        }
    }
}
