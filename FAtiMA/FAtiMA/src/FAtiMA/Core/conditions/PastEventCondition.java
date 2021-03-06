/**
 * PastEventCondition.java - 
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
 * Created: 31/Ago/2006
 * @author: Jo�o Dias
 * Email to: joao.assis@tagus.ist.utl.pt
 * 
 * History: 
 * Jo�o Dias: 31/Ago/2006 - File created
 * Jo�o Dias: 24/03/2008 - Restructure, changed the way EventConditions Hierarchy. Now, PastEventConditions
 * 						   is the super class, and RecentEventConditions is the child class
 */

package FAtiMA.Core.conditions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.StringTokenizer;

import org.xml.sax.Attributes;

import FAtiMA.Core.AgentModel;
import FAtiMA.Core.memory.episodicMemory.ActionDetail;
import FAtiMA.Core.memory.episodicMemory.AutobiographicalMemory;
import FAtiMA.Core.memory.episodicMemory.SearchKey;
import FAtiMA.Core.sensorEffector.Event;
import FAtiMA.Core.util.Constants;
import FAtiMA.Core.util.enumerables.ActionEvent;
import FAtiMA.Core.util.enumerables.EventType;
import FAtiMA.Core.util.enumerables.GoalEvent;
import FAtiMA.Core.wellFormedNames.Name;
import FAtiMA.Core.wellFormedNames.Substitution;
import FAtiMA.Core.wellFormedNames.SubstitutionSet;
import FAtiMA.Core.wellFormedNames.Symbol;


/**
 * @author User
 *
 */
public class PastEventCondition extends PredicateCondition {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	protected Symbol _subject;
	protected Symbol _action;
	protected Symbol _target;
	protected ArrayList<Symbol> _parameters;
	protected String _emotion;
	
	//Meiyii - 12/01/10
	protected short _type = -1;
	protected short _status = -1;
	
	
	/**
	 * Parses a RecentEventCondition given a XML attribute list
	 * @param attributes - A list of XMl attributes
	 * @return - the EventCondition Parsed
	 */
	public static PastEventCondition ParseEvent(Attributes attributes) {
		boolean occurred;
		Symbol subject = null;
		Symbol action;
		Symbol target = null;
		String emotion = null;
		String ToM;
		PastEventCondition event;
		ArrayList<Symbol> parameters = new ArrayList<Symbol>();
		
		String aux;
		aux = attributes.getValue("occurred");
		if(aux != null)
		{
			occurred = Boolean.parseBoolean(aux);
		}
		else occurred = true;
		
		aux = attributes.getValue("subject");
		if(aux != null)
		{
			subject = new Symbol(aux);
		}
		
		action = new Symbol(attributes.getValue("action"));
		
		aux = attributes.getValue("target");
		if(aux != null)
		{
			target = new Symbol(aux);
		}
		
		aux = attributes.getValue("parameters");
		
		if(aux != null) {
			StringTokenizer st = new StringTokenizer(aux, ",");
			while(st.hasMoreTokens()) {
				parameters.add(new Symbol(st.nextToken()));
			}
		}
		
		emotion = attributes.getValue("emotion");
		
			
		event = new PastEventCondition(occurred,EventType.ACTION,ActionEvent.SUCCESS,subject,action,target,emotion,parameters);
		
		aux = attributes.getValue(Constants.ToM_STRING);
		if(aux == null)
		{
			ToM = Constants.UNIVERSAL_SYMBOL.toString();
		}
		else
		{
			ToM = aux;
		}
		
		event.setToM(ToM);
		return event;
	}

	protected PastEventCondition()
	{
	}
	
	public PastEventCondition(boolean occurred, Event e)
	{
		super(occurred,null,Constants.SELF_STRING);
		
		this._subject = new Symbol(e.GetSubject());
		this._action = new Symbol(e.GetAction());
		this._target = new Symbol(e.GetTarget());
		this._parameters = new ArrayList<Symbol>(e.GetParameters().size());
		
		//Meiyii - 12/01/10
		this._type = e.GetType();
		this._status = e.GetStatus();
		this._emotion = null;
		
		updateName();
	}
	
	// Meiyii - 12/01/10 added type and status
	public PastEventCondition(boolean occurred, short type, short status, Name event)
	{
		super(occurred, event,Constants.SELF_STRING);
		
		ListIterator<Symbol> li = event.getLiteralList().listIterator();
		li.next();
		this._subject = (Symbol) li.next();
		this._action = (Symbol) li.next();
		if(li.hasNext())
		{
			this._target = (Symbol) li.next();
		}
		this._parameters = new ArrayList<Symbol>();
		while(li.hasNext())
		{
			this._parameters.add(li.next());
		}		
		
		this._type = type;
		this._status = status;
		this._emotion = null;
	}
	
