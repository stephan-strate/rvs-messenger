package com.messenger;

public class Peer {

    private String ip;
    private int port;
    private String name;

    public Peer (String ip, int port) {
        this(ip, port, "");
    }

    public Peer (String ip, int port, String name) {
        this.ip = ip;
        this.port = port;
        this.name = name;
    }

    public boolean equals(Peer peer) {
        return name.equals(peer.getName())
                && getHostName().equals(peer.getHostName())
                && getPort() == peer.getPort();
    }

    public String getHostName () {
        return ip;
    }

    public int getPort () {
        return port;
    }

    public String getName () {
        return name;
    }

    public void setName (String name) {
        this.name = name;
    }
}