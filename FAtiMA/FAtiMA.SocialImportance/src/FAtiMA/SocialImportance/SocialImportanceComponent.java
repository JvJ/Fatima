package FAtiMA.SocialImportance;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;

import javax.xml.XMLConstants;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import FAtiMA.Core.AgentModel;
import FAtiMA.Core.Display.AgentDisplayPanel;
import FAtiMA.Core.componentTypes.IComponent;
import FAtiMA.Core.componentTypes.IModelOfOtherComponent;
import FAtiMA.Core.conditions.Condition;
import FAtiMA.Core.conditions.RecentEventCondition;
import FAtiMA.Core.goals.ActivePursuitGoal;
import FAtiMA.Core.plans.Step;
import FAtiMA.Core.sensorEffector.Event;
import FAtiMA.Core.util.AgentLogger;
import FAtiMA.Core.util.Constants;
import FAtiMA.Core.util.parsers.ReflectXMLHandler;
import FAtiMA.Core.wellFormedNames.Name;
import FAtiMA.Core.wellFormedNames.SubstitutionSet;
import FAtiMA.Core.wellFormedNames.Symbol;
import FAtiMA.DeliberativeComponent.DeliberativeComponent;
import FAtiMA.SocialImportance.display.SIPanel;
import FAtiMA.SocialImportance.parsers.SILoaderHandler;

public class SocialImportanceComponent implements Serializable,IComponent, IModelOfOtherComponent {

	private static final long serialVersionUID = 1L;

	public static float BASE_CONFERRAL_GOAL_IMPORTANCE = 10;
	
	public static float ALPHA_GOAL_IMPORTANCE = 0.1f;

	public static final String NAME = "SocialImportance";

	private ArrayList<String> _parsingFiles;

	private ArrayList<SIClaimRule> _SIClaimRules; 

	private ArrayList<SIConferralRule> _SIConferralRules;

	private ArrayList<SIAttributionRule> _SIAttributionRules;



	public SocialImportanceComponent(ArrayList<String> extraParsingFiles) {


		_parsingFiles = new ArrayList<String>();
		_parsingFiles.addAll(extraParsingFiles);
		_SIClaimRules = new ArrayList<SIClaimRule>();	
		_SIAttributionRules = new ArrayList<SIAttributionRule>();
		_SIConferralRules = new ArrayList<SIConferralRule>();
	}


	public SocialImportanceComponent() {
	}


	@Override
	public AgentDisplayPanel createDisplayPanel(AgentModel am) {
		return new SIPanel();
	}

	@Override
	public IComponent createModelOfOther() {
		SocialImportanceComponent sic = new SocialImportanceComponent();
		sic._SIClaimRules = (ArrayList<SIClaimRule>) _SIClaimRules.clone();
		sic._SIConferralRules = (ArrayList<SIConferralRule>) _SIConferralRules.clone();
		sic._SIAttributionRules = (ArrayList<SIAttributionRule>) _SIAttributionRules.clone();
		return sic;
	}




	@Override
	public String[] getComponentDependencies() {
		String[] dependencies = {};
		return dependencies;
	}

	@Override
	public void initialize(AgentModel am) {
		DeliberativeComponent dc = (DeliberativeComponent) am.getComponent(DeliberativeComponent.NAME);
		dc.setActionFilterStrategy(new SocialClaimFilterStrategy(this));		
	}

	public String name() {
		return SocialImportanceComponent.NAME;
	}

	@Override
	public void reset() {
	}

	@Override
	public void update(AgentModel am, Event e) {
		updateSocialImportanceRelations(am, e);

		/*Check whether the event matches a claim or not*/
		for(SIClaimRule rule : _SIClaimRules){
			if(rule.MatchEvent(e)){
				System.out.println("Found an SI-Claim");
			}
		}

		/*Check whether the event matches a conferral or not*/
		for(SIConferralRule rule : _SIConferralRules){
			if(rule.MatchEvent(e)){
				System.out.println("Found an SI-Conferral");
			}
		}

	}

	@Override
	public void update(AgentModel am, long time) {
	}

