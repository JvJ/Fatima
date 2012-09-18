/**
 * PropertyGreaterEqual.java - Class that represents a specific property test, in this case checks
 * if one property is bigger or equal than another value (only works with numeric properties)
 *  
 * Copyright (C) 2007 GAIPS/INESC-ID 
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
 * Created: 12/02/2007 
 * @author: Jo�o Dias
 * Email to: joao.assis@tagus.ist.utl.pt
 * 
 * History: 
 * Jo�o Dias: 12/02/2007 - File created
 */

package FAtiMA.Core.conditions;

import FAtiMA.Core.AgentModel;
import FAtiMA.Core.util.AgentLogger;
import FAtiMA.Core.wellFormedNames.Name;


/**
 * Test that compares if a property is bigger than a given value. Only works with numeric values.
 * 
 * @author Jo�o Dias
 */

public class PropertyGreaterEqual extends PropertyCondition {

	
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
     * Creates a new PropertyTest of Type GreaterEqual
     * 
     * @param name -
     *            the PropertyTest's name
     * @param value -
     *            the PropertyTest's value
     */
	public PropertyGreaterEqual(Name name, Name value, String ToM) {
		super(name, value, ToM);
	}
	
	protected PropertyGreaterEqual(PropertyGreaterEqual pGE)
	{
		super(pGE);
	}

	/**
     * Checks if the Property Condition is verified in the agent's memory (KB + AM)
     * @return true if the condition is verified, false otherwise
     */
	public float checkConditionUsingPerspective(AgentModel perspective) {
		Object propertyValue;
		Object value;
		Float aux;
		Float aux2;

		if(!this.isGrounded()) return 0;
        
        propertyValue = this.getName().evaluate(perspective.getMemory());
        value = this.getValue().evaluate(perspective.getMemory());

		if (propertyValue == null || value == null)
			return 0;
		aux = new Float( propertyValue.toString());
		aux2 = new Float(value.toString());
		if(aux.floatValue() >= aux2.floatValue())
		{
			return 1;
		}
		else
		{
			return 0;
		}
	}
	
	/**
	 * Clones this PropertyTest, returning an equal copy.
	 * If this clone is changed afterwards, the original object remains the same.
	 * @return The PropertyTest's copy.
	 */
	public Object clone()
	{
		return new PropertyGreaterEqual(this);
	}
	

	

	/**
	 * Prints the PropertyTest to the Standard Output
	 */
	public void print() {
		super.print();
		AgentLogger.GetInstance().logAndPrint(" Operator: GreaterEqual");
	}

	/**
     * Converts the PropertyTest to a String
     * @return the Converted String
     */
	public String toString() {
		return getToM() + ":" + getName() + " >= " + getValue();
	}
}