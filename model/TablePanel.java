package model;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.util.ArrayList;

import javax.swing.table.*;


public class TablePanel extends JPanel {
	JTable table;
	JScrollPane scrollPane;
	JTextField title;
	JTextField input;
	String tableType;
	public EconomyDisplayPanels display;
	public Agent agent; // The source of data may be an agent...
	public Retailer retailer; // ... or a retailer
	public Integer industry;
	
	public TablePanel(String tableTitle, String type, EconomyDisplayPanels d, boolean inputField) {
		tableType=type;
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		title=new JTextField(tableTitle);
		title.setEditable(false);
		title.setFont(new Font("Palatino",Font.BOLD,14));
		add(title);
		if (inputField) {
			input=new JTextField(8);
			title.setLayout(new BorderLayout());
			title.add(BorderLayout.EAST, input);
			input.addActionListener(new InputListener(input));
		}
		display=d;
		refreshTable();
	}
	
	public void refreshTable() {
		boolean testing=false;
		if (tableType=="Agent ID") table = new JTable(new AgentIDTableModel());
		if (tableType=="Agent retailer list") table = new JTable(new AgentRetailerListTableModel());
		if (tableType=="Retailer 2c") table = new JTable(new Retailer2cTableModel());
		if (tableType=="Retailer 2d") table = new JTable(new Retailer2dTableModel());
		if (tableType=="Census: Agents") {
			table = new JTable(new AgentCensusTableModel());
			if (testing) System.out.println("TESTING TablePanel.refreshTable() for a Census: Agents pane");
		}
		if (tableType=="Census: Retailers") table = new JTable(new RetailerCensusTableModel());
		if (tableType=="Industry: historical profile") table = new JTable(new IndustryHistoricalProfileTableModel());
		if (tableType=="Industry: retailers") {
			table = new JTable(new IndustryRetailersTableModel());
		}
		if (tableType=="Industry: population") {
			table = new JTable(new IndustryPopulationTableModel());
		}
		if (table==null) table = stupidJTable();
		if (scrollPane!=null) remove(scrollPane);
		scrollPane=new JScrollPane(table);
		scrollPane.setVerticalScrollBar(new JScrollBar());
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		table.setFillsViewportHeight(true);
		add(scrollPane);
		setBorder(new LineBorder(Color.BLACK));
		update();
		scrollPane.validate();
	}
	
	public JTable stupidJTable() {
		Object[][] data = { // Pseudo data for now
			    {"Kathy", "Smith",
			     "Snowboarding", new Integer(5), new Boolean(false)},
			    {"John", "Doe",
			     "Rowing", new Integer(3), new Boolean(true)},
			    {"Sue", "Black",
			     "Knitting", new Integer(2), new Boolean(false)},
			    {"Jane", "White",
			     "Speed reading", new Integer(20), new Boolean(true)},
			    {"Joe", "Brown",
			     "Pool", new Integer(10), new Boolean(false)}
			};
		String[] columnNames = {"First Name",
                "Last Name",
                "Sport",
                "# of Years",
                "Vegetarian"};
		JTable ret = new JTable(data, columnNames);
		return ret;
	}
	
	public void update() {
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		if (display.economy==null) return; // Do nothing if there is no economy, hence no data to report.
		if (tableType=="Agent ID") {
			if (agent!=null) update(agent);
		}
		if (tableType=="Retailer 2c") {
			boolean changeRetailer=retailer==null;
			if (retailer!=null) {
				if (retailer.defunct) changeRetailer=true;
			}
			if (changeRetailer&(display.economy.traders.size()>0)) {
				int selector=(int) (Math.random()*display.economy.traders.size());
				retailer=(Retailer) display.economy.traders.get(selector);
				display.retailerDetail_chart2a.retailer=retailer;
				display.retailerDetail_chart2b.retailer=retailer;
				display.retailerDetail_table2d.retailer=retailer;
			}
			if (retailer!=null) update(retailer);
		}
		if (tableType=="Retailer 2d") {
			if (retailer!=null) update(retailer);
		}
		if (tableType=="Census: Retailer") {
			input.setText(""+display.economy.traders.size());
		}
	}
	
	public void update(int i) {
		industry=i;
		if (tableType=="Industry: historical profile") {
			display.industryDetail_table2c.update(industry);
			display.industryDetail_table2d.update(industry);
			input.setText(""+industry);
			double l0=display.economy.productionFunction.getL0(industry);
			double alpha=display.economy.utilityFunction.coeffs[industry];
			DecimalFormat format=new DecimalFormat("0.000");
			display.industryDetail_table2c.input.setColumns(10);
			display.industryDetail_table2c.input.setText("L0="+format.format(l0)+". a="+format.format(alpha));
			display.industryDetail_chart2a.industry=industry;
		}
	}
	