	public PastEventCondition(boolean occurred, short type, short status, Symbol subject, Symbol action, Symbol target, String emotion, ArrayList<Symbol> parameters)
	{
		super(occurred,null);
		this._type = type;
		this._status = status;
		this._subject = subject;
		this._action = action;
		this._target = target;
		this._emotion = emotion;
		this._parameters = parameters;
		
		updateName();
	}
	
	public PastEventCondition(PastEventCondition pEC){
		super(pEC);
		
		_type = pEC._type;
		_status = pEC._status;
		_action = (Symbol) pEC._action.clone();
		_emotion = pEC._emotion;
		
		if(pEC._subject != null)
		{
			_subject = (Symbol) pEC._subject.clone();
		}
		
		if(pEC._target != null)
		{
			_target = (Symbol) pEC._target.clone();
		}
		_parameters = new ArrayList<Symbol>(pEC._parameters.size());
		
		for(Symbol p : pEC._parameters){
			_parameters.add((Symbol) p.clone());
		}
		
	}
	
	@Override
	public void applyPerspective(String name)
	{
		super.applyPerspective(name);
		
		this._subject.applyPerspective(name);
		this._target.applyPerspective(name);
		
		for(Symbol s : this._parameters)
		{
			s.applyPerspective(name);
		}
	}
	
	/**
	 * Checks if the EventCondition is verified in the agent's AutobiographicalMemory
	 * @return true if the PastPredicate is verified, false otherwise
	 * @see AutobiographicalMemory
	 */
	@Override
	public float checkConditionUsingPerspective(AgentModel perspective) {
		if(!(getName().isGrounded())) return 0;
		
		if(getPositive() == perspective.getMemory().getEpisodicMemory().ContainsPastEvent(getSearchKeys()))
		{
			return 1;
		}
		else
		{
			return 0;
		}
	}
	
	public Object clone() {
		return new PastEventCondition(this);
	}
	
	protected ArrayList<ActionDetail> getPossibleBindings(AgentModel am)
	{
		return am.getMemory().getEpisodicMemory().SearchForPastEvents(getSearchKeys());
	}
	
	protected ArrayList<SearchKey> getSearchKeys()
	{
		Symbol param;
		
		ArrayList<SearchKey> keys = new ArrayList<SearchKey>();
		if(this._subject != null && this._subject.isGrounded())
		{
			keys.add(new SearchKey(SearchKey.SUBJECT,this._subject.toString()));
		}
		
		//Meiyii 19/01/10
		if(this._action.isGrounded())
		{
			if(this._type == EventType.GOAL)
			{
				keys.add(new SearchKey(SearchKey.INTENTION,this._action.toString()));
			}
			else
			{
				keys.add(new SearchKey(SearchKey.ACTION,this._action.toString()));
			}
		}
		if(this._status >= 0)
		{
			if(this._type == EventType.GOAL)
			{
				keys.add(new SearchKey(SearchKey.STATUS, GoalEvent.GetName(this._status)));
			}
			else
			{
				keys.add(new SearchKey(SearchKey.STATUS, ActionEvent.GetName(this._status)));
			}
		}
	
		if(this._target != null && this._target.isGrounded())
		{
			keys.add(new SearchKey(SearchKey.TARGET, this._target.toString()));
		}
		
		if(this._emotion != null)
		{
			keys.add(new SearchKey(SearchKey.EMOTION,this._emotion));
		}
		
		
		if(this._parameters.size() > 0)
		{
			ArrayList<String> params = new ArrayList<String>();
			for(ListIterator<Symbol> li = this._parameters.listIterator();li.hasNext();)
			{
				param = (Symbol) li.next();
				if(param.isGrounded())
				{
					params.add(param.toString());
				}
				else
				{
					params.add("*");
				}
			}
			keys.add(new SearchKey(SearchKey.PARAMETERS, params));
		}		
		
		return keys;
	}

	
	/**
	 * This method finds all the possible sets of Substitutions that applied to the condition
     * will make it valid (true) according to the agent's AutobiographicalMemory 
     * @return A list with all SubstitutionsSets that make the condition valid
	 * @see AutobiographicalMemory
	 */
	@Override
	public ArrayList<SubstitutionSet> getValidSubstitutionsUsingPerspective(AgentModel perspective) {
		ActionDetail detail;
		Substitution sub;
		SubstitutionSet subSet;
		Symbol param;
		String subject;
		String target;
		String parameter;
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
		
		details = getPossibleBindings(perspective);
		
		//we cannot determine bindings for negative event conditions,
		//assume false
		if(!getPositive())
		{
			if(details.size() == 0) 
			{
				bindingSets.add(new SubstitutionSet());
				return bindingSets;
			}
			else return null;
		}
		
		if(details.size() == 0) return null;
		
		Iterator<ActionDetail> it = details.iterator();
		while(it.hasNext())
		{
			detail = (ActionDetail) it.next();
			subSet = new SubstitutionSet();
			
			if(this._subject != null && !this._subject.isGrounded())
			{
				subject = detail.getSubject();
				sub = new Substitution(this._subject,new Symbol(subject));
				subSet.AddSubstitution(sub);
			}
			// Meiyii 19/01/10
			if(this._type == EventType.GOAL)
			{
				if(!this._action.isGrounded())
				{
					sub = new Substitution(this._action,new Symbol(detail.getIntention()));
					subSet.AddSubstitution(sub);
				}
			}
			else 
			{
				if(!this._action.isGrounded())
				{
					sub = new Substitution(this._action,new Symbol(detail.getAction()));
					subSet.AddSubstitution(sub);
				}
			}
			if(this._target != null && !this._target.isGrounded())
			{
				target = detail.getTarget();
				sub = new Substitution(this._target,new Symbol(target));
				subSet.AddSubstitution(sub);
			}
			
			for(int i=0; i < this._parameters.size(); i++)
			{
				param = (Symbol) this._parameters.get(i);
				if(!param.isGrounded())
				{
					parameter = detail.getParameters().get(i).toString();
					sub = new Substitution(param, new Symbol(parameter));
					subSet.AddSubstitution(sub);
				}
			}
			bindingSets.add(subSet);
		}
		return bindingSets;
	}

