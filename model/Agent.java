package model;
import java.util.*;
import java.io.*;

public class Agent implements Serializable, Comparable<Agent> {
	private Economy economy;
	public double[] has; // the agent's property, with each position in the vector corresponding to a good
	private UtilityFunction likes; // likes and makes are fairly self-explanatory: utility and (home) production functions
	private ProductionFunction makes; 
	private double[] owes;
	public int id;
	public ConsumptionReport cons;
	public LaborReport labor;
	public double waste;
	public ArrayList<Trader> enterprises;
	public TurnHistory lastTurn;
	public String type;
//	public transient data_management.Diary diary;
	public boolean arbitrage_this_turn;
	public double oddsOfEntry_retail;
	
	// Transactions
	public ArrayList<Transaction> transactions;
	public Double[] bought;
	public Double[] paid;
	public Double[] sold;
	public Double[] earned;
	
	// DATA FROM EDOL OTPIMIZATION
	public ArrayList<TradingStep> shoppingPlan;
	public ScoredSet sellSet;
	public ScoredSet[] productionSets;
	public ArrayList<Integer> productionUniverse;
	
	// MEMORY MANAGEMENT
	public ArrayList<Trader> knows; // this records a network of traders of whom the agent knows, and with whom it may do business
	public ArrayList<Trader> shoppedAt; // this is where Transactions records the agent's shopping experiences.
	public ArrayList<Agent> friends;
	
	// HISTORY
	public ArrayList<ArrayList<Integer>> diverseConsHistory;
	public ArrayList<ArrayList<Integer>> diverseProdHistory;
	public ArrayList<Double> utilityHistory;
	public ArrayList<Double> moneyHistory;
	public ArrayList<Double> capitalHistory;
	
	// THE AGENT'S MAIN TASK IS TO FIND THE OPTIMAL CONSUMPTION AND LABOR PATTERN, AND THE OPTIMAL
	// NICHE IN THE DIVISION OF LABOR. THAT IS DONE HERE
		
	public void trade(Integer money) {
		boolean testing=false;
		if (testing) System.out.println("TESTING Agent.trade()");
		transactions=new ArrayList<Transaction>();
		economy.memoryModel.memory(this);
		lastTurn=new TurnHistory();
		lastTurn.initInventory=has.clone();
		shoppedAt=new ArrayList<Trader>();
		arbitrage_this_turn=false; // unless it turns out otherwise
		arbitrage(money);
		if (testing) System.out.println("About to start EDOL optimization");
		if (makes.type()=="AvoidCost") EDOL_Planning.manage(this);
		if (testing) System.out.println("Just finished with EDOL optimization");
		summarizeTransactions();
		if (enterprises.size()>0) ((Retailer)enterprises.get(0)).considerExit();
		diverseProdHistory.add(labor.reportDiversification());
		diverseConsHistory.add(cons.reportDiversification());
		utilityHistory.add(cons.utility);
		moneyHistory.add(has[0]);
		capitalHistory.add(has[0]);
	}

	public void summarizeTransactions() {
		int numGoods=economy.numGoods;
		bought=new Double[numGoods];
		paid=new Double[numGoods];
		sold=new Double[numGoods];
		earned=new Double[numGoods];
		for (int i=0; i<numGoods; i++) {
			bought[i]=0.0;
			paid[i]=0.0;
			sold[i]=0.0;
			earned[i]=0.0;
		}
		for (Transaction t: transactions) {
			if (t.pay==economy.money) { // if the agent initiated the transaction by paying money, that is, if it was buying...
				bought[t.sell]=bought[t.sell]+t.sellQty; // ... add what was sold to the agent to the total of what it bought...
				paid[t.sell]=paid[t.sell]+t.payQty; // ... and add what the agent paid for the good to the total of what was paid...
			}
			if (t.sell==economy.money) { // ... while if the agent was transacting with a "seller of money," i.e., if the agent was supplying goods to a retailer...
				sold[t.pay]=sold[t.pay]+t.payQty; // ... the quantity "paid" (in goods) for the money is added to the agent's sales of that good...
				earned[t.pay]=earned[t.pay]+t.sellQty; // ... while the amount of money the agent received is added to his earnings for that good
			}
		}
	}

