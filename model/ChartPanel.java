package model;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.*;

import org.jfree.chart.*;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYDifferenceRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.TimeSeriesDataItem;
import org.jfree.data.xy.*;


public class ChartPanel extends JPanel {
	EconomyDisplayPanels display;
//	private JCheckBoxMenuItem menuItem;
	private JFreeChart chart;
	String chartType;
	ChartPanel master;
	Image image;
	public Agent agent; // Some charts need to have the address of a particular agent, from which they derive their data.
	public Retailer retailer; // Others derive their data from a retailer.
	Integer industry;
	String economyMarker;
	
	public ChartPanel(String type, EconomyDisplayPanels e) {
		chartType=type;
//		menuItem=new JCheckBoxMenuItem(type);
		display=e;
		update();
	}
	
	public void paintComponent(Graphics g) {
		if (chart!=null) {
			image=chart.createBufferedImage(500, 250);			
			g.drawImage(image, 5, 5, this);			
		}
	}
	
/*	public JCheckBoxMenuItem getMenuItem() {
		return menuItem;
	}
	
	public void setMenuItem(JCheckBoxMenuItem item) {
		menuItem=item;
	}*/
	
	public JFreeChart getChart() {
		return chart;
	}
	
	public void setChart(JFreeChart newChart) {
		chart=newChart;
		repaint();
	}
	
/*	public void update(EconomyDisplayPanels d) {
		display=d;
		update();
	}*/
	
	public void update() {
		if (master!=null) {
			chart=master.chart;
			return;
		}
		boolean testing=false;
		EconomyDisplayPanels d=display;
		GuiFrame gui=d.gui;
		EconomyDisplayPanels activeEconomy=gui.activeEconomy;
		if (activeEconomy==display) display.active=true;
		if (testing) System.out.println("TESTING ChartPanel.update(). chart type="+chartType);
		if (chartType=="Technology: goods space") {
			if (display.active&(display.gui.technology_table2c!=null)) update_techGoodsSpaceViewer();
		}
		if (display.economy==null) return; // Can't show anything if there's no economy.
		if (chartType=="Median and average utility") {
			update_medAvgUtil();
		}
		if (chartType=="GDP: Demand and Supply Side") {
			update_gdpDemandSupply();
		}
		if (chartType=="Cash: Agents and Retailers") {
			update_cashAgentRetailer();
		}
		if (chartType=="Historical sales") {
			update_histSales();
		}
		if (chartType=="Historical prices") {
			update_histPrices();
		}
		if (chartType=="Labor by Overhead/Self-Supply/Market") {
			update_laborByOSM();
		}
		if (chartType=="Labor by Good") {
			update_laborByGood();
		}
		if (chartType=="Consumption by Good") {
			update_consByGood();
		}
		if (chartType=="Retailer Access by Good") {
			update_retailerAccess();
		}
		if (chartType=="Consumables and Tradables") {
			update_consumablesTradables();
		}
		if (chartType=="Goods space") {
			update_goodsSpace();
		}
		if (chartType=="Diversification, Make vs. Use") {
			update_diversifyMakeUse();
		}
		if (chartType=="Avg. utility: Agents vs. Retailers") {
			update_avgURetNRet();
		}
		if (chartType=="Industries in P/Q space") {
			update_industriesPQ();
		}
		if (chartType=="Industries in P/Q space (moving avg.") {
			update_industriesPQm_avg();
		}
		if (chartType=="Industries in P/Q space (snake)") {
			update_industriesPQsnake();
		}
		if (chartType=="GDP supply: profits and wages") {
			update_gdpSupplyBreakdown();
		}
		if (chartType=="Price-to-overhead ratio") {
			update_pL0ratio();
		}
		if (chartType=="Specialization: production and consumption") {
			update_agentConsProd();
		}
		if (chartType=="Retailer: price and volume") {
			update_retailerPV();
		}
		if (chartType=="Retailer: short-run inventory dynamics") {
			update_retailerTH();
		}
		if (chartType=="Capital: retailers and non-retailers") {
			update_capitalRetNRet();
		}
		if (chartType=="Count: retailers and non-retailers") {
			update_countRetNRet();
		}
		if (chartType=="Industry price history") {
			update_industryPriceHistory();
		}
		if (chartType=="Industries: retailers and specialist producers") {
			update_industriesRW();
		}
		if (chartType=="Industries: consumers and producers") {
			update_industriesCP();
		}
		if (chartType=="Agents in M/U space") {
			update_agentsMU();
		}
		if (chartType=="Agent: utility vs. money") {
			update_agentMU();
		}
	}
	
	public void update_medAvgUtil() {
		if (display.economy.averageUtility!=null) {
			ArrayList<Double> avgUtil=display.economy.averageUtility;
			ArrayList<Double> medUtil=display.economy.medianUtility;
			double[][] avg=new double[avgUtil.size()][2];
			double[][] med=new double[medUtil.size()][2];
			double maxU=0;
			for (int i=0; i<avg.length; i++) {
				avg[i][0]=i;
				avg[i][1]=avgUtil.get(i);
				if (avg[i][1]>maxU) maxU=avg[i][1];
				med[i][0]=i;
				med[i][1]=medUtil.get(i);
				if (med[i][1]>maxU) maxU=med[i][1];
			}
			String title="Median and average utility";
			if (economyMarker!=null) title=title+economyMarker;
			chart=differenceChart(title, // title
					"Time", // xLabel,
					"Utility", // yLabel,
					"Avg. utility", // label1,
					"Med. utility", // label2,
					avg, // first dataset
					med, // second dataset
					0, // minimum on the vertical axis
					(maxU*1.05) // top of the axis
					);					
		}		
	}
	
	public void update_gdpDemandSupply() {
		if (display.economy.averageUtility!=null) {
			ArrayList<Double> gdpDemand=display.economy.gdpDemandSide;
			ArrayList<Double> gdpSupply=display.economy.gdpSupplySide;
			double[][] qD=new double[gdpDemand.size()][2];
			double[][] qS=new double[gdpSupply.size()][2];
			ArrayList<Double> topFive=new ArrayList<Double>();
			for (int i=0; i<qD.length; i++) {
				qD[i][0]=i;
				qD[i][1]=gdpDemand.get(i);
				qS[i][0]=i;
				qS[i][1]=gdpSupply.get(i);
				double larger=Math.max(qD[i][1],qS[i][1]);
				if (qD.length-i<500) {
					if (topFive.size()<5) topFive.add(larger);
					else {
						Double minTopFive=topFive.get(0);
						for (int j=1; j<5; j++) {
							if (topFive.get(j)<minTopFive) minTopFive=topFive.get(j);
						}
						if (larger>minTopFive) {
							topFive.remove(minTopFive);
							topFive.add(larger);
						}
					}							
				}
			}
			Double minTopFive=topFive.get(0);
			for (int j=1; j<topFive.size(); j++) {
				if (topFive.get(j)<minTopFive) minTopFive=topFive.get(j);
			}					
			String title="Two Measures of GDP";
			if (economyMarker!=null) title=title+economyMarker;
			chart=differenceChart(title, // title
					"Time", // xLabel,
					"Dollars of transactions / period", // yLabel,
					"GDP: demand side", // label1,
					"GDP: supply side", // label2,
					qD, // first dataset
					qS, // second dataset
					0, // minimum on the vertical axis
					(minTopFive*1.05) // set the top of the axis to the fourth-most-recent max of GDP
					);					
		}		
	}
	
