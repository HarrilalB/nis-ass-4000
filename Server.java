   import java.io.*;
   import java.lang.*;
   import java.net.*;

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
   
      public void run() 
      {
         try
         {
            // Read a message sent by client application
            ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
            String message = (String) ois.readObject();
            System.out.println("Message received from client: " + message);
         
            // Send a response information to the client application
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            oos.writeObject("Message Acknowledged");
         
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
      }
   }
