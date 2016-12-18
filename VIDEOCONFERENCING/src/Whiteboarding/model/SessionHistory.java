package Whiteboarding.model;

import java.io.Serializable;

import java.util.Vector;

/**
 * <p>SessionHistory stores the 'history' - that is, a
 * chronologically ordered set of the ChangeGroups which have been applied
 * (since the client joined this session or since the
 * session was created).</p>
 * @author David Morgan
 */

public class SessionHistory implements Serializable
{
  static final long serialVersionUID = 5142134529785288466L;

  // 2. Instance variable field declarations

  /**
   * <p>Vector of ChangeGroup objects comprising the history.</p>
   */

  private Vector history;

  // 7. Instance constructor declarations

  /**
   * <p>Create an empty SessionHistory.</p>
   */

  public SessionHistory()
  {
    history=new Vector();
  }

  // 9. Instance method declarations

  /**
   * <p>Add a {@link ChangeGroup} to the history. Only changes with timestamps
   * after or equal to the last stamp, or before or equal to the first timestamp,
   * will be accepted. Other changes will cause an exception if assertions are
   * enabled.</p>
   * @param change the {@link ChangeGroup} to add to the history
   */

  public void addChangeGroup(ChangeGroup change)
  {
    // Preconditions; change is not null, identifiers are global

    assert(change!=null);

    EntityID[][] identifier=change.getEntityIDArrays();

    for(int x=0; x!=identifier.length; x++)
    {
      for(int y=0; y!=identifier[x].length; y++)
      {
	assert(identifier[x][y].isGlobal());
      }
    }

    // Method proper

    // Try adding to an empty history; return if successful

    if(history.size()==0)
    {
      history.add(change);
      return;
    }

    // Try adding to the end; return if successful
    
    ChangeGroup last=(ChangeGroup)history.get(history.size()-1);
    if(last.getTimeStamp()<=change.getTimeStamp())
    {
      history.add(change);
      return;
    }

    // Try adding to the beginning; return if successful
	
    ChangeGroup first=(ChangeGroup)history.get(0);
    if(change.getTimeStamp()<=last.getTimeStamp())
    {
      history.add(0, change);
      return;
    }

    // Could not add; raise an exception if assertions are enabled

    assert(false);
  }

  /**
   * <p>Obtain the {@link ChangeGroup} at (or directly before/after, depending
   * on the 'direction' parameter) the specified timestamp.</p>
   * @param timeStamp the number of milliseconds since session initialization
   * @param direction whether to search forward in time
   * @return the {@link ChangeGroup}, or null if it does not exist
   */

  public ChangeGroup getChangeGroup(int timeStamp, boolean direction)
  {
    // Preconditions; timeStamp is positive or zero

    assert(timeStamp>=0);

    // Method proper 

    if(history.size()==0) return null;

    // Search forwards

    if(direction)
    {
      int index=0;

      while(((ChangeGroup)history.get(index)).getTimeStamp()<timeStamp)
      {
	index++;
	if(index>=history.size()) return null;
      }
      
      return (ChangeGroup)history.get(index);
    }
    else // Search backwards
    {
      int index=history.size()-1;

      while(((ChangeGroup)history.get(index)).getTimeStamp()>timeStamp)
      {
	index--;
	if(index<0) return null;
      }
      
      return (ChangeGroup)history.get(index);
    }
  }

  /**
   * <p>Obtain the {@link ChangeGroup} from directly after a specified
   * ChangeGroup.</p>
   * @param changeGroup a reference {@link ChangeGroup}
   * @return the {@link ChangeGroup} following the reference ChangeGroup,
   * or null if it does not exist
   */

 /* public ChangeGroup getNext(ChangeGroup changeGroup)
  {
    // Preconditions; changeGroup not null

    assert(changeGroup!=null);

    // Method proper

    return getChangeGroup(changeGroup.getTimeStamp()+1, true);
  }
*/
  /**
   * <p>Obtain the {@link ChangeGroup} from directly before a specified
   * ChangeGroup.</p>
   * @param changeGroup a reference {@link ChangeGroup}
   * @return the {@link ChangeGroup} preceding the reference ChangeGroup,
   * or null if it does not exist
   */

  /*public ChangeGroup getPrevious(ChangeGroup changeGroup)
  {
    // Preconditions; changeGroup not null

    assert(changeGroup!=null);

    // Method proper

    return getChangeGroup(changeGroup.getTimeStamp()-1, false);
  }
*/
  /**
   * <p>Alter a SessionState as required after a complete session load.</p>
   */

  public void initializeAfterLoad()
  {
    for(int i=0; i!=history.size(); i++)
    {
      ChangeGroup changeGroup=(ChangeGroup)history.get(i);

      changeGroup.initializeAfterLoad();
    }
  }

  /**
   * <p>Obtain a String representation of the history.</p>
   */

  public String toString()
  {
    StringBuffer result=new StringBuffer("SessionHistory containing "
					 +history.size()
					 +" ChangeGroup objects:");

    for(int i=0; i!=history.size(); i++)
    {
      result.append("\n"+history.get(i));
    }

    return new String(result);
  }
}
