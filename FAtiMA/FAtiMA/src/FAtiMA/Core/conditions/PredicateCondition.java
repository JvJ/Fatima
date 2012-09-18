/** 
 * PredicateCondition.java - Class that Represents a predicate condition, used to represent preconditions,
 * success conditions, action effects, etc.
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
 * Created: 16/01/2004 
 * @author: João Dias
 * Email to: joao.assis@tagus.ist.utl.pt
 * 
 * History: 
 * João Dias: 16/01/2004 - File created
 * João Dias: 17/05/2004 - Added clone() method
 * João Dias: 22/05/2006 - Added comments to each public method's header
 * João Dias: 10/07/2006 - the class is now serializable 
 * João Dias: 12/07/2006 - Removed the Reference to a KB stored in conditions. It didn't 
 * 						   make much sense and it causes lots of problems with serialization
 * 						   Because of this, there are additional changes in some of the methods
 * 						   that need to receive a reference to the KB
 * João Dias: 12/07/2006 - Changes in groundable methods, the class now implements
 * 						   the IGroundable Interface, the old ground methods are
 * 					       deprecated
 * João Dias: 31/08/2006 - Class renamed from Predicate to PredicateCondition (Refactor)
 * 						 - the field _positive is now protected instead of private (it can be inherited)
 * 						 - Added an empty constructor
 * 						 - Important conceptual change: Since we have now two types of memory,
 * 						   the KnowledgeBase (Semantic memory) and Autobiographical memory (episodic memory),
 * 						   and we have RecentEvent and PastEvent conditions that are searched in episodic
 * 						   memory (and the old conditions that are searched in the KB), it does not make 
 * 						   sense anymore to receive a reference to the KB in searching methods 
 * 						   (checkCondition, getValidBindings, etc) for Conditions. Since both the KB 
 * 						   and AutobiographicalMemory are singletons that can be accessed from any part of 
 * 						   the code, these methods do not need to receive any argument. It's up to each type
 * 						   of condition to decide which memory to use when searching for information.
 */

package FAtiMA.Core.conditions;

import java.util.ArrayList;

import org.xml.sax.Attributes;

import FAtiMA.Core.AgentModel;
import FAtiMA.Core.memory.semanticMemory.KnowledgeBase;
import FAtiMA.Core.util.Constants;
import FAtiMA.Core.wellFormedNames.Name;
import FAtiMA.Core.wellFormedNames.SubstitutionSet;
import FAtiMA.Core.wellFormedNames.Symbol;


/**
 * Represents a test to a predicate. Used to represent preconditions, success conditions, etc
 * 
 * @author João Dias
 */

public class PredicateCondition extends Condition {

	private static final long serialVersionUID = 1L;
	private boolean _positive;
	
	/**
	 * Parses a Predicate given a XML attribute list
	 * @param attributes - A list of XMl attributes
	 * @return - the Predicate Parsed
	 */
    public static PredicateCondition ParsePredicate(Attributes attributes) {
		String aux;
		String ToM;
		
		PredicateCondition cond;
	
		Name name;
		boolean positive = true;
		aux = attributes.getValue("name");
		if(aux.charAt(0) == '!') {
			aux = aux.substring(1);
			positive = false;
		}
		name = Name.ParseName(aux);
		
		aux = attributes.getValue(Constants.ToM_STRING);
		if(aux == null) 
		{
			ToM = Constants.UNIVERSAL_SYMBOL.toString();
		}
		else
		{
			ToM = aux;
		}
		
		cond = new PredicateCondition(positive,name,ToM);
		
		aux = attributes.getValue("static");
		if(aux != null)
		{
			if(aux.equalsIgnoreCase("true"))
			{
				cond.setStatic(true);
			}
		}
		
		return new PredicateCondition(positive,name,ToM);
	}
	
	protected PredicateCondition()
	{
	}
	
	/**
	 * Creates a new Test to a Predicate
	 * @param positive - Indicates if the Predicate is positive or negative
	 * @param name - the predicate's name
	 */
	
	public PredicateCondition(boolean positive, Name name) {
		super(name);
		_positive = positive;
	}
		
	public PredicateCondition(boolean positive, Name name, String ToM) {
		super(name,ToM);
		_positive = positive;
	}
	
	
	protected PredicateCondition(PredicateCondition pC){
		super(pC);
		_positive = pC._positive;
	}

	/**
	 * Checks if the Predicate is verified in the agent's KnowledgeBase
	 * @return true if the Predicate is verified, false otherwise
	 * @see KnowledgeBase
	 */
	public float checkConditionUsingPerspective(AgentModel perspective) {
		boolean result;
		
		if(!getName().isGrounded()) return 0;
		
		
		result = perspective.getMemory().getSemanticMemory().AskPredicate(getName());
		if(_positive == result)
		{
			return 1;
		}
		else return 0;
	}
	
	/**
	 * Clones this Predicate, returning an equal copy.
	 * If this clone is changed afterwards, the original object remains the same.
	 * @return The Predicates's copy.
	 */
	public Object clone()
	{
	    return new PredicateCondition(this);
	}
	
	
	protected boolean getPositive()
	{
		return _positive;
	}
			
	
	
	
	protected ArrayList<SubstitutionSet> getValidSubstitutionsUsingPerspective(AgentModel perspective)
	{
		ArrayList<SubstitutionSet> validSubstitutionSets = new ArrayList<SubstitutionSet>();
		ArrayList<SubstitutionSet> bindingSets;
		
		Condition cond;

		if (getName().isGrounded()) {
			//if the name is grounded, we just need to check the condition's value to see if it matches
			//with the name
			if(this.checkConditionUsingPerspective(perspective)==1)
			{
				validSubstitutionSets.add(new SubstitutionSet());
				return validSubstitutionSets;
			}
			else
			{
				return null;
			}
		}
		else
		{
			//if the name is not grounded, we must first search memory for substitutions that make
			//the name match with existing knowledge in semantic memory
			bindingSets = perspective.getMemory().getSemanticMemory().GetPossibleBindings(getName());
			if (bindingSets == null)
				return null;
			
			for(SubstitutionSet ss: bindingSets)
			{
				cond = (Condition) this.clone();
				cond.makeGround(ss.GetSubstitutions());
				if(cond.checkConditionUsingPerspective(perspective)==1)
				{
					validSubstitutionSets.add(ss);
				}
			}
			if(validSubstitutionSets.size() == 0) return null;
			
			return validSubstitutionSets;	
		}
	}
	
	/**
	 * Gets the predicates's value - the object compared against the condition's name
	 * @return the predicates's value
	 */
	public Name getValue() {
		if(_positive) return new Symbol("True");
		else return new Symbol("False");
	}
	
	
	/**
	 * Indicates if the Predicate is positive or negative.
     * A negative predicate corresponds to the negation of the original predicate
	 * @return True if the Predicate is positive, false otherwise.
	 */
	public boolean isPositive() {
		return _positive;
	}
	
	protected void setPositive(boolean positive)
	{
		_positive = positive;
	}
	
	
	/**
	 * Converts the Predicate to a String
	 * @return the converted String
	 */
	public String toString() {
		if (_positive) return getToM() + ":" + getName();
		else return   getToM() + ":!" + getName();
	}
}
