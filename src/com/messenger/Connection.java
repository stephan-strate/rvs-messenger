package com.messenger;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * <p>Represents a connection with a peer. You
 * can send messages to the given peer.
 * It is a simplex connection, so you can not
 * receive any messages.</p>
 */
public class Connection {

    /**
     * <p>Status of the connection, is updated every
     * 60 seconds by timer class.</p>
     */
    private boolean active = true;

    /**
     * <p>Timestamp of last poke received by this
     * connection.</p>
     */
    private long lastPoke;

    /**
     * <p>Timer procedure is executed every 60 seconds
     * and updates the status.</p>
     */
    private Timer timer;

    /**
     * <p>Ip address, port and name of this connection
     * or peer.</p>
     */
    private Peer peer;

    /**
     * <p>Socket of this connection.</p>
     */
    private Socket socket;

    /**
     * <p>Writer to communicate with the peer, created
     * from socket.</p>
     */
    private PrintWriter writer;

    /**
     * <p>Creates a connection to given peer and
     * inits a timer that is constantly checking if
     * connection is still active.</p>
     * @param peer  peer to open connection with
     */
    public Connection (Peer peer) {
        this.peer = peer;

        try {
            System.out.println("Verbinde mit " + peer.getHostName() + ":" + peer.getPort());

            // connect with peer
            socket = new Socket();
            socket.connect(new InetSocketAddress(peer.getHostName(), peer.getPort()));
            System.out.println("Verbindung zu " + peer.getHostName() + ":" + peer.getPort() + " aufgebaut.");

            // start writer, to send messages
            writer = new PrintWriter(socket.getOutputStream(), true);

            // start timer
            timer = new Timer(this);
            timer.start();
        } catch (IOException e) {
            System.err.println("Connection couldn't be initiated properly.");
            e.printStackTrace();
        }
    }

    /**
     * <p>Terminate timer thread and close socket
     * connection.</p>
     */
    public void close () {
        // terminate thread
        timer.terminate();

        try {
            // close socket connection
            socket.close();
        } catch (IOException e) {
            System.err.println("Connection couldn't be terminated properly.");
        }
    }

    /**
     * <p>Sending a message to given connection.</p>
     * @param message   {@link Message} to send
     */
    public void sendMessage (Message message) {
        writer.println(message.toString());
        writer.flush();
    }

    /**
     * <p>Track current timestamp as last
     * poke.</p>
     */
    public void resetLastPoke () {
        lastPoke = System.currentTimeMillis()/1000L;
    }

    /**
     * <p>Set connection status to inactive.</p>
     */
    public void setInactive () {
        active = false;
    }

    /**
     * <p>Check if connection is inactive.</p>
     * @return  not {@link Connection#active}
     */
    public boolean isInactive () {
        return !active;
    }

    /**
     * <p>Gets {@link Connection#lastPoke}.</p>
     * @return  {@link Connection#lastPoke}
     */
    public long getLastPoke () {
        return lastPoke;
    }

    /**
     * <p>Gets {@link Connection#peer}.</p>
     * @return  {@link Connection#peer}
     */
    public Peer getPeer () {
        return peer;
    }

    /**
     * <p>Procedure to check if last poke is
     * 60 seconds ago. Then set connection to
     * inactive.</p>
     */
    private class Timer extends Thread {

        /**
         * <p>Status of thread.</p>
         */
        private boolean _terminate = false;

        /**
         * <p>Connection to work at.</p>
         */
        private Connection con;

        /**
         * <p>Creates a timer. Resetting last poke
         * and applying connection to timer class.</p>
         * @param con   connection
         */
        Timer (Connection con) {
            lastPoke = System.currentTimeMillis() / 1000L;
            this.con = con;
        }

        /**
         * <p>Constantly check if connection is
         * inactive and eventually terminate thread
         * and set connection status.</p>
         */
        @Override
        public void run () {
            while (!_terminate) {
                // last poke was 60+ seconds ago
                if (con.getLastPoke() + 60 < System.currentTimeMillis()/1000L) {
                    // declare connection as inactive
                    con.setInactive();
                    // terminate thread
                    terminate();
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