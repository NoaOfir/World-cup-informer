package bgu.spl.net.srv;

//import bgu.spl.net.api.MessageEncoderDecoder;
//import bgu.spl.net.api.MessagingProtocol;
import bgu.spl.net.impl.stomp.ConnectionsImpl;
import bgu.spl.net.impl.stomp.StompMessagingProtocolImpl;
import bgu.spl.net.impl.stomp.stompMessagingEncoderDecoder;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.function.Supplier;

public abstract class BaseServer<T> implements Server<T> {

    private final int port;
    private final Supplier<StompMessagingProtocolImpl<T>> protocolFactory;
    private final Supplier<stompMessagingEncoderDecoder<T>> encdecFactory;
    private ServerSocket sock;
    ///we add
    private ConnectionsImpl<T> connections;

    public BaseServer(
            int port,
            Supplier<StompMessagingProtocolImpl<T>> protocolFactory,
            Supplier<stompMessagingEncoderDecoder<T>> encdecFactory) {

        this.port = port;
        this.protocolFactory = protocolFactory;
        this.encdecFactory = encdecFactory;
		this.sock = null;
        this.connections= ConnectionsImpl.getInstance();
    }

    @Override
    public void serve() {

        try (ServerSocket serverSock = new ServerSocket(port)) {
			System.out.println("Server started");

            this.sock = serverSock; //just to be able to close

            while (!Thread.currentThread().isInterrupted()) {

                Socket clientSock = serverSock.accept();
                int connectionId=connections.getAndInctId();               
    
        
                 BlockingConnectionHandler<T> handler=new BlockingConnectionHandler<>(
                        clientSock,
                        encdecFactory.get(),
                        protocolFactory.get(),
                        connections,
                        connectionId);
                        connections.connect(handler, connectionId);//add the new clinet to connections only if he didnt logged in yet

        
                execute(handler);
            }
        } catch (IOException ex) {
        }

        System.out.println("server closed!!!");
    }

    @Override
    public void close() throws IOException {
		if (sock != null)
			sock.close();
    }

    protected abstract void execute(BlockingConnectionHandler<T>  handler);

}
