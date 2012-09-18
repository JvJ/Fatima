package FAtiMA.socialRelations;

import java.io.Serializable;
import java.util.ArrayList;

import org.xml.sax.Attributes;


import FAtiMA.Core.AgentModel;
import FAtiMA.Core.conditions.Condition;
import FAtiMA.Core.exceptions.ContextParsingException;
import FAtiMA.Core.exceptions.InvalidEmotionTypeException;
import FAtiMA.Core.util.Constants;
import FAtiMA.Core.wellFormedNames.Name;
import FAtiMA.Core.wellFormedNames.Substitution;
import FAtiMA.Core.wellFormedNames.SubstitutionSet;
import FAtiMA.Core.wellFormedNames.Symbol;
/**
 * Represents a Like Relation that needs to be fullfiled in order to trigger
 * the condition.
 * The xml should be defined as <LikeRelation TARGET_STR="character_name" OPERATOR_STR="operator" VALUE_STR="relation_value"/>,
 * for example, by default: <LikeRelation target="John" operator=">" value="3"/>.
 * 		A target must be a character;
 * 		The value must be an integer in the range [-10;10]; 
 * 		Operator can be one of the following < <= = >= > !=
 * @author nafonso
 * @see Context
 * @see Ritual
 */
public class LikeCondition extends Condition {
	
	private static class Equal implements Serializable, Operator
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public boolean process( float val1, float val2 ){
			return val1 == val2;
		}
		
