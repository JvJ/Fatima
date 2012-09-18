package FAtiMA.Core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.logging.Logger;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import FAtiMA.Core.Display.AgentDisplay;
import FAtiMA.Core.Display.AgentDisplayPanel;
import FAtiMA.Core.componentTypes.IAdvancedPerceptionsComponent;
import FAtiMA.Core.componentTypes.IAffectDerivationComponent;
import FAtiMA.Core.componentTypes.IAppraisalDerivationComponent;
import FAtiMA.Core.componentTypes.IBehaviourComponent;
import FAtiMA.Core.componentTypes.IComponent;
import FAtiMA.Core.componentTypes.IModelOfOtherComponent;
import FAtiMA.Core.componentTypes.IProcessEmotionComponent;
import FAtiMA.Core.componentTypes.IProcessExternalRequestComponent;
import FAtiMA.Core.conditions.Condition;
import FAtiMA.Core.emotionalState.ActiveEmotion;
import FAtiMA.Core.emotionalState.AppraisalFrame;
import FAtiMA.Core.emotionalState.BaseEmotion;
import FAtiMA.Core.emotionalState.EmotionalState;
import FAtiMA.Core.emotionalState.NeutralEmotion;
import FAtiMA.Core.exceptions.AgentParsingException;
import FAtiMA.Core.exceptions.GoalLibParsingException;
import FAtiMA.Core.exceptions.RequiredComponentException;
import FAtiMA.Core.exceptions.UndefinedComponentException;
import FAtiMA.Core.exceptions.UnknownGoalException;
import FAtiMA.Core.goals.Goal;
import FAtiMA.Core.goals.GoalLibrary;
import FAtiMA.Core.memory.Memory;
import FAtiMA.Core.memory.semanticMemory.KnowledgeSlot;
import FAtiMA.Core.perceptions.EntitiesPerception;
import FAtiMA.Core.perceptions.EntityRemovedPerception;
import FAtiMA.Core.perceptions.LookAtPerception;
import FAtiMA.Core.perceptions.PropertyPerception;
import FAtiMA.Core.perceptions.PropertyRemovedPerception;
import FAtiMA.Core.sensorEffector.Event;
import FAtiMA.Core.sensorEffector.IONRemoteAgent;
import FAtiMA.Core.sensorEffector.RemoteAgent;
import FAtiMA.Core.sensorEffector.SpeechAct;
import FAtiMA.Core.sensorEffector.WorldSimulatorRemoteAgent;
import FAtiMA.Core.util.AgentLogger;
import FAtiMA.Core.util.ConfigurationManager;
import FAtiMA.Core.util.Constants;
import FAtiMA.Core.util.VersionChecker;
import FAtiMA.Core.util.enumerables.AgentPlatform;
import FAtiMA.Core.util.parsers.ActionsLoaderHandler;
import FAtiMA.Core.util.parsers.BinaryStringConverter;
import FAtiMA.Core.util.parsers.CentralXMLParser;
import FAtiMA.Core.util.parsers.EmotionDispositionsLoaderHandler;
import FAtiMA.Core.util.parsers.GoalLoaderHandler;
import FAtiMA.Core.util.parsers.MemoryLoaderHandler;
import FAtiMA.Core.util.parsers.ReflectXMLHandler;
import FAtiMA.Core.util.writers.MemoryWriter;
import FAtiMA.Core.wellFormedNames.Name;
import FAtiMA.Core.wellFormedNames.Symbol;


public class AgentCore implements Serializable, AgentModel, IGetModelStrategy {

	private static final long serialVersionUID = 1L;
	public static final String MEMORY_FILENAME = "XMLMemory";
	
	public static final Name ACTION_CONTEXT = Name.ParseName("ActionContext()");

	//components
	protected HashMap<String, IComponent> _generalComponents;
	protected ArrayList<IProcessEmotionComponent> _processEmotionComponents;
	protected ArrayList<IBehaviourComponent> _behaviourComponents;
	protected ArrayList<IModelOfOtherComponent> _modelOfOtherComponents;
	protected ArrayList<IProcessExternalRequestComponent> _processExternalRequestComponents;
	protected ArrayList<IAdvancedPerceptionsComponent> _processPerceptionsComponents;
	protected ArrayList<IAffectDerivationComponent> _affectDerivationComponents;
	protected ArrayList<IAppraisalDerivationComponent> _appraisalComponents;
	
	//Strategies
	//protected IDetectThreatStrategy _detectThreatStrat;
	
	//Data structures
	
	protected EmotionalState _emotionalState;
	protected Memory _memory;
	protected GoalLibrary _goalLibrary;
	protected ActionLibrary _actionLibrary;
	protected ArrayList<ValuedAction> _actionsForExecution;
	//Henrique Reis
	private HashMap<String, String> _actionsVisibility;
	
	//perception structures
	protected ArrayList<Event> _perceivedActions;
	protected ArrayList<Event> _perceivedActionFailures;
	protected ArrayList<LookAtPerception> _perceivedLookAts;
	protected ArrayList<EntitiesPerception> _perceivedEntities;
	protected ArrayList<EntityRemovedPerception> _perceivedEntitiesRemoved;
	protected ArrayList<PropertyPerception> _perceivedProperties;
	protected ArrayList<PropertyRemovedPerception> _perceivedPropertiesRemoved;
	
	//state and configuration variables
	protected boolean _shutdown;
	protected RemoteAgent _remoteAgent;
	protected String _role;
	protected String _name; //the agent's name
	protected String _sex;
	protected String _displayName; 
	protected SpeechAct _speechAct;
	protected String _currentEmotion;
	protected long _numberOfCycles;
	protected long _totalexecutingtime=0;

	protected AgentDisplay _agentDisplay;
	protected boolean _showStateWindow;
	protected Logger _logger;

