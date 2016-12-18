package Whiteboarding.model;

import java.io.Serializable;

/**
 * <p>Entity is the base class for items of global state.</p>
 * @author David Morgan
 */

public class Entity implements Serializable, Cloneable
{
  static final long serialVersionUID = 6201984608805867653L;

  // 2. Instance variable field declarations

  /**
   * <p>The Entity's EntityID. Initially, this will be used to identify it
   * locally. When an Entity creation has passed via the server it will become
   * a new global ID.</p>
   */

  protected EntityID identifier;

  // 4. Static member inner class declarations

  /**
   * <p>An instance of Change represents a creation, modification or deletion of
   * an Entity. Entity.Change only provides facilities for changing Entity
   * instances. Subclasses of Entity.Change provide further functionality.</p>
   * @author David Morgan
   */

  public static class Change implements Serializable
  {
    static final long serialVersionUID = 1146801957045304751L;

    // 4.1. Static variable field declarations

    /**
     * <p>Indicates an operation that creates an Entity.</p>
     *
     * <p>If parameter is null, a default Entity is created; all the parameters
     * are default or zeroed. Otherwise, the first and only parameter contains
     * the full Entity to be created.</p>
     *
     * <p>The first and only target specifies the {@link EntityID} the new
     * Entity is to be created with.</p>
     */

    public static final int CREATE=0;

    /**
     * <p>Indicates an operation that deletes an Entity.</p>
     *
     * <p>Takes no parameters. First and only target Entity is deleted.</p>
     */

    public static final int DELETE=1;

    /**
     * <p>Indicates an operation that pastes an Entity into the project.</p>
     *
     * <p>Takes one parameter, the Entity to paste. First and only target
     * specifies the {@link EntityID} the new Entity is to be created with.</p>
     */

    public static final int PASTE=-1;

    // 4.2. Instance variable declarations

    /**
     * <p>Byte indicating the type of change encoded by this instance.</p>
     */

    protected byte changeType;

    /**
     * <p>Array of Objects giving the parameters for this change.</p>
     */

    protected Object[] parameter;

    /**
     * <p>Array of {@link EntityID} objects giving the target Entity objects
     * for this change.</p>
     */

    protected EntityID[] target;

    /**
     * <p>Array of Objects containing all data necessary to undo this change.</p>
     */

    protected Object[] undoData;

    // 4.7. Instance constructor declarations

    /**
     * <p>Protected empty constructor.</p>
     */

    protected Change() {}

    /**
     * <p>Change modifies an Entity or array of Entities in a specified way;
     * additional changeType identifiers will be added by subclasses.</p>
     */
   
    public Change(int changeType, Object[] parameter, EntityID[] target)
    {
      initialize(changeType, parameter, target);
    }

    // 4.9 Instance method declarations

    /**
     * <p>Initialize a new Entity.Change.</p>
     */
    
    protected void initialize(int changeType, Object[] parameter,
			      EntityID[] target)
    {
      this.changeType=(byte)changeType;
      this.parameter=parameter;
      this.target=target;

      assert(isDataValid());
    }

    /**
     * Validate the main data.
     */

    protected boolean isDataValid()
    {
      switch(changeType)
      {
	case CREATE:
	{
	  return (parameter==null)&&(target!=null)&&(target.length==1)
	    &&(target[0]!=null);
	}

	case DELETE:
	{
	  return (parameter==null)&&(target!=null)&&(target.length==1)
	    &&(target[0]!=null);
	}

	case PASTE:
	{
	  return (parameter!=null)&&(parameter.length==1)&&
	    (parameter[0]!=null)&&(parameter[0] instanceof Entity)&&
	    (target!=null)&&(target.length==1)&&(target[0]!=null);
	}

	default:
	{
	  return false;
	}
      }
    }

    /**
     * <p>Validate the undo data.</p>
     */

    protected boolean isUndoDataValid()
    {
      switch(changeType)
      {
	case CREATE:
	{
	  return undoData==null;
	}
	
	case DELETE:
	{
	  return (undoData!=null)&&(undoData[0]!=null)&&
	    (undoData[0] instanceof Entity);
	}

	case PASTE:
	{
	  return undoData==null;
	}

	default:
	{
	  return false;
	}
      }
    }

    /**
     * <p>Obtain an array of the EntityID objects used. This is provided so that
     * the server may update invalid EntityID's.</p>
     * @return an array of all EntityID objects referenced by this Change
     */

    public EntityID[] getEntityIDs()
    {
      return target;
    }

    /**
     * <p>Apply the change, specifying the time at which the change takes place. 
     * This must be overridden by subclasses of Entity.Change that provide new
     * functionality.</p>
     */

    public void apply(SessionState state, int timeStamp)
    {
      apply(state);
    }

    /**
     * <p>Apply the change. This must be overridden by subclasses of Entity.Change
     * that provide new functionality.</p>
     */

