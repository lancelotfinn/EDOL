package model;
import java.util.*;
import java.io.Serializable;
import java.text.*;
 
public class Retailer extends GenericTrader implements Comparable<Retailer>, Serializable  {
	public Economy world;
	boolean records;

	// Essential inputs to retailer strategy.
	public double basePrice;
	public int r_money; // One of the goods that every retailer trades is money
	public int r_product; // The other good that every retailer trades is its product
	public double income;
	public double supply;
	public int cashouts; // This and the next count the number of stockouts that occur in any given turn.
	public int stockouts; 
	public ArrayList<Integer> stockoutsHistory;
	public ArrayList<Integer> cashoutsHistory;
	public ArrayList<Double> basePriceHistory;
	public ArrayList<Double> salesPriceHistory;
	public ArrayList<Double> supplyPriceHistory;
	public ArrayList<Double> incomeHistory;
	public ArrayList<Double> supplyHistory;
	public ArrayList<Double> cashHistory;
	public ArrayList<Double> inventoryHistory;
	public ArrayList<Double> dividendHistory;
	public ArrayList<Integer> modeHistory;
	public ArrayList<Integer> numTransHistory;
	public ArrayList<Transaction> lastTurnTransactions;
	public ArrayList<Double> oddsOfExitHistory;
	public TradeOffer purchasingManager;
	public TradeOffer salesManager;
	public Retailer emulate;
	
	public double markup; // a key parameter: how greedy is the retailer?
	public boolean defunct;
	public int lifespan;
	public int option; // 1 = normal business conditions; 2 = inventory problems; 3 = cash flow problems; 4 = just starting
	public double oddsOfExit;
	
	// For debugging
	boolean testAll;
	
	public int compareTo(Retailer other) {
		if (id<other.id) return -1;
		return 1;
	}
	
	public Retailer(Agent owner,Economy w,int g1,int g2) throws InvalidGoodException {
		super(owner,w);
		boolean testing=false;
		if (testing==true) System.out.println("Retailer founded. trades "+g1+" and "+g2);
		defunct=false;
		world=w;
		if (g1==w.money) {
			r_money=g1;
			r_product=g2;
		} else {
			r_money=g2;
			r_product=g1;
		}
		transactions=new ArrayList<Transaction>();
		numTransHistory=new ArrayList<Integer>();
		stockoutsHistory=new ArrayList<Integer>();
		cashoutsHistory=new ArrayList<Integer>();
		basePriceHistory=new ArrayList<Double>();
		salesPriceHistory=new ArrayList<Double>();
		supplyPriceHistory=new ArrayList<Double>();
		incomeHistory=new ArrayList<Double>();
		supplyHistory=new ArrayList<Double>();
		cashHistory=new ArrayList<Double>();
		inventoryHistory=new ArrayList<Double>();
		dividendHistory=new ArrayList<Double>();
		modeHistory=new ArrayList<Integer>();
		oddsOfExitHistory=new ArrayList<Double>();
		boolean invalidGood=false;
		if ((r_money<0)|(r_product<0)) invalidGood=true;
		if ((r_money>=w.numGoods)|(r_product>=w.numGoods)) invalidGood=true;
		if (invalidGood) throw new InvalidGoodException();
		income=0;
		supply=0;
		purchasingManager=new TradeOffer(this,r_money,r_product,1.1);
		salesManager=new TradeOffer(this,r_product,r_money,1.1);
		markup=world.MIN_STARTING_MARKUP+(world.MAX_STARTING_MARKUP-world.MIN_STARTING_MARKUP)*Math.random();
		oddsOfExit=world.MIN_RETAILER_EXIT_PROB;
		lifespan=0;
		cashouts=0;
		stockouts=0;
		testAll=false;
		w.register(this);
//		diary=new data_management.Diary("Retailer "+id+" Diary");
		records=w.retailRecords;
		owner.enterprises.add(this);
		startingPrice();
		if (testing==true) System.out.println("RETAILER FOUNDED, TRADING GOODS "+r_money+" AND "+r_product+" with buy price "+
				(1/tradeOffers().get(0).getPrice())+" and sell price "+tradeOffers().get(0).getPrice());
		if (basePrice==Double.NaN) {
			System.out.println("ERROR: Retailer is charging price NaN");
			Exception ex=new Exception();
			ex.printStackTrace();
			System.exit(1);
		}
	}
	
