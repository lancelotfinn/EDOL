package model;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import org.jfree.chart.*;
import org.jfree.data.*;
import org.jfree.data.category.*;
import org.jfree.chart.block.GridArrangement;
import org.jfree.chart.plot.*;
import java.util.*;

public class GuiFrame extends JFrame {
	/*
	 * This class also houses a main() method and 
	 * could launch the program. 
	 * 
	 * Its main work is to create and run the graphical 
	 * user interface. It contains many references to 
	 * components within the GUI, such as the display
	 * panels where data related to the economy is 
	 * depicted.
	 */
	EconomyDisplayPanels activeEconomy;
	EconomyDisplayPanels[] allEconomies;
	FourEconomiesThread fourEconomiesThread;
	JPanel contentPane;
	JPanel panel1;
	JPanel panel2;
	int panel2_selector; // 1=chartDisplay, 2=technology, 3=agent detail, 4=retailer detail, 5=industry detail, 6=census
	// All the different panes under the view menu
	JPanel chartDisplay; 
	JPanel technology;
	// Chart Display pane items
	ChartPanel chartDisplay_chart2a;
	ChartPanel chartDisplay_chart2b;
	ChartPanel chartDisplay_chart2c;
	ChartPanel chartDisplay_chart2d;
	// The Technology/Parameters view fields
	ChartPanel technology_chart2a;
	JPanel technology_console;
	TechnologyTablePanel technology_table2c;
	ParametersTablePanel technology_table2d;
	JButton[] technology_consoleButtons; // 10 buttons
	public boolean paused;
	public boolean running;
	JButton button_run;
	JButton button_pause;
	JButton button_new;
	JTextField field_numAgents;
	JTextField field_numGoods;
	public JTextField field_turn;
	public JLabel label_turn;
	ButtonListener buttonListener;

	// Menus
	JMenu m_TimeSeries;
	JMenu m_Scatterplots;
	JMenu m_Economy;
	JMenu m_Mode;
	JCheckBoxMenuItem chart_m_active1; // chart_m_active stands for "chart menu item that is currently activated"
	JCheckBoxMenuItem chart_m_active2;
	JCheckBoxMenuItem chart_m_active3;
	JCheckBoxMenuItem chart_m_active4;
	JCheckBoxMenuItem[] m_TimeSeries_menuItems;
	JCheckBoxMenuItem[] m_Scatterplots_menuItems;
	JCheckBoxMenuItem[] modeMenuItems;
	JCheckBoxMenuItem[] economyMenuItems;

	// Information
	int mode; // 0=one economy, 1=4 economies
	Integer[] numGoods4; // The number of goods for the four economies case
	Integer[] numAgents4; // The number of agents for the four economies case
	Double[] sigma4;

	public String[] m_View_items={"Chart Display",		// item 0
			"Technology/Parameters",					// item 1
			"Agent Detail",								// item 3
			"Retailer Detail",							// item 4
			"Industry Detail",							// item 5
			"Census"									// item 6
			};
	public static String[] m_TimeSeries_items={"Median and average utility", 	// item 0
			"Historical prices", 												// item 1
			"Historical sales", 												// item 2
			"Cash: Agents and Retailers", 										// item 3
			"GDP: Demand and Supply Side",										// item 4
			"Labor by Overhead/Self-Supply/Market",								// item 5
			"Labor by Good",													// item 6
			"Consumption by Good",												// item 7
			"Retailer Access by Good",											// item 8
			"Consumables and Tradables",										// item 9
			"Diversification, Make vs. Use",									// item 10
			"Avg. utility: Agents vs. Retailers",								// item 11
			"GDP supply: profits and wages",									// item 12
			"Price-to-overhead ratio",											// item 13
			"Capital: retailers and non-retailers",								// item 14
			"Count: retailers and non-retailers"								// item 15
	};
	int[] activatedMenuItems_TimeSeries={0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15};
	public static String[] m_Scatterplots_items={
		"Goods space",
		"Industries in P/Q space",
		"Industries in P/Q space (moving avg.",
		"Industries in P/Q space (snake)",
		"Industries: retailers and specialist producers",
		"Industries: consumers and producers",
		"Agents in M/U space",
		"Agents in K/U space"
	};
	int[] activatedMenuItems_Scatterplots={0, 1, 2, 3, 4, 5, 6};

		
	public static void main(String[] args) {
		// Make the frame first.
		GuiFrame gui=new GuiFrame();
		gui.runGUI();
	}
	
	public void setNumGoods(int g) {
		activeEconomy.numGoods=g;
		field_numGoods.setText(""+g);
	}	
	
	public void setMode(int m) {
		if (mode==m) return;
		mode=m;
		if (mode==0) {
			/*
			 *  If I recall correctly, mode=0 signifies
			 *  that only one economy is to be displayed.
			 *  mode=1 signifies that four economies are 
			 *  to be displayed in parallel.
			 */
			m_Economy.setEnabled(false);
			modeMenuItems[0].setSelected(true);
			modeMenuItems[1].setSelected(false);
			allEconomies[0]=new EconomyDisplayPanels(this);
			activeEconomy=allEconomies[0];
			if (fourEconomiesThread!=null) fourEconomiesThread.stop=true;
			for (int i=1; i<5; i++) {
				allEconomies[i]=null;
			}
		}
		if (mode==1) {
			m_Economy.setEnabled(true);
			modeMenuItems[0].setSelected(false);
			modeMenuItems[1].setSelected(true);
			for (int i=1; i<5; i++) {
				allEconomies[i]=new EconomyDisplayPanels(this);
				activeEconomy=allEconomies[i];
			}
			economyMenuItems[0].setSelected(true);
			economyMenuItems[1].setSelected(false);
			economyMenuItems[2].setSelected(false);
			economyMenuItems[3].setSelected(false);
			activeEconomy=allEconomies[1];
			if (allEconomies[0].economy!=null) allEconomies[0].economy.stop=true;
			allEconomies[0]=null;
		}
		updateEditability();
	}
	
