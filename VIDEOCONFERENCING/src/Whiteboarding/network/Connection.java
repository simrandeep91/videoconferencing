package Whiteboarding.network;

import java.net.*;
import java.io.*;

/**
 * <p>Encapsulate a Socket, notifying the owner of incoming messages and
 * of disconnection.</p>
 *
 * @author idb20
 * @author David Morgan
 */

public class Connection implements Runnable
{
  /*
   * <p>The Socket this Connection encapsulates.</p>
   */

  private Socket socket;

  /**
   * <p>Thread that listens for incoming {@link Message}s.</p>
   */

  private Thread monitor;

  /**
   * <p>Connection identifier.</p>
   */

  private int clientID;

  /**
   * <p>The ConnectionOwner to be notified on receipt of messages or
   * on disconnection.</p>
   */

  private ConnectionOwner owner;

  /**
   * <p>Input stream used to receive data.</p>
   */

  private InputStream inStream;

  /**
   * <p>Output stream used to send data.</p>
   */

  private OutputStream outStream;

  /**
   * <p>Object input stream used to receive {@link Message} objects.</p>
   */

  private ObjectInputStream in;

  /**
   * <p>Object output stream used to send {@link Message} objects.</p>
   */

  private ObjectOutputStream out;

  /**
   * <p>Boolean indicating whether the clientID is valid yet.</p>
   * <p>Since valid clientIDs can only be assigned by the server, clients
   * must wait until they are given their ID.</p>
   */

  private boolean isClientIDValid;

  /**
   * <p>Connection objects are constructed with a SocketAddress and a
   * receiver for Message-type objects received from that Socket.
   * When constructed, Connection will initiate a Thread to monitor
   * incoming Messages.</p>
   * 
   * <p>This constructor supplies a clientID; it should only be used
   * by the server since only the server can properly assign clientIDs.</p>
   * 
   */
    
  public Connection(Socket socket, ConnectionOwner owner,
		    int clientID) throws IOException
  {
    this.clientID=clientID;
    this.owner=owner;
    this.socket=socket;
    isClientIDValid=true;

    inStream=socket.getInputStream();
    outStream=socket.getOutputStream();
        
    monitor=new Thread(this);
    monitor.start();
  }

  /**
   * <p>Connection objects are constructed with a SocketAddress and a
   * receiver for Message-type objects received from that Socket.
   * When constructed, Connection will initiate a Thread to monitor
   * incoming Messages.</p>
   * 
   * <p>This constructor does not contain a clientID; the correct clientID
   * will be obtained from the remote Connection.</p>
   * 
   */
    
  public Connection(Socket socket, ConnectionOwner owner) throws IOException
  {
    this.clientID=clientID;
    this.owner=owner;
    this.socket=socket;
    isClientIDValid=false;
    inStream=socket.getInputStream();
    outStream=socket.getOutputStream();        
    monitor=new Thread(this);
    monitor.start();

    sendMessage(new Message(Message.REQUEST_CLIENT_ID, null));
  }
   

  /**
     * Method run
     * 
     * This method (only to be called by the Connection's internal Thread)
     * monitors the Socket for incoming data - each received Message object is
     * passed on to the receiver (of type IncomingMessage) specified in the
     * construction of this Connection. This method will only terminate once the
     * Socket is closed.
     * 
     * @see Runnable#run()
     * @see close
     */

  public void run()
  {
    try
    {
      in=new ObjectInputStream(inStream);

      while(true)
      {
	Message message=(Message)in.readObject();

	// Some messages are handled here

	switch(message.getType())
	{
	  case Message.REQUEST_CLIENT_ID:
	  {
	    sendMessage(new Message(Message.CLIENT_ID, new Integer(clientID)));

	    break;
	  }
	  
	  case Message.CLIENT_ID:
	  {
	    if(clientID==0)
	    {
	      clientID=((Integer)message.getData()).intValue();
	      isClientIDValid=true;
	    }

	    break;
	  }

	  // Other messages handled by the Connection owner

	  default:
	  {
	    message.setClientID(clientID);

	    try
	    {
	      owner.receiveRemoteMessage(message);
	    }
	    catch(Exception e)
	    {
	      System.out.println("Error on receiving message: "+
				 message.getTypeString());

	      e.printStackTrace();
	    }
	  }
	}
      }
    }
    catch(Exception e)
    {
      System.out.println(this+" closing.");
    }

    try
    {
      if (!socket.isClosed())
      {
	socket.close();                    
      }
    }
    catch(IOException e) {}

    owner.close(this);
  }
    
  /**
   * <p>Returns the clientID, whose purpose is to help identify Connections
   * through which {@link Message}s are being sent.</p>
   * <p>If the Connection does not yet have a valid clientID, this will
   * sleep for a maximum of five seconds awaiting a valid clientID from
   * the server. If it does not get a valid clientID it will close the
   * connection.</p>
   */
  
  public int getClientID()
  {
    if(!isClientIDValid)
    {
      for(int i=0; (!isClientIDValid)&&(i!=100); i++)
      {
	try
	{
	  Thread.sleep(50);
	}
	catch(InterruptedException e) { e.printStackTrace(); }
      }
    }

    if(!isClientIDValid)
    {
      System.out.println(this+" timed out.");

      close();
    }

    return clientID;
  }

  /**
   * <p>Send a {@link Message} over the connection.</p>
   */

  public synchronized void sendMessage(Message message) throws IOException
  {
    if(out==null) out=new ObjectOutputStream(outStream);   
    out.writeObject(message);
  }

  /**
   * <p>Check whether the socket is connected.</p>
   */

  public boolean isClosed()
  {
    return (socket==null)||socket.isClosed();
  }
	
  /**
   * <p>This method closes the Connection's Socket, which will cause the
   * termination of its monitor Thread. On return, the Connection should be
   * discarded.</p>
   */

  public void close()
  {
    try
    {
      if (!socket.isClosed())
      {
	socket.close();                    
      }
    }
    catch (IOException e)
    {
      e.printStackTrace();
      // Fail silently; an error here means it was already closed
    }
  }

  /**
   * <p>Flush the connection of outgoing data.</p>
   */

  public void flush() throws IOException
  {
    out.flush();
  }

  /**
   * <p>Obtain a String representation of the connection.</p>
   */

  public String toString()
  {
    return "Connection "+clientID;
  }
}
