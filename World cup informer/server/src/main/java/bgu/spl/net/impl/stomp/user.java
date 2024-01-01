package bgu.spl.net.impl.stomp;


import java.util.concurrent.ConcurrentHashMap;

public class user {
   private String name;
   private String passcode;
   private int userId; 
   private ConcurrentHashMap <Integer, String> myChannelById; //int- subscribe id, string- channel i subscribe to
   private volatile boolean isConnected=false;



   public user(String name, String passcode, int userId){
    this.name=name;
   this.passcode=passcode;
this.userId=userId;
   this.myChannelById=new ConcurrentHashMap<>();
   }


   public String getName(){
    return name;
   }

   public String getPasscode(){
    return passcode;
   }

   public ConcurrentHashMap <Integer, String> getMyChannelById(){
    return myChannelById;
   }
   

   public void addChannel(String channel, Integer userChannelId){
    if(!myChannelById.containsValue(channel)){
        myChannelById.put(userChannelId,channel);

    }
   }
   public void removeChannel( Integer userChannelId){
    if(myChannelById.containsKey(userChannelId)){
        myChannelById.remove(userChannelId);

    }
   }
   public void removeAllChannel(){
    myChannelById.clear();//when i logout
   }

   public void setIsConnected(boolean newval){
    isConnected=newval;
   }

   public boolean getIsConnectes(){
    return isConnected;
   }


   }