	public void update_retailerPV() {
		if (retailer==null) return;
		if (retailer.salesPriceHistory.size()>0) {
			XYSeriesCollection dataset1=new XYSeriesCollection();
			XYSeries s1=new XYSeries("Sales Price");
			for (int j=0; j<retailer.salesPriceHistory.size(); j++) {
				Double salesPrice=retailer.salesPriceHistory.get(j);
				if ((salesPrice>0)==false) salesPrice=0.0;
				double[] point={j, salesPrice};
				s1.add(j, salesPrice);
			}				
			dataset1.addSeries(s1);
			XYSeries s2=new XYSeries("Revenue");
			XYSeriesCollection dataset2=new XYSeriesCollection();
			for (int j=0; j<retailer.incomeHistory.size(); j++) {
				Double income=retailer.incomeHistory.get(j);
				if ((income>0)==false) income=0.0;
				s2.add(j, income);
			}				
			dataset2.addSeries(s2);
			String title="Retailer: Price vs. Revenue";
			if (economyMarker!=null) title=title+economyMarker;
	        chart = ChartFactory.createXYLineChart(title,"Time","Sales Price",dataset1,PlotOrientation.VERTICAL,
	                true,true,false);		
			XYPlot plot=(XYPlot) chart.getPlot();
			plot.setDomainPannable(true);
			plot.setRangePannable(true);
	        XYLineAndShapeRenderer renderer1 = new XYLineAndShapeRenderer();
	        XYLineAndShapeRenderer renderer2 = new XYLineAndShapeRenderer();
        	renderer1.setSeriesLinesVisible(0, true);
        	renderer1.setSeriesShapesVisible(0, false);
	        renderer1.setSeriesPaint(0, Color.BLUE);
        	renderer2.setSeriesLinesVisible(0, true);
        	renderer2.setSeriesShapesVisible(0, false);
	        renderer2.setSeriesPaint(0, Color.RED);
	        chart.setBackgroundPaint(Color.white);
	        chart.getPlot().setBackgroundPaint(Color.white);
	        plot.setRenderer(0, renderer1);	   
	        plot.setRenderer(1, renderer2);	   
	        NumberAxis axis2 = new NumberAxis("Revenue");
	        plot.setRangeAxis(1,axis2);
	        plot.setRangeAxisLocation(1,AxisLocation.BOTTOM_OR_RIGHT);
	        plot.setDataset(1,dataset2);
	        plot.mapDatasetToRangeAxis(1, 1);
		}
	}
	
	public void update_agentMU() {
		if (agent==null) return;
		if (agent.utilityHistory.size()>0) {
			XYSeriesCollection dataset1=new XYSeriesCollection();
			XYSeries s1=new XYSeries("Utility");
			for (int j=0; j<agent.utilityHistory.size(); j++) {
				Double u=agent.utilityHistory.get(j);
				if ((u>0)==false) u=0.0;
				double[] point={j, u};
				s1.add(j, u);
			}				
			dataset1.addSeries(s1);
			XYSeries s2=new XYSeries("Money");
			XYSeriesCollection dataset2=new XYSeriesCollection();
			for (int j=0; j<agent.moneyHistory.size(); j++) {
				Double cash=agent.moneyHistory.get(j);
				if ((cash>0)==false) cash=0.0;
				s2.add(j, cash);
			}				
			dataset2.addSeries(s2);
			String title="Agent: Money and utility";
			if (economyMarker!=null) title=title+economyMarker;
	        chart = ChartFactory.createXYLineChart(title,"Time","Utility",dataset1,PlotOrientation.VERTICAL,
	                true,true,false);		
			XYPlot plot=(XYPlot) chart.getPlot();
			plot.setDomainPannable(true);
			plot.setRangePannable(true);
	        XYLineAndShapeRenderer renderer1 = new XYLineAndShapeRenderer();
	        XYLineAndShapeRenderer renderer2 = new XYLineAndShapeRenderer();
        	renderer1.setSeriesLinesVisible(0, true);
        	renderer1.setSeriesShapesVisible(0, false);
	        renderer1.setSeriesPaint(0, Color.BLUE);
        	renderer2.setSeriesLinesVisible(0, true);
        	renderer2.setSeriesShapesVisible(0, false);
	        renderer2.setSeriesPaint(0, Color.RED);
	        chart.setBackgroundPaint(Color.white);
	        chart.getPlot().setBackgroundPaint(Color.white);
	        plot.setRenderer(0, renderer1);	   
	        plot.setRenderer(1, renderer2);	   
	        NumberAxis axis2 = new NumberAxis("Money");
	        plot.setRangeAxis(1,axis2);
	        plot.setRangeAxisLocation(1,AxisLocation.BOTTOM_OR_RIGHT);
	        plot.setDataset(1,dataset2);
	        plot.mapDatasetToRangeAxis(1, 1);
		}
	}

	public void update_retailerTH() {
		if (retailer==null) return;
		if (retailer.lastTurnTransactions==null) return;
		XYSeriesCollection dataset1=new XYSeriesCollection();
		XYSeriesCollection dataset2=new XYSeriesCollection();
		XYSeries s1=new XYSeries("Cash");
		XYSeries s2=new XYSeries("Inventory");
		for (int j=0; j<retailer.lastTurnTransactions.size(); j++) {
			Transaction t=retailer.lastTurnTransactions.get(j);
			double cashLeft=t.payQtyLeft; // for consumer transactions
			double inventoryLeft=t.sellQtyLeft;
			if (t.sell==retailer.r_money) {
				cashLeft=t.sellQtyLeft;
				inventoryLeft=t.payQtyLeft;
			}
			s1.add(j, cashLeft);
			s2.add(j, inventoryLeft);
		}				
		dataset1.addSeries(s1);
		dataset2.addSeries(s2);
		String title="Retailer: short-term inventory dynamics";
		if (economyMarker!=null) title=title+economyMarker;
        chart = ChartFactory.createXYLineChart(title,"Time","Cash",dataset1,PlotOrientation.VERTICAL,
                true,true,false);		
		XYPlot plot=(XYPlot) chart.getPlot();
		plot.setDomainPannable(true);
		plot.setRangePannable(true);
        XYLineAndShapeRenderer renderer1 = new XYLineAndShapeRenderer();
        XYLineAndShapeRenderer renderer2 = new XYLineAndShapeRenderer();
    	renderer1.setSeriesLinesVisible(0, true);
    	renderer1.setSeriesShapesVisible(0, false);
        renderer1.setSeriesPaint(0, Color.BLUE);
    	renderer2.setSeriesLinesVisible(0, true);
    	renderer2.setSeriesShapesVisible(0, false);
        renderer2.setSeriesPaint(0, Color.RED);
        chart.setBackgroundPaint(Color.white);
        chart.getPlot().setBackgroundPaint(Color.white);
        plot.setRenderer(0, renderer1);	   
        plot.setRenderer(1, renderer2);	   
        NumberAxis axis2 = new NumberAxis("Inventory");
        plot.setRangeAxis(1,axis2);
        plot.setRangeAxisLocation(1,AxisLocation.BOTTOM_OR_RIGHT);
        plot.setDataset(1,dataset2);
        plot.mapDatasetToRangeAxis(1, 1);
	}

	public void update_cashAgentRetailer() {
		if (display.economy.averageUtility!=null) {
			ArrayList<Double> retailerCash=display.economy.totRetailerCash;
			ArrayList<Double> agentCash=display.economy.totAgentCash;
			double[][] rC=new double[retailerCash.size()][2];
			double[][] aC=new double[agentCash.size()][2];
			double max=0;
			for (int i=0; i<rC.length; i++) {
				rC[i][0]=i;
				rC[i][1]=retailerCash.get(i);
				aC[i][0]=i;
				aC[i][1]=agentCash.get(i);
				if (rC[i][1]+aC[i][1]>max) max=rC[i][1]+aC[i][1];
			}
			String title="Cash in the Hands of Agents vs. Retailers";
			if (economyMarker!=null) title=title+economyMarker;
			chart=differenceChart(title, // title
					"Time", // xLabel,
					"Dollars", // yLabel,
					"Cash Held by Retailers", // label1,
					"Cash Held by Agents", // label2,
					rC, // first dataset
					aC, // second dataset
					0, // minimum on the vertical axis
					(max*1.05) // top of the axis
					);					
		}		
	}
	
	public void update_histSales() {
		if (display.economy.historicalSales!=null) {
			ArrayList<Double[]> historicalSales=display.economy.historicalSales;
			ArrayList<Integer> includableGoods=new ArrayList<Integer>();
			for (int i=1; i<historicalSales.get(0).length; i++) { // For each good...
				boolean include=false; // ... initially assume the good is NOT included...
				for (Double[] series: historicalSales) { // ... then search the data...
					if (series[i]>0) include=true; // ... and if there is ANY non-null price for good i...
				}
				if (include) includableGoods.add(i); // include it in the displayable price series
			}
			int g=includableGoods.size();
			String[] seriesNames=new String[g];
			boolean[] showShapes=new boolean[g];
			boolean[] showLines=new boolean[g];
			for (int i=0; i<g; i++) {
				seriesNames[i]="Good "+includableGoods.get(i);
				showShapes[i]=false;
				showLines[i]=true;
			}
			ArrayList<double[][]> data=new ArrayList<double[][]>();
			for (int i=0; i<g; i++) { // Goods (but indexed with the help of includableGoods)
				ArrayList<double[]> series_prep=new ArrayList<double[]>(); // start this as an ArrayList because we don't know it's length
				for (int j=Math.max(0, historicalSales.size()-100); j<historicalSales.size(); j++) { // Periods
					Double y=historicalSales.get(j)[includableGoods.get(i)];
					double[] point=new double[2];
					point[0]=j;
					point[1]=y;
					series_prep.add(point);								
				}
				double[][] series=new double[series_prep.size()][2];
				for (int j=0; j<series_prep.size(); j++) {
					series[j]=series_prep.get(j);
				}
				data.add(series);
			}
			String title="Historical Sales";
			if (economyMarker!=null) title=title+economyMarker;
			chart=lineAndShapeChart(
					title,	// String title,
					"Time",	// String xAxis,
					"Units",	// String yAxis,
					seriesNames,	// String[] seriesNames,
					data,	// ArrayList<double[][]> data,
					showShapes, 	// boolean[] showShapes,
					showLines,	// boolean[] showLines,
					null	// java.awt.Paint[] pallet
					);
		}		
	}
	
