import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;

public class MusicServer {

    ArrayList<ObjectOutputStream> clientOutputStreams;

    public static void main(String[] args) {
        new MusicServer().go();
    }

    public class ClientHandler implements Runnable {
        ObjectInputStream in;
        Socket clientSocket;

        public ClientHandler(Socket socket) {
            try {
                clientSocket = socket;
                in = new ObjectInputStream(clientSocket.getInputStream());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            Object o2 = null;
            Object o1 = null;

            try {
                while ((o1 = in.readObject()) != null) {
                    o2 = in.readObject();
                    System.out.println("read 2 objects");
                    tellEveryone(o1,o2);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void tellEveryone(Object o1, Object o2) {
        Iterator it = clientOutputStreams.iterator();
        while (it.hasNext()){
            try {
                ObjectOutputStream out = (ObjectOutputStream) it.next();
                out.writeObject(o1);
                out.writeObject(o2);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void go() {
        clientOutputStreams = new ArrayList<ObjectOutputStream>();
        try {
            ServerSocket serverSock = new ServerSocket(4242);

            while (true) {
                Socket clientSocket = serverSock.accept();
                ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
                clientOutputStreams.add(out);

                Thread t = new Thread(new ClientHandler(clientSocket));
                t.start();

                System.out.println("got a connection");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}