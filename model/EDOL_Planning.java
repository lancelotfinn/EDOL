package model;
import java.util.ArrayList;
import java.util.Collections;

import java.text.*;


public class EDOL_Planning {
	public static void manage(Agent a) {
		boolean testing=false;
		boolean capitalTesting=false;
		if (capitalTesting) System.out.println("TESTING EDOL_Planning.manage() for what's happening to capital");
		if (testing) System.out.println("TESTING EDOL_Planning.manage()");
		MarketParameters m=new MarketParameters();
		m.populateData(a);
		if (capitalTesting) {
			System.out.println("EDOL_Planning.manage(), line 18, agent has capital="+a.has(m.capitalID));
			System.out.println("EDOL_Planning.manage(), line 19, MarketParameters records capital="+m.capital);
		}
		Economy e=a.getEconomy();
		m.capital=a.has(m.capitalID); // This is confusing: the data initially loaded into MarketParameters is now obsolete and has to be replaced
		m.labor=a.laborSupply();
		if (capitalTesting) {
			System.out.println("EDOL_Planning.manage(), line 36, agent has capital="+a.has(m.capitalID));
			System.out.println("EDOL_Planning.manage(), line 37, MarketParameters records capital="+m.capital);
		}
		if (e.LEARNING_BY_DOING_FACTOR!=null) {
			int univMax=a.getEconomy().LEARNING_BY_DOING_FACTOR;
			// Implement the "learning by doing" algorithm, which allows agents only to make goods 
			// which they have "learned by doing" in recent rounds, plus a few goods, randomly
			// chosen, in which they have the opportunity to acquire a skill, up to a maximum set
			// by this factor.
			m.productionUniverse=new ArrayList<Integer>();
			for (int i=0; i<5; i++) {
				if (a.productionSets[i]!=null) {
					for (Integer j: a.productionSets[i].set) { // Skills are forgotten with 10% probability.
						if ((j!=e.money)&(j!=e.capital)&(Math.random()<
								a.getEconomy().SKILL_RETENTION_PROB_IN_USE
								-a.getEconomy().SKILL_RETENTION_PROB_DECAY_RATE
								*(double)i)) // money and capital are never in the production universe
						{ // 100% chance of remembering skills just used, 90% those used two turns ago, 80% the turn before that, etc.
							if (m.productionUniverse.contains(j)==false) m.productionUniverse.add(j);
						}
					}
				}				
			}
			int skills=m.productionUniverse.size();
			if (skills>univMax) { // If the agent is trying to hold too many skills in memory, he forgets one.
				int selector=(int) (Math.random()*m.productionUniverse.size());
				m.productionUniverse.remove(selector);
			}
			while (skills<univMax) { // If the agent has less than the maximum number of skills he can hold in memory, he acquires new ones at random
				Integer extra=(int) (2+Math.random()*(e.numGoods-2));
				if ((m.productionUniverse.contains(extra)==false)&(extra!=e.capital)&(extra!=m.moneyID)) {
					m.productionUniverse.add(extra);
					skills=m.productionUniverse.size();
				}
			}
			if (testing) {
				System.out.print("PRODUCTION UNIVERSE: ");
				for (Integer i: m.productionUniverse) {
					System.out.print(i+",");
				}
				System.out.println();
			}
		}
		a.productionUniverse=m.productionUniverse;
		if (testing) System.out.println(m.toString());
		int n=m.uFunction.coeffs.length;
		Specialization v=new Specialization();
		PreferredToSell p=new PreferredToSell();
		ArrayList<Integer> sellUniverse=(ArrayList<Integer>) m.productionUniverse.clone();
		NumberFormat format=new DecimalFormat("0.000");
		if (testing) System.out.println("ABOUT TO RUN THE INFRAMARGINAL OPTIMIZER (FIRST TIME)...");
		if (capitalTesting) System.out.println("EDOL_Planning.manage(), line 75, agent has capital="+a.has(m.capitalID));
		ScoredSet sellSet=InframarginalOptimizer.optimalSet(sellUniverse, m, new ArrayList<Integer>(), v, p);
		if (capitalTesting) System.out.println("EDOL_Planning.manage(), line 77, agent has capital="+a.has(m.capitalID));
		if (testing) System.out.println("JUST FINISHED THE INFRAMARGINAL OPTIMIZER (FIRST TIME)...");
		Shopping v2=new Shopping();
		PreferredToProduce p2=new PreferredToProduce();
		ArrayList<Integer> universe2=new ArrayList<Integer>();
		for (Integer i: m.productionUniverse) {
			if (sellSet.set.contains(i)==false) universe2.add(i);
		}
		if (testing) System.out.println("ABOUT TO RUN THE INFRAMARGINAL OPTIMIZER (SECOND TIME)...");
		if (capitalTesting) System.out.println("EDOL_Planning.manage(), line 86, agent has capital="+a.has(m.capitalID));
		ScoredSet pSet=InframarginalOptimizer.optimalSet(universe2, m, sellSet.set, v2, p2);
		if (capitalTesting) System.out.println("EDOL_Planning.manage(), line 88, agent has capital="+a.has(m.capitalID));
		if (testing) System.out.println("JUST FINISHED THE INFRAMARGINAL OPTIMIZER (SECOND TIME)...");
		if (testing) System.out.println("Optimal set: \n"+pSet.toString());
		Boolean[] buyableSet=new Boolean[n];
		Boolean[] productionSet=new Boolean[n];
		ArrayList<TradeOffer> buyers=new ArrayList<TradeOffer>();
		for (Integer i: pSet.set) {
			ArrayList<TradeOffer> applicableSeries=m.buyers[i];
			for (TradeOffer offer: applicableSeries) {
				buyers.add(offer);
			}
		}
		Collections.sort(buyers);
		for (int i=0; i<n; i++) {
			if (i!=m.moneyID) {
				buyableSet[i]=false;
				productionSet[i]=false;
				if (pSet.set.contains(i)==true) {
					productionSet[i]=true;
				} else {
					if (m.sellers[i].size()>0) buyableSet[i]=true;				
				}				
			}
		}
		double labor=m.labor;
		AvoidCostPF pFunction=(AvoidCostPF) m.pFunction;
		for (int i=0; i<n; i++) {
			if (i!=m.moneyID) {
				if (productionSet[i]==true) labor=labor-pFunction.getL0(i);				
			}
		}
		if (testing) {
			System.out.print("Buyable set: ");
			for (int i=0; i<n; i++) {
				if (i!=m.moneyID) {
					if (buyableSet[i]) System.out.print(i+", ");
				}
			}
			System.out.println();
			System.out.print("Production set: ");
			for (int i=0; i<n; i++) {
				if (i!=m.moneyID) {
					if (productionSet[i]) System.out.print(i+", ");
				}
			}
			System.out.println();
		}
		if (capitalTesting) System.out.println("EDOL_Planning.manage(), line 135, agent has capital="+a.has(m.capitalID));
		TradingStep firstStep=new TradingStep(m.agentID,buyableSet,productionSet,buyers,m.sellers,
				m.money,m.capital,m.moneyID,m.capitalID,labor,m.uFunction,pFunction);
		if (capitalTesting) System.out.println("EDOL_Planning.manage(), line 138, agent has capital="+a.has(m.capitalID));
		ArrayList<TradingStep> shoppingPlan=firstStep.optimize();
		// NEXT: Carry out the activities in the following order: (a) production, (b) sales, (c) consumption.
		Double[] production=new Double[n];
		for (int i=0; i<n; i++) {
			production[i]=0.0;
			if (i!=m.moneyID) {
				if (firstStep.productionSet[i]==true) production[i]=production[i]+pFunction.getL0(i);				
			}
		}
		if (testing) System.out.println("SHOPPING PLAN");
		for (TradingStep step: shoppingPlan) {
			if (testing) System.out.print("Production plan... ");
			// Home production of consumables.
			for (int i=0; i<n; i++) {
				if (i!=m.moneyID) {
					production[i]=production[i]+step.delta_m[i];					
				}
				if (testing) System.out.print(production[i]+" of "+i+",");
			}
			// Production of goods for sale in the market.
			if (step.margBuyer<step.buyers.size()) {
				int makeGood=step.buyers.get(step.margBuyer).pay;
				production[makeGood]=production[makeGood]+step.lStar;				
				if (testing) System.out.print("makes "+makeGood+", lStar="+step.lStar);
			}
			if (testing) System.out.println();
//			if (testing) System.out.println(step.toString());
		}
		try {
			if (capitalTesting) System.out.println("EDOL_Planning.manage(), line 165, agent has capital="+a.has(m.capitalID));
			a.labor=pFunction.produce(a, production);
		} catch(Exception ex) {
			ex.printStackTrace();
			System.exit(1);
		}
		if (testing) System.out.println("PRODUCTION COMPLETE");
		int lastStepIndex=shoppingPlan.size()-1;
		int numJobs=Math.min(shoppingPlan.get(lastStepIndex).margBuyer+1,firstStep.buyers.size());
		TradeOffer[] jobs=new TradeOffer[numJobs];
		double[] workPerJob=new double[numJobs];
		for (int i=0; i<numJobs; i++) {
			workPerJob[i]=0;
		}
		int jobID=-1;
		if (testing) System.out.println("Starting to compile job/hours list...");
		Y: for (TradingStep step: shoppingPlan) {
			if (step.margBuyer>=step.buyers.size()) break Y;
			TradeOffer buyer=step.buyers.get(step.margBuyer);
			if (testing) System.out.println(buyer.toString());
			boolean nextJob=false;
			if (jobID<0) nextJob=true;
			else if (jobs[jobID].equals(buyer)==false) nextJob=true; 
			if (nextJob==true) {
				jobID++;
				jobs[jobID]=buyer;
				if (jobs[jobID]==null) {
					System.out.println("ERROR: Null job added to jobs vector.");
					Exception ex=new Exception();
					ex.printStackTrace();
					System.exit(1);
				}
			} 
			if (step.lStar!=Double.NaN) workPerJob[jobID]=workPerJob[jobID]+step.lStar;
			else {
				System.out.println("ERROR IN EDOL_PLANNING: step.laborStep is NaN");
				Exception ex=new Exception();
				ex.printStackTrace();
				System.exit(1);
			}
		}
		if (testing) {
			System.out.println("LABOR SUPPLY: "+a.laborSupply());
			System.out.println("OVERHEAD LABOR");
			for (int i=0; i<productionSet.length; i++) {
				if (i!=m.moneyID) {
					if (productionSet[i]) {
						System.out.print("   on good "+i+": ");
						System.out.println(pFunction.getL0(i));
					}					
				}
			}
			System.out.println("JOB LIST. "+numJobs+" JOBS");
			for (int i=0; i<numJobs; i++) {
				System.out.println(jobs[i].toString());
				System.out.println("WORK AT THIS JOB: "+workPerJob[i]+"\n");
			}
		}
		for (int i=0; i<numJobs; i++) {
			// This is where sales are executed.
			try {
//				System.out.println("agent "+a.id);
//				System.out.println("workPerJob["+i+"]="+workPerJob[i]);
//				System.out.println("jobs["+i+"]="+jobs[i]);
				if (workPerJob[i]!=Double.NaN) Transactions.buy(a, jobs[i], workPerJob[i]);		
				else {
					System.out.println("ERROR IN EDOL_PLANNING: workPerJob["+i+"]=Double.NaN");
					Exception ex=new Exception();
					ex.printStackTrace();
					System.exit(1);
				}
			} catch(Exception ex) {ex.printStackTrace();System.exit(1);}
		}
		for (TradingStep step: shoppingPlan) {
			step.executePurchases(a);
		}
		if (testing) {
			System.out.println("In EDOL_Planning.manage(). Agent "+a.id+" is about to start consuming, with inventory... ");
/*			for (int i=0; i<a.has.length; i++) {
				System.out.println("    "+a.has[i]+" units of good "+i);
			}*/
		}
		a.consume();
		a.shoppingPlan=shoppingPlan;
		a.sellSet=sellSet;
		// The Agent recalls five turns' worth of production sets to use to update his production universe
		a.productionSets[4]=a.productionSets[3];
		a.productionSets[3]=a.productionSets[2];
		a.productionSets[2]=a.productionSets[1];
		a.productionSets[1]=a.productionSets[0];
		a.productionSets[0]=pSet;
	}
}
