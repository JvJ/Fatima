package FAtiMA;


import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import FAtiMA.Core.exceptions.AgentParsingException;
import FAtiMA.Core.exceptions.GoalLibParsingException;
import FAtiMA.Core.exceptions.UnknownGoalException;

//This class only exists for retrocompatibility issues:

public class Agent {
	public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException, UnknownGoalException,  GoalLibParsingException, AgentParsingException{
		AgentLauncher.main(args);
	}
}