	public void update(Agent a) {
		agent=a;
		if (tableType=="Agent ID") {
			String text=""+agent.id;
			if (agent.enterprises.size()>0) {
				Retailer r=(Retailer) agent.enterprises.get(0); 
				text=text+" (R"+r.id+")";
			}
			input.setText(text);
			display.agentDetail_chart2a.agent=agent;
			display.agentDetail_chart2a.update();
			display.agentDetail_chart2b.agent=agent;
			display.agentDetail_chart2b.update();
			display.agentDetail_table2d.update(agent);
			display.agentDetail_chart2a.revalidate();
			display.agentDetail_chart2b.revalidate();
			display.agentDetail.revalidate();
			display.agentDetail_chart2a.repaint();
			display.agentDetail_chart2b.repaint();
			display.agentDetail.repaint();
			if (display.active) {
				display.gui.validate();
				display.gui.repaint();
			}
		}
		this.repaint();
	}
	
	public void update(Retailer r) {
		if (retailer==null) return;
		retailer=r;
		if (tableType=="Retailer 2c") {
			title.setText("Retailer ID "+retailer.id+", in Industry "+r.r_product+", History");
			String text=""+retailer.id+" (O="+r.owner.id+")";
			input.setText(text);
			display.retailerDetail_chart2a.retailer=retailer;
			display.retailerDetail_chart2a.update();
			display.retailerDetail_chart2b.retailer=retailer;
			display.retailerDetail_chart2b.update();
			display.retailerDetail_chart2a.revalidate();
			display.retailerDetail_chart2b.revalidate();			
			display.retailerDetail_table2d.update(retailer);
			display.retailerDetail.validate();
			display.retailerDetail.repaint();
		}
		if (tableType == "Retailer 2d") {
			table = new JTable(new Retailer2dTableModel());
			title.setText("Retailer ID "+retailer.id+", in Industry "+r.r_product+", Inventory Dynamics");
			input.setEditable(false);
			if (retailer.lastTurnTransactions==null) return;
			input.setText(""+retailer.lastTurnTransactions.size());
		}
		this.repaint();
	}
	
	public class InputListener implements ActionListener {
		JTextField source;
		
		public InputListener(JTextField s) {
			source=s;
		}
		
		public void actionPerformed(ActionEvent event) {
			source.transferFocus();
			boolean testing=false;
			if (testing) System.out.println("TESTING TablePanel.InputListener.actionPerformed()");
			if (tableType=="Agent ID") {
				Integer id=Integer.valueOf(source.getText());
				EconomyDisplayPanels d=display;
				Economy e=d.economy;
				Y: for (Agent a: e.population) {
					if (a.id==id) {
						agent=a;
						break Y;
					}
				}
				update(agent);
			}
			Y: if (tableType=="Retailer 2c") {
				Integer id=Integer.valueOf(source.getText());
				EconomyDisplayPanels d=display;
				Economy e=d.economy;
				for (Trader t: e.traders) {
					Retailer r=(Retailer) t;
					if (r.id==id) {
						retailer=r;
						update(retailer);
						break Y;
					}
				}
				int selector=(int) (Math.random()*display.economy.traders.size());
				Retailer r=(Retailer) display.economy.traders.get(selector);
				update(r); // Pick one randomly if the one requested does not exist
			}
			if (tableType=="Industry: historical profile") {
				Integer i=Integer.valueOf(source.getText());
				if ((i>=2)&(display.economy.numGoods>i)) {
					industry=i;
				}
				update(industry);
			}
		}
	}
	
	public class AgentIDTableModel extends AbstractTableModel {
        private String[] columnNames = {"Good",	// column 0 records the ID of the good
        		"Able",							// column 1 records whether the agent is able to make the good or not
        		"Alpha",						// column 2 records how much the agent likes this good
        		"L0",							// column 3 keeps track of overhead labor
        		"Labor", 						// column 4 records how much labor the agent devoted to making each good
        		"Consumption",					// column 5 records how much the agent consumed of the good
        		"Bought", 						// column 6 records how much the agent spent buying the good
        		"Paid",							// column 7 records how much the agent paid for the good
        		"Sold",							// column 8 records how much the agent sold of the good
        		"Earned"						// column 9 records how much the agent earned by selling the good
        };
	 
        public int getColumnCount() {
            return columnNames.length;
        }
 
        public int getRowCount() {
            return display.economy.numGoods; // The number of rows in this table should be the same as the number of goods in the economy (including money and capital)
        }
 
        public String getColumnName(int col) {
            return columnNames[col];
        }
 
