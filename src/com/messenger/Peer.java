import java.net.InetSocketAddress;

public class Peer {

    public final InetSocketAddress adress;
    public final String name;

    public Peer(InetSocketAddress adress, String name) {
        this.adress = adress;
        this.name = name;
    }
}
