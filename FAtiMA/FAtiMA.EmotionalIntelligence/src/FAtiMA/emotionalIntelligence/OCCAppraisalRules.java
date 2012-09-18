package FAtiMA.emotionalIntelligence;

import java.util.ArrayList;

import FAtiMA.Core.AgentModel;
import FAtiMA.Core.conditions.EmotionCondition;
import FAtiMA.Core.conditions.NewEventCondition;
import FAtiMA.Core.conditions.PropertyEqual;
import FAtiMA.Core.emotionalState.EmotionDisposition;
import FAtiMA.Core.plans.Effect;
import FAtiMA.Core.plans.Step;
import FAtiMA.Core.util.Constants;
import FAtiMA.Core.util.enumerables.ActionEvent;
import FAtiMA.Core.util.enumerables.EventType;
import FAtiMA.Core.wellFormedNames.Name;
import FAtiMA.Core.wellFormedNames.Symbol;
import FAtiMA.OCCAffectDerivation.OCCAppraisalVariables;
import FAtiMA.OCCAffectDerivation.OCCEmotionType;


public abstract class OCCAppraisalRules {
	
	//private final Step _joyOperator;
	//private final Step _distressOperator;
	//private ArrayList<Step> _appraisalOperators;
	
	public static ArrayList<Step> GenerateOCCAppraisalRules(AgentModel am)
	{	
		Step joyOperator;
		Step distressOperator;
		ArrayList<Step> appraisalOperators;	
		EmotionDisposition disp;
		int threshold;
		Effect aux;
		EmotionCondition c;
		AppraisalCondition appraisal;
		NewEventCondition event;
		ArrayList<Symbol> params;
		PropertyEqual subjectIsSelf;
		PropertyEqual subjectIsPerson;
		
		appraisalOperators = new ArrayList<Step>();
		
		subjectIsPerson = new PropertyEqual(Name.ParseName("[s](isPerson)"),new Symbol("True"));
		subjectIsSelf = new PropertyEqual(new Symbol("[s]"), Constants.SELF_SYMBOL);
		
		
		joyOperator = new Step(Constants.AGENT_SYMBOL,Name.ParseName("JoyAppraisal([X],[s],[a],[t],[p1])"),1.0f);
		c = new EmotionCondition(true,OCCEmotionType.JOY.name(),Constants.AGENT_STRING);
		c.SetIntensity(new Symbol("[X]"));
		aux = new Effect(am, "JoyEmotion", 1.0f,c);
		joyOperator.AddEffect(aux);
		
		disp = am.getEmotionalState().getEmotionDisposition(OCCEmotionType.JOY.name());
		threshold = disp.getThreshold();
		
		params = new ArrayList<Symbol>();
		params.add(new Symbol("[p1]"));
		//params.add(new Symbol("[p2]"));
		appraisal = new AppraisalCondition(
				OCCAppraisalVariables.DESIRABILITY.name(), 
				new Symbol("[X]"),
				threshold, 
				(short)0,
				new Symbol("[s]"),
				new Symbol("[a]"),
				new Symbol("[t]"), 
				params,
				Constants.AGENT_STRING);
		
		joyOperator.AddPrecondition(appraisal);
		
		Name ev = Name.ParseName("EVENT([s],[a],[t],[p1])");
		
		event = new NewEventCondition(true, EventType.ACTION,ActionEvent.SUCCESS,ev);
		joyOperator.AddPrecondition(event);
		
		joyOperator.AddPrecondition((PropertyEqual) subjectIsPerson.clone());
		joyOperator.AddPrecondition((PropertyEqual) subjectIsSelf.clone());
		
		appraisalOperators.add(joyOperator);
		
		
		//distress
		distressOperator = new Step(Constants.AGENT_SYMBOL,Name.ParseName("DistressAppraisal([X],[s],[a],[t],[p1])"),1.0f);
		c = new EmotionCondition(true,OCCEmotionType.DISTRESS.name(),Constants.AGENT_STRING);
		c.SetIntensity(new Symbol("[X]"));
		aux = new Effect(am, "DistressEmotion", 1.0f,c);
		distressOperator.AddEffect(aux);
		
		disp = am.getEmotionalState().getEmotionDisposition(OCCEmotionType.DISTRESS.name());
		threshold = disp.getThreshold();
		
		params = new ArrayList<Symbol>();
		params.add(new Symbol("[p1]"));
		//params.add(new Symbol("[p2]"));
		appraisal = new AppraisalCondition(
				OCCAppraisalVariables.DESIRABILITY.name(), new Symbol("[X]"),
				threshold,
				(short)1,
				new Symbol("[s]"),
				new Symbol("[a]"),
				new Symbol("[t]"), 
				params,
				Constants.AGENT_STRING);
		
		distressOperator.AddPrecondition(appraisal);
		
		ev = Name.ParseName("EVENT([s],[a],[t],[p1])");
		
		event = new NewEventCondition(true, EventType.ACTION,ActionEvent.SUCCESS,ev);
		
		distressOperator.AddPrecondition(event);
		distressOperator.AddPrecondition((PropertyEqual) subjectIsPerson.clone());
		distressOperator.AddPrecondition((PropertyEqual) subjectIsSelf.clone());
		
		appraisalOperators.add(distressOperator);
		
		return appraisalOperators;
		
	}
	
	

	
	/*public Step getJoyOperator()
	{
		return _joyOperator;
	}*/
	
	/*public Step getDistressOperator()
	{
		return _distressOperator;
	}
	
	public ArrayList<Step> getAppraisalOperators()
	{
		return _appraisalOperators;	
	}*/
	
	
	
}
