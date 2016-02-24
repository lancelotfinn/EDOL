package model;
import java.util.*;


public class PreferredToProduce implements PreferenceRelation {
	public boolean preferredTo(MarketParameters m, ArrayList<Integer> sellSet, int i, int j) {
		boolean testing=false;
		if (testing) System.out.println("TESTING PreferredToProduce.preferredTo()...");
		AvoidCostPF pFunction=(AvoidCostPF) m.pFunction;
		// First, compile a list of employment opportunities, a wage series.
		ArrayList<TradeOffer> buyers=new ArrayList<TradeOffer>();
		int n=m.uFunction.coeffs.length;
		for (int k=0; k<n; k++) {
			if (sellSet.contains(k)) {
				for (TradeOffer offer: m.buyers[k]) {
					buyers.add(offer);
				}				
			}
		}
		Collections.sort(buyers);
		// Second, figure out how many of these employers the agent might actually turn to.
		double minWage=0;
		double maxWage=0;
		int minWageFinder=-1;
		if (buyers.size()>0) {
			double laborSpent=0;
			Y: for (TradeOffer offer: buyers) {
				minWageFinder++;
				laborSpent=laborSpent+offer.limit()*offer.getPrice();
				if (laborSpent>m.labor) break Y;
			}
			minWage=1/buyers.get(minWageFinder).getPrice();
			maxWage=1/buyers.get(0).getPrice();			
		}
		// Third, calculate how much the agent might earn if he exploits all sales opportunities to the max.
		double maxExp=m.money;
		double laborSpent=0;
		Y: for (int k=0; k<=minWageFinder; k++) {
			TradeOffer offer=buyers.get(k);
			double laborIncrement=offer.limit()*offer.getPrice();
			if (laborSpent+laborIncrement<m.labor) {
				laborSpent=laborSpent+laborIncrement;
				maxExp=maxExp+offer.limit();
			}
			if (laborSpent+laborIncrement>m.labor) {
				maxExp=maxExp+(m.labor-laborSpent)/offer.getPrice();
				break Y;
			}
		}
		// Fourth, find the minimum price for good i and the maximum price for good j
		Double minPrice_i=Double.POSITIVE_INFINITY;
		if (m.sellers[i].size()>=1) minPrice_i=m.sellers[i].get(0).getPrice();
		double exp_j=0;
		int maxPriceFinder_j=-1;
		Y: for (TradeOffer offer: m.sellers[j]) {
			maxPriceFinder_j++;
			exp_j=exp_j+offer.limit()*offer.getPrice();
			if (exp_j>m.money+maxExp) break Y;
		}
		Double maxPrice_j=Double.POSITIVE_INFINITY;
		if (maxPriceFinder_j>=0) maxPrice_j=m.sellers[j].get(maxPriceFinder_j).getPrice();
		// Fifth, if there is no wage at the margin, use a weaker preference condition and exit to avoid problems later.
		boolean preferred=true;
		if (minWage==0) {
			boolean l0_preferred=pFunction.getL0(i)<pFunction.getL0(j);
			boolean a_preferred=m.uFunction.coeffs[i]<m.uFunction.coeffs[j];
			boolean price_preferred=minPrice_i>maxPrice_j;
			if (l0_preferred&a_preferred&price_preferred) return true;
			else return false;
		}
		// Sixth, find the "marginal value of self-production" for goods i and j
		double mValMake_i=Math.pow(m.uFunction.coeffs[i],1/(1-m.uFunction.sigma));
		if (minPrice_i<Double.POSITIVE_INFINITY) mValMake_i=mValMake_i*
			(1-Math.pow(minPrice_i/maxWage,m.uFunction.sigma/(m.uFunction.sigma-1)));
		double mValMake_j=Math.pow(m.uFunction.coeffs[j],1/(1-m.uFunction.sigma));
		if (maxPrice_j<Double.POSITIVE_INFINITY) mValMake_j=mValMake_j*
			(1-Math.pow(maxPrice_j/minWage,m.uFunction.sigma/(m.uFunction.sigma-1)));
		// Finally, try the conditions and return the result
		if (mValMake_i<mValMake_j) preferred=false;
		if (pFunction.getL0(i)>pFunction.getL0(j)) preferred=false;
		if ((mValMake_i<=mValMake_j)&(pFunction.getL0(i)>=pFunction.getL0(j))) preferred=false;
		if ((i==j)&testing) System.out.println("i=j="+i+"\nmValMake_i="+mValMake_i+", mValMake_j="+
				mValMake_j+"\nminPrice_i="+minPrice_i+", maxPrice_j="+maxPrice_j+
				"\nmaxWage="+maxWage+", minWage="+minWage+"\ni is preferred to j: "+preferred);
		return preferred;
	}
	
	public String type() {
		return "PreferredToProduce";
	}
}
