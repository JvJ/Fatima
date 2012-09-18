package FAtiMA.emotionalIntelligence;

import java.util.ArrayList;
import java.util.Collections;
import java.util.ListIterator;

import FAtiMA.Core.AgentModel;
import FAtiMA.Core.conditions.PastEventCondition;
import FAtiMA.Core.emotionalState.EmotionalPameters;
import FAtiMA.Core.util.RatedObject;
import FAtiMA.Core.wellFormedNames.Name;
import FAtiMA.Core.wellFormedNames.Substitution;
import FAtiMA.Core.wellFormedNames.SubstitutionSet;
import FAtiMA.Core.wellFormedNames.Symbol;
import FAtiMA.ReactiveComponent.ReactiveComponent;
import FAtiMA.motivationalSystem.MotivationalComponent;

public class AppraisalCondition extends PastEventCondition {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static final float K = 0.1f; 
	
	private String _appraisalVariable;
	private Symbol _value;
	private short _test;
	private int _threshold;
	
	
	private AppraisalCondition(AppraisalCondition ac)
	{
		super(ac);
		this._appraisalVariable = ac._appraisalVariable;
		this._threshold = ac._threshold;
		this._test = ac._test;
		this._value = (Symbol) ac._value.clone();
		
	}
	
	public AppraisalCondition(String appraisalVariable, Symbol value, int threshold, short test, Symbol subject, Symbol action, Symbol target, ArrayList<Symbol> parameters, String ToM)
	{
		//this._type = type;
		//this._status = status;
		
		
		this.setToM(ToM);
		
		this.setPositive(true);
		this._appraisalVariable = appraisalVariable;
		this._value = value;
		this._threshold = threshold;
		this._test = test;
		
		this._subject = subject;
		this._action = action;
		this._target = target;
		
		this._parameters = parameters;
		
		updateName();
	}
	
	public float checkConditionUsingPerspective(AgentModel perspective) {
		if(this._action.isGrounded())
		{
			return 1;
		}
		else return 0;
	}
	
	public Object clone()
	{
		return new AppraisalCondition(this);	
	}

	public Object generateName(int id) {
		AppraisalCondition c = (AppraisalCondition) this.clone();
		c.replaceUnboundVariables(id);
		return c;
	}

	public ArrayList<SubstitutionSet> getValidSubstitutionsUsingPerspective(AgentModel perspective) {
		
		float finalemotionvalue;
		float appraisalVariableValue;
		ArrayList<RatedObject<SubstitutionSet>> ratedSubs;
		ArrayList<SubstitutionSet> subs;
		float mood;
	 	
		if(!this._value.isGrounded()) return null;
		
		mood = perspective.getEmotionalState().GetMood();
		
		finalemotionvalue = Float.parseFloat(this._value.toString()) + _threshold + K;
		
		//meaning that the valence of the corresponding emotion is positive
		if(_test == 0){
			appraisalVariableValue = finalemotionvalue - (mood * EmotionalPameters.MoodInfluenceOnEmotion);
			if(appraisalVariableValue < 0)
			{
				appraisalVariableValue = 0;
			}
		}
		else
		{
			//meaning that the valence of the corresponding emotion is negative
			appraisalVariableValue = - finalemotionvalue + (mood * EmotionalPameters.MoodInfluenceOnEmotion);
			if(appraisalVariableValue > 0)
			{
				appraisalVariableValue = 0;
			}
		}
		
		ratedSubs = new ArrayList<RatedObject<SubstitutionSet>>();

		//subs = searchReactiveAppraisals(modelToTest, appraisalVariableValue);
		
		//subs = searchMemoryAppraisals(modelToTest, appraisalVariableValue);
		
		ratedSubs.addAll(searchDrivesAppraisals(perspective, appraisalVariableValue));
		
		Collections.sort(ratedSubs);
	
		if(ratedSubs.size() > 0)
		{
			subs = new ArrayList<SubstitutionSet>();
			for(RatedObject<SubstitutionSet> ro : ratedSubs)
			{
				subs.add(ro.getObject());
			}
			return subs;
		}
		else return null;
		
	}
	
