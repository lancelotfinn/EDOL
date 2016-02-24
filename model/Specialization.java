package model;
import java.util.*;

public class Specialization implements ValueFunction {
	public ScoredSet utility(MarketParameters m,ArrayList<Integer> sellSet) {
		boolean testing=false;
//		if (Math.random()<0.01) testing=true;
		AvoidCostPF pFunction=(AvoidCostPF) m.pFunction;
		double fixedLabor=0;
		for (Integer i: sellSet) {
			fixedLabor=fixedLabor+pFunction.getL0(i);
		}
		if (fixedLabor>m.labor) {
			ScoredSet ret=new ScoredSet();
			ret.set=sellSet;
			ret.score=-99;
			if (testing) System.out.println(ret.toString());
			return ret;
		}
		if (testing) {
			System.out.println("TESTING Specialization.utility()");
			System.out.println("   Sell set: "+InframarginalOptimizer.printInts(sellSet));
		}
		// Only include in the "universe" those goods which would not displace goods in the sell set.
		ArrayList<Integer> universe=new ArrayList<Integer>();
		ArrayList<TradeOffer> buyers_sellSet=new ArrayList<TradeOffer>();
		for (Integer i: sellSet) {
			for (TradeOffer offer: m.buyers[i]) {
				buyers_sellSet.add(offer);
			}
		}
		Collections.sort(buyers_sellSet);
		int n=m.uFunction.coeffs.length;
		for (int i=0; i<n; i++) {
			if ((sellSet.contains(i)==false)&(m.productionUniverse.contains(i)==true)) {
				if (testing) System.out.println("Considering adding good "+i+" to universe...");
				ArrayList<TradeOffer> buyers_i=m.buyers[i];
				boolean universe_contains_i=(new PreferredToSell()).i_wage_above_j(buyers_sellSet, buyers_i, m.labor);
				if (universe_contains_i) universe.add(i);
			}
		}
		if (testing) System.out.println("   Universe: "+InframarginalOptimizer.printInts(universe));
		ValueFunction v=new Shopping();
		PreferredToProduce p=new PreferredToProduce();
		ScoredSet ret=InframarginalOptimizer.optimalSet(universe, m, sellSet, v, p);
		if (testing) System.out.println(ret.toString());
		return ret;
	}
	
	public String type() {
		return "Specialization";
	}
}
