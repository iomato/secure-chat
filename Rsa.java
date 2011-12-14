import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.io.IOException;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.Cipher;
import java.security.NoSuchAlgorithmException;

public class Rsa {
    public enum KEY {PRIVATE,PUBLIC};
    static final String PUBLICPATH = "public.key";
    static final String PRIVATEPATH = "private.key";
    static final String KEYPATH = "/home/badnack/Projects/SecureChat/Ssl-Chat/KeyFiles/";	
    //static final String KEYPATH = "/home/davide/Ssl-Chat/KeyFiles/";

    public static String UserToPath(String UserName,KEY k){
          if(k==KEY.PUBLIC)
            return KEYPATH + UserName + "_" + PUBLICPATH;
            return KEYPATH + UserName + "_" + PRIVATEPATH;
    }

    //Checks whether a key is present or not
    public static boolean isPresent(String UserName){
        try{
            FileInputStream fis = new FileInputStream(KEYPATH + UserName + "_" + PUBLICPATH);
            fis.close();
        }catch(Exception x){return false; }
        return true;
    }

    //Get a public key stored giving the username
    public static PublicKey GetPublicKey(String UserName) throws IOException,InvalidKeySpecException,NoSuchAlgorithmException{
        String path = KEYPATH + UserName + "_" + PUBLICPATH;
        FileInputStream fis = new FileInputStream(path);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int i = 0;
        while((i = fis.read()) != -1) {
            baos.write(i);
        }
        
        
        byte[] publicKeyBytes = baos.toByteArray();
        baos.close();
        

        // CONVERTI CHIAVE PUBBLICA DA X509 A CHIAVE UTILIZZABILE
        
        // Inizializza convertitore da X.509 a chiave pubblica
        X509EncodedKeySpec ks = new X509EncodedKeySpec(publicKeyBytes);
        // Inizializza un KeyFactory per ricreare la chiave usando RSA 
        KeyFactory kf = KeyFactory.getInstance("RSA");
        // Crea una chiave pubblica usando generatePublic di KeyFactory in base la chiave decodificata da ks
        return kf.generatePublic(ks); 

    }

    /*To protect with password?*/
    public static PrivateKey GetPrivateKey(String UserName) throws IOException,InvalidKeySpecException,NoSuchAlgorithmException{
        String path = KEYPATH + UserName + "_" + PRIVATEPATH;        
        FileInputStream fis = new FileInputStream(path);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        int i = 0;
        while((i = fis.read()) != -1) {
            baos.write(i);
        }
        byte[] privateKeyBytes = baos.toByteArray();
        baos.close();
        
        
        
        // CONVERTI CHIAVE PRIVATA PKCS8 IN CHIAVE NORMALE
        
        PKCS8EncodedKeySpec ks = new PKCS8EncodedKeySpec(privateKeyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
       return kf.generatePrivate(ks);
       
    }


    public static byte[] Encrypt(String data, PublicKey publicKey) throws Exception {
        
        byte[] plainFile;
        plainFile=data.getBytes();
        
        
        // Inizializzo un cifrario che usa come algoritmo RSA, come modalita' ECB e come padding PKCS1
        Cipher c = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        
        // Lo inizializzo dicendo modalita' di codifica e chiave pubblica da usare
        c.init(Cipher.ENCRYPT_MODE, publicKey);
        // codifico e metto il risultato in encodeFile
        byte[] encodeData = c.doFinal(plainFile);
        return encodeData;
    }
    
    public static String Decrypt(byte[] sorg ,PrivateKey privateKey) throws Exception{
        
        // DECODIFICA
        
        Cipher c = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        c.init(Cipher.DECRYPT_MODE, privateKey);
        
        byte[] plainFile = c.doFinal(sorg);
        
        // DA BYTE[] A STRING
        StringBuilder sb = new StringBuilder (plainFile.length);
        for (byte b: plainFile)
            sb.append ((char) b);
        
        return sb.toString();
    }
    
    public static void createKeys(String UserName) throws Exception {
        boolean Exists = true;
        // GENERA COPPIA DI CHIAVI
        try{
            FileInputStream fis = new FileInputStream(KEYPATH + UserName + "_" + PUBLICPATH);
        }catch(Exception x){
            //creates a key file to store keys
            Exists = false;
        }
        
        if(Exists) return;
        
        
        //inizializza un generatore di coppie di chiavi usando RSA
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(1024);
        // genera la coppia
        KeyPair kp = kpg.generateKeyPair();
        
        // SALVA CHIAVE PUBBLICA
        
        byte[] publicBytes = kp.getPublic().getEncoded();
        // salva nel keystore selezionato dall'utente
        FileOutputStream fos = new FileOutputStream( KEYPATH + UserName + "_" + PUBLICPATH);
        
        fos.write(publicBytes);
        fos.close();
        
        // SALVA CHIAVE PRIVATA
        
        // ottieni la versione codificata in PKCS#8
        byte[] privateBytes = kp.getPrivate().getEncoded();
        
        fos = new FileOutputStream(KEYPATH + UserName + "_" + PRIVATEPATH);
        fos.write(privateBytes);
        fos.close();
    }
  
}


