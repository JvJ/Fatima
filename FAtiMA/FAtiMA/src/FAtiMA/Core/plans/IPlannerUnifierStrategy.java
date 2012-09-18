package FAtiMA.Core.plans;

import java.util.ArrayList;

import FAtiMA.Core.conditions.Condition;
import FAtiMA.Core.wellFormedNames.Substitution;

public interface IPlannerUnifierStrategy {
	
	/**
	 * Checks if a given condition c1 threatens another condition c2 
	 * @param c1 - the condition that we want to check for threat
	 * @param c2 - a possible threatened condition
	 * @return true if condition c1 threatens condition c2 
	 */
	public boolean isThreat(Condition c1, Condition c2);
	
	/**
	 * Elaborate unifying method that tries to unify two conditions, meaning that one of them can establish
	 * the other in a plan. This unifying method takes into account particular types of conditions, ToM and 
	 * other factors.
	 * @param c1 - the first conditions to be unified
	 * @param c2 - the second condition to be unified
	 * @param subs - you should put here an empty list of substitutions that will be filled by this method with
	 *               the required substitutions to make c1 and c2 syntactically equal. If this list is not empty,
	 *               the method will first start by applying the substitutions in the list to both conditions, and
	 *               then proceeds to try to unify them (adding other substitutions to the list if required)
	 * @return true if c1 can be made syntactically equal to c2, by applying a set of substitutions to both c1 and c2. False otherwise.
	 */
	public boolean unifyConditions(Condition c1, Condition c2, ArrayList<Substitution> subs);
}
