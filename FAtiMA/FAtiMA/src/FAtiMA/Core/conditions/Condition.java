/** 
 * Condition.java - Abstract condition, used to represent preconditions, success conditions,
 * action effects, etc. Conditions can be either Predicate Conditions or Property Conditions
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
 * @author: Jo�o Dias
 * Email to: joao.assis@tagus.ist.utl.pt
 * 
 * History: 
 * Jo�o Dias: 16/01/2004 - File created
 * Jo�o Dias: 17/05/2006 - Created clone() Method
 * Jo�o Dias: 22/05/2006 - Added comments to each public method's header
 * Jo�o Dias: 10/07/2006 - the class is now serializable
 * Jo�o Dias: 12/07/2006 - Removed the Reference to a KB stored in conditions. It didn't 
 * 						   make much sense and it causes lots of problems with serialization
 * 						   Because of this, there are additional changes in some of the methods
 * 						   that need to receive a reference to the KB
 * Jo�o Dias: 12/07/2006 - Changes in groundable methods, the class now implements
 * 						   the IGroundable Interface, the old ground methods are
 * 					       deprecated
 * Jo�o Dias: 31/08/2006 - Added empty constructor
 * 						 - Important conceptual change: Since we have now two types of memory,
 * 						   the KnowledgeBase (Semantic memory) and Autobiographical memory (episodic memory),
 * 						   and we have RecentEvent and PastEvent conditions that are searched in episodic
 * 						   memory (and the old conditions that are searched in the KB), it does not make 
 * 						   sense anymore to receive a reference to the KB in searching methods 
 * 						   (checkCondition, getValidBindings, etc) for Conditions. Since both the KB 
 * 						   and AutobiographicalMemory are singletons that can be accessed from any part of 
 * 						   the code, these methods do not need to receive any argument. It's up to each type
 * 						   of condition to decide which memory to use when searching for information.
 * Jo�o Dias: 12/09/2006 - The method GetValidBindings returns null when in some cases it would return 
 * 						   an empty list
 * Jo�o Dias: 03/10/2006 - Important refactorization: the method CheckActivation() was moved from the
 * 						   the class ActivePursuitGoal to this class, since it is indeed a very general
 * 						   method that can be reused to test a list of preconditions. The only thing that
 * 						   the old method was using from an ActivePursuitGoal was the precondition list
 * 						   being tested. Therefore, this method has become a static method that receives
 * 						   as a parameter the list of preconditions to test. 
 * 							
 */

package FAtiMA.Core.conditions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.StringTokenizer;

import FAtiMA.Core.AgentModel;
import FAtiMA.Core.memory.semanticMemory.KnowledgeBase;
import FAtiMA.Core.util.Constants;
import FAtiMA.Core.wellFormedNames.IGroundable;
import FAtiMA.Core.wellFormedNames.Name;
import FAtiMA.Core.wellFormedNames.Substitution;
import FAtiMA.Core.wellFormedNames.SubstitutionSet;
import FAtiMA.Core.wellFormedNames.Symbol;


/** 
 * Abstract condition test, used to represent preconditions, success conditions, etc.
 * Conditions can be either Predicate conditions or Property conditions.
 * Provides methods to search the value of such conditions in the KnowledgeBase.
 * 
 * @see PredicateCondition
 * @see PropertyCondition
 * @see KnowledgeBase
 * 
 * @author Jo�o Dias
 */

public abstract class Condition implements IGroundable, Cloneable, Serializable {
	
	private static final long serialVersionUID = 1L;

	private boolean _previousVerifiableStatus;
	private boolean _currentVerifiableStatus;
	private boolean _hasChangedVerifiability;
	
	//attribute that defines is a condition (PropertyCondition) is static or dynamic
	protected boolean _static;
	
	private Name _name;
	private ArrayList<Symbol> _ToM;
	

