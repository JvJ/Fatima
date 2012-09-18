package FAtiMA.SocialImportance.display;

import java.util.ArrayList;
import java.util.Hashtable;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import FAtiMA.Core.AgentCore;
import FAtiMA.Core.AgentModel;
import FAtiMA.Core.Display.AgentDisplayPanel;
import FAtiMA.Core.util.Constants;
import FAtiMA.SocialImportance.SocialImportanceRelation;

public class SIPanel extends AgentDisplayPanel {

	private static final long serialVersionUID = 1L;

	JPanel _SIPanel;

	protected Hashtable<String, SIDisplay> _SIDisplays;

	public SIPanel() {
		super();
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		_SIDisplays = new Hashtable<String, SIDisplay>();

		_SIPanel = new JPanel();
		_SIPanel.setLayout(new BoxLayout(_SIPanel,BoxLayout.Y_AXIS));

		JScrollPane relationsScroll = new JScrollPane(_SIPanel);
		relationsScroll.setBorder(BorderFactory.createTitledBorder("Social Importance"));

		this.add(relationsScroll);
	}
	
	public boolean Update(AgentModel am)
	{
		
		ArrayList<SocialImportanceRelation> relations = new ArrayList<SocialImportanceRelation>();
		relations.addAll(SocialImportanceRelation.getAllRelations(am.getMemory(),Constants.SELF_STRING));
		
		boolean updated = false;

		// in this case, there's a new relation added (it is not usual for
		// relations to disappear)
		// so we have to clear all relations and start displaying them all again
		if (_SIDisplays.size() != relations.size()) {
			_SIPanel.removeAll(); // removes all displayed emotions
											// from the panel
			_SIDisplays.clear();

			for(SocialImportanceRelation r : relations){
				SIDisplay display = new SIDisplay(am.getMemory(), r);
				_SIPanel.add(display.getPanel());
				_SIDisplays.put(r.getHashKey(), display);
		
			}
			updated = true;
		}

		for(SocialImportanceRelation r : relations){
			SIDisplay display = (SIDisplay) _SIDisplays.get(r.getHashKey());
			display.setValue(r.getValue(am.getMemory()));
		}

		return updated;
	}

	public boolean Update(AgentCore ag) 
	{
		return Update((AgentModel) ag);
	}
}