	public synchronized void update_histPrices() {
		if (display.economy.historicalPrices!=null) {
			ArrayList<Double[]> historicalPrices=display.economy.historicalPrices;
			ArrayList<Integer> includableGoods=new ArrayList<Integer>();
			for (int i=1; i<historicalPrices.get(0).length; i++) { // For each good...
				boolean include=false; // ... initially assume the good is NOT included...
				for (Double[] series: historicalPrices) { // ... then search the data...
					if (series[i]!=null) include=true; // ... and if there is ANY non-null price for good i...
				}
				if (include) includableGoods.add(i); // include it in the displayable price series
			}
			int g=includableGoods.size();
			String[] seriesNames=new String[g];
			for (int i=0; i<g; i++) {
				seriesNames[i]="Good "+includableGoods.get(i);
			}
			ArrayList<double[][]> data=new ArrayList<double[][]>();
			for (int i=0; i<g; i++) { // Goods (but indexed with the help of includableGoods)
				ArrayList<double[]> series_prep=new ArrayList<double[]>(); // start this as an ArrayList because we don't know it's length
				for (int j=Math.max(historicalPrices.size()-100,0); j<historicalPrices.size(); j++) { // Periods
					Double y=historicalPrices.get(j)[includableGoods.get(i)];
					if (y!=null) {
						double[] point=new double[2];
						point[0]=j;
						point[1]=y;
						series_prep.add(point);								
					}
				}
				double[][] series=new double[series_prep.size()][2];
				for (int j=0; j<series_prep.size(); j++) {
					series[j]=series_prep.get(j);
				}
				data.add(series);
			}
			String title="Historical Prices";
			if (economyMarker!=null) title=title+economyMarker;
			chart=lineAndShapeChart(
					title,	// String title,
					"Time",	// String xAxis,
					"$ / Unit",	// String yAxis,
					seriesNames,	// String[] seriesNames,
					data,	// ArrayList<double[][]> data,
					false, 	// boolean[] showShapes,
					true,	// boolean[] showLines,
					null	// java.awt.Paint[] pallet
					);
		}		
	}
	
	public void update_laborByOSM() {
		if (display.economy.laborByOSM!=null) {
			ArrayList<Double[][]> laborByOSM=display.economy.laborByOSM;
			int turns=laborByOSM.size();
			int numGoods=laborByOSM.get(0).length; // numGoods is the number of goods; for each good, 0=overhead, 1=self supply, 2=market
			double[][] overhead=new double[turns][2];
			double[][] selfsupply=new double[turns][2];
			double[][] market=new double[turns][2];
			double[][] retailing=new double[turns][2];
			for (int i=0; i<turns; i++) { // For each turn...
				overhead[i][0]=i; // ... record the name of the turn in the first column of each of the series...
				selfsupply[i][0]=i;
				market[i][0]=i;
				retailing[i][0]=i;
				overhead[i][1]=0; // ... then initialize the second column of each of the series at zero...
				selfsupply[i][1]=0;
				market[i][1]=0;
				retailing[i][1]=0;
				Double[][] turn=laborByOSM.get(i); // ... grab the data for the current turn...
				for (int j=2; j<numGoods; j++) {
					overhead[i][1]=overhead[i][1]+turn[j][0]; // ... and good by good, add the qtys of labor to the appropriate series
					selfsupply[i][1]=selfsupply[i][1]+turn[j][1];
					market[i][1]=market[i][1]+turn[j][2];
					retailing[i][1]=retailing[i][1]+turn[j][3];
				}
				selfsupply[i][1]=selfsupply[i][1]+overhead[i][1];
				market[i][1]=market[i][1]+selfsupply[i][1];
				retailing[i][1]=retailing[i][1]+market[i][1];
			}
			String[] seriesNames={"Overhead","+ Self-supply","+ Market","+ Retailing"};
			ArrayList<double[][]> data=new ArrayList<double[][]>();
			data.add(overhead);
			data.add(selfsupply);
			data.add(market);
			data.add(retailing);
			java.awt.Paint[] pallet={Color.GREEN,Color.RED,Color.BLUE,Color.BLACK};
			String title="Capacity Use by Overhead / Self-Supply / Market / Retailing";
			if (economyMarker!=null) title=title+economyMarker;
			chart=lineAndShapeChart(
					title,	// String title,
					"Time",	// String xAxis,
					"Labor",	// String yAxis,
					seriesNames,	// String[] seriesNames,
					data,	// ArrayList<double[][]> data,
					false, 	// boolean[] showShapes,
					true,	// boolean[] showLines,
					pallet	// java.awt.Paint[] pallet
					);
		}		
	}
	
	public void update_laborByGood() {
		if (display.economy.laborByGood!=null) {
			ArrayList<Double[]> laborByGood=display.economy.laborByGood;
			ArrayList<Integer> includableGoods=new ArrayList<Integer>();
			for (int i=1; i<laborByGood.get(0).length; i++) { // For each good...
				boolean include=false; // ... initially assume the good is NOT included...
				for (Double[] series: laborByGood) { // ... then search the data...
					if (series[i]>0) include=true; // ... and if there is ANY non-null price for good i...
				}
				if (include) includableGoods.add(i); // include it in the displayable price series
			}
			int g=includableGoods.size();
			String[] seriesNames=new String[g];
			for (int i=0; i<g; i++) {
				seriesNames[i]="Good "+includableGoods.get(i);
			}
			ArrayList<double[][]> data=new ArrayList<double[][]>();
			for (int i=0; i<g; i++) { // Goods (but indexed with the help of includableGoods)
				ArrayList<double[]> series_prep=new ArrayList<double[]>(); // start this as an ArrayList because we don't know it's length
				for (int j=Math.max(0, laborByGood.size()-100); j<laborByGood.size(); j++) { // Periods
					Double y=laborByGood.get(j)[includableGoods.get(i)];
					double[] point=new double[2];
					point[0]=j;
					point[1]=y;
					series_prep.add(point);								
				}
				double[][] series=new double[series_prep.size()][2];
				for (int j=0; j<series_prep.size(); j++) {
					series[j]=series_prep.get(j);
				}
				data.add(series);
			}
			String title="Labor By Good";
			if (economyMarker!=null) title=title+economyMarker;
			chart=lineAndShapeChart(
					title,	// String title,
					"Time",	// String xAxis,
					"Labor",	// String yAxis,
					seriesNames,	// String[] seriesNames,
					data,	// ArrayList<double[][]> data,
					false, 	// boolean[] showShapes,
					true,	// boolean[] showLines,
					null	// java.awt.Paint[] pallet
					);
		}		
	}
	
