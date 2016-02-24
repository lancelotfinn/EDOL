package model;
import java.io.Serializable;
import java.util.*;

import model.Agent;
import model.AvoidCostPF;
import model.Retailer;
import model.TradeOffer;
import model.Transactions;
import model.UtilityFunction;

public class TradingStep implements Serializable {
	// This object is used as part of the agent's problem in the endogenous division of labor (EDOL)
	// stockout-constrained case. Of course, when the stockout constraints are not binding, it serves to 
	// calculate a one-off optimization. The optimization will consist of a series of one or more trading
	// steps, to be executed in succession.
	public int agentID;
	public int id;
	public boolean firstStep;
	public Boolean[] margProductionSet;
	public Boolean[] productionSet;
	public Boolean[] buyableSet;
	public Boolean[] holdSet;
	public ArrayList<TradeOffer>[] sellers;
	public ArrayList<TradeOffer> buyers;
	public int margBuyer;
	public Integer[] margSellers;
	public double margWage;
	public Double[] margPrices;
	public double incLimit; // The most that the agent can earn by selling to the marginal buyer.
	public Double[] consLimits; // The most that the agent can buy from the marginal sellers.
	public double residualLabor;
	public double money;
	public double capital;
	public int moneyID;
	public int capitalID;
	public ArrayList<TradingStep> backStory;
	UtilityFunction utility;
	AvoidCostPF pFunction;
	public double[] consumedSoFar;
	// To be calculated by the TradingStep itself.
	public double lStar;
	public double deltaM; // Money to be acquired at the margin (can be negative).
	public double[] delta_b; // Goods of each type to be bought at the margin (must be >=0).
	public double[] delta_m; // Goods of each type to be self-produced ("made") at the margin (must be >=0)
	public double laborStep;
	boolean complete; // Set to true if this is a feasible optimization, false if further trading steps needed.
	public String resultDescription;
	
	public TradingStep(int aid, Boolean[] bt, Boolean[] p,ArrayList<TradeOffer> bSeries, 
			ArrayList<TradeOffer>[] sSeries, double m, double k, int mid, int kid,
			double rl, UtilityFunction u, AvoidCostPF pf) {
		boolean testing=false;
		firstStep=true;
		id=(int) (1000000*Math.random());
		agentID=aid;
		margProductionSet=p.clone();
		productionSet=p.clone();
		pFunction=pf;
		if (testing) {
			System.out.print("TradingStep constructor. Production set: ");
			for (int i=1; i<bt.length; i++) {
				if (productionSet[i]==true) System.out.print(i+", ");
			}
		}
		buyableSet=bt.clone();
		margBuyer=0;
		int n=buyableSet.length;
		holdSet=new Boolean[n];
		margSellers=new Integer[n];
		for (int i=0; i<n; i++) {
			margSellers[i]=0;
			holdSet[i]=false;
		}
		sellers=sSeries;
		buyers=bSeries;
		money=m;
		capital=k;
		residualLabor=rl;
		backStory=new ArrayList<TradingStep>();
		backStory.add(this);
		utility=u;
		pFunction=pf;
		consumedSoFar=new double[n];
		for (int i=0; i<n; i++) {
			consumedSoFar[i]=0;
		}
		consumedSoFar[capitalID]=capital;
		moneyID=mid;
		capitalID=kid;
		delta_b=new double[buyableSet.length];
		delta_m=new double[buyableSet.length];
		findMarginals();
	}
	public void findMarginals() {
		boolean testing=false;
		if (testing) System.out.println("TESTING TradingStep.findMarginals()");
		// Find out what the marginal wage and marginal prices are, based on the variables above.
		margWage=0;
		incLimit=0;
		if (margBuyer<buyers.size()) {
			TradeOffer margBuyerTO=buyers.get(margBuyer);
			margWage=1/margBuyerTO.getPrice();
			incLimit=margBuyerTO.limit();			
		}
		int n=sellers.length;
		margPrices=new Double[n];
		consLimits=new Double[n];
		for (int i=0; i<n; i++) {
			if (i!=moneyID) {
				margPrices[i]=null;
				if (testing) System.out.println("sellers["+i+"].size()="+
						sellers[i].size());
				if (margSellers[i]<sellers[i].size()) {
					TradeOffer margSellerI=sellers[i].get(margSellers[i]);
					margPrices[i]=margSellerI.getPrice();
					consLimits[i]=margSellerI.limit();								
				} 				
			}
		}
	}
	
