package com.messenger.console;

import com.messenger.Application;
import com.messenger.Connection;
import com.messenger.Message;
import com.messenger.Peer;

import java.util.Date;

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

        System.out.println(
                "* * * * * * * * * * * * * * * * * * * * * * * * * *\n" +
                "*         Welcome to the messenger console!       *\n" +
                "*      You can use 'help [command (optional)]'    *\n" +
                "* to get informations about the functions of this *\n" +
                "*     application. Type help for a command list.  *\n" +
                "* * * * * * * * * * * * * * * * * * * * * * * * * *"
        );
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

                // send a poke request to peer without adding it to peer list
                new Connection(new Peer(ip, port)).poke(application);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Error: Port must be a valid number.\n" +
                        "Example: CONNECT 127.0.0.1 6734");
            }
        } else {
            throw new IllegalArgumentException("CONNECT erwartet genau eine IP Adresse und einen Port.\n" +
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
            throw new IllegalArgumentException("Error: DISCONNECT does not expect arguments.");
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
            throw new IllegalArgumentException("Error: EXIT does not expect arguments.");
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
            System.out.println("> [" + new Date().toString() + "] You: " + message);
        } else {
            throw new IllegalArgumentException("M does expect a name and a message.\n" +
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
                System.out.println("> [" + new Date().toString() + "] You: " + message);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Error: Port must be a valid number.");
            }
        } else {
            throw new IllegalArgumentException("Error: MX expects an ip address, a port and a message.\n" +
                    "Example: MX 127.0.0.1 6734 Hello World!");
        }
    }

    /**
     * <p>Documentation method with hopefully helping tips,
     * to work with this application.</p>
     * @param args  expecting HELP [optional (String) method]
     */
    @Method
    protected void help (String[] args) {
        if (args.length == 0) {
            // printing command list
            System.out.println(
                    "Commands:\n" +
                            "* CONNECT <IP> <Port> - used to connect with a peer\n" +
                            "* DISCONNECT - used to disconnect from the peer-to-peer network\n" +
                            "* EXIT - used to disconnect from peer-to-peer network and exit the application\n" +
                            "* M <Name> <Text> - used to send messages to all peers with name\n" +
                            "* MX <IP> <Port> <Text> - used to send message to the peer with ip and port"
            );
        } else if (args.length == 1 && args[0] != null) {
            // printing detailed informations to each command
            switch (args[0].toUpperCase()) {
                case "CONNECT": {
                    System.out.println(
                            "Usage: CONNECT <IP> <Port>\n" +
                            "CONNECT is used to connect to a peer that is logged in to\n" +
                            "the peer-to-peer network. You send a poke to him and when he is online,\n" +
                            "he will send back a poke, so you know he is online.\n" +
                            "To make the CONNECT command work, you need to parse a valid ip address and\n" +
                            "a valid port.\n" +
                            "Example: CONNECT 232.233.77.12 5375"
                    );
                    break;
                }

                case "DISCONNECT": {
                    System.out.println(
                            "Usage: DISCONNECT\n" +
                            "DISCONNECT needs no parameters and signals all your connected\n" +
                            "peers, that you are about to disconnect from the peer-to-peer network."
                    );
                    break;
                }

                case "EXIT": {
                    System.out.println(
                            "Usage: EXIT\n" +
                            "EXIT is similar to DISCONNECT, but closes the application."
                    );
                    break;
                }

                case "M": {
                    System.out.println(
                            "Usage: M <Name> <Text>\n" +
                            "M used to send messages to all connected peers with the name you\n" +
                            "parsed to the method.\n" +
                            "Example: M Tim Hello World!"
                    );
                    break;
                }

                case "MX": {
                    System.out.println(
                            "Usage: MX <IP> <Port> <Text>\n" +
                            "MX used to send a message to a specific connected peer. Peer is defined by\n" +
                            "a ip address and port.\n" +
                            "Example: MX 232.233.77.12 5375 Hello World!"
                    );
                    break;
                }

                default: {
                    System.out.println("Command " + args[0].toUpperCase() + " not found.");
                }
            }
        } else {
            throw new IllegalArgumentException("Error: HELP expects no or one argument.");
        }
    }
}