	public void update_consByGood() {
		if (display.economy.historicalSales!=null) {
			boolean testing2=false;
			ArrayList<Double[]> consumptionByGood=display.economy.consumptionByGood;
			ArrayList<Integer> includableGoods=new ArrayList<Integer>();
			for (int i=1; i<consumptionByGood.get(0).length; i++) { // For each good...
				boolean include=false; // ... initially assume the good is NOT included...
				for (Double[] series: consumptionByGood) { // ... then search the data...
					if (series[i]>0) include=true; // ... and if there is ANY non-null price for good i...
				}
				if (include) includableGoods.add(i); // include it in the displayable price series
			}
			if (testing2) for (int g: includableGoods) System.out.println("TESTING ChartPanel.update(), chartType=consumption by good. Included good: "+g);
			int g=includableGoods.size();
			String[] seriesNames=new String[g];
			for (int i=0; i<g; i++) {
				seriesNames[i]="Good "+includableGoods.get(i);
			}
			ArrayList<double[][]> data=new ArrayList<double[][]>();
			for (int i=0; i<g; i++) { // Goods (but indexed with the help of includableGoods)
				ArrayList<double[]> series_prep=new ArrayList<double[]>(); // start this as an ArrayList because we don't know it's length
				for (int j=Math.max(0, consumptionByGood.size()-100); j<consumptionByGood.size(); j++) { // Periods
					Double y=consumptionByGood.get(j)[includableGoods.get(i)];
					double[] point=new double[2];
					point[0]=j;
					point[1]=y;
					series_prep.add(point);								
					if (testing2) System.out.println("Added point ("+point[0]+","+point[1]+").");
				}
				double[][] series=new double[series_prep.size()][2];
				for (int j=0; j<series_prep.size(); j++) {
					series[j]=series_prep.get(j);
				}
				data.add(series);
			}
			String title="Consumption By Good";
			if (economyMarker!=null) title=title+economyMarker;
			chart=lineAndShapeChart(
					"Consumption By Good",	// String title,
					"Time",	// String xAxis,
					"Units",	// String yAxis,
					seriesNames,	// String[] seriesNames,
					data,	// ArrayList<double[][]> data,
					false, 	// boolean[] showShapes,
					true,	// boolean[] showLines,
					null	// java.awt.Paint[] pallet
					);
		}		
	}
	
	public void update_retailerAccess() {
		if (display.economy.retailerAccess!=null) {
			ArrayList<Double[]> retailerAccess=display.economy.retailerAccess;
			ArrayList<Integer> includableGoods=new ArrayList<Integer>();
			for (int i=1; i<retailerAccess.get(0).length; i++) { // For each good...
				boolean include=false; // ... initially assume the good is NOT included...
				for (Double[] series: retailerAccess) { // ... then search the data...
					if (series[i]>0) include=true; // ... and if there is ANY non-null price for good i...
				}
				if (include) includableGoods.add(i); // include it in the displayable price series
			}
			int g=includableGoods.size();
			String[] seriesNames=new String[g];
			for (int i=0; i<g; i++) {
				seriesNames[i]="Good "+includableGoods.get(i);
			}
			ArrayList<double[][]> data=new ArrayList<double[][]>();
			for (int i=0; i<g; i++) { // Goods (but indexed with the help of includableGoods)
				ArrayList<double[]> series_prep=new ArrayList<double[]>(); // start this as an ArrayList because we don't know it's length
				for (int j=Math.max(0, retailerAccess.size()-100); j<retailerAccess.size(); j++) { // Periods
					Double y=retailerAccess.get(j)[includableGoods.get(i)];
					double[] point=new double[2];
					point[0]=j;
					point[1]=y;
					series_prep.add(point);								
				}
				double[][] series=new double[series_prep.size()][2];
				for (int j=0; j<series_prep.size(); j++) {
					series[j]=series_prep.get(j);
				}
				data.add(series);
			}
			String title="Retailer Access by Good";
			if (economyMarker!=null) title=title+economyMarker;
			chart=lineAndShapeChart(
					"Retailer Access by Good",	// String title,
					"Time",	// String xAxis,
					"% with Access",	// String yAxis,
					seriesNames,	// String[] seriesNames,
					data,	// ArrayList<double[][]> data,
					false, 	// boolean[] showShapes,
					true,	// boolean[] showLines,
					null	// java.awt.Paint[] pallet
					);
		}		
	}
	
	public void update_consumablesTradables() {
		if (display.economy.retailerAccess!=null) {
			ArrayList<Integer> numConsumables=display.economy.numConsumables;
			ArrayList<Integer> numTradables=display.economy.numTradables;
			String[] seriesNames={"Goods Consumed","Tradable Goods"};
			ArrayList<double[][]> data=new ArrayList<double[][]>();
			int t=numConsumables.size();
			double[][] consumables=new double[t][2];
			double[][] tradables=new double[t][2];
			for (int j=0; j<t; j++) { // Periods
				Double numCons=(Double) numConsumables.get(j).doubleValue();
				Double numTrad=(Double) numTradables.get(j).doubleValue();
				double[] pointCons={(double)j,numCons};
				double[] pointTrad={(double)j,numTrad};
				consumables[j]=pointCons;
				tradables[j]=pointTrad;
			}
			data.add(consumables);
			data.add(tradables);
			String title="Active Industries (Consumables and Tradables)";
			if (economyMarker!=null) title=title+economyMarker;
			chart=lineAndShapeChart(
					title,	// String title,
					"Time",	// String xAxis,
					"Number of goods",	// String yAxis,
					seriesNames,	// String[] seriesNames,
					data,	// ArrayList<double[][]> data,
					false, 	// boolean[] showShapes,
					true,	// boolean[] showLines,
					null	// java.awt.Paint[] pallet
					);
			XYPlot plot=(XYPlot) chart.getPlot();
			ValueAxis rangeAxis=plot.getRangeAxis();
			rangeAxis.setUpperBound(display.economy.numGoods);
		}		
	}
	
	public void update_goodsSpace() {
		if (display.economy.industryStatus_consumed!=null) {
			boolean testing2=false;
			if (testing2) System.out.println("TESTING ChartPanel.update() with chartType=Goods space");
			int turn=display.economy.industryStatus_consumed.size();
			Boolean[] consumed=display.economy.industryStatus_consumed.get(turn-1);
			Boolean[] traded=display.economy.industryStatus_traded.get(turn-1);
			String[] seriesNames={"Consumed and tradable","Consumed but not tradable","Tradable but not consumed","Neither tradable nor consumed"};
			ArrayList<double[]> c_and_t=new ArrayList<double[]>(); // Consumed and tradable
			ArrayList<double[]> c_and_not_t=new ArrayList<double[]>(); // Consumed but not tradable
			ArrayList<double[]> not_c_and_t=new ArrayList<double[]>(); // Tradable but not consumed
			ArrayList<double[]> not_c_and_not_t=new ArrayList<double[]>(); // Neither tradable nor consumed
			for (int i=2; i<display.economy.numGoods; i++) {
				double alpha=display.economy.utilityFunction.coeffs[i];
				double l0=display.economy.productionFunction.getL0(i);
				double[] point={alpha,l0};
				if (consumed[i]&traded[i]) c_and_t.add(point);
				if (consumed[i]&!traded[i]) c_and_not_t.add(point);
				if (!consumed[i]&traded[i]) not_c_and_t.add(point);
				if (!consumed[i]&!traded[i]) not_c_and_not_t.add(point);
			}
			double[][] c_and_t_array=makeArray(c_and_t);
			double[][] c_and_not_t_array=makeArray(c_and_not_t);
			double[][] not_c_and_t_array=makeArray(not_c_and_t);
			double[][] not_c_and_not_t_array=makeArray(not_c_and_not_t);
			ArrayList<double[][]> data=new ArrayList<double[][]>();
			/* if (c_and_t_array!=null) */ data.add(c_and_t_array);
			/* if (c_and_not_t_array!=null) */ data.add(c_and_not_t_array);
			/* if (not_c_and_t_array!=null) */ data.add(not_c_and_t_array);
			/* if (not_c_and_not_t_array!=null) */ data.add(not_c_and_not_t_array);
			java.awt.Paint[] pallet={Color.BLACK,	// Black stands for goods that are consumed and traded
				Color.BLUE,							// Blue stands for goods that are consumed but not traded
				Color.RED,							// Red stands for goods that are traded but not consumed
				Color.YELLOW						// Yellow stands for goods that are neither traded nor consumed
			};
			String title="Exploring the Goods Space";
			if (economyMarker!=null) title=title+economyMarker;
			chart=lineAndShapeChart(
					title,	// String title,
					"Alpha (Well Liked)",	// String xAxis,
					"L0 (Hard to Make)",	// String yAxis,
					seriesNames,	// String[] seriesNames,
					data,	// ArrayList<double[][]> data,
					true, 	// boolean[] showShapes,
					false,	// boolean[] showLines,
					pallet	// java.awt.Paint[] pallet
					);
		}		
	}
	
