package FAtiMA.ReactiveComponent.display;

import javax.swing.table.DefaultTableModel;

public class ActionTendenciesTableModel extends DefaultTableModel {

		private static final long serialVersionUID = 1L;

		@Override
		public Class getColumnClass(int column) {
			if(column == 2){
				return Float.class;
			}else{
				return String.class;
			}
		}
		
		@Override
		public boolean isCellEditable(int row, int column) {
			return false;
		}
}
