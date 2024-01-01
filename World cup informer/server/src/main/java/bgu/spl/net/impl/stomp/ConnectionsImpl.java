package bgu.spl.net.impl.stomp;

import bgu.spl.net.srv.Connections;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import bgu.spl.net.srv.ConnectionHandler;

public class ConnectionsImpl<T> implements Connections<T> {
    private Map<Integer, ConnectionHandler<T>>myClientsById;//active_clients_map
    private AtomicInteger supllyId;//id_count
    ReadWriteLock mapRWLock;//readwritelock

    private static class SingeltoneHolder{//as we saw in practice, we want 1 instance of connections
        private static ConnectionsImpl connections= new ConnectionsImpl<>();
            
        }
    public  ConnectionsImpl(){
        myClientsById=new HashMap<>();
       supllyId=new AtomicInteger(0);
        mapRWLock=new ReentrantReadWriteLock();

    }


    public static ConnectionsImpl getInstance(){//from the practice
        return SingeltoneHolder.connections;
    }
    public boolean send (int connectionId, T msg){
        mapRWLock.readLock().lock();
        //in try and finally in order to release key after return or exception
        try{
        if(myClientsById.containsKey(connectionId)){
            myClientsById.get(connectionId).send(msg);
            return true;
        }
        return false;
    }
    finally{
        mapRWLock.readLock().unlock();
    }
    }


    public void send(String channel, T msg){
     
    }
    public void disconnect(int connectionId){
        mapRWLock.writeLock().lock();
        if(myClientsById.containsKey(connectionId)){
            myClientsById.remove(connectionId);
        }
        mapRWLock.writeLock().unlock();
    }

    public void connect(ConnectionHandler<T>handler, int connectionId){
        mapRWLock.writeLock().lock();
        if(!myClientsById.containsKey(connectionId)){
            myClientsById.put(connectionId, handler);

        }
        mapRWLock.writeLock().unlock();
    }

     public int getAndInctId(){//a uniqe id for every new active client
        return supllyId.getAndIncrement();
     }

       public Map<Integer, ConnectionHandler<T>> getMyClientsById(){
        return myClientsById;
       }

    




   
        
    }





    
    

