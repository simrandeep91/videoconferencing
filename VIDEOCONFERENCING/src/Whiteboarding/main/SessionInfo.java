
package Whiteboarding.main;

import java.net.*;
import java.io.*;

/**
 * <p>SessionInfo holds a minimal amount of information describing a Session
 * (name, passwords, etc). It can notify a {@link SessionInfoListener} on
 * change.</p>
 * 
 * @author idb20
 * @author David Morgan
 */
public class SessionInfo implements Serializable, Cloneable
{
  static final long serialVersionUID = -6502272171582275040L;

  /**
   * <p>Session subject.</p>
   */

  private String subject;

  /**
   * <p>Password for read-only access; null indicates not present.</p>
   */

  private String readOnlyPassword;

  /**
   * <p>Password for read-write access; null indicates not present.</p>
   */

  private String readWritePassword;

  /**
   * <p>Address of the ServerSocket for this Session.</p>
   */

  private InetSocketAddress serverAddress;

  /**
   * <p>Number of users current in the Session.</p>
   */

  private int population;

  /**
   * <p>Listener to be notified when the information changes.</p>
   */

  private transient SessionInfoListener listener;

  /**
   * <p>A Session must be initialised with a name String but passwords (read-only
   * and read-write) are optional, as is the server address.</p>
   */

  public SessionInfo(InetSocketAddress serverAddress, String subject,
		     String readOnly, String readWrite,
		     SessionInfoListener listener)
  {
    assert(subject!=null);

    this.serverAddress=serverAddress;
    this.subject=subject;
        
    readOnlyPassword  = readOnly;
    readWritePassword = readWrite;
        
    population = 0;

    this.listener=listener;
  }

  public int getPopulation()
  {
    return population;
  }
    
  public void setPopulation(int population)
  {
    assert(population>=0);

    this.population=population;

    if(listener!=null) listener.sessionInfoChange(this);
  }

  public InetSocketAddress getServerAddress()
  {
    return serverAddress;
  }

  public void setServerAddress(InetSocketAddress serverAddress)
  {
    assert(serverAddress!=null);

    this.serverAddress=serverAddress;

    if(listener!=null) listener.sessionInfoChange(this);
  }

  public String getReadOnlyPassword()
  {
    return readOnlyPassword;
  }
    
  public String getReadWritePassword()
  {
    return readWritePassword;
  }

  /**
   * <p>Set the listener.</p>
   */

  public void setListener(SessionInfoListener listener)
  {
    this.listener=listener;
  }
    
  /**
   * <p>Returns the subject String for the associated Session.</p>
   */

  public String getSubject()
  {
    return subject;
  }

  /**
   * <p>Return a deep copy of the SessionInfo.</p>
   */

  public Object clone()
  {
    try
    {
      SessionInfo result=(SessionInfo)super.clone();
      
      return result;
    }
    catch(CloneNotSupportedException e)
    {
      // This should never happen

      assert(false);

      return null;
    }
  }

  /**
   * <p>Obtain a String representation of the SessionInfo suitable for
   * displaying to users.</p>
   */

  public String toString()
  {
    String result=subject+" ("+population+" user"+((population==1)?"":"s")+")";

    if((readWritePassword!=null)||(readOnlyPassword!=null))
    {
      result+=" (password required)";
    }

    return result;
  }
}
