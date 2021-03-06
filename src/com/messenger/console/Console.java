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

    /**
     * <p>Status of console. Can be terminated
     * by using {@link Console#exit(String[])}.</p>
     */
    private boolean active = false;

    /**
     * <p>Used to store all fetched methods with
     * {@link com.messenger.console.Method} annotation.</p>
     */
    private ArrayList<Method> methods = new ArrayList<>();

    /**
     * <p>Create a new console. Fetches all methods with
     * {@link Method} annotation and adds them to a list.
     * Methods will be invoked from this list later.</p>
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
     * Reading new lines, invoking the correct methods and
     * parsing the correct arguments.</p>
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
                    boolean methodFound = false;
                    for (Method method : methods) {
                        // check if any method has the name of endpoint
                        if (method.getName().equals(endpoint)) {
                            // prepare arguments for method
                            Object[] params = new Object[1];
                            params[0] = args;
                            // invoke method
                            method.invoke(this, params);
                            methodFound = true;
                        }
                    }

                    // when no method is found
                    if (!methodFound) {
                        System.err.println("Error: Method not found.");
                    }
                }
            } catch (IOException e) {
                System.err.println("Error: Input/Output error.");
            } catch (IllegalAccessException e) {
                System.err.println("Internal Error: No permission to call method.");
            } catch (InvocationTargetException e) {
                System.err.println("Error: Console method threw error.");
            }
        }
    }

    /**
     * <p>Start the console application by setting
     * {@link Console#active} to {@code true} and invoking
     * {@link Console#process()}.</p>
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