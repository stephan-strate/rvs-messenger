package com.messenger;

import com.messenger.console.DefaultConsole;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.*;

/**
 * <p>Represents the main application. Here we start our server,
 * receiving new messages, take care of our peer list and providing
 * the console functions. Everything is started in
 * {@link Application#Application(int, String)}.</p>
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
     * <p>Main constructor of whole application. Peer list gets
     * initiated, Server and Timer are started and the console
     * is started.</p>
     * @param port  own port
     * @param name  own name
     */
    public Application (int port, String name) {
        try {
            // fetch external ip address
            URL getMyIp = new URL("http://checkip.amazonaws.com");
            BufferedReader in = new BufferedReader(new InputStreamReader(getMyIp.openStream()));

            // init own peer
            me = new Peer(in.readLine(), port, name);
            /* LOCAL DEBUG */ me = new Peer(InetAddress.getLocalHost().getHostAddress(), port, name); /* LOCAL DEBUG END */
            // init synchronized peer list
            List<Connection> prepare = new ArrayList<>();
            connections = Collections.synchronizedList(prepare);

            // run server
            server = new Server(this, port);
            server.start();

            // run timer
            timer = new Timer(this);
            timer.start();

            System.out.println("> [" + new Date().toString() + "] You are logged in as " + name + " and listening on " + me.getHostName() + ":" + me.getPort() + ".");

            // init a console associated with this application
            DefaultConsole console = new DefaultConsole(this);
            console.start();
        } catch (IOException e) {
            throw new IllegalArgumentException("Fatal Error: Can not fetch your remote ip address.\n" +
                    "Check your internet connection.");
        }
    }

    /**
     * <p>Remove a connection from peer list. This is necessary,
     * because connection will be closed and removed from the
     * synchronized list.</p>
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

        System.out.println("§ DEBUG > Message received: " + message.toString());

        // behaviour by command
        statement:
        switch (message.getCommand()) {
            case "POKE": {
                // check if peer is already in peer list
                for (Connection c : connections) {
                    if (c.getPeer().equals(message.getPeer())) {
                        // reset last poke time
                        c.resetLastPoke();

                        System.out.println("§ DEBUG > Check! Peer already in list: " + c.getPeer().toString());

                        // end switch statement
                        break statement;
                    }
                }

                // forward poke to whole peer list
                for (Connection c : connections) {
                    c.sendMessage(message);
                }

                Connection newPeer = new Connection(message.getPeer());
                // send poke to the new connection
                newPeer.poke(this);
                // add peer to peer list
                connections.add(newPeer);

                System.out.println("§ DEBUG > New peer added to peer list: " + newPeer.getPeer().toString());

                System.out.println("> [" + new Date().toString() + "] " + newPeer.getPeer().getName() + " (" + newPeer.getPeer().getHostName() +
                        ":" + newPeer.getPeer().getPort() + ") is online.");
                break;
            }

            case "DISCONNECT": {
                // check if peer is in peer list
                for (Connection c : connections) {
                    if (c.getPeer().equals(message.getPeer())) {
                        System.out.println("> [" + new Date().toString() + "] " + c.getPeer().getName() + " (" + c.getPeer().getHostName() +
                            ":" + c.getPeer().getPort() + ") disconnected.");

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
                // show received message with timestamp, name and text
                System.out.println("> [" + new Date().toString() + "] " + message.getPeer().getName() + ": " + message.getText());
                break;
            }

            default: {
                throw new IllegalStateException("Valid command expected, but "
                        + message.getCommand()+ " found instead.");
            }
        }
    }

    /**
     * <p>Checking if peer, you want to send a message
     * to, is in peer list and sends the message if so.
     * Otherwise throws a exception.</p>
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
     * <p>Sends the message to all peers in peer list, which
     * have the given name. If no peer has the given name,
     * nothing happens.</p>
     * @param name      name to send message to
     * @param message   message
     */
    public void sendMessagesByName (String name, Message message) {
        for (Connection c : connections) {
            if (c.getPeer().getName().equals(name)) {
                c.sendMessage(message);
            }
        }
    }

    /**
     * <p>Shutdown server and timer thread and sending disconnect
     * messages to the whole peer list, to signal that we going
     * offline.</p>
     */
    public void exit () {
        // shutdown server
        server.terminate();
        System.out.println("> [" + new Date().toString() + "] Server shutdown.");

        // terminate timer
        timer.terminate();
        System.out.println("> [" + new Date().toString() + "] Timer terminated.");

        // sending disconnect messages to peer list
        for (Connection c : connections) {
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
     * <p>Represents the server that is listening for new
     * connections and takes care of them.</p>
     */
    private class Server extends Thread {

        /**
         * <p>Status of thread.</p>
         */
        private boolean _terminate = false;

        /**
         * <p>Application the server should work on.</p>
         */
        private Application application;

        /**
         * <p>Port the server is listening on.</p>
         */
        private int port;

        /**
         * <p>Creates a server with an application and
         * the port it should listen on.</p>
         * @param application   application
         * @param port          listening port
         */
        Server (Application application, int port) {
            this.application = application;
            this.port = port;
        }

        /**
         * <p>Runs the server on {@link Server#port} and accepts
         * connection/parses them to a new {@link ClientHandler} thread.</p>
         */
        @Override
        public void run () {
            try {
                // open server
                ServerSocket socket = new ServerSocket();
                // binding port
                socket.bind(new InetSocketAddress(port));

                while (!_terminate) {
                    // listen for new messages
                    Socket client = socket.accept();
                    ClientHandler clientHandler = new ClientHandler(application, client);
                    clientHandler.start();
                }
            } catch (IOException e) {
                throw new IllegalArgumentException("Fatal Error: Can not start server.");
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
     * <p>Receives new messages from a socket connection
     * and parses them back to the application.</p>
     */
    private class ClientHandler extends Thread {

        /**
         * <p>Status of thread.</p>
         */
        private boolean _terminate = false;

        /**
         * <p>Application the socket can send
         * messages to.</p>
         */
        private Application application;

        /**
         * <p>Open connection to the client, can
         * receive messages.</p>
         */
        private Socket socket;

        /**
         * <p>Creates a handler that is receiving messages
         * from a specific socket (simplex) and parsing them
         * to the application.</p>
         * @param application   application
         * @param socket        connection
         */
        ClientHandler (Application application, Socket socket) {
            this.application = application;
            this.socket = socket;
        }

        /**
         * <p></p>
         */
        @Override
        public void run () {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        socket.getInputStream()));

                // constantly read messages from socket
                while (!_terminate) {
                    application.receiveMessage(reader.readLine());
                }

                // closing socket when terminated
                socket.close();
            } catch (IOException e) {
                throw new IllegalArgumentException("Error: Can not read message.");
            }
        }

        /**
         * <p>Terminate thread, by ending the loop in
         * {@link ClientHandler#run()}.</p>
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

        /**
         * <p>Status of thread.</p>
         */
        private boolean _terminate = false;

        /**
         * <p>Application the timer works on.</p>
         */
        private Application application;

        /**
         * <p>Creates a new timer for application.</p>
         * @param application   application
         */
        Timer (Application application) {
            this.application = application;
        }

        /**
         * <p>Procedure that executes every 30 seconds and
         * updates the peer list of application.</p>
         */
        @Override
        public void run () {
            while (!_terminate) {
                try {
                    // init buffer, to store inactive peers
                    ArrayDeque<Connection> buffer = new ArrayDeque<>();

                    // iterate peer list
                    for (Connection c : application.getConnections()) {
                        if (c.isInactive()) {
                            // add inactive peers to buffer
                            buffer.add(c);
                        } else {
                            // poke active peers
                            c.poke(application);
                        }
                    }

                    // remove all inactive peers from peer list
                    for (Connection c : buffer) {
                        application.removeConnection(c);
                    }

                    // wait 30 seconds before executing again
                    sleep(30000);
                } catch (InterruptedException e) {
                    throw new IllegalArgumentException("Error: Thread interrupted.");
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