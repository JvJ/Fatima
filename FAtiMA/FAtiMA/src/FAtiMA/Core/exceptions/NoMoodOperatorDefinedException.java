/** 
 * InvalidMoodOperatorException.java - Exception thrown when an invalid mood operator 
 * is parsed when defining a mood precondition
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
 * Created: 10/02/2007 
 * @author: Jo�o Dias
 * Email to: joao.assis@tagus.ist.utl.pt
 * 
 * History: 
 * Jo�o Dias: 10/02/2007 - File created
 */

package FAtiMA.Core.exceptions;

public class NoMoodOperatorDefinedException extends Exception {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public NoMoodOperatorDefinedException() {
        super("ERROR: No mood operator defined in a MoodCondition");
    }
}