	public void update_industriesCP() {
		if (display.economy.industryHistoricalProfiles!=null) {
			int last=display.economy.industryHistoricalProfiles[0].size()-1;
			if (last<0) return;
			double[][] series=new double[display.economy.industryHistoricalProfiles.length][2];
			for (int i=0; i<series.length; i++) {
				int numRetailers=(int) (double) display.economy.industryHistoricalProfiles[i].get(last)[1];
				int numSellers=(int) (double) display.economy.industryHistoricalProfiles[i].get(last)[2];
				double[] point={numRetailers,numSellers};
				series[i]=point;
			}
			ArrayList<double[][]> data=new ArrayList<double[][]>();
			data.add(series);
			String[] seriesNames={"Industries"};
			String title="Industries: consumers and producers";
			chart=lineAndShapeChart(
					title,	// String title,
					"Consumers",	// String xAxis,
					"Producers",	// String yAxis,
					seriesNames,	// String[] seriesNames,
					data,	// ArrayList<double[][]> data,
					true, 	// boolean[] showShapes,
					false,	// boolean[] showLines,
					null	// would have been a java.awt.Paint[] pallet
					);
			XYPlot plot=(XYPlot) chart.getPlot();
	        XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer(); 
        	renderer.setSeriesShape(0, new Ellipse2D.Double(-2.0,-2.0,2.0,2.0));
	        renderer.setBaseShape(new Ellipse2D.Double(-1.0,-1.0,2.0,2.0));
		}		
	}
	
	public void update_techGoodsSpaceViewer() {
		if (display.gui.activeEconomy.pFunction==null) return;
		if (display.gui.activeEconomy.uFunction==null) return;
		double[][] series=new double[display.gui.activeEconomy.uFunction.coeffs.length][2];
		for (int i=2; i<series.length; i++) {
			Double alpha=display.gui.activeEconomy.uFunction.coeffs[i];
			Double l0=display.gui.activeEconomy.pFunction.getL0(i);
			if ((alpha!=null)&(l0!=null)) {
				double[] point={alpha,l0};				
				series[i]=point;
			}
		}
		ArrayList<double[][]> data=new ArrayList<double[][]>();
		data.add(series);
		String[] seriesNames={"Goods"};
		String title="Goods space";
		if (economyMarker!=null) title=title+economyMarker;
		chart=lineAndShapeChart(
				title,	// String title,
				"Alpha (Well-Liked)",	// String xAxis,
				"L0 (Hard to Make)",	// String yAxis,
				seriesNames,	// String[] seriesNames,
				data,	// ArrayList<double[][]> data,
				true, 	// boolean[] showShapes,
				false,	// boolean[] showLines,
				null	// would have been a java.awt.Paint[] pallet
				);
/*		XYPlot plot=(XYPlot) chart.getPlot();
        XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer(); 
       	renderer.setSeriesShape(0, new Ellipse2D.Double(-2.0,-2.0,2.0,2.0));
        renderer.setBaseShape(new Ellipse2D.Double(-1.0,-1.0,2.0,2.0)); */
	}
	
	public void update_industriesRW() {
		if (display.economy.industryHistoricalProfiles!=null) {
			int last=display.economy.industryHistoricalProfiles[0].size()-1;
			if (last<0) return;
			double[][] series=new double[display.economy.industryHistoricalProfiles.length][2];
			for (int i=0; i<series.length; i++) {
				int numRetailers=(int) (double) display.economy.industryHistoricalProfiles[i].get(last)[11];
				int numSellers=(int) (double) display.economy.industryHistoricalProfiles[i].get(last)[4];
				double[] point={numRetailers,numSellers};
				series[i]=point;
			}
			ArrayList<double[][]> data=new ArrayList<double[][]>();
			data.add(series);
			String[] seriesNames={"Industries"};
			String title="Industries: retailers and workers";
			if (economyMarker!=null) title=title+economyMarker;
			chart=lineAndShapeChart(
					title,	// String title,
					"Retailers",	// String xAxis,
					"Workers",	// String yAxis,
					seriesNames,	// String[] seriesNames,
					data,	// ArrayList<double[][]> data,
					true, 	// boolean[] showShapes,
					false,	// boolean[] showLines,
					null	// would have been a java.awt.Paint[] pallet
					);
			XYPlot plot=(XYPlot) chart.getPlot();
	        XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer(); 
        	renderer.setSeriesShape(0, new Ellipse2D.Double(-2.0,-2.0,2.0,2.0));
 	        renderer.setBaseShape(new Ellipse2D.Double(-1.0,-1.0,2.0,2.0));
		}		
	}
	
	public void update_agentsMU() {
		boolean testing=true;  
//		if (testing) System.out.println("TESTING ChartPanel.update_agentsMU(). BEGINNING OF METHOD");
		agent=display.agentDetail_table2c.agent;
		ArrayList<double[]> retailers=new ArrayList<double[]>();
		ArrayList<double[]> nonRetailers=new ArrayList<double[]>();
		int money=display.economy.money;
		for (int i=0; i<display.economy.population.size(); i++) {
			Agent agent=display.economy.population.get(i);
			double log_a_money=Math.log(agent.has(money));
			double log_a_utility=Math.log(agent.cons.utility);
			
			double[] point={log_a_money,log_a_utility};
			if (agent.enterprises.size()>0) retailers.add(point);
			else nonRetailers.add(point);
		}
		double[][] seriesR=makeArray(retailers);
		double[][] seriesNR=makeArray(nonRetailers);
		ArrayList<double[][]> data=new ArrayList<double[][]>();
		data.add(seriesR);
		data.add(seriesNR);
		String[] seriesNames={"Retailers","Non-retailers"};
		java.awt.Paint[] pallet={Color.RED,	// Red for retailers
				Color.BLACK					// Black for non-retailers
			};
		String title="Agents: money and utility";
		if (economyMarker!=null) title=title+economyMarker;
		chart=lineAndShapeChart(
				title,	// String title,
				"Log Money",	// String xAxis,
				"Log Utility",	// String yAxis,
				seriesNames,	// String[] seriesNames,
				data,	// ArrayList<double[][]> data,
				true, 	// boolean[] showShapes,
				false,	// boolean[] showLines,
				pallet	// would have been a java.awt.Paint[] pallet
				);
		XYPlot plot=(XYPlot) chart.getPlot();
        XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer(); 
    	renderer.setSeriesShape(0, new Ellipse2D.Double(-2.0,-2.0,3.0,3.0));
    	renderer.setSeriesShape(1, new Ellipse2D.Double(-1.0,-1.0,2.0,2.0));
        renderer.setBaseShape(new Ellipse2D.Double(-1.0,-1.0,2.0,2.0));
 //     if (testing) System.out.println("TESTING ChartPanel.update_agentsMU(). END OF METHOD. chart=null: "+(chart==null));
	}
	
	public void update_diversifyMakeUse() {
		if (display.economy.retailerAccess!=null) {
			ArrayList<Double> numConsumed=display.economy.avgNumberGoodsConsumed;
			ArrayList<Double> numProduced=display.economy.avgNumberGoodsProduced;
			String[] seriesNames={"Goods Used","Goods Made"};
			ArrayList<double[][]> data=new ArrayList<double[][]>();
			int t=numConsumed.size();
			double[][] used=new double[t][2];
			double[][] made=new double[t][2];
			for (int j=0; j<t; j++) { // Periods
				Double numUsed=(Double) numConsumed.get(j).doubleValue();
				Double numMade=(Double) numProduced.get(j).doubleValue();
				double[] pointCons={(double)j,numUsed};
				double[] pointTrad={(double)j,numMade};
				used[j]=pointCons;
				made[j]=pointTrad;
			}
			data.add(used);
			data.add(made);
			String title="Diversification in Consumption and Production";
			if (economyMarker!=null) title=title+economyMarker;
			chart=lineAndShapeChart(
					title,	// String title,
					"Time",	// String xAxis,
					"Number of goods",	// String yAxis,
					seriesNames,	// String[] seriesNames,
					data,	// ArrayList<double[][]> data,
					false, 	// boolean[] showShapes,
					true,	// boolean[] showLines,
					null	// java.awt.Paint[] pallet
					);
			XYPlot plot=(XYPlot) chart.getPlot();
			ValueAxis rangeAxis=plot.getRangeAxis();
			rangeAxis.setUpperBound(display.economy.numGoods);
		}		
	}
	