        public Object getValueAt(int row, int col) {
        	java.text.DecimalFormat format=new java.text.DecimalFormat("0.00000");
        	if (col==0) { // All the values in this column are strings: it is simply the number of the good
        		if (row==0) return "0 (money)";
        		if (row==1) return "1 (capital)";
        		else return ""+row;
        	}
        	if (col==1) { // Here the answers are boolean: it is whether the agent can make the good or not
        		if (row<2) return false;
        		else {
        			boolean canProduce=false;
        			for (Integer i: agent.productionUniverse) {
        				if (row==i) canProduce=true;
        			}
        			return canProduce;
        		}
        	}
           	if (col==2) { // Alpha
        		if (row>2) return format.format(display.economy.utilityFunction.coeffs[row]);
        		return "N/A";
        	}
        	if (col==3) { // L0
        		if (row<2) return "N/A";
        		double l0=display.economy.productionFunction.getL0(row);
        		return format.format(l0);
        	}
        	if (col==4) { // Here the answer is a String: it is how much labor the agent devoted to making the good
        		LaborReport report=agent.labor;
        		Double[] vector=report.labor;
        		double labor=vector[row];
        		if (labor==0) return "";
        		return format.format(labor);
        	}
        	if (col==5) { // Another String: how much the agent consumed of the good
        		if (row==0) return "("+agent.has(0)+")";
        		if (row==1) return "("+agent.has(1)+")";
        		double cons=agent.cons.used[row];
        		if (cons==0) return "";
        		return format.format(cons);
        	}
        	if (col==6) { // String: how much the bought of each good
        		double bought=agent.bought[row];
        		if (bought==0) return "";
        		return format.format(bought);
        	}
        	if (col==7) { // String: how much the agent paid for the good
        		if (row==0) return "UTILITY:";
        		double paid=agent.paid[row];
        		if (paid==0) return "";
        		return format.format(paid);
        	}
        	if (col==8) { // String: how much the agent sold of the good
        		if (row==0) return agent.cons.utility;
        		double sold=agent.sold[row];
        		if (sold==0) return "";
        		return format.format(sold);
        	}
        	if (col==9) { // String: how much the agent earned from selling the good
        		double earned=agent.earned[row];
        		if (earned==0) return "";
        		return format.format(earned);
        	}
        	return null; // Never will reach this line
        }
 
        public Class getColumnClass(int c) {
            if (c==1) return Boolean.class;
            return String.class;
        }
 
         public boolean isCellEditable(int row, int col) {
        	 return false;
         }
	}
	
	public class AgentRetailerListTableModel extends AbstractTableModel {
        private String[] columnNames = {"ID",	// column 0 records the ID of the retailer
        		"Product",						// column 1 records the product the retailer is buying/selling
        		"Alpha",						// column 2 records the alpha for this product
        		"Sell@", 						// column 3 records the retailers sell price
        		"Inventory",					// column 4 records the retailer's inventory of its product
        		"Buy@", 						// column 5 records the price the retailer will pay suppliers
        		"Cash",							// column 6 records how much cash the retailer has on hand to pay suppliers
        		"Owner",						// column 7 records the ID of the retailer's owner
        		"Mode",							// column 8: 1=normal business conditions, 2=inventory problems, 3=cash flow problems, 4=just starting
        		"Income",							// column 9: number of stockouts in the last round
        		"Supply"							// column 10: number of cashouts in the last round
        };
	 
        public int getColumnCount() {
            return columnNames.length;
        }
 
        public int getRowCount() {
        	return agent.knows.size();
        }
 
        public String getColumnName(int col) {
            return columnNames[col];
        }
 
        public Object getValueAt(int row, int col) {
        	java.text.DecimalFormat format=new java.text.DecimalFormat("0.00000");
    		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    		input.setText(""+agent.knows.size());
    		if (agent.knows.size()<=row) return "";
    		Trader t=agent.knows.get(row);
    		Retailer r=(Retailer) t;
        	if (col==0) { // Retailer ID
        		return r.id;
        	}
        	if (col==1) { // Product
        		return r.r_product;
        	}
        	if (col==2) { // Alpha
        		return format.format(display.economy.utilityFunction.coeffs[r.r_product]);
        	}
        	if (col==3) { // Sell price
        		return format.format(r.salesManager.getPrice());
        	}
        	if (col==4) { // Inventory
        		return format.format(r.inventory(r.r_product));
        	}
        	if (col==5) { // Buy price
        		return format.format((1/r.purchasingManager.getPrice()));
        	}
        	if (col==6) { // Cash
        		return format.format(r.inventory(r.r_money));
        	}
        	if (col==7) { // Owner
        		return r.owner.id;
        	}
        	if (col==8) {
        		return r.option;
        	}
        	if (col==9) {
        		int last=r.incomeHistory.size()-1;
        		if (last>0) return format.format(r.incomeHistory.get(last));
        		return "";
        	}
        	if (col==10) {
        		int last=r.supplyHistory.size()-1;
        		if (last>-1) return format.format(r.supplyHistory.get(last));
        		return "";
        	}
        	return null; // Never will reach this line
        }
 
