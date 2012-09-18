/** 
 * KnowledgeSlot.java - Class used to store information in the KnowledgeBase. This class builds up 
 * the KnowledgeBase by having KnowledgeSlot's stored in KnowledgeSlots which can be inside other
 * KnowledgeSlots.
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
 * Created: 30/12/2004 
 * @author: Jo�o Dias
 * Email to: joao.assis@tagus.ist.utl.pt
 * 
 * History: 
 * Jo�o Dias: 30/12/2004 - File created
 * Jo�o Dias: 05/04/2006 - added the CountElements Method
 * Jo�o Dias: 10/07/2006 - the class is now serializable
 * Jo�o Dias: 22/05/2006 - Added comments to each public method's header 
 */

package FAtiMA.Core.memory.semanticMemory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;


/**
 * Class used to store knowledge in the KnowledgeBase. A KnowledgeSlot can store
 * an object, but also any number of children KnowledgeSlots. This hierarchical
 * composition of KnowledgeSlots builds up the KnowledgeBase and allows fast indexing
 * and search of properties.
 * 
 * @see KnowledgeBase
 * 
 * @author Jo�o Dias
 */

public class KnowledgeSlot implements Serializable {
    
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private HashMap<String,Object> _children;
    private String _name;
    private String _displayName;
    private Object _value;
    private boolean _persistent;
    
    /**
     * Creates an empty KnowledgeSpot, identified by the received name
     * @param name - the name that identifies the KnowledgeSlot
     */
    public KnowledgeSlot(String name, String displayName) {
    	_displayName = displayName;
        _name = name;
        _children = new HashMap<String,Object>();
        _persistent = false;
    }
    
    /**
     * Creates an empty KnowledgeSpot, identified by the received name
     * @param name - the name that identifies the KnowledgeSlot
     */
    public KnowledgeSlot(Boolean persistent, String name, String displayName) {
    	_displayName = displayName;
        _name = name;
        _children = new HashMap<String,Object>();
        if(persistent != null)
        {
        	_persistent = persistent;
        }
        else
        {
        	_persistent = false;
        }
    }
	
    /**
     * Clears all the information stored in the 
     * KnowledgeSlot (including all its children)
     */
	public void clear() {
	    _value = null;
	    _children.clear();
	    _persistent = false;
	}
	
	
	/**
	 * Determines if this KnowledgeSlot has a child with a specific key 
	 * @param key - the key of the child to search
	 * @return true if the KnowledgeSlot contains a child with the received key
	 */
	public boolean containsKey(String key) {
	    return _children.containsKey(key);
	}
	
	/**
	 * Gets the child KnowledgeSlot identified by the given key
	 * @param key - the key of the child to get
	 * @return the child KnowledgeSlot
	 */
	public KnowledgeSlot get(String key) {
	    return (KnowledgeSlot) _children.get(key);
	}

	
	public Set<String> getKeys()
	{
		return _children.keySet();
	}
	
	/**
	 * Gets an Iterator that iterates over the set of existing child Keys
	 * @return the key Iterator
	 */
	public Iterator<String> getKeyIterator() {
	    return _children.keySet().iterator();
	}
	
	/**
	 * Gets the KnowledgeSlot identifier
	 * @return the KnowledgeSlot ID
	 */
	public String getName() {
	    return _name;
	}
	
	/**
	 * Gets the KnowledgeSlot display Name, usefull for displaying the KS in the ShortTermMemory Panel for isntance 
	 * @return the KnowledgeSlot display Name
	 */
	public String getDisplayName() {
		return _displayName;
	}
	
	public ArrayList<KnowledgeSlot> getLeafs()
	{		
		ArrayList<KnowledgeSlot> leafSlots = new ArrayList<KnowledgeSlot>();
		
		if(this._value != null)
		{
			//if the slot has a value, it means that it contains information and should be return as a leaf
			leafSlots.add(this);
		}
		
		//recursive function
		for(Object ks : this._children.values())
		{
			leafSlots.addAll(((KnowledgeSlot)ks).getLeafs());
		}
		
		return leafSlots;
	}
    
    /**
     * Gets the object stored in the KnowledgeSlot
	 * @return the object stored in the KS
	 */
	public Object getValue() {
		return _value;
	}
	
	/**
	 * checks whether the KnowledgeFact is persistent (i.e. it will be saved and reused after migration) or not
	 */
	public boolean getPersistent()
	{
		return _persistent;
	}
	
	/**
	 * Sets a KS's persistent value (i.e. it will be saved and reused on migration)
	 * @param stat - whether this KS is persistent or not
	 */
	public void setPersistent(Boolean persistent)
	{
		if(persistent != null)
		{
			_persistent = persistent;
		}
	}
	
	/**
	 * Counts the number of valid elements (ones that have a value stored in them)
	 * stored within this KnowledgeSlot (may include children, grandchildren,
	 * grandgrandchildren, etc).
	 * @return the number of elements stored inside the KnowledgeSlot
	 */
    public int CountElements()
    {
    	KnowledgeSlot ks;
        int number = 0;
        if(this._value != null) number++;
        
        Iterator<Object> it = this._children.values().iterator();
        
        while(it.hasNext()) {
        	ks = (KnowledgeSlot) it.next();
        	number = number + ks.CountElements();
        }
        
        return number;
    }
	
    /**
     * Adds a KS child to the KnowledgeSlot
     * @param key - the Key of the KnowledgeSlot to ADD
     * @param kSlot - the KnowledgeSlot to ADD
     */
	public void put(String key, KnowledgeSlot kSlot) {
	    _children.put(key,kSlot);
	}
	
	/**
	 * Removes a child from the KnowledgeSlot
	 * @param key - the key of the child to remove
	 */
	public void remove(String key) {
	    _children.remove(key);
	}

	/**
	 * Sets the object stored in the KnowledgeSlot
	 * @param object - the new object to store in the KnowledgeSlot
	 */
	public void setValue(Object object) {
		_value = object;
	}
	
	/**
	 * Converts the KnowledgeSlot to a String
	 * @return the converted String
	 */
	public String toString() {
	    Iterator<Object> it;
	    String aux;
	    if(_value == null) {
	        aux = _name + ":";
	    }
	    else {
	        aux = _name + ":" + _value;
	    }
	 
	    it = _children.values().iterator();
	    if(it.hasNext()) {
	        
	        aux = aux + " {" + it.next();
	        while(it.hasNext()) {
		        aux = aux + "," + it.next();
		    }
	        aux = aux + "}";
	    }
	    return aux;
	}
	

	/** remove all non persistent properties */
	public void removeNonPersistent() {
		Iterator<String> it = _children.keySet().iterator();
		while (it.hasNext())
		{
			String key = it.next();
			Object value = _children.get(key);
			if(value instanceof KnowledgeSlot) 
			{
				KnowledgeSlot ks = (KnowledgeSlot) value;
				ks.removeNonPersistent();								
				if(!ks.getPersistent() && ks._children.isEmpty())
					it.remove();
			}
		}
		
	}
	
	
	public String toXML() {
		
		String property = "<Property persistent=\"";
		property += this._persistent + "\" ";
		property += "name=\"" + this._displayName + "\" ";
		property += "value=\"" + this._value + "\"/>";
		
	    return property;
	}
		
}
