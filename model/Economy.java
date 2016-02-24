package model;
import java.text.DecimalFormat;
import java.util.*;
import java.io.*;
import java.text.*;
import java.util.ArrayList;


public class Economy extends Thread implements Serializable {
	public int id;
	// Pointers to the various elements of the economy
	public transient EconomyDisplayPanels display;
	public ArrayList<Agent> population;
	public ArrayList<Agent> sortedPopulation;
	public ArrayList<Trader> traders;
	int nextRetailerID;
	int nextAgentID;
	// Parameters of the economy.
	public int numGoods; // The number of goods, including money and capital: two more than the number of consumable goods
	public UtilityFunction utilityFunction;
	public AvoidCostPF productionFunction;
	ArrayList<LaborReport> laborReports;
	ArrayList<ConsumptionReport> consumptionReports;
	public double[] avgLabor;
	public double[] avgConsumption;
	public double[] tradeVolumes;
	public int[] transactionProfile;
	public double marginRule; 
	public ArrayList<Transaction> transactionRecords;
	public Integer money;
	public Integer capital;
	public Memory memoryModel;
	public ArrayList<Retailer> retailers; 
	public ArrayList<Retailer> sortedRetailers;
	public ArrayList<Retailer>[] retailersSortedByIndustry;
	public ArrayList<Agent> ownersOfRetailers;
	public ArrayList<Agent> nonRetailers;	
	EntryAlgorithm entryAlgorithm;
	public Parameters parameters;
	
	// CAPITAL DYNAMICS PARAMETERS
	public Double SAVINGS_RATE;
	public Double CAPITAL_CONVERSION_RATE; // Coefficient for the conversion of foregone consumption to capital
	public Double CAPITAL_EXPONENT;
	public double DEPRECIATION_RATE;
	public double CAPITAL_ENDOWMENT_MIN;
	public double CAPITAL_ENDOWMENT_MAX;
	public double CAPITAL_ENDOWMENT_SKEW_EXP;
	
	// AGENT BEHAVIOR PARAMETERS
	public int AGENT_MEMORY_MAX;
	public int FRIENDS_MAX;
	public double SKILL_RETENTION_PROB_DECAY_RATE;
	public double SKILL_RETENTION_PROB_IN_USE;
	public Integer LEARNING_BY_DOING_FACTOR;
	
	// ENDOWMENTS
	public double MONEY_ENDOWMENT_MIN;
	public double MONEY_ENDOWMENT_MAX;
	
	// RETAILER BEHAVIOR PARAMETERS
	public Double PROFIT_RATE;
	public Double RETAILER_TIME_COST;
	public double MAX_RETAILER_EXIT_PROB_INCREMENT_DUE_TO_LOW_PAY;
	public double MIN_RETAILER_EXIT_PROB_INCREMENT_DUE_TO_HIGH_PAY;
	public double MIN_RETAILER_EXIT_PROB;
	public double MAX_RETAILER_EXIT_PROB;
	public double RETAILER_EXIT_PROB_INCREMENT_DUE_TO_NO_SALES;
	public double THRESHOLD_CHOOSE_EMULATE;
	public double THRESHOLD_CHOOSE_FRIENDS_MAKE;
	public double THRESHOLD_CHOOSE_FRIENDS_CAN_MAKE;
	public double THRESHOLD_CHOOSE_CAN_SELFSUPPLY_WHILE_RETAILING;
	public double RETAILER_ENTRY_PROB_IF_RETAILERS_DOING_WORSE;
	public double RETAILER_ENTRY_MULTIPLIER_FOR_LOG_UTIL_DIFFERENCE;
	public double RETAILER_ENTRY_HANDICAP_FOR_LOG_UTIL_DIFFERENCE;
	public double RETAILER_STARTING_PRICE_BIAS;
	public double RETAILER_MAX_PRICE_DROP_BASED_ON_INCOME_SUPPLY_RATIO;
	public double RETAILER_MAX_PRICE_RISE_BASED_ON_INCOME_SUPPLY_RATIO;
	public double RETAILER_PRICE_SHIFT_MULTIPLIER_BASED_ON_CASH_INVENTORY_IMBALANCE;
	public double RETAILER_PRICE_SHIFT_BIAS_BASED_ON_CASH_INVENTORY_IMBALANCE;
	public double MAX_PRICE_CHANGE_BASED_ON_CASH_INVENTORY_IMBALANCE;
	public double MIN_PRICE_CHANGE_BASED_ON_CASH_INVENTORY_IMBALANCE;
	public double NORMAL_BUS_COND_PRICE_RANDOMIZATION_FACTOR;
	public double ODDS_OF_MARKUP_CHANGE_UNDER_NORMAL_BUS_COND;
	public double SIZE_OF_MARKUP_CHANGE_UNDER_NORMAL_BUS_COND;
	public double ODDS_OF_MARKUP_CHANGE_UNDER_INVENTORY_PROBLEMS;
	public double SIZE_OF_MARKUP_CHANGE_UNDER_INVENTORY_PROBLEMS;
	public double ODDS_OF_MARKUP_CHANGE_UNDER_CASH_FLOW_PROBLEMS;
	public double SIZE_OF_MARKUP_CHANGE_UNDER_CASH_FLOW_PROBLEMS;
	public double RETAILER_MAX_PRICE_RESPONSE_TO_STOCKOUT;
	public double RETAILER_STOCKOUT_RESPONSE_SKEW_EXP;
	public double RETAILER_MAX_PRICE_RESPONSE_TO_CASHOUT;
	public double RETAILER_CASHOUT_RESPONSE_SKEW_EXP;
	public double RETAILER_MINIMUM_MARKUP;
	public double RETAILER_PRICE_SETTING_RECENCY_WEIGHT_FACTOR;
	public int RETAILER_PRICE_SETTING_MAX_HINDSIGHT;
	public double MIN_STARTING_MARKUP;
	public double MAX_STARTING_MARKUP;
	
