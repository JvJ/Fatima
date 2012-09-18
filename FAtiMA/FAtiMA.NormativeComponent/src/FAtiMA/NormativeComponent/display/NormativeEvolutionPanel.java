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

public class NormativeEvolutionPanel extends AgentDisplayPanel{
	
	private static final long serialVersionUID = 1L;

	private static boolean firstUpdate = true;
	
	private NormativeComponent _normativeComponent;
	private ArrayList<NormEvolutionDisplay> _normDisplays;
	private JPanel _instancesFollowedBySelf;
	private JPanel _instancesFollowedByOthers;
	private JPanel _instancesFulfilledBySelf;
	private JPanel _instancesFulfilledByOthers;
	private JPanel _instancesViolatedBySelf;
	private JPanel _instancesViolatedByOthers;
	
	public NormativeEvolutionPanel(NormativeComponent normativeComponent){
		super();
		_normativeComponent = normativeComponent;
		_normDisplays = new ArrayList<NormEvolutionDisplay>();
		this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
				
		_instancesFollowedBySelf = new JPanel();
		_instancesFollowedBySelf.setBorder(BorderFactory.createTitledBorder("Instances Followed by SELF"));
		_instancesFollowedBySelf.setLayout(new BoxLayout(_instancesFollowedBySelf, BoxLayout.Y_AXIS));
		JScrollPane followedBySelfNormsScroll = new JScrollPane(_instancesFollowedBySelf);
		this.add(followedBySelfNormsScroll);
		
		_instancesFollowedByOthers = new JPanel();
		_instancesFollowedByOthers.setBorder(BorderFactory.createTitledBorder("Instances Followed by Others"));
		_instancesFollowedByOthers.setLayout(new BoxLayout(_instancesFollowedByOthers, BoxLayout.Y_AXIS));
		JScrollPane followedByOthersNormsScroll = new JScrollPane(_instancesFollowedByOthers);
		this.add(followedByOthersNormsScroll);
			
		_instancesFulfilledBySelf = new JPanel();
		_instancesFulfilledBySelf.setBorder(BorderFactory.createTitledBorder("Instances Fulfilled by SELF"));
		_instancesFulfilledBySelf.setLayout(new BoxLayout(_instancesFulfilledBySelf, BoxLayout.Y_AXIS));
		JScrollPane fulfilledBySelfNormsScroll = new JScrollPane(_instancesFulfilledBySelf);
		this.add(fulfilledBySelfNormsScroll);
		
		_instancesFulfilledByOthers = new JPanel();
		_instancesFulfilledByOthers.setBorder(BorderFactory.createTitledBorder("Instances Fulfilled by Others"));
		_instancesFulfilledByOthers.setLayout(new BoxLayout(_instancesFulfilledByOthers, BoxLayout.Y_AXIS));
		JScrollPane fulfilledByOthersNormsScroll = new JScrollPane(_instancesFulfilledByOthers);
		this.add(fulfilledByOthersNormsScroll);
		
		_instancesViolatedBySelf = new JPanel();
		_instancesViolatedBySelf.setBorder(BorderFactory.createTitledBorder("Instances Violated by SELF"));
		_instancesViolatedBySelf.setLayout(new BoxLayout(_instancesViolatedBySelf, BoxLayout.Y_AXIS));
		JScrollPane violatedBySelfNormsScroll = new JScrollPane(_instancesViolatedBySelf);
		this.add(violatedBySelfNormsScroll);
		
		_instancesViolatedByOthers = new JPanel();
		_instancesViolatedByOthers.setBorder(BorderFactory.createTitledBorder("Instances Violated by Others"));
		_instancesViolatedByOthers.setLayout(new BoxLayout(_instancesViolatedByOthers, BoxLayout.Y_AXIS));
		JScrollPane violatedByOthersNormsScroll = new JScrollPane(_instancesViolatedByOthers);
		this.add(violatedByOthersNormsScroll);
	}
	
	public boolean mustUpdate(AgentCore ag){
		if(firstUpdate == true){
			firstUpdate = false;
			return true;
		}
		else{
			NormativeComponent nc = (NormativeComponent)ag.getComponent(NormativeComponent.NAME);
			return nc.areNormsChanged();
		}
	}
	
	
	public boolean Update(AgentModel am){
		return false;
	}
	
	public boolean Update(AgentCore ag){
		NormEvolutionDisplay nDisplay;
		boolean update = mustUpdate(ag);
		if(update){
			_instancesFollowedBySelf.removeAll();
			_instancesFollowedByOthers.removeAll();
			_instancesFulfilledBySelf.removeAll();
			_instancesFulfilledByOthers.removeAll();
			_instancesViolatedBySelf.removeAll();
			_instancesViolatedByOthers.removeAll();
			_normDisplays.clear();
			
			for(Norm norm : _normativeComponent.getNormativeEnvironment().getInstancesFollowedBySelf()){
				nDisplay = new NormEvolutionDisplay(_normativeComponent, ag, norm);
				_instancesFollowedBySelf.add(nDisplay.getNormLabel());
				_normDisplays.add(nDisplay);
			}
			
			for(Norm norm : _normativeComponent.getNormativeEnvironment().getInstancesFollowedByOthers()){
				nDisplay = new NormEvolutionDisplay(_normativeComponent, ag, norm);
				_instancesFollowedByOthers.add(nDisplay.getNormLabel());
				_normDisplays.add(nDisplay);
			}
						
			for(Norm norm : _normativeComponent.getNormativeEnvironment().getInstancesFulfilledBySelf()){
				nDisplay = new NormEvolutionDisplay(_normativeComponent, ag, norm);
				_instancesFulfilledBySelf.add(nDisplay.getNormLabel());
				_normDisplays.add(nDisplay);
			}
			
			for(Norm norm : _normativeComponent.getNormativeEnvironment().getInstancesFulfilledByOthers()){
				nDisplay = new NormEvolutionDisplay(_normativeComponent, ag, norm);
				_instancesFulfilledByOthers.add(nDisplay.getNormLabel());
				_normDisplays.add(nDisplay);
			}
			
			for(Norm norm : _normativeComponent.getNormativeEnvironment().getInstancesViolatedBySelf()){
				nDisplay = new NormEvolutionDisplay(_normativeComponent, ag, norm);
				_instancesViolatedBySelf.add(nDisplay.getNormLabel());
				_normDisplays.add(nDisplay);
			}
			
			for(Norm norm : _normativeComponent.getNormativeEnvironment().getInstancesViolatedByOthers()){
				nDisplay = new NormEvolutionDisplay(_normativeComponent, ag, norm);
				_instancesViolatedByOthers.add(nDisplay.getNormLabel());
				_normDisplays.add(nDisplay);
			}
		}
		return update;
	}

}
