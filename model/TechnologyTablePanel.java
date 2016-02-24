package model;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.event.CellEditorListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;

import model.AvoidCostPF;
import model.UtilityFunction;

public class TechnologyTablePanel extends TablePanel implements TableModelListener {
	MakeItButton button;
	GuiFrame gui;
	
	public TechnologyTablePanel(GuiFrame g) {
		super("Technology","Technology",g.activeEconomy,false);
		button=new MakeItButton();
		title.setLayout(new BorderLayout());
		title.add(BorderLayout.EAST, button);
		button.addActionListener(new MakeItListener());
		button.notReady();
		gui=g;
	}
	
	public void refreshTable() { // This will be called by the superconstructor as well as by the GUI
		boolean testing=false;
		if (testing) System.out.println("TESTING TechnologyTablePanel.refreshTable()");
		table = new JTable(new TechnologyTableModel());
		if (scrollPane!=null) remove(scrollPane);
		scrollPane=new JScrollPane(table);
		scrollPane.setVerticalScrollBar(new JScrollBar());
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		table.setFillsViewportHeight(true);
		table.getModel().addTableModelListener(this);
		add(scrollPane);
		setBorder(new LineBorder(Color.BLACK));
		scrollPane.validate();
		scrollPane.repaint();
		validate();
		update();
		repaint();
	}
	
	public void reset() {
		gui.activeEconomy.uFunction=null;
		gui.activeEconomy.pFunction=null;
		refreshTable();
		button.setEnabled(false);
	}
	
	public class MakeItButton extends JButton {
		public void finished() {
			setText("Made");
			setEnabled(false);
		}
		public void notReady() {
			setText("Not Ready");
			setEnabled(false);
		}
		public void ready() {
			if (gui.activeEconomy.economy!=null) {
				finished();
				return;
			}
			boolean ready=checkReady();
			if (ready==false) {
				notReady();
				return;
			}
			setText("Make It");
			setEnabled(true);
		}
	}
	
	public class MakeItListener implements ActionListener {
		
		public void actionPerformed(ActionEvent event) {
			gui.makeEconomy();
		}
	}
	
	public void tableChanged(TableModelEvent event) {
		boolean ready=checkReady();
		if (ready==true) button.ready();
	}
	
	public boolean checkReady() {
		boolean testing=false;
		if (testing) System.out.println("TESTING TechnologyTablePanel.checkReady().");
		boolean ready=true;
		if (invalidValue(0,2)) return false;
		for (int i=2; i<gui.activeEconomy.numGoods; i++) {
			if (testing) System.out.println("TESTING TechnologyTablePanel.checkReady(). row"+i);
			if (invalidValue(i,1)) return false;
			if (invalidValue(i,2)) return false;
		} 			
		return ready;
	}
	
	public boolean invalidValue(int row, int col) {
		boolean testing=false;
		if (testing) System.out.println("TESTING TechnologyTablePanel.invalidValue(). row="+row+", col="+col);
		try {
			String string=(String) table.getValueAt(row, col);
			if (string=="") return true; // true means it is invalid
			Double value=Double.valueOf(string);
			if (testing) System.out.println("TESTING TechnologyTablePanel.invalidValue(). value="+value);
			if ((value>=0)==false) return true;					
		} catch(Exception ex) {ex.printStackTrace();return false;}
		return false;
	}
	
	public void fireTableCellUpdated() {
		boolean testing=true;
		if (testing) System.out.println("TESTING TechnologyTablePane.fireTableCellUpdated()");
	}
	
	public class TechnologyTableModel extends AbstractTableModel {
        private String[] columnNames = {"Good ID",	// column 0 records the ID of the agent 
        		"L0",					// column 1 records the IDs of the retailers of this good the agent knows
        		"Alpha",							// column 2 records how much money the agent has
        };
	 
        public int getColumnCount() {
            return columnNames.length;
        }

        public int getRowCount() {
        	boolean testing=false;
        	if (testing) {
        		System.out.println("This is when TechnologyTablePanel.TechnologyTableModel.getRowCount() is being called. id="+((int) (100000*Math.random())));
        		System.out.println("g.numGoods="+gui.activeEconomy.numGoods);
        	}
        	if (gui.activeEconomy.numGoods!=null) return gui.activeEconomy.numGoods;
        	else return 0;
        }
 
        public String getColumnName(int col) {
            return columnNames[col];
        }
 
        public Object getValueAt(int row, int col) {
        	if (col==0) { // Good ID
        		if (row==0) return "0 (money)";
        		if (row==1) return "1 (capital)";
        		return row;
        	}
        	if ((gui.activeEconomy.pFunction==null)&(col==1)) {
        		if (row==0) return "N/A (sigma=#.##)";
        		if (row==1) return "N/A";
        		return "";
        	}
        	if ((gui.activeEconomy.uFunction==null)&(col==2)) {
        		if (row==1) return "N/A"; 
        		return "";
        	}
        	java.text.DecimalFormat format=new java.text.DecimalFormat("0.0000");
        	if (col==1) { // L0
        		if (row==0) {
        			if (gui.activeEconomy.uFunction!=null) return "N/A (sigma="+gui.activeEconomy.uFunction.sigma+")";
        			return "N/A (sigma=#.##)";
        		}
        		if (row==1) return "N/A";
        		Double l0=gui.activeEconomy.pFunction.getL0(row);
        		if (l0==null) return "";
        		return format.format(l0);
        	}
        	if (col==2) { // Alpha
        		if (row==1) return "N/A";
        		Double alpha=gui.activeEconomy.uFunction.coeffs[row];
        		if (alpha==null) return "";
        		return format.format(alpha);
        	}
        	return null; // Never will reach this line
        }

        public boolean isCellEditable(int row, int col) {
        	if (col==0) return false;
        	if ((col==1)&(row<2)) return false;
        	if ((col==2)&(row==1)) return false;
        	if (gui.activeEconomy.economy==null) return true;
        	return false;
         }
        
        public Class getColumnClass(int c) {
        	return String.class;
        }
        
        public void setValueAt(Object value, int row, int col) {
        	boolean testing=false;
        	if (testing) System.out.println("TESTING TechnologyTablePanel.TechnologyTableModel.setValueAt(). value="+value+", row="+row+", col="+col);
        	if (col==1) {
        		if (gui.activeEconomy.pFunction==null) gui.activeEconomy.pFunction=new AvoidCostPF(gui.activeEconomy.numGoods);
        		Double l0=null;
        		try {
            		l0=Double.valueOf((String) value);        			
        		} catch(Exception ex) {ex.printStackTrace();System.exit(1);} 
        		gui.activeEconomy.pFunction.setL0(row, l0);
        		if (testing) System.out.println(gui.activeEconomy.pFunction.toString());
        	}
        	if (col==2) {
        		if (gui.activeEconomy.uFunction==null) gui.activeEconomy.uFunction=new UtilityFunction(gui.activeEconomy.numGoods);
        		Double alpha=null;
        		try {
            		alpha=Double.valueOf((String) value);       			
        		} catch(Exception ex) {} 
        		gui.activeEconomy.uFunction.coeffs[row]=alpha;
        		if (testing) System.out.println(gui.activeEconomy.uFunction.toString());
        	}
        	validate();  
        	button.ready();
 //       	display.updateDisplays();
        	gui.repaint();
        }
	}

}
