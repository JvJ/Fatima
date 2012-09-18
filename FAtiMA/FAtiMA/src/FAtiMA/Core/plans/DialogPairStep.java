package FAtiMA.Core.plans;

import java.util.ArrayList;
import java.util.HashMap;

import FAtiMA.Core.conditions.Condition;
import FAtiMA.Core.wellFormedNames.Name;
import FAtiMA.Core.wellFormedNames.Symbol;

public class DialogPairStep extends Step {
	
	
	private static HashMap<String,ArrayList<Name>> initiationActsLibrary = new HashMap<String,ArrayList<Name>>();
	private static HashMap<String,ArrayList<Name>> reactiveActsLibrary = new HashMap<String,ArrayList<Name>>();
	
	private static void registerAct(HashMap<String,ArrayList<Name>> actLibrary, String actName, Name dialogPair){
		if(actLibrary.containsKey(actName)){
			ArrayList<Name> dialogPairList = actLibrary.get(actName);
			dialogPairList.add(dialogPair);
		}else{
			ArrayList<Name> newDialogPairList = new ArrayList<Name>();
			newDialogPairList.add(dialogPair);
			actLibrary.put(actName, newDialogPairList);
		}
	}

	
	public static boolean existsInitiationAct(String initiationAct){
		return initiationActsLibrary.containsKey(initiationAct);
	}
	
	public static ArrayList<Name> getInitiationActPossiblePairs(String initiationAct){
		return initiationActsLibrary.get(initiationAct);
	}
	
	public static boolean existsReactiveAct(String initiationAct){
		return reactiveActsLibrary.containsKey(initiationAct);
	}

	public static ArrayList<Name> getReactiveActPossiblePairs(String reactiveAct){
		return reactiveActsLibrary.get(reactiveAct);
	}
	
	
	private String _initiationActName;
	private String _reactiveActName;
	
	public void setInitiationAct(String initiationActName){
		this._initiationActName = initiationActName;
		registerAct(this.initiationActsLibrary,_initiationActName,_name);
	}
	
	public void setReactiveAct(String reactiveAct){
		this._reactiveActName = reactiveAct;
		registerAct(this.reactiveActsLibrary,_initiationActName,_name);
	}
	
	
	public DialogPairStep(Symbol agent, Name action, float probability) {
		super(agent, action, probability);
		// TODO Auto-generated constructor stub
	}
	
	private DialogPairStep(){};
	

	public String getInitiationAct(){
		return _initiationActName;
	}
	
	public String getReactiveAct(){
		return _reactiveActName;
	}
	
	@Override
	public Object clone() {
		
		DialogPairStep op = new DialogPairStep();
		
		op._initiationActName = new String(this._initiationActName);
		op._reactiveActName = new String(this._reactiveActName);
		op._agent = (Symbol) this._agent.clone();
		op._name = (Name) this._name.clone();
		op._key = new String(this._key);
		op._id = this._id;
		op._baseprob = this._baseprob;
		op._selfExecutable = this._selfExecutable;
		
		if(_preconditions != null) {
			op._preconditions = new ArrayList<Condition>(_preconditions.size());
			for(Condition cond : _preconditions)
			{
				op._preconditions.add((Condition)cond.clone());
			}	
		}
		
		if(_effects != null) {
			op._effects = new ArrayList<Effect>(_effects.size());
			for(Effect effect : _effects)
			{
				op._effects.add((Effect)effect.clone());
			}	
		}
		
		return op;
	}

	
	
}
