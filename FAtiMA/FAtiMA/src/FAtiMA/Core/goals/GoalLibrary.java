/** 
 * GoalLibrary.java - Stores information about all goals in the domain
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
 * @author: João Dias
 * Email to: joao.assis@tagus.ist.utl.pt
 * 
 * History: 
 * João Dias: 17/01/2004 - File created
 * João Dias: 24/05/2006 - Added comments to each public method's header
 * João Dias: 10/07/2006 - the class is now serializable 
 */

package FAtiMA.Core.goals;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.ListIterator;

import FAtiMA.Core.AgentModel;
import FAtiMA.Core.IntegrityValidator;
import FAtiMA.Core.exceptions.GoalLibParsingException;
import FAtiMA.Core.exceptions.UnreachableGoalException;
import FAtiMA.Core.wellFormedNames.Name;


/**
 * Stores information about all goals in the domain
 * 
 * @author João Dias
 */
public class GoalLibrary implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private ArrayList<Goal> _goals;
	
	/**
	 * Creates a new GoalLibrary
	 */
	public GoalLibrary()  {
		_goals = new ArrayList<Goal>();
	}
	/**
	 * Loads the goal's library file
	 * @param file - the file that contains the information about goals and that
	 * 				 will be parsed
	 *  
	 * @throws GoalLibParsingException - thrown when there is a parsing error when reading
	 * 		   the GoalLibrary file
	 */
	/*public void LoadGoalsFile(String file) throws GoalLibParsingException 
	{
		GoalLoaderHandler g = Load(file);
		_goals = g.GetGoals();
	}*/
	
	public void AddGoal(Goal g)
	{
		_goals.add(g);
	}
	
	/**
	 * Gets a ListIterator that allows you to iterate over the goals stored in
	 * the goal's library
	 * @return a ListIterator over goals
	 */
	public ListIterator<Goal> GetGoals() {
	    return _goals.listIterator();
	}

	/*private void SetGoalLinks() {
		Goal g;
		ListIterator li = _goals.listIterator();
		while (li.hasNext()) {
			g = (Goal) li.next();
			g.SetGoalLinks(this);
		}
	}*/

	/**
	 * Searches for a goal with the given name in the Library.
	 * If such goal is found, it is returned.
	 * @return the searched goal if exists in the GoalLibrary, null otherwise
	 */
	public Goal GetGoal(Name goalName) {
		ListIterator<Goal> li;
		Goal g;

		li = _goals.listIterator();
		while (li.hasNext()) {
			g = (Goal) li.next();
			if (g.getName().equals(goalName))
				return g;
		}
		return null;
	}

	public void checkIntegrity(AgentModel am, IntegrityValidator val) throws Exception
	{
		for(Goal g : _goals)
		{
			g.checkIntegrity(am, val);
		}
	}
}