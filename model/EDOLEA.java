package model;
import java.util.*;
import java.io.*;

public class EDOLEA implements EntryAlgorithm, Serializable { // Endogenous division of labor (economy) entry algorithm
	public int[] selectGoods(Agent a) {
		ArrayList<Integer> candidates=new ArrayList<Integer>();
		for (int i=0; i<a.getEconomy().numGoods; i++) {
			candidates.add(i);
		}
		int money=a.getEconomy().money;
		candidates.remove(money);
		int selector=(int) (Math.random()*(candidates.size()-1));
		int good1=candidates.get(selector);
		int good2=money;
		int[] ret={good1,good2};
		return ret;
	}
	
	public String type() {
		return "EDOL EA";
	}
}