         public boolean isCellEditable(int row, int col) {
        	 return false;
         }
	}

	public class Retailer2cTableModel extends AbstractTableModel {
        private String[] columnNames = {"Turn",	// column 0 records the number of the turn in question
        		"BaseP",						// column 1 records the base price set by the retailer during the turn
        		"SellP",						// column 2 records the sell price set by the retailer during the turn
        		"BuyP",							// column 3 records the buy price set by the retailer during the turn
        		"Income", 						// column 4 records the income the retailer earned during the turn
        		"Supply",						// column 5 records the supply the retailer was able to acquire during the turn
        		"Cash", 						// column 6 records how much cash the retailer had left at the end of the turn
        		"Inventory",					// column 7 records how much inventory the retailer had left at the end of the turn
        		"Stockouts",					// column 8 records how many times the agent ran out of stock
        		"Cashouts",						// column 9 records how many times the agent ran ouf cash to pay willing suppliers
        		"Mode",							// column 10 records the "mode" (1=normal business conditions etc.) the retailer is operating in
        		"p(exit)"						// column 11 records the odds of exiting
        };
	 
        public int getColumnCount() {
            return columnNames.length;
        }
 
        public int getRowCount() {
        	if (retailer==null) return 0;
            return retailer.inventoryHistory.size(); // The number of rows in this table should be the same as the number of goods in the economy (including money and capital)
        }
 
        public String getColumnName(int col) {
            return columnNames[col];
        }
 
        public Object getValueAt(int row, int col) {
        	if (retailer==null) return "";
        	java.text.DecimalFormat format=new java.text.DecimalFormat("0.00000");
    		int turn=retailer.inventoryHistory.size()-1-row;
    		if (turn<0) return "";
        	if (col==0) { // The turn in the retailer's history
        		return turn;
        	}
        	if (col==1) { // Base price
        		return format.format(retailer.basePriceHistory.get(turn));
        	}
           	if (col==2) { // Sell price
        		return format.format(retailer.salesPriceHistory.get(turn));
        	}
        	if (col==3) { // Buy price
        		return format.format(retailer.supplyPriceHistory.get(turn));
        	}
        	if (col==4) { // Income
        		return format.format(retailer.incomeHistory.get(turn));
        	}
        	if (col==5) { // Supply
        		return format.format(retailer.supplyHistory.get(turn));
        	}
        	if (col==6) { // Cash
        		return format.format(retailer.cashHistory.get(turn));
        	}
        	if (col==7) { // Inventory
        		return format.format(retailer.inventoryHistory.get(turn));
        	}
        	if (col==8) { // Stockouts
        		return retailer.stockoutsHistory.get(turn);
        	}
        	if (col==9) { // Cashouts
        		return retailer.cashoutsHistory.get(turn);
        	}
        	if (col==10) { // Mode
        		return retailer.modeHistory.get(turn);
        	}
        	if (col==11) { // Odds of exit
        		return retailer.oddsOfExitHistory.get(turn);
        	}
        	return null; // Never will reach this line
        }
 
          public boolean isCellEditable(int row, int col) {
        	 return false;
         }
	}
	
	public class Retailer2dTableModel extends AbstractTableModel {
        private String[] columnNames = {"Agent ID",	// column 0 records the ID of the agent who initiated the transaction
        		"Paid",								// column 1 records how much the agent paid (in money) for buy transactions
        		"Bought",							// column 2 records how much the agent bought of the good
        		"Earned",							// column 3 records how much (money) the agent earned in supply transactions
        		"Sold", 							// column 4 records how much of the good the agent sold, in supply transactions
        		"Price",							// column 5 records the price (money/good) on the transaction
        		"Left: Cash", 						// column 6 records how much cash the agent had left after the transaction
        		"Inventory",						// column 7 records how much inventory the agent had left after the transaction
        		"Turn"
        };
	 
        public int getColumnCount() {
            return columnNames.length;
        }

        public int getRowCount() {
            if (retailer==null) return 0;
            if (retailer.lastTurnTransactions==null) return 0;
           	return retailer.lastTurnTransactions.size(); // The number of rows in this table should be the same as the number of goods in the economy (including money and capital)
        }
 
