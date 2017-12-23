package com.messenger;

import java.net.InetSocketAddress;

public class Peer {

    private final InetSocketAddress address;
    private String name;

    public Peer (String ip, int port) {
        this(new InetSocketAddress(ip, port));
    }

    public Peer (String ip, int port, String name) {
        this(new InetSocketAddress(ip, port), name);
    }

    public Peer (InetSocketAddress address) {
        this.address = address;
        this.name = "";
    }

    public Peer (InetSocketAddress address, String name) {
        this.address = address;
        this.name = name;
    }

    public boolean equals(Peer peer) {
        return name.equals(peer.getName())
                && getHostName().equals(peer.getHostName())
                && getPort() == peer.getPort();
    }

    public InetSocketAddress getAddress () {
        return address;
    }

    public String getHostName () {
        return address.getHostName();
    }

    public int getPort () {
        return address.getPort();
    }

    public String getName () {
        return name;
    }

    public void setName (String name) {
        this.name = name;
    }
}