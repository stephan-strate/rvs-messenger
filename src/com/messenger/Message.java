public class Message {

    private String command;
    private String name;
    private String ip;
    private int port;
    private String text;

    public Message(String command, String name, String ip, int port) {
        this.command = command;
        this.name = name;
        this.ip = ip;
        this.port = port;
    }

    public Message(String command, String name, String ip, int port, String text) {
        this(command, name, ip, port);
        this.text = text;
    }

    public Message(String rawInput) {
        String[] input = rawInput.split(" ");

        if(input.length == 4 || input.length == 5) {
            this.command = input[0];
            this.name = input[1];
            this.ip = input[2];
            this.port = Integer.parseInt(input[3]);

            if(input.length == 5) {
                this.text = text;
            }
        }
        else {
            throw new IllegalArgumentException("Invalid number of arguments in " +
                    "input string.");
        }
    }

    public String getCommand() {
        return command;
    }

    public String getName() {
        return name;
    }

    public String getIP() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public String getText() {
        if(text != null) {
            return text;
        }
        else {
            throw new NullPointerException("This message has no text.");
        }
    }

    public boolean hasText() {
        return text == null;
    }

    @Override
    public String toString() {
        String message = command + " " + name + " " + ip + " " + port;
        if(hasText()) {
            message += " " + text;
        }
        return message;
    }
}
