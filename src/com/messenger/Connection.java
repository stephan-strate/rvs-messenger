package com.messenger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class Connection {

    private boolean active;
    private long lastPoke;

    private Peer peer;
    private Socket socket;

    private Reciever reciever;
    private OutputStreamWriter writer;
    private Timer timer;

    public Connection(Peer peer) {
        active = true;
        this.peer = peer;
    }

    public void init(Application app) {
        try{
            socket = new Socket(peer.getHostName(),
                    peer.getPort());

            reciever = new Reciever();
            reciever.run(app, new BufferedReader(
                    new InputStreamReader(socket.getInputStream())));

            writer = new OutputStreamWriter(socket.getOutputStream(), "UTF-8");
        } catch(IOException e) {
            System.err.println("Connection couldn't be initiated properly.");
            e.printStackTrace();
        }

        resetLastPoke();
        timer = new Timer();
        timer.run(this);
    }

    public void close() {
        reciever.terminate();
        timer.terminate();
        try{
            socket.close();
        }
        catch(IOException e) {
            System.err.println("Connection couldn't be terminated properly.");
            e.printStackTrace();
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
        }
        catch(IOException e){
            System.err.println("OutputStreamWriter could not send message.");
            e.printStackTrace();
        }
    }

    private class Reciever extends Thread {

        private boolean active;

        public Reciever() {
            active = true;
        }

        public void run(Application app, BufferedReader reader) {
            while(active) {
                try{
                    if(reader.ready()) {
                        app.recieveMessage(reader.readLine());
                    }
                }
                catch(IOException e) {
                    System.err.println("BufferedReader failed to read the sockets input stream.");
                    e.printStackTrace();
                }
            }
        }

        public void terminate() {
            active = false;
        }
    }

    private class Timer extends Thread {

        private boolean active;

        public Timer() {
            active = true;
            lastPoke = System.currentTimeMillis() / 1000L;
        }

        public void run(Connection con) {
            while(active) {
                if(con.getLastPoke() + 60 < System.currentTimeMillis()/1000L) {
                    con.setInactive();
                }
            }
        }

        public void terminate() {
            active = false;
        }
    }
}
