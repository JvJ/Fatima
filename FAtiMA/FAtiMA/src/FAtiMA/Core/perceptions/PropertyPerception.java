/** 
 * PropertyPerception.java - Represents a property change event that happened in the virtual world
 *  
 * Copyright (C) 2011 GAIPS/INESC-ID 
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
 * Created: November/2011 
 * @author: João Dias
 * Email to: joao.dias@gaips.inesc-id.pt
 * 
 * History: 
 * João Dias: November/2011 - File created
 * 						   
 */
package FAtiMA.Core.perceptions;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.StringTokenizer;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;

import FAtiMA.Core.sensorEffector.Event;
import FAtiMA.Core.util.Constants;
import FAtiMA.Core.util.parsers.PropertyPerceptionHandler;
import FAtiMA.Core.wellFormedNames.Name;
import FAtiMA.Core.wellFormedNames.Symbol;

public class PropertyPerception implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private Boolean _persistent;
	private Event _event;
	private ArrayList<Symbol> _ToM;
	private Name _property;
	private String _value;
	
	public static PropertyPerception parseFromXML(String xml)
	{
		PropertyPerceptionHandler ph = new PropertyPerceptionHandler();
		
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();
			saxParser.parse(new InputSource(new ByteArrayInputStream(xml.getBytes())), ph);
			return ph.getPropertyPerception();
		}
		catch (Exception ex) {
			return null;
		}
	}
	
	public PropertyPerception()
	{
		_persistent = null;
		_event = null;
		
		_ToM = new ArrayList<Symbol>();
		_ToM.add(Constants.UNIVERSAL_SYMBOL);
		
		_property = null;
		_value = null;
	}
	
	private PropertyPerception(PropertyPerception p)
	{
		if(p._event != null)
		{
			this._event = (Event) p._event.clone();
		}
		this._persistent = p._persistent;
		
		this._ToM = new ArrayList<Symbol>(p._ToM.size());
		for(Symbol s : p._ToM)
		{
			this._ToM.add((Symbol)s.clone());
		}
		
		this._property = (Name) p._property.clone();
		this._value = p._value;
	}
	
	public PropertyPerception(Boolean persistent, Event event, String ToM, Name property, String value)
	{
		this._event = event;
		this._persistent = persistent;
		this._property = property;
		this._value = value;
		
		this.setToM(ToM);
	}
	
	public PropertyPerception clone()
	{
		return new PropertyPerception(this);
	}
	
	public Event getEvent()
	{
		return this._event;
	}
	
	public ArrayList<Symbol> getToM()
	{
		return this._ToM;
	}
	
	public Name getProperty()
	{
		return this._property;
	}
	
	public String getValue()
	{
		return this._value;
	}
	
	public Boolean getPersistent()
	{
		return this._persistent;
	}
	
	public void setProperty(Name property)
	{
		this._property = property;
	}
	
	public void setValue(String value)
	{
		this._value = value;
	}
	
	public void setToM(String ToM)
	{
		this._ToM = new ArrayList<Symbol>();
		StringTokenizer st = new StringTokenizer(ToM,":");
		while(st.hasMoreTokens())
		{
			this._ToM.add(new Symbol(st.nextToken()));
		}
		if(this._ToM.size() == 0)
		{
			this._ToM.add(Constants.UNIVERSAL_SYMBOL);
		}
	}
	
	public void setEvent(Event event)
	{
		this._event = event;
	}
	
	public void setPersistent(Boolean persistent)
	{
		this._persistent = persistent;
	}
	
	public void applyPerspective(String name)
	{	
		this._property.applyPerspective(name);
		this._value = Name.applyPerspective(this._value, name);
		if(this._event != null)
		{
			this._event = this._event.applyPerspective(name);
		}
		
		for(Symbol s : this._ToM)
		{
			s.applyPerspective(name);
		}
	}
	
	public void removePerspective(String name)
	{	
		this._property.removePerspective(name);
		this._value = Name.removePerspective(this._value, name);
		if(this._event != null)
		{
			this._event = this._event.RemovePerspective(name);
		}
		
		for(Symbol s : this._ToM)
		{
			s.removePerspective(name);
		}
	}
	
	public String toString()
	{
		String pp = "PropertyPerception: ";
		pp += " persistent:" + this._persistent;
		pp += " ToM:" + this._ToM;
		pp += " event:" + this._event;
		pp += " property:" + this._property;
		pp += " value:" + this._value;
		
		return pp;
	}
}
