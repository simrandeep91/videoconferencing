package Whiteboarding.main;

/**
 *
 * <p>Interface for objects wanting to listen for changes to a
 * {@link SessionInfo}.</p>
 *  
 * @author David Morgan
 */
public interface SessionInfoListener
{
  /**
   * <p>Notify of a chang to a {@link SessionInfo}.</p>
   */

  public void sessionInfoChange(SessionInfo sessionInfo);
}
