package Whiteboarding.main;

/**
 * <p>Interface for objects wanting to receive notification about {@link Session}
 * objects.</p>
 * 
 * @author idb20
 * @author David morgan
 */

public interface SessionListener
{

  /**
   * <p>Called when the session closes.</p>
   */

  public void handleSessionClose(Session session);
}
