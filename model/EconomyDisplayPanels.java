package model;

import java.awt.Color;
import java.awt.GridLayout;
import java.util.ArrayList;

import javax.swing.JPanel;

import model.AvoidCostPF;
import model.Parameters;
import model.UtilityFunction;

public class EconomyDisplayPanels {
	Economy economy;
	public GuiFrame gui;
	boolean active;
	// Information to be used in construction
	Integer numGoods;
	Integer numAgents;
	Double sigma;
	UtilityFunction uFunction;
	AvoidCostPF pFunction;
	Parameters parameters;
	ChartPanel[] chartsMenu_TimeSeries;
	ChartPanel[] chartsMenu_Scatterplots;

	// Four main panels to be substituted into panel2 in the GUI
	JPanel retailerDetail;
	JPanel industryDetail;
	JPanel agentDetail;
	JPanel census;
	// Chart Display pane items
	ChartPanel chartDisplay_chart2a;
	ChartPanel chartDisplay_chart2b;
	ChartPanel chartDisplay_chart2c;
	ChartPanel chartDisplay_chart2d;
	// The Agent Detail view fields
	ChartPanel agentDetail_chart2a;
	ChartPanel agentDetail_chart2b;
	TablePanel agentDetail_table2c;
	TablePanel agentDetail_table2d;
	// The Retailer Detail view fields
	ChartPanel retailerDetail_chart2a;
	ChartPanel retailerDetail_chart2b;
	public TablePanel retailerDetail_table2c;
	TablePanel retailerDetail_table2d;
	// The Census view fields
	TablePanel census_agentPanel;
	TablePanel census_retailerPanel;
	// The Industry Detail view fields
	ChartPanel industryDetail_chart2a;
	TablePanel industryDetail_table2b;
	TablePanel industryDetail_table2c;
	TablePanel industryDetail_table2d;
	ArrayList<JPanel> allPanels;

	public EconomyDisplayPanels(GuiFrame g, Economy e) {
		this(g);
		buildDisplays(e);
	}
	
	public EconomyDisplayPanels(GuiFrame g) {
		gui=g;
		numGoods=20; // Default
		numAgents=200; // Default
		pFunction=new AvoidCostPF(numGoods);
		uFunction=new UtilityFunction(numGoods);
		parameters=Parameters.defaultValues();
		allPanels=new ArrayList<JPanel>();
		chartsMenu_TimeSeries=new ChartPanel[gui.m_TimeSeries_items.length];
		int count=0;
		for (String s: gui.m_TimeSeries_items) {
			ChartPanel chart=new ChartPanel(s,this);
//			chart.getMenuItem().addActionListener(gui.chartsMenuListener);
//			m_TimeSeries.add(chart.getMenuItem());
			chartsMenu_TimeSeries[count]=chart;
			count++;
		}
		chartsMenu_Scatterplots=new ChartPanel[gui.m_Scatterplots_items.length];
		count=0;
		for (String s: gui.m_Scatterplots_items) {
			ChartPanel chart=new ChartPanel(s,this);
//			chart.getMenuItem().addActionListener(gui.chartsMenuListener);
//			m_TimeSeries.add(chart.getMenuItem());
			chartsMenu_Scatterplots[count]=chart;
			count++;
		}
	}

	public void buildDisplays(Economy e) {
		economy=e;
		pFunction=e.productionFunction;
		uFunction=e.utilityFunction;
		parameters=e.parameters;
		numGoods=e.numGoods;
		numAgents=e.population.size();
		sigma=uFunction.sigma;
		e.display=this; // Create the link between the economy and the display
		makeAgentDetailPane();
		makeRetailerDetailPane();	
		makeCensusPane();
		makeIndustryDetailPane();
		updateDisplays();
	}	
	
	public void makeAgentDetailPane() {
		// Agent detail
		agentDetail=new JPanel(new GridLayout(2,2));
		agentDetail.setBackground(Color.WHITE);
		agentDetail_chart2a=new ChartPanel("Specialization: production and consumption", this);
//		agentDetail_chart2a.agent=economy.population.get(0);
		agentDetail_chart2b=new ChartPanel("Agent: utility vs. money", this);
//		agentDetail_chart2b.agent=economy.population.get(0);
		agentDetail_table2c=new TablePanel("Consumption, Production, by Good for AGENT ID ","Agent ID",this,true);
		agentDetail_table2d=new TablePanel("Retailers","Agent retailer list",this,true);
		agentDetail_table2d.input.setEditable(false);
		agentDetail.add(agentDetail_chart2a);
		agentDetail.add(agentDetail_chart2b);
		agentDetail.add(agentDetail_table2c);
		agentDetail.add(agentDetail_table2d);
		allPanels.add(agentDetail_chart2a);
		allPanels.add(agentDetail_chart2b);
		allPanels.add(agentDetail_table2c);
		allPanels.add(agentDetail_table2d);
//		agentDetail_table2c.update(economy.population.get(0));		
	}
	
