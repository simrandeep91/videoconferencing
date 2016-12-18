package Whiteboarding.main;

import java.io.IOException;

/**
 * <p>Launches jinn in either client or server mode.</p>
 * 
 * @author idb20
 * @author David Morgan
 */

public class Client
{
  public static void main(String[] arg)
  {
      String args[]={"-c"};
    if(args.length==1)
    {        
      if (args[0].equalsIgnoreCase("-c") ||
	  args[0].equalsIgnoreCase("-client"))
      {
        SessionBrowser client = new SessionBrowser();
	client.show();
	return;
      }
      else if (args[0].equalsIgnoreCase("-s") ||
	       args[0].equalsIgnoreCase("-server"))
      {
	try
	{
	  new SessionBrowserServer();
	  return;
	}
	catch(IOException e)
	{
	  System.out.println("Unable to create SessionBrowserServer.");
	  e.printStackTrace();

	  return;
	}
      }
    }

    java.lang.System.out.print(
	"Usage: java Jinn [params]\n"
	+ "  params:\n"
	+ "   -c,  -client:       run as client\n"
	+ "   -s,  -server:       run as server\n");
    }
}
