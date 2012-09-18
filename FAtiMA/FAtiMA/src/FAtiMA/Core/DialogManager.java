/** 
 * DialogManager.java - Manages dialog and conversation between agents
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
 * João Dias: 28/09/2006 - Support speechActs do not update the SpeechContext
 * João Dias: 30/07/2007 - Added decay for context. After a predifined ammount of time
 * 						   where noone says anything the current speech context disappears
 */

package FAtiMA.Core;

import java.io.Serializable;

import FAtiMA.Core.plans.DialogPairStep;
import FAtiMA.Core.sensorEffector.Event;
import FAtiMA.Core.wellFormedNames.Name;


/**
 * @author User
 *
 */
public class DialogManager implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static boolean _activeDialogPair;
	private static String _replierName;
	private static String _initiationAct;
	private static String _expectedReactiveAct;
	private static Name _dialogPairStepName;
	private static String _initiatorName;

	private static boolean _initiationConfirmed = false;
		
	public static void initiateDialogPair(String initiatorName, String initiationAct, String expectedReactiveAct, String replierName, Name dialogPairName){
		_initiatorName = initiatorName;
		_initiationAct = initiationAct;
		_replierName = replierName;
		_expectedReactiveAct = expectedReactiveAct;
		_dialogPairStepName = dialogPairName;
		_activeDialogPair = true;
	}
			
	public static void confirmInitiation(){
		_initiationConfirmed = true;
	}
	
	public static boolean isActiveDialogPair(){
		return _activeDialogPair;
	}
		
	public static void closeActiveDialogPair() {
		_initiationConfirmed = false;
		_initiatorName = null;
		_initiationAct = null;
		_replierName = null;
		_expectedReactiveAct = null;
		_dialogPairStepName = null;
		_activeDialogPair = false;
	}
	

	public static String getInitiationAct(){
		return _initiationAct;
	}

	public static Name getDialogPairName() {
		return _dialogPairStepName;
	}	
	
	public static String getInitiatorName(){
		return _initiatorName;
	}

	public static String getReplierName(){
		return _replierName;
	}
	
	public static void setDialogPairName(Name name){
		_dialogPairStepName = name;
	}
	public static void setExpectedReactiveAct(String reactiveAct){
		_expectedReactiveAct = reactiveAct;
	}
	
	public static String getExpectedReactiveAct(){
		return _expectedReactiveAct;
	}
	
	
	public static boolean matchEventWithInitiationAct(Event e){
		String eventFirstParameter = e.GetParameters().get(0).toString();
		if(e.GetAction().equals("SpeechAct")){
			if(e.GetTarget().equals(_replierName)){
				if(eventFirstParameter.equals(_initiationAct)){
					return true;
				}		
			}
		}
		return false;
	}
	
	public static boolean matchEventWithExpectedReactiveAct(Event e, String agentName){
		String eventFirstParameter = e.GetParameters().get(0).toString();
		
		if(e.GetSubject().equalsIgnoreCase(_replierName) &&
				e.GetTarget().equalsIgnoreCase(agentName) &&
					eventFirstParameter.equalsIgnoreCase(_expectedReactiveAct)){	
			return true;
		}else{
			return false;
		}
	}
	
	
	
	public static Name matchEventWithUnexpectedReactiveAct(Event e, AgentModel am){
		String unexpectedReactiveAct = e.GetParameters().get(0).toString();
		
		if(!e.GetSubject().equalsIgnoreCase(_replierName) || !e.GetTarget().equalsIgnoreCase(am.getName())){
			return null;
		}
	
		for(Name dialogPair : DialogPairStep.getInitiationActPossiblePairs(_initiationAct)){
			DialogPairStep step = (DialogPairStep) am.getActionLibrary().getAction(dialogPair.toString());
			if(step.getReactiveAct().equalsIgnoreCase(unexpectedReactiveAct)){
				return dialogPair;
			}
		}
		return null;
	
	}

	
	
	


}
