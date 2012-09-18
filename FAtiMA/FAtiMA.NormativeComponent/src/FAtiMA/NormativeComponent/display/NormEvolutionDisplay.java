package FAtiMA.NormativeComponent.display;

import javax.swing.JLabel;

import FAtiMA.Core.AgentModel;
import FAtiMA.NormativeComponent.Norm;
import FAtiMA.NormativeComponent.NormativeComponent;

public class NormEvolutionDisplay {
	
	private JLabel _normLabel;
	
	public NormEvolutionDisplay(NormativeComponent normativeComponent, AgentModel model, Norm norm){
		_normLabel = new JLabel(norm.toString());
	}
	
	public JLabel getNormLabel(){
		return _normLabel;
	}
}
