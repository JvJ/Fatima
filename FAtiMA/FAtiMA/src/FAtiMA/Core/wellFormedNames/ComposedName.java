/** 
 * ComposedName.java - Instantiation of a Well Formed Name composed by several symbols 
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
 * Created: 17/01/2004 
 * @author: Jo�o Dias
 * Email to: joao.assis@tagus.ist.utl.pt
 * 
 * History: 
 * Jo�o Dias: 17/01/2004 - File created
 * Jo�o Dias: 14/05/2006 - The class constructor does not receive the constant value anymore
 * Jo�o Dias: 17/05/2006 - added clone() method
 * Jo�o Dias: 22/05/2006 - Added comments to each public method's header
 * Jo�o Dias: 10/07/2006 - the class is now serializable
 * Jo�o Dias: 12/07/2006 - Added the evaluate method to names
 * Jo�o Dias: 12/07/2006 - Changes in groundable methods, the class now implements
 * 						   the IGroundable Interface, the old ground methods are
 * 					       deprecated 
 * Jo�o Dias: 22/09/2006 - Added the evaluate attribute. If this attribute is false, the 
 * 						   the evaluate method behaves like a Symbol, just returning a 
 * 						   String with the composedName. If the attribute is true, it will
 * 						   evaluate using the value stored in the KB.
 * 						 - In order to make less changes, the constructor remains but I've
 * 						   added a method to change the evaluation. By default a ComposedName
 * 						   is evaluated as normal.
 */

package FAtiMA.Core.wellFormedNames;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.ListIterator;
import java.util.StringTokenizer;

import FAtiMA.Core.memory.Memory;

/**
 * @see Name
 * @see Symbol
 * Well Formed Name composed by several symbols. 
 * If S and s1,s2,...sn are symbols, then S(s1,s2,...,sn) is a Composed Name 
 * The first symbol "S" is called the major symbol and it is followed by a list of
 * comma separated parameter symbols (s1,s2,..,sn), which are enclosed in parenthesis.
 * 
 * @author Jo�o Dias
 */

public class ComposedName extends Name implements Serializable {

	private static final long serialVersionUID = 1L;
	
	protected ArrayList<Symbol> _literals;
	protected boolean _evaluate = true;

	/**
	 * Creates a new ComposedName, receiving a major symbol, and a string with several 
	 * parameter symbols
	 * @param name - the Major symbol
	 * @param literals - string containing parameter symbols separated by commas
	 */
	public ComposedName(String name, String literals) {

		StringTokenizer st;
		Symbol l;

		//initial size - two literals
		_literals = new ArrayList<Symbol>(2);
		_constant = false;
		_grounded = true;
		l = new Symbol(name);
		if (!l.isGrounded())
			_grounded = false;
		_literals.add(l);
		if(literals != null) {
			st = new StringTokenizer(literals, ",");
			while (st.hasMoreTokens()) {
				l = new Symbol(st.nextToken());
				if (!l.isGrounded())
					_grounded = false;
				_literals.add(l);
			}
		}
	}
	
	private ComposedName() {
	}
	
	public void applyPerspective(String agentName)
	{
		for(Symbol s : _literals)
		{
			s.applyPerspective(agentName);
		}
	}
	
	public void removePerspective(String agentName)
	{
		for(Symbol s : _literals)
		{
			s.removePerspective(agentName);
		}
	}
	
	
	/**
	 * Sets the evaluation method. If this attribute is false, the evaluate method 
	 * behaves like a Symbol, and does not evaluate the name but just returns a 
	 * String with the composedName. If the attribute is true, it will evaluate 
	 * using the value stored in the KB.
	 * */
	public void SetEvaluation(boolean evaluate)
	{
		this._evaluate = evaluate;
	}
	
	/**
	 * Get's the Name's first symbol. Since this name is a ComposedName 
	 * (composed by several symbols), this function returns the first one, which
	 * corresponds to the Major symbol
	 * @return the Major symbol
	 */
	public Symbol getFirstLiteral() {
		return (Symbol) _literals.get(0);
	}
	
