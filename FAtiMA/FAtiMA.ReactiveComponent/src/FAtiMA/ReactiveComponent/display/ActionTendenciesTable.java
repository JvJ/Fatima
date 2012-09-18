package FAtiMA.ReactiveComponent.display;

import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import FAtiMA.ReactiveComponent.Action;
import FAtiMA.ReactiveComponent.ActionTendencies;

public class ActionTendenciesTable extends JTable {
	
	private static final String [] COLUMNS = {"Action",
										   	  "Eliciting Emotion",
										   	  "Minimum Intensity",
											  "Cause Event"};
	
	private static final long serialVersionUID = 1L;
	private DefaultTableModel _model;

	public ActionTendenciesTable(DefaultTableModel model, ActionTendencies actionTendencies)
	{
		super(model);
		this._model = model;
		
		for(int i=0; i < COLUMNS.length; i++){
			this._model.addColumn(COLUMNS[i]);
		}

		this.setAutoCreateRowSorter(true);
		this.setCenterAlignment();
		
		for(Action action : actionTendencies.getActions()){
			this.addAction(action.getName().toString(),
						   action.GetElicitingEmotion().getType(),
						   action.GetElicitingEmotion().GetPotential(),
						   action.GetElicitingEmotion().GetCause().toTableString());
		}
	}
	
	private void setCenterAlignment() {
		DefaultTableCellRenderer dtcr = new DefaultTableCellRenderer();    
	    dtcr.setHorizontalAlignment(SwingConstants.CENTER);  
	    TableColumn col;
	    
		for(int i = 0; i < this.getColumnCount(); i++){
			col = this.getColumnModel().getColumn(i);
			col.setCellRenderer(dtcr);
		}
	}


	private void addAction(String actionName, String emotionType, float minIntensity, String causeEvent)
	{
		Object[] rowData = new Object[_model.getColumnCount()];

		rowData[0] = actionName;
		rowData[1] = emotionType;
		rowData[2] = minIntensity;		
		rowData[3] = causeEvent;		
		
		this._model.addRow(rowData);
	}
}

