package FAtiMA.ToM;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import FAtiMA.Core.AgentCore;
import FAtiMA.Core.AgentModel;
import FAtiMA.Core.IGetModelStrategy;
import FAtiMA.Core.Display.AgentDisplayPanel;
import FAtiMA.Core.componentTypes.IAdvancedPerceptionsComponent;
import FAtiMA.Core.componentTypes.IAppraisalDerivationComponent;
import FAtiMA.Core.componentTypes.IComponent;
import FAtiMA.Core.componentTypes.IModelOfOtherComponent;
import FAtiMA.Core.componentTypes.IProcessExternalRequestComponent;
import FAtiMA.Core.emotionalState.AppraisalFrame;
import FAtiMA.Core.goals.ActivePursuitGoal;
import FAtiMA.Core.memory.semanticMemory.KnowledgeSlot;
import FAtiMA.Core.perceptions.PropertyPerception;
import FAtiMA.Core.plans.Plan;
import FAtiMA.Core.sensorEffector.Event;
import FAtiMA.Core.util.Constants;
import FAtiMA.Core.util.parsers.ReflectXMLHandler;
import FAtiMA.Core.wellFormedNames.Name;
import FAtiMA.Core.wellFormedNames.Symbol;
import FAtiMA.DeliberativeComponent.DeliberativeComponent;
import FAtiMA.DeliberativeComponent.strategies.IGetUtilityForOthers;
import FAtiMA.OCCAffectDerivation.OCCAppraisalVariables;
import FAtiMA.ReactiveComponent.ReactiveComponent;

public class ToMComponent implements Serializable, IAppraisalDerivationComponent, IAdvancedPerceptionsComponent, IGetModelStrategy, IGetUtilityForOthers, IProcessExternalRequestComponent {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final String NAME = "ToM";
	
	protected String _name;
	protected HashMap<String,ModelOfOther> _ToM;
	protected ArrayList<String> _nearbyAgents;
	
	
	public ToMComponent(String agentName)
	{
		this._name = agentName;
		this._nearbyAgents = new ArrayList<String>();
		this._ToM = new HashMap<String,ModelOfOther>();
	}
	
	@Override
	public void actionFailedPerception(AgentModel am, Event e) {	
	}
	
	private void addNearbyAgent(String agent)
	{
		if(!this._nearbyAgents.contains(agent))
		{
			this._nearbyAgents.add(agent);
		}
	}
	
	@Override
	public void appraisal(AgentModel am, Event e, AppraisalFrame as) {
		
		Event e2 = e.RemovePerspective(_name);
		Event e3;
		float desirability;
		
		AppraisalFrame otherAF;
		
		for(String s : _nearbyAgents)
		{
			ModelOfOther m = _ToM.get(s);
			e3 = e2.applyPerspective(s);
			otherAF = new AppraisalFrame(e3);
			m.appraisal(e3, otherAF);
			m.updateEmotions(otherAF);
			
			desirability = otherAF.getAppraisalVariable(OCCAppraisalVariables.DESIRABILITY.name());
			if(desirability != 0)
			{
				as.SetAppraisalVariable(NAME, 
						(short)7,
						OCCAppraisalVariables.DESFOROTHER.name()+s,
						otherAF.getAppraisalVariable(OCCAppraisalVariables.DESIRABILITY.name()));
			} 
		}
	}

	@Override
	public AgentDisplayPanel createDisplayPanel(AgentModel am) {
		return new ToMPanel(this);
	}

	@Override
	public void entityRemovedPerception(AgentModel am, String entity) {
		_nearbyAgents.remove(entity);
	}
	
	@Override
	public ReflectXMLHandler getActionsParser(AgentModel am) {
		return null;
	}
	
	@Override
	public String[] getComponentDependencies() {
		String[] dependencies = {ReactiveComponent.NAME,DeliberativeComponent.NAME};
		return dependencies;
	}

	@Override
	public ReflectXMLHandler getGoalsParser(AgentModel am) {
		return null;
	}

	public AgentModel getModel(Symbol ToM)
	{
		if(_ToM.containsKey(ToM.toString()))
		{
			return _ToM.get(ToM.toString());
		}
		else return null;
	}
	
	@Override
	public ReflectXMLHandler getPersonalityParser(AgentModel am) {
		return null;
	}

	public HashMap<String,ModelOfOther> getToM()
	{
		return this._ToM;
	}
	
	@Override
	public float getUtilityForOthers(AgentModel am, ActivePursuitGoal g) {
		DeliberativeComponent dp = (DeliberativeComponent) am.getComponent(DeliberativeComponent.NAME);
		
		float utility = 0;
		
		for(ModelOfOther m : _ToM.values())
		{
			utility+= dp.getUtilityStrategy().getUtility(m, g);
		}
		
		return utility;
	}

	@Override
	public void initialize(AgentModel am) {
		DeliberativeComponent dc = (DeliberativeComponent) am.getComponent(DeliberativeComponent.NAME);
		am.setModelStrategy(this);
		Plan.setPlannerUnifierStrategy(new PlannerUnifierStrategy());
		dc.setUtilityForOthersStrategy(this);
	}

