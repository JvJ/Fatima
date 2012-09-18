package FAtiMA.SocialImportance.parsers;

import java.util.ArrayList;

import org.xml.sax.Attributes;

import FAtiMA.Core.AgentModel;
import FAtiMA.Core.conditions.NewEventCondition;
import FAtiMA.Core.conditions.PastEventCondition;
import FAtiMA.Core.conditions.PredicateCondition;
import FAtiMA.Core.conditions.PropertyCondition;
import FAtiMA.Core.conditions.RecentEventCondition;
import FAtiMA.Core.util.Constants;
import FAtiMA.Core.util.parsers.ReflectXMLHandler;
import FAtiMA.Core.wellFormedNames.Substitution;
import FAtiMA.Core.wellFormedNames.Symbol;
import FAtiMA.SocialImportance.SIAttributionRule;
import FAtiMA.SocialImportance.SIClaimRule;
import FAtiMA.SocialImportance.SIConferralRule;

public class SILoaderHandler  extends ReflectXMLHandler  {

	
	private Substitution _self = new Substitution(new Symbol("[SELF]"), new Symbol(Constants.SELF_STRING));
	
	private ArrayList<SIClaimRule> _siClaimRules;
	private ArrayList<SIConferralRule> _siConferralRules;
	
	private ArrayList<SIAttributionRule> _siAttributionRules;
	
	private SIAttributionRule _siAttributionRule;
	
	public SILoaderHandler(AgentModel aM){
		_siClaimRules = new ArrayList<SIClaimRule>();
		_siConferralRules = new ArrayList<SIConferralRule>();
		_siAttributionRules = new ArrayList<SIAttributionRule>();
	}	


	public void SIAttributionRule(Attributes attributes){
		Symbol target = null;
		
		String aux = attributes.getValue("target");
    	if(aux!=null){
    		target = new Symbol(aux);
    	}
    	
    	float value = Float.valueOf(attributes.getValue("value"));
    	
    	_siAttributionRule = new SIAttributionRule(target, value);
    	_siAttributionRules.add(_siAttributionRule);
 
	}
	
	public void SIClaim(Attributes attributes) {
		String actionName = attributes.getValue("action");
		String parameters = attributes.getValue("parameters");
		float value = 0;
		String aux = attributes.getValue("value");
		if(aux!=null){
			value = Float.valueOf(aux);
		}
		_siClaimRules.add(new SIClaimRule(actionName, parameters, value));
	}
	
	public void SIConferral(Attributes attributes) {
		String actionName = attributes.getValue("action");
		String parameters = attributes.getValue("parameters");
		float value = 0;
		String aux = attributes.getValue("value");
		if(aux!=null){
			value = Float.valueOf(aux);
		}
		_siConferralRules.add(new SIConferralRule(actionName, parameters, value));
	}

	
	public ArrayList<SIClaimRule> getSIClaimRules(){
		return _siClaimRules;
	}
	
	public ArrayList<SIAttributionRule> getSIAttributionRules(){
		return _siAttributionRules;
	}
	
	public ArrayList<SIConferralRule> getSIConferralRules (){
		return _siConferralRules;
	}	
	
	public void Predicate(Attributes attributes){
		PredicateCondition cond = PredicateCondition.ParsePredicate(attributes);
		cond.makeGround(this._self);
		if(_siAttributionRule != null) _siAttributionRule.addCondition(cond);
	}

	public void Property(Attributes attributes){
		PropertyCondition cond = PropertyCondition.ParseProperty(attributes);
		cond.makeGround(this._self);
		if(_siAttributionRule != null) _siAttributionRule.addCondition(cond);
	}

	public void NewEvent(Attributes attributes){
		NewEventCondition cond = new NewEventCondition(PastEventCondition.ParseEvent(attributes));
		cond.makeGround(this._self);
		if(_siAttributionRule != null) _siAttributionRule.addCondition(cond);
	}

	public void RecentEvent(Attributes attributes){
		RecentEventCondition cond = new RecentEventCondition(PastEventCondition.ParseEvent(attributes));
		cond.makeGround(this._self);
		if(_siAttributionRule != null) _siAttributionRule.addCondition(cond);
	}

	public void PastEvent(Attributes attributes){
		PastEventCondition cond = PastEventCondition.ParseEvent(attributes);
		cond.makeGround(this._self);
		if(_siAttributionRule != null) _siAttributionRule.addCondition(cond);
		
	}
}

