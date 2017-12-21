import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class Connection {

    private boolean active;
    private long lastPoke;

    private Peer peer;
    private Socket socket;

    private RecieverThread reciever; //wenn ein socket == nur eine verbindung, dann hier eigentlich eigener socket
    private TimerThread timer;

    public Connection(Peer peer) {
        active = true;
        this.peer = peer;
    }

    public void init(Application app) {
        try{
            socket = new Socket(peer.adress.getAddress(),
                    peer.adress.getPort());

            reciever = new RecieverThread();
            reciever.run(app, new BufferedReader(
                    new InputStreamReader(socket.getInputStream())));
        }
        catch(IOException e) {
            //TODO add proper description
            e.printStackTrace();
        }

        resetLastPoke();
        timer = new TimerThread();
        timer.run(this);
    }

    public void close() {
        reciever.terminate();
        timer.terminate();
        try{
            socket.close();
        }
        catch(IOException e) {
            System.out.println("Connection couldn't be terminated properly.");
            e.printStackTrace();
        }
    }

    public Peer getPeer() {
        return peer;
    }

    public long getLastPoke() {
        return lastPoke;
    }

    public void resetLastPoke() {
        lastPoke = System.currentTimeMillis()/1000L;
    }

    public boolean isInactive() {
        return !active;
    }

    public void setInactive() {
        active = false;
    }

    public void sendMessage(String message) {
        //TODO socket.send()
    }

    private class RecieverThread extends Thread {

        private boolean active;

        public RecieverThread() {
            active = true;
        }

        public void run(Application app, BufferedReader reader) {
            while(active) {
                try{
                    if(reader.ready()) {
                        app.recieveMessage(reader.readLine());
                    }
                }
                catch(IOException e) {
                    //TODO add proper description
                    e.printStackTrace();
                }
            }
        }

        public void terminate() {
            active = false;
        }
    }

    private class TimerThread extends Thread {

        private boolean active;

        public TimerThread() {
            active = true;
            lastPoke = System.currentTimeMillis() / 1000L;
        }

        public void run(Connection con) {
            while(active) {
                if(con.getLastPoke() + 60 < System.currentTimeMillis()/1000L) {
                    con.setInactive();
                }
            }
        }

        public void terminate() {
            active = false;
        }
    }
}