	private void initializeModelOfOther(AgentCore ag, String name)
	{
		if(!_ToM.containsKey(name))
		{
			ModelOfOther model = new ModelOfOther(name, ag);
			for(IComponent c : ag.getComponents())
			{
				if(c instanceof IModelOfOtherComponent)
				{
					IComponent componentOfOther = ((IModelOfOtherComponent)c).createModelOfOther();
					if(componentOfOther != null)
					{
						model.addComponent(componentOfOther);
					}
				}
			}
			updateMemoryOfOtherWithTargetInfo(ag, model, name);
			
			_ToM.put(name, model);
		}
	}
	
	@Override
	public void inverseAppraisal(AgentModel am, AppraisalFrame af) {
	}

	private boolean isPerson(AgentCore ag, String agent)
	{
		Name isPerson = Name.ParseName(agent + "(isPerson)");
		return ag.getMemory().getSemanticMemory().AskPredicate(isPerson);
	}

	@Override
	public void lookAtPerception(AgentCore ag, String subject, String target) {
		
		if(subject.equals(Constants.SELF_STRING))
		{
			if(!target.equals(Constants.SELF_STRING))
			{
				if(isPerson(ag, target))
				{
					initializeModelOfOther(ag, target);
					addNearbyAgent(target);
				}
			}
			return;
		}
		
		if(!subject.equals(target))
		{
			for(String other : _nearbyAgents)
			{	
				if(other.equals(subject))
				{
					ModelOfOther m = _ToM.get(other);
					updateMemoryOfOtherWithTargetInfo(ag,m,target);		
				}	
			}
		}
	}

	@Override
	public String name() {
		return ToMComponent.NAME;
	}

	@Override
	public void otherAgentPerception(AgentModel ag, String target,ArrayList<String> agents) {		
	}

	@Override
	public void parseAdditionalFiles(AgentModel am) {	
	}

	@Override
	public void processExternalRequest(AgentModel am, String msgType, String perception) {
		
		for(String s: _nearbyAgents)
		{
			ModelOfOther m = _ToM.get(s);
			m.processExternalRequest(msgType, perception);
		}
	}
	
	@Override
	public void propertyChangedPerception(AgentModel am, PropertyPerception p) 
	{
		
		Symbol ToM = p.getToM().get(0);
		Boolean persistent = p.getPersistent();
		
		PropertyPerception propertyWithoutPerspective = p.clone();
		propertyWithoutPerspective.removePerspective(_name);
		
		PropertyPerception propertyWithNewPerspective;
			
		if(ToM.equals(Constants.UNIVERSAL_SYMBOL))
		{
			for(String other : _nearbyAgents)
			{
				propertyWithNewPerspective = propertyWithoutPerspective.clone();
				propertyWithNewPerspective.applyPerspective(other);
				
				
				ModelOfOther m = _ToM.get(other);
				m.getMemory().getSemanticMemory().Tell(persistent,propertyWithNewPerspective.getProperty(), propertyWithNewPerspective.getValue());
				m.propertyChangedPerception(propertyWithNewPerspective);
			}
		}
		else if(!ToM.toString().equals(_name))
		{
			ModelOfOther m = _ToM.get(ToM);
			if(m != null)
			{
				propertyWithNewPerspective = propertyWithoutPerspective.clone();
				propertyWithNewPerspective.applyPerspective(ToM.toString());
				
				m.getMemory().getSemanticMemory().Tell(persistent,propertyWithNewPerspective.getProperty(), propertyWithNewPerspective.getValue());
				m.propertyChangedPerception(propertyWithNewPerspective);
			}
		}	
	}

	@Override
	public AppraisalFrame reappraisal(AgentModel am) {
		return null;
	}

	public void removeNearByAgent(String entity)
	{
		this._nearbyAgents.remove(entity);
	}

	@Override
	public void reset() {
		// TODO Auto-generated method stub
	}

	@Override
	public void update(AgentModel am, Event e)
	{
		Event e2 = e.RemovePerspective(_name);
		Event e3;
		for(String s : _nearbyAgents)
		{
			ModelOfOther m = _ToM.get(s);
			e3 = e2.applyPerspective(s);
			m.update(e3);
		}		
	}

	@Override
	public void update(AgentModel am,long time) {
		
		for(String s : _nearbyAgents)
		{
			ModelOfOther m = _ToM.get(s);
			m.update(time);
		}		
	}

	private void updateMemoryOfOtherWithTargetInfo(AgentModel am, ModelOfOther m, String target)
	{
		Name propertyName;
		String valueName;
		
		
		KnowledgeSlot knownInfo;
		knownInfo = am.getMemory().getSemanticMemory().GetObjectDetails(target);
		if(knownInfo!= null)
		{	
			for(KnowledgeSlot property : knownInfo.getLeafs())
			{
				propertyName = Name.ParseName(property.getDisplayName());
				propertyName.removePerspective(am.getName());
				propertyName.applyPerspective(m.getName());
				
				valueName = property.getValue().toString();
			
				valueName = Name.removePerspective(valueName, am.getName());
				valueName = Name.applyPerspective(valueName, m.getName());
				
				m.getMemory().getSemanticMemory().Tell(property.getPersistent(),propertyName, valueName);
				PropertyPerception p = new PropertyPerception(property.getPersistent(),
						null,
						Constants.UNIVERSAL_SYMBOL.toString(),
						propertyName, 
						valueName);
				m.propertyChangedPerception(p);
			}
		}
	}

	@Override
	public void validate(AgentModel am) throws Exception {
		// TODO Auto-generated method stub
		
	}
}
