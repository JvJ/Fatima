package FAtiMA.NormativeComponent;

import java.util.ArrayList;

import FAtiMA.Core.AgentModel;
import FAtiMA.Core.conditions.Condition;
import FAtiMA.Core.goals.Goal;
import FAtiMA.Core.wellFormedNames.Name;
import FAtiMA.Core.wellFormedNames.Substitution;

public abstract class Norm {
	
	public static final String ACTIVATION_CONDITION = "ActivationCondition";
	public static final String EXPIRATION_CONDITION = "ExpirationCondition";
	public static final String EXCEPTION_CONDITION = "ExceptionCondition";
	public static final String NORMATIVE_CONDITION = "NormativeCondition";
	public static final String REWARD = "Reward";
	public static final String PUNISHMENT = "Punishment";
	
	public static final String TARGET = "Target";
	public static final String BENEFICIARY = "Beneficiary";
	public static final String ISSUER = "Issuer";
	public static final String ENFORCER = "Enforcer";
	
	private int _id;
	private Name _name;
	private Name _holder;
	private ArrayList<Role> _targets;
	private ArrayList<Condition> _activationConditions;
	private ArrayList<Condition> _expirationConditions;
	private ArrayList<Condition> _exceptionConditions;
	private ArrayList<Condition> _normativeConditions;
	private ArrayList<Condition> _rewards;
	private ArrayList<Condition> _punishments;
	private ArrayList<Role> _beneficiaries;
	private ArrayList<Role> _issuers;
	private ArrayList<Role> _enforcers;
	
	private float _baseSalience;
	private float _salience;
	
	protected Norm(Name name){
		_id = 0;
		_name = name;
		_holder = Name.ParseName(" ");
		_targets = new ArrayList<Role>();
		_activationConditions = new ArrayList<Condition>();
		_expirationConditions = new ArrayList<Condition>();
		_exceptionConditions = new ArrayList<Condition>();
		_normativeConditions = new ArrayList<Condition>();
		_rewards = new ArrayList<Condition>();
		_punishments = new ArrayList<Condition>();
		_beneficiaries = new ArrayList<Role>();
		_issuers = new ArrayList<Role>();
		_enforcers = new ArrayList<Role>();
	}
	
	public int getID(){
		return _id;
	}
	
	public void setID(int id){
		_id = id;
	}
	
	public boolean isAgentATarget(Name agentName, AgentModel aM){
		for(Role target: this.getTargets()){
			if(target.isHoldByAgent(agentName, aM)) return true;
		}
		return false;
	}
	
	public Name getHolder(){
		return _holder;
	}
	
	public void setHolder(Name holder){
		_holder = holder;
	}
	
	public boolean hasHolder(){
		return !_holder.equals(Name.ParseName(" "));
	}
	
	public ArrayList<Role> getTargets() {
		return _targets;
	}

	public ArrayList<Condition> getActivationConditions() {
		return _activationConditions;
	}

	public ArrayList<Condition> getExpirationConditions() {
		return _expirationConditions;
	}

	public ArrayList<Condition> getExceptionConditions() {
		return _exceptionConditions;
	}

	public ArrayList<Condition> getNormativeConditions() {
		return _normativeConditions;
	}

	public ArrayList<Condition> getRewards() {
		return _rewards;
	}

	public ArrayList<Condition> getPunishments() {
		return _punishments;
	}

	public ArrayList<Role> getBeneficiaries() {
		return _beneficiaries;
	}

	public ArrayList<Role> getIssuers() {
		return _issuers;
	}

	public ArrayList<Role> getEnforcers() {
		return _enforcers;
	}

	public float getBaseSalience(){
		return _baseSalience;
	}
	
	public void setBaseSalience(float baseSalience){
		_baseSalience = baseSalience;
		this.updateSalience();
	}
	
	public float getSalience(){
		return _salience;
	}
	
	protected void setSalience(float salience){
		_salience = salience;
	}
	
	public void addRole(Role role, String roleType){
		if(roleType.equalsIgnoreCase(TARGET)) _targets.add(role);
		else if(roleType.equalsIgnoreCase(BENEFICIARY)) _beneficiaries.add(role);
		else if(roleType.equalsIgnoreCase(ISSUER)) _issuers.add(role);
		else if(roleType.equalsIgnoreCase(ENFORCER)) _enforcers.add(role);
	}
	
	public void addCondition(Condition cond, String conditionType){
		if(conditionType.equalsIgnoreCase(ACTIVATION_CONDITION)) _activationConditions.add(cond);
		else if(conditionType.equalsIgnoreCase(EXPIRATION_CONDITION)) _expirationConditions.add(cond);
		else if(conditionType.equalsIgnoreCase(EXCEPTION_CONDITION)) _exceptionConditions.add(cond);
		else if(conditionType.equalsIgnoreCase(NORMATIVE_CONDITION)) _normativeConditions.add(cond);
		else if(conditionType.equalsIgnoreCase(REWARD)) _rewards.add(cond);
		else if(conditionType.equalsIgnoreCase(PUNISHMENT)) _punishments.add(cond);
	}
	
