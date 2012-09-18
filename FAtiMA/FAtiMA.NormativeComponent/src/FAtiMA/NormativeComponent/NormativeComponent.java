package FAtiMA.NormativeComponent;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import javax.xml.XMLConstants;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import FAtiMA.Core.AgentModel;
import FAtiMA.Core.Display.AgentDisplayPanel;
import FAtiMA.Core.componentTypes.IAppraisalDerivationComponent;
import FAtiMA.Core.conditions.Condition;
import FAtiMA.Core.emotionalState.AppraisalFrame;
import FAtiMA.Core.exceptions.UnknownGoalException;
import FAtiMA.Core.goals.ActivePursuitGoal;
import FAtiMA.Core.goals.Goal;
import FAtiMA.Core.memory.semanticMemory.SemanticMemory;
import FAtiMA.Core.sensorEffector.Event;
import FAtiMA.Core.util.AgentLogger;
import FAtiMA.Core.util.parsers.ReflectXMLHandler;
import FAtiMA.Core.wellFormedNames.Name;
import FAtiMA.Core.wellFormedNames.Substitution;
import FAtiMA.Core.wellFormedNames.SubstitutionSet;
import FAtiMA.Core.wellFormedNames.Symbol;
import FAtiMA.DeliberativeComponent.DeliberativeComponent;
import FAtiMA.DeliberativeComponent.Intention;
import FAtiMA.NormativeComponent.display.NormativeEvolutionPanel;
import FAtiMA.NormativeComponent.parsers.NormativeLoaderHandler;
import FAtiMA.NormativeComponent.parsers.RolesLoaderHandler;
import FAtiMA.OCCAffectDerivation.OCCAppraisalVariables;
import FAtiMA.ReactiveComponent.ReactiveComponent;

public class NormativeComponent implements IAppraisalDerivationComponent{
	
	
	private static final long serialVersionUID = 1L;

	public static final String NAME = "NormativeComponent";
	
	private int _instantiatedNormsCounter;
	
	private String _normativeFile;
	private String _rolesFile;
	
	private ArrayList<Role> _roleLibrary;
	private ArrayList<Norm> _normLibrary;
	
	private boolean _areNormsChanged;
	private NormativeEnvironment _normativeEnvironment;
	
	public NormativeComponent(String normativeFile, String rolesFile){
		this._instantiatedNormsCounter = 0;
		this._normativeFile = normativeFile;
		this._rolesFile = rolesFile;
		this._roleLibrary = new ArrayList<Role>();
		this._normLibrary = new ArrayList<Norm>();
		this._areNormsChanged = false;
		this._normativeEnvironment = NormativeEnvironment.getNormativeEnvironment();
	}
	
	public boolean areNormsChanged(){
		return _areNormsChanged;
	}
	
	public NormativeEnvironment getNormativeEnvironment(){
		return _normativeEnvironment;
	}
	
	@Override
	public ReflectXMLHandler getActionsParser(AgentModel am){
		return null;
	}
	
	@Override
	public ReflectXMLHandler getGoalsParser(AgentModel am){
		return null;
	}
	
	@Override
	public ReflectXMLHandler getPersonalityParser(AgentModel am) {
		return null;
	}
	
	@Override
	public void parseAdditionalFiles(AgentModel am) throws Exception 
	{
		loadRoles(am);
		loadNorms(am);
	}
	
	public ArrayList<Role> getRoleLibrary(){
		return _roleLibrary;
	}

	public ArrayList<Norm> getNormLibrary(){
		return _normLibrary;
	}

