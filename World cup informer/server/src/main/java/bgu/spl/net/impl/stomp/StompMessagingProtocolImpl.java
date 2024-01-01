package bgu.spl.net.impl.stomp;

import java.util.concurrent.atomic.AtomicInteger;

import bgu.spl.net.api.StompMessagingProtocol;
import bgu.spl.net.impl.stomp.Frames.Errorm;
import bgu.spl.net.srv.Connections;

public class StompMessagingProtocolImpl<T> implements StompMessagingProtocol<String>{
   private int ConnectionId;
   private ConnectionsImpl <String> connections;
   private volatile boolean shouldTerminate= false;
 
   private AtomicInteger messageId=new AtomicInteger(0);//static,messagecounter//a Uniqe ID for each message

   public StompMessagingProtocolImpl(){}//an empty contructer
   
   
   public void start(int ConnectionId,Connections connections ){
    this.ConnectionId=ConnectionId;
    this.connections=(ConnectionsImpl)connections;

   }
   public void process(String message){
      String[]frameLines=message.split("\n");
      String type=frameLines[0];
               
	//go to the relevant command	

   if(type.equals("CONNECT")){
   connectFrame(frameLines,ConnectionId,message);
   return;
      }   
      if(manageAllUsers.getInstance().getActiveUserById().containsKey(ConnectionId)){
   if(type.equals("SUBSCRIBE")){
     subscribeFrame(frameLines, ConnectionId, message);
     return;
   }

if(type.equals("UNSUBSCRIBE")){
     unSubscribeFrame(frameLines, ConnectionId, message);
     return;
}
if(type.equals("DISCONNECT")){
   disconnectFrame(frameLines, ConnectionId, message);
   return;
        
   }
   if(type.equals("SEND"))
   {
      
     sendFrame(frameLines, ConnectionId, message);
   }
   else{
       Errorm error=new Errorm("","Wrong frame", message,"undefine command");
        error.regError();
       connections.send(ConnectionId, error.getError());
        closeConnection(ConnectionId);

   }
      }
}
private void connectFrame(String[]frameLines, int ConnectionId, String message){
   String receipt=findHeader("receipt", frameLines);
   String ans=manageAllUsers.getInstance().login(connections,ConnectionId,frameLines,message);
 
    if(ans.equals("already handled")){//mean that we send an header error
      closeConnection(ConnectionId);
      return;
    }
    if(ans.equals("Wrong password")){
      Errorm error=new Errorm(receipt,"Wrong password", message,"The password doesn't match the saved password");
        error.regError();
       connections.send(ConnectionId, error.getError());
        closeConnection(ConnectionId);
    }
    else if(ans.equals("User already logged in")){
      Errorm error=new Errorm(receipt,"User already logged in", message,"User already logged in");
        error.regError();
       connections.send(ConnectionId, error.getError());
       closeConnection(ConnectionId);
    }
    else if(ans.equals("Login successful")){
       
      String connectedFrame="CONNECTED\n"+
      "version:1.2"+"\n\n"+
      "\u0000";
    
      connections.send(ConnectionId, connectedFrame);
       if(!receipt.equals("")){
      String receiptFrame="RECEIPT\n"+
      "receipt-id: "+receipt+"\n\n"+
      "\u0000";

      connections.send(ConnectionId, receiptFrame);
      }
 
    

      }
}
private void subscribeFrame(String[]frameLines,int ConnectionId,String message){
   String receipt=findHeader("receipt",frameLines);
   if(receipt.equals("")){
         Errorm error=new Errorm(receipt,"malformed frame received", message,"receipt");
        error.headerError();
       connections.send(ConnectionId, error.getError());
       closeConnection(ConnectionId);
       return;
   }
   

   user u=manageAllUsers.getInstance().getUserByConnectionId(ConnectionId);
      if(!u.getIsConnectes()){
         Errorm error=new Errorm(receipt,"Didn't logged in", message,"User have to log in before subscribe");
        error.regError();
       connections.send(ConnectionId, error.getError());
        closeConnection(ConnectionId);

      }
      else{
      String destination=findHeader("destination", frameLines);

      if(destination.equals("")){
          Errorm error=new Errorm(receipt,"malformed frame received", message,"destination");
        error.headerError();
       connections.send(ConnectionId, error.getError());
       closeConnection(ConnectionId);
       return;
      }
      String tempsSubscribeId=findHeader("id",frameLines);
      if(tempsSubscribeId.equals("")){
         Errorm error=new Errorm(receipt,"malformed frame received", message,"id");
        error.headerError();
       connections.send(ConnectionId, error.getError());
        closeConnection(ConnectionId);
       return;

      }
      int subscribeId=Integer.parseInt(tempsSubscribeId);

      manageAllUsers.getInstance().subscribe(ConnectionId, destination, subscribeId);
      if(!receipt.equals("")){
      String receiptFrame="RECEIPT\n"+
      "receipt-id: "+receipt+"\n\n"+
      "\u0000";

      connections.send(ConnectionId, receiptFrame);
      }
   }
 
      

}

private void unSubscribeFrame(String[]frameLines,int ConnectionId,String message){
   String receipt=findHeader("receipt",frameLines);
   if(receipt.equals("")){
         Errorm error=new Errorm(receipt,"malformed frame received", message,"receipt");
        error.headerError();
       connections.send(ConnectionId, error.getError());
       closeConnection(ConnectionId);
       return;
   }
   
   user u=manageAllUsers.getInstance().getUserByConnectionId(ConnectionId);
      if(!u.getIsConnectes()){
         Errorm error=new Errorm(receipt,"Didn't logged in", message,"User have to log in before unsubscribe");
        error.regError();
       connections.send(ConnectionId, error.getError());
        closeConnection(ConnectionId);
    

      }
      
      else{
      String tempsSubscribeId=findHeader("id",frameLines);
      if(tempsSubscribeId.equals("")){
         Errorm error=new Errorm(receipt,"malformed frame received", message,"id");
        error.headerError();
       connections.send(ConnectionId, error.getError());
       closeConnection(ConnectionId);
       
       return;

      }
      int subscribeId=Integer.parseInt(tempsSubscribeId);
         
      if(!u.getMyChannelById().containsKey(subscribeId)){
           Errorm error=new Errorm(receipt,"cannot unsubscribe from this channel", message,"User have to subscribe before unsubscribe");
        error.regError();
       connections.send(ConnectionId, error.getError());
        closeConnection(ConnectionId);
        return;
    


      }
      
     if(manageAllUsers.getInstance().getActiveUserById().get(ConnectionId).getMyChannelById().containsKey(subscribeId)){
      manageAllUsers.getInstance().unSubscribe(ConnectionId, subscribeId);
      if(!receipt.equals("")){
      String receiptFrame="RECEIPT\n"+
      "receipt-id: "+receipt+"\n\n"+
      "\u0000";

      connections.send(ConnectionId, receiptFrame);
      }
   }
 
        }
}


private void disconnectFrame(String[]frameLines,int ConnectionId,String message){
   String receipt=findHeader("receipt",frameLines);
   if(receipt.equals("")){
         Errorm error=new Errorm(receipt,"malformed frame received", message,"receipt");
        error.headerError();
       connections.send(ConnectionId, error.getError());
       closeConnection(ConnectionId);
       return;
   }

   manageAllUsers.getInstance().logoutUser(ConnectionId);

   String receiptFrame="RECEIPT\n"+
      "receipt-id:"+receipt+"\n\n"+
      "\u0000";

      connections.send(ConnectionId, receiptFrame);
      shouldTerminate=true;
      connections.disconnect(ConnectionId);
        
}

private void sendFrame(String[]frameLines,int ConnectionId,String message){
      if(manageAllUsers.getInstance().getActiveUserById().containsKey(ConnectionId)){
    String receipt=findHeader("receipt",frameLines);
   String channel=findHeader("destination",frameLines);
   if(channel.equals("")){
   Errorm error=new Errorm(receipt,"malformed frame received", message,"destination");
        error.headerError();
       connections.send(ConnectionId, error.getError());
       closeConnection(ConnectionId);
       return;
       }
      
       if(!manageAllUsers.getInstance().getChannelUsers().containsKey(channel)||!manageAllUsers.getInstance().getChannelIds(channel).contains(ConnectionId)){
         Errorm error=new Errorm(receipt,"The client isn't subscribe to this channel", message,"The clinet have to subscribes to the channel before sending message");
         error.regError();
         connections.send(ConnectionId, error.getError());
         closeConnection(ConnectionId);
       return;

       }
   String userName=findHeader("user", frameLines);
   String thisMsgId=userName+"-"+messageId.getAndIncrement();
   String messageFrame="MESSAGE\n"+
   "subscription:@"+"\n"+
   "message-id:"+thisMsgId+"\n"+
   "destination:"+channel+"\n\n"+
   message+"\n"+
   "\u0000";
//send to all users that subscribe to the channel
   if(manageAllUsers.getInstance().getChannelIds(channel)!=null){//checks if users subscribe to this channel
            for(Integer id: manageAllUsers.getInstance().getChannelIds(channel)){
                connections.mapRWLock.readLock().lock();
                if(connections.getMyClientsById().containsKey(id))//check if this is an active client
                {
                  String[] help=messageFrame.split("@");
             
                  String newMessage=help[0]+id+help[1];
                 
                connections.getMyClientsById().get(id).send( newMessage);}
                connections.mapRWLock.readLock().unlock();

}
   }
}
}
private String findHeader (String toFind, String []frameLines){
   for(String s:frameLines){
      String[] line= s.split(":");
      if(line[0].equals(toFind)){
         return line[1];
      }
   }
   return "";
   
        
   }
   private void closeConnection(int ConnectionId){
  manageAllUsers.getInstance().logoutUser(ConnectionId);
  shouldTerminate=true;

}
  
   
public void setShouldTerminate(boolean newVal){
shouldTerminate=newVal;
}
  
   

   @Override
   public boolean shouldTerminate() {
     
      return shouldTerminate;
   } 

    
}