	// MACRO VARIABLES VISIBLE TO AGENTS
	int[] lastTransactionProfile;
	// RECORD-KEEPING
	public boolean retailRecords;
	public int turn;
//	public int agentRecordsPolicy; // 0=no records, 1=detail
//	public ArrayList<Diary> statisticalAgency;
	
	// Thread stuff
	public volatile transient boolean stop;
//	public volatile transient boolean alive;

	// THE CONSTRUCTOR SETS ALL THE ECONOMY'S PARAMETERS AND CREATES THE AGENT POPULATION AND MONEY SUPPLY
	
	public Economy(int numAgents, // Population of agents
			UtilityFunction u, // Utility function
			AvoidCostPF p, // Production function (with "avoidable costs")
			Parameters param
			) throws DimensionalityException {
		id=(int) (1000000*Math.random());
		numGoods=u.coeffs.length; // n goods, plus capital and money
		money=0;
		capital=1;
		parameters=param;
		parameters.setParameters(this);
		resetTradeVolumes();
		resetTransactionProfile();
		turn=0;
		population=new ArrayList<Agent>();
		nextAgentID=0;
		for (int i=0; i<numAgents; i++) {
			new Agent(this);
		}
		traders=new ArrayList<Trader>(); // No traders exist yet, but this is a placeholder for when they appear.
		nextRetailerID=1;
		resetFriends();
		retailers=new ArrayList<Retailer>();
		entryAlgorithm=new EDOLEA();
		memoryModel=new Memory();		
		if (u.coeffs.length!=p.numGoods()) throw new DimensionalityException();
		utilityFunction=u;
		productionFunction=p;
		// Make the agents, give them production and utility functions.
		for (Agent a: population) {
			a.setProductionFunction(productionFunction);
			a.setUtilityFunction(u); 
		}
		for (Agent a: population) {
			try {
				double moneyEndowment=MONEY_ENDOWMENT_MIN+Math.random()*(MONEY_ENDOWMENT_MAX-MONEY_ENDOWMENT_MIN);
				double capitalEndowment=CAPITAL_ENDOWMENT_MIN
						+Math.pow(Math.random(), CAPITAL_ENDOWMENT_SKEW_EXP)*
						(CAPITAL_ENDOWMENT_MAX-CAPITAL_ENDOWMENT_MIN);
				a.gets(money,moneyEndowment); 	// Give each agent some money to get the economy started.	
				a.gets(capital,capitalEndowment);
			} catch(Exception ex) {
				ex.printStackTrace();
				System.exit(1);
			}
		}
		retailersSortedByIndustry=(ArrayList<Retailer>[]) new ArrayList[numGoods];
	}

	// THE RUN METHOD IS THE CORE OF WHAT THE ECONOMY DOES
	
	public void run() {
		while (true) {
			if (stop) break;
			if (display.gui.running==true) {
				run(true);	
				display.gui.field_turn.setText(""+turn);
				display.gui.label_turn.setText(""+turn);	
				display.updateDisplays();
				display.gui.validate();
			}
		}
	}
	
