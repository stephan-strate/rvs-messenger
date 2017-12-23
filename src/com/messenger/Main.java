package com.messenger;

/**
 * <p>Messenger application.</p>
 * @author Jan-Philip Richter
 * @author Stephan Strate
 * @version 1.0
 */
public class Main {

    /**
     * <p>Read input parameters and start peer/
     * messenger from it. Expected parameters are
     * (String) name and (int) port.
     * All other combinations of parameters will
     * cause the program to exit with errors.</p>
     * @param args  expecting messenger.jar (String) name, (int) port
     */
    public static void main (String[] args) {
        // check if all parameters are given
        if (args.length == 2 && args[0] != null && args[1] != null) {
            try {
                // store name
                String name = args[0];
                // parse port to int
                int port = Integer.parseInt(args[1]);

                // start application/program
                System.out.println("Messenger/Peer wird gestartet.");
                Application application = new Application(name, port);
            } catch (NumberFormatException e) {
                System.out.println("Der Port muss eine valide Nummer sein.\n" +
                        "Beispiel: messenger.jar Jon 6734");

                // terminate application with errors
                System.exit(1);
            }
        } else {
            System.out.println("Es muss genau ein Name und ein valider Port übergebn werden.\n" +
                    "Beispiel: messenger.jar Jon 6734");

            // terminate application with errors
            System.exit(1);
        }
    }
}