package Whiteboarding.model;

import java.io.Serializable;

/**
 * <p>An EntityID uniquely and globally identifies a specific Entity in the
 * project. Clients must only generate local EntityIDs; these will be
 * changed to global EntityIDs by the server.</p>
 * @author David Morgan
 */

public class EntityID implements Serializable, Cloneable
{
  // 1. Static variable field declarations

  static final long serialVersionUID = 4574154599312138743L;

  /**
   * <p>Counter used to ensure local IDs are unique.</p>
   */
  
  private static int counter=0;

  // 2. Instance variable field declarations

  /**
   * <p>Single integer used to encode all identifier information.</p>
   */

  private int identifier;

  /**
   * <p>Integer storing user ID.</p>
   */

  private int clientID;

  // 4. Instance constructor declarations

  /**
   * <p>Create a global EntityID. The EntityID is automatically created so
   * that it is unique globally, with the supplied clientID, which should
   * be obtained from {@link SessionClient.getClientID()}.</p>
   */

  public EntityID(int clientID)
  {
    counter++;

    this.identifier=counter;
    this.clientID=clientID;
  }


  /**
   * <p>Set the user identifier part of the EntityID, making it global.</p>
   */

  public void setUserIdentifier(int clientID)
  {
    // Preconditions; clientID not -1

    assert(clientID!=-1);

    // Method proper

    this.clientID=clientID;
  }

  // 9. Instance method declarations

  /**
   * <p>Obtain a boolean indicating whether the label is global or local.</p>
   */

  public boolean isGlobal()
  {
    return clientID!=0;
  }

  /**
   * <p>Overriding hashCode() and equals(), so that two EntityID objects that are
   * distinct yet have the same label are considered identical, allows an
   * EntityID to be used as a Map key.</p>
   */

  public int hashCode()
  {
    return identifier*13;
  }

  /**
   * <p>Overriding hashCode() and equals(), so that two EntityID objects that are
   * distinct yet have the same label are considered identical, allows an
   * EntityID to be used as a Map key.</p>
   */

  public boolean equals(Object object)
  {
    // Preconditions; none

    // Method proper

    if(!(object instanceof EntityID)) return false;
    if(((EntityID)object).identifier!=identifier) return false;
    if(((EntityID)object).clientID!=clientID) return false;

    return true;
  }

  /**
   * <p>Return a deep copy of the EntityID.</p>
   */

  public Object clone()
  {
    EntityID result=new EntityID(clientID);
    counter--;

    result.identifier=this.identifier;

    return result;
  }

  /**
   * <p>Return a String representation of the EntityID object.</p>
   */

  public String toString()
  {
    return "EntityID:"+(isGlobal()?"global":"local")+":"+identifier+
      "/"+clientID;
  }
}