        public String getColumnName(int col) {
            return columnNames[col];
        }
 
        public Object getValueAt(int row, int col) {
        	java.text.DecimalFormat format=new java.text.DecimalFormat("0.00000");
        	Transaction t=retailer.lastTurnTransactions.get(retailer.lastTurnTransactions.size()-1-row);
        	int money=display.economy.money;
        	if (col==0) { // The ID of the agent who initiated the transaction
        		return t.buyerID;
        	}
        	if (col==1) { // Paid
        		if (t.pay==money) return format.format(t.payQty);
        	}
           	if (col==2) { // Bought
        		if (t.pay==money) return format.format(t.sellQty);
        	}
        	if (col==3) { // Earned
        		if (t.sell==money) return format.format(t.sellQty);
        	}
        	if (col==4) { // Sold
        		if (t.sell==money) return format.format(t.payQty);
        	}
        	if (col==5) { // Price
        		double price=t.payQty/t.sellQty; // if this was a "sell" transaction, with money as the pay good
        		if (t.sell==money) price=t.sellQty/t.payQty; // recalculate price if this was a "supply" transaction
        		return format.format(price);
        	}
        	if (col==6) { // Left: Cash
        		if (t.pay==money) return format.format(t.payQtyLeft); // consumer transactions here
        		return format.format(t.sellQtyLeft); // supply transactions here
        	}
        	if (col==7) { // Inventory
        		if (t.sell==money) return format.format(t.payQtyLeft); // consumer transactions here
        		return format.format(t.sellQtyLeft); // supply transactions here
        	}
        	if (col==8) { // Turn
        		return t.turn;
        	}
        	return null; // Never will reach this line
        }
 

        public boolean isCellEditable(int row, int col) {
        	 return false;
         }
	}

	public class AgentCensusTableModel extends AbstractTableModel {
        private String[] columnNames = {"Agent ID",	// column 0 records the ID of the agent 
        		"Money",							// column 1 records how much money the agent has
        		"Capital",							// column 2 records how much capital the agent has
        		"Utility",							// column 3 records the agent's utility last turn
        		"Earned", 							// column 4 records how much the agent earned last turn
        		"Spent",							// column 5 records how much the agent spent last turn
        		"Retailer", 						// column 6 records whether the agent runs a retailer or not
        		"Skills",							// column 7 records which skills the agent had
        		"Jobs",								// column 8 records the jobs (market labor) the agent held
        		"Activities",						// column 9 records the types of work the agent engaged in
        		"Consumed",							// column 10 records how many different goods the agent consumed
        		"Arbitrage",						// column 11 records whether the agent engaged in arbitrage
        		"p(entry/exit)"							// column 12 records how likely the agent was to enter
        };
	 
        public int getColumnCount() {
            return columnNames.length;
        }

        public int getRowCount() {
           	return display.economy.population.size(); // The number of rows in this table should be the same as the number of goods in the economy (including money and capital)
        }
 
        public String getColumnName(int col) {
            return columnNames[col];
        }
 
        public Object getValueAt(int row, int col) {
        	java.text.DecimalFormat format=new java.text.DecimalFormat("0.0000");
        	Agent agent=display.economy.sortedPopulation.get(row);
        	int money=display.economy.money;
        	int capital=display.economy.capital;
        	if (col==0) { // The ID of the agent
        		return agent.id;
        	}
        	if (col==1) { // Money
        		return format.format(agent.has(money));
        	}
           	if (col==2) { // Capital
        		return format.format(agent.has(capital));
        	}
        	if (col==3) { // Utility
        		return format.format(agent.cons.utility);
        	}
        	if (col==4) { // Earned
        		double earned=0;
        		for (int i=0; i<agent.earned.length; i++) {
        			earned=earned+agent.earned[i];
        		}
        		return format.format(earned);
        	}
        	if (col==5) { // Spent
        		double spent=0;
        		for (int i=0; i<agent.paid.length; i++) {
        			spent=spent+agent.paid[i];
        		}
        		return format.format(spent);
        	}
        	if (col==6) { // Retailer
        		if (agent.enterprises.size()>0) return ((Retailer)agent.enterprises.get(0)).id;
        		return "";
        	}
        	if (col==7) { // Skills
        		String ret="";
        		for (Integer i: agent.productionUniverse){
        			ret=ret+i+",";
        		}
        		return ret;
        	}
        	if (col==8) { // Jobs
        		String ret="";
        		for (int i=0; i<agent.sold.length; i++) {
        			if (agent.sold[i]>0) ret=ret+i+",";
        		}
        		return ret;
        	}
        	if (col==9) { // Types of work
        		String ret="";
        		for (int i=0; i<agent.labor.labor.length; i++) {
        			if (agent.labor.labor[i]>0) ret=ret+i+",";
        		}
        		return ret;
        	}
        	if (col==10) {
        		int goodsConsumed=0;
        		for (int i=0; i<agent.cons.used.length; i++) {
        			if (agent.cons.used[i]>0) goodsConsumed++;
        		}
        		return goodsConsumed;
        	}
        	if (col==11) {
        		return agent.arbitrage_this_turn;
        	}
        	if (col==12) {
        		if (agent.enterprises.size()==0) return agent.oddsOfEntry_retail;
        		else return ((Retailer)agent.enterprises.get(0)).oddsOfExit;
        	}
        	return null; // Never will reach this line
        }
 
