/*
  Server Class: Simulates a Server that collects information from various sensors(Clients) and uploads a Report regularly
	Written By: Eric Su(SXXERI002), Lynray Barends (BRNLYN013), Phaswana Malatjie(MLTPHA002)
	Date: 5 April 2012
*/

import java.io.*;
import java.net.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Scanner;

public class Server
{

    static Socket client_socket = null;
    static ServerSocket server_socket = null;

    static clientThread t[] = new clientThread [3]; //accepting about 3 clients to connect

    public static void main (String[] args)
    {

	int port_num = 32505; //this could be anyone's port number.

	if (args.length < 1)
	{
	    System.out.println ("server is running on port: " + port_num);
	}
	else
	{
	    port_num = Integer.valueOf (args [0]).intValue ();
	}

	try
	{
	    server_socket = new ServerSocket (port_num);
	}
	catch (IOException e)
	{
	    System.out.println (e);
	}

	Multi:
	while (true)
	{
	    try
	    {
		client_socket = server_socket.accept ();
		//Creates Dedicated thread for each Sensor
		for (int i = 0 ; i <= 2 ; i++)
		{
		    if (t [i] == null)
		    {
			if (i == 0)
			    (t [i] = new clientThread ("water", client_socket, t)).start ();
			else if (i == 1)
			    (t [i] = new clientThread ("sound", client_socket, t)).start ();
			else if (i == 2)
			{
			    (t [i] = new clientThread ("bend", client_socket, t)).start ();
			    break Multi;
			}
			break;
		    }
		}
	    } //this allows multiple clients running
	    catch (IOException e)
	    {
		System.out.println (e);
	    }
	    //Thread that will upload HTML report and notify Client when it is ready to be uploaded
	    Thread fileLoader = new Thread (new Runnable ()
	    {

		public void run ()
		{

		    //Sets up log file
		    PrintStream log = null;
		    try
		    {
			log = new PrintStream (new FileOutputStream ("sensors/log.txt"));
		    }
		    catch (Exception e)
		    {
			e.printStackTrace ();
		    }

		    //Sets up Date Format to record Current Time
		    DateFormat dateFormat = new SimpleDateFormat ("HH:mm:ss");
		    Calendar cal;
		    String time;
		    while (true)
		    {
			//Will Upload newest report every 20 Seconds
			try
			{
			    Thread.sleep (20000);
			}
			catch (Exception e)
			{
			    e.printStackTrace ();
			}

			Html.generate (); //Generates HTML

			//Records Time To Log File
			cal = Calendar.getInstance ();
			time = dateFormat.format (cal.getTime ());
			log.append (time + ": Server Uploaded Report to http://people.cs.uct.ac.za/~" + Html.STUDENT_NUMBER + ".Report.html\n");
			//instructs client to download report
			if (t [0] != null)
			    t [0].output_stream.println ("update report");
			cal = Calendar.getInstance ();
			time = dateFormat.format (cal.getTime ());
			log.append (time + ": Client Downloaded Report\n"); //Logs the Time
		    }
		}
	    }
	    );

	    fileLoader.start (); //Starts The Thread

	}
    }


    //Each Client Thread Responds to a Sensor (Client Class)
    static class clientThread extends Thread
    {

	Scanner input_stream = null;
	PrintStream output_stream = null;
	Socket client_socket = null;

	clientThread t[];
	String name; //Name of the Sensor it is Responsible for
	//Constructor
	public clientThread (String n, Socket client_socket, clientThread[] t)
	{
	    this.client_socket = client_socket;
	    this.t = t;
	    name = n;
	}

	public void run ()
	{
	    String line = "";
	    String dir;
	    PrintStream file = null;
	    //Sets Up File Directory Name According to the Sensor it responds to
	    if (name.equals ("water"))
		dir = "water.data";
	    else if (name.equals ("sound"))
		dir = "sound.data";
	    else
		dir = "bend.data";

	    DateFormat dateFormat = new SimpleDateFormat ("HH:mm:ss");
	    Calendar cal;
	    String time;
	    int count = 0; //Keeps track of the Values Recorded
	    try
	    {
		//Opens The Streams
		input_stream = new Scanner (client_socket.getInputStream ());
		output_stream = new PrintStream (client_socket.getOutputStream ());
		file = new PrintStream (new FileOutputStream ("sensors/" + dir));


		while (count != 20)
		{ //Can Change depending on how any values you would like each Client to produce
		    line = input_stream.nextLine (); //input received from client
		    //Gets Current Time
		    cal = Calendar.getInstance ();
		    time = dateFormat.format (cal.getTime ());

		    //Saves input from client ( as well as time stamp that it was received at) to respective sensor file
		    file.append (line.substring (0, line.indexOf (",")) + "," + time + "," + line.substring (line.indexOf (",") + 1) + "\n");
		    output_stream.println ("Processed"); //Can Edit out - just used for debugging purposes.

		    count++; //increases count

		}

		//Closes Current thread
		for (int i = 0 ; i <= 2 ; i++)
		    if (t [i] == this)
			t [i] = null;
		//Closes Files
		file.close ();
		//Sends Message to client that it has processed the requested amount of values
		output_stream.println ("Finished");
		//Closes the Streams
		input_stream.close ();
		output_stream.close ();
		client_socket.close ();
	    }
	    catch (IOException e)
	    {
		e.printStackTrace ();
	    }
	}
    }
}
