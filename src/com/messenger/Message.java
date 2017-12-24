package com.messenger;

public class Message {

    private String command;
    private String ip;
    private int port;
    private String name;
    private String text;

    public Message (String command, String ip, int port, String name) {
        this.command = command;
        this.ip = ip;
        this.port = port;
        this.name = name;
    }

    public Message (String command, String ip, int port, String text, String name) {
        this(command, ip, port, name);
        this.text = text;
    }

    public Message (String rawInput) {
        String[] input = rawInput.split(" ");

        if (input.length == 4 || input.length == 5) {
            this.command = input[0];
            this.ip = input[1];
            this.port = Integer.parseInt(input[2]);
            this.name = input[3];

            if (input.length == 5) {
                this.text = input[4];
            }
        } else {
            throw new IllegalArgumentException("Invalid number of arguments in " +
                    "input string.");
        }
    }

    public String getCommand () {
        return command;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public String getName() {
        return name;
    }

    public String getText () {
        if (text != null) {
            return text;
        } else {
            throw new NullPointerException("This message has no text.");
        }
    }

    public boolean hasText() {
        return text != null;
    }

    @Override
    public String toString() {
        String message = command + " " +  ip + " " + port + " " + name;
        if (hasText()) {
            message += " " + text;
        }
        return message;
    }
}
