package FAtiMA;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import FAtiMA.Core.AgentCore;
import FAtiMA.Core.exceptions.AgentParsingException;
import FAtiMA.Core.exceptions.GoalLibParsingException;
import FAtiMA.Core.exceptions.UnknownGoalException;
import FAtiMA.Core.util.ConfigurationManager;
import FAtiMA.DeliberativeComponent.DeliberativeComponent;
import FAtiMA.OCCAffectDerivation.OCCAffectDerivationComponent;
import FAtiMA.ReactiveComponent.ReactiveComponent;
import FAtiMA.SocialImportance.SocialImportanceComponent;
import FAtiMA.ToM.ToMComponent;
import FAtiMA.motivationalSystem.MotivationalComponent;

public class AgentLauncher {
	
	 /**
     * The main method
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 * @throws GoalLibParsingException 
	 * @throws ActionsParsingException 
	 * @throws UnknownGoalException 
     */
	static public void main(String args[]) throws ParserConfigurationException, SAXException, IOException, UnknownGoalException, AgentParsingException, GoalLibParsingException  {
			
		
		AgentCore aG = initializeAgentCore(args);
		ArrayList<String> extraFiles = new ArrayList<String>();
		String cultureFile = ConfigurationManager.getMindPath() + ConfigurationManager.getOptionalConfigurationValue("cultureName") + ".xml";
		String locationOntologyFile = ConfigurationManager.getMindPath() + "locations/AmyHouse.xml";
		
		if (!aG.getAgentLoad())
		{
			//extraFiles.add(cultureFile); 

			
			aG.addComponent(new ReactiveComponent());
			aG.addComponent(new OCCAffectDerivationComponent());

			aG.addComponent(new DeliberativeComponent());

			//aG.addComponent(new SocialImportanceComponent(extraFiles));
			//aG.addComponent(new ToMComponent(ConfigurationManager.getName()));
		
			//aG.addComponent(new NormativeComponent("./data/SocialImportance/NormativeLibrary.xml","./data/SocialImportance/RoleLibrary.xml"));
			
			aG.addComponent(new MotivationalComponent(extraFiles));
			
			//aG.addComponent(new MotivationalComponent(extraFiles));
			//aG.addComponent(new EmpathyComponent());
			//aG.addComponent(new ToMComponent(ConfigurationManager.getName()));
			//aG.addComponent(new CulturalDimensionsComponent(cultureFile));
			//aG.addComponent(new AdvancedMemoryComponent());
		}
		aG.StartAgent();
	}
	
	
	static private AgentCore initializeAgentCore(String args[]) throws ParserConfigurationException, SAXException, IOException, UnknownGoalException, AgentParsingException, GoalLibParsingException{
		if(args.length < 4){
			System.err.println("ERROR - expecting 4 arguments at least: Scenario Directory, Scenario File, Scenario Name, and Agent Name");
			System.exit(1);
		}
		
		String scenarioDirectory = args[0];
		String scenarioFile = args[1];
		String scenarioName = args[2];
		String agentName = args[3];	
		
		FAtiMA.Core.AgentCore agent = new AgentCore(agentName);
		agent.initialize(scenarioDirectory, scenarioFile,scenarioName,agentName);
		
		return agent;
	}
}

