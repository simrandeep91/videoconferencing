
package Whiteboarding.network;

/**
 *
 * <p>Interface for objects owning a Connection.</p>
 *  
 * @author David Morgan
 */
public interface ConnectionOwner
{
  /**
   * <p>This method is used by an implementing class to receive notification of
   * the disconnection; the implementing class should handle the disconnection
   * cleanly.</p>
   */
  
  public void close(Connection connection);

  /**
   * <p>This method is used to receive an incoming message.</p>
   */

  public void receiveRemoteMessage(Message message);
}
