package com.messenger;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * <p></p>
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
     * <p></p>
     * @param peer
     */
    public Connection (Peer peer) {
        this.peer = peer;

        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(peer.getHostName(), peer.getPort()));

            writer = new PrintWriter(socket.getOutputStream(), true);

            resetLastPoke();

            timer = new Timer(this);
            timer.start();
        } catch (IOException e) {
            System.err.println("Connection couldn't be initiated properly.");
            e.printStackTrace();
        }
    }

    public void close () {
        timer.terminate();

        try {
            socket.close();
        } catch (IOException e) {
            System.err.println("Connection couldn't be terminated properly.");
        }
    }

    public void sendMessage (Message message) {
        writer.println(message.toString());
        writer.flush();
    }

    public void resetLastPoke () {
        lastPoke = System.currentTimeMillis()/1000L;
    }

    public void setInactive () {
        active = false;
    }

    public boolean isInactive () {
        return !active;
    }

    public long getLastPoke () {
        return lastPoke;
    }

    public Peer getPeer () {
        return peer;
    }

    private class Timer extends Thread {

        private boolean _terminate = false;
        private Connection con;

        Timer (Connection con) {
            lastPoke = System.currentTimeMillis() / 1000L;
            this.con = con;
        }

        @Override
        public void run () {
            while (!_terminate) {
                if (con.getLastPoke() + 60 < System.currentTimeMillis()/1000L) {
                    con.setInactive();
                }
            }
        }

        void terminate () {
            _terminate = true;
        }
    }
}