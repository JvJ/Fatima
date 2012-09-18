package FAtiMA.NormativeComponent.display;

import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import FAtiMA.Core.AgentCore;
import FAtiMA.Core.AgentModel;
import FAtiMA.Core.Display.AgentDisplayPanel;
import FAtiMA.NormativeComponent.Norm;
import FAtiMA.NormativeComponent.NormativeComponent;

public class NormativeDescriptionPanel extends AgentDisplayPanel{
	
	private static final long serialVersionUID = 1L;
	
	private static ArrayList<Norm> _lastActiveNorms = new ArrayList<Norm>();
	
	private NormativeComponent _normativeComponent;
	private ArrayList<NormDescriptionDisplay> _normDisplays;
	private JPanel _norms;
	
	public NormativeDescriptionPanel(NormativeComponent normativeComponent){
		super();
		_normativeComponent = normativeComponent;
		_normDisplays = new ArrayList<NormDescriptionDisplay>();
		this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
		_norms = new JPanel();
		_norms.setBorder(BorderFactory.createTitledBorder("Norms"));
		_norms.setLayout(new BoxLayout(_norms, BoxLayout.Y_AXIS));
		JScrollPane normsScroll = new JScrollPane(_norms);
		this.add(normsScroll);
	}
	
	public boolean mustUpdate(AgentCore ag){
		boolean result = _lastActiveNorms.equals(_normativeComponent.getNormativeEnvironment().getInstancesCurrentlyActive());
		_lastActiveNorms = _normativeComponent.getNormativeEnvironment().getInstancesCurrentlyActive();
		return result;
	}
	
	
	public boolean Update(AgentModel am){
		return false;
	}
	
	public boolean Update(AgentCore ag){
		NormDescriptionDisplay nDisplay;
		boolean update = mustUpdate(ag);
		if(update){
			_norms.removeAll();
			_normDisplays.clear();
			for(Norm norm : _normativeComponent.getNormativeEnvironment().getInstancesCurrentlyActive()){
				nDisplay = new NormDescriptionDisplay(_normativeComponent, ag, norm);
				_norms.add(nDisplay.getNormsPanel());
				_normDisplays.add(nDisplay);
			}
		}
		return update;
	}

}
