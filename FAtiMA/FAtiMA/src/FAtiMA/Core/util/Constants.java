package FAtiMA.Core.util;

import FAtiMA.Core.wellFormedNames.*;

public abstract class Constants {

	public static final String SELF_STRING = "SELF";
	public static final String EMPTY_LOCATION = "";
	public static final String UNIVERSAL_STRING = "*";
	public static final String AGENT_STRING = "[AGENT]";
	public static final String ToM_STRING = "ToM";
	
	public static final Symbol SELF_SYMBOL = new Symbol(SELF_STRING);
	public static final Symbol AGENT_SYMBOL = new Symbol(AGENT_STRING);
	public static final Symbol UNIVERSAL_SYMBOL = new Symbol(UNIVERSAL_STRING);
}