	public void update_avgURetNRet() {
		if (display.economy.retailerAccess!=null) {
			ArrayList<Double> utilRetail=display.economy.averageUtility_retailers;
			ArrayList<Double> utilNonRetail=display.economy.averageUtility_nonRetailers;
			String[] seriesNames={"Retailers","Non-retailers"};
			ArrayList<double[][]> data=new ArrayList<double[][]>();
			int t=utilRetail.size();
			double[][] uR=new double[t][2];
			double[][] uNR=new double[t][2];
			for (int j=0; j<t; j++) { // Periods
				Double avgUtilR=(Double) utilRetail.get(j).doubleValue();
				Double avgUtilNR=(Double) utilNonRetail.get(j).doubleValue();
				double[] pointR={(double)j,avgUtilR};
				double[] pointNR={(double)j,avgUtilNR};
				uR[j]=pointR;
				uNR[j]=pointNR;
			}
			data.add(uR);
			data.add(uNR);
			String title="Average Utility of Retailers and Non-Retailers";
			if (economyMarker!=null) title=title+economyMarker;
			chart=lineAndShapeChart(
					title,	// String title,
					"Time",	// String xAxis,
					"Utility",	// String yAxis,
					seriesNames,	// String[] seriesNames,
					data,	// ArrayList<double[][]> data,
					false, 	// boolean[] showShapes,
					true,	// boolean[] showLines,
					null	// java.awt.Paint[] pallet
					);
		}		
	}
	
	public void update_capitalRetNRet() {
		if (display.economy.retailerAccess!=null) {
			ArrayList<Double> kRetail=display.economy.totCapitalRetailers;
			ArrayList<Double> kNonRetail=display.economy.totCapitalNonRetailers;
			String[] seriesNames={"Retailers","Non-retailers"};
			ArrayList<double[][]> data=new ArrayList<double[][]>();
			int t=kRetail.size();
			double[][] kR=new double[t][2];
			double[][] kNR=new double[t][2];
			for (int j=0; j<t; j++) { // Periods
				Double avgK_R=(Double) kRetail.get(j).doubleValue();
				Double avgK_NR=(Double) kNonRetail.get(j).doubleValue();
				double[] pointR={(double)j,avgK_R};
				double[] pointNR={(double)j,avgK_NR};
				kR[j]=pointR;
				kNR[j]=pointNR;
			}
			data.add(kR);
			data.add(kNR);
			String title="Total Capital of Retailers and Non-Retailers";
			if (economyMarker!=null) title=title+economyMarker;
			chart=lineAndShapeChart(
					title,	// String title,
					"Time",	// String xAxis,
					"Capital",	// String yAxis,
					seriesNames,	// String[] seriesNames,
					data,	// ArrayList<double[][]> data,
					false, 	// boolean[] showShapes,
					true,	// boolean[] showLines,
					null	// java.awt.Paint[] pallet
					);
		}		
	}

	public void update_countRetNRet() {
		if (display.economy.retailerAccess!=null) {
			ArrayList<Integer> kRetail=display.economy.countRetailers;
			ArrayList<Integer> kNonRetail=display.economy.countNonRetailers;
			String[] seriesNames={"Retailers","Non-retailers"};
			ArrayList<double[][]> data=new ArrayList<double[][]>();
			int t=kRetail.size();
			double[][] kR=new double[t][2];
			double[][] kNR=new double[t][2];
			for (int j=0; j<t; j++) { // Periods
				Double avgK_R=(Double) kRetail.get(j).doubleValue();
				Double avgK_NR=(Double) kNonRetail.get(j).doubleValue();
				double[] pointR={(double)j,avgK_R};
				double[] pointNR={(double)j,avgK_NR};
				kR[j]=pointR;
				kNR[j]=pointNR;
			}
			data.add(kR);
			data.add(kNR);
			String title="Count of Retailers and Non-Retailers";
			if (economyMarker!=null) title=title+economyMarker;
			chart=lineAndShapeChart(
					title,	// String title,
					"Time",	// String xAxis,
					"Number",	// String yAxis,
					seriesNames,	// String[] seriesNames,
					data,	// ArrayList<double[][]> data,
					false, 	// boolean[] showShapes,
					true,	// boolean[] showLines,
					null	// java.awt.Paint[] pallet
					);
		}		
	}

	public void update_industriesPQ() {
		if (display.economy.historicalSales!=null) {
			boolean testing2=false;
			if (testing2) System.out.println("TESTING ChartPanel.update() with chartType=Goods space");
			int turn=display.economy.industryStatus_consumed.size();
			String[] seriesNames={"Consumed and tradable","Consumed but not tradable","Tradable but not consumed","Neither tradable nor consumed"};
			ArrayList<double[]> data=new ArrayList<double[]>();
			Double[] sales=display.economy.historicalSales.get(turn-1);
			Double[] prices=display.economy.historicalPrices.get(turn-1);
			for (int i=2; i<display.economy.numGoods; i++) {
				if (sales[i]>0) {
					double sales_i=Math.log(sales[i]);
					double price_i=Math.log(prices[i]);
					double[] point={sales_i,price_i};
					data.add(point);
				}
			}
			double[][] data_as_array=makeArray(data);
			ArrayList<double[][]> dataset=new ArrayList<double[][]>();
			dataset.add(data_as_array);
			String title="Industries in Price/Quantity Space";
			if (economyMarker!=null) title=title+economyMarker;
			chart=lineAndShapeChart(
					title,	// String title,
					"Log Total Sales",	// String xAxis,
					"Log Avg. Price",	// String yAxis,
					seriesNames,	// String[] seriesNames,
					dataset,	// ArrayList<double[][]> data,
					true, 	// boolean[] showShapes,
					false,	// boolean[] showLines,
					null	// java.awt.Paint[] pallet
					);
			XYPlot plot=(XYPlot) chart.getPlot();
			ValueAxis rangeAxis=plot.getRangeAxis();
			ValueAxis domainAxis=plot.getDomainAxis();
			domainAxis.setUpperBound(5);
			domainAxis.setLowerBound(-2);
			rangeAxis.setUpperBound(5);
			rangeAxis.setLowerBound(-2);
		}		
	}
	
	public void update_industriesPQm_avg() {
		if (display.economy.industryStatus_consumed!=null) {
			boolean testing2=false;
			if (testing2) System.out.println("TESTING ChartPanel.update() with chartType=Goods space");
			int turn=display.economy.industryStatus_consumed.size();
			String[] seriesNames={"Consumed and tradable","Consumed but not tradable","Tradable but not consumed","Neither tradable nor consumed"};
			ArrayList<double[]> data=new ArrayList<double[]>();
			ArrayList<Double[]> sales=display.economy.historicalSales;
			ArrayList<Double[]> prices=display.economy.historicalPrices;
			for (int i=2; i<display.economy.numGoods; i++) {
				Double totSales_i=0.0;
				Double sumPrices_i=0.0;
				int countRelevantTurnsForSalesAvg=0;
				int countRelevantTurnsForPriceAvg=0;
				Y: for (int j=0; j<10; j++) {
					int t=sales.size()-1-j;
					if (t<0) break Y;
					totSales_i=totSales_i+sales.get(j)[i];
					countRelevantTurnsForSalesAvg++;
					if (sales.get(j)[i]>0) {
						sumPrices_i=sumPrices_i+prices.get(j)[i];
						countRelevantTurnsForPriceAvg++;
					}
				}
				double avg_sales_i=Math.log(totSales_i/countRelevantTurnsForSalesAvg);
				double avg_price_i=Math.log(sumPrices_i/countRelevantTurnsForPriceAvg);
				double[] point={avg_sales_i,avg_price_i};
				data.add(point);
			}
			double[][] data_as_array=makeArray(data);
			ArrayList<double[][]> dataset=new ArrayList<double[][]>();
			dataset.add(data_as_array);
			String title="Industries in P/Q Space (Moving Avg.)";
			if (economyMarker!=null) title=title+economyMarker;
			chart=lineAndShapeChart(
					title,	// String title,
					"Log Total Sales",	// String xAxis,
					"Log Avg. Price",	// String yAxis,
					seriesNames,	// String[] seriesNames,
					dataset,	// ArrayList<double[][]> data,
					true, 	// boolean[] showShapes,
					false,	// boolean[] showLines,
					null	// java.awt.Paint[] pallet
					);
			XYPlot plot=(XYPlot) chart.getPlot();
			ValueAxis rangeAxis=plot.getRangeAxis();
			ValueAxis domainAxis=plot.getDomainAxis();
			domainAxis.setUpperBound(5);
			domainAxis.setLowerBound(-2);
			rangeAxis.setUpperBound(5);
			rangeAxis.setLowerBound(-2);					
		}
	}
	
