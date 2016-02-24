package model;
import java.io.*;

public class ConsumptionReport implements Serializable, Comparable<ConsumptionReport> {
	public double[] used;
	public double[] wasted;
	public double[] invested;
	public double utility;
	public double investment;
	
	public ConsumptionReport(int numGoods) {
		used=new double[numGoods];
		wasted=new double[numGoods];
		invested=new double[numGoods];
		for (int i=0; i<numGoods; i++) {
			used[i]=0;
			wasted[i]=0;
			invested[i]=0;
		}
		utility=0;
		investment=0;
	}
	
	public java.util.ArrayList<Integer> reportDiversification() {
		java.util.ArrayList<Integer> ret=new java.util.ArrayList<Integer>();
		for (int i=0; i<used.length; i++) {
			if (used[i]>0) ret.add(i);
		}
		return ret;
	}
	
	public String toString() {
		String ret="CONSUMPTION REPORT\nUSED:\n";
		for (int i=0; i<used.length; i++) {
			if (used[i]>0) ret=ret+"GOOD "+i+": "+used[i]+"\n";
		}
		ret=ret+"WASTED:\n";
		for (int i=0; i<wasted.length; i++) {
			if (wasted[i]>0) ret=ret+"GOOD "+i+": "+wasted[i]+"\n";
		}
		ret=ret+"UTILITY: "+utility+"\nEND CONSUMPTION REPORT\n";
		return ret;
	}
	
	public int compareTo(ConsumptionReport other) {
		if (other.utility>utility) return -1;
		if (other.utility==utility) return 0;
		return 1;
	}
}
