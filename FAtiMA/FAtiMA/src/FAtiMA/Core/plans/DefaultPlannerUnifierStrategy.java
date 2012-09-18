package FAtiMA.Core.plans;

import java.io.Serializable;
import java.util.ArrayList;

import FAtiMA.Core.conditions.Condition;
import FAtiMA.Core.conditions.PastEventCondition;
import FAtiMA.Core.conditions.PropertyNotEqual;
import FAtiMA.Core.wellFormedNames.Substitution;
import FAtiMA.Core.wellFormedNames.Unifier;

public class DefaultPlannerUnifierStrategy implements Serializable, IPlannerUnifierStrategy {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DefaultPlannerUnifierStrategy()
	{
	}

	@Override
	public boolean isThreat(Condition c1, Condition c2) {
		
		boolean different;
		
		if(c1.getName().equals(c2.getName())) 
		{
			different = !c1.getValue().equals(c2.getValue());
			if(c2 instanceof PropertyNotEqual)
			{
				return !different;
			}
			else return different;
	    }
		return false;
	}
	
	@Override
	public boolean unifyConditions(Condition c1, Condition c2, ArrayList<Substitution> subs)
	{ 
		
		boolean unifyResult;
	
		if(c1 instanceof PastEventCondition)
		{
			unifyResult = Unifier.PartialUnify(c1.getName(),c2.getName(), subs);
		}
		else
		{
			unifyResult = Unifier.Unify(c1.getName(), c2.getName(), subs);
		}
		
		if(unifyResult)
		{
			if (c1 instanceof PropertyNotEqual)
			{
				unifyResult = Unifier.Disunify(c1.getValue(), c2.getValue(), subs);
			}
			else
			{
				unifyResult = Unifier.Unify(c1.getValue(), c2.getValue(), subs);
			}
		}
		
		return unifyResult;
	}
}