	public void arbitrage(Integer money) {
		boolean testing=false;
		double minQty=0.0000000000001;
		// Look for trade offers involving money.
		TradeOffer[] bestSeller=new TradeOffer[economy.numGoods];
		TradeOffer[] bestBuyer=new TradeOffer[economy.numGoods];
		boolean arbex=false; // varname means "arbitrage exhausted"
		int count=0;
		Y: while (arbex==false) {
			count++;
			ArrayList<TradeOffer> sellForMoney=new ArrayList<TradeOffer>();
			ArrayList<TradeOffer> buyForMoney=new ArrayList<TradeOffer>();
			for (Trader t: knows) {
				for (TradeOffer offer: t.tradeOffers()) { // only include agents with positive inventory
					if ((offer.pay==money)&(offer.trader.inventory(offer.sell)>minQty)) sellForMoney.add(offer);
					if ((offer.sell==money)&(offer.trader.inventory(offer.sell)>minQty)) buyForMoney.add(offer);
				}
			}
			if (testing==true) {
				System.out.println("\nSELLERS -- ALL");
//				OutputOMatic.displayPath(sellForMoney);
				System.out.println("\nBUYERS -- ALL");
//				OutputOMatic.displayPath(buyForMoney);
			}
			// Sort the trade offers by the good that's being bought or sold.
			bestSeller=new TradeOffer[economy.numGoods];
			for (TradeOffer t: sellForMoney) {
				int sellGood=t.sell;
				if (bestSeller[sellGood]==null) {
					bestSeller[sellGood]=t;
				} else {
					if (bestSeller[sellGood].getPrice()>t.getPrice()) bestSeller[sellGood]=t;
				}
			}
			bestBuyer=new TradeOffer[economy.numGoods];
			for (TradeOffer t: buyForMoney) {
				int buyGood=t.pay;
				if (bestBuyer[buyGood]==null) {
					bestBuyer[buyGood]=t;
				} else {
					if (bestBuyer[buyGood].getPrice()<t.getPrice()) bestBuyer[buyGood]=t;
				}
			}
			ArrayList<Integer> arbitrage=new ArrayList<Integer>(); // to contain the ids of goods that can be arbitraged
			arbex=true;
			for (int i=0; i<economy.numGoods; i++) {
				if ((bestBuyer[i]!=null)&(bestSeller[i]!=null)) {
					if ((bestBuyer[i].getPrice()*bestSeller[i].getPrice()<1)
							&(bestBuyer[i].limit()>0)&(bestSeller[i].limit()>0)
							&(bestBuyer[i].getPrice()<Double.POSITIVE_INFINITY)&
							(bestSeller[i].getPrice()<Double.POSITIVE_INFINITY)) {
						arbitrage.add(i); 
						arbitrage_this_turn=true;
						arbex=false;
					}					
				}
			}
			if (arbex==false) {
				int selector=(int) (Math.random()*arbitrage.size()); // rotate through the arbitrage opportunities, starting at a random place
				for (int i=0; i<arbitrage.size(); i++) {
					int selector2=(selector+i)%arbitrage.size();
					Integer arbGood=arbitrage.get(selector2); // arbGood means "arbitrage good"
					arbitrage_transaction(money,bestSeller[arbGood],bestBuyer[arbGood]);
				}
			}
			if (count>20) break Y;
		}
	}
	
	public void arbitrage_transaction(Integer money,TradeOffer seller,TradeOffer buyer) {
		boolean testing=false;
		// This method exploits an "arbitrage opportunity" by buying a good and selling it at a higher price
		// First it needs to check that the arbitrage opportunity has been correctly identified
		boolean real_ao=(seller.pay==money)&(buyer.sell==money)&(seller.sell==buyer.pay)&(seller.getPrice()*buyer.getPrice()<1);
		if (real_ao==false) {
			System.out.println("Agent.arbitrage_simple(). Arbitrage oppotunity mistakenly identified.");
			System.exit(1);
		}
		if (testing==true) System.out.println("Arbitrage. good: "+seller.sell+". low seller's price: "+
				seller.getPrice()+". high buyer's price: "+(1/buyer.getPrice()));
		// Find the maximum, of money, that can be bought, and sold
		double maxBuy=seller.limit()*seller.getPrice(); // Value of maximum qty that can be bought
		double maxSell=buyer.limit(); // Maximum qty of money that the buyer will "sell" for the good
		double value=Math.min(maxBuy,maxSell);
		if (testing==true) System.out.println("buyer has: "+buyer.limit()+". seller has: "+seller.limit()
				+". value of arbitrage opportunity: "+value);
		Transactions.borrow(this,money,value);
		if ((buyer.getPrice()==Double.POSITIVE_INFINITY)|(seller.getPrice()==Double.POSITIVE_INFINITY)) return;
		try {
			if (testing==true) {
				System.out.println("arbitrage. step 1 inventory");
			}
			Transactions.buy(this,seller,value);	
			if (testing==true) {
				System.out.println("arbitrage. step 2 inventory");
			}
			double qty=value/seller.getPrice(); // the seller's price is money/goods
			Transactions.buy(this,buyer,qty); // now there should be a little extra money in inventory
			if (testing==true) {
				System.out.println("arbitrage. step 3 inventory");
			}
		} catch(BuyNegativeQtyException ex) {
			System.out.println("Agent.arbitrage_simple(). Agent "+id+" tried to buy a negative qty of good "+seller.sell);
			ex.printStackTrace();
			System.exit(1);
		} catch(SellerHasNoTraderException ex) {
			System.out.println("Agent.arbitrage_simple(). Agent "+id+" tried to buy a negative qty of good "+seller.sell);
			ex.printStackTrace();
			System.exit(1);
		} catch (Exception ex) {
			System.out.println("value="+value+". seller.getPrice()="+seller.getPrice());
			ex.printStackTrace();
			System.exit(1);
		}
		try {
			Transactions.repay(this,money,value);			
		} catch(Exception ex) {
			ex.printStackTrace();
			System.exit(1);
		}
	}

