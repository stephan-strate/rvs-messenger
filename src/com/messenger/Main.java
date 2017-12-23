package com.messenger;

import com.messenger.console.DefaultConsole;

/**
 * <p></p>
 * @author Jan-Philip Richter
 * @author Stephan Strate
 * @version 1.0
 */
public class Main {

    public static void main (String[] args) {
        System.out.println("Hello World!");

        // starting console methods
        DefaultConsole console = new DefaultConsole();
        console.start();
    }
}