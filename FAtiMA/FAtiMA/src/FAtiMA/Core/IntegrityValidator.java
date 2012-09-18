/** 
 * IntegrityValidator.java - Class used to validate the integrity of information about the 
 * agent parsed at the begining. For example, among other things, this class verifies 
 * if the personality file does not reference an unspecified goal (it's not defined 
 * in the GoalLibrary)
 *  
 * Copyright (C) 2006 GAIPS/INESC-ID 
 *  
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 * 
 * Company: GAIPS/INESC-ID
 * Project: FAtiMA
 * Created: 15/01/2005 
 * @author: João Dias
 * Email to: joao.assis@tagus.ist.utl.pt
 * 
 * History: 
 * João Dias: 15/01/2005 - File created
 * João Dias: 25/05/2006 - FindSpeechAct method made private, it doesn't make sense to
 * 						   be public
 * João Dias: 25/05/2006 - Added comments to each public method's header
 * João Dias: 31/08/2006 - Solved bug that occurred when comparing predicates in 
 * 						   FindUnreachableConditions method
 * João Dias: 03/10/2006 - Small change in the test that was verifying if a given step
 * 						   corresponded to a SpeechAct. Instead of comparing the step's
 * 						   name with a prefined String ("SpeechAct") we now use the 
 * 						   isSpeechAct(String) method from the class SpeechAct
 * Joao Dias: 08/10/2006 - small change in the test that was verifying if a given step
 * 						   corresponded to a SpeechAct. If the SpeechAct's type equals
 * 						   "*", the test will also return true.
 */

package FAtiMA.Core;

import java.util.ArrayList;

import FAtiMA.Core.conditions.Condition;
import FAtiMA.Core.goals.Goal;
import FAtiMA.Core.plans.Effect;
import FAtiMA.Core.plans.Plan;
import FAtiMA.Core.plans.Step;
import FAtiMA.Core.util.AgentLogger;
import FAtiMA.Core.wellFormedNames.Substitution;
import FAtiMA.Core.wellFormedNames.Symbol;

/**
 * Class used to validate the integrity of information about the 
 * agent parsed at the beginning. For example, among other things, this class verifies 
 * if the personality file does not reference an unspecified goal (it's not defined 
 * in the GoalLibrary)
 * 
 * @author João Dias
 */
public class IntegrityValidator {
    
    ArrayList<Step> _operators;
    
    /**
     * Creates a new IntegrityValidator
     * 
     * @param operators - the list of domain actions that will be used by the planner,
     * 					  necessary to test if a Goal is achievable by any of the actions
     */
    public IntegrityValidator(ArrayList<Step> operators) {
        _operators = operators;
    }

    /**
     * Checks if the received conditions are unreachable. This means that one
     * of the conditions cannot be achieved by any operator that the planner 
     * can use. Thus, such condition is impossible to make true. In this situation,
     * a warning is given to the output, since this is likely a typo error when
     * specifying the conditions (or one of the steps).
     * 
     * @param objectName - the name of the object (usually goal or action) that specifies
     * 				       the conditions tested. This name is required for the warning
     * 					   if an unreachable condition is found  
     * @param conditions - the list of conditions to test if they are unreachable
     * @return true if any of the conditions received is unreachable, false otherwise
     */
    public boolean findUnreachableConditions(AgentModel am, String objectName, ArrayList<Condition> conditions)
	{
    	boolean foundUnreachable = false;
	    boolean ok;
	    Condition effCondition;
	    ArrayList<Substitution> substs;
	    
	    for(Condition cond : conditions)
	    {
	    	if(cond.getValidSubstitutions(am)==null)
	    	{
	    		ok = false;
		    	for(Step s : this._operators)
		    	{
		    		for(Effect e : s.getEffects())
		    		{
		    			effCondition = e.GetEffect();
		    			substs = new ArrayList<Substitution>();
		    			if(Plan.getPlannerUnifierStrategy().unifyConditions(effCondition, cond, substs))
		    			{
		    				ok = true;
		    				break;
		    			}
		    		}
		    		if(ok) break;
		    	}
		    	
		    	if(!ok)
		    	{
		    		AgentLogger.GetInstance().logAndPrint("WARNING: condition " + cond + " of the goal/action " + objectName + " cannot be achived by any step!");
		            foundUnreachable = true;
		    	}
	    	}
	    }
	    
	    return foundUnreachable;
	}
    
    public ArrayList<Step> getOperators()
    {
    	return _operators;
    }
    
    public Step getOperator(String name)
    {
    	for(Step s : _operators)
    	{
    		if(s.getName().toString().equals(name))
    		{
    			return s;
    		}
    	}
    	return null;
    }
    
    public static ArrayList<Symbol> getListOfVariables(ArrayList<Condition> conditions)
    {
    	 ArrayList<Symbol> unboundVariables = new ArrayList<Symbol>();
 	    
 	    for(Condition cond : conditions)
 	    {
 	    	unboundVariables.addAll(cond.getName().getVariableList());
 	    	if(cond.getValue() != null)
 	    	{
 	    		unboundVariables.addAll(cond.getValue().getVariableList());
 	    	}
 	    }
 	    
 	    return unboundVariables;
    }
}
