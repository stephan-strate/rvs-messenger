package com.messenger;

import com.messenger.console.DefaultConsole;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.Semaphore;

public class Application {

    private Server server;
    private Timer timer;

    private LinkedBlockingDeque<Connection> connections;
    private Semaphore semaphore;

    public Application (int port, String name) {
        // init a console associated with this application
        DefaultConsole console = new DefaultConsole(this);
        console.start();

        connections = new LinkedBlockingDeque<>();
        semaphore = new Semaphore(1, true);

        server = new Server();
        server.run(this, port);

        timer = new Timer();
        timer.run(this);
    }

    public synchronized void addConnection (Connection c) {
        c.init(this);

        try {
            semaphore.acquire();
            connections.add(c);
        } catch (InterruptedException e) {
            System.out.println("Kann Verbindung nicht hinzufügen.");
        } finally {
            semaphore.release();
        }
    }

    public synchronized void removeConnection (Connection c) {
        c.close();

        try {
            semaphore.acquire();
            connections.remove(c);
        } catch (InterruptedException e) {
            System.out.println("Kann Verbindung nicht entfernen.");
        } finally {
            semaphore.release();
        }
    }

    public void receiveMessage (String raw) {
        Message message = new Message(raw);

        Peer sender = new Peer(message.getIp(), message.getPort(), message.getName());

        switch (message.getCommand()) {
            case "POKE": {
                try {
                    semaphore.acquire();
                    for (Connection c : connections) {
                        if (c.getPeer().equals(sender)) {
                            c.resetLastPoke();
                            break;
                        }
                    }

                    // @TODO: Add peer to list
                } catch (InterruptedException e) {
                    System.out.println("");
                } finally {
                    semaphore.release();
                }

                break;
            }

            case "DISCONNECT": {
                String command = "DISCONNECT";
                String ip = sender.getHostName();
                int port = sender.getPort();
                String name = sender.getName();

                Message disconnect = new Message(command, ip, port, name);

                try {
                    semaphore.acquire();
                    for (Connection c : connections) {
                        if (c.getPeer().equals(sender)) {
                            removeConnection(c);

                            for (Connection g : connections) {
                                sendMessage(c.getPeer(), disconnect);
                            }
                        }
                    }
                } catch (InterruptedException e) {
                    System.out.println("");
                } finally {
                    semaphore.release();
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
        try {
            semaphore.acquire();
            for (Connection c : connections) {
                if (c.getPeer() == peer) {
                    c.sendMessage(message);
                    break;
                }
            }

            throw new IllegalArgumentException("Valid adress expected. " +
                    "The client you tried to message may have gone offline.");
        } catch (InterruptedException e) {
            System.out.println("");
        } finally {
            semaphore.release();
        }
    }

    /**
     * <p></p>
     */
    public void exit () {
        try {
            server.terminate();
            timer.terminate();

            //semaphore.acquire();
            for (Connection c : connections) {
                String command = "DISCONNECT";
                String ip = c.getPeer().getHostName();
                int port = c.getPeer().getPort();
                String name = c.getPeer().getName();

                Message message = new Message(command, ip, port, name);
                c.sendMessage(message);
                c.close();
            }
        } catch (Exception e) {

        } finally {
            //semaphore.release();
        }
    }

    /**
     * <p>Get {@link Application#connections}.</p>
     * @return  {@link Application#connections}
     */
    private LinkedBlockingDeque<Connection> getConnections () {
        return connections;
    }

    /**
     * <p></p>
     */
    private class Server extends Thread {

        private boolean _terminate = false;

        /**
         * <p></p>
         * @param app   application
         * @param port  port to listen on
         */
        void run (Application app, int port) {
            try {
                // open server
                ServerSocket socket = new ServerSocket();
                socket.bind(new InetSocketAddress(port));

                while(!_terminate) {
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

        /**
         * <p>Procedure that executes every 30 seconds and
         * updates the peer list of application.</p>
         * @param app   application to update
         */
        void run (Application app) {
            while (!_terminate) {
                // execute procedure every 30 seconds
                if (timestamp + 30 < System.currentTimeMillis()/1000L) {
                    try {
                        // init buffer, to store inactive peers
                        ArrayDeque<Connection> buffer = new ArrayDeque<>();

                        semaphore.acquire();
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
                    } catch (InterruptedException e) {
                        System.out.println("Timer error.");
                    } finally {
                        semaphore.release();
                    }
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