package com.messenger.console;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * <p>Console class that's make it possible to create
 * simple, but powerful console applications.
 * Just extend from it and use {@link Method} annotations
 * to create methods for the console.</p>
 */
abstract class Console {

    private boolean active = false;
    private ArrayList<Method> methods = new ArrayList<>();

    /**
     * <p>Create a new console.</p>
     */
    Console () {
        for (Method method : getClass().getDeclaredMethods()) {
            if (method.isAnnotationPresent(com.messenger.console.Method.class)) {
                // get all methods that have the annotation
                methods.add(method);
            }
        }
    }

    /**
     * <p>Logical part of console application.
     * Reading new lines and invoking the correct methods.</p>
     */
    private void process () {
        // init reader
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while (active) {
            try {
                // read line
                String command = br.readLine();
                // split inputs from eg. "update database test" to
                // ["update", "database", "test"]
                String[] parts = command.split(" ");
                // if command is available
                if (parts.length > 0) {
                    // get command/endpoint
                    String endpoint = parts[0].toLowerCase();
                    // get the attributes
                    String[] args = Arrays.copyOfRange(parts, 1, parts.length);

                    // iterate all methods
                    for (Method method : methods) {
                        // check if any method has the name of endpoint
                        if (method.getName().equals(endpoint)) {
                            // prepare arguments for method
                            Object[] params = new Object[1];
                            params[0] = args;
                            // invoke method
                            method.invoke(this, params);
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println("Error. Input/Output error.");
            } catch (IllegalAccessException e) {
                System.out.println("Internal error. No permission to call method.");
            } catch (InvocationTargetException e) {
                System.out.println("Can not execute console method (wrong arguments).");
                e.printStackTrace();
            }
        }
    }

    /**
     * <p>Start the console application.</p>
     */
    public void start () {
        active = true;
        process();
    }

    /**
     * <p>Get active attribute.</p>
     * @return  {@link Console#active}
     */
    public boolean getActive () {
        return active;
    }

    /**
     * <p>Default exit method for user. Can be
     * overwritten.</p>
     * @param args  can be null
     */
    @com.messenger.console.Method
    public void exit (String[] args) {
        active = false;
    }
}