	private String _saveDirectory;
	private boolean _saveRequest = false;
	private boolean _agentLoaded = false;
	private boolean _saveMemoryRequest = false;
	
	private MemoryWriter _memoryWriter;
	private IGetModelStrategy _strat;


	public AgentCore(String name){
		_name = name;
		_shutdown = false;
		_numberOfCycles = 0;
		_currentEmotion = NeutralEmotion.NAME; //neutral emotion - no emotion
		_actionsForExecution = new ArrayList<ValuedAction>();
		_saveDirectory = "";
		
		//data structures
		_emotionalState = new EmotionalState();
		_memory = new Memory();
		// creating a new episode when the agent starts 13/09/10
		_memory.getEpisodicMemory().StartEpisode(_memory);
		_memoryWriter = new MemoryWriter(_memory);
		_strat = this;
		_actionLibrary = new ActionLibrary();
		_goalLibrary = new GoalLibrary();
		_actionsVisibility = new HashMap<String, String>();
		
		//perception structures
		_perceivedActions = new ArrayList<Event>();
		_perceivedActionFailures = new ArrayList<Event>();
		_perceivedLookAts = new ArrayList<LookAtPerception>();
		_perceivedEntities = new ArrayList<EntitiesPerception>();
		_perceivedEntitiesRemoved = new ArrayList<EntityRemovedPerception>();
		_perceivedProperties = new ArrayList<PropertyPerception>();
		_perceivedPropertiesRemoved = new ArrayList<PropertyRemovedPerception>();
		
		//component lists
		_generalComponents = new HashMap<String,IComponent>();
		_processEmotionComponents = new ArrayList<IProcessEmotionComponent>();
		_behaviourComponents = new ArrayList<IBehaviourComponent>();
		_modelOfOtherComponents = new ArrayList<IModelOfOtherComponent>();
		_processExternalRequestComponents = new ArrayList<IProcessExternalRequestComponent>();
		_processPerceptionsComponents = new ArrayList<IAdvancedPerceptionsComponent>();
		_affectDerivationComponents = new ArrayList<IAffectDerivationComponent>();
		_appraisalComponents = new ArrayList<IAppraisalDerivationComponent>();
		
		
		AgentSimulationTime.GetInstance(); //This call will initialize the timer for the agent's simulation time
	}


	public void initialize(String scenarioFolder, String scenariosFile, String scenarioName, String agentName) throws ParserConfigurationException, SAXException, IOException, UnknownGoalException, GoalLibParsingException{
		
		if (VersionChecker.runningOnAndroid()){
			ConfigurationManager.initialize(scenarioFolder,scenariosFile,scenarioName,agentName);
		}else{
			ConfigurationManager.initialize(scenarioFolder,scenariosFile,scenarioName,agentName);
		}
				
		try{
			if (ConfigurationManager.getAgentLoad())
			{
				_agentLoaded = true;
				agentCoreLoad(ConfigurationManager.getPlatform(), ConfigurationManager.getHost(), ConfigurationManager.getPort(), ConfigurationManager.getSaveDirectory(), agentName);
			}
			else
			{
				_agentLoaded = false;
				
				_showStateWindow = ConfigurationManager.getDebugMode();
				_displayName = ConfigurationManager.getDisplayName();
				_role = ConfigurationManager.getRole();
				_sex = ConfigurationManager.getSex();
				_saveDirectory = ConfigurationManager.getSaveDirectory();
				_memory.setSaveDirectory(_saveDirectory);
				
				AgentLogger.GetInstance().initialize(_name,_showStateWindow);
				
				if(_showStateWindow && !VersionChecker.runningOnAndroid()){
					_agentDisplay = new AgentDisplay(this);
				}
	
				//loading agent memory from xml
				if (ConfigurationManager.getMemoryLoad())
				{
					_memory.setMemoryLoad(true);
					loadAgentMemory(_saveDirectory + MEMORY_FILENAME);
				}
				else
				{
					_memory.setMemoryLoad(false);
				}
				
				//Start the remote agent socket
				//this was moved from initialize method to here, because it has to be done after all parsing
				_remoteAgent = createNewRemoteAgent(ConfigurationManager.getPlatform(), ConfigurationManager.getHost(), ConfigurationManager.getPort(), ConfigurationManager.getAgentProperties());
				_remoteAgent.start();
			}
			
		}catch (Exception e) {
			e.printStackTrace();
			AgentLogger.GetInstance().log("Exception: " + e);
			terminateExecution();
		}
	}


	private RemoteAgent createNewRemoteAgent(short aP, String host, int port, HashMap<String,String> properties) throws IOException{
		if(aP == AgentPlatform.WORLDSIM){
			properties.put("name", _name); 
			properties.put("role", _role);
			properties.put("sex", _sex);	
			return new WorldSimulatorRemoteAgent(host, port, this, properties);
		}else if (aP == AgentPlatform.ION){
			return new IONRemoteAgent(host, port, this);	
		}else{
			throw new RuntimeException("startRemoteAgent: AgentPlatform has an incorrect value:" + aP);
		}
	}
	
	public void agentCoreLoad(short agentPlatform, String host, int port, String directory, String fileName)
	{
		try{
			_shutdown = false;
			_numberOfCycles = 0;
			LoadAgentState(directory + fileName);
						
			AgentLogger.GetInstance().initialize(fileName,_showStateWindow);

			if(_showStateWindow && !VersionChecker.runningOnAndroid()){
				_agentDisplay = new AgentDisplay(this);
				for (IComponent c: this.getComponents())
				{
					AgentDisplayPanel panel = c.createDisplayPanel(this);
					if(panel != null)
					{
						this._agentDisplay.AddPanel(panel, c.name(),"");
					}
				}	
			}
			
			// creating a new episode when the agent loads 13/09/10
			_memory.getEpisodicMemory().StartEpisode(_memory);
			_remoteAgent = this.createNewRemoteAgent(agentPlatform, host, port, new HashMap<String,String>());
			_remoteAgent.LoadState(directory + fileName + "-RemoteAgent.dat");
		}
		catch (Exception e) {
			e.printStackTrace();
			this.terminateExecution();
		}
	}
	
