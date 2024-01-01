package bgu.spl.net.srv;

//import java.io.IOException;
import java.util.Map;

public interface Connections<T> {

    boolean send(int connectionId, T msg);

    void send(String channel, T msg);

    void disconnect(int connectionId);
    
    void connect(ConnectionHandler<T>handler, int connectionId);

    int getAndInctId();

    Map<Integer, ConnectionHandler<T>> getMyClientsById();
}
