/** 
 * ActionTendencies.java - Implements a character's Action Tendencies
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
 * Jo�o Dias: 23/05/2006 - Added comments to each public method's header
 * Jo�o Dias: 02/07/2006 - Replaced System's timer by an internal agent simulation timer
 * Jo�o Dias: 10/07/2006 - the class is now serializable
 * Jo�o Dias: 27/12/2006 - Added Method ReinforceActionTendency
 */

package FAtiMA.ReactiveComponent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;

import FAtiMA.Core.AgentModel;
import FAtiMA.Core.ValuedAction;
import FAtiMA.Core.conditions.Condition;
import FAtiMA.Core.emotionalState.ActiveEmotion;
import FAtiMA.Core.emotionalState.BaseEmotion;
import FAtiMA.Core.emotionalState.EmotionalState;
import FAtiMA.Core.util.AgentLogger;
import FAtiMA.Core.wellFormedNames.Name;
import FAtiMA.Core.wellFormedNames.Substitution;
import FAtiMA.Core.wellFormedNames.Unifier;


/**
 * Implements a character's set of Action Tendencies and implements
 * the selection mechanism
 * 
 * @author Jo�o Dias
 */
public class ActionTendencies implements Serializable, Cloneable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	protected ArrayList<Action> _actions;
	//protected HashMap<String,Long> _filteredActions;
	protected ArrayList<ActiveEmotion> _filteredEmotions;
	
	/**
	 * Create a new ActionTendenciesSet
	 */
	public ActionTendencies() {
		_actions = new ArrayList<Action>();
		//_filteredActions = new HashMap<String,Long>();
		_filteredEmotions = new ArrayList<ActiveEmotion>();
	}

	/**
	 * Adds a Action to the ActionTendencies Set
	 * @param action
	 */
	public void AddAction(Action action) {
		_actions.add(action);
	}
	
	public ArrayList<Action> getActions()
	{
		return _actions;
	}
	
	/**
	 * deactives an emotion that was used to triggered an action tendency. It means that 
	 * this particular instance of the emotion cannot be used to trigger another action 
	 * even if the action's preconditions and emotions are verified
	 * @param va -  the action with the emotion that needs to be deactivated
	 */
	public void IgnoreActionEmotion(AgentModel am, ValuedAction va) {
		_filteredEmotions.add(va.getEmotion(am.getEmotionalState()));
	}
	
	public void ClearFilters()
	{
		_filteredEmotions.clear();
	}
	
	public void UpdateFilters()
	{
		ListIterator<ActiveEmotion> li = this._filteredEmotions.listIterator();
		
		while(li.hasNext())
		{
			if(li.next().GetIntensity() < 0.5f)
			{
				li.remove();
			}
		}
	}
	
	/*protected boolean isIgnored(ValuedAction va) {
		String actionName = va.getAction().toString();
		if(_filteredActions.containsKey(actionName)) {
			Long wakeUpTime = (Long)_filteredActions.get(actionName);
			return AgentSimulationTime.GetInstance().Time() < wakeUpTime.longValue();
		}
		else return false;
	}*/
	
	/**
	 * Selects the most appropriate ActionTendency given the 
	 * character's emotional state 
	 * @param emState - the agent's emotional state that influences the actions performed
	 * @return the most relevant Action (according to the emotional state)
	 */
	public ValuedAction SelectAction(AgentModel am) {
		Iterator<Action> it;
		Action a;
		ValuedAction va;
		ValuedAction bestAction = null;
		EmotionalState emState = am.getEmotionalState();
		
		this.UpdateFilters();
		
		it = _actions.iterator();
		while(it.hasNext()) {
			a = it.next();
			va = a.TriggerAction(am, _filteredEmotions, emState.GetEmotionsIterator());
			if (va != null) {
				if(bestAction == null || va.getValue(emState) > bestAction.getValue(emState)) 
				{
				    bestAction = va;
				}
			}	
		}
		
		return bestAction;
	}
	
	public BaseEmotion RecognizeEmotion(AgentModel am, Name action)
	{
		Iterator<Action> it;
		Action a;
		Action a2;
		ArrayList<Substitution> bindings = new ArrayList<Substitution>();
		
		it = _actions.iterator();
		while(it.hasNext())
		{
			a = (Action) it.next();
			if(Unifier.Unify(action, a.getName(), bindings))
			{
				a2 = (Action) a.clone();
				a2.makeGround(bindings);
				if(Condition.checkActivation(am, a2.GetPreconditions())!=null)
				{
					return a2.GetElicitingEmotion();
				}
			}
		}
		
		return null;
	}
	
	public void ReinforceActionTendency(String action)
	{
		action = action.toLowerCase();
		Action a;
		for(ListIterator<Action> li = _actions.listIterator();li.hasNext();)
		{
			a =  li.next();
			if(a.getName().toString().toLowerCase().contains(action))
			{
				AgentLogger.GetInstance().log("Reinforcing AT: " + a.getName());	
				a.ReinforceAction(2);
			}
		}
	}
	
	public void Print()
	{
		Action act;
		for(ListIterator<Action> li = _actions.listIterator();li.hasNext();)
		{
			act = li.next();
			AgentLogger.GetInstance().logAndPrint(act.toString());
		}
	}
	
	public Object clone()
	{
		ActionTendencies at = new ActionTendencies();
		for(Action a : this._actions)
		{
			at._actions.add(a);
		}
		
		at._filteredEmotions = new ArrayList<ActiveEmotion>(_filteredEmotions);
		
		return at;
	}
}