	public void run(boolean retail) {
		turn++;
//		DecimalFormat form=new DecimalFormat("0.000");
		boolean testing=false;
		sortedPopulation=(ArrayList<Agent>) population.clone();
		Collections.sort(sortedPopulation);
		sortedRetailers=(ArrayList<Retailer>) retailers.clone();
		Collections.sort(sortedRetailers);
		// Sort retailers by industry
		for (int i=0; i<numGoods; i++) {
			retailersSortedByIndustry[i]=new ArrayList<Retailer>();
		}
		for (Trader t: traders) {
			Retailer r=(Retailer) t;
			retailersSortedByIndustry[r.r_product].add(r);
		}
		for (int i=0; i<numGoods; i++) {
			Collections.sort(retailersSortedByIndustry[i]);
		}
		boolean agentBehaviorDetail=false;
		if (testing) System.out.println("TESTING Economy.run()");
		if (testing) System.out.println("SHOWING AGENT DETAIL AT Economy.run(). Turn "+turn);
		ownersOfRetailers=new ArrayList<Agent>();
		nonRetailers=new ArrayList<Agent>();
		for (Agent a: population) {
			if (a.enterprises.size()>0) ownersOfRetailers.add(a);
			if (a.enterprises.size()==0) nonRetailers.add(a);
		}
		initiate();
		if (testing) System.out.println("TESTING Economy.run(). About to activate agents...");
		for (Agent a: population) {
			if (stop) break; // Terminate the thread if the boolean stop is turned on.
			while (display.gui.running!=true) {
				try {
					if (stop) break;
					sleep(100); // every 100 milliseconds, the thread wakes up for a moment and sees if it is supposed to be running/runnable
				} catch(InterruptedException ex) {ex.printStackTrace();}
			}
			if (testing) System.out.println("TESTING Economy.run(). About to activate AGENT "+a.id);
			if (retail) { // The retail variable allows us to turn the retail sector off.
				if ((a.enterprises.size()==0)&(a.getEconomy().population.size()>1)) {
					Retailer r=Retailer.considerEntry(a, a.has[money]/2);				
				}
			}
			a.trade(money);
		}
		if (testing) System.out.println("TESTING Economy.run(). About to activate retailers...");
		if ((retail)&(turn>1)) { 
			ArrayList<Retailer> defunct=new ArrayList<Retailer>();
			for (Retailer r: retailers) {
				if (stop) break;
				while (display.gui.running!=true) {
					if (stop) break;
					try {
						sleep(100); // every 100 milliseconds, the thread wakes up for a moment and sees if it is supposed to be running/runnable
					} catch(InterruptedException ex) {ex.printStackTrace();}
				}
				if (testing) System.out.println("Retailer "+r.id+" activated");
				r.activate();
				if (r.defunct()==true) defunct.add(r);
			}
			for (Retailer r: defunct) {
				retailers.remove(r);
			}			
		}
		laborReports=new ArrayList<LaborReport>();
		consumptionReports=new ArrayList<ConsumptionReport>();
		for (Agent a: population) {
			if (agentBehaviorDetail) {
				System.out.println(a.labor.toString());
				System.out.println(a.cons.toString());
			}
			laborReports.add(a.labor);
			consumptionReports.add(a.cons);
		}
		double[] totLabor=new double[numGoods];
		avgLabor=new double[numGoods];
		double[] totConsumption=new double[numGoods];
		avgConsumption=new double[numGoods];
		for (int i=0; i<numGoods; i++) {
			for (LaborReport report: laborReports) {
				totLabor[i]=totLabor[i]+report.labor[i];
			}
			avgLabor[i]=totLabor[i]/population.size();
			totConsumption[i]=0;
			for (ConsumptionReport report: consumptionReports) {
				totConsumption[i]=totConsumption[i]+report.used[i];
			}
			avgConsumption[i]=totConsumption[i]/population.size();
		}		
		collectStats();
	}
	
	public void initiate() { // Called by run, before activating the Agents
		population=randomSortAgents(population);
		traders=randomSortTraders(traders);
		// Compile macro statistics visible to agents
		lastTransactionProfile=transactionProfile;
		resetFriends();
		transactionRecords=new ArrayList<Transaction>();
		resetTradeVolumes();
		resetTransactionProfile();
	}
	
	public void resetTradeVolumes() { // Called by the constructor and by initiate()
		tradeVolumes=new double[numGoods];
		for (int i=0; i<tradeVolumes.length; i++) {
			tradeVolumes[i]=0;
		}
	}
	
	public void resetTransactionProfile() {
		transactionProfile=new int[numGoods];
		for (int i=0; i<tradeVolumes.length; i++) {
			transactionProfile[i]=0;
		}
	}

	// METHODS RELATED TO RANDOMIZING THE ORDERS OF AGENTS AND RETAILERS TO PREVENT ARTIFACTS RELATED TO SEQUENCING
	
	public ArrayList<Agent> randomSortAgents(ArrayList<Agent> population) {
		ArrayList<Object> popAsObjects=new ArrayList<Object>();
		for (Agent a: population) {
			popAsObjects.add((Object) a);
		}
		popAsObjects=randomSort(popAsObjects);
		ArrayList<Agent> ret=new ArrayList<Agent>();
		for (Object o: popAsObjects) {
			ret.add((Agent) o);
		}
		return ret;
	}
	
	public static ArrayList<Trader> randomSortTraders(ArrayList<Trader> population) {
		ArrayList<Object> popAsObjects=new ArrayList<Object>();
		for (Trader r: population) {
			popAsObjects.add((Object) r);
		}
		popAsObjects=randomSort(popAsObjects);
		ArrayList<Trader> ret=new ArrayList<Trader>();
		for (Object o: popAsObjects) {
			ret.add((Trader) o);
		}
		return ret;
	}

	public static ArrayList<Object> randomSort(ArrayList<Object> population) {
		population=splitTheDeck(population);
		population=shuffle(population); population=shuffle(population); population=shuffle(population); population=shuffle(population); population=shuffle(population);
		return population;
	}
	
