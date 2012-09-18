package FAtiMA.ReactiveComponent;

import FAtiMA.Core.sensorEffector.Event;

public interface IReactionNode extends Cloneable {
	
	public Reaction getReaction(Event e);
	
	public Object clone();

}
