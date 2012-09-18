package FAtiMA.SocialImportance;

import FAtiMA.Core.sensorEffector.Event;

public class SIClaimRule {
	private static final long serialVersionUID = 1L;
	
	protected float _valueOfClaim;
	protected Event _event;

	
	public SIClaimRule(String actionName, String parameters, float valueOfClaim){
		this._valueOfClaim = valueOfClaim;
		_event = Event.ParseEvent("*", actionName, "*", parameters);
		//this is a trick just to save time, replacing [SELF] by SELF
		_event.applyPerspective("[SELF]");		
	}
	
	public float getValue(){
		return _valueOfClaim;
	}

	public Event getEvent() {
		return _event;
	}
	
	public void setEvent(Event e){
		_event = e;
	}

	public boolean MatchEvent(Event eventPerception) {
		return Event.MatchEvent(_event, eventPerception);
	}
	
}
