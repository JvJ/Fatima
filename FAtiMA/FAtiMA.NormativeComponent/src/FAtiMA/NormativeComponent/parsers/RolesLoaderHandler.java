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
import FAtiMA.NormativeComponent.NormativeComponent;
import FAtiMA.NormativeComponent.Role;

public class RolesLoaderHandler extends ReflectXMLHandler {
	
	private Role _role;
	private ArrayList<Role> _roles;
	private String _conditionType;
	private Substitution _self;
	
	public RolesLoaderHandler(AgentModel aM, NormativeComponent nC){
		_conditionType = Role.ACTIVATION_CONDITION;
		_roles = new ArrayList<Role>();
		_self = new Substitution(new Symbol("[SELF]"), new Symbol(FAtiMA.Core.util.Constants.SELF_STRING));
	}
	
	public ArrayList<Role> getRoles(){
		return _roles;
	}
	
	public void Role(Attributes attributes){
		_role = new Role(Name.ParseName(attributes.getValue("name")));
		_roles.add(_role);
	}
	
	public void ActivationConditions(Attributes attributes){
		_conditionType = Role.ACTIVATION_CONDITION;
	}
	
	public void ExpirationConditions(Attributes attributes){
		_conditionType = Role.EXPIRATION_CONDITION;
	}
	
	public void Predicate(Attributes attributes){
		PredicateCondition cond = PredicateCondition.ParsePredicate(attributes);
		cond.makeGround(this._self);
		if(_role != null) _role.addCondition(cond, _conditionType);
	}

	public void Property(Attributes attributes){
		PropertyCondition cond = PropertyCondition.ParseProperty(attributes);
		cond.makeGround(this._self);
		if(_role != null) _role.addCondition(cond, _conditionType);
	}

	public void NewEvent(Attributes attributes){
		NewEventCondition cond = new NewEventCondition(PastEventCondition.ParseEvent(attributes));
		cond.makeGround(this._self);
		if(_role != null) _role.addCondition(cond, _conditionType);
	}

	public void RecentEvent(Attributes attributes){
		RecentEventCondition cond = new RecentEventCondition(PastEventCondition.ParseEvent(attributes));
		cond.makeGround(this._self);
		if(_role != null) _role.addCondition(cond, _conditionType);
	}

	public void PastEvent(Attributes attributes){
		PastEventCondition cond = PastEventCondition.ParseEvent(attributes);
		cond.makeGround(this._self);
		if(_role != null) _role.addCondition(cond, _conditionType);
	}

}