	/**
	 * Checks if a list of conditions (usually preconditions) to see
	 * if all of them are verified. The method returns a list of possible 
	 * SubstitutionSets. Each of the returned SustitutionSet applied to the entire set of conditions
	 * will make them all true.
	 * 
	 * As example, if we have the generic goal of bullying someone Bully([Victim]),
	 * and we have a precondition list consisting of seeing someone weaker than us, 
	 * So if we see John (a weak victim) and Frances (another weak victim) we can activate
	 * the preconditions and subsequently the specific goal Bully(John) and Bully(Frances),
	 * by applying the substitutionset {[Victim]/John} and the substitutionset {[Victim]/Frances}
	 * @param preconditions - the list of preconditions that we want to test for activation 
	 * @return a list of SubstitutionSets that if applied to all the preconditions will make them true,
	 * returns null if no such SubstitutionSet exists
	 * @see SubstitutionSet
	 */
	public static ArrayList<SubstitutionSet> checkActivation(AgentModel am, ArrayList<Condition> preconditions) {
		ListIterator<SubstitutionSet> subsi;
		Condition newCond;
		SubstitutionSet subSet;
		ArrayList<SubstitutionSet> aux;
		ArrayList<SubstitutionSet> validSubstitutionsSet = new ArrayList<SubstitutionSet>();
		ArrayList<SubstitutionSet> newValidSubstitutionsSet;
		
		if(preconditions.size() == 0) 
		{
			validSubstitutionsSet.add(new SubstitutionSet());
			return validSubstitutionsSet;
		}
		
		for(Condition cond : preconditions)
		{
			boolean previousVerifiableStatus = cond._currentVerifiableStatus;
			cond._currentVerifiableStatus = false;
			
			//For each condition we need to verify if it is valid
			//This list contains the SubstitutionSets that correspond to Substitutions 
			//that if applied to the tested conditions, will make them true
			//this list is rebuilt from scratch at each condition, because a previous
			//valid Substitution may become invalid when testing another condition 
			newValidSubstitutionsSet = new ArrayList<SubstitutionSet>();
			
			//if there are substitutions that resulted from testing previous conditions
			//we need to apply them and test the new grounded condition. For example, consider 
			//<Property name="[target](strength)" operator="LesserThan" value="[SELF](strength)" />
			//and imagine there are two characters weaker than the SELF. Then the valid substitution,
			//will contain two substitution sets: {[target]/John} and {[target]/Ollie}. So, we need to
			//test the subsequent conditions replacing the [target] variable with John, and replacing
			//the [target] variable with Ollie. 
			subsi = validSubstitutionsSet.listIterator();
			if(subsi.hasNext()) {
				while(subsi.hasNext()) {
					//for each of the valid substitution sets, we apply the substitutions to the precondition
					//so, if the precondition was Like([SELF],[target]) < 0 and the substitutionSet is
					//{[target]/John}, the new condition that we need to test is Like([SELF],John)
					subSet = (SubstitutionSet) subsi.next();
					newCond = (Condition) cond.clone();
					newCond.makeGround(subSet.GetSubstitutions());
					
					//we test the new condition against memory and receive a list of new SubstitutionsSets
					//that individually can satisfy the new condition
					aux = newCond.getValidSubstitutions(am);
					if (aux != null) {
						cond._currentVerifiableStatus = true;
						if(previousVerifiableStatus == false){
							cond._hasChangedVerifiability = true;		
						}
						//if the list is not null, it means that the condition can be verified 
						//in this case we need to apply more substitutions to make the last condition
						//true, and there might exist more than one set of substitutions that does that.
						//So, for each valid set of substitutions we need to add the substitutions
						//that satisfy the previous conditions, and the result is a valid SubstitutionSet
					
						for(SubstitutionSet newSubSet : aux){
						 	//We're adding the substitutions needed for the previous conditions
						 	//to the SubstitutionSet that verifies the current condition 
						 	newSubSet.AddSubstitutions(subSet.GetSubstitutions());
						 	//this set that resulted from this addition is valid
						 	newValidSubstitutionsSet.add(newSubSet);
						}
					}
					
				}
				if(newValidSubstitutionsSet.size() == 0) 
				{
					return null;
				}
			}
			else {
				//in this case there are no previous substitutions that resulted from the
				//previous condition tests
				aux = cond.getValidSubstitutions(am);
				if (aux != null && aux.size() > 0) {
					cond._currentVerifiableStatus = true;
					if(previousVerifiableStatus == false){
						cond._hasChangedVerifiability = true;		
					}
					newValidSubstitutionsSet.addAll(aux);
				}
				else{
					return null;
				}
			}
			validSubstitutionsSet = newValidSubstitutionsSet;
			
			if(previousVerifiableStatus == true && cond._currentVerifiableStatus == false){
				cond._hasChangedVerifiability = true;
			}
		}
		
		
		if(validSubstitutionsSet.size() == 0) return null;	
		else return validSubstitutionsSet;
	}
	
