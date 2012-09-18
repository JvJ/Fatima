package FAtiMA.DeliberativeComponent;

import FAtiMA.Core.AgentModel;
import FAtiMA.Core.plans.IPlanningOperator;
import FAtiMA.Core.plans.Step;

public interface IActionFilterStrategy {
	public boolean filterActionFromPlan(AgentModel am, IPlanningOperator step);
}