    public void apply(SessionState state)
    {
      // Preconditions; supplied state is not null

      assert(state!=null);

      // Method proper

      switch(changeType)
      {
	case CREATE:
	{
          // Gather data for undo

          // Make the change

	  Entity entity=new Entity(target[0]);
	  state.addEntity(entity);
	  break;
	}
	
	case DELETE:
	{
          // Gather data for undo

          undoData=new Object[] {state.getEntity(target[0]).clone()};

          // Make the change

	  state.removeEntity(target[0]);
	  break;
	}

	case PASTE:
	{
          // Gather data for undo

          // Make the change

          ((Entity)parameter[0]).setEntityID(target[0]);

	  state.addEntity((Entity)((Entity)parameter[0]).clone());
	  break;
	}

	default:
	{
	  assert(false);
	}
      }

      // Postconditions; undo data is valid

      assert(isUndoDataValid());
    }

    /**
     * <p>Apply the change, specifying the time at which the change takes place. 
     * This must be overriden by subclasses of Entity.Change that provide new
     * functionality.</p>
     */

    public void undo(SessionState state, int timeStamp)
    {
      undo(state);
    }

    /**
     * <p>Undo the change. This must be overriden by subclasses of Entity.Change
     * that provide new functionality.</p>
     */

    public void undo(SessionState state)
    {
      // Preconditions; supplied state not null, undo data valid

      assert(state!=null);
      assert(isUndoDataValid());

      // Method proper

      switch(changeType)
      {
	case CREATE:
        case PASTE:
	{
	  state.removeEntity(target[0]);
	  break;
	}
	
	case DELETE:
	{
          state.addEntity((Entity)((Entity)undoData[0]).clone());
	  break;
	}
       
	default:
	{
	  assert(false);
	}
      }
    }

    /**
     * <p>Obtain a new Change representing the reverse of the operation,
     * specifying the time at which the change takes place. Intended
     * to be used during undo, so an undo operation is still a new operation.</p>
     */

    public Entity.Change reverse(int timeStamp)
    {
      return reverse();
    }

    /**
     * <p>Obtain a new Change representing the reverse of the operation. Intended
     * to be used during undo, so an undo operation is still a new operation.</p>
     */

    public Change reverse()   //NEVER USED ACTUALLY
    {
      // Preconditions; undo data is valid

      assert(isUndoDataValid());

      // Method proper

      switch(changeType)
      {
	case CREATE:
        case PASTE:
	{
	  return new Entity.Change(DELETE, undoData, target);
	}

	case DELETE:
	{
	  return new Entity.Change(PASTE, undoData, target);
	}

	default:
	{
	  assert(false); return null;
	}
      }
    }

    /**
     * <p>Signal to a Change that it has been accepted by the server. Any
     * extra configuration that needs doing is done.</p>
     * <p>This implementation does nothing, but must always be called on
     * acceptance by the server because subclasses may use it.</p>
     */
    
    public void acceptedByServer() //Used by chat state to set the time in chat box
    {
    }

    /**
     * <p>Alter the Change to make it suitable after a session load operation.
     * This method has no effect, but should always be called after a session
     * load so subclasses of Entity.Change may use it as required.</p>
     */

    public void initializeAfterLoad()
    {
    }

    /**
     * <p>Obtain a String representation of the Change.</p>
     * @return a String describing the effect of applying this Change
     */

    public String toString()
    {
      switch(changeType)
      {
	case CREATE:
	{
	  return "Entity.Change:CREATE:"+target[0];
	}
	
	case DELETE:
	{
	  return "Entity.Change:DELETE:"+target[0];
	}

        case PASTE:
        {
          return "Entity.Change:PASTE:"+target[0];
        }

	default:
	{
	  return "Entity.Change:UNKNOWN";
	}
      }	
    }
  }

  // 7. Instance constructor declarations

  /**
   * <p>Create an Entity with a specified {@link EntityID}.</p>
   */

  protected Entity(EntityID identifier)
  {
    this.identifier=identifier;

    assert(isEntityDataValid());
  }

  // 9. Instance method declarations

  /**
   * <p>Validate the instance data.</p>
   */

  protected boolean isEntityDataValid()
  {
    return (identifier!=null);
  }

  /**
   * <p>Obtain the Entity's identifier. If the Entity has been admitted into
   * the global state, this is a global identifier. Otherwise, it may be
   * a local identifier.</p>
   * @return an EntityID identifying the Entity globally or locally
   */

  public EntityID getEntityID()
  {
    return identifier;
  }

  /**
   * <p>Set the Entity's identifier. If the Entity is to be admitted into the
   * global state, this should be a global identifier. Otherwise, a local
   * identifier may be used.</p>
   * @param identifier the EntityID to assign to this Entity
   */

  public void setEntityID(EntityID identifier)   // set globally when session calls the instance of entity to load it.
  {
    // Preconditions: identifier not null

    assert(identifier!=null);

    // Method proper

    this.identifier=identifier;
  }

  /**
   * <p>Create a deep copy of the Entity.</p>
   */

  public Object clone()
  {
    try
    {
      return super.clone();
    }
    catch(CloneNotSupportedException e)
    {
      assert(false); return null;
    }
  }

  /**
   * <p>Alter the Entity to make it suitable after a session load operation.
   * This method has no effect, but should always be called after a session
   * load so subclasses of Entity may use it as required.</p>
   */
  
  public void initializeAfterLoad()
  {
  }

  /**
   * <p>Return a String representation of the Entity.</p>
   */

  public String toString()
  {
    return "Entity:"+identifier;
  }
}