	public TradingStep(TradingStep model) {
		boolean testing=false;
		firstStep=false;
		id=(int) (1000000*Math.random());
		agentID=model.agentID;
		if (testing==true) System.out.println("INITIATING TradingStep");
		backStory=(ArrayList<TradingStep>) model.backStory.clone();
		backStory.add(this);
		buyableSet=model.buyableSet.clone();
		buyers=(ArrayList<TradeOffer>) model.buyers.clone();
		int n=buyableSet.length;
		consumedSoFar=model.consumedSoFar.clone();
		for (int i=0; i<n; i++) {
			consumedSoFar[i]=consumedSoFar[i]+model.delta_b[i]+model.delta_m[i];
		}
		money=model.money+model.deltaM;
		capital=model.consumedSoFar[capitalID];
		moneyID=model.moneyID;
		capitalID=model.capitalID;
		residualLabor=model.residualLabor-model.laborStep;
		productionSet=model.productionSet.clone();
		margProductionSet=model.margProductionSet.clone();
		holdSet=model.holdSet.clone();
		margSellers=model.margSellers.clone();
		margBuyer=model.margBuyer;
		utility=model.utility;
		pFunction=model.pFunction;
		sellers=model.sellers.clone();
		delta_b=new double[n];
		delta_m=new double[n];
		findMarginals();
	}
	
