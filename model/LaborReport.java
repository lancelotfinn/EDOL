package model;

import java.io.Serializable;

public class LaborReport implements Serializable {
	public Double[] labor;
	public Double retailing;
	public int sellGood;
	public int id;
	
	public LaborReport(Agent a) {
		id=a.id;
		labor=new Double[a.getEconomy().numGoods];
		for (int i=0; i<labor.length; i++) {
			labor[i]=0.0;
		}
	}
	
	public String toString() {
		String ret="AGENT "+id+" LABOR REPORT\n";
		for (int i=0; i<labor.length; i++) {
			if (labor[i]>0) ret=ret+"Labor for good "+i+": "+labor[i]+"\n";
		}
		return ret;
	}
	
	public java.util.ArrayList<Integer> reportDiversification() {
		java.util.ArrayList<Integer> ret=new java.util.ArrayList<Integer>();
		for (int i=0; i<labor.length; i++) {
			if (labor[i]>0) ret.add(i);
		}
		return ret;
	}
}
