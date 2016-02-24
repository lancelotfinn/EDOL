package model;

public class BasicEA implements EntryAlgorithm {
	public int[] selectGoods(Agent a) {
		int numGoods=a.getEconomy().numGoods;
		int good1=a.makes().makeables()[0];
		int good2=-99;
		if (a.getEconomy().money!=null) good2=a.getEconomy().money;
		else good2=(int) (Math.random()*(numGoods-1));
		if (good2>=good1) good2++;
		int[] ret={good1,good2};
		return ret;
	}
	
	public String type() {
		return "Basic EA";
	}
}