	public ArrayList<TradingStep> optimize() {
		boolean testing=false;
//		if (Math.random()<0.0001) testing=true;
		resultDescription="";
		if (testing) {
			System.out.println("\n\n\n\n\n\n\n\n\nTESTING TradingStep.optimize(). id="+id+". agent ID="+agentID+
					". This is the first step? "+firstStep);
			System.out.print("PRODUCTION SET: ");
			for (int i=0; i<buyableSet.length; i++) {
				if (i!=moneyID) {
					if (productionSet[i]==true) System.out.print(i+", ");					
				}
			}
			System.out.println();
//			if (Math.random()<0.005) System.exit(1);
		}
		// This method calculates the unconstrained marginal consumption vector.
		// It then checks whether this unconstrained marginal consumption vector is feasible.
		// If it is, it records that this optimization iteration is "complete" and returns an
		// ArrayList of TradingSteps ending with itself.
		// If it is not, it truncates the trading plan to the limits of feasibility, records that it 
		// is incomplete, generates the next trading step in the sequence, and orders it to optimize.
		// Thus this method launches a recursive algorithm which executes an iteration ending in a complete optimization.
		// Case A: The Full Employment Case
		int n=buyableSet.length;
		boolean fullEmployment=margWage>0;
		boolean underEmployment=!fullEmployment;
		boolean jobExhausted=false;
		Integer expandHoldSet=null;
		if (testing) System.out.println("tracker... location 1");
		lStar=0;
		deltaM=0;
		for (int i=0; i<n; i++) {
			delta_m[i]=0;
			delta_b[i]=0;
		}
		if (fullEmployment) {
			resultDescription="Full employment, ";
			// First assign the buyable and marginal production sets.
			if (testing) System.out.println("***FULL EMPLOYMENT***");
			assignBandP(margWage);
			// Then calculate yStar
			lStar=yStar();
			if (testing) System.out.println("tracker... location 2");
			if ((lStar>=0)&(lStar*margWage<=incLimit)) { // Only if the income plan is feasible are the other values calculated.
				double k=k(lStar);
				deltaM=k-money;
				delta_b=delta_b(k);
				delta_m=delta_m(k,margWage);
				if (testing) System.out.println("tracker... location 3");
			} 
			if (lStar<0) {
				lStar=0;
				underEmployment=true;
			}
			if (lStar*margWage>=incLimit) {
				lStar=incLimit/margWage;
				jobExhausted=true;
			}
		}
		// Now for the underemployed case.
		// This is where we use the "piano keys" algorithm
		X: if (underEmployment==true) {
			resultDescription=resultDescription+"Underemployment, ";
			if (testing) System.out.println("***UNDEREMPLOYMENT***");
			ArrayList<Integer> blackKeys=new ArrayList<Integer>();
			for (int i=0; i<n; i++) {
				if (i!=moneyID) {
					// Add shadow wages to the series only if a good is in the production set 
					// and can also be bought at the margin; only these are candidates for the 
					// dual source good.
					if ((productionSet[i]==true)&(margSellers[i]<sellers[i].size())&(holdSet[i]==false)) {
						blackKeys.add(i);
					}					
				}
			}
			if (testing) System.out.println("tracker... location 4");
			Collections.sort(blackKeys);
			Integer lowerBound=-1;
			Integer upperBound=blackKeys.size();
			Integer d=null;
			Double shadowWage=1.0; // This 1.0 is a meaningless, arbitrary shadow wage for when there is no overlap between candidates for B and P.
			if (residualLabor==0.0) shadowWage=Double.POSITIVE_INFINITY;
			double delta_bdt=0;
			double delta_mdt=0;
			boolean blackKey=false;
			boolean whiteKey=false;
			if ((blackKeys.size()==0)|(residualLabor==0)) whiteKey=true; // If there are no "black keys," there is no problem to solve here.
			Y: while ((blackKey==false)&(whiteKey==false)) {
				if (testing) System.out.println("Iteration within underemployment algorithm. blackKeys.size()="
						+blackKeys.size()+". upperBound="+upperBound+". lowerBound="+lowerBound);
				if (lowerBound==upperBound-1) { // If the two bounds are adjacent, choose the "white key" between them.
					if (testing) System.out.println("Upper and lower bounds are adjacent; time to resort to the white key in between");
					if (upperBound==blackKeys.size()) {
						Integer lowerBoundIndex=blackKeys.get(blackKeys.size()-1);
						shadowWage=sellers[lowerBoundIndex].get(margSellers[lowerBoundIndex]).getPrice()+1;
						// Add one so that *ALL* the potential dual-source goods are bought.
					}
					if (lowerBound==-1) {
						Integer upperBoundIndex=blackKeys.get(0);
						shadowWage=sellers[upperBoundIndex].get(margSellers[upperBoundIndex]).getPrice()-1;
						// Subtract one so that *ALL* the potential dual-source goods are self-produced.
					}
					if ((upperBound<blackKeys.size())&(lowerBound>-1)) {
						Integer upperBoundIndex=blackKeys.get(upperBound);
						Integer lowerBoundIndex=blackKeys.get(lowerBound);
						Double aboveBlackKeyPrice=sellers[upperBoundIndex].get(margSellers[upperBoundIndex]).getPrice();
						Double belowBlackKeyPrice=sellers[lowerBoundIndex].get(margSellers[lowerBoundIndex]).getPrice();
						shadowWage=(aboveBlackKeyPrice+belowBlackKeyPrice)/2;
						if (testing) System.out.println("tracker... location 6");
					}
					whiteKey=true;
					break Y;
				}
				int dIndex=(int) ((lowerBound+upperBound)/2);
				d=blackKeys.get(dIndex);
				if (testing) System.out.println("blackKeys.size()="+blackKeys.size()+
						". dIndex="+dIndex+". d="+d);
				shadowWage=sellers[d].get(margSellers[d]).getPrice();
				// For this shadow wage, assign goods to the buyable and marginal production sets.
				assignBandP(shadowWage);
				buyableSet[d]=false;
				// Now that the buyable and marginal production sets have been assigned (and d), it's time to
				// calculate the optimal values delta mdt and delta bdt.
				delta_bdt=underemployed_bdt(d);
				double k=k_underemployment(shadowWage*delta_bdt);
				if (testing) System.out.println("tracker... location 7");
				delta_mdt=k*Math.pow(margPrices[d]/(utility.coeffs[d]/utility.coeffs[moneyID]),1/(utility.sigma-1))-consumedSoFar[d]-delta_bdt;
				// Now find out whether the shadow price is too high or too low.
				if (testing) System.out.println("delta_bdt="+delta_bdt+". delta_mdt="+delta_mdt);
				if ((delta_bdt<0)&(delta_mdt<0)) {
					expandHoldSet=d;
					break X;
				}
				if ((delta_bdt<0)&(delta_mdt>=0)) {
					// The agent wants to buy a negative quantity in order to save labor, indicating 
					// that the shadow wage is too high. Therefore, make this the new upper bound.
					upperBound=dIndex;
				}
				if ((delta_bdt>=0)&(delta_mdt<0)) lowerBound=dIndex;
				if ((delta_bdt>=0)&(delta_mdt>=0)) blackKey=true;
			}
			// Now we know the shadow wage, we can assign goods to B and P and calculate the choice variables.
			if (testing) System.out.println("tracker... location 8");
			assignBandP(shadowWage);
			if (blackKey) {
				buyableSet[d]=false;
				margProductionSet[d]=false;
				double k=k_underemployment(shadowWage*delta_bdt);
				delta_b=delta_b(k);
				delta_m=delta_m(k,shadowWage);
				delta_b[d]=delta_bdt;
				delta_m[d]=delta_mdt;				
				buyableSet[d]=true;
				margProductionSet[d]=true;
				resultDescription=resultDescription+"Black key d="+d+", ";
			}
			if (whiteKey) {
				double k=k_underemployment(0);
				delta_b=delta_b(k);
				deltaM=k-money;
				double[] sums=standardSummations();
				double pSum_x=sums[2];
				double pSum_a=sums[3];
				double mMult=(residualLabor+pSum_x)/pSum_a;
				for (int i=0; i<margProductionSet.length; i++) {
					if (i!=moneyID) {
						delta_m[i]=0;
						if (margProductionSet[i]==true) {
							delta_m[i]=mMult*Math.pow(utility.coeffs[i]/utility.coeffs[moneyID],1/(1-utility.sigma))-consumedSoFar[i];
						}						
					}
				}
				resultDescription=resultDescription+"White key, ";
			}
			if (testing) {
				System.out.print("\nPIANO KEYS ALGORITHM OUTCOMES: shadowWage="+shadowWage+" B: ");
				for (int i=0; i<n; i++) {
					if (i!=moneyID) {
						if (buyableSet[i]==true) System.out.print(i+", ");						
					}
				}
				System.out.print(" P: ");
				for (int i=0; i<n; i++) {
					if (i!=moneyID) {
						if (margProductionSet[i]==true) System.out.print(i+",");						
					}
				}
				System.out.println("\n");
			}
		}
		// CHECK 1. IF IT IS NECESSARY TO EXPAND THE HOLD SET, THE AGENT DOES THAT FIRST
		if (testing) System.out.println("tracker... location 9");
		if (expandHoldSet!=null) {
			if (testing) System.out.println("STOPPED ON CHECK 1. EXPAND THE HOLD SET WITH GOOD "+expandHoldSet);
			TradingStep next=new TradingStep(this);
			next.margProductionSet[expandHoldSet]=false;
			next.buyableSet[expandHoldSet]=false;
			next.holdSet[expandHoldSet]=true;
			ArrayList<TradingStep> ret=next.optimize();
			resultDescription=resultDescription+"Expand hold set with good "+expandHoldSet+".";
			lStar=0;
			return ret;
		}
		// CHECK 2. IF THE MARGINAL JOB IS EXHAUSTED, THE AGENT WORKS THAT JOB UNTIL THE WORK IS GONE, 
		//   THEN RESTARTS THE OPTIMIZATION
		if (jobExhausted) {
			if (testing) System.out.println("STOPPED ON CHECK 2. MARGINAL JOB EXHAUSTED");
			// The iteration cannot terminate, because the income plan is not feasible.
			// Knowing she will want more income than the current planned marginal seller allows,
			// the agent will plan, at this trading step, to do no marginal consumption but only 
			// to exhaust the capacity of the marginal buyer.
			deltaM=Math.min(incLimit,residualLabor*margWage);
			for (int i=0; i<n; i++) {
				delta_b[i]=0;
				delta_m[i]=0;
			}
			if (testing) System.out.println("tracker... location 10");
			laborStep=deltaM/margWage;
			if (testing) System.out.println(toString());
			TradingStep next=new TradingStep(this);
			next.margBuyer++;
			next.findMarginals();
			ArrayList<TradingStep> ret=next.optimize();
			resultDescription=resultDescription+"Job exhausted.";
			return ret;
		}
		// CHECK 3. IF ANY OF THE delta_bi OR delta_mi ARE NEGATIVE, ASSIGN THEM TO HOLD.
		boolean negativeDelta=false;
		Y: for (int i=0; i<n; i++) {
			if (((delta_m[i]<0)|(delta_b[i]<0))&(i!=moneyID)) {
				if (testing) System.out.println("negative delta found: delta_m["+i+"]="
						+delta_m[i]+". delta_b["+i+"]="+delta_b[i]);
				negativeDelta=true;
				break Y;
			}
		}
		if (testing) System.out.println("tracker... location 11");
		if (negativeDelta) {
			if (testing) System.out.println("STOPPED ON CHECK 3. NEGATIVE VALUES OF CHOICE VARIABLES");
			boolean[] deltaWasNegative=new boolean[n];
			for (int i=0; i<n; i++) {
				deltaWasNegative[i]=(delta_m[i]<0)|(delta_b[i]<0);
				delta_m[i]=0;
				delta_b[i]=0;
			}
			lStar=0;
			deltaM=0;
			if (testing) System.out.println(toString());
			TradingStep next=new TradingStep(this);
			for (int i=0; i<n; i++) {
				if (i!=moneyID) {
					if (buyableSet[i]==true) {
						if (testing) System.out.println("delta_b["+i+"]="+delta_b[i]);
						if (deltaWasNegative[i]==true) {
							next.buyableSet[i]=false;
							next.holdSet[i]=true;
						}
					}
					if (margProductionSet[i]==true) {
						if (testing) System.out.println("delta_m["+i+"]="+delta_m[i]);
						if (deltaWasNegative[i]==true) {
							next.margProductionSet[i]=false;
							next.holdSet[i]=true;
						}
					}
					if (testing) System.out.println("tracker... location 12");					
				}
			}
			next.findMarginals();
			if (testing) System.out.println("ABOUT TO LAUNCH OPTIMIZATION OF NEXT TRADING STEP FROM CHECK 3");
			ArrayList<TradingStep> ret=next.optimize();
			resultDescription=resultDescription+"Negative buy or make.";
			return ret;
		}
		// CHECK 4. If any seller stockouts will occur under the plan, first stock out the sellers in question,
		// using cash.
		boolean sellerStockout=false;
		Y: for (int i=0; i<n; i++) {
			if (i!=moneyID) {
				if (buyableSet[i]==true) {
					if (delta_b[i]>consLimits[i]) {
						sellerStockout=true;
						break Y;
					}
				}				
			}
		}
		if (sellerStockout) {
			if (testing) System.out.println("STOPPED ON CHECK 4. SELLER STOCKOUTS ENCOUNTERED.");
			boolean[] stockouts=new boolean[n];
			double costToStockouts=0;
			if (testing) System.out.println("tracker... location 13");
			for (int i=0; i<n; i++) {
				if (i!=moneyID) {
					stockouts[i]=false;
					if (buyableSet[i]==true) {
						if (delta_b[i]>consLimits[i]) {
							double exp=consLimits[i]*margPrices[i];
							costToStockouts=costToStockouts+exp;
							stockouts[i]=true;
							if (testing) System.out.println("tracker... location 14");
							delta_b[i]=consLimits[i];
						} else {
							delta_b[i]=0;
						}
					} else {
						delta_b[i]=0;
					}					
					delta_m[i]=0;
				}
			}
			if (testing) System.out.println("tracker... location 15");
			deltaM=-costToStockouts;
			lStar=0;
			laborStep=0;
			if (testing) {
				System.out.println("Trading step id="+id+"\n"+toString());
				System.out.println("ABOUT TO LAUNCH A NEW TRADING STEP WITHIN TRADING STEP id="+id);
			}
			TradingStep next=new TradingStep(this);
			if (testing) System.out.println("\n\nJUST COMPLETED TRADING STEP id="+next.id+" WITHING TRADING STEP id="+id);
			for (int i=0; i<n; i++) {
				if (stockouts[i]==true) next.margSellers[i]++;
			}
			next.findMarginals();
			ArrayList<TradingStep> ret=next.optimize();
			if (testing) System.out.println("tracker... location 16");
			resultDescription=resultDescription+"Seller stockout in goods ";
			for (int i=0; i<stockouts.length; i++) {
				boolean b=stockouts[i];
				if (b) resultDescription=resultDescription+i+", ";
			}
			return ret;
		}
		// IF THIS STEP IS REACHED, END THE ITERATION.
		complete=true;
		resultDescription=resultDescription+"Complete.";
		laborStep=lStar;
		for (int i=0; i<n; i++) {
			if (i!=moneyID) {
				if (margProductionSet[i]==true) {
					laborStep=laborStep+delta_m[i];
				}				
			}
		}
		if (testing) {
			if (firstStep) {
				System.out.println("Iteration is complete after "+backStory.size()+" steps.");
				double[] qtys=new double[delta_m.length];
				qtys[moneyID]=money+deltaM;
				for (int i=0; i<delta_m.length; i++) {
					qtys[i]=delta_m[i]+delta_b[i]+consumedSoFar[i];
				}
				double u=utility.utility(qtys);
				System.out.println("Agent "+agentID+". Utility="+u);
			}
			System.out.println("   y star: "+lStar);
			System.out.println(toString());
			System.out.println("RESULT WAS: "+resultDescription);
		}
		if (laborStep>residualLabor+0.000001) {
			System.out.println("ERROR: TradingStep tries to schedule "+laborStep+" units of labor when there are only "+residualLabor+" left.");
			Exception ex=new Exception();
			ex.printStackTrace();
			System.exit(1);
		}
		if (testing) System.out.println("tracker... location 17");
		return backStory;
	}
	
