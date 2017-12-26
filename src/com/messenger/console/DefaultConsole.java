package com.messenger.console;

import com.messenger.Application;
import com.messenger.Connection;
import com.messenger.Message;
import com.messenger.Peer;

/**
 * <p>Default console for rvs-messenger application.
 * We define the text interface methods right here, you
 * can use the following methods directly in the
 * unix/windows console: CONNECT, DISCONNECT, EXIT, M, MX.</p>
 */
public class DefaultConsole extends Console {

    /**
     * <p>Current application, the console
     * runs in.</p>
     */
    private Application application;

    /**
     * <p>Get all methods by calling the super class
     * and assigning the current application running, used
     * to send messages, connect to peers and poke them.</p>
     * @param application   current message application
     */
    public DefaultConsole (Application application) {
        // default functionality
        super();

        // assigning current application
        this.application = application;
    }

    /**
     * <p>Sends a POKE message with own name/ip/port to
     * the given peer defined by ip address and port.</p>
     * @param args  expecting CONNECT (String) IP, (int) Port
     */
    @Method
    protected void connect (String[] args) {
        if (args.length == 2 && args[0] != null && args[1] != null) {
            try {
                String ip = args[0];
                int port = Integer.parseInt(args[1]);

                // send a poke request to peer
                new Connection(new Peer(ip, port)).poke(application);
            } catch (NumberFormatException e) {
                System.out.println("Error: Port must be a valid number.\n" +
                        "Example: CONNECT 127.0.0.1 6734");
            }
        } else {
            System.out.println("CONNECT erwartet genau eine IP Adresse und einen Port.\n" +
                    "Beispiel: CONNECT 127.0.0.1 6734");
        }
    }

    /**
     * <p>Sends a DISCONNECT message to all active peers (peer list)
     * and clear the peer list.</p>
     * @param args  expecting DISCONNECT null
     */
    @Method
    protected void disconnect (String[] args) {
        // check if no more parameters are given
        if (args.length == 0 || args[0] == null) {
            // sending DISCONNECT message
            application.exit();
        } else {
            System.err.println("Error: DISCONNECT does not expect arguments.");
        }
    }

    /**
     * <p>Sends a DISCONNECT message to all active peers (peer list),
     * clears the peer list and closes the client.</p>
     * @param args  expecting EXIT null
     */
    @Method
    public void exit (String[] args) {
        // check if no more parameters are given
        if (args.length == 0 || args[0] == null) {
            // sending DISCONNECT message
            disconnect(args);

            // close client
            System.out.println("Closing messenger.");
            System.exit(0);
        } else {
            System.err.println("Error: EXIT does not expect arguments.");
        }
    }

    /**
     * <p>Sends a MESSAGE to all known peers that are
     * associated with the given name.</p>
     * @param args  expecting M (String) Name, (String) Text
     */
    @Method
    protected void m (String[] args) {
        // check if all parameters are given
        if (args.length > 1 && args[0] != null && args[1] != null) {
            String name = args[0];

            // concat message
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(args[1]);
            for (int i = 2; i < args.length; i++) {
                stringBuilder.append(" ");
                stringBuilder.append(args[i]);
            }

            String message = stringBuilder.toString();

            // preparing message and sending it to all name
            application.sendMessagesByName(name, new Message("MESSAGE", application.me, message));
        } else {
            System.err.println("M does expect a name and a message.\n" +
                    "Example: M Jon Hello World!");
        }
    }

    /**
     * <p>Sends a MESSAGE to the peer that is associated with
     * given ip address and port.</p>
     * @param args  expecting MX (String) IP, (int) Port, (String) Text
     */
    @Method
    protected void mx (String[] args) {
        // check if all parameters are given
        if (args.length > 2 && args[0] != null && args[1] != null && args[2] != null) {
            try {
                String ip = args[0];
                int port = Integer.parseInt(args[1]);

                // concat message
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append(args[2]);
                for (int i = 3; i < args.length; i++) {
                    stringBuilder.append(" ");
                    stringBuilder.append(args[i]);
                }

                String message = stringBuilder.toString();

                // preparing message and sending it to peer
                application.sendMessage(new Peer(ip, port), new Message("MESSAGE", application.me, message));
            } catch (NumberFormatException e) {
                System.err.println("Error: Port must be a valid number.");
            }
        } else {
            System.err.println("Error: MX expects an ip address, a port and a message.\n" +
                    "Example: MX 127.0.0.1 6734 Hello World!");
        }
    }

    @Method
    protected void print (String[] args) {
        System.out.println("Printing all connections:");
        for (Connection c : application.getConnections()) {
            System.out.println(c.getPeer().toString());
        }
    }
}