	public static ArrayList<Object> shuffle(ArrayList<Object> population) {
		ArrayList<Object> upperDeck=new ArrayList<Object>();
		ArrayList<Object> lowerDeck=new ArrayList<Object>();
		int splitPoint=(int) (Math.random()*population.size());
		int nextUpper=0;
		int nextLower=0;
		for (int i=0; i<splitPoint; i++) {
			lowerDeck.add(population.get(i));
		}
		for (int i=splitPoint; i<population.size(); i++) {
			upperDeck.add(population.get(i));
		}
		population=new ArrayList<Object>();
		while ((nextUpper<upperDeck.size())&(nextLower<lowerDeck.size())) {
			if (Math.random()<0.5) {
				population.add(lowerDeck.get(nextLower));
				nextLower++;
			} else {
				population.add(upperDeck.get(nextUpper));
				nextUpper++;
			}
		}
		while (nextLower<lowerDeck.size()) {
			population.add(lowerDeck.get(nextLower));
			nextLower++;
		}
		while (nextUpper<upperDeck.size()) {
			population.add(upperDeck.get(nextUpper));
			nextUpper++;
		}
		return population;
	}
	
	public static ArrayList<Object> splitTheDeck(ArrayList<Object> population) {
		ArrayList<Object> upperDeck=new ArrayList<Object>();
		ArrayList<Object> lowerDeck=new ArrayList<Object>();
		int splitPoint=(int) (0.5*population.size());
		for (int i=0; i<splitPoint; i++) {
			lowerDeck.add(population.get(i));
		}
		for (int i=splitPoint; i<population.size(); i++) {
			upperDeck.add(population.get(i));
		}
		population=new ArrayList<Object>();
		for (Object o: upperDeck) {
			population.add(o);
		}
		for (Object o: lowerDeck) {
			population.add(o);
		}
		return population;
	}

	public ArrayList<Agent> shuffle(ArrayList<Agent> population, int i) { // Int does nothing
		ArrayList<Object> objects=new ArrayList<Object>();
		for (Agent a: population) {
			objects.add((Object)a);
		}
		objects=shuffle(objects);
		population=new ArrayList<Agent>();
		for (Object o: objects) {
			population.add((Agent)o);
		}
		return population;
	}

	public ArrayList<Retailer> shuffle(ArrayList<Retailer> population, double d) { // Double does nothing
		ArrayList<Object> objects=new ArrayList<Object>();
		for (Retailer a: population) {
			objects.add((Object)a);
		}
		objects=shuffle(objects);
		population=new ArrayList<Retailer>();
		for (Object o: objects) {
			population.add((Retailer)o);
		}
		return population;
	}

	// THE ECONOMY KEEPS SOME DATA ABOUT ITSELF
	
	// HISTORICAL DATA
	public ArrayList<Double> gdpDemandSide; // GDP measured as total payments by consumers for final goods (demand)
	public ArrayList<Double> gdpSupplySide; // GDP measured as total payments to producers (supply)
	public ArrayList<Double> totWagesHistory;
	public ArrayList<Double> totProfitHistory;
	public ArrayList<Double> totRetailerCash; // Cash in the hands of retailers 
	public ArrayList<Double> totAgentCash; // Cash in the hands of agents
	public ArrayList<Double[]> historicalPrices; // The historical price levels for each good (to consumers)
	public ArrayList<Double[]> historicalSales; // The historical volume of (final) sales for each industry
	public ArrayList<Double[]> laborByGood; // The average amount of labor dedicated to the production of each good
	public ArrayList<Double[]> consumptionByGood; // The average consumption of each good
	public ArrayList<Double[][]> laborByOSM; // The shares of labor devoted to (a) overhead, (b) home production, (c) market labor
	public ArrayList<Double[]> retailerAccess; // The share of agents that know at least one retailer of each good
	public ArrayList<Double> averageUtility;
	public ArrayList<Double> averageUtility_retailers;
	public ArrayList<Double> averageUtility_nonRetailers;
	public ArrayList<Double> medianUtility;
	public ArrayList<Integer> numConsumables; // The number of industries in which average consumption is greater than zero
	public ArrayList<Integer> numTradables; // The number of industries in which there are active retailers
	public ArrayList<Boolean[]> industryStatus_consumed;
	public ArrayList<Boolean[]> industryStatus_traded;
	public ArrayList<Double> avgNumberGoodsProduced; // The average number of different goods which an agent produces in each period
	public ArrayList<Double> avgNumberGoodsConsumed; // The average number of different goods which an agent consumes in each period
	public ArrayList<Double> priceToOverhead; // The "specialist wage" for a transaction is price times average labor supply - overhead labor; weighted by transaction qty
	public ArrayList<Double> totCapitalRetailers;
	public ArrayList<Double> totCapitalNonRetailers;
	public ArrayList<Integer> countRetailers;
	public ArrayList<Integer> countNonRetailers;
	public ArrayList<ArrayList<Double[]>>[] industryPriceHistories; // an array (by good) of ArrayLists (turns) of ArrayLists (retailers) of arrays (each data point consists of ID, price)
	public ArrayList<Double[]>[] industryHistoricalProfiles; // an array (by good) of ArrayLists (turns) of arrays of Doubles, where the items represent:
		// 0=turn, 1=number consuming, 2=number producing, 3=number buying, 4=number selling, 5=tot. qty made
		// 6=tot. qty consumed, 7=total qty bought, 8=tot. quantity sold, 9=tot. qty paid, 10=tot. qty earned,
		// 11=number of retailers, 12=average price, 13=average (marginal) wage (or supply price)
	