	protected Condition(){
		_currentVerifiableStatus = false;
		_previousVerifiableStatus = false;
		_hasChangedVerifiability = false;
		_ToM = new ArrayList<Symbol>();
		_ToM.add(Constants.UNIVERSAL_SYMBOL);
		
		//by default all conditions are assumed non-static, i.e, dynamic
		_static = false;
	}
	
	/**
	 * Creates a new Condition - not used directly because its an abstract class
	 *
	 */
	protected Condition(Condition c)
	{
		_name = (Name) c._name.clone();
		_ToM = new ArrayList<Symbol>();
		for(Symbol s : c._ToM)
		{
			_ToM.add((Symbol)s.clone());
		} 
		
		_currentVerifiableStatus = c._currentVerifiableStatus;
		_previousVerifiableStatus = c._previousVerifiableStatus;
		_hasChangedVerifiability = c._hasChangedVerifiability;
		
		//by default all conditions are assumed non-static, i.e, dynamic
		_static = c._static;
	}
	
	/**
	 * Creates a new Condition - not used directly because its an abstract class
	 * @param name - the condition's name
	 */
	public Condition(Name name) {
		_name = name;
		_ToM = new ArrayList<Symbol>();
		_ToM.add(Constants.UNIVERSAL_SYMBOL);
		_currentVerifiableStatus = false;
		
		//by default all conditions are assumed non-static, i.e, dynamic
		_static = false;
	}
	
	public Condition(Name name, String ToM)
	{
		_name = name;
		setToM(ToM);
		_currentVerifiableStatus = false;
		
		//by default all conditions are assumed non-static, i.e, dynamic
		_static = false;
	}
	
	public void applyPerspective(String name)
	{
		this._name.applyPerspective(name);
		
		for(Symbol s : _ToM)
		{
			s.applyPerspective(name);
		}
	}
	
	/**
	 * Checks if the condition is verified in the agent's internal state (KB + AM + Emotional State, etc)
	 * @return true if the condition is verified, false otherwise
	 */
	public final float checkCondition(AgentModel am) 
	{
		Condition cond;
		AgentModel perspective = am.getModelToTestCondition(this);
		if(perspective == null) return 0;
		
		//if the perspective is different from the current agent model, we need to change perspectives
		if(!perspective.getName().equals(am.getName()))
		{
			cond = (Condition) this.clone();
			cond.removePerspective(am.getName());
			cond.applyPerspective(perspective.getName());
		}
		else
		{
			cond = this;
		}
		
		return cond.checkConditionUsingPerspective(perspective);
	}
	
	protected abstract float checkConditionUsingPerspective(AgentModel perspective);
	
	/**
	 * Clones this Condition, returning an equal copy.
	 * If this clone is changed afterwards, the original object remains the same.
	 * @return The Conditions's copy.
	 */
	public abstract Object clone();
	
	public Name getName() {
		return _name;
	}

	
	public ArrayList<Symbol> getToM()
	{
		return this._ToM;
	}
	
	public Symbol getToMLvl1()
	{
		return this._ToM.get(0);
	}
	
	
	public Symbol getToMLvl2()
	{
		if(this._ToM.size() > 1)
		{
			return this._ToM.get(1);
		}
		else return Constants.UNIVERSAL_SYMBOL;
	}
	
	
	/**
	 * This method finds all the possible sets of Substitutions that applied to the condition
     * will make it valid (true) according to the agent's internal state (KB + AM + emotional state + etc)
     * @return A list with all SubstitutionsSets that make the condition valid, if there are 
     * no such substitutions, the method returns null
	 */
	public final ArrayList<SubstitutionSet> getValidSubstitutions(AgentModel am) {
		
		Condition cond;
		AgentModel perspective = am.getModelToTestCondition(this);
		if(perspective == null) return null;
		
		//if the perspective is different from the current agent model, we need to change perspectives
		if(!perspective.getName().equals(am.getName()))
		{
			cond = (Condition) this.clone();
			cond.removePerspective(am.getName());
			cond.applyPerspective(perspective.getName());
		}
		else
		{
			cond = this;
		}
		
		return cond.getValidSubstitutionsUsingPerspective(perspective);
	}
	