	public String toString() {
		int n=buyableSet.length;
		String ret="TRADING STEP "+backStory.size()+" IN SEQUENCE";
		ret=ret+"\n  INVENTORY";
		ret=ret+"\n    Money (Good "+moneyID+"): "+money;
		for (int i=0; i<n; i++) {
			if (i!=moneyID) {
				ret=ret+"\n    Good "+i+": "+consumedSoFar[i]+
					"     (p="+margPrices[i]+",a="+utility.coeffs[i]+",l0="+pFunction.getL0(i)+")";
			}
		}
		ret=ret+"\n    Residual labor: "+residualLabor;
		ret=ret+"\n  PARAMETERS:";
		ret=ret+"\n    Production set: ";
		for (int i=0; i<n; i++) {
			if (i!=moneyID) {
				if (productionSet[i]==true) ret=ret+i+", ";				
			}
		}
		if (margBuyer<buyers.size()) {
			TradeOffer mBuyer=buyers.get(margBuyer);
			ret=ret+"\n    Marginal buyer. Good: "+mBuyer.pay+". Wage: $"+(1/mBuyer.getPrice())+
				". Limit: $"+mBuyer.limit()+". Steps: "+margBuyer+".";			
		} else {
			ret=ret+"\n     NO MARGINAL BUYERS";
		}
		ret=ret+"\nMARGINAL BUY/MAKE PLAN";
		ret=ret+"\n    Marginal production set: ";
		for (int i=0; i<n; i++) {
			if (i!=moneyID) {
				if (margProductionSet[i]==true) ret=ret+i+", ";				
			}
		}
		ret=ret+"\n    Buyable set: ";
		for (int i=0; i<n; i++) {
			if (i!=moneyID) {
				if (buyableSet[i]==true) ret=ret+i+", ";		
			}
		}
		ret=ret+"\n  Marginal sellers:";
		for (int i=0; i<n; i++) {
			if (i!=moneyID) {
				if ((buyableSet[i]==true)&(margSellers[i]>sellers[i].size())) {
					TradeOffer mSeller=sellers[i].get(margSellers[i]);
					ret=ret+"\n    Good: "+i+". Price: $"+mSeller.getPrice()+". Limit: "+mSeller.limit()+
						". Steps: "+margSellers[i];
				}				
			}
		}
		ret=ret+"\n  OUTCOMES:";
		ret=ret+"\n"+resultDescription;
		ret=ret+"\n    Delta M: "+deltaM;
		for (int i=0; i<n; i++) {
			if (i!=moneyID) {
				if (buyableSet[i]==true) {
					ret=ret+"\n    Delta b"+i+": "+delta_b[i];				
				}				
				if (margProductionSet[i]==true) {
					ret=ret+"\n    Delta m"+i+": "+delta_m[i];								
				}				
			}
		}
		ret=ret+"\n    Labor step: "+laborStep;
		ret=ret+"\n    Complete: "+complete;
		return ret;
	}
	