	public void update_industriesPQsnake() {
		if (display.economy.industryStatus_consumed!=null) {
			boolean testing2=false;
			if (testing2) System.out.println("TESTING ChartPanel.update() with chartType=Goods space");
			int turn=display.economy.industryStatus_consumed.size();
			ArrayList<String> seriesNames=new ArrayList<String>();
			ArrayList<java.awt.Paint> pallet=new ArrayList<java.awt.Paint>();
//			java.awt.Paint paint=new java.awt.Color(100, 100, 100);
			ArrayList<double[][]> data=new ArrayList<double[][]>();
			ArrayList<Double[]> sales=display.economy.historicalSales;
			ArrayList<Double[]> prices=display.economy.historicalPrices;
			int snakeLength=10;
			if (sales.size()<10) snakeLength=sales.size();
			for (int i=2; i<display.economy.numGoods; i++) {
				ArrayList<double[]> snake=new ArrayList<double[]>(); // Ten turns' worth of data; price/qty for each turn
				boolean keep=false;
				Double lastPrice=Double.NaN;
				for (int j=0; j<snakeLength; j++) {
					int t=sales.size()-1-j;
					double[] point=new double[2];
					point[0]=Math.log(sales.get(j)[i]); // the "x" coordinate of the snake is sales
					if (prices.get(j)[i]!=null) {				
						Double price=Math.log(prices.get(j)[i]);
						if (price>0) {
							lastPrice=price;
						}
					}
					if (lastPrice>0) {
						point[1]=lastPrice;
						snake.add(point);
					}
				}
				if (snake.size()>0) {
					java.awt.Paint paint=new Color((int) (100+Math.random()*100),
							(int) (100+Math.random()*100),
							(int) (100+Math.random()*100));
					pallet.add(paint);
					seriesNames.add(""+i);
					double[][] snake_array=makeArray(snake);
					data.add(snake_array);
				}
			}
			java.awt.Paint[] pallet_array=new java.awt.Paint[pallet.size()];
			int count=0;
			for (java.awt.Paint paint: pallet) {
				pallet_array[count]=paint;
				count++;
			}
			String[] seriesNames_array=new String[seriesNames.size()];
			count=0;
			for (String s: seriesNames) {
				seriesNames_array[count]=s;
				count++;
			}
			String title="Industries in P/Q Space (Snaked Values)";
			if (economyMarker!=null) title=title+economyMarker;
			chart=lineAndShapeChart(
					title,	// String title,
					"Log Total Sales",	// String xAxis,
					"Log Avg. Price",	// String yAxis,
					seriesNames_array,	// String[] seriesNames,
					data,	// ArrayList<double[][]> data,
					false, 	// boolean[] showShapes,
					true,	// boolean[] showLines,
					pallet_array	// java.awt.Paint[] pallet
					);
			XYPlot plot=(XYPlot) chart.getPlot();
			ValueAxis rangeAxis=plot.getRangeAxis();
			ValueAxis domainAxis=plot.getDomainAxis();
			domainAxis.setUpperBound(3);
			domainAxis.setLowerBound(-1);
			rangeAxis.setUpperBound(3);
			rangeAxis.setLowerBound(-1);					
		}
	}
	
	public void update_industryPriceHistory() {
		if (industry==null) return;
		if (display.economy.industryPriceHistories!=null) {
			ArrayList<String> seriesNames=new ArrayList<String>();
			ArrayList<java.awt.Paint> pallet=new ArrayList<java.awt.Paint>();
			ArrayList<ArrayList<Double[]>> priceHistory=display.economy.industryPriceHistories[industry];
			ArrayList<ArrayList<double[]>> priceHistory_resorted=new ArrayList<ArrayList<double[]>>();
			ArrayList<Integer> idsOfRetailers=new ArrayList<Integer>();
			int start=Math.max(0, priceHistory.size()-100);
			for (int turn=start; turn<priceHistory.size(); turn++) {
				ArrayList<Double[]> record=priceHistory.get(turn);
				for (Double[] point: record) {
					int r=(int) (double) point[0];
					Integer seriesNumber=null;
					Y: for (int i=0; i<idsOfRetailers.size(); i++) { // Look for a retailer id that matches this one
						if (idsOfRetailers.get(i)==r) {
							seriesNumber=i;
							break Y;
						}
					}
					if (seriesNumber==null) { // If there is none, then add this retailer id to the list and make a new series
						seriesNumber=idsOfRetailers.size();
						idsOfRetailers.add(r);
						priceHistory_resorted.add(new ArrayList<double[]>());
						seriesNames.add("R"+r);
						java.awt.Paint paint=new Color((int) (100+Math.random()*100),
								(int) (100+Math.random()*100),
								(int) (100+Math.random()*100));
						pallet.add(paint);
					}
					double logPrice=Math.log(point[1]);
					double[] new_point={turn,logPrice}; 
					priceHistory_resorted.get(seriesNumber).add(new_point);
				}
			}
			ArrayList<double[][]> data=new ArrayList<double[][]>();
			for (ArrayList<double[]> record: priceHistory_resorted) {
				double[][] series=new double[record.size()][2];
				for (int i=0; i<record.size(); i++) {
					series[i]=record.get(i);
				}
				data.add(series);
			}
			java.awt.Paint[] pallet_array=new java.awt.Paint[pallet.size()];
			int count=0;
			for (java.awt.Paint paint: pallet) {
				pallet_array[count]=paint;
				count++;
			}
			String[] seriesNames_array=new String[seriesNames.size()];
			count=0;
			for (String s: seriesNames) {
				seriesNames_array[count]=s;
				count++;
			}
			String title="Price History for Industry "+industry;
			if (economyMarker!=null) title=title+economyMarker;
			chart=lineAndShapeChart(
					title,	// String title,
					"Time",	// String xAxis,
					"Log Price",	// String yAxis,
					seriesNames_array,	// String[] seriesNames,
					data,	// ArrayList<double[][]> data,
					false, 	// boolean[] showShapes,
					true,	// boolean[] showLines,
					pallet_array	// java.awt.Paint[] pallet
					);
		}
	}

	public void update_industriesRetSpec() {
		
	}
	
	public void update_avgMedUtil_ret() {
		
	}
	
	public void update_avgMedUtil_nRet() {
		
	}
	
	public void update_pL0ratio() {
		if (display.economy.priceToOverhead!=null) {
			ArrayList<Double> priceToOverhead=display.economy.priceToOverhead;
			String[] seriesNames={"Price-to-overhead ratio"};
			ArrayList<double[][]> data=new ArrayList<double[][]>();
			int t=Math.min(50,priceToOverhead.size()); // t is the number of turns to be included in this chart
			double[][] pToL0=new double[t][2];
			int start=priceToOverhead.size()-t;
			for (int j=0; j<t; j++) { // Periods
				Double pl0=(Double) priceToOverhead.get(start+j).doubleValue();
				double[] point={(double)j,pl0};
				pToL0[j]=point;
			}
			data.add(pToL0);
			chart=lineAndShapeChart(
					"Price to Overhead Ratio",	// String title,
					"Time",	// String xAxis,
					"P-to-L0 ratio",	// String yAxis,
					seriesNames,	// String[] seriesNames,
					data,	// ArrayList<double[][]> data,
					false, 	// boolean[] showShapes,
					true,	// boolean[] showLines,
					null	// java.awt.Paint[] pallet
					);
		}				
		
	}
	
	public void update_agentConsProd() {
		if (agent!=null) {
			String[] seriesNames={"Production","Consumption"};
			ArrayList<double[][]> data=new ArrayList<double[][]>();
			ArrayList<double[]> prodPoints=new ArrayList<double[]>();
			int start=Math.max(agent.diverseProdHistory.size()-100,0);
			for (int i=start; i<agent.diverseProdHistory.size(); i++) {
				for (Integer j: agent.diverseProdHistory.get(i)) {
					double[] point={(double) i,(double) j.doubleValue()};
					prodPoints.add(point);
				}
			}
			ArrayList<double[]> consPoints=new ArrayList<double[]>();
			for (int i=start; i<agent.diverseConsHistory.size(); i++) {
				for (Integer j: agent.diverseConsHistory.get(i)) {
					double[] point={(double) i,(double) j.doubleValue()};
					consPoints.add(point);
				}
			}
			data.add(makeArray(prodPoints));
			data.add(makeArray(consPoints));
			java.awt.Paint[] pallet={Color.RED,Color.YELLOW};
			chart=lineAndShapeChart(
					"Consumption and Production",	// String title,
					"Time",	// String xAxis,
					"Good",	// String yAxis,
					seriesNames,	// String[] seriesNames,
					data,	// ArrayList<double[][]> data,
					true, 	// boolean[] showShapes,
					false,	// boolean[] showLines,
					pallet	// java.awt.Paint[] pallet
					);		
			XYPlot plot=(XYPlot) chart.getPlot();
	        XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer(); 
        	renderer.setSeriesShape(0, new Ellipse2D.Double(-2.0,-2.0,2.0,2.0));
        	renderer.setSeriesShape(1, new Ellipse2D.Double(-1.0,-1.0,2.0,2.0));
	        renderer.setBaseShape(new Ellipse2D.Double(-1.0,-1.0,2.0,2.0));
		}
	}
	