	public ArrayList<TradeOffer> tradeOffers() {
		ArrayList<TradeOffer> tradeoffers=new ArrayList<TradeOffer>();
		tradeoffers.add(purchasingManager);
		tradeoffers.add(salesManager);
		return tradeoffers;
	}
	
	public TradeOffer getPrice(int sell, int buy) {
		if ((sell==r_money)&(buy==r_product)) return salesManager;
		if ((sell==r_product)&(buy==r_money)) return purchasingManager;
		return null;
	}
	
	public void executeSale(double qty,double price,int pay,int sell) {
/* 
When the retailer makes a sale, it needs not only to deliver the goods and accept the payment, but also 
to keep track of how much is being supplied of each good, so that it can use that data to set prices during 
the next round.
 */
		boolean testing=false;
		if (testing|testAll) System.out.println("RETAILER "+id+". Execute sale. qty="+qty+". price="+price+". buy="+pay+". sell="+sell);
		this.increaseInventory(pay,qty*price);
		this.increaseInventory(sell,-qty);
		if (pay==r_money) income=income+qty*price;
		if (pay==r_product) supply=supply+qty*price;
		if (sell==r_money) {
			if (this.getInventory()[r_money]<0.00001) cashouts++;
		}
		if (sell==r_product) {
			if (this.getInventory()[r_product]<0.00001) stockouts++;
		}
		double epsilon=0.000001;
		if (this.getInventory()[sell]<epsilon) Transactions.dividend(this,owner,sell,this.getInventory()[sell]);
		if (this.getInventory()[pay]<epsilon) Transactions.dividend(this,owner,pay,this.getInventory()[pay]);
	}
	
