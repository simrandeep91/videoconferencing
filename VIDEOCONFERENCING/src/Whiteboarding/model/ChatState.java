package Whiteboarding.model;

import java.io.Serializable;

import java.text.DateFormat;

import java.util.Vector;
import java.util.Date;

/**
 * <p>Class representing the UserInfo Entity.</p>
 * @author David Morgan
 */

public class ChatState extends Entity implements Serializable, Cloneable
{
  static final long serialVersionUID = 2382993601105619054L;

  // 2. Instance variable field declarations

  /**
   * <p>Vector of Chat lines; each entry may be either a String or a
   * {@link Shortcut}.</p>
   */

  protected Vector chatLines;

  /**
   * <p>Vector of timestamps; each entry is an Integer indicating time
   * since session creation in milliseconds of the corresponding line
   * in chatLines.</p>
   */

  protected Vector timeStamps;

  // 4. Static member inner class declarations

  /**
   * <p>Subclass of {@link Entity.Change} providing extra operations on
   * ChatStates, and implementing existing operations differently where
   * necessary.</p>
   * <p>Note that Change.reverse() does not function exactly as it should
   * on ChatState changes. While it is possible to correctly reverse an
   * addition, it is not possible to correctly reverse a deletion. The
   * Change that is returned will add text to the end of the Chat, not
   * to the place from which it was deleted.</p>
   */ 

  public static class Change extends Entity.Change
  {
    static final long serialVersionUID = 2112205664404606570L;

    // 4.1 Static variable field declarations

    /**
     * <p>Indicates an operation that adds text to the Chat window.</p>
     *
     * <p>First and only parameter is a String giving the new text.</p>
     *
     * <p>First and only target specifies the ChatState to alter.</p>
     */

    public static final int SAY=15;

    /**
     * <p>Indicates an operation that adds a shortcut to the Chat window.</p>
     *
     * <p>First and only parameter is a Shortcut.</p>
     *
     * <p>First and only target specifies the ChatState to alter.</p>
     */

    public static final int ADD_SHORTCUT=16;

    /**
     * <p>Indicates an operation that deletes items with a particular
     * timeStamp from the Chat window.</p>
     *
     * <p>First and only parameter is an Integer timeStamp</p>
     *
     * <p>First and only target specifies the ChatState to alter.</p>
     */

    public static final int DELETE_CHAT=-5;

    // 4.7 Instance constructor declarations

    /**
     * <p>Change modifies a ChatState in a specified way.</p>
     */
   
    public Change(int changeType, Object[] parameter, EntityID[] target)
    {
      initialize(changeType, parameter, target);
    }

    // 4.9 Instance method declarations

    protected void initialize(int changeType, Object[] parameter,
			      EntityID[] target)
    {
      // Determine whether this change is handled here or by Entity.Change

      if(isParent(changeType))
      {
	super.initialize(changeType, parameter, target);
      }
      else
      {
	this.changeType=(byte)changeType;
	this.parameter=parameter;
	this.target=target;
	
	assert(isDataValid());
      }
    }

    /**
     * Determine whether a particular changeType should be handled by the parent
     * class.
     */

    private boolean isParent(int changeType)
    {
      switch(changeType)
      {
	case SAY:
	case ADD_SHORTCUT:
	case DELETE_CHAT:
	{
	  return false;
	}
	
	default:
	{
	  return true;
	}
      }
    }

    /**
     * <p>Validate the main data.</p>
     */

    protected boolean isDataValid()
    {
      switch(changeType)
      {
	case CREATE:
	{
	  return (parameter==null)&&
	    (target!=null)&&(target.length==1)&&(target[0]!=null);
	}

	case SAY:
	{
	  return (parameter!=null)&&(parameter.length==1)&&
	    (parameter[0] instanceof String)&&
	    (target!=null)&&(target.length==1)&&(target[0]!=null);
	}

	case ADD_SHORTCUT:
	{
	  return (parameter!=null)&&(parameter.length==1)&&
	    (parameter[0] instanceof Shortcut)&&
	    (target!=null)&&(target.length==1)&&(target[0]!=null);
	}

	case DELETE_CHAT:
	{
	  return (parameter!=null)&&(parameter.length==1)&&
	    (parameter[0] instanceof Integer)&&
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

	case SAY:
	case ADD_SHORTCUT:
	{
	  return (undoData!=null)&&(undoData.length==1)&&
	    (undoData[0] instanceof Integer);
	}

	case DELETE_CHAT:
	{
	  return (undoData!=null)&&(undoData.length==1)&&
	    ((undoData[0] instanceof String)||(undoData[0] instanceof Shortcut));
	}	  
          
	default:
	{
	  return false;
	}
      }
    }

    /**
     * <p>Apply the change. This must be overriden by subclasses of Entity.Change
     * that provide new functionality.</p>
     */

    public void apply(SessionState state, int timeStamp)
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

	  ChatState chatState=new ChatState(target[0], new Vector(),
					    new Vector());
	  state.addEntity(chatState);
	  break;
	}

	case SAY:
	{
	  ChatState chatState=(ChatState)state.getEntity(target[0]);

	  // Gather data for undo

	  undoData=new Object[] { new Integer(timeStamp) };

	  // Make the change

	  chatState.chatLines.add((String)parameter[0]);
	  chatState.timeStamps.add(new Integer(timeStamp));

	  break;
	}

	case ADD_SHORTCUT:
	{
	  ChatState chatState=(ChatState)state.getEntity(target[0]);

	  // Gather data for undo

	  undoData=new Object[] { new Integer(timeStamp) };

	  // Make the change

	  chatState.chatLines.add((Shortcut)parameter[0]);
	  chatState.timeStamps.add(new Integer(timeStamp));

	  break;
	}

