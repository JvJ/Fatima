package FAtiMA.Core.componentTypes;

import java.util.ArrayList;

import FAtiMA.Core.AgentCore;
import FAtiMA.Core.AgentModel;
import FAtiMA.Core.perceptions.PropertyPerception;
import FAtiMA.Core.sensorEffector.Event;

public interface IAdvancedPerceptionsComponent extends IComponent{
	
	public void propertyChangedPerception(AgentModel am, PropertyPerception p);
	
	public void lookAtPerception(AgentCore ag, String subject, String target);
	
	public void entityRemovedPerception(AgentModel am, String entity);
	
	public void actionFailedPerception(AgentModel am, Event e);
	
	/** perception added by Henrique Reis to handle ToM Lvl 2+ **/
	public void otherAgentPerception(AgentModel ag, String target, ArrayList<String> agents);
	
}
