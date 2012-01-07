
import SecureChat.file.*;
import SecureChat.login.*;
import java.io.*;
import java.util.*;
import java.lang.*;
import java.util.concurrent.locks.*;
import java.net.*;
import java.security.*;
import javax.crypto.SecretKey;

public class ClientThread extends Thread {
    private boolean type;
    private User usr;
    static boolean  connect;
    static boolean accepted;
    static Lock  sem ;
    static Condition WaitCall; 	
    private boolean PresentKey;

    public ClientThread (User usr, boolean type)throws IOException{
        this.sem = new ReentrantLock();
        this.type = type;
        this.usr = usr;
        WaitCall = sem.newCondition();
        accepted = false;
        PresentKey = true;
    }
    
    public boolean IsConnected(){
        boolean test;
        sem.lock();
        test = connect;
        sem.unlock();
        return test;
    }

    public void signalConnected(){
        sem.lock();
        try{
            WaitCall.signal();
        }
        finally{
            sem.unlock();
        }	
    }
    
    public void exitThread(){
        System.exit(0);	
    }
    
    private void resetConnect(){
        sem.lock();
        connect = false;							
        sem.unlock();
    }
    
    private void setConnect(){
        sem.lock();
        connect = true;							
        sem.unlock();
    }
    
    public void setAccepted(boolean value){
        accepted = value;
    }
    public boolean isAccepted(){
        return accepted;
    }

    public String getText (byte[] arr) throws UnsupportedEncodingException
    {
        String s = new String( arr, "UTF-8" );
        return s;
    }
    
    public void run() throws RuntimeException{
        Socket csock;
        ServerSocket ss;
        String FName = null;
        ObjectOutputStream StreamOut=null;
        byte[] Buff = null;
        ObjectInputStream ois;
        BufferedReader stdIn = new BufferedReader ( new InputStreamReader (System.in));
        try{
            
            //server's body
            if(type){
                while(true){
                    resetConnect();
                                        
                    ss = new ServerSocket(usr.getServerPort());
                    csock = ss.accept();// Attesa socket
                    if(IsConnected())System.exit(0); //whether the user has already connect the process has killed.
                   
                    StreamOut = new ObjectOutputStream( csock.getOutputStream() );
                    ois = new ObjectInputStream( csock.getInputStream() );
                    Buff = (byte[]) ois.readObject();
                    
                    FName = getText(Buff);
                    System.out.print("[CHAT] "+ FName + ": "); //accept message
                    Buff = (byte[]) ois.readObject();
                    FName = getText(Buff);
                    usr.setFriendName(FName);

                    sem.lock();
                    connect = true;							
                    WaitCall.await();//wait user decision (see other thread)				
                    sem.unlock();
                    
                    if(!isAccepted())StreamOut.writeObject(("NACK").getBytes());						
                    else {
                        StreamOut.writeObject((usr.getUserName()).getBytes());
                        System.out.println("[CHAT] Connected with " + usr.getFriendName());
                        break;
                    }
                }
                
            }
            
            //client's body
            //receive the name on connect success
            else{
                System.out.println("Trying to connect on port: " + usr.getClientPort());
                if(IsConnected())System.exit(0); //whether the server mode in on, the client mode have to be closed
                csock = new Socket(usr.getClientIp(),usr.getClientPort());
                StreamOut = new ObjectOutputStream( csock.getOutputStream() );
                ois = new ObjectInputStream( csock.getInputStream() );
                
                StreamOut.writeObject(("\n" + usr.getUserName() + " would talk with you, please press \'y\' to accept").getBytes());    
                StreamOut.writeObject((usr.getUserName()).getBytes());

                Buff = (byte[]) ois.readObject();
                FName = getText(Buff);
                
                
                if(FName.compareTo("NACK")==0){
                    System.out.println("[CHAT] Connection not accepted");
                    System.exit(0);
                }

                else{/*Initializes the friend's parameters*/
                    System.out.println("[CHAT] Connected with " + FName);
                    usr.setFriendName(FName);
                }
                
            }
            SecretKey key = usr.createDiffieHellman(Directory.PATHDH,StreamOut,ois);                       
           
            if(key == null)
                {
                    System.out.println("[Error] Unable to complete Diffie-Hellman algorhitm");
                    System.exit(-1);
                }
  
            usr.desInstance(key);

            //gestire meglio, se un utente esce deve farlo anche l'altro.
            if(!usr.isRsaPresent(usr.getFriendName()))
              PresentKey = false;
            else PresentKey = true;

            /*receive messages*/
            new ReceiveMessage(ois,usr).start();
            
            /*send messages*/
            while (true)
                StreamOut.writeObject(usr.Encrypt(stdIn.readLine()));
                
        }
        catch (Exception e) {
            if(PresentKey){ 
                System.out.println("[Error] User appears to be offline");
                System.err.println(e.getMessage());
            }

            else System.out.println("Unable to find public key of " + usr.getFriendName() + ", fecth the key first.");            
            System.exit(0);
        }	
    }
}