	public void setNumAgents(int n) {
		activeEconomy.numAgents=n;
		field_numAgents.setText(""+n);
	}

	public void runGUI() {
		// Create the "infrastructure" the four economies model even though we're not ready to use it
		allEconomies=new EconomyDisplayPanels[5];
		allEconomies[0]=new EconomyDisplayPanels(this);
		activeEconomy=allEconomies[0]; // Rule: 0=one-economy economy; 1-4 refer to the four economies in four-economy mode
		
		// Next, set up the menus
		JMenuBar menuBar=new JMenuBar();
		MenuListener menuListener=new MenuListener(this);
		MenuListener2 chartsMenuListener=new MenuListener2(this);
		setJMenuBar(menuBar);
		JMenu m_File=new JMenu("File");
		String[] m_File_items={"Save","Load","Exit"};
		for (String s: m_File_items) {
			JMenuItem i=new JMenuItem(s);
			i.addActionListener(menuListener);
			m_File.add(i);
		}
		JMenu m_View=new JMenu("View");
		for (String s: m_View_items) {
			JMenuItem i=new JMenuItem(s);
			i.addActionListener(menuListener);
			m_View.add(i);
		}
		m_TimeSeries=new JMenu("TimeSeries");
		int count=0;
		m_TimeSeries_menuItems=new JCheckBoxMenuItem[m_TimeSeries_items.length];
		for (String s: m_TimeSeries_items) {
			m_TimeSeries_menuItems[count]=new JCheckBoxMenuItem(m_TimeSeries_items[count]);
			m_TimeSeries.add(m_TimeSeries_menuItems[count]);
			m_TimeSeries_menuItems[count].addActionListener(chartsMenuListener);
			count++;
		}
		m_Scatterplots=new JMenu("Scatterplots");
		count=0;
		m_Scatterplots_menuItems=new JCheckBoxMenuItem[m_Scatterplots_items.length];
		for (String s: m_Scatterplots_items) {
			m_Scatterplots_menuItems[count]=new JCheckBoxMenuItem(m_Scatterplots_items[count]);
			m_Scatterplots_menuItems[count].addActionListener(chartsMenuListener); // So I reuse the same charts menu listener, though the menu is different
			m_Scatterplots.add(m_Scatterplots_menuItems[count]);
			count++;
		}
		JMenuItem m_RestoreDefaults=new JMenuItem("Restore Defaults");
		m_Scatterplots.add(m_RestoreDefaults);
		m_RestoreDefaults.addActionListener(menuListener);
		m_Mode=new JMenu("Mode");
		modeMenuItems=new JCheckBoxMenuItem[2];
		String[] m_Mode_items={"One Economy","Four Economies"};
		for (int i=0; i<2; i++) {
			modeMenuItems[i]=new JCheckBoxMenuItem(m_Mode_items[i]);
			modeMenuItems[i].addActionListener(menuListener);
			m_Mode.add(modeMenuItems[i]);
		}
		m_Economy=new JMenu("Economy");
		economyMenuItems=new JCheckBoxMenuItem[4];
		String[] m_Economy_items={"Economy 1","Economy 2","Economy 3","Economy 4"};
		for (int i=0; i<4; i++) {
			economyMenuItems[i]=new JCheckBoxMenuItem(m_Economy_items[i]);
			economyMenuItems[i].addActionListener(menuListener);
			m_Economy.add(economyMenuItems[i]);
		}
		m_Economy.setEnabled(false);
		menuBar.add(m_File);
		menuBar.add(m_View);
		menuBar.add(m_TimeSeries);
		menuBar.add(m_Scatterplots);
		menuBar.add(m_Mode);
		menuBar.add(m_Economy);
		// Now organize the content pane
		contentPane=new JPanel(new BorderLayout());
		setContentPane(contentPane);
		// First, a ribbon at the top that tells the number of agents, the number of goods, etc.
		panel1=new JPanel(new FlowLayout());
		panel1.setBackground(Color.PINK);
		// Number of agents
		panel1.add(new JLabel("Agents: "));
		field_numAgents=new JTextField();
		field_numAgents.setEditable(true);
		field_numAgents.setColumns(4);
		field_numAgents.addActionListener(new NumAgentsFieldListener());
		panel1.add(field_numAgents);
		// Number of goods
		panel1.add(new JLabel("   Goods: "));
		field_numGoods=new JTextField();
		field_numGoods.setEditable(true);
		field_numGoods.setColumns(4);
		field_numGoods.addActionListener(new NumGoodsFieldListener());
		panel1.add(field_numGoods);
		// Turn
		panel1.add(new JLabel("   Turn: "));
		field_turn=new JTextField();
		field_turn.setText("0");
		field_turn.setColumns(3);
		field_turn.setEditable(false);
		panel1.add(field_turn);
		panel1.add(new JLabel("(of    "));
		label_turn=new JLabel("");
		panel1.add(label_turn);
		panel1.add(new JLabel(")        "));
		// Buttons to run and pause the simulation
		buttonListener=new ButtonListener(this);
		button_new=new JButton("New");
		button_new.addActionListener(buttonListener);
		button_run=new JButton("Run");
		button_run.addActionListener(buttonListener);
		button_run.setEnabled(false);
		button_pause=new JButton("Pause");
		button_pause.addActionListener(buttonListener);
		button_pause.setEnabled(false);
		panel1.add(button_run);
		panel1.add(button_pause);
		panel1.add(button_new);
		contentPane.add(BorderLayout.NORTH, panel1);

		// Make all the panes which will rotate to fill panel 2 of the GUI.
		makeChartDisplayPane();
		makeTechnologyDetailPane();

		// One economy mode (not four)
		setMode(0);
		modeMenuItems[0].setSelected(true);
		
		panel2=technology;
		contentPane.add(panel2); 
		setSize(1100,650);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		paused=true;
		running=false;
		setNumGoods(20); // the default is 20 goods
		setNumAgents(200); // the default is 200 agents
		setTechnology(3); // starry skies is the default technology
		setVisible(true);
		validate();
	}
	