	public boolean isSelf()
	{
		return true;
	}


	public void addComponent(IComponent c)
	{
		for(String dependency : c.getComponentDependencies()){
			if (!this._generalComponents.containsKey(dependency))
				throw new RequiredComponentException(c.name(),dependency);
		}
			
		this._generalComponents.put(c.name(), c);
		if(c instanceof IProcessEmotionComponent)
		{
			_processEmotionComponents.add((IProcessEmotionComponent) c);
		}
		if(c instanceof IBehaviourComponent)
		{
			_behaviourComponents.add((IBehaviourComponent) c);
		}
		if(c instanceof IModelOfOtherComponent)
		{
			_modelOfOtherComponents.add((IModelOfOtherComponent) c);
		}
		if(c instanceof IProcessExternalRequestComponent)
		{
			_processExternalRequestComponents.add((IProcessExternalRequestComponent) c);
		}
		if(c instanceof IAdvancedPerceptionsComponent)
		{
			_processPerceptionsComponents.add((IAdvancedPerceptionsComponent) c);
		}
		if(c instanceof IAffectDerivationComponent)
		{
			_affectDerivationComponents.add((IAffectDerivationComponent) c);
		}
		if(c instanceof IAppraisalDerivationComponent)
		{
			_appraisalComponents.add((IAppraisalDerivationComponent) c);
		}
		
		c.initialize(this);
		
		AgentDisplayPanel panel = c.createDisplayPanel(this);
		if(panel != null & _showStateWindow)
		{
			this._agentDisplay.AddPanel(panel, c.name(),"");
		}
	}
	
	public RemoteAgent getRemoteAgent()
	{
		return this._remoteAgent;
	}

	public IComponent getComponent(String name)
	{
		if(!this._generalComponents.containsKey(name))
		{
			throw new UndefinedComponentException(name);
		}
		else return this._generalComponents.get(name);
	}

	public Collection<IComponent> getComponents()
	{
		return this._generalComponents.values();
	}
	
	public Collection<IProcessExternalRequestComponent> getProcessExternalRequestComponents()
	{
		return this._processExternalRequestComponents;
	}
	
	public ActionLibrary getActionLibrary()
	{
		return _actionLibrary;
	}

	/*public void RemoveComponent(IComponent c)
	{
		this._components.remove(c.name());
	}*/

	/**
	 * Gets the agent's name that is displayed externally
	 * @return the agent's external name
	 */
	public String displayName() {
		return _displayName;
	}


	public EmotionalState getEmotionalState()
	{
		return _emotionalState;
	}


	public Memory getMemory()
	{
		return _memory;
	}

	public boolean getAgentLoad()
	{
		return _agentLoaded;
	}
	
	public GoalLibrary getGoalLibrary()
	{
		return _goalLibrary;
	}
	
	/**
	 * Gets the name of the agent
	 * @return the agent's name
	 */
	public String getName() {
		return _name;
	}


	public void setModelStrategy(IGetModelStrategy strat)
	{
		_strat = strat;
	}
	
	public void EnforceCopingStrategy(AgentModel am, String coping)
	{
		Goal g;
		coping = coping.toLowerCase();
		for(ListIterator<Goal> li = _goalLibrary.GetGoals();li.hasNext();)
		{
			g = (Goal) li.next();
			if(g.getName().toString().toLowerCase().startsWith(coping)
					|| (coping.equals("standup") && g.getName().toString().startsWith("ReplyNegatively")))
			{
				AgentLogger.GetInstance().logAndPrint("");
				AgentLogger.GetInstance().logAndPrint("Enforcing coping strategy: " + g.getName());
				AgentLogger.GetInstance().logAndPrint("");
				g.IncreaseImportanceOfFailure(am, 2);
				g.IncreaseImportanceOfSuccess(am, 2);
			}
		}
	}

	/**
	 * Perceives a given event from the virtual world
	 * @param e - the Event to perceive
	 */
	public void PerceiveEvent(Event e) 
	{   
		synchronized (this)
		{	
			Event e2 = e.applyPerspective(_name);
			_perceivedActions.add(e2);
		}
	}
	
	public void PerceiveActionFailed(Event e)
	{
		Event e2 = e.applyPerspective(_name);
		_perceivedActionFailures.add(e2);
	}
	
	public void PerceiveLookAt(String subject, String target)
	{
		String auxTarget;
		String auxSubject;
		
		auxSubject = Name.applyPerspective(subject, _name);
		
		auxTarget = Name.applyPerspective(target, _name);

		_perceivedLookAts.add(new LookAtPerception(auxSubject, auxTarget));
	}
	
	//by Henrique Reis
	public void PerceiveEntities(ArrayList<String> entities) 
	{
		_perceivedEntities.add(new EntitiesPerception(entities));
	}

	public void PerceivePropertyChanged(PropertyPerception p)
	{
		p.applyPerspective(_name);
		_perceivedProperties.add(p);
	}

	public void PerceivePropertyRemoved(String subject, String property)
	{
		if(subject.equals(_name))
		{
			subject = Constants.SELF_STRING;
		}

		_perceivedPropertiesRemoved.add(new PropertyRemovedPerception(subject, property));
	}

	public void PerceiveEntityRemoved(String entity)
	{
		if(entity.equals(_name))
		{
			entity = Constants.SELF_STRING;
		}
		
		_perceivedEntitiesRemoved.add(new EntityRemovedPerception(entity));
	}

