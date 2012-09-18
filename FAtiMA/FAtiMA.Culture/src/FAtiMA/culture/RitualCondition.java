/**
 * RitualCondition.java - 
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
 * Created: 15/04/2008
 * @author: João Dias
 * Email to: joao.dias@gaips.inesc-id.pt
 * 
 * History: 
 * João Dias: 15/04/2008 - File created
  */

package FAtiMA.culture;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.StringTokenizer;

import org.xml.sax.Attributes;

import FAtiMA.Core.AgentModel;
import FAtiMA.Core.conditions.PredicateCondition;
import FAtiMA.Core.goals.Goal;
import FAtiMA.Core.memory.episodicMemory.ActionDetail;
import FAtiMA.Core.memory.episodicMemory.AutobiographicalMemory;
import FAtiMA.Core.memory.episodicMemory.SearchKey;
import FAtiMA.Core.util.PermutationGenerator;
import FAtiMA.Core.wellFormedNames.Name;
import FAtiMA.Core.wellFormedNames.Substitution;
import FAtiMA.Core.wellFormedNames.SubstitutionSet;
import FAtiMA.Core.wellFormedNames.Symbol;


/**
 * @author João Dias
 *
 */

public class RitualCondition extends PredicateCondition {
	

	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	protected ArrayList<Symbol> _roles;
	
	protected Symbol _ritualName;
	boolean _repeat;
	public static final RitualCondition ParseRitualCondition(Attributes attributes)
	{
		ArrayList<Symbol> roles = new ArrayList<Symbol>();
    	String aux;
    	Symbol ritualName = null;
    	boolean occurred = true; //default
    	boolean repeat = false; // default    	
    	
    	aux = attributes.getValue("name");
    	if(aux != null)
    	{
    		ritualName = new Symbol(aux);
    	}
    	
    	if(attributes.getValue("repeat") != null){
			Boolean.parseBoolean(attributes.getValue("repeat"));
    	}

    	if(attributes.getValue("occurred") != null){
    		occurred = Boolean.parseBoolean(attributes.getValue("occurred"));
    	}
    	
 		aux = attributes.getValue("roles");
    	
    	
    	if(aux != null) {
			StringTokenizer st = new StringTokenizer(aux, ",");
			while(st.hasMoreTokens()) {
				Symbol role = new Symbol(st.nextToken());
				roles.add(role);
			}
		}
		
		return new RitualCondition(ritualName,roles,occurred,repeat);
	} 
	

	public RitualCondition(RitualCondition rC){
		super(rC);
		
		_ritualName = (Symbol) rC._ritualName.clone();
		_roles = new ArrayList<Symbol>();	
		for(Symbol role : rC._roles){
			_roles.add((Symbol)role.clone());
		}
		_repeat = rC._repeat;	
	}
	
	@SuppressWarnings("unchecked")
	public RitualCondition(Symbol ritualName, ArrayList<Symbol> roles, boolean occurred, boolean repeat)
	{
		super(occurred,null);
		this._ritualName = ritualName;
		this._roles = (ArrayList<Symbol>) roles.clone();
		
		String name = ritualName + "(";
		
		for(int i=0; i < _roles.size(); i++)
		{
			name+= _roles.get(i).toString();
			name+=",";
		}
		
		if(_roles.size() > 0)
		{
			name = name.substring(0,name.length()-1);
		}
		
		name+= ")";
		
		this.setName(Name.ParseName(name));
	}

	
	@SuppressWarnings("unchecked")
	public RitualCondition(Symbol ritualName, ArrayList<Symbol> roles, boolean occurred, boolean repeat, String ToM)
	{
		super(occurred,null,ToM);
		this._ritualName = ritualName;
		this._roles = (ArrayList<Symbol>) roles.clone();
		
		String name = ritualName + "(";
		
		for(int i=0; i < _roles.size(); i++)
		{
			name+= _roles.get(i).toString();
			name+=",";
		}
		
		if(_roles.size() > 0)
		{
			name = name.substring(0,name.length()-1);
		}
		
		name+= ")";
		
		this.setName(Name.ParseName(name));
	}
	
