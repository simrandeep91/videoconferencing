package Whiteboarding.model;

import java.io.Serializable;

/**
 * <p>SessionModel stores a {@link SessionHistory} object and a
 * 'base' {@link SessionState} object. The SessionState object is a snapshot 
 * of the whiteboard as it was just before the timestamp of the first entry in
 * the SessionHistory.</p>
 *
 * <p>From this information, additional SessionState objects may be produced, to
 * represent snapshots of the whiteboard at any moment in its lifetime. These
 * objects may also be moved back and fore in time, through the
 * SessionHistory.</p>
 * 
 * <p>The SessionModel may receive new Changes from the network, which will
 * be appended to its SessonHistory. It may also be serialized, allowing for
 * saving/restoring of whiteboard Sessions.</p>
 */

public class SessionModel implements Serializable
{
  static final long serialVersionUID = 937965098487237083L;

  // 2. Instance variable field declarations

  /**
   * <p>{@link SessionState} object storing the base state.</p>
   */

  private SessionState baseState;

  /**
   * <p>{@link SessionState} object storing the latest state.</p>
   */

  private SessionState latestState;

  /**
   * <p>{@link SessionHistory} object storing the session history.</p>
   */

  private SessionHistory history;
 
  // 7. Instance constructor definitions

  /**
   * Create a SessionModel with the specified 'base' SessionState
   * and an empty SessionHistory.
   */

  public SessionModel(SessionState baseState)
  {
    // Preconditions; baseState not null

    assert(baseState!=null);

    // Method proper

    this.baseState=baseState;
    this.latestState=(SessionState)baseState.clone();
    history=new SessionHistory();
  }

  // 9. Instance method declarations

  /**
   * <p>Add a {@link ChangeGroup} representing a state change to the contained
   * SessionHistory. The ChangeGroup is expected to have a time stamp either
   * before all known history events or after all known history events. An
   * exception will be thrown if this is not the case and assertions are
   * enabled.</p>
   * @param changeGroup a {@link ChangeGroup} object representing the change to
   * add
   */

  public void addChangeGroup(ChangeGroup changeGroup)
  {
    // Preconditions; changeGroup not null

    assert(changeGroup!=null);

    // Method proper

    history.addChangeGroup(changeGroup);

    if(changeGroup.getTimeStamp()>=latestState.getTimeStamp())
    {
      changeGroup.apply(latestState);
      latestState.setTimeStamp(changeGroup.getTimeStamp());
    }
  }

  /**
   * <p>Obtain a {@link SessionState} object representing the Whiteboarding.main.jinn.at the
   * specified timestamp.</p>
   * @param timeStamp the number of milliseconds since session initialization
   * @return a {@link SessionState} representing the state at the specified
   * time
   */

  public SessionState getSessionState(int timeStamp)
  {
    // Preconditions; timeStamp positive

    assert(timeStamp>=0);

    // Method proper

    SessionState result=(SessionState)baseState.clone();

    timeShiftSessionState(result, timeStamp);
    return result;
  }

  /**
   * <p>Move a SessionState to a different timestamp.</p>
   */

  public SessionState timeShiftSessionState(SessionState current, int timeStamp)
  {
    // Preconditions; current not null, timeStamp positive

    assert((current!=null)&&(timeStamp>=0));

    // Method proper

    if(current.getTimeStamp()<=timeStamp)
    {
      while(current.getTimeStamp()<=timeStamp)
      {
	ChangeGroup changeGroup=history.getChangeGroup(current.getTimeStamp()+1,
						       true);
	if((changeGroup==null)||(changeGroup.getTimeStamp()>timeStamp)) break;

	changeGroup.apply(current);
	current.setTimeStamp(changeGroup.getTimeStamp());
      }
      current.setTimeStamp(timeStamp);
      return current;
    }
    else
    {
      while(current.getTimeStamp()>timeStamp)
      {
	ChangeGroup changeGroup=history.getChangeGroup(current.getTimeStamp()-1,
						       false);

	if((changeGroup==null)||(changeGroup.getTimeStamp()<timeStamp)) break;

	changeGroup.undo(current);
	current.setTimeStamp(changeGroup.getTimeStamp());
      }
      current.setTimeStamp(timeStamp);
      return current;
    }
  }

  /**
   * <p>Obtain the timestamp of the latest change.</p>
   */

  public int getLatestTimeStamp()
  {
    return latestState.getTimeStamp();
  }

  /**
   * <p>Alter a SessionModel as required after a complete load.</p>
   */

  public void initializeAfterLoad()
  {
    if(baseState!=null) baseState.initializeAfterLoad();
    if(latestState!=null) latestState.initializeAfterLoad();
    if(history!=null) history.initializeAfterLoad();
  }

  /**
   * <p>Obtain a String representation of the SessionModel.</p>
   */

  public String toString()
  {
    return "SessionModel containing SessionHistory:\n"
      +history.toString()+"\n"
      +"SessionModel also contains SessionState:\n"
      +baseState.toString();
  }
}