	public void activate() {
		boolean testing=false;
		if (testing) System.out.println("TESTING Retailer.activate(). Retailer ID="+id);
		
		// ACCOUNTING
		stockoutsHistory.add(stockouts);
		stockouts=0;
		cashoutsHistory.add(cashouts);
		cashouts=0;
		incomeHistory.add(income);
		income=0;
		supplyHistory.add(supply);
		supply=0;
		basePriceHistory.add(basePrice);
		salesPriceHistory.add(salesManager.getPrice());
		supplyPriceHistory.add(1/purchasingManager.getPrice());
		cashHistory.add(inventory(r_money));
		inventoryHistory.add(inventory(r_product));
		numTransHistory.add(transactions.size());
		oddsOfExitHistory.add(oddsOfExit);
		
		// CASE IDENTIFICATION
		if (testing) System.out.println("TESTING Retailer.activate(). Line 157. Retailer ID="+id);
		option=4; // Option 4, where there is no data, is the default.
		int last=stockoutsHistory.size()-1;
		if (last>0) { // For the first couple of turns, stick with option 4
			Boolean stockout=stockoutsHistory.get(last)>0;
			Boolean cashout=cashoutsHistory.get(last)>0;
			Double supply=supplyHistory.get(last);
			Double income=incomeHistory.get(last);
			if (testing) System.out.println("last="+last+". stockout="+stockout+". cashout="+cashout+". supply="+supply);
			if (stockout|(inventory(r_product)==0)) {
				if (testing) System.out.println("selecting case 2");
				option=2; // If the retailer experienced a stockout, case 2 applies; raise prices.
			}
			if ((option==4)&(cashout|(inventory(r_money)==0)|(income==0))) {
				if (testing) System.out.println("selecting case 3");
				option=3; // Case 2 has precedence, but if not, a cashout or income=0 makes case 3 apply
			}
			if (option==4) {
				if (testing) System.out.println("selecting case 1");
				option=1; // Normal conditions
			}
		}
		
		// PRICE SETTING
		if (testing) System.out.println("TESTING Retailer.activate(). Line 170. Retailer ID="+id);
		if (option==1) { // IN NORMAL BUSINESS CONDITIONS
			if ((basePrice>0)==false) startingPrice(); // This shouldn't ever happen.
			ArrayList<Integer> relevantTurns=new ArrayList<Integer>(); // The number of turns to be included in the average
			int maxTurnsToInclude=Math.min(world.RETAILER_PRICE_SETTING_MAX_HINDSIGHT, incomeHistory.size()-1);
			for (int i=0; i<maxTurnsToInclude; i++) {
				if (testing) System.out.println("Checking turns to include...");
				int selector=last-i;
				Boolean stockout=stockoutsHistory.get(selector)>0;
				Boolean cashout=cashoutsHistory.get(selector)>0;
				if (testing) System.out.println("selector="+selector+". stockout="+stockout+". cashout="+cashout);
				if ((!stockout)&(!cashout)) relevantTurns.add(selector);
			}
			double tot_wIncome=0; // Total weighted income
			double tot_wSupply=0; // Total weighted supply
			double sumWeights=0;
			for (Integer i: relevantTurns) {
				int ago=last-i;
				double weight=Math.pow(world.RETAILER_PRICE_SETTING_RECENCY_WEIGHT_FACTOR,ago); // The longer ago the turn in question was, the smaller its weight
				sumWeights=sumWeights+weight;
				tot_wIncome=tot_wIncome+incomeHistory.get(i)*weight;
				tot_wSupply=tot_wSupply+supplyHistory.get(i)*weight;
			}
			double avgIncome=tot_wIncome/sumWeights;
			double avgSupply=tot_wSupply/sumWeights;
			Double basePrice_update=avgIncome/avgSupply;
			Double basePrice_minimum=(1-world.RETAILER_MAX_PRICE_DROP_BASED_ON_INCOME_SUPPLY_RATIO)*basePrice;
			Double basePrice_maximum=(1+world.RETAILER_MAX_PRICE_RISE_BASED_ON_INCOME_SUPPLY_RATIO)*basePrice;
			if (testing) System.out.println("avgIncome = "+avgIncome+", avgSupply = "+avgSupply+". included turns in average: "+relevantTurns.size());
			if (basePrice_update>basePrice) basePrice=Math.min(basePrice_update, basePrice_maximum); 
			if (basePrice_update<basePrice) basePrice=Math.max(basePrice_update, basePrice_minimum); 
			double logValueOfCash=Math.log10(inventory(r_money));
			double logValueOfInventory=Math.log10(inventory(r_product)*basePrice);
			double priceChange=world.RETAILER_PRICE_SHIFT_MULTIPLIER_BASED_ON_CASH_INVENTORY_IMBALANCE
					*(logValueOfCash-logValueOfInventory)+world.RETAILER_PRICE_SHIFT_BIAS_BASED_ON_CASH_INVENTORY_IMBALANCE;
			if (priceChange>world.MAX_PRICE_CHANGE_BASED_ON_CASH_INVENTORY_IMBALANCE) 
				priceChange=world.MAX_PRICE_CHANGE_BASED_ON_CASH_INVENTORY_IMBALANCE;
			if (priceChange<world.MIN_PRICE_CHANGE_BASED_ON_CASH_INVENTORY_IMBALANCE) 
				priceChange=world.MIN_PRICE_CHANGE_BASED_ON_CASH_INVENTORY_IMBALANCE;
			basePrice=basePrice*(1+priceChange);
			basePrice=basePrice*((1-world.NORMAL_BUS_COND_PRICE_RANDOMIZATION_FACTOR)
					+2*world.NORMAL_BUS_COND_PRICE_RANDOMIZATION_FACTOR*Math.random());
			if (Math.random()<world.ODDS_OF_MARKUP_CHANGE_UNDER_NORMAL_BUS_COND) 
				markup=(1+world.SIZE_OF_MARKUP_CHANGE_UNDER_NORMAL_BUS_COND)*markup;
		}
		if (option==2) { // DIFFICULTIES MAINTAINING INVENTORY
			basePrice=basePrice*(1+world.RETAILER_MAX_PRICE_RESPONSE_TO_STOCKOUT
					*Math.pow(Math.random(),world.RETAILER_STOCKOUT_RESPONSE_SKEW_EXP)); 
			if (Math.random()<world.ODDS_OF_MARKUP_CHANGE_UNDER_INVENTORY_PROBLEMS) {
				markup=(1+world.SIZE_OF_MARKUP_CHANGE_UNDER_INVENTORY_PROBLEMS)*markup;
			}
		}
		if (option==3) { // "CASHOUT" PROBLEMS: TOO MANY WILLING SUPPLIERS RELATIVE TO DEMAND
			basePrice=basePrice*(1-world.RETAILER_MAX_PRICE_RESPONSE_TO_CASHOUT
					*Math.pow(Math.random(),world.RETAILER_CASHOUT_RESPONSE_SKEW_EXP));
			if (Math.random()<world.ODDS_OF_MARKUP_CHANGE_UNDER_CASH_FLOW_PROBLEMS) 
				markup=(1+world.SIZE_OF_MARKUP_CHANGE_UNDER_CASH_FLOW_PROBLEMS)*markup;
		}
		if (option==4) {
			startingPrice();
		}
		markup=Math.max(world.RETAILER_MINIMUM_MARKUP, markup);
		double sales_price=basePrice*(1+markup);
		double supply_price=basePrice/(1+markup);
		purchasingManager.setPrice(1/supply_price); // The reciprocal because supply_price is a money price for the good, but the purchasing manager is offering to sell money for goods
		salesManager.setPrice(sales_price);
		modeHistory.add(option);
		lastTurnTransactions=transactions;
		transactions=new ArrayList<Transaction>();
		
		// PROFIT TAKING
		double profit=world.PROFIT_RATE*inventory(r_money);
		Transactions.dividend(this, owner, r_money, profit); // Pay 25% of the cash to the owner as a dividend (or wage for the retailer's labor).
		dividendHistory.add(profit);
		lifespan++;
		if (testing) {
			System.out.println("TESTING Retailer.activate(). Line 218. Retailer ID="+id);
			System.out.println("basePrice="+basePrice+". purchasingManager.price="+(1/purchasingManager.getPrice())+
					". salesManager.price="+salesManager.getPrice());
			System.out.println("Case option was "+option);
		}
		double salesPrice=salesManager.getPrice();
		double supplyPrice=1/purchasingManager.getPrice();
		boolean salesPriceValid=salesPrice>0;
		boolean supplyPriceValid=supplyPrice>0;
		if ((!supplyPriceValid)|(!salesPriceValid)) {
			System.out.println("ERROR: Retailer is charging invalid price. supplyPrice="+supplyPrice+". salesPrice="+salesPrice);
			for (int i=0; i<incomeHistory.size(); i++) {
				System.out.println("\nTURN "+i);
				System.out.println("Income in turn "+i+": "+incomeHistory.get(i));
				System.out.println("Supply in turn "+i+": "+supplyHistory.get(i));
				System.out.println("Inventory in turn "+i+": "+inventoryHistory.get(i));
				System.out.println("Cash in turn "+i+": "+cashHistory.get(i));
				System.out.println("Stockouts in turn "+i+": "+stockoutsHistory.get(i));
				System.out.println("Cashouts in turn "+i+": "+cashoutsHistory.get(i));
				System.out.println("Base price in turn "+i+": "+basePriceHistory.get(i));
			}
			Exception ex=new Exception();
			ex.printStackTrace();
			System.exit(1);
		}
	}
	