	public void setUpDataContainers() {
		// Create data containers
		gdpDemandSide=new ArrayList<Double>();
		gdpSupplySide=new ArrayList<Double>();
		totWagesHistory=new ArrayList<Double>();
		totProfitHistory=new ArrayList<Double>();
		totRetailerCash=new ArrayList<Double>();
		totAgentCash=new ArrayList<Double>();
		totCapitalRetailers=new ArrayList<Double>();
		totCapitalNonRetailers=new ArrayList<Double>();
		countRetailers=new ArrayList<Integer>();
		countNonRetailers=new ArrayList<Integer>();
		historicalPrices=new ArrayList<Double[]>();
		historicalSales=new ArrayList<Double[]>();
		laborByGood=new ArrayList<Double[]>();
		consumptionByGood=new ArrayList<Double[]>();
		laborByOSM=new ArrayList<Double[][]>();
		retailerAccess=new ArrayList<Double[]>();
		averageUtility=new ArrayList<Double>();
		averageUtility_retailers=new ArrayList<Double>();
		averageUtility_nonRetailers=new ArrayList<Double>();
		medianUtility=new ArrayList<Double>();
		numConsumables=new ArrayList<Integer>();
		numTradables=new ArrayList<Integer>();
		industryStatus_consumed=new ArrayList<Boolean[]>();
		industryStatus_traded=new ArrayList<Boolean[]>();
		avgNumberGoodsProduced=new ArrayList<Double>();
		avgNumberGoodsConsumed=new ArrayList<Double>();
		priceToOverhead=new ArrayList<Double>();
		industryPriceHistories=(ArrayList<ArrayList<Double[]>>[]) new ArrayList[numGoods];
		for (int i=0; i<numGoods; i++) {
			industryPriceHistories[i]=new ArrayList<ArrayList<Double[]>>();
		}
		industryHistoricalProfiles=(ArrayList<Double[]>[]) new ArrayList[numGoods];
		for (int i=0; i<numGoods; i++) {
			industryHistoricalProfiles[i]=new ArrayList<Double[]>();
		}
	}
	
