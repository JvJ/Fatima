package FAtiMA.ToM;

import java.util.ArrayList;

import FAtiMA.Core.conditions.Condition;
import FAtiMA.Core.plans.DefaultPlannerUnifierStrategy;
import FAtiMA.Core.util.Constants;
import FAtiMA.Core.wellFormedNames.Substitution;
import FAtiMA.Core.wellFormedNames.Unifier;

public class PlannerUnifierStrategy extends DefaultPlannerUnifierStrategy {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public PlannerUnifierStrategy()
	{
	}
	
	@Override
	public boolean isThreat(Condition c1, Condition c2) {
		if(c1.getToMLvl1().equals(Constants.UNIVERSAL_SYMBOL) || c1.getToMLvl1().equals(c2.getToMLvl1()))
		{
			return super.isThreat(c1, c2);
		}
		else return false;
	}
	
	@Override
	public boolean unifyConditions(Condition c1, Condition c2, ArrayList<Substitution> subs)
	{ 
		if(c1.getToMLvl1().equals(Constants.UNIVERSAL_SYMBOL) ||
				c2.getToMLvl1().equals(Constants.UNIVERSAL_SYMBOL) ||
				Unifier.Unify(c1.getToMLvl1(),c2.getToMLvl1(),subs))
		{
			return super.unifyConditions(c1, c2, subs);
		}
		else return false;
	}
}