	/**
	 * Resets the agent's reasoning layers (deliberative + cognitive)
	 *
	 */
	public void Reset() {
		for(IComponent c : this._generalComponents.values())
		{
			c.reset();
		}
	}
	
	private void LoadActionsFile() throws Exception
	{
		ReflectXMLHandler parser;
		// Load Actions file
		CentralXMLParser centralParser = new CentralXMLParser();
		centralParser.addParser(new ActionsLoaderHandler(_actionLibrary, this));
		
		for(IComponent c : this._generalComponents.values())
		{
			parser = c.getActionsParser(this);
			if(parser!= null)
			{
				centralParser.addParser(parser);
			}
		}

		SAXParserFactory saxFactory = SAXParserFactory.newInstance();
		saxFactory.setValidating(false);
		saxFactory.setNamespaceAware(true);
		
		SchemaFactory  schemaFactory = 
		    SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

		saxFactory.setSchema(schemaFactory.newSchema(new File(ConfigurationManager.getMindPath() + "../schemas/Actions.xsd")));

		SAXParser SAXparser = saxFactory.newSAXParser();
		
		SAXparser.parse(new File(ConfigurationManager.getActionsFile()), centralParser);
		
	}
	
	private void LoadGoalsFile() throws Exception
	{
		ReflectXMLHandler parser;
		// Load Actions file
		CentralXMLParser centralParser = new CentralXMLParser();
		centralParser.addParser(new GoalLoaderHandler(this));
		
		for(IComponent c : this._generalComponents.values())
		{
			parser = c.getGoalsParser(this);
			if(parser!= null)
			{
				centralParser.addParser(parser);
			}
		}
		
		SAXParserFactory saxFactory = SAXParserFactory.newInstance();
		saxFactory.setValidating(false);
		saxFactory.setNamespaceAware(true);
		
		SchemaFactory  schemaFactory = 
		    SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

		saxFactory.setSchema(schemaFactory.newSchema(new File(ConfigurationManager.getMindPath() + "../schemas/GoalLibrary.xsd")));

		SAXParser SAXparser = saxFactory.newSAXParser();
		
		SAXparser.parse(new File(ConfigurationManager.getGoalsFile()), centralParser);
	}
	
	private void LoadPersonalityFile() throws Exception
	{
		ReflectXMLHandler parser;
		// Load Actions file
		CentralXMLParser centralParser = new CentralXMLParser();
		centralParser.addParser(new EmotionDispositionsLoaderHandler(this._emotionalState));
		
		for(IComponent c : this._generalComponents.values())
		{
			parser = c.getPersonalityParser(this);
			if(parser!= null)
			{
				centralParser.addParser(parser);
			}
		}
		
		SAXParserFactory saxFactory = SAXParserFactory.newInstance();
		saxFactory.setValidating(false);
		saxFactory.setNamespaceAware(true);
		
		SchemaFactory  schemaFactory = 
		    SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

		saxFactory.setSchema(schemaFactory.newSchema(new File(ConfigurationManager.getMindPath() + "../schemas/Character.xsd")));

		SAXParser SAXparser = saxFactory.newSAXParser();
		
		SAXparser.parse(new File(ConfigurationManager.getPersonalityFile()), centralParser);		
	}
	

	/**
	 * Gets the agent's role
	 * @return the role of the agent (Victim, Bully, etc)
	 */
	public String role() {
		return _role;
	}
	
	private void lateInitialization() throws Exception
	{
		//parsing the files will be done right before the first run
		LoadActionsFile();
		LoadGoalsFile();
		LoadPersonalityFile();
			
		for(IComponent c : this._generalComponents.values())
		{
			c.parseAdditionalFiles(this);
		}
		
		IntegrityValidator validator = new IntegrityValidator(_actionLibrary.getActions());
		
		_actionLibrary.checkIntegrity(this,validator);
		_goalLibrary.checkIntegrity(this,validator);
		
		for(IComponent c : this._generalComponents.values())
		{
			c.validate(this);
		}
		
	}
	