	public void collectStats() { // Called at the end of run()
		boolean testing=false;
		if (testing) System.out.println("Economy.collectStats()");
		ArrayList<Double[]>[] industryPriceHistories_update=(ArrayList<Double[]>[]) new ArrayList[numGoods];
		if (industryPriceHistories!=null) {
			for (int i=0; i<numGoods; i++) {
				industryPriceHistories_update[i]=new ArrayList<Double[]>();
			}
			for (Trader t: traders) {
				Retailer r=(Retailer) t;
				double id=(double) r.id;
				double price=r.salesManager.getPrice();
				Double[] point={id,price};
				industryPriceHistories_update[r.r_product].add(point);
			}
			for (int i=0; i<numGoods; i++) {
				industryPriceHistories[i].add(industryPriceHistories_update[i]);
			}			
		}
		if (industryHistoricalProfiles!=null) {
			int[] numConsuming=new int[numGoods];
			int[] numProducing=new int[numGoods];
			int[] numBuying=new int[numGoods];
			int[] numSelling=new int[numGoods];
			double[] totMade=new double[numGoods];
			double[] totConsumed=new double[numGoods];
			double[] totBought=new double[numGoods];
			double[] totSold=new double[numGoods];
			double[] totPaid=new double[numGoods];
			double[] totEarned=new double[numGoods];
			int[] numRetailers=new int[numGoods];
			for (int i=0; i<numGoods; i++) {
				numConsuming[i]=0;
				numProducing[i]=0;
				numBuying[i]=0;
				numSelling[i]=0;
				totConsumed[i]=0;
				totMade[i]=0;
				totBought[i]=0;
				totSold[i]=0;
				totPaid[i]=0;
				totEarned[i]=0;
				numRetailers[i]=0;
			}
			for (Agent a: population) {
				Integer retailedGood=-99;
				if (a.enterprises.size()>0) retailedGood=((Retailer)a.enterprises.get(0)).r_product;
				for (int i=0; i<numGoods; i++) {
					if (a.cons.used[i]>0) numConsuming[i]++;
					if (a.labor.labor[i]>0) numProducing[i]++;
					if (a.bought[i]>0) numBuying[i]++;
					if (a.sold[i]>0) numSelling[i]++;
					totMade[i]=totMade[i]+Math.max(0, a.labor.labor[i]-productionFunction.getL0(i));
					totConsumed[i]=totConsumed[i]+a.cons.used[i];
					totBought[i]=totBought[i]+a.bought[i];
					totSold[i]=totSold[i]+a.sold[i];
					totPaid[i]=totPaid[i]+a.paid[i];
					totEarned[i]=totEarned[i]+a.earned[i];
					if (i==retailedGood) numRetailers[i]++;
				}
			}
			double[] avgPrice=new double[numGoods]; // the total paid (in money) over the total bought (by agents)
			double[] avgWage=new double[numGoods]; // the total earned (in money) over the total sold (by agents)
			Double[][] industryHistoricalProfiles_update=new Double[numGoods][14];
			for (int i=0; i<numGoods; i++) {
				if (totBought[i]>0) avgPrice[i]=totPaid[i]/totBought[i];
				if (totSold[i]>0) avgWage[i]=totEarned[i]/totSold[i];
				Double[] update_i={Double.valueOf(turn), 			// item 0: turn
						Double.valueOf((double)numConsuming[i]),	// item 1: number of agents consuming
						Double.valueOf((double)numProducing[i]),	// item 2: number of agents producing
						Double.valueOf((double)numBuying[i]),		// item 3: number of agents buying
						Double.valueOf((double)numSelling[i]),		// item 4: number of agents selling
						totMade[i],									// item 5: total qty made
						totConsumed[i],								// item 6: total qty consumed
						totBought[i],								// item 7: total qty bought
						totSold[i],									// item 8: total qty sold
						totPaid[i],									// item 9: total qty paid
						totEarned[i],								// item 10: total qty earned
						Double.valueOf((double)numRetailers[i]),	// item 11: number of retailers
						avgPrice[i],								// item 12: average price
						avgWage[i]									// item 13: average wage
				};
				industryHistoricalProfiles_update[i]=update_i;
			}
			for (int i=0; i<numGoods; i++) {
				industryHistoricalProfiles[i].add(industryHistoricalProfiles_update[i]);
			}
		}
		if (gdpDemandSide==null) setUpDataContainers();
		// Some of the above fields are populated from the economy's transactions record
		Double totWages=0.0;
		Double[] sales_volume=new Double[numGoods];
		Double[] sales_value=new Double[numGoods];
		Double[] supply_volume=new Double[numGoods];
		for (int i=0; i<numGoods; i++) {
			sales_volume[i]=0.0;
			sales_value[i]=0.0;
			supply_volume[i]=0.0;
		}
		Double priceToOverhead_numerator=0.0;
		Double priceToOverhead_denominator=0.0;
		for (Transaction t: transactionRecords) {
			if (t.pay==money) {
				sales_volume[t.sell]=sales_volume[t.sell]+t.sellQty; // Calculate the total volume of sales for each good
				sales_value[t.sell]=sales_value[t.sell]+t.payQty; // Calculate the total value of sales for each good
			}
			if (t.sell==money) {
				totWages=totWages+t.sellQty; // Total money paid out by retailers to suppliers of goods
				supply_volume[t.pay]=supply_volume[t.pay]+t.payQty; // Calculates the total "paid" in various goods by agents to retailers in return for money
				priceToOverhead_numerator=priceToOverhead_numerator+t.sellQty/productionFunction.getL0(t.pay);
				priceToOverhead_denominator=priceToOverhead_denominator+t.payQty;
			}
		}
		Double totProfit=0.0;
		for (Trader t: traders) {
			Retailer r=(Retailer) t;
			int last=r.dividendHistory.size()-1;
			if (last>=0) {
				double profit=r.dividendHistory.get(r.dividendHistory.size()-1);
				totProfit=totProfit+profit;
			}
		}
		if (testing) System.out.println("Economy.collectStats(). Line 424");
		if (priceToOverhead_denominator==0) priceToOverhead.add(1.0);
		else priceToOverhead.add(priceToOverhead_numerator/priceToOverhead_denominator);
		historicalSales.add(sales_volume);
		Double[] avgPrices=new Double[numGoods];
		Double gdpDemandSide_update=0.0;
		avgPrices[0]=-99.0;
		for (int i=1; i<numGoods; i++) { // Omit money from this calculation
			if (sales_volume[i]>0) avgPrices[i]=sales_value[i]/sales_volume[i];
			gdpDemandSide_update=gdpDemandSide_update+sales_value[i];
		}
		gdpSupplySide.add(totWages+totProfit);
		totWagesHistory.add(totWages);
		totProfitHistory.add(totProfit);
		historicalPrices.add(avgPrices);
		gdpDemandSide.add(gdpDemandSide_update);
		// Cash stock variables are populated by looking at agents' and retailers' inventories
		Double totRetailerCash_update=0.0;
		Double totAgentCash_update=0.0;
		for (Trader t: traders) {
			totRetailerCash_update=totRetailerCash_update+t.inventory(money);
		}
		if (testing) System.out.println("Economy.collectStats(). Line 444");
		Double totCapitalRetailers_update=0.0;
		Double totCapitalNonRetailers_update=0.0;
		Integer countRetailers_update=0;
		Integer countNonRetailers_update=0;
		for (Agent a: population) {
			totAgentCash_update=totAgentCash_update+a.has[money];
			if (a.enterprises.size()>0) {
				totCapitalRetailers_update=totCapitalRetailers_update+a.has(capital);
				countRetailers_update++;
			}
			else {
				totCapitalNonRetailers_update=totCapitalNonRetailers_update+a.has(capital);
				countNonRetailers_update++;
			}
		}
		totCapitalRetailers.add(totCapitalRetailers_update);
		totCapitalNonRetailers.add(totCapitalNonRetailers_update);
		countRetailers.add(countRetailers_update);
		countNonRetailers.add(countNonRetailers_update);
		totRetailerCash.add(totRetailerCash_update);
		totAgentCash.add(totAgentCash_update);
		// Some are populated from the agent's labor reports 
		// (though supply data from the transaction records is also used for Labor by OSM)
		Double[] laborByGood_total=new Double[numGoods];
		for (int i=0; i<numGoods; i++) {
			laborByGood_total[i]=0.0;			
		}
		Double[][] laborByOSM_update=new Double[numGoods][4];
		Integer sumNumberGoodsProduced=0;
		for (int i=0; i<4; i++) { // 0=overhead; 1=self-supply; 2=market sales
			for (int j=0; j<numGoods; j++) {
				laborByOSM_update[j][i]=0.0;							
			}
		}
		for (Agent a: population) {
			LaborReport l=a.labor;
			for (int i=2; i<numGoods; i++) {  
				double labor_good_i=l.labor[i]; // First, find the total labor exerted by the agent for good i
				laborByGood_total[i]=laborByGood_total[i]+labor_good_i; // Add this to the total labor figure for all agents in industry i
				if (labor_good_i>0) {
					double overhead=((AvoidCostPF) a.makes()).getL0(i);
					double production=labor_good_i-overhead;
					double consumption=a.cons.used[i];
					double selfsupply=Math.min(production,consumption);
					double market=production-selfsupply;
					laborByOSM_update[i][0]=laborByOSM_update[i][0]+overhead; // Add up all the overhead devoted to each good
					laborByOSM_update[i][1]=laborByOSM_update[i][1]+selfsupply; // Add up the amounts of labor used to produce goods that were then consumed at home
					laborByOSM_update[i][2]=laborByOSM_update[i][2]+market; // Add up any surplus of home-produced goods over and above what was consumed; this must have been sold
				}
				if (l.sellGood==i) laborByOSM_update[i][3]=laborByOSM_update[i][3]+l.retailing;
				if (labor_good_i>0) sumNumberGoodsProduced++;
			}
		}
		Double avgNumberGoodsProduced_update=(Double) sumNumberGoodsProduced.doubleValue()/population.size();
		avgNumberGoodsProduced.add(avgNumberGoodsProduced_update);
		laborByGood.add(laborByGood_total);
		laborByOSM.add(laborByOSM_update);
		// Now collect data from the agents' consumption reports
		Double[] consumptionByGood_update=new Double[numGoods];
		int[] countRetailerAccess=new int[numGoods]; // Number of agents with access to retailers of each good
		for (int i=0; i<numGoods; i++) {
			consumptionByGood_update[i]=0.0;
			countRetailerAccess[i]=0;
		}
		ArrayList<ConsumptionReport> allConsumers=new ArrayList<ConsumptionReport>();
		Integer sumNumberOfGoodsConsumed=0;
		if (testing) System.out.println("Economy.collectStats(). Line 495");
		Double totalUtility=0.0; 
		Double totalUtility_retailers=0.0;
		Double totalUtility_nonRetailers=0.0;
		int countRetailers=0;
		int countNonRetailers=0;
		for (Agent a: population) {
			ConsumptionReport cons=a.cons;
			totalUtility=totalUtility+Math.max(cons.utility,0); // "Data cleaning" is in order if utility < 0 is seen.
			if (a.enterprises.size()>0) {
				totalUtility_retailers=totalUtility_retailers+Math.max(cons.utility, 0);
				countRetailers++;
			}
			if (a.enterprises.size()==0) {
				totalUtility_nonRetailers=totalUtility_nonRetailers+Math.max(cons.utility, 0);
				countNonRetailers++;
			}
			boolean[] retailerAccess_a=new boolean[numGoods]; 
			for (int i=0; i<numGoods; i++) {
				retailerAccess_a[i]=false;
			}
			for (Trader r: a.knows) {
				int offer=r.tradeOffers().get(0).sell; // Find out what the retailer is selling
				if (offer==money) offer=r.tradeOffers().get(1).sell;
				retailerAccess_a[offer]=true; // Record that the agent knows a trader (at least one) of the offer good
			}
			for (int i=0; i<numGoods; i++) {
				if (retailerAccess_a[i]) countRetailerAccess[i]++; // Increment the retailer access inventory for good i if the agent knows a trader in it
				consumptionByGood_update[i]=consumptionByGood_update[i]+cons.used[i];
				if (cons.used[i]>0) sumNumberOfGoodsConsumed++;
			}
			allConsumers.add(cons);
		}
		Double avgNumberGoodsConsumed_update=(Double) sumNumberOfGoodsConsumed.doubleValue()/population.size();
		avgNumberGoodsConsumed.add(avgNumberGoodsConsumed_update);
		consumptionByGood.add(consumptionByGood_update);
		averageUtility.add(totalUtility/population.size());
		averageUtility_retailers.add(totalUtility_retailers/countRetailers);
		averageUtility_nonRetailers.add(totalUtility_nonRetailers/countNonRetailers);
		Collections.sort(allConsumers);
		ConsumptionReport median=allConsumers.get((int)(allConsumers.size()/2));
		medianUtility.add(median.utility);
		Double[] retailerAccess_update=new Double[numGoods];
		for (int i=0; i<numGoods; i++) {
			retailerAccess_update[i]=((double)countRetailerAccess[i])/population.size();
		}
		retailerAccess.add(retailerAccess_update);
		Boolean[] industryStatus_consumed_update=new Boolean[numGoods];
		Boolean[] industryStatus_traded_update=new Boolean[numGoods];
		for (int i=0; i<numGoods; i++) {
			industryStatus_consumed_update[i]=false;
			industryStatus_traded_update[i]=false;
		}
		for (Trader t: traders) {
			Retailer r=(Retailer) t;
			int product=r.r_money;
			if (product==money) product=r.r_product;
			industryStatus_traded_update[product]=true;
		}
		for (Agent a: population) {
			ConsumptionReport cons=a.cons;
			for (int i=0; i<numGoods; i++) {
				if (cons.used[i]>0) industryStatus_consumed_update[i]=true;
			}
		}
		Integer consumables=0;
		Integer tradables=0;
		for (int i=2; i<numGoods; i++) {
			if (industryStatus_consumed_update[i]) consumables++;
			if (industryStatus_traded_update[i]) tradables++;
		}
		numTradables.add(tradables);
		numConsumables.add(consumables);		
		industryStatus_consumed.add(industryStatus_consumed_update);
		industryStatus_traded.add(industryStatus_traded_update);
		if (testing) System.out.println("Economy.collectStats(). Line 555 (END)");
	}

