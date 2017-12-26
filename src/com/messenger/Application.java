package com.messenger;

import com.messenger.console.DefaultConsole;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
    private List<Connection> connections;

    /**
     * <p>Represents the own server.</p>
     */
    public Peer me;

    /**
     * <p></p>
     * @param port  own port
     * @param name  own name
     */
    public Application (int port, String name) {
        try {
            // init own peer
            me = new Peer(InetAddress.getLocalHost().getHostAddress(), port, name);
            System.out.println("You: " + me.toString());
            // init synchronized peer list
            List<Connection> prepare = new ArrayList<>();
            connections = Collections.synchronizedList(prepare);

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
            System.err.println("Fatal Error: Can not get own ip address.");
        }
    }

    /**
     * <p></p>
     * @param c connection to remove
     */
    public void removeConnection (Connection c) {
        // closing given connection
        c.close();

        // removing connection from peer list
        connections.remove(c);
    }

    /**
     * <p>Handles messages the server receives.
     * Forwards POKE and DISCONNECT messages and
     * prints MESSAGE messages.</p>
     * @param input raw input message
     */
    public void receiveMessage (String input) {
        // generating message from input string
        Message message = new Message(input);
        System.out.println("Receiving: " + input);

        // behaviour by command
        statement:
        switch (message.getCommand()) {
            case "POKE": {
                // check if peer is already in peer list
                for (Connection c : connections) {
                    if (c.getPeer().equals(message.getPeer())) {
                        System.out.println("FOUND PEER IN LIST!");
                        // reset last poke time
                        c.resetLastPoke();
                        // end switch statement
                        break statement;
                    }
                }

                // forward message to whole peer list
                for (Connection c : connections) {
                    c.sendMessage(message);
                }

                // add peer to peer list
                Connection newPeer = new Connection(message.getPeer());
                newPeer.poke(this);
                connections.add(newPeer);
                break;
            }

            case "DISCONNECT": {
                // check if peer is in peer list
                for (Connection c : connections) {
                    if (c.getPeer().equals(message.getPeer())) {
                        // remove him from peer list
                        removeConnection(c);

                        // forward disconnect message to all peers
                        for (Connection g : connections) {
                            g.sendMessage(message);
                        }
                    }
                }

                break;
            }

            case "MESSAGE": {
                // show received message
                System.out.println(message.toString());
                break;
            }

            default: {
                throw new IllegalStateException("Valid command expected, but "
                        + message.getCommand()+ " found instead.");
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
                System.out.println("Sending message " + message.toString() + " to " + c.getPeer().toString());
                c.sendMessage(message);
                break;
            }
        }

        throw new IllegalArgumentException("Valid adress expected. " +
                "The client you tried to message may have gone offline.");
    }

    /**
     * <p></p>
     * @param name      name to send message to
     * @param message   message
     */
    public void sendMessagesByName (String name, Message message) {
        for (Connection c : connections) {
            if (c.getPeer().getName().equals(name)) {
                System.out.println("Sending message " + message.toString() + " to " + c.getPeer().toString());
                c.sendMessage(message);
            }
        }
    }

    /**
     * <p></p>
     */
    public void exit () {
        server.terminate();
        timer.terminate();
        System.out.println("Shutdown all servers.");

        for (Connection c : connections) {
            System.out.println("Sending DISCONNECT message to " + c.getPeer().toString());
            c.sendMessage(new Message("DISCONNECT", me));
            c.close();
        }
    }

    /**
     * <p>Gets {@link Application#connections}.</p>
     * @return  {@link Application#connections}
     */
    public List<Connection> getConnections () {
        return connections;
    }

    /**
     * <p></p>
     */
    private class Server extends Thread {

        /**
         * <p>Status of thread.</p>
         */
        private boolean _terminate = false;

        /**
         * <p>Application the server should work on.</p>
         */
        private Application app;

        /**
         * <p>Port the server is listening on.</p>
         */
        private int port;

        /**
         * <p>Creates a server with an application and
         * the port it should listen on.</p>
         * @param app   application
         * @param port  listening port
         */
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

                while (!_terminate) {
                    // listen for new messages
                    Socket client = socket.accept();
                    ClientHandler clientHandler = new ClientHandler(client, app);
                    clientHandler.start();
                }
            } catch (IOException e) {
                System.err.println("Fatal Error: Can not start server.");
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
     * <p></p>
     */
    private class ClientHandler extends Thread {

        private boolean _terminate = false;

        /**
         * <p></p>
         */
        private Socket socket;

        /**
         * <p></p>
         */
        private Application app;

        /**
         * <p></p>
         * @param socket
         * @param app
         */
        ClientHandler (Socket socket, Application app) {
            this.socket = socket;
            this.app = app;
        }

        /**
         * <p></p>
         */
        @Override
        public void run () {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                while (!_terminate) {
                    app.receiveMessage(reader.readLine());
                }
                socket.close();
            } catch (IOException e) {
                System.out.println("");
            }
        }

        /**
         * <p></p>
         */
        public void terminate () {
            _terminate = true;
        }
    }

    /**
     * <p>Responsible for updating the peer list. Can be
     * started by calling {@link Timer#run()} and terminated
     * by calling {@link Timer#terminate()}.</p>
     */
    private class Timer extends Thread {

        /**
         * <p></p>
         */
        private boolean _terminate = false;

        /**
         * <p></p>
         */
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
                try {
                    System.out.println("TIMER: Executed.");
                    // init buffer, to store inactive peers
                    ArrayDeque<Connection> buffer = new ArrayDeque<>();

                    // iterate peer list
                    for (Connection c : app.getConnections()) {
                        System.out.println("PEER: " + c.getPeer().toString());
                        if (c.isInactive()) {
                            // add inactive peers to buffer
                            buffer.add(c);
                        } else {
                            c.poke(app);
                        }
                    }

                    // remove all inactive peers from peer list
                    app.getConnections().removeAll(buffer);

                    // execute procedure every 30 seconds
                    sleep(30000);
                } catch (InterruptedException e) {
                    System.err.println("Error: Thread interrupted.");
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