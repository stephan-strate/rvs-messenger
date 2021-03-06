package com.messenger;

/**
 * <p>Represents a peer in our peer-to-peer network.
 * It contains an ip address, a associated port and
 * a name. You can create a peer without a name aswell.</p>
 */
public class Peer {

    /**
     * <p>Ip address for this peer.</p>
     */
    private String ip;

    /**
     * <p>Port for this peer.</p>
     */
    private int port;

    /**
     * <p>Name of this peer.</p>
     */
    private String name;

    /**
     * <p>Creates a new peer without knowing the name,
     * because sometimes you only know ip and port of a
     * peer.</p>
     * @param ip    ip address
     * @param port  port
     */
    public Peer (String ip, int port) {
        this(ip, port, null);
    }

    /**
     * <p>Creates a peer with ip, port and name.</p>
     * @param ip    ip address
     * @param port  port
     * @param name  peer name
     */
    public Peer (String ip, int port, String name) {
        this.ip = ip;
        this.port = port;
        this.name = name;
    }

    /**
     * <p>Compares two peers with each other. If
     * name, hostname and port are equal, the function
     * returns {@code true}.</p>
     * @param peer  peer to compare with
     * @return  is peer equal
     */
    public boolean equals (Peer peer) {
        return getHostName().equals(peer.getHostName())
                && getPort() == peer.getPort();
    }

    /**
     * <p>toString method for {@link Peer}. Concatenates
     * all attributes to print it.</p>
     * @return  concatenation of all attributes
     */
    @Override
    public String toString () {
        return (hasName() ? name + " " : "") + ip + " " + port;
    }

    /**
     * <p>Checking if this peer has a name assigned,
     * returns {@code true} if so.</p>
     * @return  has peer name
     */
    public boolean hasName () {
        return name != null;
    }

    /**
     * <p>Gets {@link Peer#ip}.</p>
     * @return  {@link Peer#ip}
     */
    public String getHostName () {
        return ip;
    }

    /**
     * <p>Gets {@link Peer#port}.</p>
     * @return  {@link Peer#port}
     */
    public int getPort () {
        return port;
    }

    /**
     * <p>Gets {@link Peer#name}.</p>
     * @return  {@link Peer#name}
     */
    public String getName () {
        if (name != null) {
            return name;
        }

        return "";
    }

    /**
     * <p>Sets {@link Peer#name}.</p>
     * @param name  client name to set
     */
    public void setName (String name) {
        this.name = name;
    }
}