        public Class getColumnClass(int c) {
            if (c==11) return Boolean.class;
            return String.class;
        }

        public boolean isCellEditable(int row, int col) {
        	 return false;
         }
	}

	public class RetailerCensusTableModel extends AbstractTableModel {
        private String[] columnNames = {"ID",		// column 0 records the ID of the retailer 
        		"Owner",							// column 1 records the ID of the owner
        		"Product",							// column 2 records the product the retailer deals in
        		"Money",							// column 3 records how much money the retailer has
        		"Inventory", 						// column 4 records how much the retailer has in inventory
        		"Income",							// column 5 records how much the retailer received in cash income
        		"Supply", 							// column 6 records how much in supplies the retailer procured
        		"Base price",						// column 7 records the retailer's base price
        		"Sell@",							// column 8 records the retailer's sell price
        		"Buy@",								// column 9 records the retailer's buy (supply) price
        		"Markup",							// column 10 records the retailer's markup
        		"Dividend",							// column 11 records the dividend the retailer paid to its owner 
        		"Cashouts",							// column 12 records how many cashouts the retailer experienced
        		"Stockouts",						// column 13 records how many stockouts the retailer experienced
        		"Mode",								// column 14 records the mode: 1=normal business conditions, etc.
        		"Transactions",						// column 15 records how many transactions the agent engaged in
        		"Lifespan",							// column 16 is the retailer's lifespan
        		"Owner: U",							// column 17 records the utility of the owner
        		"Owner: M",							// column 18 records how much money the owner has
        		"Owner: K",							// column 19 records how much capital the owner has
        		"p(exit)"							// column 20 shows the odds that the retailer will exit
        };
	 
        public int getColumnCount() {
            return columnNames.length;
        }

        public int getRowCount() {
           	return display.economy.traders.size(); // The number of rows in this table should be the same as the number of goods in the economy (including money and capital)
        }
 
        public String getColumnName(int col) {
            return columnNames[col];
        }
 
        public Object getValueAt(int row, int col) {
        	java.text.DecimalFormat format=new java.text.DecimalFormat("0.0000");
        	if (row>=display.economy.sortedRetailers.size()) return "";
        	Retailer r=display.economy.sortedRetailers.get(row);
        	int money=display.economy.money;
        	int product=r.r_product;
        	int last=r.basePriceHistory.size()-1;
        	if (last<0) return "";
        	if (col==0) { // The ID of the retailer
        		return r.id;
        	}
        	if (col==1) { // Owner
        		return r.owner.id;
        	}
           	if (col==2) { // Product
        		return r.r_product;
        	}
        	if (col==3) { // Money
        		return format.format(r.cashHistory.get(last));
        	}
        	if (col==4) { // Inventory
        		return format.format(r.inventoryHistory.get(last));
        	}
        	if (col==5) { // Income
        		return format.format(r.incomeHistory.get(last));
        	}
        	if (col==6) { // Supply
        		return format.format(r.supplyHistory.get(last));
        	}
        	if (col==7) { // Base price
        		return format.format(r.basePriceHistory.get(last));
        	}
        	if (col==8) { // Sales price ("Sell@")
        		return format.format(r.salesPriceHistory.get(last));
        	}
        	if (col==9) { // Buy price ("Buy@")
        		return format.format(r.supplyPriceHistory.get(last));
        	}
        	if (col==10) { // Markup
        		return format.format(r.markup);
        	}
        	if (col==11) {
        		return format.format(r.dividendHistory.get(last));
        	}
        	if (col==12) { // Cashouts
        		return r.cashoutsHistory.get(last);
        	}
        	if (col==13) { // Stockouts
        		return r.stockoutsHistory.get(last);
        	}
        	if (col==14) { // Mode
        		return r.modeHistory.get(last);
        	}
        	if (col==15) { // Transactions
        		return r.numTransHistory.get(last);
        	}
        	if (col==16) { // Lifespan (turns in business)
        		return r.lifespan;
        	}
        	if (col==17) { // Owner: utility
        		return format.format(r.owner.cons.utility);
        	}
        	if (col==18) { // Owner: money
        		return format.format(r.owner.has(money));
        	}
        	if (col==19) { // Owner: capital
        		return format.format(r.owner.has(display.economy.capital));
        	}
        	if (col==20) {
        		return format.format(r.oddsOfExit);
        	}
        	return null; // Never will reach this line
        }
 
