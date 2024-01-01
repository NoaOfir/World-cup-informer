package bgu.spl.net.impl.stomp;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
//import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
// here we use the connection id because this is the information we have, anyway clinet can be connected with one user every time

import bgu.spl.net.impl.stomp.Frames.Errorm;
import bgu.spl.net.srv.Connections;

public class manageAllUsers {
    private CopyOnWriteArrayList<user> allUsers;
    private AtomicInteger supplyUserId;
    private ConcurrentHashMap <Integer, user> activeUserById;
    private ConcurrentHashMap<String, CopyOnWriteArrayList<Integer>>channelUsers;
    private static class SingeltoneHolder{
        private static  manageAllUsers manageInstance= new manageAllUsers();

    }
    public manageAllUsers() {
        this.allUsers=new CopyOnWriteArrayList<>();
        this.supplyUserId=new AtomicInteger(0);
        this.activeUserById= new ConcurrentHashMap<>();
        this.channelUsers= new ConcurrentHashMap<>();
    }

    public static manageAllUsers getInstance(){
        return SingeltoneHolder.manageInstance;
    }

    public String login(Connections connections,int ConnectionId,String []frameLines, String message){//we know this is a connect frame
        //save all the information
        
       String receipt=findHeader("receipt",frameLines);
    
     String accept_version=findHeader("accept-version",frameLines);
     if(!accept_version.equals("1.2")){

        
        Errorm error=new Errorm(receipt,"malformed frame received", message,"accept-version");
        error.headerError();
       connections.send(ConnectionId, error.getError());
       return"already handled";
     
        
        }
    

    String host=findHeader("host",frameLines);
     if(host.equals("")){
    
     
        
        Errorm error=new Errorm(receipt,"malformed frame received", message,"host");
        error.headerError();
       connections.send(ConnectionId, error.getError());
       return"already handled";
     
        
        }
    
    
     String userName=findHeader("login",frameLines);
     if(userName.equals("")){
    
     
        
        Errorm error=new Errorm(receipt,"malformed frame received", message,"login");
        error.headerError();
       connections.send(ConnectionId, error.getError());
       return"already handled";
     
        
        }
    
       
        String passcode=findHeader("passcode",frameLines);
     if(passcode.equals("")){
    
     
        
        Errorm error=new Errorm(receipt,"malformed frame received", message,"passcode");
        error.headerError();
       connections.send(ConnectionId, error.getError());
       return"already handled";
     
        
        }
    

       //check the conditation

      
      synchronized(allUsers){//dont want to creat the same user twice 
        for(user u: allUsers){
            if(u.getName().equals(userName)){//an existing user
                if(activeUserById.containsKey(ConnectionId))
                   return "User already logged in";
                else{//existing user- correct user name correct passcode so login successfully
                    if(u.getPasscode().equals(passcode)){
                        activeUserById.put(ConnectionId, u);
                        u.setIsConnected(true);
                        return "Login successful";
                    }
                    else{
                        return "Wrong password";
                    }
                }   
            
        }
    }
            //creat a new user
            int newUserId=supplyUserId.getAndIncrement();
            user newUser=new user(userName, passcode,newUserId);
            allUsers.add(newUserId, newUser);
            activeUserById.put(ConnectionId, newUser);
            newUser.setIsConnected(true);
             return "Login successful";
        }
   

      
      
    }


    public void subscribe(Integer connectionId,String channel, Integer subscribeId){
        if(channelUsers.containsKey(channel)){
            if(!channelUsers.get(channel).contains(connectionId)){
                channelUsers.get(channel).add(connectionId);
                activeUserById.get(connectionId).addChannel(channel, subscribeId);
            }
            
        }
        else{//create a new channel
            CopyOnWriteArrayList<Integer> newChannel= new CopyOnWriteArrayList<>();
            newChannel.add(connectionId);
            channelUsers.put(channel, newChannel);
            activeUserById.get(connectionId).addChannel(channel, subscribeId);
        }

    }

    public void unSubscribe(Integer connectionId, Integer subscribeId){
        String channel= activeUserById.get(connectionId).getMyChannelById().get(subscribeId); 
//if the channel exist
        if(channel!=null){
            channelUsers.get(channel).remove(connectionId);
            activeUserById.get(connectionId).removeChannel(subscribeId);
        }

       
    }

    public user getUserByConnectionId(Integer connectionId){
        return activeUserById.get(connectionId);
    }

    public void logoutUser(Integer connectionId){
        //remove the user from all channels
        for(CopyOnWriteArrayList<Integer> channelArray: channelUsers.values()){
            channelArray.remove(connectionId);
        }
        //if im not already logout
        if(activeUserById.get(connectionId)!=null){
        activeUserById.get(connectionId).removeAllChannel();
         activeUserById.get(connectionId).setIsConnected(false);
         activeUserById.remove(connectionId);
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
   public ConcurrentHashMap<String, CopyOnWriteArrayList<Integer>> getChannelUsers(){
    return channelUsers;
   }
   public CopyOnWriteArrayList<Integer> getChannelIds(String channel){
   return channelUsers.get(channel);
   }

   public  ConcurrentHashMap <Integer, user> getActiveUserById(){
    return   activeUserById;
   }
        







    
}