		public String toString()
		{
			return "=";
		}
	}
	
	private static class LessThan implements Serializable, Operator 
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public boolean process( float val1, float val2 ){
			return val1 < val2;
		}
		
		public String toString()
		{
			return "<";
		}
	}
	
	private static class LessThanOrEqual implements Serializable, Operator
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public boolean process( float val1, float val2 ){
			return val1 <= val2;
		}
		
		public String toString()
		{
			return "<=";
		}
	}
	
	private static class MoreThan implements Serializable, Operator
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public boolean process( float val1, float val2 ){
			return val1 > val2;
		}
		
		public String toString()
		{
			return ">";
		}
	}
	
	private static class MoreThanOrEqual implements Serializable, Operator 
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public boolean process( float val1, float val2 ){
			return val1 >= val2;
		}
		
		public String toString()
		{
			return ">=";
		}
	}
	private static class NotEqual implements Serializable, Operator{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public boolean process( float val1, float val2 ){
			return val1 != val2;
		}
		
		public String toString()
		{
			return "!=";
		}
	}
	
	private interface Operator{
		public abstract boolean process( float val1, float val2 );
	}
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static Operator LESS_THAN = new LessThan();
	
	private static Operator LESS_THAN_OR_EQUAL = new LessThanOrEqual();
	
	private static Operator EQUAL = new Equal();
	
	private static Operator MORE_THAN_OR_EQUAL = new MoreThanOrEqual();
	
	private static Operator MORE_THAN = new MoreThan();
	
	private static Operator NOT_EQUAL = new NotEqual();
	
	
	private Operator _operator;
	
	
	private Symbol _value;
	
	
	private static Operator parseOperator( String operator ) throws ContextParsingException{
		if( operator == null )
			throw new ContextParsingException("No operator was found in SocialRelationCondition");
		Operator auxOp;
		if(operator.equals("LesserThan"))
			auxOp = LESS_THAN; //Operator.LESS_THAN;
		else if(operator.equals("LesserEqual"))
			auxOp = LESS_THAN_OR_EQUAL;
		else if(operator.equals("="))
			auxOp = EQUAL;
		else if(operator.equals("GreaterEqual"))
			auxOp = MORE_THAN_OR_EQUAL;
		else if(operator.equals("GreaterThan"))
			auxOp = MORE_THAN;
		else if(operator.equals("!="))
			auxOp = NOT_EQUAL;
		else
			throw new ContextParsingException("Invalid operator '"+operator+"' found in SocialRelationCondition");
		return auxOp;
	}
	
	public static LikeCondition ParseSocialCondition(Attributes attributes) throws InvalidEmotionTypeException, ContextParsingException {
		LikeCondition sc;
		Symbol target;
		String ToM = null;
		Operator op;
		Symbol value = new Symbol("0");
		String aux;
		
		
		aux = attributes.getValue(Constants.ToM_STRING);
		if(aux == null)
		{
			ToM = Constants.UNIVERSAL_STRING;
		}
		else
		{
			ToM = aux;
		}
		
		target = new Symbol(attributes.getValue("target"));
		
		aux = attributes.getValue("operator");
		op = LikeCondition.parseOperator(aux);

		aux = attributes.getValue("value");
		if(aux != null)
		{
			value = new Symbol(aux);	
		}
		
		sc = new LikeCondition(target,ToM,value,op);
			
		return sc;
	}
	
	protected LikeCondition(LikeCondition lC){
		super(lC);
		_value = (Symbol)lC._value.clone();
		_operator = lC._operator;
	}
	
	protected LikeCondition(Symbol target, String ToM, Symbol value, Operator op){
		super(target,ToM);
		this._value = value;
		this._operator = op;
	}
	
	public float checkConditionUsingPerspective(AgentModel perspective) {
		float existingValue;
		
		if(!this.isGrounded()) return 0;
		
		//String targetName = getTargetName(perspective);
		
		existingValue = LikeRelation.getRelation(Constants.SELF_STRING, getName().toString()).getValue(perspective.getMemory());
		
		if(_operator.process(existingValue, Float.parseFloat(_value.toString())))
		{
			return 1;
		}
		else
		{
			return 0;
		}
	}
	
	public Object clone()
	{
		return new LikeCondition(this);
	}
	
	/*private String getTargetName(AgentModel am)
	{
		String targetName; 
		
		if(getToMLvl1().equals(Constants.SELF_SYMBOL))
		{
			targetName = getName().toString();
		}
		else
		{
			if(getName().equals(Constants.SELF_SYMBOL))
			{
				targetName = am.getName();
			}
			else
			{
				targetName = getName().toString();
			}
		}
		
		return targetName;
	}*/
	
	@Override
	public ArrayList<SubstitutionSet> getValidSubstitutionsUsingPerspective(AgentModel perspective) {
		ArrayList<SubstitutionSet> bindingSets = new ArrayList<SubstitutionSet>();
		ArrayList<SubstitutionSet> validBindings = new ArrayList<SubstitutionSet>();
		SubstitutionSet subSet;
		String targetName;
			
		if(this.getName().isGrounded())
		{
			//TODO complete the rest for when we have Like John Luke != [3], or Like John [y] = [z]
			if(this._value.isGrounded())
			{
				if(checkConditionUsingPerspective(perspective)==1)
				{
					bindingSets.add(new SubstitutionSet());
					return bindingSets;
				}
				else return null;
			}
			else 
			{
				//targetName = getTargetName(perspective);
				
				Float existingValue = LikeRelation.getRelation(Constants.SELF_STRING, getName().toString()).getValue(perspective.getMemory());
				subSet = new SubstitutionSet();
				subSet.AddSubstitution(new Substitution(this._value,new Symbol(existingValue.toString())));
				bindingSets.add(subSet);
				return bindingSets;
			}	
		}
		
		
		
		if(getToM().toString().equals(Constants.SELF_STRING))
		{
			targetName = getName().toString();
		}
		else
		{
			if(getName().toString().equals(Constants.SELF_STRING))
			{
				targetName = perspective.getName();
			}
			else
			{
				targetName = getName().toString();
			}
		}
	
		Name likeProperty = Name.ParseName("Like(" + Constants.SELF_STRING + "," + targetName + ")");
		
		bindingSets = perspective.getMemory().getSemanticMemory().GetPossibleBindings(likeProperty);
		

		if(bindingSets != null){
			for(SubstitutionSet subst : bindingSets){
				LikeCondition auxCondition = (LikeCondition) this.clone();

				Name aux = (Name) this.getName().clone();		
				aux.makeGround(subst.GetSubstitutions());

				if(aux.isGrounded()){
					auxCondition.setName(aux);
					if(auxCondition.checkConditionUsingPerspective(perspective) == 1){
						validBindings.add(subst);
					}
				}

			}
		}	
		return validBindings;
		
	}

	@Override
	public Name getValue() {
		return new Symbol(String.valueOf(this._value));
	}

	@Override
	public boolean isGrounded() {
		return super.isGrounded() && this._value.isGrounded();
	}

	@Override
	public void makeGround(ArrayList<Substitution> bindings) {
		super.makeGround(bindings);
		this._value.makeGround(bindings);
	}

	@Override
	public void makeGround(Substitution subst) {
		super.makeGround(subst);
		this._value.makeGround(subst);
	}

	@Override
	public void replaceUnboundVariables(int variableID) {
		super.replaceUnboundVariables(variableID);
		this._value.replaceUnboundVariables(variableID);
	}

	public String toString()
	{
		return getToM() + " like " + _operator + " " + getName() + " " + _value;
	}
}
