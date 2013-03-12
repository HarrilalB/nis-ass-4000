   import java.io.*;
   import java.lang.*;
   import java.net.*;

   public class Client
   {
      public static void main(String[] args)
      {
         try 
         {
            // Create a connection to the server socket on the server application
            InetAddress host = InetAddress.getLocalHost();
            Socket socket = new Socket(host.getHostName(), 7777);
         
            // Send a message to the client application
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            oos.writeObject("Hello");
         
            // Read and display the response message sent by server application
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            String message = (String) ois.readObject();
            System.out.println("Server response: " + message);
         
            ois.close();
            oos.close();
         } 
            catch (UnknownHostException e)
            {
               e.printStackTrace();
            } 
            catch (IOException e)
            {
               e.printStackTrace();
            } 
            catch (ClassNotFoundException e)
            {
               e.printStackTrace();
            }
      }
   }