	public void startingPrice() {
		boolean testing=false;
		// Start by estimating what you'll need to pay your workers
		double sumLaborSupply=0;
		for (Agent a: owner.friends) {
			sumLaborSupply=sumLaborSupply+a.rawLaborSupply();
		}
		double estAvgLabor=sumLaborSupply/owner.friends.size();
		if (testing) System.out.println("estAvgLabor="+estAvgLabor);
		double wSumEffWage=0;
		double sumWeights=0;
		for (Trader t: owner.knows) {
			Retailer r=(Retailer) t;
			double buyPrice=1/r.purchasingManager.getPrice();
			double l0=owner.getEconomy().productionFunction.getL0(r.r_product);
			double effWage=(buyPrice*(estAvgLabor-l0))/estAvgLabor;
			if (testing) System.out.println("retailer "+r.id+" pays buy price "+buyPrice+
					" for a good with l0="+l0+", thus paying estimated effWage="+effWage+". weight="+r.income);
			if (effWage>0) { // Ignore goods that most agents can't afford to make
				wSumEffWage=wSumEffWage+effWage*r.income; // The longer the first has been in business, the more weight
				sumWeights=sumWeights+r.income;
			}
		}
		Double estPrevailingWage=1.0;
		if (sumWeights>0) {
			estPrevailingWage=wSumEffWage/sumWeights;
		}
		if (testing) System.out.println("estPrevailingWage="+estPrevailingWage+", i.e. wSumEffWage="+wSumEffWage+" divided by sumWeights="+sumWeights);
		double l0=owner.getEconomy().productionFunction.getL0(r_product);
		if (testing) System.out.println("l0="+l0);
		double buyPrice=1; // This "1" never sticks
		if (l0>estAvgLabor) buyPrice=10*estPrevailingWage;
		else buyPrice=estPrevailingWage*estAvgLabor/(estAvgLabor-l0);
//		if (buyPrice<0) buyPrice=1;
		basePrice=buyPrice*(1+markup);
		basePrice=basePrice*Math.pow(2,-world.RETAILER_STARTING_PRICE_BIAS+4*Math.random()); // Give it a bit of a bias towards discounting
/*		if (emulate!=null) {
			if (emulate.defunct==false) {
				basePrice=emulate.basePrice;
				markup=0.9*emulate.markup;
			}
		}*/
		if (basePrice<0) {
			System.out.println("ERROR in Retailer.startingPrices(). Retailer is setting invalid base price "+basePrice
					+". estAvgLabor="+estAvgLabor+". estPrevailingWage="+estPrevailingWage+". sumWeights="+sumWeights);
			Exception ex=new Exception();
			ex.printStackTrace();
			System.exit(1);
		}
		double sales_price=basePrice*(1+markup);
		double supply_price=basePrice/(1+markup);
		purchasingManager.setPrice(1/supply_price); // The reciprocal because supply_price is a money price for the good, but the purchasing manager is offering to sell money for goods
		salesManager.setPrice(sales_price);
		if (testing) System.out.println("markup="+markup);
		if (testing) System.out.println("Retailer.basePrices() just set prices to \nbase price: "
				+basePrice+"\nsales price: "+salesManager.getPrice()+"\nsupply price: "+purchasingManager.getPrice());
	}

