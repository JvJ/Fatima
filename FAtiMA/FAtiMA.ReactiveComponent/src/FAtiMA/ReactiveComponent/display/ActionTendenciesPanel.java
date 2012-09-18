package FAtiMA.ReactiveComponent.display;

import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import FAtiMA.Core.AgentCore;
import FAtiMA.Core.AgentModel;
import FAtiMA.Core.Display.AgentDisplayPanel;
import FAtiMA.ReactiveComponent.ActionTendencies;
import FAtiMA.ReactiveComponent.Reaction;
import FAtiMA.ReactiveComponent.ReactiveComponent;

public class ActionTendenciesPanel extends AgentDisplayPanel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private JPanel _emotionalRulesPanel;
	private JPanel _actionsPanel;
	private int _numberOfAT, _numberOfER;
	private ReactiveComponent _reactiveComponent;

	public ActionTendenciesPanel(ReactiveComponent reactiveComponent)
	{
		super();
		
		_numberOfAT = 0;
		_numberOfER = 0;
		_reactiveComponent = reactiveComponent;

		this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
		

		_emotionalRulesPanel = new JPanel();
		_emotionalRulesPanel.setLayout(new BoxLayout(_emotionalRulesPanel,BoxLayout.Y_AXIS));			
		JScrollPane emotionalRulesScroll = new JScrollPane(_emotionalRulesPanel);
		emotionalRulesScroll.setBorder(BorderFactory.createTitledBorder("Emotional Reactions"));
		this.add(emotionalRulesScroll);
		
		_actionsPanel = new JPanel();
		_actionsPanel.setLayout(new BoxLayout(_actionsPanel,BoxLayout.Y_AXIS));			
		JScrollPane actionsScroll = new JScrollPane(_actionsPanel);
		actionsScroll.setBorder(BorderFactory.createTitledBorder("Action Tendencies"));
		this.add(actionsScroll);
	}

	@Override
	public boolean Update(AgentCore ag) {
		return Update((AgentModel) ag);
	}

	@Override
	public boolean Update(AgentModel am) {
		ArrayList<Reaction> reactions = _reactiveComponent.getEmotionalReactions().getAllReactions();
		if(reactions.size() != _numberOfER){
			_emotionalRulesPanel.removeAll();
			ReactionsTable reactionsTable = new ReactionsTable(new ReactionsTableModel(),reactions);
			JScrollPane scrollPane = new JScrollPane(reactionsTable);
			_emotionalRulesPanel.add(scrollPane);
			_numberOfER = reactions.size();
		}
		
		ActionTendencies at = _reactiveComponent.getActionTendencies();
		if(at.getActions().size() != _numberOfAT)
		{
			_actionsPanel.removeAll();
			ActionTendenciesTable actionTendenciesTable = new ActionTendenciesTable(new ActionTendenciesTableModel(),at);
			JScrollPane scrollPane = new JScrollPane(actionTendenciesTable);
			_actionsPanel.add(scrollPane);
			_numberOfAT = at.getActions().size();
			return true;
		}
		
		return false;
	}

}