         public boolean isCellEditable(int row, int col) {
        	 return false;
         }
	}

	public class IndustryHistoricalProfileTableModel extends AbstractTableModel {
        private String[] columnNames = {"Turn",		// column 0 records the turn 
        		"#Using",							// column 1 records the number using the good
        		"#Making",							// column 2 records the number making the good
        		"#Buying",							// column 3 records the number buying the good
        		"#Selling", 						// column 4 records the number selling the good
        		"Made(qty)",						// column 5 records the total qty made
        		"Used(qty)", 						// column 6 records the total qty used
        		"Bought",							// column 7 records the total qty bought (by agents)
        		"Sold",								// column 8 records the total qty sold (by agents)
        		"Paid",								// column 9 records the total amount paid (by agents buying the good)
        		"Earned",							// column 10 records the total amount earned (by agents selling the good)
        		"#Ret",								// column 11 records the number of retailers dealing in the good 
        		"Price(avg)",						// column 12 records the average price at which the good was sold by retailers to agents
        		"Wage(avg)",						// column 13 records the average price at which the good was bought by retailers from agents
        };
	 
        public int getColumnCount() {
            return columnNames.length;
        }

        public int getRowCount() {
        	if (display.economy.industryHistoricalProfiles!=null) {
               	return display.economy.industryHistoricalProfiles[0].size();       		
        	}
        	return 0;
        }
 
        public String getColumnName(int col) {
            return columnNames[col];
        }
 
        public Object getValueAt(int row, int col) {
        	java.text.DecimalFormat format=new java.text.DecimalFormat("0.0000");
        	int last=display.economy.industryHistoricalProfiles[0].size()-1-row;
        	Double[] data=display.economy.industryHistoricalProfiles[industry].get(last);
        	if (col==0) { // Turn
        		return (int) (double) (data[0]);
        	}
        	if (col==1) { // Number consuming
        		return (int) (double) (data[1]);
        	}
           	if (col==2) { // Number producing
        		return (int) (double) (data[2]);
        	}
        	if (col==3) { // Number buying
        		return (int) (double) (data[3]);
        	}
        	if (col==4) { // Number selling
        		return (int) (double) (data[4]);
        	}
        	if (col==5) { // Total qty made
        		return format.format(data[5]);
        	}
        	if (col==6) { // Total qty consumed
        		return format.format(data[6]);
        	}
        	if (col==7) { // Total qty bought
        		return format.format(data[7]);
        	}
        	if (col==8) { // Total qty sold
        		return format.format(data[8]);
        	}
        	if (col==9) { // Total qty paid
        		return format.format(data[9]);
        	}
        	if (col==10) { // Total qty earned
        		return format.format(data[10]);
        	}
        	if (col==11) { // Number of retailers
        		return (int) (double) data[11];
        	}
        	if (col==12) { // Avg. price
        		return format.format(data[12]);
        	}
        	if (col==13) { // Avg. wage
        		return format.format(data[13]);
        	}
        	return null; // Never will reach this line
        }
 
         public boolean isCellEditable(int row, int col) {
        	 return false;
         }
	}

	public class IndustryRetailersTableModel extends AbstractTableModel {
        private String[] columnNames = {"ID",		// column 0 records the ID of the retailer 
        		"Money",							// column 1 records how much money the retailer has
        		"Inventory", 						// column 2 records how much the retailer has in inventory
        		"Income",							// column 3 records how much the retailer received in cash income
        		"Supply", 							// column 4 records how much in supplies the retailer procured
        		"Base price",						// column 5 records the retailer's base price
        		"Sell@",							// column 6 records the retailer's sell price
        		"Buy@",								// column 7 records the retailer's buy (supply) price
        		"Markup",							// column 8 records the retailer's markup
        		"Dividend",							// column 9 records the dividend the retailer paid to its owner 
        		"Mode",								// column 10 records the mode: 1=normal business conditions, etc.
        		"Transactions",						// column 11 records how many transactions the agent engaged in
        		"Lifespan",							// column 12 is the retailer's lifespan
        		"p(exit)"							// column 13 shows the odds that the retailer will exit
        };
	 
        public int getColumnCount() {
            return columnNames.length;
        }

