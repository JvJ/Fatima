package FAtiMA.NormativeComponent.parsers;

import java.util.ArrayList;

import org.xml.sax.Attributes;

import FAtiMA.Core.AgentModel;
import FAtiMA.Core.conditions.NewEventCondition;
import FAtiMA.Core.conditions.PastEventCondition;
import FAtiMA.Core.conditions.PredicateCondition;
import FAtiMA.Core.conditions.PropertyCondition;
import FAtiMA.Core.conditions.RecentEventCondition;
import FAtiMA.Core.util.parsers.ReflectXMLHandler;
import FAtiMA.Core.wellFormedNames.Name;
import FAtiMA.Core.wellFormedNames.Substitution;
import FAtiMA.Core.wellFormedNames.Symbol;
import FAtiMA.NormativeComponent.Norm;
import FAtiMA.NormativeComponent.NormativeComponent;
import FAtiMA.NormativeComponent.Obligation;
import FAtiMA.NormativeComponent.Prohibition;
import FAtiMA.NormativeComponent.Role;

public class NormativeLoaderHandler extends ReflectXMLHandler{
	
	private Norm _norm;
	private ArrayList<Obligation> _obligations;
	private ArrayList<Prohibition> _prohibitions;
	private String _conditionType;
	private String _roleType;
	private Substitution _self;
	private int _abstractNormsCounter;
	
	public NormativeLoaderHandler(AgentModel aM, NormativeComponent nC){
		_obligations = new ArrayList<Obligation>();
		_prohibitions = new ArrayList<Prohibition>();
		_conditionType = Norm.ACTIVATION_CONDITION;
		_roleType = Norm.TARGET;
		_self = new Substitution(new Symbol("[SELF]"), new Symbol(FAtiMA.Core.util.Constants.SELF_STRING));
		_abstractNormsCounter = 0;
	}
	
	public ArrayList<Obligation> getObligations(){
		return _obligations;
	}
	
	public ArrayList<Prohibition> getProhibitions(){
		return _prohibitions;
	}
	
	public void Obligation(Attributes attributes){
		_norm = new Obligation(Name.ParseName(attributes.getValue("name")));
		_abstractNormsCounter++;
		_norm.setID(_abstractNormsCounter);
		_norm.setBaseSalience(Float.valueOf(attributes.getValue("baseSalience")));
		_obligations.add((Obligation)_norm);
	}
	
	public void Prohibition(Attributes attributes){
		_norm = new Prohibition(Name.ParseName(attributes.getValue("name")));
		_abstractNormsCounter++;
		_norm.setID(_abstractNormsCounter);
		_norm.setBaseSalience(Float.valueOf(attributes.getValue("baseSalience")));
		_prohibitions.add((Prohibition)_norm);
	}
	
	public void ActivationConditions(Attributes attributes){
		_conditionType = Norm.ACTIVATION_CONDITION;
	}
	
	public void ExpirationConditions(Attributes attributes){
		_conditionType = Norm.EXPIRATION_CONDITION;
	}
	
	public void ExceptionConditions(Attributes attributes){
		_conditionType = Norm.EXCEPTION_CONDITION;
	}
	
	public void NormativeConditions(Attributes attributes){
		_conditionType = Norm.NORMATIVE_CONDITION;
	}
	
	public void Rewards(Attributes attributes){
		_conditionType = Norm.REWARD;
	}
	
	public void Punishments(Attributes attributes){
		_conditionType = Norm.PUNISHMENT;
	}
	
	public void Targets(Attributes attributes){
		_roleType = Norm.TARGET;
	}
	
	public void Beneficiaries(Attributes attributes){
		_roleType = Norm.BENEFICIARY;
	}
	
	public void Issuers(Attributes attributes){
		_roleType = Norm.ISSUER;
	}
	
	public void Enforcers(Attributes attributes){
		_roleType = Norm.ENFORCER;
	}
	
	public void Role(Attributes attributes){
		if(_norm != null) _norm.addRole(new Role(Name.ParseName(attributes.getValue("name"))), _roleType);
	}
	
	public void Predicate(Attributes attributes){
		PredicateCondition cond = PredicateCondition.ParsePredicate(attributes);
		cond.makeGround(this._self);
		if(_norm != null) _norm.addCondition(cond, _conditionType);
	}

	public void Property(Attributes attributes){
		PropertyCondition cond = PropertyCondition.ParseProperty(attributes);
		cond.makeGround(this._self);
		if(_norm != null) _norm.addCondition(cond, _conditionType);
	}

	public void NewEvent(Attributes attributes){
		NewEventCondition cond = new NewEventCondition(PastEventCondition.ParseEvent(attributes));
		cond.makeGround(this._self);
		if(_norm != null) _norm.addCondition(cond, _conditionType);
	}

	public void RecentEvent(Attributes attributes){
		RecentEventCondition cond = new RecentEventCondition(PastEventCondition.ParseEvent(attributes));
		cond.makeGround(this._self);
		if( _norm != null) _norm.addCondition(cond, _conditionType);
	}

	public void PastEvent(Attributes attributes){
		PastEventCondition cond = PastEventCondition.ParseEvent(attributes);
		cond.makeGround(this._self);
		if(_norm != null) _norm.addCondition(cond, _conditionType);
	}
}