	public double yStar() {
		// First prepare the summations.
		double[] sums=standardSummations();
		double bSum_px=sums[0];
		double bSum_pa=sums[1];
		double pSum_x=sums[2];
		double pSum_a=sums[3];
		// Next calculate the first term/numerator.
		double numer=residualLabor+pSum_x-Math.pow(margWage,1/(utility.sigma-1))*(money+bSum_px)*pSum_a/(1+bSum_pa);
		double denom=1+Math.pow(margWage,utility.sigma/(utility.sigma-1))*pSum_a/(1+bSum_pa);
		double yStar=numer/denom;
		return yStar;
	}
	
	public double k(double yStar) {
		boolean testing=false;
		double[] sums=standardSummations();
		double bSum_px=sums[0];
		double bSum_pa=sums[1];
		double numer=money+margWage*yStar+bSum_px;
		double denom=1+bSum_pa;
		double k=numer/denom;
		if (testing) System.out.println("TESTING TradingStep.k(). RUN id="+id+". bSum_px="+
				bSum_px+". bSum_pa="+bSum_pa+". numer="+numer+". denom="+denom+". lStar="+yStar+". k="+k);
		return k;
	}
	
	public double k_underemployment(double exp_on_d) {
		boolean testing=false;
		double[] sums=standardSummations();
		double bSum_px=sums[0];
		double bSum_pa=sums[1];
		double numer=money+bSum_px-exp_on_d;
		double denom=1+bSum_pa;
		double k2=numer/denom;
		if (testing) System.out.println("TESTING TradingStep.k_underemployment().  RUN id="+id+".bSum_px="+bSum_px+
				". bSum_pa="+bSum_pa+". money="+money+". numer="+numer+". denom="+denom+". exp_on_d="+exp_on_d+". k2="+k2);
		return k2;
	}
	
