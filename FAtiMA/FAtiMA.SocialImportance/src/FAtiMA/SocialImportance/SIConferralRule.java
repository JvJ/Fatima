package FAtiMA.SocialImportance;

import FAtiMA.Core.sensorEffector.Event;

public class SIConferralRule {
	private static final long serialVersionUID = 1L;
	
	protected float _valueOfConferral;
	protected Event _event;

	
	public SIConferralRule(String actionName, String parameters, float valueOfConferral){
		this._valueOfConferral = valueOfConferral;
		_event = Event.ParseEvent("*", actionName, "*", parameters);
		//this is a trick just to save time, replacing [SELF] by SELF
		_event.applyPerspective("[SELF]");		
	}
	
	public float getValue(){
		return _valueOfConferral;
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
