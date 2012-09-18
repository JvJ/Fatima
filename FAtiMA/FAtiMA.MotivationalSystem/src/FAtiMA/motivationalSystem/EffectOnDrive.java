package FAtiMA.motivationalSystem;

import java.io.Serializable;
import java.util.ArrayList;

import FAtiMA.Core.wellFormedNames.IGroundable;
import FAtiMA.Core.wellFormedNames.Substitution;
import FAtiMA.Core.wellFormedNames.Symbol;



public class EffectOnDrive implements Serializable, IGroundable, Cloneable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Symbol _value;
	private short _type;
	private String _driveName;
	private Symbol _target;
	
	public EffectOnDrive(Short type, String driveName, Symbol target, Symbol value)
	{
		this._type = type;
		this._target = target;
		this._value = value;
		this._driveName = driveName;
	}
	
	private EffectOnDrive(EffectOnDrive eff)
	{
		this._type = eff._type;
		this._value = (Symbol) eff._value.clone();
		this._driveName = eff._driveName;
		this._target = (Symbol) eff._target.clone();
	}
	
	public short getType()
	{
		return this._type;
	}
	
	public Symbol getTarget()
	{
		return this._target;
	}
	
	public Symbol getValue()
	{
		return this._value;
		/*if(!this._value.isGrounded()) return 0;
		return Float.parseFloat(this._value.toString());*/
	}
	
	public String getDriveName()
	{
		return this._driveName;
	}
	
	public Object clone()
	{
		return new EffectOnDrive(this);
	}

	@Override
	public void replaceUnboundVariables(int variableID) {
		this._target.replaceUnboundVariables(variableID);
		this._value.replaceUnboundVariables(variableID);
		
	}

	@Override
	public void makeGround(ArrayList<Substitution> bindings) {
		this._target.makeGround(bindings);
		this._value.makeGround(bindings);
	}

	@Override
	public void makeGround(Substitution subst) {
		this._target.makeGround(subst);
		this._value.makeGround(subst);
	}

	@Override
	public boolean isGrounded() {
		return this._target.isGrounded() && this._value.isGrounded();
	}

}