	public void makeRetailerDetailPane() {
		// Make the Retailer Detail pane
		retailerDetail=new JPanel(new GridLayout(2,2));
		retailerDetail_chart2a=new ChartPanel("Retailer: price and volume", this); // Two axes: base price and income
		retailerDetail_chart2b=new ChartPanel("Retailer: short-run inventory dynamics", this); // Transaction by transaction
		retailerDetail_table2d=new TablePanel("Inventory Dynamics", "Retailer 2d", this, true);
		retailerDetail_table2c=new TablePanel("Retailer: price and volume", "Retailer 2c", this, true);
		retailerDetail_table2d.input.setEditable(false);
		retailerDetail.add(retailerDetail_chart2a);
		retailerDetail.add(retailerDetail_chart2b);
		retailerDetail.add(retailerDetail_table2c);
		retailerDetail.add(retailerDetail_table2d);		
		allPanels.add(retailerDetail_chart2a);
		allPanels.add(retailerDetail_chart2b);
		allPanels.add(retailerDetail_table2c);
		allPanels.add(retailerDetail_table2d);
	}

	public void makeCensusPane() {
		// Make the Census pane
		census=new JPanel(new GridLayout(2,1));
		census_agentPanel=new TablePanel("Census: Agents", "Census: Agents", this, true);
		census_retailerPanel=new TablePanel("Census: Retailers", "Census: Retailers", this, true);
		census.add(census_agentPanel);
		census.add(census_retailerPanel);
		census_agentPanel.input.setEditable(false);
		census_retailerPanel.input.setEditable(false);
		allPanels.add(census_agentPanel);
		allPanels.add(census_retailerPanel);
	}
	
	public void makeIndustryDetailPane() {
		// Make the Industry Detail pane
		industryDetail=new JPanel(new GridLayout(2,2));
		industryDetail_chart2a=new ChartPanel("Industry price history", this);
		industryDetail_table2b=new TablePanel("Industry: historical profile", "Industry: historical profile", this, true);
		industryDetail_table2b.input.setEditable(true);
		industryDetail_table2c=new TablePanel("Industry: retailers", "Industry: retailers", this, true);
		industryDetail_table2c.input.setEditable(false);
		industryDetail_table2d=new TablePanel("Industry: population", "Industry: population", this, true);
		industryDetail_table2d.input.setEditable(false);
		industryDetail.add(industryDetail_chart2a);
		industryDetail.add(industryDetail_table2b);
		industryDetail.add(industryDetail_table2c);
		industryDetail.add(industryDetail_table2d);
		allPanels.add(industryDetail_chart2a);
		allPanels.add(industryDetail_table2b);
		allPanels.add(industryDetail_table2c);
		allPanels.add(industryDetail_table2d);
	}
	
	public void updateDisplays() {
		boolean testing=false;
		if (economy==null) return; // Can't display anything without an economy to get data from.
/*		if (active) {
			if (economy!=null) {
				int[] byGoodMenuItems={1, 2, 6, 7, 8};
				for (int i: byGoodMenuItems) { // The menus that are "by good" become embarrassingly unreadable when g>20
					gui.chartsMenu_TimeSeries[i].getMenuItem().setEnabled(economy.numGoods<=20);
				}				
			}			
		} */
		if (agentDetail_table2c.agent==null) {
			int selector=(int) (Math.random()*economy.population.size());
			agentDetail_table2c.agent=economy.population.get(selector);
		}
		agentDetail_chart2a.agent=agentDetail_table2c.agent;
		agentDetail_chart2b.agent=agentDetail_table2c.agent;
		agentDetail_table2d.agent=agentDetail_table2c.agent;
		agentDetail_chart2a.update();
		agentDetail_chart2b.update();
		if (testing) {
			System.out.println("TESTING GuiFrame.updateCharts(). retailerDetail_table2c is null: "+(retailerDetail_table2c==null));
			System.out.println("TESTING GuiFrame.updateCharts(). retailerDetail_table2c.retailer is null: "
					+(retailerDetail_table2c.retailer==null));
			System.out.println("TESTING GuiFrame.updateCharts(). retailerDetail_table2d.retailer is null: "
					+(retailerDetail_table2d.retailer==null));
			System.out.println("Economy is at turn "+economy.turn);
		}
		retailerDetail_table2c.update();
		if (retailerDetail_table2c.retailer!=null) {
			retailerDetail_chart2a.retailer=retailerDetail_table2c.retailer;
			retailerDetail_chart2b.retailer=retailerDetail_table2c.retailer;
			retailerDetail_table2d.retailer=retailerDetail_table2c.retailer;
			retailerDetail_chart2a.update();
			retailerDetail_chart2b.update();			
		}
		census_agentPanel.update();
		census_retailerPanel.update();
		if (industryDetail_table2b.industry==null) {
			int industry=(int) (Math.random()*(economy.numGoods-2)+2);
			industryDetail_table2b.industry=industry;
			industryDetail_chart2a.industry=industry;
			industryDetail_table2c.industry=industry;
			industryDetail_table2d.industry=industry;
		}
		industryDetail_chart2a.update();
		industryDetail_table2b.update();
		industryDetail_table2c.update();
		industryDetail_table2d.update();
		for (JPanel panel: allPanels) {
			panel.revalidate();
			panel.repaint();
		}
		retailerDetail.revalidate();
		retailerDetail.repaint();
		agentDetail.revalidate();
		agentDetail.repaint();
		census.revalidate();
		census.repaint();
		industryDetail.revalidate();
		industryDetail.repaint();
		gui.updateDisplays();
		gui.validate();
		gui.repaint();
		for (ChartPanel chart: chartsMenu_TimeSeries) {
			chart.update();
		}
		for (ChartPanel chart: chartsMenu_Scatterplots) {
			chart.update();
		}
	}

}