	public boolean isActive(AgentModel aM){
		return Condition.checkActivation(aM, this.getActivationConditions()) != null;
	}
	
	public Name getName(){
		return _name;
	}
	
	public void setName(Name name){
		_name = name;
	}
	
//	public String toString(){
//		return "NORM: " + _name.GetFirstLiteral().getName() + "\n" +
//		       "Tgt: " + _targets.toString() + "\n" +
//			   "Act: " + _activationConditions.toString() + "\n" +
//		       "Exp: " + _expirationConditions.toString() + "\n" +
//		       "Exc: " + _exceptionConditions.toString() + "\n" +
//		       "Con: " + _normativeConditions.toString() + "\n" +
//		       "Rew: " + _rewards.toString() +"\n" +
//		       "Pun: " + _punishments.toString() + "\n" +
//		       "Ben: " + _beneficiaries.toString() + "\n" +
//		       "Iss: " + _issuers.toString() + "\n" +
//		       "Enf: " + _enforcers.toString();
//	}
	
	
	public String toString(){
		return 
		"NORM: " + _name.getFirstLiteral().getName() +
		" -- id: " + _id +
		" -- salience: " + _salience + 
		" -- holder: " + _holder.getFirstLiteral().toString();
		       
	}

	public void MakeGround(ArrayList<Substitution> bindings){
		for(Role r : _targets){
			r.MakeGround(bindings);
		}
		for(Condition c : _activationConditions){
			c.makeGround(bindings);
		}
		for(Condition c : _expirationConditions){
			c.makeGround(bindings);
		}
		for(Condition c : _exceptionConditions){
			c.makeGround(bindings);
		}
		for(Condition c : _normativeConditions){
			c.makeGround(bindings);
		}
		for(Condition c : _rewards){
			c.makeGround(bindings);
		}
		for(Condition c : _punishments){
			c.makeGround(bindings);
		}
		for(Role r : _beneficiaries){
			r.MakeGround(bindings);
		}
		for(Role r : _issuers){
			r.MakeGround(bindings);
		}
		for(Role r : _enforcers){
			r.MakeGround(bindings);
		}
	}
	
	public void MakeGround(Substitution subst){
		for(Role r : _targets){
			r.MakeGround(subst);
		}
		for(Condition c : _activationConditions){
			c.makeGround(subst);
		}
		for(Condition c : _expirationConditions){
			c.makeGround(subst);
		}
		for(Condition c : _exceptionConditions){
			c.makeGround(subst);
		}
		for(Condition c : _normativeConditions){
			c.makeGround(subst);
		}
		for(Condition c : _rewards){
			c.makeGround(subst);
		}
		for(Condition c : _punishments){
			c.makeGround(subst);
		}
		for(Role r : _beneficiaries){
			r.MakeGround(subst);
		}
		for(Role r : _issuers){
			r.MakeGround(subst);
		}
		for(Role r : _enforcers){
			r.MakeGround(subst);
		}
	}

	
	public abstract Object clone();
	
	public abstract boolean equals(Object obj);
	
	public abstract boolean isViolated(AgentModel aM);
	
	public abstract boolean isFulfilled(AgentModel aM);
	
	public void updateSalience(){
		_salience = _baseSalience;
	}
	
	public float computeGoalImportanceOfSuccess(){
		return getSalience();
	}
	
	public float computeGoalImportanceOfFailure(){
		return getSalience();
	}
	
	public float computeGoalUrgency(){
		return getSalience();
	}
	
	public boolean isExpired(AgentModel aM){
		for(Condition cond : this.getExpirationConditions()){
			if(cond.checkCondition(aM) == 1){
				return true;
			}
		}
		return false;
	}
	
	public boolean isTrue(AgentModel aM){
		return Condition.checkActivation(aM, _normativeConditions) != null;
	}
	
	public boolean isGrounded(){
		if(!_name.isGrounded()) return false;
		if(!_holder.isGrounded()) return false;
		for(Role r : _targets){
			if(!r.isGrounded()) return false;
		}
		for(Condition c : _activationConditions){
			if(!c.isGrounded()) return false;
		}
		for(Condition c : _expirationConditions){
			if(!c.isGrounded()) return false;
		}
		for(Condition c : _exceptionConditions){
			if(!c.isGrounded()) return false;
		}
		for(Condition c : _normativeConditions){
			if(!c.isGrounded()) return false;
		}
		for(Condition c : _rewards){
			if(!c.isGrounded()) return false;
		}
		for(Condition c : _punishments){
			if(!c.isGrounded()) return false;
		}
		for(Role r : _beneficiaries){
			if(!r.isGrounded()) return false;
		}
		for(Role r : _issuers){
			if(!r.isGrounded()) return false;
		}
		for(Role r : _enforcers){
			if(!r.isGrounded()) return false;
		}
		return true;
	}
	
	public abstract Goal generateGoal(AgentModel aM);
	
	public abstract void processFulfillment(AgentModel aM);
	
	public abstract void processViolation(AgentModel aM);
}