        public int getRowCount() {
        	Economy e=display.economy;
        	ArrayList<Retailer> retailers=e.retailersSortedByIndustry[industry];
         	return retailers.size(); 
        }
 
        public String getColumnName(int col) {
            return columnNames[col];
        }
 
        public Object getValueAt(int row, int col) {
        	java.text.DecimalFormat format=new java.text.DecimalFormat("0.0000");
        	Retailer r=display.economy.retailersSortedByIndustry[industry].get(row);
        	int money=display.economy.money;
        	int last=r.basePriceHistory.size()-1;
        	if (last<0) return "";
        	if (col==0) { // The ID of the retailer
        		return r.id;
        	}
        	if (col==1) { // Money
        		return format.format(r.cashHistory.get(last));
        	}
        	if (col==2) { // Inventory
        		return format.format(r.inventoryHistory.get(last));
        	}
        	if (col==3) { // Income
        		return format.format(r.incomeHistory.get(last));
        	}
        	if (col==4) { // Supply
        		return format.format(r.supplyHistory.get(last));
        	}
        	if (col==5) { // Base price
        		return format.format(r.basePriceHistory.get(last));
        	}
        	if (col==6) { // Sales price ("Sell@")
        		return format.format(r.salesPriceHistory.get(last));
        	}
        	if (col==7) { // Buy price ("Buy@")
        		return format.format(r.supplyPriceHistory.get(last));
        	}
        	if (col==8) { // Markup
        		return format.format(r.markup);
        	}
        	if (col==9) {
        		return format.format(r.dividendHistory.get(last));
        	}
        	if (col==10) { // Mode
        		return r.modeHistory.get(last);
        	}
        	if (col==11) { // Transactions
        		return r.numTransHistory.get(last);
        	}
        	if (col==12) { // Lifespan (turns in business)
        		return r.lifespan;
        	}
        	if (col==13) {
        		return format.format(r.oddsOfExit);
        	}
        	return null; // Never will reach this line
        }
 
         public boolean isCellEditable(int row, int col) {
        	 return false;
         }
	}

	public class IndustryPopulationTableModel extends AbstractTableModel {
        private String[] columnNames = {"Agent ID",	// column 0 records the ID of the agent 
        		"Retailers known",					// column 1 records the IDs of the retailers of this good the agent knows
        		"Money",							// column 2 records how much money the agent has
        		"Utility",							// column 3 records the agent's utility last turn
        		"Labor(i)", 						// column 4 records how much the agent worked at making this good last turn
        		"Used(i)",							// column 5 records how much the agent used of the good last turn
        		"Bought(i)", 						// column 6 records how much the agent bought of the good last turn
        		"Sold(i)",							// column 7 records how much the agent sold of the good last turn
        		"Paid(i)",							// column 8 records how much the agent paid for this good last turn
        		"Earned(i)",						// column 9 records how much the agent earned by selling this good last turn
        };
	 
        public int getColumnCount() {
            return columnNames.length;
        }

        public int getRowCount() {
           	return display.economy.sortedPopulation.size(); // The number of rows in this table should be the same as the number of goods in the economy (including money and capital)
        }
 
        public String getColumnName(int col) {
            return columnNames[col];
        }
 
        public Object getValueAt(int row, int col) {
        	java.text.DecimalFormat format=new java.text.DecimalFormat("0.0000");
        	Agent agent=display.economy.sortedPopulation.get(row);
        	int money=display.economy.money;
        	int capital=display.economy.capital;
        	if (col==0) { // The ID of the agent
        		return agent.id;
        	}
        	if (col==1) {
        		String ret="";
        		for (Trader t: agent.knows) {
        			Retailer r=(Retailer) t;
        			if (r.r_product==industry) ret=ret+r.id+",";
        		}
        		return ret;
        	}
        	if (col==2) { // Money
        		return format.format(agent.has(money));
        	}
           	if (col==3) { // Utility
        		return format.format(agent.cons.utility);
        	}
        	if (col==4) { // Labor at good i
        		return format.format(agent.labor.labor[industry]);
        	}
        	if (col==5) { // Consumed of good i
        		return format.format(agent.cons.used[industry]);
        	}
        	if (col==6) { // Bought of good i
        		return format.format(agent.bought[industry]);
        	}
        	if (col==7) { // Sold of good i
           		return format.format(agent.sold[industry]);
           	}
        	if (col==8) { // Paid for good i
           		return format.format(agent.paid[industry]);
        	}
        	if (col==9) { // Earned for good i
           		return format.format(agent.earned[industry]);
        	}
        	return null; // Never will reach this line
        }

        public boolean isCellEditable(int row, int col) {
        	 return false;
         }
	}


}
