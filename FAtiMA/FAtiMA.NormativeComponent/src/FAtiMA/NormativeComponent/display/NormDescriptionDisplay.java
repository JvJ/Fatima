package FAtiMA.NormativeComponent.display;

import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

import FAtiMA.Core.AgentModel;
import FAtiMA.Core.Display.ConditionsPanel;
import FAtiMA.NormativeComponent.Norm;
import FAtiMA.NormativeComponent.NormativeComponent;
import FAtiMA.NormativeComponent.Obligation;
import FAtiMA.NormativeComponent.Prohibition;

public class NormDescriptionDisplay {
	
	private JPanel _panel;
	
	public NormDescriptionDisplay(NormativeComponent normativeComponent, AgentModel model, Norm norm){
		_panel = new JPanel();
		if(norm instanceof Obligation){
			_panel.setBorder(BorderFactory.createTitledBorder("Obligation - " + norm.getName().toString()));
		}
		else if(norm instanceof Prohibition){
			_panel.setBorder(BorderFactory.createTitledBorder("Prohibition - " + norm.getName().toString()));
		}
		
        _panel.setLayout(new BoxLayout(_panel,BoxLayout.Y_AXIS));
        _panel.setMinimumSize(new Dimension(500,300));
        init(normativeComponent, model, norm);
	}
	
	public JPanel getNormsPanel(){
		return _panel;
	}
	
	private void init(NormativeComponent normativeComponent, AgentModel model, Norm norm){
		
		ConditionsPanel activationConditionsPanel = new ConditionsPanel("Activation Conditions", norm.getActivationConditions());
		_panel.add(activationConditionsPanel);
		
		ConditionsPanel expirationConditionsPanel = new ConditionsPanel("Expiration Conditions", norm.getExpirationConditions());
		_panel.add(expirationConditionsPanel);
		
		ConditionsPanel normativeConditionsPanel = new ConditionsPanel("Normative Conditions", norm.getNormativeConditions());
		_panel.add(normativeConditionsPanel);
	}
}
