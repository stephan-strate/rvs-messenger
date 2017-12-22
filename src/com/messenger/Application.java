import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayDeque;

public class Application {

    private Console console;

    private ServerThread server;
    private HandleConnectionsThread poke;

    private ArrayDeque<Connection> connections; 
    //TODO write mutex for accessing the list, synchronized is not sufficient when
    //server is adding connections while poke is iterating over the list

    public Application(String name, int port) {
        console = new Console(this);

        connections = new ArrayDeque<>();

        server = new ServerThread();
        poke = new HandleConnectionsThread();

        server.run(this, port);
        poke.run(this);
    }

    public ArrayDeque<Connection> getConnections() {
        return connections;
    }

    public synchronized void addConnection(Connection c) {
        c.init(this);
        connections.add(c);
    }

    public synchronized void removeConnection(Connection c) {
        c.close();
        connections.remove(c);
    }

    public void recieveMessage(String message) {
        //TODO check kind of message
        //TODO either console.print, disconnect peer, or poke
    }

    public void sendMessage(Peer peer, String message) {
        for(Connection c : connections) {
            if(c.getPeer() == peer) {
                c.sendMessage(message);
                return;
            }
        }
        throw new IllegalArgumentException("Valid adress expected. " +
                "The client you tried to message may have gone offline.");
    }

    public void exit() {
        server.terminate();
        poke.terminate();

        for(Connection c : connections) {
            //TODO send disconnect
            c.close();
        }
    }

    private class ServerThread extends Thread {

        private boolean active;

        public ServerThread() {
            active = true;
        }

        public void run(Application app, int port) {
            try{
                ServerSocket socket = new ServerSocket(port);
                while(active) {
                    Socket client = socket.accept();
                    //TODO get client name
                    //TODO app.addConnection
                }
            }
            catch(IOException e) {
                System.out.println("Failed to initalize server socket.");
                e.printStackTrace();
            }
        }

        public void terminate() {
            active = false;
        }
    }

    private class HandleConnectionsThread extends Thread {

        private boolean active;
        private long timer;

        public HandleConnectionsThread() {
            active = true;
        }

        public void run(Application app) {
            timer = System.currentTimeMillis()/1000L;
            while(active) {
                if(timer + 30 < System.currentTimeMillis()/1000L) {
                    ArrayDeque<Connection> buffer = new ArrayDeque<>();
                    for(Connection c : app.getConnections()) {
                        if(c.isInactive()) {
                            buffer.add(c);
                            continue;
                        }
                        //c.sendMessage Poke
                    }
                    app.getConnections().removeAll(buffer);
                    timer = System.currentTimeMillis()/1000L;
                }
            }
        }

        public void terminate() {
            active = false;
        }
    }
}