	public double[] delta_b(double k) {
		boolean testing=false;
		double[] ret=new double[buyableSet.length];
		for (int i=0; i<buyableSet.length; i++) {
			if (i!=moneyID) {
				ret[i]=0;
				Boolean b=buyableSet[i];
				if (b==true) {
					double relative_alpha_i=utility.coeffs[i]/utility.coeffs[moneyID];
					if (testing) System.out.println("TESTING TradingStep.delta_b().  RUN id="+id+".alpha_i="+utility.coeffs[i]+
							", alpha_money="+utility.coeffs[moneyID]+", relative_alpha_i="+relative_alpha_i);
					double multiplier=Math.pow(margPrices[i]/relative_alpha_i,1/(utility.sigma-1));
					ret[i]=k*multiplier-consumedSoFar[i];
				}				
			}
		}
		return ret;
	}
	
	public double[] delta_m(double k,double wage) {
		boolean testing=false;
		double[] ret=new double[margProductionSet.length];
		for (int i=0; i<margProductionSet.length; i++) {
			if (i!=moneyID) {
				ret[i]=0;
				Boolean b=margProductionSet[i];
				if (b==true) {
					double relative_alpha_i=utility.coeffs[i]/utility.coeffs[moneyID];
					if (testing) System.out.println("TESTING TradingStep.delta_m().  RUN id="+id+".alpha_i="+utility.coeffs[i]+
							", alpha_money="+utility.coeffs[moneyID]+", relative_alpha_i="+relative_alpha_i);
					double multiplier=Math.pow(wage/relative_alpha_i,1/(utility.sigma-1));
					ret[i]=k*multiplier-consumedSoFar[i];
				}				
			}
		}
		return ret;
	}
	
