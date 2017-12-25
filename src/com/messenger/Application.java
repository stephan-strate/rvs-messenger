package com.messenger;

import com.messenger.console.DefaultConsole;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.ArrayDeque;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * <p></p>
 */
public class Application {

    /**
     * <p>Server to listen for messages. Runs in
     * a separate thread.</p>
     */
    private Server server;

    /**
     * <p>Timer, that executes a procedure every
     * 30 seconds.</p>
     */
    private Timer timer;

    /**
     * <p>Peer list, that contains all active
     * (maybe inactive) connections with peers.</p>
     */
    private CopyOnWriteArrayList<Connection> connections;

    /**
     * <p>Represents the own server.</p>
     */
    private Peer me;

    /**
     * <p></p>
     * @param port  own port
     * @param name  own name
     */
    public Application (int port, String name) {
        try  {
            // init own peer
            me = new Peer(InetAddress.getLocalHost().getHostName(), port, name);
            // init peer list
            connections = new CopyOnWriteArrayList<>();

            // run server
            server = new Server(this, port);
            server.start();

            // run timer
            timer = new Timer(this);
            timer.start();

            // init a console associated with this application
            DefaultConsole console = new DefaultConsole(this);
            console.start();
        } catch (UnknownHostException e) {
            System.out.println("Can not get own ip address.");
        }
    }

    /**
     * <p></p>
     * @param peer
     */
    public synchronized void addConnection (Peer peer) {
        // add new connection to peer list
        connections.add(new Connection(peer));
    }

    /**
     * <p></p>
     * @param c
     */
    public synchronized void removeConnection (Connection c) {
        // closing given connection
        c.close();

        // removing connection from peer list
        connections.remove(c);
    }

    /**
     * <p></p>
     * @param input
     */
    public void receiveMessage (String input) {
        // generating message from input string
        Message message = new Message(input);

        // behaviour by command
        switch (message.getCommand()) {
            case "POKE": {
                for (Connection c : connections) {
                    if (c.getPeer().equals(message.getPeer())) {
                        c.resetLastPoke();
                        break;
                    }
                }

                // @TODO: Add peer to list
                break;
            }

            case "DISCONNECT": {
                String command = "DISCONNECT";
                Message disconnect = new Message(command, message.getPeer());

                for (Connection c : connections) {
                    if (c.getPeer().equals(message.getPeer())) {
                        removeConnection(c);

                        for (Connection g : connections) {
                            sendMessage(g.getPeer(), disconnect);
                        }
                    }
                }

                break;
            }

            case "MESSAGE": {
                System.out.println("Message received.");
                System.out.println(message.toString());
                // @TODO: Handle messages
                break;
            }

            default: {
                throw new IllegalStateException("Valid command expected, but "
                        +message.getCommand()+ " found instead.");
            }
        }
    }

    /**
     * <p></p>
     * @param peer      peer to send the message to
     * @param message   message
     */
    public void sendMessage (Peer peer, Message message) {
        for (Connection c : connections) {
            if (c.getPeer().equals(peer)) {
                c.sendMessage(message);
                break;
            }
        }

        throw new IllegalArgumentException("Valid adress expected. " +
                "The client you tried to message may have gone offline.");
    }

    /**
     * <p></p>
     */
    public void exit () {
        server.terminate();
        timer.terminate();

        for (Connection c : connections) {
            Message message = new Message("DISCONNECT", c.getPeer());
            c.sendMessage(message);
            c.close();
        }
    }

    public CopyOnWriteArrayList<Connection> getConnections () {
        return connections;
    }

    /**
     * <p></p>
     */
    private class Server extends Thread {

        private boolean _terminate = false;
        private Application app;
        private int port;

        Server (Application app, int port) {
            this.app = app;
            this.port = port;
        }

        /**
         * <p></p>
         */
        @Override
        public void run () {
            try {
                // open server
                ServerSocket socket = new ServerSocket();
                socket.bind(new InetSocketAddress(port));

                while(!_terminate) {
                    System.out.println("Server executed.");
                    // listen for new messages
                    Socket client = socket.accept();
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(
                                    client.getInputStream()));
                    app.receiveMessage(in.toString());
                    socket.close();
                }
            } catch (IOException e) {
                System.err.println("Server kann nicht gestartet werden.");
            }
        }

        /**
         * <p>Terminate thread, by ending the loop in
         * {@link Server#run()}.</p>
         */
        void terminate () {
            _terminate = true;
        }
    }

    /**
     * <p>Responsible for updating the peer list. Can be
     * started by calling {@link Timer#run()} and terminated
     * by calling {@link Timer#terminate()}.</p>
     */
    private class Timer extends Thread {

        private boolean _terminate = false;
        private long timestamp = System.currentTimeMillis()/1000L;
        private Application app;

        /**
         * <p></p>
         * @param app
         */
        Timer (Application app) {
            this.app = app;
        }

        /**
         * <p>Procedure that executes every 30 seconds and
         * updates the peer list of application.</p>
         */
        @Override
        public void run () {
            while (!_terminate) {
                // execute procedure every 30 seconds
                if (timestamp + 30 < System.currentTimeMillis()/1000L) {
                    System.out.println("Timer executed.");
                    // init buffer, to store inactive peers
                    ArrayDeque<Connection> buffer = new ArrayDeque<>();

                    // iterate peer list
                    for (Connection c : app.getConnections()) {
                        if (c.isInactive()) {
                            // add inactive peers to buffer
                            buffer.add(c);
                        } else {
                            // poke active peers
                            String command = "POKE";
                            String ip = c.getPeer().getHostName();
                            int port = c.getPeer().getPort();
                            String name = c.getPeer().getName();

                            Message message = new Message(command, ip, port, name);
                            c.sendMessage(message);
                        }
                    }

                    // remove all inactive peers from peer list
                    app.getConnections().removeAll(buffer);
                    // track new timestamp
                    timestamp = System.currentTimeMillis()/1000L;
                }
            }
        }

        /**
         * <p>Terminate thread, by ending the loop in
         * {@link Timer#run()}.</p>
         */
        void terminate () {
            _terminate = true;
        }
    }
}