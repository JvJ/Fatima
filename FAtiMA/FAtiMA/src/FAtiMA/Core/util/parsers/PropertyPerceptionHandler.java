/** 
 * PropertyPerception.java - Parses a XML PropertyChangedPerception
 *  
 * Copyright (C) 2012 GAIPS/INESC-ID 
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
 * Created: 27/07/2012 
 * @author: João Dias
 * Email to: joao.dias@gaips.inesc-id.pt
 * 
 */

package FAtiMA.Core.util.parsers;

import org.xml.sax.Attributes;

import FAtiMA.Core.perceptions.PropertyPerception;
import FAtiMA.Core.sensorEffector.RemoteAction;
import FAtiMA.Core.util.enumerables.ActionEvent;
import FAtiMA.Core.wellFormedNames.Name;


/**
 * @author João Dias
 *
 */
public class PropertyPerceptionHandler extends ReflectXMLHandler {

	private RemoteAction _ra;
	private PropertyPerception _property;
	private String _entity;
	
	public PropertyPerceptionHandler () {
		_property = new PropertyPerception();
	}
	
	public PropertyPerception getPropertyPerception()
	{
		return _property;
	}
	
	public void Action(Attributes attributes)
	{
		_ra = new RemoteAction();
	}
	
	public void ActionEnd()
	{
		if(_ra!=null)
		{
			_property.setEvent(_ra.toEvent(ActionEvent.SUCCESS));
		}
		
	}
	public void TargetCharacters(String target) {
		_ra.setTarget(target);
	}
	
	public void SubjectCharacters(String sender) 
	{
		_ra.setSubject(sender);
	}
	
	public void TypeCharacters(String type) {
		_ra.setActionType(type);
	}
	
	public void ParamCharacters(String param)
	{
		_ra.AddParameter(param);
	}
	
	public void VisibilityCharacters(String visibility)
	{
		_property.setToM(visibility);
	}
	
	public void EntityCharacters(String entity)
	{
		_entity = entity;
	}
	
	public void NameCharacters(String name)
	{
		_property.setProperty(Name.ParseName(_entity + "(" + name + ")"));
	}
	
	public void ValueCharacters(String value)
	{
		_property.setValue(value);
	}
}