	public void appraisal(AgentModel aM, Event e, AppraisalFrame aF){
		float praiseWorthiness = 0;

		//Appraisal?	
		this.getNormativeEnvironment().getInstancesRecentlyExpired().clear();
		for(Norm instance : this.getNormativeEnvironment().getInstancesCurrentlyActive()){
			if(instance.isViolated(aM)){
				
				praiseWorthiness += -instance.getSalience();
				
				if(instance.getHolder().equals((Name)new Symbol(FAtiMA.Core.util.Constants.SELF_STRING))){
					this.getNormativeEnvironment().getInstancesViolatedBySelf().add(instance);
				}
				else{
					this._normativeEnvironment.getInstancesViolatedByOthers().add(instance);
				}
				this.getNormativeEnvironment().getInstancesRecentlyExpired().add(instance);
				instance.processViolation(aM);
				_areNormsChanged = true;
				AgentLogger.GetInstance().logAndPrint("Instance " + instance.getName() + " Violated by " + instance.getHolder());
			}
			
			else if(instance.isFulfilled(aM)){
				praiseWorthiness += instance.getSalience();
	
				if(instance.getHolder().equals((Name)new Symbol(FAtiMA.Core.util.Constants.SELF_STRING))){
					this.getNormativeEnvironment().getInstancesFulfilledBySelf().add(instance);
				}
				else{
					this._normativeEnvironment.getInstancesFulfilledByOthers().add(instance);
				}
				this.getNormativeEnvironment().getInstancesRecentlyExpired().add(instance);
				instance.processFulfillment(aM);
				_areNormsChanged = true;
				AgentLogger.GetInstance().logAndPrint("Instance " + instance.getName() + " Fulfilled by " + instance.getHolder());
			}
		}
		// end appraisal

		aF.SetAppraisalVariable(NAME, (short)4, OCCAppraisalVariables.PRAISEWORTHINESS.name(), praiseWorthiness);	
	
	}
	
	public AgentDisplayPanel createDisplayPanel(AgentModel aM) {
		return new NormativeEvolutionPanel(this);
	//	return new NormativeDescriptionPanel(this);
	}
	
	public String[] getComponentDependencies(){
		String[] dependencies = {ReactiveComponent.NAME, DeliberativeComponent.NAME};
		return dependencies;
	}
		
	public void initialize(AgentModel aM){

	}
	
	public void inverseAppraisal(AgentModel aM, AppraisalFrame aF){
		
	}
	
	private void loadNorms(AgentModel aM) throws Exception
	{
		NormativeLoaderHandler  normativeLoader = new NormativeLoaderHandler(aM, this);
	
		SAXParserFactory saxFactory = SAXParserFactory.newInstance();
		saxFactory.setValidating(true);
		saxFactory.setNamespaceAware(true);
	
		SAXParser SAXparser = saxFactory.newSAXParser();
		SAXparser.setProperty("http://java.sun.com/xml/jaxp/properties/schemaLanguage", XMLConstants.W3C_XML_SCHEMA_NS_URI);
		
		SAXparser.parse(new File(this._normativeFile), normativeLoader);
		
		AgentLogger.GetInstance().logAndPrint("LOADING OBLIGATIONS....");
		
		for(Norm norm : normativeLoader.getObligations()){
			_normLibrary.add(norm);
			this.getNormativeEnvironment().getNormsAdoptedBySelf().add(norm);
			AgentLogger.GetInstance().logAndPrint("Obligation " + norm.getName() + " loaded with id = " + norm.getID());
		}
		
		AgentLogger.GetInstance().logAndPrint("LOADING PROHIBITIONS....");
		
		for(Norm norm : normativeLoader.getProhibitions()){
			_normLibrary.add(norm);
			this.getNormativeEnvironment().getNormsAdoptedBySelf().add(norm);
			AgentLogger.GetInstance().logAndPrint("Prohibition " + norm.getName() + " loaded with id = " + norm.getID());
		}
	}
	
