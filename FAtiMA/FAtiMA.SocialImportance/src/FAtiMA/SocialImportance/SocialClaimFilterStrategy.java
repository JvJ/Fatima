package FAtiMA.SocialImportance;

import FAtiMA.Core.AgentModel;
import FAtiMA.Core.plans.IPlanningOperator;
import FAtiMA.Core.sensorEffector.Event;
import FAtiMA.Core.sensorEffector.Parameter;
import FAtiMA.Core.wellFormedNames.Symbol;
import FAtiMA.DeliberativeComponent.IActionFilterStrategy;

public class SocialClaimFilterStrategy implements IActionFilterStrategy{
	
	
	SocialImportanceComponent siComponent;
	
	public SocialClaimFilterStrategy(SocialImportanceComponent siComponent){
		this.siComponent = siComponent;
	}

	
	@Override
	public boolean filterActionFromPlan(AgentModel am, IPlanningOperator step) {
		
		if(step.isGrounded() == false){
			return true;
		}
		
		String agent = step.getAgent().getName();
		String actionName = step.getName().getFirstLiteral().getName();
		String target = step.getName().getLiteralList().get(1).getName();
			
		Event simulatedEvent = new Event(agent,actionName,target);
		
		for(int i = 2; i < step.getName().getLiteralList().size(); i++){
			Parameter param = new Parameter("param", step.getName().getLiteralList().get(i).getName());
			simulatedEvent.AddParameter(param);
		}
		
		for(SIClaimRule rule : this.siComponent.getSIClaimRules()){
			
			if(rule.MatchEvent(simulatedEvent)){
				
				float valueClaimed = rule.getValue();
		
				AgentModel perspective = am.getModelToTest(new Symbol(simulatedEvent.GetTarget()),null);
				float existingSIValue = SocialImportanceRelation.getRelation("SELF", am.getName()).getValue(perspective.getMemory());
				if(valueClaimed > existingSIValue){
					return false;
				}
			}
		}		
		return true;
	}
	
}