	public class NumAgentsFieldListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			Integer new_numAgents=null;
			try {
				new_numAgents=Integer.valueOf(field_numAgents.getText());
			} catch(Exception ex) {}
			if (new_numAgents==activeEconomy.numAgents) return;
			if (new_numAgents==null) new_numAgents=activeEconomy.numAgents;
			setNumAgents(new_numAgents);
			if (activeEconomy.economy!=null) activeEconomy.economy.stop=true;
			activeEconomy.economy=null; // Redundant because if the economy were not null the field would not be editable
			// No need to reset economy because it doesn't alter the production function, utility function, or parameters
			updateDisplays();
			field_numAgents.transferFocus();
		}
	}
	
	public class NumGoodsFieldListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			UtilityFunction u_old=activeEconomy.uFunction; // Keep these because they may be used as the basis for new versions
			AvoidCostPF p_old=activeEconomy.pFunction;
			int old_numGoods=0;
			if (u_old!=null) old_numGoods=u_old.coeffs.length; // Remember how many goods there WERE
			boolean testing=false;
			if (testing) System.out.println("TESTING GuiFrame.NumGoodsFieldListener.actionPerformed(). I HEAR YOU!!!");
			Integer new_numGoods=null;
			try {
				new_numGoods=Integer.valueOf(field_numGoods.getText());
			} catch(Exception ex) {
				if (testing) System.out.println("Content of field_numGoods not parseable");
			}
			if (new_numGoods==activeEconomy.numGoods) return;
			if (new_numGoods==null) new_numGoods=activeEconomy.numGoods;
			if (testing) System.out.println("new_numGoods=="+new_numGoods);
			setNumGoods(new_numGoods);
			if (activeEconomy.economy!=null) activeEconomy.economy.stop=true;
			activeEconomy.economy=null; // Redundant because if the economy were not null the field would not be editable.
			resetEconomy();
			UtilityFunction u_new=new UtilityFunction(activeEconomy.numGoods);
			AvoidCostPF p_new=new AvoidCostPF(activeEconomy.numGoods);
			for (int i=0; i<activeEconomy.numGoods; i++) {
				if (i<old_numGoods) {
					if (u_old.coeffs[i]!=null) u_new.coeffs[i]=u_old.coeffs[i];
					if (p_old.getL0(i)!=null) p_new.setL0(i, p_old.getL0(i));
				}
			}
			activeEconomy.pFunction=p_new; // So technology_table2c refers only to the active economy
			activeEconomy.uFunction=u_new;
			technology_table2c.refreshTable();
			field_numGoods.transferFocus();
			technology_table2c.button.ready();
			updateDisplays();
		}
	}
	
	public void updateNumGoodsField() {
		if (activeEconomy.numGoods!=null) {
			field_numGoods.setText(""+activeEconomy.numGoods);
			return;
		}
		field_numGoods.setText("");
		return;
	}
	
	public void updateNumAgentsField() {
		if (activeEconomy.numAgents!=null) {
			field_numAgents.setText(""+activeEconomy.numAgents);
			return;
		}
		field_numAgents.setText("");
		return;
	}
	
	public void makeChartDisplayPane() {
		// Make the ChartDisplay panel
		chartDisplay=new JPanel(new GridLayout(2,2));
		chartDisplay.setBackground(Color.WHITE);
		chartDisplay_chart2a=new ChartPanel("", activeEconomy);
		chartDisplay_chart2b=new ChartPanel("", activeEconomy);
		chartDisplay_chart2c=new ChartPanel("", activeEconomy);
		chartDisplay_chart2d=new ChartPanel("", activeEconomy);
		chartDisplay.add(chartDisplay_chart2a);
		chartDisplay.add(chartDisplay_chart2b);
		chartDisplay.add(chartDisplay_chart2c);
		chartDisplay.add(chartDisplay_chart2d);
	}
	
	public void defaultCharts() {
		if (mode==0) {
			/*
			 *  Four charts that are likely to be of 
			 *  special interest are created automatically.
			 *  Others can be added by the user. Each new 
			 *  chart causes one chart to rotate out of 
			 *  the display window.
			 */
			chartDisplay_chart2a.master=activeEconomy.chartsMenu_Scatterplots[0];
			chartDisplay_chart2b.master=activeEconomy.chartsMenu_TimeSeries[5];
			chartDisplay_chart2c.master=activeEconomy.chartsMenu_TimeSeries[10];
			chartDisplay_chart2d.master=activeEconomy.chartsMenu_TimeSeries[11];
			chart_m_active1=m_Scatterplots_menuItems[0];
			chart_m_active2=m_TimeSeries_menuItems[5];
			chart_m_active3=m_TimeSeries_menuItems[10];
			chart_m_active4=m_TimeSeries_menuItems[11];
			return;
		}				
	}

	public void makeTechnologyDetailPane() {
		// The Technology/parameters pane
		technology=new JPanel(new GridLayout(2,2));
		technology_console=new JPanel(new GridLayout(5,3));
		technology_chart2a=new ChartPanel("Technology: goods space",activeEconomy); 
		technology_table2c=new TechnologyTablePanel(this);
		technology_table2d=new ParametersTablePanel(this);
		technology.add(technology_chart2a);
		technology.add(technology_console);
		technology.add(technology_table2c);
		technology.add(technology_table2d);
		technology_consoleButtons=new JButton[11];
		technology_consoleButtons[0]=new JButton("Economy 1");
		technology_consoleButtons[1]=new JButton("Economy 2");
		technology_consoleButtons[2]=new JButton("Economy 3");
		technology_consoleButtons[3]=new JButton("Economy 4");
		technology_consoleButtons[4]=new JButton("Copy Economy 1");
		technology_consoleButtons[4].addActionListener(buttonListener);
		technology_consoleButtons[5]=new TechMakerButton(this, 0);
		technology_consoleButtons[6]=new TechMakerButton(this, 1);
		technology_consoleButtons[7]=new TechMakerButton(this, 2);
		technology_consoleButtons[8]=new TechMakerButton(this, 3);
		technology_consoleButtons[9]=new TechMakerButton(this, 4);
		technology_consoleButtons[10]=new TechMakerButton(this, 5);
		for (JButton button: technology_consoleButtons) {
			technology_console.add(button);
		}
		for (int i=0; i<4; i++) {
			technology_consoleButtons[i].setEnabled(false);
		}		
	}
	
	public class TechMakerButton extends JButton {
		int type; 
		GuiFrame gui;
		String[] typeNames={"TECHNOLOGY: 'Diagonal'",		// item 0
				"TECHNOLOGY: 'Snake'",						// item 1
				"TECHNOLOGY: 'Spray'",						// item 2
				"TECHNOLOGY: 'Starry skies'",				// item 3
				"TECHNOLOGY: 'Breakthroughs'",				// item 4
				"TECHNOLOGY: 'Diminishing returns'"			// item 5
		};
		public TechMakerButton(GuiFrame g, int i) {
			super();
			type=i;
			setText(typeNames[type]);
			gui=g;
			addActionListener(new TechButtonListener(type, gui));
		}
	}
	
	public class TechButtonListener implements ActionListener {
		int type;
		GuiFrame gui;
		public TechButtonListener(int i, GuiFrame g) {
			type=i;
			gui=g;
		}
		public void actionPerformed(ActionEvent event) {
			gui.setTechnology(type);
		}
	}
	
	public void updateDisplays() {
		boolean testing=false;
		technology_chart2a.update();
		technology_table2c.refreshTable();
		technology_table2d.refreshTable();
		updateNumGoodsField();
		updateNumAgentsField();
		if (activeEconomy.economy==null) return;
		if (mode==0) {
			chartDisplay_chart2a.update();
			chartDisplay_chart2b.update();
			chartDisplay_chart2c.update();
			chartDisplay_chart2d.update();			
			for (ChartPanel chart: activeEconomy.chartsMenu_TimeSeries) {
				chart.update();
			}
			for (ChartPanel chart: activeEconomy.chartsMenu_Scatterplots) {
				chart.update();
			}
		}
		if (mode==1) {
			chartDisplay_chart2a.update();
			chartDisplay_chart2b.update();
			chartDisplay_chart2c.update();
			chartDisplay_chart2d.update();	
			for (int i=1; i<5; i++) {
				EconomyDisplayPanels display_i=allEconomies[i];
				for (ChartPanel chart: activeEconomy.chartsMenu_TimeSeries) {
					chart.update();
				}
				for (ChartPanel chart: activeEconomy.chartsMenu_Scatterplots) {
					chart.update();
				}
			}
		}
		chartDisplay.validate();
//		activeEconomy.updateDisplays();
		validate();
		repaint();
	}
	
	public void setTechnology(int type) {
		boolean testing=false;
		if (testing) System.out.println("TESTING GuiFrame.setTechnology(). numGoods="+activeEconomy.numGoods);
		Integer localNumGoods=20;
		if (activeEconomy.numGoods!=null) localNumGoods=activeEconomy.numGoods;
		setNumGoods(localNumGoods);
		if (activeEconomy.economy!=null) activeEconomy.economy.stop=true;
		activeEconomy.economy=null;
		UtilityFunction u=new UtilityFunction(activeEconomy.numGoods);
		AvoidCostPF p=new AvoidCostPF(activeEconomy.numGoods);
		if (testing) System.out.println("TESTING GuiFrame.TechButtonListener.actionPerformed(). gui.numGoods="+activeEconomy.numGoods);
		if (type==0) { // Diagonal
			for (int i=2; i<activeEconomy.numGoods; i++) {
				double x=3*Math.random();
				u.coeffs[i]=x;
				p.setL0(i, x);
			}
		}
		if (type==1) { // Snake
			for (int i=2; i<activeEconomy.numGoods; i++) {
				double a=Math.random()*3;
				u.coeffs[i]=a;
				double l0=0.2+0.5*a+0.5*Math.sin(5*a);
				p.setL0(i, l0);
			}
		}
		if (type==2) { // Spray
			for (int i=2; i<activeEconomy.numGoods; i++) {
				double x=Math.random()*4;
				u.coeffs[i]=x*(0.5+0.7*Math.random());
				double l0=Math.pow(x, 1.5)*(0.8+0.7*Math.random());
				p.setL0(i, l0);
			}
		}
		if (type==3) { // Starry skies
			for (int i=2; i<activeEconomy.numGoods; i++) {
				double l0=Math.random();
				double a=Math.random();
				u.coeffs[i]=a;
				p.setL0(i, l0);
			}
		}
		if (type==4) { // Breakthroughs
			for (int i=2; i<activeEconomy.numGoods; i++) {
				double x=1+((int) (Math.random()*5))*2.5;
				double a=0.3*x+Math.random()*0.5;
				double l0=0.5*x+Math.random();
				u.coeffs[i]=a;
				p.setL0(i, l0);
			}
		}
		if (type==5) { // Diminishing returns
			for (int i=2; i<activeEconomy.numGoods; i++) {
				double a=0.1+Math.pow(Math.random(),0.5)*3;
				double l0=Math.random()/(0.2+a);
				u.coeffs[i]=a;
				p.setL0(i, l0);
			}
		}
		double moneyAlpha=0;
		for (int i=2; i<activeEconomy.numGoods; i++) {
			moneyAlpha=moneyAlpha+u.coeffs[i];
		}
		u.coeffs[0]=moneyAlpha;
		u.coeffs[1]=moneyAlpha;
		p.setL0(0, 1000000000);
		p.setL0(1, 1000000000);
		TechnologyTablePanel techTable=technology_table2c;
		activeEconomy.uFunction=u;
		activeEconomy.pFunction=p;
		techTable.refreshTable();
		techTable.update();
		updateEditability();
//		techTable.revalidate();
//		techTable.scrollPane.repaint();
		technology_chart2a.update();
		if (testing) System.out.println(
				"TESTING GuiFrame.TechButtonListener.actionPerformed(). u.coeffs.length="
						+u.coeffs.length);
		repaint();
	}
	
	private void substitutePanel2(JPanel substitute) {
		if (substitute!=technology) {
			field_numAgents.setEditable(false);
			field_numGoods.setEditable(false);
			button_new.setEnabled(false);			
		}
		contentPane.remove(panel2);
		panel2=substitute;
		contentPane.add(BorderLayout.CENTER, substitute);
		activeEconomy.updateDisplays();
		updateDisplays();
		repaint();
	}
	
	public class MenuListener implements ActionListener {
		GuiFrame outer;
		
		public MenuListener(GuiFrame g) {
			outer=g;
		}
		
		public synchronized void actionPerformed(ActionEvent event) {
			JMenuItem source=(JMenuItem) event.getSource();
			String text=source.getText();
			if (text=="Exit") {
				System.exit(1);
			}
			if (text=="Save") {
				pause();
				JFileChooser fileChooser=new JFileChooser();
				JOptionPane pane=new JOptionPane();
				int returnVal=fileChooser.showSaveDialog(pane);
				Y: if (returnVal==JFileChooser.APPROVE_OPTION){
					File file=fileChooser.getSelectedFile();
					if (file.exists()) {
						int confirm = JOptionPane.showConfirmDialog(null, "This file already exists. Do you want to replace it?");
						if ((confirm == JOptionPane.CANCEL_OPTION)|(confirm == JOptionPane.NO_OPTION)) break Y;
					}
					try {
						FileOutputStream fos=new FileOutputStream(file+".ser");
						ObjectOutputStream oos=new ObjectOutputStream(fos);
						if (mode==0) oos.writeObject(activeEconomy.economy);
						if (mode==1) oos.writeObject(fourEconomiesThread);
					} catch(Exception ex) {ex.printStackTrace();}					
				}
				return;
			}
			if (text=="Load") {
				pause();
				JFileChooser fileChooser=new JFileChooser();
				JOptionPane pane=new JOptionPane();
				int returnVal=fileChooser.showOpenDialog(pane);
				File file=fileChooser.getSelectedFile();
				if (file.exists()==false) {
					JOptionPane.showMessageDialog(null, "File does not exist.");
				} else {
					try {
						FileInputStream fis=new FileInputStream(file);
						ObjectInputStream ois=new ObjectInputStream(fis);
						Object o=ois.readObject();		
						if (o.getClass()==Economy.class) {
							setMode(0);
							Economy e=(Economy) o;
							makeEconomy(e);
							readyToRun();
						}
						if (o.getClass()==FourEconomiesThread.class) {
							setMode(1);
							fourEconomiesThread=(FourEconomiesThread) o;
							for (int i=0; i<4; i++) {
								allEconomies[i+1].buildDisplays(fourEconomiesThread.economies[i]);
							}
							readyToRun();
						}
					} catch(Exception ex) {ex.printStackTrace();}					
				}
				return;
			}
			if (text=="Economy 1") {
				activeEconomy=allEconomies[1];
				panel2_economyChange();
				updateEditability();
				if (activeEconomy.economy==null) {
					field_numGoods.setEditable(true);
					field_numAgents.setEditable(true);
				} else {
					field_numGoods.setEditable(false);
					field_numAgents.setEditable(false);
				}
				for (int i=0; i<4; i++) {
					economyMenuItems[i].setSelected(false);
				}
				economyMenuItems[0].setSelected(true);
				updateDisplays();
				return;
			}
			if (text=="Economy 2") {
				activeEconomy=allEconomies[2];
				panel2_economyChange();
				updateEditability();
				if (activeEconomy.economy==null) {
					field_numGoods.setEditable(true);
					field_numAgents.setEditable(true);
				} else {
					field_numGoods.setEditable(false);
					field_numAgents.setEditable(false);
				}
				for (int i=0; i<4; i++) {
					economyMenuItems[i].setSelected(false);
				}
				economyMenuItems[1].setSelected(true);
				updateDisplays();
				return;
			}
			if (text=="Economy 3") {
				activeEconomy=allEconomies[3];
				panel2_economyChange();
				updateEditability();
				if (activeEconomy.economy==null) {
					field_numGoods.setEditable(true);
					field_numAgents.setEditable(true);
				} else {
					field_numGoods.setEditable(false);
					field_numAgents.setEditable(false);
				}
				for (int i=0; i<4; i++) {
					economyMenuItems[i].setSelected(false);
				}
				economyMenuItems[2].setSelected(true);
				updateDisplays();
				return;
			}
			if (text=="Economy 4") {
				activeEconomy=allEconomies[4];
				panel2_economyChange();
				updateEditability();
				for (int i=0; i<4; i++) {
					economyMenuItems[i].setSelected(false);
				}
				economyMenuItems[3].setSelected(true);
				updateDisplays();
				return;
			}
			if (text=="One Economy") {
				setMode(0);
				updateDisplays();
				return;
			}
			if (text=="Four Economies") {
				setMode(1);
				updateDisplays();
				return;
			}
			if (text=="Technology/Parameters") {
//				outer.pause();
				panel2_selector=2;
				field_numAgents.setEditable(true);
				field_numGoods.setEditable(true);
				button_new.setEnabled(true);
				outer.substitutePanel2(technology);
				return;
			}
			if (activeEconomy.economy==null) {
				JOptionPane.showMessageDialog(null, "Other panes become viewable only when an economy exists.");	
				return;
			}
			if (text=="Chart Display") {
				panel2_selector=1;
				outer.substitutePanel2(chartDisplay);
				return;
			}
			if (text=="Agent Detail") {
				panel2_selector=3;
				outer.substitutePanel2(activeEconomy.agentDetail);
				return;
			}
			if (text=="Retailer Detail") {
				panel2_selector=4;
				if (activeEconomy.economy.traders.size()==0) {
					JOptionPane.showMessageDialog(null, "No retailers exist yet.");
					return;
				}
				if (activeEconomy.retailerDetail_table2c.retailer==null) {
					int selector=(int) (Math.random()*activeEconomy.economy.traders.size());
					Retailer r=(Retailer) activeEconomy.economy.traders.get(selector);
					activeEconomy.retailerDetail_table2c.update(r);
				}
				substitutePanel2(activeEconomy.retailerDetail);
				return;
			}
			if (text=="Census") {
				panel2_selector=6;
				substitutePanel2(activeEconomy.census);
				return;
			}
			if (text=="Industry Detail") {
				panel2_selector=5;
				substitutePanel2(activeEconomy.industryDetail);
				return;
			}
			if (text == "Restore Defaults") {
				defaultCharts();
				return;
			}
			JOptionPane.showMessageDialog(null, "The menu item "+text+" is not yet supported.");
		}
		
	}
	
	public void panel2_economyChange() {
		if (panel2_selector==3) substitutePanel2(activeEconomy.agentDetail);
		if (panel2_selector==4) substitutePanel2(activeEconomy.retailerDetail);
		if (panel2_selector==5) substitutePanel2(activeEconomy.industryDetail);
		if (panel2_selector==6) substitutePanel2(activeEconomy.census);
	}
	
	public void updateEditability() {
		if (activeEconomy.economy==null) {
			field_numGoods.setEditable(true);
			field_numAgents.setEditable(true);
			technology_table2d.button.setEnabled(true);
		} else {
			field_numGoods.setEditable(false);
			field_numAgents.setEditable(false);
			technology_table2d.button.setEnabled(false);
		}
		technology_table2c.button.ready();
	}

	public class MenuListener2 implements ActionListener { 
		// This is the menu listener for the TimeSeries and Scatterplot menus that display charts
		GuiFrame gui;

		public MenuListener2(GuiFrame g) {
			gui=g;
		}
				
		public synchronized void actionPerformed(ActionEvent event) {
			JCheckBoxMenuItem source=(JCheckBoxMenuItem) event.getSource();
			if (activeEconomy.economy==null) {
				JOptionPane.showMessageDialog(null, "No economy exists yet. Make one first.");
				source.setSelected(false);
				return;
			}
			String text=source.getText();
//			System.out.println(text);
			if (mode==0) {
				boolean found=updateCharts_oneEconomy(text);
				if (found) return;
			}
			if (mode==1) {
				boolean found=updateCharts_fourEconomies(text);
				if (found) return;
			} 
			source.setSelected(false);
			JOptionPane.showMessageDialog(null, "No actions have been programmed for state changes at "+text+".");
		}
		
		public boolean updateCharts_fourEconomies(String text) {
			boolean testing=false;
			if (testing) System.out.println("TESTING GuiFrame.MenuListener2.updateCharts_fourEconomies() with text="+text);
			for (int chartNumber: activatedMenuItems_TimeSeries) {
				if (text == m_TimeSeries_items[chartNumber]) {
					ChartPanel[] chartPanels=new ChartPanel[4];
					for (int i=0; i<4; i++) {
						int ec=i+1;
						chartPanels[i]=allEconomies[ec].chartsMenu_TimeSeries[chartNumber];
						chartPanels[i].economyMarker=" ("+ec+")";
					}
					if (testing) System.out.println("TESTING GuiFrame.MenuListener2.updateCharts_fourEconomies(). text="
							+text+". chart1="+chartPanels[0]+". chart2="+chartPanels[1]+
							". chart3="+chartPanels[2]+". chart4="+chartPanels[3]);	
					chartDisplay_chart2a.master=chartPanels[0];
					chartDisplay_chart2b.master=chartPanels[1];
					chartDisplay_chart2c.master=chartPanels[2];
					chartDisplay_chart2d.master=chartPanels[3];
					for (int i=1; i<5; i++) {
						allEconomies[i].updateDisplays();
					}
					gui.updateDisplays();
					gui.repaint();
					return true;
				}
			}
			for (int chartNumber: activatedMenuItems_Scatterplots) {
				if (text == m_Scatterplots_items[chartNumber]) {
					ChartPanel[] chartPanels=new ChartPanel[4];
					for (int i=0; i<4; i++) {
						int ec=i+1;
						chartPanels[i]=allEconomies[ec].chartsMenu_Scatterplots[chartNumber];
						chartPanels[i].economyMarker=" ("+ec+")";
					}
					if (testing) System.out.println("TESTING GuiFrame.MenuListener2.updateCharts_fourEconomies(). text="
							+text+". chart1="+chartPanels[0]+". chart2="+chartPanels[1]+
							". chart3="+chartPanels[2]+". chart4="+chartPanels[3]);	
					chartDisplay_chart2a.master=chartPanels[0];
					chartDisplay_chart2b.master=chartPanels[1];
					chartDisplay_chart2c.master=chartPanels[2];
					chartDisplay_chart2d.master=chartPanels[3];
					for (int i=1; i<5; i++) {
						allEconomies[i].updateDisplays();
					}
					gui.updateDisplays();
					gui.repaint();
					return true;
				}
			}
			return false;
		}
		
		public boolean updateCharts_oneEconomy(String text) { // The return indicates whether the corresponding menu item was found
			for (int chartNumber: activatedMenuItems_TimeSeries) {
				if (text == m_TimeSeries_items[chartNumber]) {
					rotateCharts();
					ChartPanel chart=activeEconomy.chartsMenu_TimeSeries[chartNumber];
					chart.update();
					chartDisplay_chart2a.master=activeEconomy.chartsMenu_TimeSeries[chartNumber];
					chart_m_active1=m_TimeSeries_menuItems[chartNumber];
					activeEconomy.updateDisplays();
					gui.updateDisplays();
					gui.repaint();
					updateChartsMenu();
					return true;
				}				
			}
			for (int chartNumber: activatedMenuItems_Scatterplots) {
				if (text == m_Scatterplots_items[chartNumber]) {
					rotateCharts();
					ChartPanel chart=activeEconomy.chartsMenu_Scatterplots[chartNumber];
					chart.update();
					chartDisplay_chart2a.master=activeEconomy.chartsMenu_Scatterplots[chartNumber];
					chart_m_active1=m_Scatterplots_menuItems[chartNumber];
					activeEconomy.updateDisplays();
					gui.updateDisplays();
					gui.repaint();
					updateChartsMenu();
					return true;
				}				
			}	
			return false;
		}
		
		public void rotateCharts() {
			// The ChartPanel objects serve as containers for the charts. The chartsMenu array contains 
			// "originals" of all the charts, which can then be displayed or project using chart2a, chart2b, etc.
			// Those ChartPanel objects are the panes of the slide.
			chartDisplay_chart2d.master=chartDisplay_chart2c.master;
			chartDisplay_chart2c.master=chartDisplay_chart2b.master;
			chartDisplay_chart2b.master=chartDisplay_chart2a.master;
			chartDisplay_chart2d.update();
			chartDisplay_chart2c.update();
			chartDisplay_chart2b.update();
			chart_m_active4=chart_m_active3;
			chart_m_active3=chart_m_active2;
			chart_m_active2=chart_m_active1;
		}
		
		public void updateChartsMenu() {
			for (JCheckBoxMenuItem item: m_TimeSeries_menuItems) {
				item.setSelected(false);
			}
			for (JCheckBoxMenuItem item: m_Scatterplots_menuItems) {
				item.setSelected(false);
			}
			if (chart_m_active1!=null) chart_m_active1.setSelected(true);
			if (chart_m_active2!=null) chart_m_active2.setSelected(true);
			if (chart_m_active3!=null) chart_m_active3.setSelected(true);
			if (chart_m_active4!=null) chart_m_active4.setSelected(true);
		}
	}
	
	public class ButtonListener implements ActionListener {
		GuiFrame gui;
		
		public ButtonListener(GuiFrame g) {
			gui=g;
		}
		
		// All buttons have the same Listener, which distinguishes between them by checking their text.
		public synchronized void actionPerformed(ActionEvent event) {
			boolean testing=false;
			JButton button=(JButton) event.getSource();
			String text=button.getText();
			if (text=="Copy Economy 1") {
				if (mode==0) {
					EconomyDisplayPanels keep=activeEconomy;
					setMode(1);
					allEconomies[1]=keep;
					activeEconomy=allEconomies[1];
					if (activeEconomy.economy!=null) activeEconomy.economy.stop=true;
					activeEconomy.economy=null;
				} else {
					activeEconomy.uFunction=allEconomies[1].uFunction.clone();
					activeEconomy.pFunction=allEconomies[1].pFunction.clone();
					activeEconomy.numGoods=allEconomies[1].numGoods.intValue();
					activeEconomy.numAgents=allEconomies[1].numAgents.intValue();
					activeEconomy.parameters=allEconomies[1].parameters.clone();					
				}
				updateEditability();
				technology_chart2a.update();
				technology_table2c.update();
				technology_table2d.update();
				gui.updateDisplays();
			}
			if (text=="Run") {
				if (mode==0) {
					if (activeEconomy.economy.isAlive()==false) activeEconomy.economy.start();					
				}
				if (mode==1) {
					if (fourEconomiesThread.isAlive()==false) fourEconomiesThread.start();
				}
				paused=false;
				running=true;
				m_Mode.setEnabled(false);
				button_run.setForeground(Color.RED);
				button_pause.setForeground(Color.BLACK);
				field_numAgents.setEditable(false);
				field_numGoods.setEditable(false);
//				agentDetail_table2c.input.setEditable(false);
//				retailerDetail_table2c.input.setEditable(false);
				field_numAgents.setText(""+activeEconomy.economy.population.size());
				field_numGoods.setText(""+activeEconomy.economy.numGoods);
				if (panel2==technology) {
					substitutePanel2(chartDisplay);
				}
				int industry=(int) (2+Math.random()*(activeEconomy.economy.numGoods-2));
				activeEconomy.industryDetail_table2b.update(industry);
				gui.repaint();
			}
			if (text=="Pause") {
				pause();
				button_new.setEnabled(true);
				return;
			}
			if (text=="New") {
				gui.resetEconomy();
				button_new.setEnabled(false);
				technology_table2c.button.ready();
				m_Mode.setEnabled(true);
			}
		}	
	}
	
	public void resetEconomy() {
		paused=false;
		running=false;
		button_run.setForeground(Color.BLACK);
		button_pause.setForeground(Color.BLACK);
		button_run.setEnabled(false);
		button_pause.setEnabled(false);
		field_numGoods.setEditable(true);
		field_numAgents.setEditable(true);
		field_turn.setText("");
		label_turn.setText("");
		substitutePanel2(technology); // Redundant since this button will be disabled when other panes are up, but just in case
		if (mode==0) {
			if (activeEconomy.economy!=null) {
				activeEconomy.economy.stop=true; // This kills the Thread
				activeEconomy.economy=null;
			}
		}
		if (mode==1) {
			if (fourEconomiesThread!=null) fourEconomiesThread.stop=true;
			fourEconomiesThread=null;
			// CODE BLOCK NEEDS TO COME HERE WHEN THE FOUR ECONOMIES MODE IS DEVELOPED (I THINK): THE BELOW MAY NOT BE SUITABLE
			for (int i=1; i<5; i++) {
				if (allEconomies[i].economy!=null) {
					allEconomies[i].economy.stop=true;
					allEconomies[i].economy=null;
				}
			}
		}
		technology_table2d.button.setEnabled(true);
		updateDisplays();	
	}

	public void makeEconomy() {
		activeEconomy.uFunction.sigma=activeEconomy.parameters.SIGMA;
		Economy e=null;
		try {
			e=new Economy(activeEconomy.numAgents, activeEconomy.uFunction, activeEconomy.pFunction, activeEconomy.parameters);
		} catch(Exception ex) {ex.printStackTrace();System.exit(1);}
		makeEconomy(e);
	}
	
	public void makeEconomy(Economy e) {
		if (e==null) return;
		if (activeEconomy.economy!=null) {
//			activeEconomy.economy.alive=false;
			activeEconomy.economy.stop=true;			
		}
		activeEconomy.buildDisplays(e); // Now "economy" refers to the new economy
//		activeEconomy.economy.gui=this;
		technology_table2d.button.setEnabled(false);
		activeEconomy.economy.stop=false;
		technology_table2d.button.setEnabled(false);
		technology_table2c.button.finished();
		technology_table2d.button.setEnabled(false);
		field_numGoods.setEditable(false);
		field_numAgents.setEditable(false);
		activeEconomy.buildDisplays(e);
		updateDisplays();	
		int agentDetailFeature=(int) (Math.random()*activeEconomy.economy.population.size());
		Agent agent=activeEconomy.economy.population.get(agentDetailFeature);
		activeEconomy.agentDetail_table2c.update(agent);
		activeEconomy.uFunction=activeEconomy.economy.utilityFunction;
		activeEconomy.pFunction=activeEconomy.economy.productionFunction;
		activeEconomy.parameters=activeEconomy.economy.parameters;
//		activeEconomy.utilityFunction=activeEconomy.economy.utilityFunction;
//		activeEconomy.productionFunction=economy.productionFunction;
		readyToRun();
		if (mode==0) defaultCharts();
//		substitutePanel2(technology); // Redundant since this button will be disabled when other panes are up, but just in case
	}
	
	public void readyToRun() {
		if (mode==0) {
			if (activeEconomy.economy!=null) {
//				activeEconomy.economy.start(); // While the thread is started, since running is set to false, nothing actually happens.
				button_run.setEnabled(true);
				button_pause.setEnabled(true);							
			}
		}
		if (mode==1) {
			boolean ready=true;
			for (int i=1; i<5; i++) {
				if (allEconomies[i].economy==null) {
					ready=false;
				}
			}
			if (ready==true) {
				button_run.setEnabled(true);
				button_pause.setEnabled(true);		
				fourEconomiesThread=new FourEconomiesThread(this);				
			}
		}
	}
	
	public void pause() {
		paused=true;
		running=false;
		button_run.setForeground(Color.BLACK);
		button_pause.setForeground(Color.RED);
		field_numAgents.setEditable(true);
		field_numGoods.setEditable(true);
//		agentDetail_table2c.input.setEditable(true);
//		retailerDetail_table2c.input.setEditable(true);
	}
}
