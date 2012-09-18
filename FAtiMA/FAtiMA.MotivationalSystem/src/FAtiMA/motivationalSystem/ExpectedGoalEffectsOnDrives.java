package FAtiMA.motivationalSystem;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;

import FAtiMA.Core.wellFormedNames.Name;

public class ExpectedGoalEffectsOnDrives implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Name _goalName;
	private ArrayList<EffectOnDrive> _effects;
	
	public ExpectedGoalEffectsOnDrives(String goalName)
	{
		this._goalName = Name.ParseName(goalName);
		_effects = new ArrayList<EffectOnDrive>();
	}
	
	public Name getGoalName()
	{
		return _goalName;
	}
	
	public void AddEffect(EffectOnDrive e)
	{
		_effects.add(e);
	}
	
	public Collection<EffectOnDrive> getEffects()
	{
		return _effects;
	}

}