	public void update_gdpSupplyBreakdown() {
		if (display.economy.totWagesHistory!=null) {
			ArrayList<Double> totWagesHistory=display.economy.totWagesHistory;
			ArrayList<Double> totProfitHistory=display.economy.totProfitHistory;
			String[] seriesNames={"Total Wages","+ Profit = GDP (supply)"};
			ArrayList<double[][]> data=new ArrayList<double[][]>();
			int t=totWagesHistory.size();
			double[][] wages=new double[t][2];
			double[][] profits=new double[t][2];
			for (int j=0; j<t; j++) { // Periods
				Double w=(Double) totWagesHistory.get(j).doubleValue();
				Double p=(Double) totProfitHistory.get(j).doubleValue();
				p=p+w; // the "+ profits" series includes wages
				double[] pointW={(double)j,w};
				double[] pointP={(double)j,p};
				wages[j]=pointW;
				profits[j]=pointP;
			}
			data.add(wages);
			data.add(profits);
			chart=lineAndShapeChart(
					"GDP Supply Side: Wages and Profits",	// String title,
					"Time",	// String xAxis,
					"Wages, profits, GDP",	// String yAxis,
					seriesNames,	// String[] seriesNames,
					data,	// ArrayList<double[][]> data,
					false, 	// boolean[] showShapes,
					true,	// boolean[] showLines,
					null	// java.awt.Paint[] pallet
					);
		}				
	}

	/* THIS IS ONLY BEING KEPT AROUND IN CASE I WANT TO MAKE BAR CHARTS SOMETIME AND WANT A CODE SAMPLE
	public void makeConsumptionByGoodPanel(GuiFrame gui) {
		Economy e=gui.economy;
		UtilityFunction u=e.utilityFunction;
		int numGoods=e.utilityFunction.coeffs.length-2;
		double[] totCons=new double[numGoods];
		for (int i=0; i<totCons.length; i++) {
			totCons[i]=0;
		}
		for (int j=0; j<e.population.size(); j++) { // j is the iterator for a particular AGENT
			for (int i=0; i<totCons.length; i++) { // i is the iterator for a particular GOOD
				totCons[i]=totCons[i]+e.population.get(j).cons.used[i];				
			}
		}
		double[] avgCons=new double[totCons.length];
		for (int i=0; i<totCons.length; i++) {
			avgCons[i]=totCons[i]/e.population.size();
		}
		DefaultCategoryDataset dataset=new DefaultCategoryDataset();
		for (int i=0; i<avgCons.length; i++) {
			Number d=avgCons[i];
			dataset.addValue(d, "Good "+i, 0);
		}
		chart = ChartFactory.createBarChart(
				"Consumption, by Good", // chart title
				"", // domain axis label
				"Units of Labor Worked", // range axis label
				dataset, // data
				PlotOrientation.VERTICAL, // orientation
				true, // include legend
				true, // tooltips?
				false // URLs?
				);		
		image=chart.createBufferedImage(500, 250);			
	}
	*/
	
	public static JFreeChart differenceChart(String title,String xLabel,String yLabel,String label1,String label2,
			double[][] series1,double[][] series2,double pMin,double pMax) {
		XYSeries s1=new XYSeries(label1);
		XYSeries s2=new XYSeries(label2);
		for (int i=0; i<series1.length; i++) {
			s1.add(series1[i][0],series1[i][1]);
			s2.add(series2[i][0],series2[i][1]);
		}
		XYSeriesCollection dataset=new XYSeriesCollection();
		dataset.addSeries(s1);
		dataset.addSeries(s2);
		JFreeChart chart=ChartFactory.createTimeSeriesChart(title,xLabel,yLabel,dataset,true,true,false);
		XYPlot plot=(XYPlot) chart.getPlot();
		plot.setDomainPannable(true);
	    XYDifferenceRenderer r = new XYDifferenceRenderer(Color.lightGray,
	            Color.black, false);
	    r.setRoundXCoordinates(true);
	    r.setSeriesFillPaint(1,Color.darkGray);
	    r.setSeriesFillPaint(0,Color.black);
	    plot.setDomainCrosshairLockedOnData(true);
	    plot.setRangeCrosshairLockedOnData(true);
	    plot.setDomainCrosshairVisible(true);
	    plot.setRangeCrosshairVisible(true);
	    plot.setRenderer(r);
	    NumberAxis domainAxis=new NumberAxis(xLabel);
	    domainAxis.setAutoRangeIncludesZero(true);
	    plot.setDomainAxis(domainAxis);
	    NumberAxis rangeAxis=new NumberAxis(yLabel);
	    rangeAxis.setUpperBound(pMax);
	    rangeAxis.setLowerBound(pMin);
	    plot.setRangeAxis(rangeAxis);
	    plot.setForegroundAlpha(0.5f);
	    ChartUtilities.applyCurrentTheme(chart);
	    return chart;
	}

	public static JFreeChart lineAndShapeChart(String title,String xAxis,String yAxis,
			String[] seriesNames,ArrayList<double[][]> data,
			boolean showShapes,boolean showLines,java.awt.Paint[] pallet) {
		// The purpose of this method is simply to allow other methods to call lineAndShapeChart() with single 
		// booleans instead of boolean arrays for showShapes and showLines, if they are not interested in 
		// having some series shown one way and some the other.
		int numSeries=data.size();
		boolean[] showShapes_array=new boolean[numSeries];
		boolean[] showLines_array=new boolean[numSeries];
		for (int i=0; i<numSeries; i++) {
			showShapes_array[i]=showShapes;
			showLines_array[i]=showLines;
		}
		JFreeChart chart=lineAndShapeChart(title,xAxis,yAxis,seriesNames,data,showShapes_array,showLines_array,pallet);
		return chart;
	}
	
	public static JFreeChart lineAndShapeChart(String title,String xAxis,String yAxis,
			String[] seriesNames,ArrayList<double[][]> data,
			boolean[] showShapes,boolean[] showLines,java.awt.Paint[] pallet) {
		boolean[] a={false};
		boolean[] b={true};
//		java.awt.Paint[] c={Color.black};
		if (showShapes==null) showShapes=a;
		if (showLines==null) showShapes=b;
//		if (pallet==null) pallet=c;
		XYSeriesCollection dataset=new XYSeriesCollection();
		for (int i=0; i<data.size(); i++) {
			XYSeries s=new XYSeries(seriesNames[i]);
			double[][] d=data.get(i);
			if (d!=null) {
				for (int j=0; j<data.get(i).length; j++) {
					s.add(d[j][0],d[j][1]);
				}				
			}
			dataset.addSeries(s);
		}
        JFreeChart chart = ChartFactory.createXYLineChart(title,xAxis,yAxis,dataset,PlotOrientation.VERTICAL,
                true,true,false);		
		XYPlot plot=(XYPlot) chart.getPlot();
		plot.setDomainPannable(true);
		plot.setRangePannable(true);
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        for (int i=0; i<showShapes.length; i++) {
        	renderer.setSeriesLinesVisible(i, showLines[i]);
        	renderer.setSeriesShapesVisible(i, showShapes[i]);
        	if (pallet!=null) renderer.setSeriesPaint(i, pallet[i]);
        	renderer.setSeriesShape(i, new Ellipse2D.Double(-3.0,-3.0,6.0,6.0));
        }
        renderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator());
        renderer.setDefaultEntityRadius(6);
        renderer.setBaseShape(new Ellipse2D.Double(-3.0,-3.0,6.0,6.0));
        renderer.setBasePaint(Color.black);
        chart.setBackgroundPaint(Color.white);
        chart.getPlot().setBackgroundPaint(Color.white);
        plot.setRenderer(renderer);	   
        return chart;
	}

	public double[][] makeArray(ArrayList<double[]> input) {
		if (input.size()==0) return null;
		double[][] output=new double[input.size()][input.get(0).length];
		for (int i=0; i<output.length; i++) {
			for (int j=0; j<output[0].length; j++) {
				output[i][j]=input.get(i)[j];
			}
		}
		return output;
	}
}
