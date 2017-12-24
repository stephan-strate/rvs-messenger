package com.messenger;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

public class Connection {

    private boolean active;
    private long lastPoke;

    private Peer peer;
    private Socket socket;

    private PrintWriter writer;
    private Timer timer;

    public Connection (Peer peer) {
        active = true;
        this.peer = peer;
    }

    public void init (Application app) {
        try {
            System.out.println(peer.getHostName() + " " + peer.getPort());
            socket = new Socket();
            socket.connect(new InetSocketAddress(peer.getHostName(), peer.getPort()));

            writer = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            System.err.println("Connection couldn't be initiated properly.");
            e.printStackTrace();
        }

        resetLastPoke();
        timer = new Timer();
        timer.run(this);
    }

    public void close () {
        timer.terminate();

        try {
            socket.close();
        } catch (IOException e) {
            System.err.println("Connection couldn't be terminated properly.");
        }
    }

    public Peer getPeer() {
        return peer;
    }

    public long getLastPoke() {
        return lastPoke;
    }

    public void resetLastPoke() {
        lastPoke = System.currentTimeMillis()/1000L;
    }

    public boolean isInactive() {
        return !active;
    }

    public void setInactive() {
        active = false;
    }

    public void sendMessage(Message message) {
        try {
            writer.write(message.toString(), 0, message.toString().length());
        } catch (Exception e){
            System.err.println("OutputStreamWriter could not send message.");
        }
    }

    private class Timer extends Thread {

        private boolean _terminate = false;

        Timer () {
            lastPoke = System.currentTimeMillis() / 1000L;
        }

        void run (Connection con) {
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