	public void registerTransaction(int[] goods,double[] qtys) { // This is called by the Transactions object every time a purchase occurs
		tradeVolumes[goods[0]]=tradeVolumes[goods[0]]+qtys[0];
		tradeVolumes[goods[1]]=tradeVolumes[goods[1]]+qtys[1];
		transactionProfile[goods[0]]++;
		transactionProfile[goods[1]]++;
	}
	
	public UtilityFunction getUtilityFunction() { // In the basic, non-extended world, the utility function is symmetric.
		return utilityFunction;
	}

	// MANAGING POINTERS TO AGENTS AND RETAILERS
	
	public void register(Retailer r) {
		boolean testing=false;
		retailers.add(r);
		traders.add(r);
		r.id=nextRetailerID;
		nextRetailerID++;
		if (testing&(Math.random()<0.1)) r.testAll=true;
	}
	
	public void traderExit(Trader t) {
		traders.remove(t);
		retailers.remove((Retailer)t);
	}
	
	public void register(Agent a) { // Called by new Agents to register their existence with the Economy
		population.add(a);
		a.id=nextAgentID;
		nextAgentID++;
	}
	
	public void resetFriends() {
		for (Agent a: population) {
			int count1=0;
			int count2=0;
			a.friends=new ArrayList<Agent>();
			while ((count1<FRIENDS_MAX)&(count2<1000)) {
				int selector=(int) (Math.random()*population.size());
				Agent b=population.get(selector);
				if ((b.id!=a.id)&(a.friends.contains(b)==false)) {
					a.friends.add(b);
					count1++;
				}
				count2++;
			}
		}		
	}
	
	public String toString() {
		String ret="Endogenous Division of Labor Economy\n";
		ret=ret+"Number of goods: "+numGoods+"\n";
		ret=ret+"Number of agents: "+population.size()+"\n";
		ret=ret+productionFunction.toString();
		ret=ret+utilityFunction.toString()+"\n";
		boolean allAgentsHavePFs=true; // pf = production function
		for (Agent a: population) {
			if (a.makes()==null) allAgentsHavePFs=false;
		}
		boolean allAgentsHaveUFs=true; // uf = utility function
		for (Agent a: population) {
			if (a.likes()==null) allAgentsHaveUFs=false;
		}
		ret=ret+"All agents have production functions: "+allAgentsHavePFs+"\n";
		ret=ret+"All agents have utility functions: "+allAgentsHaveUFs+"\n";
		return ret;
	}

}
