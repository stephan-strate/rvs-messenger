package com.messenger;

/**
 * <p></p>
 */
public class Message {

    /**
     * <p>Command that is associated with
     * this message (eg. POKE).</p>
     */
    private String command;

    /**
     * <p>Peer that is connected with
     * this connection.</p>
     */
    private Peer peer;

    /**
     * <p>Contains text, when message has
     * a text.</p>
     */
    private String text;

    /**
     * <p></p>
     * @param command
     * @param ip
     * @param port
     * @param name
     * @param text
     */
    public Message (String command, String ip, int port, String name, String text) {
        this(command, new Peer(ip, port, name), text);
    }

    /**
     * <p></p>
     * @param command
     * @param ip
     * @param port
     * @param text
     */
    public Message (String command, String ip, int port, String text) {
        this(command, new Peer(ip, port), text);
    }

    /**
     * <p></p>
     * @param command
     * @param ip
     * @param port
     */
    public Message (String command, String ip, int port) {
        this(command, new Peer(ip, port), null);
    }

    /**
     * <p></p>
     * @param command
     * @param peer
     */
    public Message (String command, Peer peer) {
        this(command, peer, null);
    }

    /**
     * <p></p>
     * @param command
     * @param peer
     * @param text
     */
    public Message (String command, Peer peer, String text) {
        this.command = command;
        this.peer = peer;
        this.text = text;
    }

    /**
     * <p></p>
     * @param rawInput
     */
    public Message (String rawInput) {
        try {
            String[] input = rawInput.split(" ");

            if (input.length > 4) {
                this.command = input[0];
                this.peer = new Peer(input[1], Integer.parseInt(input[2]), input[3]);

                if (input.length > 5) {
                    // concat message
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(input[4]);
                    for (int i = 5; i < input.length; i++) {
                        stringBuilder.append(" ");
                        stringBuilder.append(input[i]);
                    }

                    this.text = stringBuilder.toString();
                }
            } else {
                throw new IllegalArgumentException("Invalid number of arguments in " +
                        "input string.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Port must be a number.");
        }
    }

    /**
     * <p></p>
     * @return
     */
    @Override
    public String toString () {
        String message = command + " " +  peer.toString();
        if (hasText()) {
            message += " " + text;
        }

        return message;
    }

    /**
     * <p></p>
     * @return
     */
    public boolean hasText () {
        return text != null;
    }

    /**
     * <p></p>
     * @return
     */
    public String getCommand () {
        return command;
    }

    /**
     * <p></p>
     * @return
     */
    public Peer getPeer () {
        return peer;
    }

    /**
     * <p></p>
     * @return
     */
    public String getHostName () {
        return peer.getHostName();
    }

    /**
     * <p></p>
     * @return
     */
    public int getPort () {
        return peer.getPort();
    }

    /**
     * <p></p>
     * @return
     */
    public String getName () {
        return peer.getName();
    }

    /**
     * <p></p>
     * @return
     */
    public String getText () {
        if (text != null) {
            return text;
        } else {
            throw new NullPointerException("This message has no text.");
        }
    }
}