package Whiteboarding.model;

import java.io.Serializable;

/*
 * <p>A Shortcut represents either a url or a link to another part of the
 * whiteboard implemented by separate subclasses.</p>
 */

public abstract class Shortcut implements Serializable
{
  static final long serialVersionUID = -6582301946779765389L;

  /*
   * <p>Obtain a boolean indicating whether the Shortcut is on the org.davidmorgan.jinn.
   * as opposed to being a url.</p>
   */

  public abstract boolean isWhiteboard();

  /*
   * <p>Obtain the Shortcut name as a String.</p>
   */

  public abstract String getName();
}