	public double underemployed_bdt(int d) {
		boolean testing=false;
		if (testing) System.out.println("TESTING TradingStep.underemployed_bdt()");
		double[] sums=standardSummations();
		double bSum_px=sums[0];
		double bSum_pa=sums[1];
		double pSum_x=sums[2];
		double pSum_a=sums[3];
		if (testing) {
			System.out.print("Production set: ");
			for (int i=0; i<productionSet.length; i++) {
				if (i!=moneyID) {
					if (productionSet[i]) System.out.print(i+", p="+margPrices[i]+"; ");
				}
			}
			System.out.print("\nMarginal production set: ");
			for (int i=0; i<margProductionSet.length; i++) {
				if (i!=moneyID) {
					if (margProductionSet[i]) System.out.print(i+", p="+margPrices[i]+"; ");
				}
			}
			System.out.print("\nBuyable set: ");
			for (int i=0; i<buyableSet.length; i++) {
				if (i!=moneyID) {
					if (buyableSet[i]) System.out.print(i+", p="+margPrices[i]+"; ");
				}
			}
			System.out.println("\nDual-source good: "+d+", p="+margPrices[d]);
			System.out.println("bSum_px="+bSum_px+". bSum_pa="+bSum_pa+". pSum_x="+pSum_x+". pSum_a="+pSum_a);
		}
		double a_peren=Math.pow(utility.coeffs[d]/utility.coeffs[moneyID], 1/(1-utility.sigma))+pSum_a;
		double denom=1+Math.pow(margPrices[d],utility.sigma/(utility.sigma-1))*a_peren/(1+bSum_pa);
		double numer=((money+bSum_px)/(1+bSum_pa))*Math.pow(margPrices[d],1/(utility.sigma-1))
			*a_peren-residualLabor-consumedSoFar[d]-pSum_x;
		double ret=numer/denom;
		if (testing) {
			System.out.println("a_peren="+a_peren+". numer="+numer+". denom="+denom);
			System.out.println("residual labor="+residualLabor+". xdt="+consumedSoFar[d]+". bdt="+ret);
		}
		return ret;
	}
	
