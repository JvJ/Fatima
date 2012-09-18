package FAtiMA.NormativeComponent;

import FAtiMA.Core.AgentModel;
import FAtiMA.Core.conditions.Condition;
import FAtiMA.Core.goals.Goal;
import FAtiMA.Core.goals.InterestGoal;
import FAtiMA.Core.wellFormedNames.Name;
import FAtiMA.Core.wellFormedNames.Symbol;
import FAtiMA.DeliberativeComponent.DeliberativeComponent;

public class Prohibition extends Norm{
	
	public Prohibition(Name name){
		super(name);
	}
	
	public Object clone(){
		Prohibition norm = new Prohibition(super.getName());
		norm.setHolder(super.getHolder());
		if(super.getTargets() != null){
			for(Role r : super.getTargets()){
				norm.getTargets().add((Role) r.clone());
			}
		}
		if(super.getActivationConditions() != null){
			for(Condition c : super.getActivationConditions()){
				norm.getActivationConditions().add((Condition) c.clone());
			}
		}
		if(super.getExpirationConditions() != null){
			for(Condition c : super.getExpirationConditions()){
				norm.getExpirationConditions().add((Condition) c.clone());
			}
		}
		if(super.getExceptionConditions() != null){
			for(Condition c : super.getExceptionConditions()){
				norm.getExceptionConditions().add((Condition) c.clone());
			}
		}
		if(super.getNormativeConditions() != null){
			for(Condition c : super.getNormativeConditions()){
				norm.getNormativeConditions().add((Condition) c.clone());
			}
		}
		if(super.getRewards() != null){
			for(Condition c : super.getRewards()){
				norm.getRewards().add((Condition) c.clone());
			}
		}
		if(super.getPunishments() != null){
			for(Condition c : super.getPunishments()){
				norm.getPunishments().add((Condition) c.clone());
			}
		}
		if(super.getBeneficiaries() != null){
			for(Role r : super.getBeneficiaries()){
				norm.getBeneficiaries().add((Role) r.clone());
			}
		}
		if(super.getIssuers() != null){
			for(Role r : super.getIssuers()){
				norm.getIssuers().add((Role) r.clone());
			}
		}
		if(super.getEnforcers() != null){
			for(Role r : super.getEnforcers()){
				norm.getEnforcers().add((Role) r.clone());
			}
		}
		norm.setBaseSalience(this.getBaseSalience());
		norm.setSalience(this.getSalience());
		return norm;
	}
	
	public boolean equals(Object obj){
		if(obj instanceof Prohibition){
			Prohibition norm = (Prohibition) obj;
			return 
				super.getName().equals(norm.getName()) &&
				super.getHolder().equals(norm.getHolder());				
		}
		return false;
	}
	
	public boolean isViolated(AgentModel aM){
		return 
			!isTrue(aM);												  // The protected conditions are false
	}
	
	public boolean isFulfilled(AgentModel aM){
		return 
			(!isAgentATarget(getHolder(), aM) || isExpired(aM)) && 		  // The agent is no longer a target or the prohibition expired
		    isTrue(aM);     											  // The protected conditions remain true
	}
	
	public Goal generateGoal(AgentModel aM){
		InterestGoal interestGoal = new InterestGoal(this.getName());
		for(Condition condition : this.getNormativeConditions()){
			interestGoal.AddCondition("ProtectionConstraint", condition);
		}
		return interestGoal;
	}
	
	public void removeInterestGoal(AgentModel aM){
		DeliberativeComponent dc = (DeliberativeComponent)aM.getComponent(DeliberativeComponent.NAME);
		dc.removeGoal(this.getName().getFirstLiteral().getName());
		dc.resetAllIntentions(aM);
	}
	
	public void processFulfillment(AgentModel aM){
		if(this.getHolder().equals((Name)new Symbol(FAtiMA.Core.util.Constants.SELF_STRING))){
			this.removeInterestGoal(aM);
		}
	}
	
	public void processViolation(AgentModel aM){
		if(this.getHolder().equals((Name)new Symbol(FAtiMA.Core.util.Constants.SELF_STRING))){
			this.removeInterestGoal(aM);
		}
	}
	
}
