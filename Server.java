import java.io.*;

import java.lang.*;
import java.net.*;
import java.security.*;
import java.math.BigInteger;
import java.security.AlgorithmParameterGenerator;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
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

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

public class Server
{
   private ServerSocket server;
	private int port = 7777;

	public Server()
	{
		try {
			server = new ServerSocket(port);
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public static void main(String[] args) 
	{
		Server serverObject = new Server();
		serverObject.handleConnection();
	}

	public void handleConnection()
	{
		System.out.println("Waiting for client message...");

				//Waits for client connection
				while (true) {
					try {
						Socket socket = server.accept();
						new ConnectionHandler(socket);
					} 
					catch (IOException e) 
					{
						e.printStackTrace();
					}
				}
	}
}

class ConnectionHandler implements Runnable
{
	private Socket socket;

	public ConnectionHandler(Socket socket) 
	{
		this.socket = socket;

		Thread t = new Thread(this);
		t.start();
	}


	/*
	 * Converts a byte to hex digit and writes to the supplied buffer
	 */
	private void byte2hex(byte b, StringBuffer buf) {
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
	private String toHexString(byte[] block) {
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


	public void run() 
	{
		try
		{


			// Read a message sent by client application and print it out
			ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
			byte[] alicePubKeyEnc = (byte[]) ois.readObject();
			System.out.print("Message received from client: ");



			/*for (byte theByte : alicePubKeyEnc)
             {
               System.out.print(Integer.toHexString(theByte));
             }*/ // use this code to print out the received byte array


			/*
			 * Let's turn over to Bob. Bob has received Alice's public key
			 * in encoded format.
			 * He instantiates a DH public key from the encoded key material.
			 */
			KeyFactory bobKeyFac = KeyFactory.getInstance("DH");
			X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(alicePubKeyEnc);
			PublicKey alicePubKey = bobKeyFac.generatePublic(x509KeySpec);

			/*
			 * Bob gets the DH parameters associated with Alice's public key. 
			 * He must use the same parameters when he generates his own key
			 * pair.
			 */
			DHParameterSpec dhParamSpec = ((DHPublicKey) alicePubKey).getParams();

			// Bob creates his own DH key pair
			System.out.println("BOB: Generate DH keypair ...");
			KeyPairGenerator bobKpairGen = KeyPairGenerator.getInstance("DH");
			bobKpairGen.initialize(dhParamSpec);
			KeyPair bobKpair = bobKpairGen.generateKeyPair();

			// Bob creates and initializes his DH KeyAgreement object
			System.out.println("BOB: Initialization ...");
			KeyAgreement bobKeyAgree = KeyAgreement.getInstance("DH");
			bobKeyAgree.init(bobKpair.getPrivate());

			// Bob encodes his public key, and sends it over to Alice.
			byte[] bobPubKeyEnc = bobKpair.getPublic().getEncoded();
			ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
			oos.writeObject(bobPubKeyEnc);

			/*
			 * Bob uses Alice's public key for the first (and only) phase
			 * of his version of the DH
			 * protocol.
			 */
			System.out.println("BOB: Execute PHASE1 ...");
			bobKeyAgree.doPhase(alicePubKey, true);

			byte[] bobSharedSecret = bobKeyAgree.generateSecret();
			System.out.println("Bob secret: " + toHexString(bobSharedSecret));


			ois.close();
			oos.close();
			socket.close();


			System.out.println("Waiting for client message...");
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		} 
		catch (ClassNotFoundException e)
		{
			e.printStackTrace();
		}

		catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}
}
