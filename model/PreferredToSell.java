package model;
import java.util.ArrayList;

public class PreferredToSell implements PreferenceRelation {
	public boolean preferredTo(MarketParameters m,ArrayList<Integer> fixedSet,int i,int j) {
		boolean testing=false;
		if (testing) System.out.println("TESTING PreferredToSell.preferredTo()");
		AvoidCostPF pFunction=(AvoidCostPF) m.pFunction;
		ArrayList<TradeOffer> buyers_i=m.buyers[i];
		ArrayList<TradeOffer> buyers_j=m.buyers[j];
		if (testing) {
			System.out.println("i="+i+", j="+j);
			System.out.println("buyers_i.size()="+buyers_i.size()+". buyers_j.size()="+buyers_j.size());
			System.out.println("l0 i="+pFunction.getL0(i)+". l0_j="+pFunction.getL0(j));
		}
		if (pFunction.getL0(j)<=pFunction.getL0(i)) return false;
		return i_wage_above_j(buyers_i,buyers_j,m.labor);
	}
	
	public boolean i_wage_above_j(ArrayList<TradeOffer> buyers_i,ArrayList<TradeOffer> buyers_j,double labor) {
		boolean testing=false;
		if (testing) System.out.println("TESTING PreferredToSell.i_wage_above_j()");
		double li_t=0;
		double lj_t=0;
		double l_t=0;
		int mi_t=0;
		int mj_t=0;
		while (l_t<=labor) {
			if (testing) System.out.println("l_t="+l_t+". mi_t="+mi_t+". mj_t="+mj_t);
			TradeOffer offer_i=null;
			if (buyers_i.size()>mi_t) offer_i=buyers_i.get(mi_t);
			TradeOffer offer_j=null;
			if (buyers_j.size()>mj_t) offer_j=buyers_j.get(mj_t);
			double wage_i=0;
			if (offer_i!=null) wage_i=1/offer_i.getPrice();
			double wage_j=0;
			if (offer_j!=null) wage_j=1/offer_j.getPrice();
			if (testing) System.out.println("wage_i="+wage_i+". wage_j="+wage_j);
			if (wage_j>wage_i) {
				if (testing) System.out.println("wage j is higher than wage i: i_wage_above_j is FALSE");
				return false;
			}
			Double next_jobout_i=Double.POSITIVE_INFINITY;
			if (offer_i!=null) next_jobout_i=li_t+offer_i.limit()/wage_i;
			Double next_jobout_j=Double.POSITIVE_INFINITY;
			if (offer_j!=null) next_jobout_j=lj_t+offer_j.limit()/wage_j;
			l_t=Math.min(next_jobout_i,next_jobout_j);
			if (l_t==next_jobout_i) {
				mi_t++;
				li_t=l_t;
			}
			if (l_t==next_jobout_j) {
				mj_t++;
				lj_t=l_t;
			}
			if (testing) System.out.println("l_t="+l_t);
		}
		return true;
	}
	
	public String type() {
		return "PreferredToSell";
	}
}