	/*private ArrayList<SubstitutionSet> searchMemoryAppraisals(AgentModel am, float desirability)
	{
		ArrayList<SubstitutionSet> subs = new ArrayList<SubstitutionSet>();
		SubstitutionSet sset;
		Symbol target;
		AdvancedMemoryComponent advMem;
		  
		ArrayList<String> knownInfo = new ArrayList<String>();
		knownInfo.add("desirability " + desirability);
		//float desirability = Float.parseFloat(this._value.toString());
		//if(desirability >= 0)
		//{
		//	knownInfo.add("positive");
		//}
		//else
		//{
		//	knownInfo.add("negative");
		//}
		
		String question = "events";
		
		advMem = (AdvancedMemoryComponent) am.getComponent(AdvancedMemoryComponent.NAME);
		
		advMem.getSpreadActivate().Spread(question, knownInfo, am.getMemory().getEpisodicMemory());
		
		ArrayList<ActionDetail> details = advMem.getSpreadActivate().getDetails();
		
		
		//wtf, isto devolve todos os eventos com desirability >= ao valor pretendido, inclusivamente
		//repetido
		
		//TODO must do this properly
		if(details.size() > 0)
		{
			for(ActionDetail ad : details)
			{
				if(ad.getTarget().equals(Constants.SELF))
				{
					target = this.getToM();
				}
				else
				{
					target = new Symbol(ad.getTarget()); 
				}
				sset = new SubstitutionSet();
				sset.AddSubstitution(new Substitution(this._action,new Symbol(ad.getAction())));
				sset.AddSubstitution(new Substitution(this._target,target));
				subs.add(sset);
			}
		}
		
		//TODO procurar events com desejabilidade nos GER's (Generic Event Representation) 
		
		return subs;
	}
	*/
	
	private ArrayList<SubstitutionSet> searchReactiveAppraisals(AgentModel am, float desirability)
	{
		ReactiveComponent reactiveComponent = (ReactiveComponent) am.getComponent(ReactiveComponent.NAME);
		return reactiveComponent.searchEventsWithAppraisal(am, _subject, _action, _target, _parameters.get(0), desirability);
	}
	
	private ArrayList<RatedObject<SubstitutionSet>> searchDrivesAppraisals(AgentModel am, float desirability)
	{
		MotivationalComponent motivationalComponent = (MotivationalComponent) am.getComponent(MotivationalComponent.NAME);
		
		return motivationalComponent.searchEventsWithAppraisal(am, this.getToMLvl1(), _subject, _action, _target, _parameters.get(0), desirability);	
	}

	public Object Ground(ArrayList<Substitution> bindingConstraints) {
		
		AppraisalCondition c = (AppraisalCondition) this.clone();
		c.makeGround(bindingConstraints);
		return c;
	}

	public Object Ground(Substitution subst) {
		AppraisalCondition c = (AppraisalCondition) this.clone();
		c.makeGround(subst);
		return c;
	}

	public void makeGround(ArrayList<Substitution> bindings) {
		super.makeGround(bindings);
		
		this._value.makeGround(bindings);
	}
	
	public void makeGround(Substitution subst) {
		super.makeGround(subst);
		this._value.makeGround(subst);
	}
	
	public void replaceUnboundVariables(int variableID) {
		super.replaceUnboundVariables(variableID);
		this._value.replaceUnboundVariables(variableID);
	}
	
	protected void updateName()
	{
		String aux = this._appraisalVariable + "," + this._subject + "," + this._action;
		if(this._target != null)
		{
			aux = aux + "," + this._target;
		}
		
		ListIterator<Symbol> li = this._parameters.listIterator();
		while(li.hasNext())
		{
			aux = aux + "," + li.next();
		}
		
		this.setName(Name.ParseName("Appraisal(" + aux + ")"));
	}
}