	public boolean defunct() {
		return defunct;
	}
	
	public static Retailer considerEntry(Agent a, double investment) {
		boolean testing=false;
		if (a.enterprises.size()>0) return null;
		double rSumUtil=0; // the total utility of the agent's friends who are retailers
		double aSumUtil=0; // the total utility of the agent's friends who are non-retailers (a for "just agents")
		double rFriends_rawLabor=0; // the total number of the agent's friends who are retailers
		double aFriends_rawLabor=0; // the total number of the agent's friends who are non-retailers
		for (Agent friend: a.friends) {
			if (friend.enterprises.size()>0) {
				rFriends_rawLabor=rFriends_rawLabor+friend.rawLaborSupply();
				rSumUtil=rSumUtil+friend.cons.utility;
			} else {
				aFriends_rawLabor=aFriends_rawLabor+friend.rawLaborSupply();
				aSumUtil=aSumUtil+friend.cons.utility;
			}
		}
		a.oddsOfEntry_retail=0.00001;
		if (rFriends_rawLabor==0) a.oddsOfEntry_retail=0.01; // 1% chance of starting a retailer if no retailer friends 
		if (a.knows.size()==0) a.oddsOfEntry_retail=0.02; // 2% chance of starting a retailer if the agent doesn't even know of any
		else {
			Y: if (rFriends_rawLabor>0) {
				double rAvgUtilPayRate=rSumUtil/rFriends_rawLabor;
				double aAvgUtilPayRate=aSumUtil/aFriends_rawLabor;
				if (rAvgUtilPayRate<aAvgUtilPayRate) a.oddsOfEntry_retail=a.getEconomy().RETAILER_ENTRY_PROB_IF_RETAILERS_DOING_WORSE; // 0.5% chance of entry if retailers are doing worse
				else a.oddsOfEntry_retail=a.getEconomy().RETAILER_ENTRY_MULTIPLIER_FOR_LOG_UTIL_DIFFERENCE 
						*(Math.log(rAvgUtilPayRate)-Math.log(aAvgUtilPayRate))
						-a.getEconomy().RETAILER_ENTRY_HANDICAP_FOR_LOG_UTIL_DIFFERENCE; // odds of entry rise with the success of the active retailers			
				if (testing) System.out.println("TESTING Retailer.considerEntry(). Agent ID="+a.id+". rAvgUtil="+rAvgUtilPayRate+". aAvgUtil="+aAvgUtilPayRate);
			}
		}
		if (testing) System.out.println("TESTING Retailer.considerEntry(). Agent ID="+a.id+". oddsOfEntry="+a.oddsOfEntry_retail);
		if (Math.random()>a.oddsOfEntry_retail) return null; // Doesn't pass the test: no retailer is initiated.
		int product=(int) (2+Math.random()*(a.getEconomy().numGoods-2));
		double dice=Math.random();
		int chooser=0;
		if (dice>a.getEconomy().THRESHOLD_CHOOSE_EMULATE) chooser=1;
		if (dice>a.getEconomy().THRESHOLD_CHOOSE_FRIENDS_MAKE) chooser=2;
		if (dice>a.getEconomy().THRESHOLD_CHOOSE_FRIENDS_CAN_MAKE) chooser=3;
		if (dice>a.getEconomy().THRESHOLD_CHOOSE_CAN_SELFSUPPLY_WHILE_RETAILING) chooser=4;
		Retailer emulate=null;
		if (chooser==1) { // "Emulate" another retailer, with the odds of emulation weighted by success
			if (a.knows.size()<0) {
				double sumWeights=0;
				for (Trader t: a.knows) {
					sumWeights=sumWeights+((Retailer)t).owner.has(0);
				}
				double selector=Math.random()*sumWeights;
				sumWeights=0;
				Y: for (Trader t: a.knows) {
					sumWeights=sumWeights+((Retailer)t).owner.has(0);
					if (selector<sumWeights) {
						emulate=(Retailer) t;
						break Y;
					}
				}
				if (emulate!=null) {
					product=emulate.r_product;
				}				 
			}
		} 
		Q: if (chooser==2) { // Pick one of the goods one's friends were making in the last round
			ArrayList<Integer> goodsFriendsMake=new ArrayList<Integer>();
			Z: for (Agent friend: a.friends) {
				LaborReport report=friend.labor;
				if (report==null) break Z;
				for (int i=0; i<report.labor.length; i++) {
					if (report.labor[i]>0) goodsFriendsMake.add(i);
				}
			}
			if (goodsFriendsMake.size()==0) break Q;
			int selector=(int) (Math.random()*goodsFriendsMake.size());
			product=goodsFriendsMake.get(selector);
		}
		R: if (chooser==3) { // Pick from among the goods one's friends can make
			ArrayList<Integer> goodsFriendsCanMake=new ArrayList<Integer>();
			for (Agent friend: a.friends) {
				if (friend.productionUniverse!=null) {
					for (Integer i: friend.productionUniverse) {
						goodsFriendsCanMake.add(i);
					}
				}
			}
			if (goodsFriendsCanMake.size()==0) break R;
			int selector=(int) (Math.random()*goodsFriendsCanMake.size());
			product=goodsFriendsCanMake.get(selector);
		} 
		Y: if (chooser==4) { // Select a good the agent will still have time to self-supply
			ArrayList<Integer> goodsCanStillMakeWhileRetailing=new ArrayList<Integer>();
			if (a.productionUniverse==null) break Y;
			for (Integer i: a.productionUniverse) {
				if (a.getEconomy().productionFunction.getL0(i)<(1-a.getEconomy().RETAILER_TIME_COST)*a.rawLaborSupply()) {
					goodsCanStillMakeWhileRetailing.add(i);					
				}
			}
			if (goodsCanStillMakeWhileRetailing.size()==0) break Y;
			int chooser2=(int) (Math.random()*goodsCanStillMakeWhileRetailing.size());
			product=goodsCanStillMakeWhileRetailing.get(chooser2); 
		}
		int money=a.getEconomy().money;
		Retailer r=null;
		try {
			r=new Retailer(a, a.getEconomy(), money, product);			
		} catch(Exception ex) {ex.printStackTrace();System.exit(1);}
		r.emulate=emulate;
		r.startingPrice();
		Transactions.invest(a, r, money, investment);
		return r;
	}
	