	/**
	 *  this method perceives and appraises new events
	 */
	private void perceive()
	{
		AppraisalFrame appraisalFrame;
		
		//we use a lock on the remote agent. The idea is that the perceptual process should be 
		//blocked while the remote agent is getting messages from the external world.
		synchronized(_remoteAgent)
		{
			
			for(PropertyPerception p : this._perceivedProperties)
			{
				AgentLogger.GetInstance().logAndPrint("PropertyChanged: " + p.getToM() + " " + p.getProperty() + " " + p.getValue());
				
				_memory.getSemanticMemory().Tell(p.getPersistent(),p.getProperty(), p.getValue());
				
				for(IAdvancedPerceptionsComponent c : this._processPerceptionsComponents)
				{
					c.propertyChangedPerception(this, p);
				}
			}
			this._perceivedProperties.clear();
			
			for(EntitiesPerception p : this._perceivedEntities)
			{
				AgentLogger.GetInstance().logAndPrint("Perceiving Entities: " + p.getEntities().toString());
				for(String entity : p.getEntities())
				{
					if(!entity.equalsIgnoreCase(this.getName())) 
					{
						for(IAdvancedPerceptionsComponent c : this._processPerceptionsComponents)
						{
							//only tom component makes use of this method
							c.otherAgentPerception(this, entity, p.getEntities());
						}
					}
				}
			}
			this._perceivedEntities.clear();
			
			for(LookAtPerception p : this._perceivedLookAts)
			{
				for(IAdvancedPerceptionsComponent c : this._processPerceptionsComponents)
				{
					c.lookAtPerception(this, p.getSubject(), p.getTarget());
				}
			}
			this._perceivedLookAts.clear();
			
			
			for(PropertyRemovedPerception p : this._perceivedPropertiesRemoved)
			{
				Name propertyName = Name.ParseName(p.getSubject() + "(" + p.getProperty() + ")");
				AgentLogger.GetInstance().logAndPrint("PropertyRemoved: " +  propertyName);

				_memory.getSemanticMemory().Retract(propertyName);
			}
			this._perceivedPropertiesRemoved.clear();
			
			for(EntityRemovedPerception p : this._perceivedEntitiesRemoved)
			{
				for(IAdvancedPerceptionsComponent c : this._processPerceptionsComponents)
				{
					c.entityRemovedPerception(this,p.getSubject());
				}
			}
			this._perceivedEntitiesRemoved.clear();
			
			for(Event e : this._perceivedActionFailures)
			{
				for(IAdvancedPerceptionsComponent c : _processPerceptionsComponents)
				{
					c.actionFailedPerception(this,e);
				}
			}
			this._perceivedActionFailures.clear();
			
			
			for(Event e : this._perceivedActions)
			{
				//first we need to store all events in memory even before any update method is 
				//processed. For instance Deliberative update will check for actions in episodic
				//memory
				_memory.getEpisodicMemory().StoreAction(_memory, e);
				_memory.getSemanticMemory().Tell(true,ACTION_CONTEXT, e.toName().toString());
			}
			
			for(IComponent c : this._generalComponents.values())
			{
				c.update(this, AgentSimulationTime.GetInstance().Time());
			}
			
			for(Event e : this._perceivedActions)
			{
				AgentLogger.GetInstance().log("appraising event: " + e.toName());
					
				for(IComponent c : this._generalComponents.values())
				{
					c.update(this, e);
				}
				
				appraisalFrame = new AppraisalFrame(e);
				for(IAppraisalDerivationComponent c : this._appraisalComponents)
				{
					c.appraisal(this,e, appraisalFrame);
					updateEmotions(appraisalFrame);
				}
			}
			this._perceivedActions.clear();
		}
	}

	/**
	 * Runs the agent, endless loop until there is a shutdown
	 */
	public void Run() {
		ValuedAction action;
		ValuedAction bestAction;
		float bestActionValue;
		float value;
		AppraisalFrame appraisalFrame;
		
		try
		{
			lateInitialization();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return;
		}
		
		long updateTime = System.currentTimeMillis();

		while (!_shutdown) {
			try {
				if(_remoteAgent.isShutDown()) {
					_shutdown = true;
				}
				
				updateSimulationTimer();
				long startCycleTime = System.currentTimeMillis();

				if (_remoteAgent.isRunning()) {
					
					_emotionalState.Decay();

					//perception and appraisal
					this.perceive();
					
					//continuous reappraisal
					for(IAppraisalDerivationComponent c : this._appraisalComponents)
					{
						appraisalFrame = c.reappraisal(this);
						if(appraisalFrame != null)
						{
							updateEmotions(appraisalFrame);
						}
					}
					
					//if there was new data or knowledge added we must apply inference operators
					//update any inferred property to the outside and appraise the events
					if(_memory.getEpisodicMemory().HasNewData() ||
							_memory.getSemanticMemory().HasNewKnowledge())
					{
						//just to clear facts added before the inference process.
						_memory.getSemanticMemory().getNewFacts();

						//calling the KnowledgeBase inference process
						_memory.getSemanticMemory().PerformInference(this);

						synchronized (_memory.getSemanticMemory())
						{
							ArrayList<KnowledgeSlot> facts = _memory.getSemanticMemory().getNewFacts();

							for(ListIterator<KnowledgeSlot> li = facts.listIterator();li.hasNext();)
							{
								KnowledgeSlot ks = (KnowledgeSlot) li.next();
								if(ks.getDisplayName().startsWith(Constants.SELF_STRING))
								{
									_remoteAgent.ReportInternalPropertyChange(this._name,Name.ParseName(ks.getDisplayName()),ks.getValue());
								}
							}
						}
					}

					bestActionValue = -1;
					bestAction = null;
					
					for(IBehaviourComponent c : this._behaviourComponents)
					{
						action = c.actionSelection(this);
						if(action!= null)
						{
							value = action.getValue(_emotionalState);
							if(value > bestActionValue)
							{
								bestActionValue = value;
								bestAction = action;
							}
						}
						
					}

					if(_remoteAgent.canAct() && _remoteAgent.isRunning() && bestAction != null) {
						
						_remoteAgent.AddAction(bestAction);
						IBehaviourComponent c = (IBehaviourComponent) getComponent(bestAction.getComponent());
						c.actionSelectedForExecution(this,bestAction);

						_remoteAgent.ExecuteNextAction(this);
					}

					if(System.currentTimeMillis() - updateTime > 1000)
					{
						if(_showStateWindow && _agentDisplay != null) 
						{
							_agentDisplay.update();
						}

						_remoteAgent.ReportInternalState(_emotionalState);

						/*ActiveEmotion auxEmotion = EmotionalState.GetInstance().GetStrongestEmotion();
						short nextEmotion;
						if(auxEmotion != null) {
						    nextEmotion = auxEmotion.GetType(); 
						}
						else nextEmotion = EmotionType.NEUTRAL;

						if(_currentEmotion != nextEmotion) {
						    _currentEmotion = nextEmotion;
						    _remoteAgent.ExpressEmotion(EmotionType.GetName(_currentEmotion));
						}*/

						updateTime = System.currentTimeMillis();
					}

					if(_saveRequest)
					{
						_saveRequest = false;
						SaveAgentState(this.getName());
					}
					if(_saveMemoryRequest)
					{
						_saveMemoryRequest = false;
						SaveAgentMemory();
					}
				}

				long cycleExecutionTime = System.currentTimeMillis() - startCycleTime;
				_totalexecutingtime += cycleExecutionTime;
				//System.out.println("Cycle execution (in Millis): " + cycleExecutionTime);
				//System.out.println("Average time per cycle (in Millis): " + _totalexecutingtime / _numberOfCycles);
				Thread.sleep(10);
			}
			catch (Exception ex) {
				//_shutdown = true;
				ex.printStackTrace();
				//System.out.println(ex);
			}
		}
	}
	