	private void loadRoles(AgentModel aM) throws Exception
	{
		AgentLogger.GetInstance().logAndPrint("LOADING ROLES....");
		RolesLoaderHandler  rolesLoader = new RolesLoaderHandler(aM, this);
		
		SAXParserFactory saxFactory = SAXParserFactory.newInstance();
		saxFactory.setValidating(true);
		saxFactory.setNamespaceAware(true);
	
		SAXParser SAXparser = saxFactory.newSAXParser();
		SAXparser.setProperty("http://java.sun.com/xml/jaxp/properties/schemaLanguage", XMLConstants.W3C_XML_SCHEMA_NS_URI);
		
		SAXparser.parse(new File(this._rolesFile), rolesLoader);
			
		for(Role role : rolesLoader.getRoles()){
			_roleLibrary.add(role);
			AgentLogger.GetInstance().logAndPrint("Role " + role.getName() + " loaded");
		}
	}
	
	public String name(){
		return NormativeComponent.NAME;
	}
	
	private void normManagement(AgentModel aM, Event event){
		_areNormsChanged = false;
		this.generateRecentlyCreatedInstances(aM, event);
		for(Norm instance : this.getNormativeEnvironment().getInstancesRecentlyCreated()){
			this.getNormativeEnvironment().getInstancesCurrentlyActive().add(instance);
			if(instance.getHolder().equals((Name)new Symbol(FAtiMA.Core.util.Constants.SELF_STRING))){
				this.getNormativeEnvironment().getInstancesFollowedBySelf().add(instance);
				if(instance instanceof Obligation){
					DeliberativeComponent dc = (DeliberativeComponent)aM.getComponent(DeliberativeComponent.NAME);
					Iterator<Intention> it = dc.getIntentionsIterator();
					float goalUrgency = instance.computeGoalUrgency() / 4;
					while(it.hasNext()){
						Intention intention = it.next();
						if(((Obligation)instance).expiresByFollwingPlan(intention.GetBestPlan(aM))){
							goalUrgency = goalUrgency * 4;
							break;
						}							
					}
					ActivePursuitGoal goal = (ActivePursuitGoal)instance.generateGoal(aM);
					goal.SetImportanceOfSuccess(aM, instance.computeGoalImportanceOfSuccess());
					goal.SetImportanceOfFailure(aM, instance.computeGoalImportanceOfFailure());
					goal.setUrgency(goalUrgency);
					dc.addIntention(aM, goal);

					AgentLogger.GetInstance().logAndPrint("Active Pursuit Goal " + goal.getName() + " generated");

					//dc.resetAllIntentions(aM);

				}
				else if(instance instanceof Prohibition){
					Goal goal = instance.generateGoal(aM);
					aM.getGoalLibrary().AddGoal(goal);
					DeliberativeComponent dc = (DeliberativeComponent)aM.getComponent(DeliberativeComponent.NAME);
					try{
						float ios = instance.computeGoalImportanceOfSuccess();
						float iof = instance.computeGoalImportanceOfFailure();
						dc.addGoal(aM, instance.getName().getFirstLiteral().getName(), ios, iof);
						dc.resetExecution();
						dc.resetAllIntentions(aM);
						
						AgentLogger.GetInstance().logAndPrint("Interest Goal " + goal.getName() + " generated");
						
					}
					catch(UnknownGoalException uge){
						//Should never happen
						uge.printStackTrace();
					}
				}
			}
			else{
				this.getNormativeEnvironment().getInstancesFollowedByOthers().add(instance);
			}
		}
		this.getNormativeEnvironment().getInstancesRecentlyCreated().clear();
		
		for(Norm instance : this.getNormativeEnvironment().getInstancesRecentlyExpired()){
			if(instance.getHolder().equals((Name)new Symbol(FAtiMA.Core.util.Constants.SELF_STRING))){
				this.getNormativeEnvironment().getInstancesFollowedBySelf().remove(instance);
			}
			else{
				this.getNormativeEnvironment().getInstancesFollowedByOthers().remove(instance);
			}
			this.getNormativeEnvironment().getInstancesCurrentlyActive().remove(instance);
			this.getNormativeEnvironment().getInstancesOfThePast().add(instance);
		}
		this.getNormativeEnvironment().getInstancesRecentlyExpired().clear();
	}
	
	public AppraisalFrame reappraisal(AgentModel aM) {
		return null;	
	}
	
	public void reset(){
		
	}


