package FAtiMA.NormativeComponent;

import java.util.ArrayList;
import java.util.Iterator;

import FAtiMA.Core.AgentModel;
import FAtiMA.Core.conditions.Condition;
import FAtiMA.Core.conditions.PropertyNotEqual;
import FAtiMA.Core.goals.ActivePursuitGoal;
import FAtiMA.Core.goals.Goal;
import FAtiMA.Core.plans.Effect;
import FAtiMA.Core.plans.IPlanningOperator;
import FAtiMA.Core.plans.Plan;
import FAtiMA.Core.wellFormedNames.Name;
import FAtiMA.Core.wellFormedNames.Substitution;
import FAtiMA.Core.wellFormedNames.Symbol;
import FAtiMA.Core.wellFormedNames.Unifier;
import FAtiMA.DeliberativeComponent.DeliberativeComponent;
import FAtiMA.DeliberativeComponent.Intention;

public class Obligation extends Norm{

	public Obligation(Name name) {
		super(name);
	}

	public Object clone(){
		Obligation norm = new Obligation(super.getName());
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
		if(obj instanceof Obligation){
			Obligation norm = (Obligation) obj;
			return 
				super.getName().equals(norm.getName()) &&
				super.getHolder().equals(norm.getHolder());				
		}
		return false;
	}
	
	public boolean isViolated(AgentModel aM){
		return 
			(!isAgentATarget(getHolder(), aM) || isExpired(aM)) && 		  // The agent is no longer a target or the obligation expired
			!isTrue(aM);											      // Obligation was not satisfied
	}
	
	public boolean isFulfilled(AgentModel aM){
		return 
			isTrue(aM);     											  // Obligation was satisfied
	}
	
	public Goal generateGoal(AgentModel aM){
		ActivePursuitGoal goal = new ActivePursuitGoal(this.getName());
		for(Condition normativeCondition : this.getNormativeConditions()){
			goal.AddCondition("SuccessConditions", normativeCondition);
		}
		return goal;
	}
	
	public boolean expiresByFollwingPlan(Plan plan){
		ArrayList<Substitution> substs = new ArrayList<Substitution>();
		Condition effCondition;
		Name condValue;
		Name effectValue;
		boolean unifyResult;
		if(plan == null) return false;
		for(IPlanningOperator step : plan.getSteps()){
			for(Effect effect : step.getEffects()){
				effCondition = effect.GetEffect();
				for(Condition cond : this.getExpirationConditions()){
					if(Unifier.Unify(cond.getName(), effCondition.getName(), substs)){
						condValue = cond.getValue();
						effectValue = effCondition.getValue();
						if(cond instanceof PropertyNotEqual){
							unifyResult = Unifier.Disunify(condValue, effectValue, substs);
						}
						else{
							unifyResult = Unifier.Unify(condValue, effectValue, substs);
						}
						if(unifyResult){
							return true;
						}
					}
				}
			}
		}
		return false;
	}
	
	public void processFulfillment(AgentModel aM){
		if(this.getHolder().equals((Name)new Symbol(FAtiMA.Core.util.Constants.SELF_STRING))){
			ArrayList<Intention> toRemove = new ArrayList<Intention>();
			DeliberativeComponent dc = (DeliberativeComponent) aM.getComponent(DeliberativeComponent.NAME);
			Iterator<Intention> it = dc.getIntentionsIterator();
			while(it.hasNext()){
				Intention intention = it.next();
				if(intention.getGoal().getName().equals(this.getName())){
					toRemove.add(intention);
				}
			}
			for(Intention intention : toRemove){
				intention.ProcessIntentionSuccess(aM);
				dc.removeIntention(intention);
			}
		}
	}
	
	public void processViolation(AgentModel aM){
		if(this.getHolder().equals((Name)new Symbol(FAtiMA.Core.util.Constants.SELF_STRING))){
			ArrayList<Intention> toRemove = new ArrayList<Intention>();
			DeliberativeComponent dc = (DeliberativeComponent) aM.getComponent(DeliberativeComponent.NAME);
			Iterator<Intention> it = dc.getIntentionsIterator();
			while(it.hasNext()){
				Intention intention = it.next();
				if(intention.getGoal().getName().equals(this.getName())){
					toRemove.add(intention);
				}
			}
			for(Intention intention : toRemove){
				intention.ProcessIntentionFailure(aM);
				dc.removeIntention(intention);
			}
		}
	}	
}
