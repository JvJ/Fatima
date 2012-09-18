

package FAtiMA.Core.conditions;

import java.util.ArrayList;

import org.xml.sax.Attributes;

import FAtiMA.Core.AgentModel;
import FAtiMA.Core.memory.episodicMemory.AutobiographicalMemory;
import FAtiMA.Core.memory.semanticMemory.KnowledgeBase;
import FAtiMA.Core.wellFormedNames.Name;
import FAtiMA.Core.wellFormedNames.Substitution;
import FAtiMA.Core.wellFormedNames.SubstitutionSet;
import FAtiMA.Core.wellFormedNames.Symbol;


public class EquationCondition extends Condition {

	private static final long serialVersionUID = 1L;
	private String _operator;
	private Name _operand1;
	private Name _operand2;
	
	
	/**
	 * Parses a Predicate given a XML attribute list
	 * @param attributes - A list of XMl attributes
	 * @return - the Predicate Parsed
	 */
    public static EquationCondition ParseEquation(Attributes attributes) {
	
		Symbol result;
		String operator;
		Symbol operand1;
		Symbol operand2;
	
		result = new Symbol(attributes.getValue("result"));
		operator = attributes.getValue("operator");		
		operand1 = new Symbol(attributes.getValue("operand1"));
		operand2 = new Symbol(attributes.getValue("operand2"));
		
		return new EquationCondition(result,operator, operand1, operand2);
	}
		
	protected EquationCondition()
	{
	}

	protected EquationCondition(EquationCondition eC){
		super(eC);
		this._operator = eC._operator;
		this._operand1 = (Name) eC._operand1.clone();
		this._operand2 = (Name) eC._operand2.clone();
	}
	
	public EquationCondition(Symbol result, String operator, Name operand1, Name operand2) {
		super(result);
		this._operator = operator;
		this._operand1 = operand1;
		this._operand2 = operand2;
	}
	
	
	/**
	 * Checks if the Predicate is verified in the agent's KnowledgeBase
	 * @return true if the Predicate is verified, false otherwise
	 * @see KnowledgeBase
	 */
	public float checkConditionUsingPerspective(AgentModel perspective) {
		
		float result;
		
		if(!this.isGrounded()) return 0;
		
		result = Float.parseFloat(this.getName().toString());
		
		if(result == this.evaluate())
		{
			return 1;
		}
		else
		{
			return 0;
		}
	}
			
	
	/**
	 * Clones this Predicate, returning an equal copy.
	 * If this clone is changed afterwards, the original object remains the same.
	 * @return The Predicates's copy.
	 */
	public Object clone()
	{
	    return new EquationCondition(this);
	}
	
	public float evaluate()
	{
		float result = 0;
		Float operand1 = Float.parseFloat(this._operand1.toString());
		Float operand2 = Float.parseFloat(this._operand2.toString());
		
		if(this._operator.equals("*"))
		{
			result = operand1*operand2;
		}
		else if(this._operator.equals("+"))
		{
			result = operand1 + operand2;
		}
		else if(this._operator.equals("-"))
		{
			result = operand1 - operand2;
		}
		else if(this._operator.equals("/"))
		{
			result = operand1/operand2;
		}
		return result;
	}
	
	/**
	 * This method finds all the possible sets of Substitutions that applied to the condition
     * will make it valid (true) according to the agent's memory (KB + AM)
     * @return A list with all SubstitutionsSets that make the condition valid, if there are 
     * no such substitutions, the method returns null
     * @see KnowledgeBase
	 * @see AutobiographicalMemory
	 */
	public ArrayList<SubstitutionSet> getValidSubstitutionsUsingPerspective(AgentModel perspective) 
	{
		ArrayList<SubstitutionSet> substs = new ArrayList<SubstitutionSet>();
		SubstitutionSet subSet;
		Float result;
		
		if(this._operand1.isGrounded() && this._operand2.isGrounded())
		{
			if(this.getName().isGrounded())
			{
				if(this.checkConditionUsingPerspective(perspective)==1)
				{
					substs.add(new SubstitutionSet());
					return substs;
				}
				else
				{
					return null;
				}
			}
			else
			{
				subSet = new SubstitutionSet();
				result = new Float(this.evaluate());
				subSet.AddSubstitution(new Substitution(this.getName().getFirstLiteral(),new Symbol(result.toString())));
				substs.add(subSet);
				return substs;
			}
		}
		return null;
	}
	
	
	@Override
	public Name getValue() {
		return Name.ParseName(this._operator + "(" + this._operand1 + "," + this._operand2 + ")");
	}
	
	/**
	 * Indicates if the Predicate is grounded (no unbound variables in it's WFN)
	 * Example: Stronger(Luke,John) is grounded while Stronger(John,[X]) is not.
	 * @return true if the Predicate is grounded, false otherwise
	 */
	public boolean isGrounded() {
		return super.isGrounded() && this._operand1.isGrounded() && this._operand2.isGrounded();
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
    	super.makeGround(bindings);
    	this._operand1.makeGround(bindings);
    	this._operand2.makeGround(bindings);
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
    	super.makeGround(subst);
    	this._operand1.makeGround(subst);
    	this._operand2.makeGround(subst);
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
    	super.replaceUnboundVariables(variableID);
    	this._operand1.replaceUnboundVariables(variableID);
    	this._operand2.replaceUnboundVariables(variableID);
    }

	/**
	 * Converts the Predicate to a String
	 * @return the converted String
	 */
	public String toString() {
		return this.getName() + " = " + this._operand1 + " " + _operator + " " + this._operand2;
	}
}