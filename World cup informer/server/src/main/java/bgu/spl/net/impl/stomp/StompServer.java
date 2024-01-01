package bgu.spl.net.impl.stomp;
import bgu.spl.net.srv.Server;






public class StompServer {

    public static void main(String[] args) {
        // TODO: implement this
        int port=Integer.parseInt(args[0]);
        String serverSupport= args[1];
        if(serverSupport.equals("tpc")){
        
        Server.threadPerClient(
                port, //port
                () -> new StompMessagingProtocolImpl(), //protocol factory
               ()-> new stompMessagingEncoderDecoder()
                //stomp encoder decoder factory
        ).serve();
        }
        else if(serverSupport.equals("reactor")){

        Server.reactor(
                Runtime.getRuntime().availableProcessors(),//num of threads
                port, //port
               () -> new StompMessagingProtocolImpl(), //protocol factory
                 stompMessagingEncoderDecoder::new //stomp encoder decoder factory
        ).serve();
        }
    }
    }
