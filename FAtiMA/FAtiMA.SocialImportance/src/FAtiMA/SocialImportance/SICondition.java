package FAtiMA.SocialImportance;

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
 * The xml should be defined as <LikeRelation SUBJECT_STR="subject_name" TARGET_STR="character_name" OPERATOR_STR="operator" VALUE_STR="relation_value"/>,
 * for example, by default: <LikeRelation subject="Luke" target="John" operator=">" value="3"/>.
 * 		A target must be a character;
 * 		The value must be an integer in the range [-10;10]; 
 * 		Operator can be one of the following < <= = >= > !=
 * @author nafonso
 * @see Context
 * @see Ritual
 */
public class SICondition extends Condition {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	static Operator LESS_THAN = new LessThan();
	static Operator LESS_THAN_OR_EQUAL = new LessThanOrEqual();
	static Operator EQUAL = new Equal();
	static Operator MORE_THAN_OR_EQUAL = new MoreThanOrEqual();
	static Operator MORE_THAN = new MoreThan();
	static Operator NOT_EQUAL = new NotEqual();

	private Operator _operator;
	private Symbol _value;

	public static SICondition ParseSocialCondition(Attributes attributes) throws InvalidEmotionTypeException, ContextParsingException {
		SICondition sc;
		String ToM;
		Symbol target = null;
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


		aux = attributes.getValue("target");
		if(aux != null)
		{
			target = new Symbol(aux);
		}

		aux = attributes.getValue("operator");
		op = SICondition.parseOperator(aux);

		aux = attributes.getValue("value");
		if(aux != null)
		{
			value = new Symbol(aux);	
		}

		sc = new SICondition(target,value,op,ToM);

		return sc;
	}

	protected SICondition(Symbol target, Symbol value, Operator op, String ToM){
		super(target,ToM);
		this._value = value;
		this._operator = op;
	}

	protected SICondition(SICondition siC){
		super(siC);
		_value = (Symbol)siC._value.clone();
		_operator = siC._operator;
	}

	public Object clone()
	{
		return new SICondition(this);
	}

	public float checkConditionUsingPerspective(AgentModel perspective) {
		float existingValue;

		if(!this.isGrounded()) return 0;

		existingValue = SocialImportanceRelation.getRelation(Constants.SELF_STRING, getName().toString()).getValue(perspective.getMemory());

		if(_operator.process(existingValue, Float.parseFloat(_value.toString())))
		{
			return 1;
		}
		else
		{
			return 0;
		}
	}


	public String toString()
	{
		return getToM() + " SI " + _operator + " " + getName() + " " + _value;
	}


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

	private interface Operator{
		public abstract boolean process( float val1, float val2 );
	}

	private static class LessThan implements Serializable, Operator {
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

	private static class LessThanOrEqual implements Serializable, Operator{
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

	private static class Equal implements Serializable, Operator{
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

	private static class MoreThanOrEqual implements Serializable, Operator {
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

	private static class MoreThan implements Serializable, Operator{
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

	@Override
	public Name getValue() {
		return new Symbol(String.valueOf(this._value));
	}

	@Override
	public ArrayList<SubstitutionSet> getValidSubstitutionsUsingPerspective(AgentModel perspective) {
		ArrayList<SubstitutionSet> bindingSets = new ArrayList<SubstitutionSet>();
		ArrayList<SubstitutionSet> validBindings = new ArrayList<SubstitutionSet>();
		SubstitutionSet subSet;

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
				Float existingValue = SocialImportanceRelation.getRelation(Constants.SELF_STRING, getName().toString()).getValue(perspective.getMemory());
				subSet = new SubstitutionSet();
				subSet.AddSubstitution(new Substitution(this._value,new Symbol(existingValue.toString())));
				bindingSets.add(subSet);
				return bindingSets;
			}	
		}


		Name siProperty = Name.ParseName("SI(" + Constants.SELF_STRING + "," + getName() + ")");

		bindingSets = perspective.getMemory().getSemanticMemory().GetPossibleBindings(siProperty);
		if(bindingSets != null){
			for(SubstitutionSet subst : bindingSets){
				SICondition auxCondition = (SICondition) this.clone();

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

	@Override
	public boolean isGrounded() {
		return super.isGrounded() && this._value.isGrounded();
	}
}
