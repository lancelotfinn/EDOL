package model;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.LineBorder;
import javax.swing.table.AbstractTableModel;


public class ParametersTablePanel extends TablePanel implements ActionListener {
	public JButton button;
//	public Parameters parameters;
	GuiFrame gui;
	
	public ParametersTablePanel(GuiFrame g) {
		super("Other parameters", "Other parameters", g.activeEconomy, false);
		button=new JButton("Reset Parameters");
		button.addActionListener(this);
		title.setLayout(new BorderLayout());
		title.add(BorderLayout.EAST, button);
		gui=g;
		reset();
	}
	
	public void actionPerformed(ActionEvent event) {
		reset();
	}
	
	public void reset() {
		gui.activeEconomy.parameters=Parameters.defaultValues();
		refreshTable();
		validate();
	}
	
	public void refreshTable() {
		table = new JTable(new ParametersTableModel());
		if (scrollPane!=null) remove(scrollPane);
		scrollPane=new JScrollPane(table);
		scrollPane.setVerticalScrollBar(new JScrollBar());
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		table.setFillsViewportHeight(true);
		add(scrollPane);
		setBorder(new LineBorder(Color.BLACK));
		update();
		scrollPane.validate();
		validate();
		repaint();
	}
	
	public class ParametersTableModel extends AbstractTableModel {
        private String[] columnNames = {"Good ID",	// column 0 records the ID of the agent 
        		"L0",					// column 1 records the IDs of the retailers of this good the agent knows
        		"Alpha",							// column 2 records how much money the agent has
        };
    	Integer[] blankRows={0,2,3,11,12,18,19,22,23};
	 
        public int getColumnCount() {
            return columnNames.length;
        }
        
        public int getRowCount() {
        	return 61;
        }
 
        public String getColumnName(int col) {
            return columnNames[col];
        }
        
        public int effectiveRow(int row) {
        	int effectiveRow=row-1; // The reason for these modifications is because the parameter category headings break up the sequence
        	if (row>3) effectiveRow=effectiveRow-2; // -3 altogether for the agent behavior parameters
        	if (row>11) effectiveRow=effectiveRow-2; // -3 altogether for the agent behavior parameters
        	if (row>18) effectiveRow=effectiveRow-2; // -5 altogether for the money parameters
        	if (row>22) effectiveRow=effectiveRow-2; // -7 altogether for the retailer behavior parameters
        	return effectiveRow;
        }
 
        public Object getValueAt(int row, int col) {
        	boolean testing=false;
        	if (testing) System.out.println("ParametersTableModel.getValueAt() fired. parameters=null: "+(gui.activeEconomy.parameters==null));
        	if (col==0) {
        		if (row==0) return "*** SUBSTITUTABILITY ***";
        		if (row==2) return "";
        		if (row==3) return "*** CAPITAL DYNAMICS ***";
        		if (row==11) return "";
        		if (row==12) return "*** AGENT BEHAVIOR ***";
        		if (row==18) return "";
        		if (row==19) return "*** MONEY ***";
        		if (row==22) return "";
        		if (row==23) return "*** RETAILER BEHAVIOR ***";
        		return Parameter.values()[effectiveRow(row)];
        	}
        	if (col==2) {
        		for (Integer i: blankRows) {
        			if (row==i) return "---";
        		}
        		return "default: "+Parameters.defaultValues().allValues[effectiveRow(row)];
        	}
        	if (col==1) {
        		for (Integer i: blankRows) {
        			if (row==i) return "---";
        		}
        	}
        	if (gui.activeEconomy.parameters==null) return "";
        	if (col==1) return gui.activeEconomy.parameters.allValues[effectiveRow(row)];
         	return null; // Never will reach this line
        }

        public boolean isCellEditable(int row, int col) {
        	if (col==0) return false;
        	if (col==2) return false;
        	for (Integer i: blankRows) {
        		if (row==i) return false;
        	}
        	if (gui.activeEconomy.economy==null) return true;
        	return false;        	
         }
        
        public Class getColumnClass(int c) {
        	return String.class;
        }
        
        public void setValueAt(Object value, int row, int col) {
        	// Col must be 1 because the others are not editable
        	if (gui.activeEconomy.parameters==null) gui.activeEconomy.parameters=Parameters.defaultValues();
        	gui.activeEconomy.parameters.allValues[effectiveRow(row)]=value;
        	gui.activeEconomy.parameters.transferVectorValuesToNamedVars();
        	if (row==1) {
        		Double sigma=null;
        		try {
        			sigma=(Double) value;
        		} catch(Exception ex) {}
        		if (sigma!=null) gui.activeEconomy.uFunction.sigma=sigma;
        	}
        }
	}

}