	/**
	 * Generates a list with all symbols contained in the ComposedName
	 * @return the list with the symbols
	 */
	public ArrayList<Symbol> getLiteralList() {
		return _literals;
	}
	
	public ArrayList<Symbol> getVariableList()
	{
		ArrayList<Symbol> variables = new ArrayList<Symbol>();
		
		for(Symbol s : _literals)
		{
			if(!s.isGrounded())
			{
				variables.add(s);
			}
		}
		
		return variables;
	}
	
	/**
	 * Evaluates this Name according to the evaluation attribute. If this attribute is false, 
	 * the evaluate method behaves like a Symbol, and does not evaluate the name but just
	 * returns a String with the composedName. If the attribute is true, it will search 
	 * the value associated to the ComposedName in the KnowledgeBase
	 * @param kb - a reference to the KnowledgeBase
	 * @return  the result of evaluation the ComposedName
	 */
	public Object evaluate(Memory m) 
	{
		if(_evaluate)
		{
			//All ComposedNames correspond to properties or predicates and in this case
		    //we must retrieve its value from the KnowledgeBase
		    if (!this._grounded) return null;
		    return m.getSemanticMemory().AskProperty(this);
		}
		else return this.toString();
	}
	
	/**
	 * Replaces all unbound variables in the object by applying a numeric 
     * identifier to each one. For example, the variable [x] becomes [x4]
     * if the received ID is 4. 
     * Attention, this method modifies the original object.
     * @param variableID - the identifier to be applied
	 */
	public void replaceUnboundVariables(int id)
	{
		if (this._grounded) return;
		
		ListIterator<Symbol> li = _literals.listIterator();
		while(li.hasNext())
		{
			li.next().replaceUnboundVariables(id);
		}
	}
	
	/**
	 * Applies a set of substitutions to the object, grounding it.
	 * Example: Applying the substitution "[X]/John" in the name "Weak([X])" returns
	 * "Weak(John)". 
	 * Attention, this method modifies the original object.
	 * @param bindings - A list of substitutions of the type "[Variable]/value"
	 * @see Substitution
	 */
	public void makeGround(ArrayList<Substitution> bindingConstraints)
	{
		Symbol s;
		
		if(this._grounded) return;
		this._grounded = true;
		
		ListIterator<Symbol> li = _literals.listIterator();
		while(li.hasNext())
		{
			s =  li.next();
			s.makeGround(bindingConstraints);
			if(!s.isGrounded())
			{
				this._grounded = false;
			}
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
		Symbol s;
		
		if(this._grounded) return;
		this._grounded = true;
		
		ListIterator<Symbol> li = _literals.listIterator();
		while(li.hasNext())
		{
			s = li.next();
			s.makeGround(subst);
			if(!s.isGrounded())
			{
				this._grounded = false;
			}
		}
	}
	
	/**
	 * Clones this Composed Name, returning an equal copy.
	 * If this clone is changed afterwards, the original object remains the same.
	 * @return The ComposedName's copy.
	 */
	public Object clone()
	{
	    ComposedName comp = new ComposedName();
	    comp._constant = this._constant;
	    comp._grounded = this._grounded;
	    comp._evaluate = this._evaluate;
	    
	    comp._literals = new ArrayList<Symbol>();
	    ListIterator<Symbol> li = this._literals.listIterator();
	    while(li.hasNext())
	    {
	        comp._literals.add((Symbol) li.next().clone());
	    }
	    return comp;
	}

	/**
	 * Converts the ComposedName to a String
	 * @return the converted String
	 */
	public String toString() {
		String str;
		ListIterator<Symbol> li;

		li = _literals.listIterator();
		str = li.next() + "(";
		if(li.hasNext()) {
			str = str +  li.next();
		}
		while (li.hasNext()) {
			str = str + "," + li.next();
		}
		str = str + ")";

		return str;
	}
}