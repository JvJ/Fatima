package FAtiMA.Core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import FAtiMA.Core.componentTypes.IComponent;
import FAtiMA.Core.conditions.Condition;
import FAtiMA.Core.emotionalState.AppraisalFrame;
import FAtiMA.Core.emotionalState.EmotionalState;
import FAtiMA.Core.goals.GoalLibrary;
import FAtiMA.Core.memory.Memory;
import FAtiMA.Core.sensorEffector.Event;
import FAtiMA.Core.sensorEffector.RemoteAgent;
import FAtiMA.Core.wellFormedNames.Symbol;

public interface AgentModel {
	
	public ActionLibrary getActionLibrary();
	
	public HashMap<String,String> getActionsVisibility();
	
	public IComponent getComponent(String name);
	
	public Collection<IComponent> getComponents(); 
	
	public EmotionalState getEmotionalState();
	
	public GoalLibrary getGoalLibrary();
	
	public Memory getMemory();
	
	public AgentModel getModelToTest(Symbol ToM, Condition cond);
	
	//public AgentModel getModelTestedAgainstPropVis(Symbol ToM, String propName);
	
	public AgentModel getModelToTestCondition(Condition cond);
	
	public String getName();
	
	public RemoteAgent getRemoteAgent();
	
	/* *************************************************/
	
	public boolean isSelf();
	
	public ArrayList<String> predecessorMinds();
	
	public void setActionVisibility(String actionName, String visibility);
	
	public void setModelStrategy(IGetModelStrategy strat);
	
	public EmotionalState simulateEmotionalState(Event ficticiousEvent,IComponent callingComponent);
	
	public void updateEmotions(AppraisalFrame af);
	
	
}
