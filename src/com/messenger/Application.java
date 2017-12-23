package com.messenger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayDeque;

public class Application {

    private Console console;

    private Server server;
    private Timer timer;

    private ArrayDeque<Connection> connections;
    private boolean mutex;

    public Application(String name, int port) {
        console = new Console(this);

        connections = new ArrayDeque<>();
        mutex = false;

        server = new Server();
        timer = new Timer();

        server.run(this, port);
        timer.run(this);
    }

    public ArrayDeque<Connection> getConnections() {
        return connections;
    }

    public synchronized void addConnection(Connection c) {
        c.init(this);

        while(mutex) {}
        mutex = true;
        connections.add(c);
        mutex = false;
    }

    public synchronized void removeConnection(Connection c) {
        c.close();

        while(mutex) {}
        mutex = true;
        connections.remove(c);
        mutex = false;
    }

    public void recieveMessage(String input) {
        Message message = new Message(input);

        Peer sender = new Peer(
                new InetSocketAddress(message.getIP(), message.getPort()),
                message.getName());

        switch (message.getCommand()) {

            case "poke":

                while(mutex) {}
                mutex = true;

                for(Connection c : connections) {
                    if(c.getPeer().equals(sender)) {
                        c.resetLastPoke();
                    }
                }

                mutex = false;
                break;

            case "disconnect":

                String command = "disconnect";
                String name = sender.name;
                String ip = sender.adress.getHostName();
                int port = sender.adress.getPort();

                Message disconnect = new Message(command, name, ip, port);
                Connection toRemove = null;

                while(mutex) {}
                mutex = true;

                for(Connection c : connections) {
                    mutex = false;
                    sendMessage(c.getPeer(), disconnect);
                    mutex = true;

                    if(c.getPeer().equals(sender)) {
                        toRemove = c;
                    }
                }

                mutex = false;

                if(toRemove != null) {
                    removeConnection(toRemove);
                }
                break;

            case "message":

                //console print message.toString()
                break;

            default:
                throw new IllegalStateException("Valid command expected, but "
                        +message.getCommand()+ " found instead.");
        }
    }

    public void sendMessage(Peer peer, Message message) {
        while(mutex) {}
        mutex = true;

        for(Connection c : connections) {
            if(c.getPeer() == peer) {
                c.sendMessage(message);
                mutex = false;
                return;
            }
        }

        mutex = false;
        throw new IllegalArgumentException("Valid adress expected. " +
                "The client you tried to message may have gone offline.");
    }

    public void exit() {
        server.terminate();
        timer.terminate();

        while(mutex) {}
        mutex = true;

        for(Connection c : connections) {
            String command = "disconnect";
            String name = c.getPeer().name;
            String ip = c.getPeer().adress.getHostName();
            int port = c.getPeer().adress.getPort();

            Message disconnect = new Message(command, name, ip, port);

            mutex = false;
            c.sendMessage(disconnect);
            mutex = true;

            c.close();
        }

        mutex = false;
    }

    private class Server extends Thread {

        private boolean active;

        public Server() {
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
                System.err.println("Failed to initalize server socket.");
                e.printStackTrace();
            }
        }

        public void terminate() {
            active = false;
        }
    }

    private class Timer extends Thread {

        private boolean active;
        private long timer;

        public Timer() {
            active = true;
        }

        public void run(Application app) {
            timer = System.currentTimeMillis()/1000L;

            while(active) {
                if(timer + 30 < System.currentTimeMillis()/1000L) {
                    ArrayDeque<Connection> buffer = new ArrayDeque<>();

                    while(mutex) {}
                    mutex = true;

                    for(Connection c : app.getConnections()) {
                        if(c.isInactive()) {
                            buffer.add(c);
                            continue;
                        }

                        String command = "poke";
                        String name = c.getPeer().name;
                        String ip = c.getPeer().adress.getHostName();
                        int port = c.getPeer().adress.getPort();

                        Message message = new Message(command, name, ip, port);

                        mutex = false;
                        c.sendMessage(message);
                        mutex = true;
                    }

                    app.getConnections().removeAll(buffer);
                    timer = System.currentTimeMillis()/1000L;
                }

                mutex = false;
            }
        }

        public void terminate() {
            active = false;
        }
    }
}
