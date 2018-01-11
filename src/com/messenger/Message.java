package com.messenger;

/**
 * <p>Represents a message that can be send as a
 * string (for example by sockets) and parsed into
 * an object again. It contains a type (represented by
 * command), a peer (mostly where its coming from) and
 * eventually a text.</p>
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
     * <p>Creates a message with command, ip, port, name
     * and text.</p>
     * @param command   command
     * @param ip        ip address
     * @param port      port
     * @param name      client name
     * @param text      text
     */
    public Message (String command, String ip, int port, String name, String text) {
        this(command, new Peer(ip, port, name), text);
    }

    /**
     * <p>Creates a message with command, ip, port and text.
     * Client name is missing in this constructor.</p>
     * @param command   command
     * @param ip        ip address
     * @param port      port
     * @param text      text
     */
    public Message (String command, String ip, int port, String text) {
        this(command, new Peer(ip, port), text);
    }

    /**
     * <p>Creates a message with command, ip and port.
     * Client name and text is missing in this constructor.</p>
     * @param command   command
     * @param ip        ip address
     * @param port      port
     */
    public Message (String command, String ip, int port) {
        this(command, new Peer(ip, port), null);
    }

    /**
     * <p>Creates a message with command and peer.
     * Text is missing in this constructor.</p>
     * @param command   command
     * @param peer      peer (ip, port, name)
     */
    public Message (String command, Peer peer) {
        this(command, peer, null);
    }

    /**
     * <p>Creates a message with command, peer and
     * text.</p>
     * @param command   command
     * @param peer      peer (ip, port, name)
     * @param text      text
     */
    public Message (String command, Peer peer, String text) {
        this.command = command;
        this.peer = peer;
        this.text = text;
    }

    /**
     * <p>Creates a message out of a raw message, that
     * was created by {@link Message#toString()} before.</p>
     * @param rawInput  raw input of {@link Message#toString()}
     */
    public Message (String rawInput) {
        try {
            // splitting string at whitespace
            String[] input = rawInput.split(" ");

            if (input.length > 3) {
                this.command = input[0];
                this.peer = new Peer(input[2], Integer.parseInt(input[3]), input[1]);

                if (input.length > 5) {
                    // concat message
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(input[4]);
                    for (int i = 5; i < input.length; i++) {
                        stringBuilder.append(" ");
                        stringBuilder.append(input[i]);
                    }

                    this.text = stringBuilder.toString();
                } else if (input.length > 4) {
                    this.text = input[4];
                }
            } else {
                System.err.println("Error: Invalid number of arguments in " +
                        "input string.");
            }
        } catch (NumberFormatException e) {
            System.err.println("Error: Port must be a valid number.");
        }
    }

    /**
     * <p>Concat all attributes to one single string.
     * Defined convention, to parse string back with
     * {@link Message#Message(String)}.</p>
     * @return  all attributes
     */
    @Override
    public String toString () {
        // concat command and peer
        String message = command.toUpperCase() + " " +  peer.toString();
        if (hasText()) {
            // add text, when available
            message += " " + text;
        }

        return message;
    }

    /**
     * <p>Checking if {@link Message} has a
     * text available.</p>
     * @return  is {@link Message#text} not null
     */
    public boolean hasText () {
        return text != null;
    }

    /**
     * <p>Gets {@link Message#command}.</p>
     * @return  {@link Message#command}
     */
    public String getCommand () {
        return command;
    }

    /**
     * <p>Gets {@link Message#peer}.</p>
     * @return  {@link Message#peer}
     */
    public Peer getPeer () {
        return peer;
    }

    /**
     * <p>Gets {@link Message#text} when it is not
     * null.</p>
     * @return  {@link Message#text}
     */
    public String getText () {
        if (text != null) {
            return text;
        }

        return "";
    }
}