	public boolean isGrounded()
	{
		if(!super.isGrounded())
		{
			return false;
		}
		
		if(this._subject != null)
		{
			if(!this._subject.isGrounded()) return false;
		}
			
		if(!this._action.isGrounded()) return false;
		
		if(this._target != null)
		{
			if(!this._target.isGrounded()) return false; 
		}
		
		for(Symbol s : this._parameters)
		{
			if(!s.isGrounded())
			{
				return false;
			}
		}
		
		return true; 
	}
	
	public void makeGround(ArrayList<Substitution> bindings) {
		super.makeGround(bindings);
	
		if(this._subject != null)
		{
			this._subject.makeGround(bindings);
		}
		this._action.makeGround(bindings);
		if(this._target != null)
		{
			this._target.makeGround(bindings);
		}
		
		ListIterator<Symbol> li = this._parameters.listIterator();
		while(li.hasNext())
		{
			li.next().makeGround(bindings);
		}
	}
	
	public void makeGround(Substitution subst) {
		super.makeGround(subst);
		
		if(this._subject != null)
		{
			this._subject.makeGround(subst);
		}
		
		this._action.makeGround(subst);
		if(this._target != null)
		{
			this._target.makeGround(subst);
		}
		
		ListIterator<Symbol> li = this._parameters.listIterator();
		while(li.hasNext())
		{
			li.next().makeGround(subst);
		}
	}
	
	public void removePerspective(String name)
	{
		super.removePerspective(name);
		
		this._subject.removePerspective(name);
		this._target.removePerspective(name);
		
		for(Symbol s : this._parameters)
		{
			s.removePerspective(name);
		}
	}
	
	public void replaceUnboundVariables(int variableID) {
		super.replaceUnboundVariables(variableID);
		
		if(this._subject != null)
		{
			this._subject.replaceUnboundVariables(variableID);
		}
		
		this._action.replaceUnboundVariables(variableID);
		if(this._target != null)
		{
			this._target.replaceUnboundVariables(variableID);
		}
		
		ListIterator<Symbol> li = this._parameters.listIterator();
		while(li.hasNext())
		{
			li.next().replaceUnboundVariables(variableID);
		}
	}
	
	protected void updateName()
	{
		String aux = this._subject + "," + this._action;
		if(this._target != null)
		{
			aux = aux + "," + this._target;
		}
		
		ListIterator<Symbol> li = this._parameters.listIterator();
		while(li.hasNext())
		{
			aux = aux + "," + li.next();
		}
		
		this.setName(Name.ParseName("EVENT(" + aux + ")"));
	}

}
