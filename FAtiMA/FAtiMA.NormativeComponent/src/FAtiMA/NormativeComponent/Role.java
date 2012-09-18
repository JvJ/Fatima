package FAtiMA.NormativeComponent;

import java.util.ArrayList;

import FAtiMA.Core.AgentModel;
import FAtiMA.Core.conditions.Condition;
import FAtiMA.Core.memory.semanticMemory.SemanticMemory;
import FAtiMA.Core.wellFormedNames.Name;
import FAtiMA.Core.wellFormedNames.Substitution;

public class Role {
	
	public static final String ACTIVATION_CONDITION = "ActivationCondition";
	public static final String EXPIRATION_CONDITION = "ExpirationCondition";
	
	private Name _name;
	private Name _holder;
	private ArrayList<Condition> _activationConditions;
	private ArrayList<Condition> _expirationConditions;
	
	public Role(Name name){
		_name = name;
		_holder = Name.ParseName(" ");
		_activationConditions = new ArrayList<Condition>();
		_expirationConditions = new ArrayList<Condition>();
		
	}
	
	public boolean isHoldByAgent(Name agentName, AgentModel aM){
		SemanticMemory memory = aM.getMemory().getSemanticMemory();
		return memory.AskPredicate(Name.ParseName(agentName.getFirstLiteral().getName() + "(" + "hasRole," + getName() + ")"));
	}
	
	public Name getName(){
		return _name;
	}
	
	public String toString(){
		return "\nROLE: " + _name.getFirstLiteral().getName() + "\n" +
		       "Holder: " + _holder.getFirstLiteral().getName() + "\n" +
			   "Act: " + _activationConditions.toString() + "\n" +
		       "Exp: " + _expirationConditions.toString();
	}
	
	
	public ArrayList<Condition> getActivationConditions(){
		return _activationConditions;
	}
	
	public ArrayList<Condition> getExpirationConditions(){
		return _expirationConditions;
	}

	public void addCondition(Condition cond, String conditionType){
		if(conditionType.equalsIgnoreCase(ACTIVATION_CONDITION)) _activationConditions.add(cond);
		else if(conditionType.equalsIgnoreCase(EXPIRATION_CONDITION)) _expirationConditions.add(cond);
	}
	
	public Name getHolder() {
		return _holder;
	}

	public void setHolder(Name holder) {
		this._holder = holder;
	}
	
	public boolean hasHolder(){
		return !_holder.equals(Name.ParseName(""));
	}

	public Object clone(){
		Role r = new Role((Name)_name.clone());
		r.setHolder((Name)_holder.clone());
		if(this._activationConditions != null){
			r._activationConditions = new ArrayList<Condition>(this._activationConditions.size());
			for(Condition c : this._activationConditions){
				r._activationConditions.add((Condition) c.clone());
			}
		}
		if(this._expirationConditions != null){
			r._expirationConditions = new ArrayList<Condition>(this._expirationConditions.size());
			for(Condition c : this._expirationConditions){
				r._expirationConditions.add((Condition) c.clone());
			}
		}
		return r;
	}
	
	public void MakeGround(Substitution subst){
		_name.makeGround(subst);
		_holder.makeGround(subst);
		for(Condition c : _activationConditions){
			c.makeGround(subst);
		}
		for(Condition c : _expirationConditions){
			c.makeGround(subst);
		}
	}
	
	public void MakeGround(ArrayList<Substitution> bindings){
		_name.makeGround(bindings);
		_holder.makeGround(bindings);
		for(Condition c : _activationConditions){
			c.makeGround(bindings);
		}
		for(Condition c : _expirationConditions){
			c.makeGround(bindings);
		}
	}
 
	public boolean equals(Object obj){
		if(obj instanceof Role){
			Role role = (Role) obj;
			return 
				_name.equals(role.getName()) &&
				_holder.equals(role.getHolder());		
		}
		return false;
	}
	
	public Name getPropertyName(){
		return Name.ParseName(getHolder().getFirstLiteral().getName() + "(" + "hasRole," + getName()+")");
	}
	
	public boolean isGrounded(){
		if(!_name.isGrounded()) return false;
		if(!_holder.isGrounded()) return false;
		for(Condition c : _activationConditions){
			if(!c.isGrounded()) return false; 
		}
		for(Condition c : _expirationConditions){
			if(!c.isGrounded()) return false;
		}
		return true;
	}
	
}