	public void update(AgentModel aM, Event e){
		updateRoles(aM, e);
		normManagement(aM, e);
	}
	
	public void update(AgentModel aM, long time){
		
	}

	private void generateRecentlyCreatedInstances(AgentModel aM, Event event){
		this.getNormativeEnvironment().getInstancesRecentlyCreated().clear();
		for(Norm norm : this.getNormativeEnvironment().getNormsAdoptedBySelf()){
			ArrayList<SubstitutionSet> substitutionsSet = Condition.checkActivation(aM, norm.getActivationConditions());
			if(substitutionsSet != null){
				for(SubstitutionSet sSet : substitutionsSet){
					Norm instance = (Norm) norm.clone();
					instance.MakeGround(sSet.GetSubstitutions());
					if(!instance.isGrounded()){
						AgentLogger.GetInstance().logAndPrint("Activation conditions of norm " + norm.getName().getFirstLiteral() + " can not completly ground it");
						continue;
					}
					for(int i = 0; i < sSet.GetSubstitutions().size(); i++){
						Substitution sub = sSet.GetSubstitutions().get(i);
						if(sub.getVariable().equals(new Symbol("[AGENT]"))){
							instance.setHolder(sub.getValue());
						}
					}
					if(instance.hasHolder() && 
					   instance.isAgentATarget(instance.getHolder(), aM) && 
					   !this.getNormativeEnvironment().getInstancesCurrentlyActive().contains(instance)){
							_instantiatedNormsCounter++;
							instance.setID(_instantiatedNormsCounter);
							this.getNormativeEnvironment().getInstancesRecentlyCreated().add(instance);
							AgentLogger.GetInstance().logAndPrint("The norm " + instance.getName() + " is now instantiated for agent " + instance.getHolder() + " with id: " + instance.getID());
							_areNormsChanged = true;
					}
				}
			}
		}
	}
	
	private void updateRoles(AgentModel aM, Event event){
		SemanticMemory memory = aM.getMemory().getSemanticMemory();
		for(Role role : this.getRoleLibrary()){
			ArrayList<SubstitutionSet> substitutionsSet = Condition.checkActivation(aM, role.getActivationConditions());
			if(substitutionsSet != null){
				for(SubstitutionSet sSet : substitutionsSet){		
					Role r2 = (Role)role.clone();
					r2.MakeGround(sSet.GetSubstitutions());								
					for(int i = 0; i < sSet.GetSubstitutions().size(); i++){
						Substitution sub = sSet.GetSubstitutions().get(i);
						if(sub.getVariable().equals(new Symbol("[AGENT]"))){
							r2.setHolder(sub.getValue());
						}
					}
					if(r2.hasHolder() && !memory.AskPredicate(r2.getPropertyName())){
						memory.Tell(true, r2.getPropertyName(), new Symbol("True"));
						AgentLogger.GetInstance().logAndPrint("Agent " + r2.getHolder() + " adopted the role of " + r2.getName());							
					}
				}
			}
			substitutionsSet = Condition.checkActivation(aM, role.getExpirationConditions());
			if(substitutionsSet != null){
				for(SubstitutionSet sSet : substitutionsSet){		
					Role r2 = (Role)role.clone();
					r2.MakeGround(sSet.GetSubstitutions());								
					for(int i = 0; i < sSet.GetSubstitutions().size(); i++){
						Substitution sub = sSet.GetSubstitutions().get(i);
						if(sub.getVariable().equals(new Symbol("[AGENT]"))){
							r2.setHolder(sub.getValue());
						}
					}
					if(r2.hasHolder() && memory.AskPredicate(r2.getPropertyName())){
						memory.Tell(true, r2.getPropertyName(), new Symbol("False"));
						AgentLogger.GetInstance().logAndPrint("Agent " + r2.getHolder() + " left the role of " + r2.getName());							
					}
				}
			}
		}
	}

	@Override
	public void validate(AgentModel am) throws Exception {
		// TODO Auto-generated method stub
		
	}
	
}
