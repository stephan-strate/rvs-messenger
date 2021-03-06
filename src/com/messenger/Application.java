package com.messenger;

import com.messenger.console.DefaultConsole;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * <p>Represents the main application. Here we start our server,
 * receiving new messages, take care of our peer list and providing
 * the console functions. Everything is started in
 * {@link Application#Application(int, String)}.</p>
 */
public class Application {

    /**
     * <p>Represents the own {@link Peer} with
     * ip address, port and name.</p>
     */
    public Peer me;

    /**
     * <p>Server to listen for messages. Runs in
     * a separate thread.</p>
     */
    private Server server;

    /**
     * <p>Timer, that executes a procedure every
     * 30 seconds. Runs in a separate thread.</p>
     */
    private Timer timer;

    /**
     * <p>Peer list, that contains all active
     * (maybe inactive) connections with peers.</p>
     */
    private LinkedBlockingQueue<Connection> connections;

    /**
     * <p>Main constructor of whole application. Peer list gets
     * initiated, Server and Timer are started and the console
     * is started.</p>
     * @param port  own port
     * @param name  own name
     */
    public Application (int port, String name) {
        try {
            me = new Peer(InetAddress.getLocalHost().getHostAddress(), port, name);
            // init synchronized peer list
            connections = new LinkedBlockingQueue<>();

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
            System.err.println("Fatal Error: Can not fetch your remote ip address.\n" +
                    "Check your internet connection.");
            System.exit(1);
        }
    }

    /**
     * <p>Remove a connection from peer list. This is necessary,
     * because connection will be closed and removed from the
     * synchronized list.</p>
     * @param c connection to remove
     */
    private void removeConnection (Connection c) {
        // closing given connection
        c.close();

        // removing connection from peer list
        connections.remove(c);
    }

    /**
     * <p>Clear whole peer list by sending DISCONNECT messages
     * to everyone and clearing {@link Application#connections}.</p>
     */
    public void removeAll () {
        for (Connection c : connections) {
            c.sendMessage(new Message("DISCONNECT", me));
            c.close();
        }

        // clear list
        connections.clear();
        System.out.println("> [" + new Date().toString() + "] Disconnected from all peers.");
    }

    /**
     * <p>Shutdown server and timer thread.</p>
     */
    public void exit () {
        // disconnect from all peers
        removeAll();

        // shutdown server
        server.terminate();
        System.out.println("> [" + new Date().toString() + "] Server shutdown.");

        // terminate timer
        timer.terminate();
        System.out.println("> [" + new Date().toString() + "] Timer terminated.");
    }

    /**
     * <p>Handles messages the server receives.
     * Forwards POKE and DISCONNECT messages and
     * prints MESSAGE messages.</p>
     * @param input raw     input message
     * @param clientHandler client handler
     */
    private void receiveMessage (String input, ClientHandler clientHandler) {
        // catch null messages
        if (input != null) {
            // generating message from input string
            Message message = new Message(input);

            // behaviour by command
            statement:
            switch (message.getCommand()) {
                case "POKE": {
                    // check if peer is already in peer list
                    for (Connection c : connections) {
                        if (c.getPeer().equals(message.getPeer())) {
                            // reset last poke time
                            c.resetLastPoke();
                            // end switch statement
                            break statement;
                        }
                    }

                    // forward poke to whole peer list
                    for (Connection c : connections) {
                        c.sendMessage(message);
                    }

                    Connection newPeer = new Connection(message.getPeer(), clientHandler);
                    // send poke to the new connection
                    newPeer.poke(this);
                    // add peer to peer list
                    connections.add(newPeer);

                    System.out.println("> [" + new Date().toString() + "] " + newPeer.getPeer().getName() + " (" + newPeer.getPeer().getHostName() + ":" + newPeer.getPeer().getPort() + ") is online.");
                    break;
                }

                case "DISCONNECT": {
                    // check if peer is in peer list
                    for (Iterator<Connection> it = connections.iterator(); it.hasNext();) {
                        Connection c = it.next();
                        if (c.getPeer().equals(message.getPeer())) {
                            System.out.println("> [" + new Date().toString() + "] " + c.getPeer().getName() + " (" + c.getPeer().getHostName() +
                                    ":" + c.getPeer().getPort() + ") disconnected.");

                            // remove him from peer list
                            c.close();
                            it.remove();

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
                    System.out.println("> [" + new Date().toString() + "] " + message.getPeer().getName() + " -> You: " + message.getText());
                    break;
                }

                default: {
                    System.err.println("Valid command expected, but "
                            + message.getCommand()+ " found instead. Message not handled.");
                }
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
        boolean peerFound = false;
        Peer temp = null;
        for (Connection c : connections) {
            if (c.getPeer().equals(peer)) {
                c.sendMessage(message);
                peerFound = true;
                temp = c.getPeer();
            }
        }

        if (!peerFound) {
            System.err.println("> [" + new Date().toString() + "] Valid adress expected. " +
                    "The client you tried to message may have gone offline.");
        } else {
            if (message.hasText() && temp.hasName()) {
                System.out.println("> [" + new Date().toString() + "] You -> " + temp.getName() + ": " + message.getText());
            }
        }

    }

    /**
     * <p>Sends the message to all peers in peer list, which
     * have the given name. If no peer has the given name,
     * nothing happens.</p>
     * @param name      name to send message to
     * @param message   message
     */
    public void sendMessagesByName (String name, Message message) {
        boolean peerFound = false;
        for (Connection c : connections) {
            if (c.getPeer().getName().equals(name)) {
                c.sendMessage(message);
                peerFound = true;
            }
        }

        if (!peerFound) {
            System.err.println("> [" + new Date().toString() + "] No peer with name '" + name + "' found.");
        } else {
            System.out.println("> [" + new Date().toString() + "] You -> " + name + ": " + message.getText());
        }
    }

    /**
     * <p>Gets {@link Application#connections}.</p>
     * @return  {@link Application#connections}
     */
    public LinkedBlockingQueue<Connection> getConnections () {
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
                System.err.println("Fatal Error: Can not start server.");
                System.exit(1);
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
    class ClientHandler extends Thread {

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
         * <p>Listen for new messages from a specific socket and
         * parsing them to {@link Application#receiveMessage(String, ClientHandler)}.</p>
         */
        @Override
        public void run () {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        socket.getInputStream()));

                // constantly read messages from socket
                String last = "";
                while (!_terminate && last != null) {
                    if ((last = reader.readLine()) != null) {
                        application.receiveMessage(last, this);
                    }
                }

                // closing socket when terminated
                socket.close();
            } catch (IOException e) {
                System.err.println("> [" + new Date().toString() + "] Lost connection to peer.");
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
                    System.err.println("Error: Thread interrupted.");
                    System.exit(1);
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