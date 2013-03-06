/*
	Client Class: Simulates a Client that sends information to Server and downloads a Report regularly
	Written By: Eric Su(SXXERI002), Lynray Barends (BRNLYN013), Phaswana Malatjie(MLTPHA002)
	Date: 5 April 2012
*/
import java.io.*;
import java.net.*;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.lang.Math;
import java.util.Scanner;
//libraries used for creating a graph gui
import java.awt.*;
import java.awt.font.*;
import java.awt.geom.*;
import javax.swing.*;


public class Client extends JPanel implements Runnable
{
    static Socket client_socket = null;
    static PrintStream output_stream = null;
    static Scanner input_stream = null;
    static BufferedReader input_line = null;
    static boolean closed = false;
    static double[] data = new double [20];
    final int PAD = 20;

    static final String USER = "BRNLYN013"; //could be anyone's Student Number

    public static void main (String[] args)
    {
	int port_num = 32505; //Students' Port Number
	String host = "nightmare.cs.uct.ac.za";
	String sensor;
	if (args.length < 3)
	{
	    sensor = args [0]; //Sensor corresponding to 1st Argument
	    System.out.println ("got here: " + host + " " + port_num);
	}
	else
	{
	    sensor = args [0]; //Sensor corresponding to 1st Argument
	    host = args [1];
	    port_num = Integer.valueOf (args [2]).intValue ();
	}
	try
	{
	    client_socket = new Socket (host, port_num); //Creates New Socket
	    //Creates Streams
	    input_line = new BufferedReader (new InputStreamReader (System.in));
	    output_stream = new PrintStream (client_socket.getOutputStream ());
	    input_stream = new Scanner (client_socket.getInputStream ());
	}
	catch (UnknownHostException e)
	{ //Unknown Host
	    System.err.println ("don't know about host" + host);
	}
	catch (IOException e)
	{ //Unable to get a Connection
	    System.err.println ("couldn't get I/O for the connection to the host" + host);
	}

	//If everything is in Place
	if (client_socket != null && output_stream != null && input_stream != null)
	{
	    try
	    { //Start New CLient Thread
		new Thread (new Client ()).start ();
		DateFormat dateFormat = new SimpleDateFormat ("HH:mm:ss");
		Calendar cal;
		String time;
		int i = 0;

		while (!closed)
		{ //Sends Recording Every Second while connection with Server is not Closed
		    cal = Calendar.getInstance ();
		    time = dateFormat.format (cal.getTime ());
		    output_stream.println (time + "," + Client.generateValue (sensor));
		    try
		    {
			Thread.sleep (1000);
			data [i] = Client.generateValue (sensor);
			i++;
		    }
		    catch (Exception e)
		    {
			e.printStackTrace ();
		    }
		}

		output_stream.close ();
		input_stream.close ();
		client_socket.close ();
	    }
	    catch (IOException e)
	    {
		System.err.println ("IOException: " + e);
	    }
	}

	JFrame graphWindow = new JFrame ();
	graphWindow.setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);
	graphWindow.add (new Client ());
	graphWindow.setSize (400, 400);
	graphWindow.setLocation (200, 200);
	graphWindow.setVisible (true);
    }


    //Drawing graph contents
    public void paintComponent (Graphics g)
    {
	super.paintComponent (g);
	Graphics2D g2 = (Graphics2D) g;
	g2.setRenderingHint (RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	int w = getWidth ();
	int h = getHeight ();
	// Draw ordinate.
	g2.draw (new Line2D.Double (PAD, PAD, PAD, h - PAD));
	// Draw abcissa.
	g2.draw (new Line2D.Double (PAD, h - PAD, w - PAD, h - PAD));
	// Draw labels.
	Font font = g2.getFont ();
	FontRenderContext frc = g2.getFontRenderContext ();
	LineMetrics lm = font.getLineMetrics ("0", frc);
	float sh = lm.getAscent () + lm.getDescent ();
	// Ordinate label.
	String s = "data";
	float sy = PAD + ((h - 2 * PAD) - s.length () * sh) / 2 + lm.getAscent ();
	for (int i = 0 ; i < s.length () ; i++)
	{
	    String letter = String.valueOf (s.charAt (i));
	    float sw = (float) font.getStringBounds (letter, frc).getWidth ();
	    float sx = (PAD - sw) / 2;
	    g2.drawString (letter, sx, sy);
	    sy += sh;
	}
	// Abcissa label.
	s = "time";
	sy = h - PAD + (PAD - sh) / 2 + lm.getAscent ();
	float sw = (float) font.getStringBounds (s, frc).getWidth ();
	float sx = (w - sw) / 2;
	g2.drawString (s, sx, sy);
	// Draw lines.
	double xInc = (double) (w - 2 * PAD) / (data.length - 1);
	double scale = (double) (h - 2 * PAD) / getMax ();
	g2.setPaint (Color.green.darker ());
	for (int i = 0 ; i < data.length - 1 ; i++)
	{
	    double x1 = PAD + i * xInc;
	    double y1 = h - PAD - scale * data [i];
	    double x2 = PAD + (i + 1) * xInc;
	    double y2 = h - PAD - scale * data [i + 1];
	    g2.draw (new Line2D.Double (x1, y1, x2, y2));
	}
	// Mark data points.
	g2.setPaint (Color.red);
	for (int i = 0 ; i < data.length ; i++)
	{
	    double x = PAD + i * xInc;
	    double y = h - PAD - scale * data [i];
	    g2.fill (new Ellipse2D.Double (x - 2, y - 2, 4, 4));
	}
    }


    private static int getMax ()
    {
	int max = -Integer.MAX_VALUE;
	for (int i = 0 ; i < data.length ; i++)
	{
	    if (data [i] > max)
		max = (int) data [i];
	}
	return max;
    }


    //Generates The Recording depending on Sensor Type
    public static double generateValue (String type)
    {
	int min, max;
	if (type.equalsIgnoreCase ("water"))
	{
	    //Generates Water % between 0 and 100
	    min = 0;
	    max = 100;
	    return Math.round ((min + Math.random () * (max - min)) * 100) / 100.00;
	}
	else if (type.equalsIgnoreCase ("sound"))
	{
	    //Generates Sound Frequency between 20 and 20000HZ
	    min = 20;
	    max = 20000;
	    return Math.round ((min + Math.random () * (max - min)) * 100) / 100.00;
	}
	else if (type.equalsIgnoreCase ("bend"))
	{
	    //Generates =Refractive Index corresponding to the bend between 1 and 2
	    min = 1;
	    max = 2;
	    return Math.round ((min + Math.random () * (max - min)) * 100) / 100.00;
	}
	return 0;
    }


    //Downloads The Report From the Internet
    public static void downloadReport () throws IOException
    {
	URL report = new URL ("http://people.cs.uct.ac.za/~" + USER + "/Report.html");
	ReadableByteChannel rbc = Channels.newChannel (report.openStream ());
	FileOutputStream fos = new FileOutputStream ("report.html");
	fos.getChannel ().transferFrom (rbc, 0, 1 << 24);
	System.out.println ("Report Updated");
    }


    public void run ()
    {
	String responseLine;

	//Gets Input From Server
	while ((responseLine = input_stream.nextLine ()) != null)
	{
	    if (responseLine.equalsIgnoreCase ("update report"))
		//Downloads the Report to the Same Directory that CLient is saved in
		try
		{
		    downloadReport ();
		}
	    catch (IOException e)
	    {
		e.printStackTrace ();
	    }

	    else
	    {
		System.out.println (responseLine); //print out report
		if (responseLine.indexOf ("Finished") != -1)
		    break;                                            //if all is finished closes Client
	    }
	}
	closed = true;

    }
}