	@Override
	public EmotionalState simulateEmotionalState(Event simulatedEvent,IComponent callingComponent) {
		AppraisalFrame appraisalFrame = new AppraisalFrame(simulatedEvent);
		EmotionalState simulatedEmotionalState = null;
		
		for(IAppraisalDerivationComponent c : this._appraisalComponents)
		{
			if(c != callingComponent){ //to prevent infinite loops
				c.appraisal(this,simulatedEvent, appraisalFrame);
			}
		}
		if(appraisalFrame.hasChanged())
		{
			simulatedEmotionalState = this._emotionalState.clone(); 
			simulatedEmotionalState.Clear(); //only want the emotions caused by the simulatedEvent
			
			for(IAffectDerivationComponent ac : this._affectDerivationComponents)
			{
				if(ac != callingComponent){ //again to prevent infinite loops
					ArrayList<BaseEmotion> emotions = ac.affectDerivation(this, appraisalFrame);
					for(BaseEmotion em : emotions)
					{
						simulatedEmotionalState.AddEmotion(em, this);
					}		
				}
			}
			
		}
	
		return simulatedEmotionalState;
	}

	
	
	private void updateSimulationTimer() {
		// //updates the agent's simulation timer
		AgentSimulationTime.GetInstance().Tick();
		_numberOfCycles++;	
	}


	public void RequestAgentSave()
	{
		this._saveRequest = true;
	}

	public void RequestMemorySave()
	{
		this._saveMemoryRequest = true;
	}

	private void SaveAgentMemory() throws ParserConfigurationException, SAXException, IOException
	{
		_memory.getEpisodicMemory().MoveSTEMtoAM();
		_memoryWriter.outputMemorytoXML(_saveDirectory + MEMORY_FILENAME);
		for(IProcessExternalRequestComponent ip: _processExternalRequestComponents)
		{
			ip.processExternalRequest(this,"SAVE_ADV_MEMORY","");
		}
		
	}
	
	private void loadAgentMemory(String memoryFile) throws	ParserConfigurationException, SAXException, IOException
	{
		AgentLogger.GetInstance().log("LOADING Memory: " + memoryFile);
		MemoryLoaderHandler ml = new MemoryLoaderHandler(_memory);
	
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser parser = factory.newSAXParser();
		parser.parse(new File(memoryFile), ml);
		
		// creating a new episode
		_memory.getEpisodicMemory().StartEpisode(_memory);
		
		// Advanced Memory is loaded when Advanced Memory Component is initialised
	}
		