	public boolean considerExit() {
//		if (lifespan<5) return false;
		boolean testing=false; 
//		testing=world.gui.retailerDetail_table2c.retailer==this;
		double aSumPayRate=0; // the sum of the earnings per effective labor unit, calculated for all the retailer's friends
		int aFriends=0; // the total number of the agent's friends who are non-retailers
		for (Agent friend: owner.friends) {
			if (friend.enterprises.size()==0) {
				aFriends++;
				Double earnings=0.0;
				Double marketLabor=0.0;
				if (friend.earned!=null) {
					for (int i=0; i<friend.earned.length; i++) {
						if (friend.earned[i]>0) {
							earnings=earnings+friend.earned[i]; // Add up everything the friend earned
							marketLabor=marketLabor+Math.max(0, friend.labor.labor[i]-friend.cons.used[i]); // Add up market labor, i.e., labor on goods he sold, minus the amounts he consumed
						}
					}
					if (marketLabor>0) aSumPayRate=aSumPayRate+earnings/marketLabor;					
				}
			}
		}
		Double aAvgPayRate=aSumPayRate/aFriends;
		if (testing==true) System.out.println("TESTING Retailer.considerExit(). Retailer id="+id+". aAvgPayRate="+aAvgPayRate);
		if (dividendHistory.size()>3) {
			double ownEarnings3Turns=0;
			for (int i=0; i<3; i++) {
				int turn=dividendHistory.size()-1-i;
				double dividend=dividendHistory.get(turn);
				double capValue=cashHistory.get(turn)+basePrice*inventoryHistory.get(turn);
				double prevCapValue=cashHistory.get(turn-1)+basePrice*inventoryHistory.get(turn-1);
				double earnings=dividend+capValue-prevCapValue;
				ownEarnings3Turns=ownEarnings3Turns+earnings;
			}
			Double ownPayRate=ownEarnings3Turns/3;
			Double logOwnPayRate=Double.NEGATIVE_INFINITY;
			if (ownPayRate>0) logOwnPayRate=Math.log(ownPayRate);
			Double logAvgPayRate=Math.log(aAvgPayRate);
			Double increment=0.5+0.5*logAvgPayRate-0.5*logOwnPayRate;
			if (increment.isNaN()) increment=0.0;
			if (increment>world.MAX_RETAILER_EXIT_PROB_INCREMENT_DUE_TO_LOW_PAY) 
				increment=world.MAX_RETAILER_EXIT_PROB_INCREMENT_DUE_TO_LOW_PAY;
			if (increment<world.MIN_RETAILER_EXIT_PROB_INCREMENT_DUE_TO_HIGH_PAY) 
				increment=world.MIN_RETAILER_EXIT_PROB_INCREMENT_DUE_TO_HIGH_PAY;
			oddsOfExit=oddsOfExit+increment;
			if (testing==true) System.out.println("TESTING Retailer.considerExit(). Retailer id="+id+". ownPayRate="+ownPayRate);
		}
		// If the retailer has been out of inventory for three turns or more, 
		// the likelihood of the retailer quitting rises by RETAILER_EXIT_PROB_INCREMENT_DUE_TO_NO_SALES
		if (incomeHistory.size()>3) {
			boolean threeTurnsNegligibleSales=true;
			for (int i=0; i<3; i++) {
				Double income=incomeHistory.get(incomeHistory.size()-1-i);
				if (income>0.0001) threeTurnsNegligibleSales=false;
			}
			if (threeTurnsNegligibleSales) oddsOfExit=oddsOfExit+world.RETAILER_EXIT_PROB_INCREMENT_DUE_TO_NO_SALES;			
		}
		if (testing) System.out.println("oddsOfExit="+oddsOfExit);
		if (oddsOfExit<world.MIN_RETAILER_EXIT_PROB) oddsOfExit=world.MIN_RETAILER_EXIT_PROB;
		if (oddsOfExit>world.MAX_RETAILER_EXIT_PROB) oddsOfExit=world.MAX_RETAILER_EXIT_PROB; // Maximum 50% odds of exit based on lower earnings
		boolean exit=Math.random()<oddsOfExit;
		if (exit) exit();
		return exit;
	}
	
	public void exit() {
		boolean testing=false;
		if (testing) System.out.println("TESTING Retailer.exit(). RETAILER "+id+" EXITING");
		owner.enterprises.remove(this);
		Transactions.dividend(this, owner, r_money, this.getInventory()[r_money]);
		Transactions.dividend(this, owner, r_product, this.getInventory()[r_product]);
		world.traderExit(this);
		defunct=true; // This, for one thing, helps the economy to cull defunct retailers from its lists.
	}
}
