package FAtiMA.ReactiveComponent.display;

import java.util.ArrayList;

import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import FAtiMA.ReactiveComponent.Action;
import FAtiMA.ReactiveComponent.Reaction;

public class ReactionsTable extends JTable {
	
	private static final String [] COLUMNS = {"Event Class",
										   	  "Desirability",
										   	  "Desirability For Other",
											  "Praiseworthiness"};
	
	private static final long serialVersionUID = 1L;
	private DefaultTableModel _model;

	public ReactionsTable(DefaultTableModel model, ArrayList<Reaction> reactions)
	{
		super(model);
		this._model = model;
		
		for(int i=0; i < COLUMNS.length; i++){
			this._model.addColumn(COLUMNS[i]);
		}

		this.setAutoCreateRowSorter(true);
		this.setCenterAlignment();
		
		for(Reaction r : reactions){
			this.addReaction(r.getEvent().toTableString(),
							r.getDesirability(),
							r.getDesirabilityForOther(),
							r.getPraiseworthiness());
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


	private void addReaction(String event, Float desirability, Float desirabilityForOther, Float praiseworthiness)
	{
		Object[] rowData = new Object[_model.getColumnCount()];

		rowData[0] = event;
		rowData[1] = desirability;
		rowData[2] = desirabilityForOther;		
		rowData[3] = praiseworthiness;		
		
		this._model.addRow(rowData);
	}
}

