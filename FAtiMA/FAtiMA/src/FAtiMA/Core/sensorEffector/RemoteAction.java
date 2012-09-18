/** 
 * RemoteAction.java - Represents an Action that is ready to be sent 
 * 					   to the remote virtual world for execution 
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
 * Created: 29/08/2004 
 * @author: João Dias
 * Email to: joao.assis@tagus.ist.utl.pt
 * 
 * History: 
 * João Dias: 29/08/2006 - File created
 * João Dias: 18/09/2006 - Added the Setter setActionType
 * 						 - Added the AddParameter method
 * João Dias: 28/09/2006 - Added the GetParameter method
 * João Dias: 05/10/2006 - Added parsing for RemoteActions
 * 						 - The empty constructor is now public, which is needed for parsing
 * 						 - Added Setters and Getters for subject and target
 * 						 - Added method ToEvent()
 * Joao Dias: 08/10/2006 - I was forgetting to add "</Action>" at the end of the xml string
 * 						   returned by the method toXml()
 */


package FAtiMA.Core.sensorEffector;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.ListIterator;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;

import FAtiMA.Core.AgentModel;
import FAtiMA.Core.ValuedAction;
import FAtiMA.Core.emotionalState.ActiveEmotion;
import FAtiMA.Core.util.Constants;
import FAtiMA.Core.util.enumerables.EventType;
import FAtiMA.Core.util.parsers.RemoteActionHandler;
import FAtiMA.Core.wellFormedNames.Symbol;


public class RemoteAction implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	protected String _subject;
	protected String _actionType;
	protected String _target = null;
	protected ArrayList<String> _parameters;
	protected ActiveEmotion _emotion;

	
	/**
	 * Parses a RemoteAction from a XML formatted String
	 * @param xml - the XML string to be parsed
	 * @return the parsed RemoteAction
	 */
	public static RemoteAction ParseFromXml(String xml) {
		RemoteActionHandler sh = new RemoteActionHandler();
		
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();
			saxParser.parse(new InputSource(new ByteArrayInputStream(xml.getBytes())), sh);
			return sh.getRemoteAction();
		}
		catch (Exception ex) {
			return null;
		}
	}
	
	/**
	 * Creates a new empty RemoteAction
	 *
	 */
	public RemoteAction()
	{
		_parameters = new ArrayList<String>();
	}
	
	private RemoteAction(RemoteAction ra)
	{
		this._subject = ra._subject;
		this._actionType = ra._actionType;
		this._target = ra._target;
		this._parameters = new ArrayList<String>(ra._parameters);
		//TODO got to clone this also
		this._emotion = ra._emotion;
	}
	
	public RemoteAction(AgentModel am, ValuedAction va)
	{
		String actionName;
		String aux;
		ListIterator<Symbol> li = va.getAction().getLiteralList().listIterator();
		
		actionName = li.next().toString();
		_actionType = actionName;
		_subject = am.getName();
		_parameters = new ArrayList<String>();
		_target = null;
		
		if(li.hasNext())
		{
			_target = li.next().toString();
			
			if(_target.equals(Constants.SELF_STRING))
			{
				_target = am.getName();
			}
			
			while(li.hasNext())
			{
				aux = li.next().toString();
				
				if(aux.equals(Constants.SELF_STRING))
				{
					aux = am.getName();
				}
				_parameters.add(aux);
			}
		}
		_emotion = va.getEmotion(am.getEmotionalState());

	}
	
	public RemoteAction clone()
	{
		return new RemoteAction(this);
	}
	
	public void AddParameter(String param)
	{
		_parameters.add(param);
	}
	
	public ArrayList<String> GetParameters()
	{
		return _parameters;
	}
	
	public String getSubject()
	{
		return this._subject;
	}
	
	public void setSubject(String subject)
	{
		this._subject = subject;
	}
	
	public String getActionType()
	{
		return this._actionType;
	}
	
	public void setActionType(String actionType)
	{
		this._actionType = actionType; 
	}
	
	public String getTarget()
	{
		return this._target;
	}
	
	public void setTarget(String target)
	{
		this._target = target;
	}
	
    
    /**
     * Converts the RemoteAction to an Event
     * @return the converted Event
     */
	public Event toEvent(short actionEventType) {
	    Event event;
	    event = new Event(_subject,_actionType,_target,EventType.ACTION,actionEventType);
		
		for(ListIterator<String> li = _parameters.listIterator(); li.hasNext();)
		{
			event.AddParameter(new Parameter("param",li.next()));
		}
		
		return event;
	}
    
	public String toXML()
    {
    	String xmlAction;
    	
    	xmlAction = "<Action><Subject>" + this._subject + "</Subject><Type>" +
    		_actionType + "</Type>";
    	
    	if(_target != null)
    	{
    		xmlAction = xmlAction + "<Target>" + _target + "</Target>";
    	}
    	
    	xmlAction = xmlAction + "<Parameters>";
    	
    	ListIterator<String> li = _parameters.listIterator();
    	while(li.hasNext())
    	{
    		xmlAction = xmlAction + "<Param>" + li.next() + "</Param>";
    	}
    	
    	xmlAction = xmlAction + "</Parameters>";
    	
    	if(_emotion != null)
    	{
    		xmlAction = xmlAction + _emotion.toXml();
    	}
    	
    	xmlAction = xmlAction + "</Action>";
    	
    	return xmlAction;
    }
    
    /**
     * @deprecated - you should try to send the message in xml and not
     * in plain text
     * @return
     */
    public String toPlainStringMessage()
    {
    	String msg;
    	msg = _actionType;
    	if(_target != null)
    	{
    		msg = msg + " " + _target;
    	}
    	if(_parameters.size() > 0)
    	{
    		ListIterator<String> li = _parameters.listIterator();
    		while(li.hasNext())
    		{
    			msg = msg + " " + li.next();
    		}
    	}
		
		return msg;
    }
}