	// GETTERS AND SETTERS
	
	public int compareTo(Agent other) {
		if (other.id<id) return 1;
		return -1;
	}
	
	public Economy getEconomy() {
		return economy;
	}
	// No setter for economy, since the agent should never be transferred between economies
	
	public double laborSupply(LaborReport report) { // To be called by ProductionFunction when actually making the goods
		boolean testing=false;
		if (economy.capital!=null) {
			double totalLabor=1+Math.pow(has[economy.capital],economy.CAPITAL_EXPONENT);
			if (enterprises.size()>0) {
				report.sellGood=((Retailer) enterprises.get(0)).r_product;
				report.retailing=economy.RETAILER_TIME_COST*totalLabor;
				totalLabor=(1-economy.RETAILER_TIME_COST)*totalLabor; // 80% of labor goes to running the shop if one is a retailer.
			}
			if (testing) {
				System.out.println("TESTING Agent.laborSupply(report). id="+id+". capital="+has[economy.capital]+". total labor="+totalLabor);
				System.out.println("    retailer="+(enterprises.size()>0)+". labor devoted to retailing="+report.retailing
						+". RETAILER_TIME_COST="+economy.RETAILER_TIME_COST+". report.sellGood="+report.sellGood);
			}
			return totalLabor;
		}
		if (enterprises.size()>0) return (1-economy.RETAILER_TIME_COST); // NOT RECOMMENDED TO ARRIVE HERE
		return 1;
	}
	
	public double laborSupply() {
		boolean testing=false;
		if (economy.capital!=null) {
			double totalLabor=1+Math.pow(has[economy.capital],economy.CAPITAL_EXPONENT);
			if (enterprises.size()>0) totalLabor=(1-economy.RETAILER_TIME_COST)*totalLabor; // 80% of labor goes to running the shop if one is a retailer.
			if (testing) System.out.println("TESTING Agent.laborSupply(). id="+id+". capital="+has[economy.capital]+". total labor="+totalLabor);
			return totalLabor;
		}
		if (enterprises.size()>0) return (1-economy.RETAILER_TIME_COST); // NOT RECOMMENDED TO ARRIVE HERE
		return 1;
	}

	public double rawLaborSupply() { // Ignores whether the labor is being spent on retailing
		if (economy.capital!=null) return (1+Math.pow(has[economy.capital],economy.CAPITAL_EXPONENT));
		return 1;
	}

	public UtilityFunction likes() {
		return likes;
	}
	
	public void setUtilityFunction(UtilityFunction u) {
		likes=u;
	}
	
	public ProductionFunction makes() {
		return makes;
	}
	
	public void setProductionFunction(ProductionFunction p) {
		makes=p;
	}
	
	public double owes(int good) {
		return owes[good];
	}
	
	public void borrows(int good, double qty) throws Exception {
		owes[good]=owes[good]+qty;
		if (owes[good]<0) throw new Exception();
	}
	
	public double has(int good) {
		double ret=has[good];
		return ret;
	}
	
	public void gets(int good, double qty) throws Exception { // + = gets, - = gives
		double epsilon=0.0000000001;
		if (((qty<0)|(qty==0)|(qty>0))==false) {
			System.out.println("\nERROR IN Agent.gets(). Tried to add qty="+qty+" of good "+good);
			System.out.println("AGENT ID="+id+". Good="+good+". Has="+has(good)+". money="+has(0));
			throw new Exception();
		}
		has[good]=has[good]+qty;
		if ((has[good]<0)&(has[good]>epsilon)) has[good]=0;
		if (has[good]<0) {
			System.out.println("Agent 'gets' "+qty+". Now has "+has[good]);
			throw new Exception();
		}
	}
	
