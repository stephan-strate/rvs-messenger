package com.messenger.console;

/**
 * <p>Default console for rvs-messenger application.
 * We define the text interface methods right here, you
 * can use the following methods directly in the
 * unix/windows console: CONNECT, DISCONNECT, EXIT, M, MX.</p>
 */
public class DefaultConsole extends Console {

    /**
     * <p>Sends a POKE message with own name/ip/port to
     * the given peer defined by ip address and port.</p>
     * @param args  expecting CONNECT (String) IP, (int) Port
     */
    @Method
    protected void connect (String[] args) {

    }

    /**
     * <p>Sends a DISCONNECT message to all active peers (peer list)
     * and clear the peer list.</p>
     * @param args  expecting DISCONNECT null
     */
    @Method
    protected void disconnect (String[] args) {

    }

    /**
     * <p>Sends a DISCONNECT message to all active peers (peer list),
     * clears the peer list and closes the client.</p>
     * @param args  expecting EXIT null
     */
    @Method
    public void exit (String[] args) {
        // sending DISCONNECT message
        disconnect(args);

        // close client
        System.exit(0);
    }

    /**
     * <p>Sends a MESSAGE to all known peers that are
     * associated with the given name.</p>
     * @param args  expecting M (String) Name, (String) Text
     */
    @Method
    protected void m (String[] args) {

    }

    /**
     * <p>Sends a MESSAGE to the peer that is associated with
     * given ip address and port.</p>
     * @param args  expecting MX (String) IP, (int) Port, (String) Text
     */
    @Method
    protected void mx (String[] args) {

    }
}