	case DELETE_CHAT:
	{
	  ChatState chatState=(ChatState)state.getEntity(target[0]);

	  // Gather data for undo

	  // Handled during change

	  // Make the change

	  System.out.println(chatState);

	  for(int i=0; i!=chatState.timeStamps.size(); i++)
	  {
	    if(timeStamp==((Integer)chatState.timeStamps.get(i)).intValue())
	    {
	      undoData=new Object[] { chatState.chatLines.get(i) };

	      chatState.timeStamps.remove(i);
	      chatState.chatLines.remove(i);

	      break;
	    }
	  }

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
     * <p>Undo the change. This must be overriden by subclasses of Entity.Change
     * that provide new functionality.</p>
     */

    public void undo(SessionState state, int timeStamp)
    {
      // Preconditions; supplied state not null, undo data valid

      assert(state!=null);
      assert(isUndoDataValid());

      // Method proper

      switch(changeType)
      {
	case CREATE:
	{
	  state.removeEntity(target[0]);
	  break;
	}

	case SAY:
	case ADD_SHORTCUT:
	{
	  ChatState chatState=(ChatState)state.getEntity(target[0]);
 
	  for(int i=0; i!=chatState.timeStamps.size(); i++)
	  {
	    if(timeStamp==((Integer)chatState.timeStamps.get(i)).intValue())
	    {
	      chatState.timeStamps.remove(i);
	      chatState.chatLines.remove(i);

	      i=-1;
	    }
	  }

	  break;
	}

	case DELETE_CHAT:
	{
	  ChatState chatState=(ChatState)state.getEntity(target[0]);

	  chatState.chatLines.add(undoData[0]);
	  break;
	}
      
	default:
	{
	  assert(false);
	}
      }
    }

    /**
     * <p>Obtain a new Change representing the reverse of the operation. Intended
     * to be used during undo, so an undo operation is still a new operation.</p>
     */

    public Entity.Change reverse(int timeStamp)
    {
      // Preconditions

      assert(isUndoDataValid());

      // Method proper

      switch(changeType)
      {
	case CREATE:
	{
	  return new Entity.Change(DELETE, undoData, target);
	}

	case SAY:
	case ADD_SHORTCUT:
	{
	  return new ChatState.Change(DELETE_CHAT, new Object[]
	      { new Integer(timeStamp) }, target);
	}

	case DELETE_CHAT:
	{
	  if(undoData[0] instanceof String)
	  {
	    return new ChatState.Change(SAY, undoData, target);
	  }
	  else
	  {
	    return new ChatState.Change(ADD_SHORTCUT, undoData, target);
	  }
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
     * <p>Entity.acceptedByServer() is overriden to provide a method that
     * adds time indicators to chat lines.</p>
     */
    
    public void acceptedByServer()
    {
      if(changeType==SAY)
      {
	parameter[0]=DateFormat.getTimeInstance(
	  DateFormat.SHORT).format(
	    new Date())+" "+((String)parameter[0]);
      }
    }

    /**
     * <p>Obtain a String representation of the Change.</p>
     * @return a String describing the effect of applying this Change
     */

    public String toString()
    {
      if(isParent(changeType))
      {
	return "ChatState.Change:"+super.toString();
      }
      else
      {
	switch(changeType)
	{
	  case CREATE:
	  {
	    return "ChatState.Change:CREATE:"+target[0];
	  }

	  case SAY:
	  {
	    return "ChatState.Change:SAY:"+target[0]+":"
	      +parameter[0];
	  }

	  case ADD_SHORTCUT:
	  {
	    return "ChatState.Change:ADD_SHORTCUT:"+target[0]+":"
	      +parameter[0];
	  }
	  
	  default:
	  {
	    return "ChatState.Change:UNKNOWN";
	  }
	}
      }	
    }
  }

  // 7. Instance constructor declarations

  /**
   * <p>Create a new ChatState with the specified identifier, Vector of
   * String chat lines, and Vector of Integer timeStamps.</p>
   */

  protected ChatState(EntityID identifier, Vector chatLines, 
		    Vector timeStamps)
  {
    super(identifier);

    this.chatLines=chatLines;
    this.timeStamps=timeStamps;

    assert(isChatDataValid());
  }

  // 9. Instance method declarations

  /**
   * <p>Validate the instance data.</p>
   */

  protected boolean isChatDataValid()
  {
    if(!isEntityDataValid()) return false;

    if(!((chatLines!=null)&&(timeStamps!=null)&&
      (chatLines.size()==timeStamps.size()))) return false;

    for(int i=0; i!=timeStamps.size(); i++)
    {
      if(!(timeStamps.get(i) instanceof Integer)) return false;
      if(!((chatLines.get(i) instanceof String)||
	   (chatLines.get(i) instanceof Shortcut))) return false;
    }

    return true;
  }

  /**
   * <p>Obtain a Vector of Strings and {@link Shortcuts} representing
   * consecutive lines of chat and/or Shortcuts inserted into the chat.</p>
   */

  public Vector getChatLines()
  {
    return chatLines;
  }

  /**
   * <p>Obtain a Vector of Integers giving the timestamp for each line of
   * chat, in milliseconds since the session began.</p>
   */

  public Vector getChatTimestamps()
  {
    return timeStamps;
  }

  /**
   * <p>Create a deep copy of the ChatState.</p>
   */

  public Object clone()
  {
    ChatState result=(ChatState)super.clone();

    return result;
  }

  /**
   * <p>Obtain a String representation of the ChatState.</p>
   */

  public String toString()
  {
    return "ChatState:("+super.toString()+"):"
      +chatLines+":"
      +timeStamps;
  }
}
