package FAtiMA.SocialImportance;

import java.util.ArrayList;

import FAtiMA.Core.conditions.Condition;
import FAtiMA.Core.wellFormedNames.Substitution;
import FAtiMA.Core.wellFormedNames.Symbol;

public class SIAttributionRule {
	private static final long serialVersionUID = 1L;
	
	private float _value;
	private Symbol _target;
	private ArrayList<Condition> _conditions;

	public SIAttributionRule(Symbol target, float value){
		_target = target;
		_value = value;
		_conditions = new ArrayList<Condition>();
	}

	public float getValue() {
		return _value;
	}

	public Symbol getTarget() {
		return _target;
	}
	
	public void addCondition(Condition c){
		_conditions.add(c);
	}
	
	public ArrayList<Condition> getConditions(){
		return _conditions;
	}
	
	public void MakeGround(ArrayList<Substitution> bindings){
		_target.makeGround(bindings);
		for(Condition c : _conditions){
			c.makeGround(bindings);
		}
	}
	
	public void MakeGround(Substitution subst){
		_target.makeGround(subst);
		for(Condition c : _conditions){
			c.makeGround(subst);
		}
	}
	
	public Object clone(){
		SIAttributionRule rule = new SIAttributionRule((Symbol)_target.clone(), _value);
		if(_conditions != null){
			for(Condition c : _conditions){
				rule.getConditions().add((Condition) c.clone());
			}
		}
		return rule;
	}
	
	@Override
	public String toString(){
		return "V: " + _value + " T: " + _target + " C: " + _conditions;  
	}
	
}