	/**
	 * Checks if the RitualCondition is verified in the agent's AutobiographicalMemory
	 * @return true if the RitualCondition is verified, false otherwise
	 * @see AutobiographicalMemory
	 */
	public float checkConditionUsingPerspective(AgentModel perspective) {
		boolean result = false;
		
		if(!getName().isGrounded()) return 0;
		
		
		PermutationGenerator pGenerator = new PermutationGenerator(_roles.size());
//		ArrayList<SearchKey> searchKeys = getSearchKeys();
//		for (int i = 0; i < _roles.size(); i++) {
//			searchKeys.add(new SearchKey(SearchKey.CONTAINSPARAMETER,_roles.get(i).toString()));				
//		}
		
		while(pGenerator.hasMore()){
			int [] indices = pGenerator.getNext();
		
			ArrayList<SearchKey> searchKeys = getSearchKeys();
			for (int i = 0; i < indices.length; i++) {
				searchKeys.add(new SearchKey(SearchKey.CONTAINSPARAMETER,_roles.get(indices[i]).toString()));				
			}
			
			result = perspective.getMemory().getEpisodicMemory().ContainsRecentEvent(searchKeys);
				
			if(result ==  getPositive()){
				if(result == getPositive())
				{
					return 1;
				}
				else return 0;
			}
		}	
		
		
		if(result == getPositive())
		{
			return 1;
		}
		else return 0;
	}

	public Object clone() {
		return new RitualCondition(this);
	}

	public Object GenerateName(int id) {
		RitualCondition rc = (RitualCondition) this.clone();
		rc.replaceUnboundVariables(id);
		return rc;
	}

	private ArrayList<SearchKey> getSearchKeys()
	{
		ArrayList<SearchKey> keys = new ArrayList<SearchKey>();
		
		keys.add(new SearchKey(SearchKey.STATUS,Goal.SUCCESSEVENT));
		
		keys.add(new SearchKey(SearchKey.INTENTION,this._ritualName.toString()));
		
		
		if(this._repeat){
			keys.add(new SearchKey(SearchKey.MAXELAPSEDTIME, new Long(10000)));
		}
		
		return keys;
	}

	public ArrayList<SubstitutionSet> getValidSubstitutionsUsingPerspective(AgentModel perspective) {
		ActionDetail detail;
		Substitution sub;
		SubstitutionSet subSet;
		ArrayList<SubstitutionSet> bindingSets = new ArrayList<SubstitutionSet>();
		ArrayList<ActionDetail> details;
		
		if (getName().isGrounded()) {
			if(this.checkConditionUsingPerspective(perspective)==1)
			{
				bindingSets.add(new SubstitutionSet());
				return bindingSets;
			}
			else return null;
		}
		
		details = perspective.getMemory().getEpisodicMemory().SearchForRecentEvents(getSearchKeys());
		
		if(details.size() == 0) return null;
		
		Iterator<ActionDetail> it = details.iterator();
		while(it.hasNext())
		{
			detail = (ActionDetail) it.next();
			subSet = new SubstitutionSet();
	
			PermutationGenerator pGenerator = new PermutationGenerator(_roles.size());
			
			//we are assuming that all roles are not grounded
			while(pGenerator.hasMore()){
				int [] indices = pGenerator.getNext();
				subSet = new SubstitutionSet();
				for (int i = 0; i < indices.length; i++) {
					sub = new Substitution(_roles.get(i),new Symbol(detail.getParameters().get(indices[i]).GetValue().toString()));
					subSet.AddSubstitution(sub);
				}
				bindingSets.add(subSet);
			}
		}
	
		return bindingSets;
		
	}

	public Object Ground(ArrayList<Substitution> bindingConstraints) {
		
		RitualCondition rc = (RitualCondition) this.clone();
		rc.makeGround(bindingConstraints);
		return rc;
	}

	public Object Ground(Substitution subst) {
		RitualCondition rc = (RitualCondition) this.clone();
		rc.makeGround(subst);
		return rc;
	}
	
	public void makeGround(ArrayList<Substitution> bindings) {
		this.getName().makeGround(bindings);
		this._ritualName.makeGround(bindings);
				
		ListIterator<Symbol> li = this._roles.listIterator();
		while(li.hasNext())
		{
			li.next().makeGround(bindings);
		}
	}
	
	public void makeGround(Substitution subst) {
		this.getName().makeGround(subst);
		this._ritualName.makeGround(subst);
		
		ListIterator<Symbol> li = this._roles.listIterator();
		while(li.hasNext())
		{
			li.next().makeGround(subst);
		}
	}
	
	public void replaceUnboundVariables(int variableID) {
		this.getName().replaceUnboundVariables(variableID);
		this._ritualName.replaceUnboundVariables(variableID);
		
		ListIterator<Symbol> li = this._roles.listIterator();
		while(li.hasNext())
		{
			li.next().replaceUnboundVariables(variableID);
		}
	}
}