	protected abstract ArrayList<SubstitutionSet> getValidSubstitutionsUsingPerspective(AgentModel perspective);
	
	/**
	 * Gets the condition's value - the object compared against the condition's name
	 * @return the condition's value
	 */
	public abstract Name getValue();
	
	public boolean hasChangedVerifiability() {
		boolean aux = _hasChangedVerifiability;
		_hasChangedVerifiability = false;
		return aux;
	}
	
	/**
	 * Indicates if the Predicate is grounded (no unbound variables in it's WFN)
	 * Example: Stronger(Luke,John) is grounded while Stronger(John,[X]) is not.
	 * @return true if the Predicate is grounded, false otherwise
	 */
	public boolean isGrounded() {
		if(!_name.isGrounded()) return false;
		for(Symbol s : this._ToM)
		{
			if(!s.isGrounded()) return false;
		}
		
		return true;
	}

	
	public boolean isStatic()
	{
		return this._static;
	}
	
	public boolean isToMGrounded()
	{
		for(Symbol s : this._ToM)
		{
			if(!s.isGrounded()) return false;
		}
		
		return true;
	}
	
	public boolean isVerifiable()
	{
		return this._currentVerifiableStatus;
	}

	
	/**
	 * Applies a set of substitutions to the object, grounding it.
	 * Example: Applying the substitution "[X]/John" in the name "Weak([X])" returns
	 * "Weak(John)". 
	 * Attention, this method modifies the original object.
	 * @param bindings - A list of substitutions of the type "[Variable]/value"
	 * @see Substitution
	 */
    public void makeGround(ArrayList<Substitution> bindings)
    {
    	this._name.makeGround(bindings);
    	for(Symbol s : this._ToM)
    	{
    		s.makeGround(bindings);
    	}
    }

	/**
	 * Applies a set of substitutions to the object, grounding it.
	 * Example: Applying the substitution "[X]/John" in the name "Weak([X])" returns
	 * "Weak(John)". 
	 * Attention, this method modifies the original object.
	 * @param subst - a substitution of the type "[Variable]/value"
	 * @see Substitution
	 */
    public void makeGround(Substitution subst)
    {
    	this._name.makeGround(subst);
    	for(Symbol s : this._ToM)
    	{
    		s.makeGround(subst);
    	}
    }

	public void removePerspective(String name)
	{
		this._name.removePerspective(name);
		
		for(Symbol s : _ToM)
		{
			s.removePerspective(name);
		}
	}
	
	
	/**
	 * Replaces all unbound variables in the object by applying a numeric 
     * identifier to each one. For example, the variable [x] becomes [x4]
     * if the received ID is 4. 
     * Attention, this method modifies the original object.
     * @param variableID - the identifier to be applied
	 */
    public void replaceUnboundVariables(int variableID)
    {
    	this._name.replaceUnboundVariables(variableID);
    	for(Symbol s : this._ToM)
    	{
    		s.replaceUnboundVariables(variableID);
    	}
    }
	
	protected void setName(Name name){
		this._name = name;
	}
	
	public void setStatic(boolean st)
	{
		_static = st;
	}
	
	protected void setToM(String ToM)
	{
		this._ToM = new ArrayList<Symbol>();
		StringTokenizer st = new StringTokenizer(ToM,":");
		while(st.hasMoreTokens())
		{
			this._ToM.add(new Symbol(st.nextToken()));
		}
		if(this._ToM.size() == 0)
		{
			this._ToM.add(Constants.UNIVERSAL_SYMBOL);
		}
	}
	
	public void setVerifiable(boolean verifiableStatus) {
		_currentVerifiableStatus = verifiableStatus;
		
	}

}