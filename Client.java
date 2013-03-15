import java.io.*;
import java.lang.*;
import java.net.*;
import java.math.BigInteger;
import java.security.AlgorithmParameterGenerator;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

   import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.SecretKey;
import javax.crypto.ShortBufferException;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;

   public class Client
   {
      public static void main(String[] args)
      {
         try 
         {
          	 System.out.println("point 0");
             // Create a connection to the server socket on the server application
             InetAddress host = InetAddress.getLocalHost();
             Socket socket = new Socket(host.getHostName(), 7777);
        	 
             ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
	     System.out.println("point 1");

        	 
        	 DHParameterSpec dhSkipParamSpec;
        	 
        	 // use some pre-generated, default DH parameters
        	 System.out.println("Using SKIP Diffie-Hellman parameters");
        	 dhSkipParamSpec = new DHParameterSpec(skip1024Modulus, skip1024Base);
        	 
        	 /*
        	 * Alice creates her own DH key pair, using the DH parameters from
        	 * earlier code
        	 */
        	 System.out.println("ALICE: Generate DH keypair ...");
        	 KeyPairGenerator aliceKpairGen = KeyPairGenerator.getInstance("DH");
        	 aliceKpairGen.initialize(dhSkipParamSpec);
        	 KeyPair aliceKpair = aliceKpairGen.generateKeyPair();

        	 // Alice creates and initializes her DH KeyAgreement object
        	 System.out.println("ALICE: Initialization ...");
        	 KeyAgreement aliceKeyAgree = KeyAgreement.getInstance("DH");
        	 aliceKeyAgree.init(aliceKpair.getPrivate());

        	 // Alice encodes her public key, and sends it over to Bob.
        	 byte[] alicePubKeyEnc = aliceKpair.getPublic().getEncoded();       
         
            // Send the public part of DH from alice to bob
            oos.writeObject(alicePubKeyEnc);
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            System.out.println("point 2");
         
            System.out.println("waiting for bobs key");
            // Read and display the response message sent by server application
            byte[] bobsKey = (byte[]) ois.readObject();
            
            /*
             * Alice uses Bob's public key for the first (and only) phase
             * of her version of the DH
             * protocol.
             * Before she can do so, she has to instanticate a DH public key
             * from Bob's encoded key material.
             */
             KeyFactory aliceKeyFac = KeyFactory.getInstance("DH");
             X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(bobsKey);
             PublicKey bobPubKey = aliceKeyFac.generatePublic(x509KeySpec);
             System.out.println("ALICE: Execute PHASE1 ...");
             aliceKeyAgree.doPhase(bobPubKey, true);
            
             byte[] aliceSharedSecret = aliceKeyAgree.generateSecret();
             int aliceLen = aliceSharedSecret.length;

             System.out.println("Alice secret: " + toHexString(aliceSharedSecret));
            
            
         
            ois.close();
            oos.close();
         } 
            catch (UnknownHostException e){
               e.printStackTrace();
            } catch (IOException e){
               e.printStackTrace();
            } catch (ClassNotFoundException e){
               e.printStackTrace();
            } catch (NoSuchAlgorithmException e){
				e.printStackTrace();
			} catch (InvalidAlgorithmParameterException e){
				e.printStackTrace();
			} catch (InvalidKeyException e){
				e.printStackTrace();
			} catch (InvalidKeySpecException e){
				e.printStackTrace();
			}
      }
      
      
      
      
      
      /*
       * Converts a byte to hex digit and writes to the supplied buffer
       */
       private static void byte2hex(byte b, StringBuffer buf) {
       char[] hexChars = { '0', '1', '2', '3', '4', '5', '6', '7', '8',
       '9', 'A', 'B', 'C', 'D', 'E', 'F' };
       int high = ((b & 0xf0) >> 4);
       int low = (b & 0x0f);
       buf.append(hexChars[high]);
       buf.append(hexChars[low]);
       }
      /*
       * Converts a byte array to hex string
       */
       private static String toHexString(byte[] block) {
       StringBuffer buf = new StringBuffer();

       int len = block.length;

       for (int i = 0; i < len; i++) {
       byte2hex(block[i], buf);
       if (i < len - 1) {
       buf.append(":");
       }
       }
       return buf.toString();
       }
      
      // The 1024 bit Diffie-Hellman modulus values used by SKIP
      private static final byte skip1024ModulusBytes[] = {
      (byte)0xF4, (byte)0x88, (byte)0xFD, (byte)0x58,
      (byte)0x4E, (byte)0x49, (byte)0xDB, (byte)0xCD,
      (byte)0x20, (byte)0xB4, (byte)0x9D, (byte)0xE4,
      (byte)0x91, (byte)0x07, (byte)0x36, (byte)0x6B,
      (byte)0x33, (byte)0x6C, (byte)0x38, (byte)0x0D,
      (byte)0x45, (byte)0x1D, (byte)0x0F, (byte)0x7C,
      (byte)0x88, (byte)0xB3, (byte)0x1C, (byte)0x7C,
      (byte)0x5B, (byte)0x2D, (byte)0x8E, (byte)0xF6,
      (byte)0xF3, (byte)0xC9, (byte)0x23, (byte)0xC0,
      (byte)0x43, (byte)0xF0, (byte)0xA5, (byte)0x5B,
      (byte)0x18, (byte)0x8D, (byte)0x8E, (byte)0xBB,
      (byte)0x55, (byte)0x8C, (byte)0xB8, (byte)0x5D,
      (byte)0x38, (byte)0xD3, (byte)0x34, (byte)0xFD,
      (byte)0x7C, (byte)0x17, (byte)0x57, (byte)0x43,
      (byte)0xA3, (byte)0x1D, (byte)0x18, (byte)0x6C,
      (byte)0xDE, (byte)0x33, (byte)0x21, (byte)0x2C,
      (byte)0xB5, (byte)0x2A, (byte)0xFF, (byte)0x3C,
      (byte)0xE1, (byte)0xB1, (byte)0x29, (byte)0x40,
      (byte)0x18, (byte)0x11, (byte)0x8D, (byte)0x7C,
      (byte)0x84, (byte)0xA7, (byte)0x0A, (byte)0x72,
      (byte)0xD6, (byte)0x86, (byte)0xC4, (byte)0x03,
      (byte)0x19, (byte)0xC8, (byte)0x07, (byte)0x29,
      (byte)0x7A, (byte)0xCA, (byte)0x95, (byte)0x0C,
      (byte)0xD9, (byte)0x96, (byte)0x9F, (byte)0xAB,
      (byte)0xD0, (byte)0x0A, (byte)0x50, (byte)0x9B,
      (byte)0x02, (byte)0x46, (byte)0xD3, (byte)0x08,
      (byte)0x3D, (byte)0x66, (byte)0xA4, (byte)0x5D,
      (byte)0x41, (byte)0x9F, (byte)0x9C, (byte)0x7C,
      (byte)0xBD, (byte)0x89, (byte)0x4B, (byte)0x22,
      (byte)0x19, (byte)0x26, (byte)0xBA, (byte)0xAB,
      (byte)0xA2, (byte)0x5E, (byte)0xC3, (byte)0x55,
      (byte)0xE9, (byte)0x2F, (byte)0x78, (byte)0xC7
      };
      // The SKIP 1024 bit modulus
      private static final BigInteger skip1024Modulus =
      new BigInteger(1, skip1024ModulusBytes);

      // The base used with the SKIP 1024 bit modulus
      private static final BigInteger skip1024Base = BigInteger.valueOf(2);
   }
