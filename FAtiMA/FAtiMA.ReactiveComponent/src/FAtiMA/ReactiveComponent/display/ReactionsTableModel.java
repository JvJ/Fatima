package FAtiMA.ReactiveComponent.display;

import javax.swing.table.DefaultTableModel;

public class ReactionsTableModel extends DefaultTableModel {

		private static final long serialVersionUID = 1L;

		@Override
		public Class getColumnClass(int column) {
			if(column == 0){
				return String.class;
			}else{
				return Float.class;
			}
		}
		
		@Override
		public boolean isCellEditable(int row, int column) {
			return false;
		}
}