	public void depreciate(Integer capital) { // 10% of capital depreciates each turn.
		has[capital]=0.9*has[capital];
	}
	
	public Agent(Economy w) {
		economy=w;
		type="Normal";
		w.register(this);
		// In addition to knowing what world it is living in, the agent needs to have baskets for each available good.
		has=new double[economy.numGoods];
		owes=new double[economy.numGoods];
		for (int i=0; i<has.length; i++) {
			has[i]=0;
			owes[i]=0;
		}
		// Utility functions, but not production functions, are assigned in the constructor.
		likes=economy.getUtilityFunction();
		// It also needs an address book, albeit initially blank, for traders with whom it will do business.
		knows=new ArrayList<Trader>();
		enterprises=new ArrayList<Trader>();
		cons=new ConsumptionReport(has.length);
//		goalPaths=0;
//		loops=0;
		friends=new ArrayList<Agent>();
		shoppedAt=new ArrayList<Trader>();
//		diary=new data_management.Diary("Agent "+id+" Diary");
		productionSets=new ScoredSet[5];
		diverseConsHistory=new ArrayList<ArrayList<Integer>>();
		diverseProdHistory=new ArrayList<ArrayList<Integer>>();
		utilityHistory=new ArrayList<Double>();
		moneyHistory=new ArrayList<Double>();
		capitalHistory=new ArrayList<Double>();
	}
	public Agent(Economy w, UtilityFunction u) {
		this(w);
		likes=u;
	}
	
	// How the agent finds out about new retailers
	public void learnAbout(Trader t) {
		boolean thisIsNews=true;
		for (Trader trader: knows) {
			if (trader.equals(t)) thisIsNews=false;
		}
		if (thisIsNews==true) knows.add(t);
	}
	public void learnAbout(ArrayList<Trader> ts) {
		for (Trader t: ts) {
			learnAbout(t);
		}
	}
	
	public void roundDownInventories() {
		boolean testing=false;
		double epsilon=0.00001;
		for (int i=0; i<has.length; i++) {
			boolean money=false;
			if (economy.money!=null) { if (i==economy.money) {money=true;}}
			if ((money==false)&(has[i]<epsilon)) {
				has[i]=0;		
				if (testing==true) System.out.println("INVENTORY ROUNDED DOWN");
			}
		}
	}

	public void consume() {
		boolean testing=false;
		for (int i=0; i<has.length; i++) {
			try {
				gets(i,-owes[i]);		// Pay any outstanding debts (denominated in goods)		
			} catch(Exception ex) {
				ex.printStackTrace();
				System.exit(1);
			}
		}
		try { // Actually calculating utility, as well as saving/investment, is outsourced to the utility function
			cons=likes.consume(this, 				// The utility function is given access to the agent's goods...
					economy.money,					// ... is informed which good is (non-consumable, non-perishable) money...
					economy.capital,				// ... is informed which good is capital...
					economy.SAVINGS_RATE,			// ... is informed of the savings rate...
					economy.CAPITAL_CONVERSION_RATE,	// ... is informed of the rate at which foregone utility is converted to capital...
					economy.DEPRECIATION_RATE		// ... and is informed of the depreciation rate...
					);								// ... with this info, UtilityFunction can execute the agent's consumption and savings behavior
		} catch(Exception ex) {
			System.out.println("EXCEPTION AT Agent.consume().");
			ex.printStackTrace();
			System.exit(1);
		}
		if (testing==true) System.out.println("utility="+cons.utility);
		if (lastTurn!=null) lastTurn.consumption=has.clone();
		// If there is capital in this economy, "save" a portion of consumption
	}

	public double[] initInventory() {
		return lastTurn.initInventory;
	}

	public double[] consumption() {
		return lastTurn.consumption;
	}

	public double[] arbitrageEarnings() {
		return lastTurn.arbitrageEarnings;
	}
	
	public int transactions() {
		return lastTurn.transactions;
	}
	
	class TurnHistory implements Serializable {
		double[] initInventory;
		double[] consumption;
		double[] arbitrageEarnings;
		int transactions;
		
		public TurnHistory() {
			arbitrageEarnings=new double[has.length];
			for (int i=0; i<has.length; i++) {
				arbitrageEarnings[i]=0;
			}
			transactions=0;
		}
	}
}