	@Override
	public void parseAdditionalFiles(AgentModel am) throws Exception 
	{

		AgentLogger.GetInstance().log("LOADING Social Importance Rules");

		SAXParserFactory saxFactory = SAXParserFactory.newInstance();
		saxFactory.setValidating(false);
		saxFactory.setNamespaceAware(true);
	
		SAXParser SAXparser = saxFactory.newSAXParser();
		SAXparser.setProperty("http://java.sun.com/xml/jaxp/properties/schemaLanguage", XMLConstants.W3C_XML_SCHEMA_NS_URI);
		
		for (String file : _parsingFiles) {
			if(file != null)
			{
				SILoaderHandler loader = new SILoaderHandler(am);
				SAXparser.parse(new File(file),loader);
				_SIClaimRules.addAll(loader.getSIClaimRules());
				this._SIConferralRules.addAll(loader.getSIConferralRules());
				this._SIAttributionRules.addAll(loader.getSIAttributionRules());
			}
		}

		this.createConferralGoals(am);
	}


	private void createConferralGoals(AgentModel am) {

		DeliberativeComponent dc = (DeliberativeComponent) am.getComponent("Deliberative");

		ActivePursuitGoal conferralGoal;

		for(SIConferralRule rule : this._SIConferralRules){
			
			String actionName = rule.getEvent().GetAction();

			for(Step step : am.getActionLibrary().getActions()){
	
				if(actionName.equalsIgnoreCase(step.getName().getFirstLiteral().getName())){
					
					conferralGoal = new ActivePursuitGoal(Name.ParseName(actionName+"([target])"));
					float conferralValue = rule.getValue();										
					
					SICondition siCondition = new SICondition(new Symbol("[target]"), new Symbol(""+conferralValue),SICondition.MORE_THAN_OR_EQUAL,Constants.SELF_STRING);
					conferralGoal.AddCondition("PreConditions", siCondition);
					
					
					Event successEvent = new Event(Constants.SELF_STRING, actionName, "[target]");
					RecentEventCondition recentEventCondition = new RecentEventCondition(true,successEvent);
					conferralGoal.AddCondition("SuccessConditions", recentEventCondition);
					((DeliberativeComponent) am.getComponent(DeliberativeComponent.NAME)).addGoal(conferralGoal);
			
					conferralGoal.SetImportanceOfSuccess(am, (conferralValue + BASE_CONFERRAL_GOAL_IMPORTANCE) * ALPHA_GOAL_IMPORTANCE);
				}


				//Old Mechanism
				/*if(actionName.equalsIgnoreCase(step.getName().GetFirstLiteral().getName())){
					conferralGoal = new ActivePursuitGoal(step.getName());
					for(Condition cond : step.getPreconditions()){				
						conferralGoal.AddCondition("PreConditions", cond);
					}

					for(Effect effect : step.getEffects()){

						conferralGoal.AddCondition("SuccessConditions", effect.GetEffect());		
					}

					((DeliberativeComponent) am.getComponent(DeliberativeComponent.NAME)).addGoal(conferralGoal);

				}*/
			}



		}
	}


	private void updateSocialImportanceRelations(AgentModel aM, Event event){

		//first we reset the values
		SocialImportanceRelation.resetValueAllRelations(aM.getMemory());

		for(SIAttributionRule rule : _SIAttributionRules){

			ArrayList<SubstitutionSet> substitutionsSet = Condition.checkActivation(aM, rule.getConditions());

			if(substitutionsSet != null){

				for(SubstitutionSet sSet : substitutionsSet){
					SIAttributionRule clonedRule = (SIAttributionRule) rule.clone();
					clonedRule.MakeGround(sSet.GetSubstitutions());

					SocialImportanceRelation existingRelation = SocialImportanceRelation.getRelation(Constants.SELF_STRING, clonedRule.getTarget().getName());

					existingRelation.increment(aM.getMemory(), clonedRule.getValue());
				}
			}
		}
	}

	public ArrayList<SIClaimRule> getSIClaimRules(){
		return this._SIClaimRules;
	}

	@Override
	public ReflectXMLHandler getActionsParser(AgentModel am) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public ReflectXMLHandler getGoalsParser(AgentModel am) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public ReflectXMLHandler getPersonalityParser(AgentModel am) {
		// TODO Auto-generated method stub
		return null;
	}


	@Override
	public void validate(AgentModel am) throws Exception {
		// TODO Auto-generated method stub
		
	}
}