	public void assignBandP(double shadowWage) {
		int n=buyableSet.length;
		Z: for (int i=0; i<n; i++) {
			if (i!=moneyID) {
				boolean thereIsASeller=margSellers[i]<sellers[i].size();
				if ((productionSet[i]==false)&(thereIsASeller==false)) holdSet[i]=true;
				if (holdSet[i]==true) {
					margProductionSet[i]=false;
					buyableSet[i]=false;
					continue Z;
				}
				if ((productionSet[i]==true)&(thereIsASeller==false)) {
					margProductionSet[i]=true;
					buyableSet[i]=false;
					continue Z;
				}
				if ((productionSet[i]==false)&thereIsASeller) {
					buyableSet[i]=true;
					margProductionSet[i]=false;
					continue Z;
				}
				// At this point, only those goods which the agent can either buy or produce should be left.
				double price=sellers[i].get(margSellers[i]).getPrice();
				if (price>shadowWage) {
					margProductionSet[i]=true;
					buyableSet[i]=false;
				} 
				if (price<=shadowWage) {
					margProductionSet[i]=false;
					buyableSet[i]=true;
				}				
			}
		}
	}
	
	public double[] standardSummations() {
		// 1. SUM OVER B(pi*xi). 2. SUM OVER B(p^(sigma/(sigma-1))*ai^(1/(1-sigma))). 
		// 3. SUM OVER P(xi). 4. SUM OVER P(ai^(1/(1-sigma))).
		boolean testing=false;
		if (testing) System.out.println("TESTING TradingStep.standardSummations()");
		double bSum_px=0;
		double bSum_pa=0;
		double pSum_x=0;
		double pSum_a=0;
		for (int i=0; i<buyableSet.length; i++) {
			if (i!=moneyID) {
				Boolean b1=buyableSet[i];
				if (b1==true) {
					if (testing) {
						System.out.println("buyable set good "+i+". margPrices[i]=null: "
								+(margPrices[i]==null)+". ");
						System.out.println("consumedSoFar[i]="+consumedSoFar[i]);
					}
					if (margPrices==null) {
						System.out.println("ERROR in TradingStep.standardSummations(): margPrices["+i+"]="+margPrices[i]);
						Exception ex=new Exception();
						ex.printStackTrace();
						System.exit(1);
					}
					bSum_px=bSum_px+margPrices[i]*consumedSoFar[i];
					bSum_pa=bSum_pa+Math.pow(margPrices[i],utility.sigma/(utility.sigma-1))
							*Math.pow(utility.coeffs[i]/utility.coeffs[moneyID],1/(1-utility.sigma));
				}
				Boolean b2=margProductionSet[i];
				if (b2==true) {
					pSum_x=pSum_x+consumedSoFar[i];
					pSum_a=pSum_a+Math.pow(utility.coeffs[i]/utility.coeffs[moneyID],1/(1-utility.sigma));
				}				
			}
		}
		double[] ret={bSum_px, bSum_pa, pSum_x, pSum_a};
		return ret;
	}
	
	public void executePurchases(Agent a) {
		int n=buyableSet.length;
		for (int i=0; i<n; i++) {
			if (i!=moneyID) {
				if (buyableSet[i]==true) {
					TradeOffer margSeller=sellers[i].get(margSellers[i]);
					double payQty=Math.min(margSeller.limit(),delta_b[i])*margSeller.getPrice();
					try {
						Transactions.buy(a, margSeller, payQty);
					} catch(Exception ex) {
						System.out.println("Exception thrown by Transactions. Here's what I (TradingStep.executePurchases()) know");
						System.out.println("buyableSet.length="+n+". i="+i+". margSeller="+((Retailer)margSeller.trader).id);
						System.out.println("AND THIS IS ME: \n"+toString());
						ex.printStackTrace();
						System.exit(1);
					}
				}				
			}
		}
	}
	
	public void executeSales(Agent a) {
		TradeOffer buyer=buyers.get(margBuyer);
		try {
			Transactions.buy(a, buyer, lStar);			
		} catch(Exception ex) {ex.printStackTrace();System.exit(1);}
	}
}
