/**
   This classe allows to manage users' account
   After that an user has logged in.
*/

class User{
    private String UserName;
    private String FriendName;
    private String serverIp;
    private String clientIp;
    private int serverPort;
    private int clientPort;
    private SecureLogin login;
    private Rsa rsa;
    private boolean valid;
    
    /*This class must be called after the login using SecureLogin class.
     Indeed the main constructor uses a SecureLogin object to identify an user.
    */
    public User(int port,String server,SecureLogin log){
        valid = false;
        this.login = log;
        this.UserName = log.userBound();
        this.serverPort = port;
        this.serverIp = server;
        if(this.UserName!=null) valid = true;  
    }
   
    public boolean isValid(){
        return valid;
    }
    
    public void setClientPort(int port){
        clientPort = port;
    }

    public void setClientIp(String ip){
        clientIp = ip;
    }

    public void setFriendName(String name){
        FriendName = name;
    }

    public void setServerPort(int port){
        this.serverPort = port;
    }
   
    public void setServerIp(String server){
        this.serverIp = server;
    }

    public String getUserName(){
        return UserName;
    }

    public String getFriendName(){
        return FriendName;
    }
    public int getClientPort(){
        return clientPort;
    }

    public String getClientIp(){
        return clientIp;
    }
    
    public int getServerPort(){
        return serverPort;
    }
   
    public String getServerIp(){
        return this.serverIp;
    } 
    

    /**RSA methods*/
    public boolean  CreateRsa(String KeyDir) throws Exception{
        rsa = new Rsa(KeyDir,login);  
        if(!rsa.setUserName(UserName))return false;
        rsa.createKeys();
        return true;
    }
    
    public boolean isRsaPresent(String UserName){
        return  rsa.isPresent(UserName);
    }

    public boolean SignMessage(){
        return true;
    }
    
    public boolean CheckSign(){ return true;}

    public String Decrypt(byte[] data) throws Exception{
        return rsa.Decrypt(data);
    }

    public byte[] Encrypt(String data) throws Exception{        
        return rsa.Encrypt(data,rsa.GetPublicKey(FriendName));
    }
}