	private void SaveAgentState(String agentName)
	{
		String fileName = _saveDirectory + agentName;

		// Moving all the STEM into AM before saving because when it loads the next time
		// a new episode will be started. This is to prevent the rest of the STEM events
		// being stored in the wrong episode
		// Meiyii 13/09/10
		_memory.getEpisodicMemory().MoveSTEMtoAM();

		AgentSimulationTime.SaveState(fileName+"-Timer.dat");
		_remoteAgent.SaveState(fileName+"-RemoteAgent.dat");

		try
		{
			FileOutputStream out = new FileOutputStream(fileName);
			ObjectOutputStream s = new ObjectOutputStream(out);
			
			s.writeObject(_emotionalState);
			s.writeObject(_memory);
			s.writeObject(_goalLibrary);
			s.writeObject(_actionLibrary);
			//s.writeObject(_dialogManager);
			s.writeObject(_actionsVisibility);
			
			s.writeObject(_role);
			s.writeObject(_name);
			s.writeObject(_sex);
			s.writeObject(_speechAct);
			s.writeObject(_currentEmotion);
			s.writeObject(_displayName);
			s.writeObject(new Boolean(_showStateWindow));
			s.writeObject(_actionsForExecution);
			s.writeObject(_perceivedActions);
			s.writeObject(_perceivedActionFailures);
			s.writeObject(_perceivedLookAts);
			s.writeObject(_perceivedEntities);
			s.writeObject(_perceivedEntitiesRemoved);
			s.writeObject(_perceivedProperties);
			s.writeObject(_perceivedPropertiesRemoved);			
			s.writeObject(_saveDirectory);
			
			// prevent saving of the whole AgentCore which contains _agentDisplay as this would 
			// lead to NonSerializableException
			if (_strat != this)
			{
				s.writeObject(_strat);
			}
			else
			{
				s.writeObject(new String("SELF"));
			}
			s.writeObject(_generalComponents);
			s.writeObject(_processEmotionComponents);
			s.writeObject(_behaviourComponents);
			s.writeObject(_modelOfOtherComponents);
			s.writeObject(_processExternalRequestComponents);
			s.writeObject(_processPerceptionsComponents);
			s.writeObject(_affectDerivationComponents);
			s.writeObject(_appraisalComponents);
			
			s.flush();
			s.close();
			out.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	public void LoadAgentState(String fileName) throws IOException, ClassNotFoundException {
		
		FileInputStream in = new FileInputStream(fileName);
		ObjectInputStream s = new ObjectInputStream(in);
	
		this._emotionalState = (EmotionalState) s.readObject();
		this._memory = (Memory) s.readObject();
		// we need to recreate the memory writer because it holds a reference to the memory object
		this._memoryWriter = new MemoryWriter(this._memory);
		// we also want to delete all non-persistent facts from the KB
		this._memory.getSemanticMemory().removeNonPersistent();		
		this._goalLibrary = (GoalLibrary) s.readObject();
		this._actionLibrary = (ActionLibrary) s.readObject();
		this._actionsVisibility = (HashMap<String,String>) s.readObject();
		//this._dialogManager = (DialogManager) s.readObject();
		
		this._role = (String) s.readObject();
		this._name = (String) s.readObject();
		this._sex = (String) s.readObject();
		this._speechAct = (SpeechAct) s.readObject();
		this._currentEmotion = ((String) s.readObject());
		this._displayName = (String) s.readObject();
		this._showStateWindow = ((Boolean) s.readObject()).booleanValue();
		this._actionsForExecution = (ArrayList<ValuedAction>) s.readObject();
		this._perceivedActions = (ArrayList<Event>) s.readObject();
		this._perceivedActionFailures = (ArrayList<Event>) s.readObject();
		this._perceivedLookAts = (ArrayList<LookAtPerception>) s.readObject();
		this._perceivedEntities = (ArrayList<EntitiesPerception>) s.readObject();
		this._perceivedEntitiesRemoved = (ArrayList<EntityRemovedPerception>) s.readObject();
		this._perceivedProperties = (ArrayList<PropertyPerception>) s.readObject();
		this._perceivedPropertiesRemoved = (ArrayList<PropertyRemovedPerception>) s.readObject();
			
		this._saveDirectory = (String) s.readObject();
		
		Object stratObject = s.readObject();
		if (stratObject instanceof IGetModelStrategy)
		{
			this._strat = (IGetModelStrategy) stratObject;
		}
		else 
		{	
			if (stratObject instanceof String)
			{
				String stratObjectStr = (String) stratObject;
				if (stratObjectStr.equals("SELF")) this._strat = this;
			}
		}
		 
		this._generalComponents = (HashMap<String,IComponent>) s.readObject();
		this._processEmotionComponents = (ArrayList<IProcessEmotionComponent>) s.readObject();
		this._behaviourComponents = (ArrayList<IBehaviourComponent>) s.readObject();
		this._modelOfOtherComponents = (ArrayList<IModelOfOtherComponent>) s.readObject();
		this._processExternalRequestComponents = (ArrayList<IProcessExternalRequestComponent>) s.readObject();
		this._processPerceptionsComponents = (ArrayList<IAdvancedPerceptionsComponent>) s.readObject();
		this._affectDerivationComponents = (ArrayList<IAffectDerivationComponent>) s.readObject();
		this._appraisalComponents = (ArrayList<IAppraisalDerivationComponent>) s.readObject();
		
		s.close();
		in.close();

		AgentSimulationTime.LoadState(fileName+"-Timer.dat");

		//_remoteAgent.LoadState(fileName+"-RemoteAgent.dat");
	}
	
	
	/**
	 * Gets the gender of the agent
	 * @return the agent's sex
	 */
	public String sex() 
	{
		return _sex;
	}


	/**
	 * Starting the agent
	 * added by Meiyii 19/11/09
	 */
	public void StartAgent()
	{
		try{
			this.Run();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		this.terminateExecution();
	}

	private void terminateExecution(){
		_remoteAgent.ShutDown();
		if(_showStateWindow && _agentDisplay != null) _agentDisplay.dispose();
	}

	/** returns a String that contains the serialized internal state of the agent
	 *  in a String format*/
	public String getSerializedState() 
	{
		try
		{

			ByteArrayOutputStream b = new ByteArrayOutputStream();		
			ObjectOutputStream s = new ObjectOutputStream(b);

			//s.writeObject(_ToM);
			//s.writeObject(_nearbyAgents);
			s.writeObject(_emotionalState);
			s.writeObject(_memory);
			s.writeObject(_goalLibrary);
			s.writeObject(_actionLibrary);
			s.writeObject(_actionsVisibility);
			//s.writeObject(_motivationalState);
			//s.writeObject(_dialogManager);
			//s.writeObject(_role);
			//s.writeObject(_name);
			//s.writeObject(_sex);
			s.writeObject(_speechAct);
			s.writeObject(_currentEmotion);
			s.writeObject(_displayName);
			//s.writeObject(new Boolean(_showStateWindow));
			s.writeObject(_actionsForExecution);
			s.writeObject(_perceivedActions);
			s.writeObject(_perceivedActionFailures);
			s.writeObject(_perceivedLookAts);
			s.writeObject(_perceivedEntities);
			s.writeObject(_perceivedEntitiesRemoved);
			s.writeObject(_perceivedProperties);
			s.writeObject(_perceivedPropertiesRemoved);		
			
			// prevent saving of the whole AgentCore which contains _agentDisplay as this would 
			// lead to NonSerializableException
			if (_strat != this)
			{
				s.writeObject(_strat);
			}
			else
			{
				s.writeObject(new String("SELF"));
			}	
			
			s.writeObject(_generalComponents);
			s.writeObject(_processEmotionComponents);
			s.writeObject(_behaviourComponents);
			s.writeObject(_modelOfOtherComponents);
			s.writeObject(_processExternalRequestComponents);
			s.writeObject(_processPerceptionsComponents);
			s.writeObject(_affectDerivationComponents);
			s.writeObject(_appraisalComponents);
			
			//s.writeObject(_saveDirectory);
			AgentSimulationTime.SaveState(s);
			s.flush();
			s.close();
			return BinaryStringConverter.encodeBinaryToString(b.toByteArray());
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

	/** sets the internal state of the agent to the state which is contained
	 *  in the passed string */
	@SuppressWarnings("unchecked")
	public void setSerializedState(String state) 
	{
		try
		{
			ByteArrayInputStream in = new ByteArrayInputStream(
					BinaryStringConverter.decodeStringToBinary(state));

			ObjectInputStream s = new ObjectInputStream(in);
			this._emotionalState = (EmotionalState) s.readObject();
			this._memory = (Memory) s.readObject();
			// we need to recreate the memory writer because it holds a reference to the memory object
			this._memoryWriter = new MemoryWriter(this._memory);
			// we also want to delete all non-persistent facts from the KB
			this._memory.getSemanticMemory().removeNonPersistent();
			this._goalLibrary = (GoalLibrary) s.readObject();
			this._actionLibrary = (ActionLibrary) s.readObject();
			this._actionsVisibility = (HashMap<String,String>) s.readObject();
			//this._dialogManager = (DialogManager) s.readObject();
			//this._role = (String) s.readObject();
			//this._name = (String) s.readObject();
			//this._sex = (String) s.readObject();
			this._speechAct = (SpeechAct) s.readObject();
			this._currentEmotion = (String) s.readObject();
			this._displayName = (String) s.readObject();
			//this._showStateWindow = ((Boolean) s.readObject()).booleanValue();
			this._actionsForExecution = (ArrayList<ValuedAction>) s.readObject();
			this._perceivedActions = (ArrayList<Event>) s.readObject();
			this._perceivedActionFailures = (ArrayList<Event>) s.readObject();
			this._perceivedLookAts = (ArrayList<LookAtPerception>) s.readObject();
			this._perceivedEntities = (ArrayList<EntitiesPerception>) s.readObject();
			this._perceivedEntitiesRemoved = (ArrayList<EntityRemovedPerception>) s.readObject();
			this._perceivedProperties = (ArrayList<PropertyPerception>) s.readObject();
			this._perceivedPropertiesRemoved = (ArrayList<PropertyRemovedPerception>) s.readObject();
			
			Object stratObject = s.readObject();
			if (stratObject instanceof IGetModelStrategy)
			{
				this._strat = (IGetModelStrategy) stratObject;
			}
			else 
			{	
				if (stratObject instanceof String)
				{
					String stratObjectStr = (String) stratObject;
					if (stratObjectStr.equals("SELF")) this._strat = this;
				}
			}

			this._generalComponents = (HashMap<String,IComponent>) s.readObject();
			this._processEmotionComponents = (ArrayList<IProcessEmotionComponent>) s.readObject();
			this._behaviourComponents = (ArrayList<IBehaviourComponent>) s.readObject();
			this._modelOfOtherComponents = (ArrayList<IModelOfOtherComponent>) s.readObject();
			this._processExternalRequestComponents = (ArrayList<IProcessExternalRequestComponent>) s.readObject();
			this._processPerceptionsComponents = (ArrayList<IAdvancedPerceptionsComponent>) s.readObject();
			this._affectDerivationComponents = (ArrayList<IAffectDerivationComponent>) s.readObject();
			this._appraisalComponents = (ArrayList<IAppraisalDerivationComponent>) s.readObject();
			
			AgentSimulationTime.LoadState(s);
			s.close();
			
			resetDisplay();
			
			//this._saveDirectory = (String) s.readObject();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	// reset the display after having overwritten the agents internal state 
	// (setSerializedState method)
	private void resetDisplay()
	{
		if (_agentDisplay!=null)
		{
			// first remove all panels that are linked to our old components
			_agentDisplay.clearAllComponentTabs();
			
			// now let all the new components create new panels			
			for (IComponent c : _generalComponents.values())
			{
				AgentDisplayPanel panel = c.createDisplayPanel(this);
				if(panel != null & _showStateWindow)
				{
					this._agentDisplay.AddPanel(panel, c.name(),"");
				}
			}
		}
	}
	
	public void updateEmotions(AppraisalFrame af) {
		
		ArrayList<BaseEmotion> emotions;
		ActiveEmotion activeEmotion;
		
			
		if(af.hasChanged())
		{
			for(IAffectDerivationComponent ac : this._affectDerivationComponents)
			{	
				emotions = ac.affectDerivation(this, af);
				for(BaseEmotion em : emotions)
				{
					activeEmotion = _emotionalState.AddEmotion(em, this);
					if(activeEmotion != null)
					{
						for(IProcessEmotionComponent pec : this._processEmotionComponents)
						{
							pec.emotionActivation(this,activeEmotion);
						}
					}
				}
			}
		}	
	}
	
	public AgentModel getModelToTest(Symbol ToM, Condition cond)
	{
		if(ToM.equals(Constants.UNIVERSAL_SYMBOL) || ToM.equals(Constants.SELF_SYMBOL) || ToM.toString().equals(_name))
		{
			return this;
		}
		else return _strat.getModel(ToM);
	}
	
	public AgentModel getModel(Symbol ToM)
	{
		return this;
	}
	
	/** methods added by Henrique reis to implement ToM Lvl2+ **/
	
	@Override
	public AgentModel getModelToTestCondition(Condition cond) {
		
		AgentModel am = this;
		AgentModel nextLvl;
		Symbol ToM;
		
		for(Symbol s : cond.getToM())
		{
			ToM = (Symbol) s.clone();
			ToM.removePerspective(_name);
			
			nextLvl = am.getModelToTest(ToM, cond);
			
			if(nextLvl == null) return null;
			
			am = nextLvl;
		}
		
		return am;		
	}
	
	@Override
	public ArrayList<String> predecessorMinds() {
		return new ArrayList<String>();
	}


	@Override
	public void setActionVisibility(String actionName, String visibility) {
		String value = _actionsVisibility.get(actionName);
		if(value == null) 
		{
			_actionsVisibility.put(actionName, visibility);
			return;
		}
		
		if(!value.equals(Constants.SELF_STRING))
			_actionsVisibility.put(actionName, visibility);
	}


	@Override
	public HashMap<String, String> getActionsVisibility() {
		return _actionsVisibility;
	}
}