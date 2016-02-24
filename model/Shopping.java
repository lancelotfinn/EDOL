package model;
import java.util.*;

public class Shopping implements ValueFunction {
	public ScoredSet utility(MarketParameters m,ArrayList<Integer> pSet) {
		boolean testing=false;
//		if (Math.random()<0.01) testing=true;
		int methodID=(int) (Math.random()*90000+10000);
		if (testing) {
			System.out.println("\n\n\nTESTING Shopping.utility(). agentID="+m.agentID+". methodID="+methodID);
			System.out.println("m.labor="+m.labor+", m.money="+m.money);
		}
		ArrayList<TradeOffer> buyers=new ArrayList<TradeOffer>();
		for (Integer i: pSet) {
			ArrayList<TradeOffer> applicableSeries=m.buyers[i];
			for (TradeOffer offer: applicableSeries) {
				buyers.add(offer);
			}
		}
		Collections.sort(buyers);
		int n=m.uFunction.coeffs.length;
		Boolean[] buyableSet=new Boolean[n];
		Boolean[] productionSet=new Boolean[n];
		productionSet[m.moneyID]=false;
		if (testing) System.out.print("SHOPPING: production set contains ");
		for (int i=0; i<n; i++) {
			if (i!=m.moneyID) {
				buyableSet[i]=false;
				productionSet[i]=false;
				if (pSet.contains(i)==true) {
					if (testing) {
						System.out.print(i+",");
					}
					productionSet[i]=true;
				} else {
					if (m.sellers[i].size()>0) buyableSet[i]=true;				
				}				
			}
		}
		if (testing) System.out.println();
		AvoidCostPF pFunction=(AvoidCostPF) m.pFunction;
		double overhead=0;		
		for (int i=0; i<n; i++) {
			if (i!=m.moneyID) {
				if (productionSet[i]==true) overhead=overhead+pFunction.getL0(i);				
			}
		}
		if (overhead>=m.labor) {
			ScoredSet ret=new ScoredSet();
			ret.set=pSet;
			ret.score=-99;
			if (testing) System.out.println("Labor supply of "+m.labor+" is insufficient to cover overhead of "+overhead);
			return ret;
		}
		double labor=m.labor-overhead;
		if (testing) {
			System.out.print("ABOUT TO START TradingStep constructor, with pSet: ");
			for (Integer i: pSet) {
				System.out.print(i+" ");
			}
			System.out.println("; and disposable (non-overhead) labor="+labor);
		}
		TradingStep firstStep=new TradingStep(m.agentID, buyableSet,productionSet,buyers,m.sellers,
				m.money,m.capital,m.moneyID,m.capitalID,labor,m.uFunction,pFunction);
		ArrayList<TradingStep> shoppingPlan=firstStep.optimize();
		// Now find out how much of each good the agent ends up with.
		double[] qtys=new double[n];
		for (int i=0; i<n; i++) {
			qtys[i]=0;
		}
		qtys[m.moneyID]=qtys[m.moneyID]+m.money;
		if (testing) System.out.println("TESTING Shopping.utility(). Line 65");
		for (TradingStep step: shoppingPlan) {
			for (int i=0; i<n; i++) {
				qtys[i]=qtys[i]+step.delta_m[i]+step.delta_b[i];
			}
			qtys[m.moneyID]=qtys[m.moneyID]+step.deltaM;
		}
		if (testing) {
			System.out.println("TESTING Shopping.utility(). Line 73. methodID="+methodID+". By the way, sigma="+m.uFunction.sigma);
/*			for (int i=0; i<n; i++) {
				System.out.println("QTY OF GOOD "+i+": "+qtys[i]);
			}*/
		}
		double utility=0;
		try {
			utility=m.uFunction.utility(qtys);		
		} catch(Exception ex) {ex.printStackTrace();System.exit(1);}
		ScoredSet ret=new ScoredSet();
		ret.set=pSet;
		ret.score=utility;
		if (testing) {
			for (TradingStep t: shoppingPlan) {
				System.out.println(t.toString());
			}
			System.out.println("UTILITY: "+utility);
		}
		return ret;
	}
	
	public String type() {
		return "Shopping";
	}
}
