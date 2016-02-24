package model; 
import java.util.*;
import executive.*;

public class AvoidCostPF extends ProductionFunction { // Avoidable (startup) cost production function
	private Double[] l0;
	
	public AvoidCostPF(int numGoods) {
		l0=new Double[numGoods];
		l0[0]=1000000000.0; // money is not producible by agents
		l0[1]=1000000000.0; // capital is not producible by agents in this sense; rather it comes from foregone consumption
/*		for (int i=2; i<l0.length; i++) {
			l0[i]=0.1;
		}*/
	}
	
	public AvoidCostPF clone() {
		AvoidCostPF ret=new AvoidCostPF(l0.length);
		for (int i=0; i<l0.length; i++) {
			ret.l0[i]=l0[i];
		}
		return ret;
	}
	
	public Double getL0(int i) {
		return l0[i];
	}
	
	public void setL0(int i, double newL0) {
		l0[i]=newL0;
	}
	
	public ArrayList<Integer> makeables(double laborSupply) {
		ArrayList<Integer> makeables=new ArrayList<Integer>();
		for (int i=0; i<l0.length; i++) {
			if (l0[i]<laborSupply) makeables.add(i);
		}
		return makeables;
	}
	
	public LaborReport produce(Agent a, Double[] laborAllocation) throws Exception {
		boolean testing=false;
		if (testing) System.out.println("TESTING AvoidCostPF.produce()");
		for (int i=0; i<laborAllocation.length; i++) {
			if (laborAllocation[i]<0) {
				System.out.println("ERROR: Agent "+a.id+" is trying to produce good "+i+
						" with a negative quantity "+laborAllocation[i]+" of labor.");
				throw new Exception();
			}
		}
		LaborReport report=new LaborReport(a);
		double maxLabor=a.laborSupply(report); // The agent needs this in order to record labor spent retailing.
		double planLabor=0;
		for (Double d: laborAllocation) {
			if (d!=null) planLabor=planLabor+d;
		}
		if (planLabor>maxLabor+0.000001) {
			System.out.println("ERROR in AvoidCostPF.produce(). Plan labor "+planLabor+" exceeds available labor "+maxLabor+".");
			System.out.println("Agent ID "+a.id+". capital="+a.has[a.getEconomy().capital]+". turn="+a.getEconomy().turn);
//			System.exit(1);
		}
		int n=laborAllocation.length;
		for (int i=0; i<n; i++) {
			if (i!=a.getEconomy().money) {
				Double thisLaborStep=laborAllocation[i];
				double thisProduction=Math.max(thisLaborStep-l0[i],0);
				try {
					a.gets(i,thisProduction);				
				} catch (Exception ex) {ex.printStackTrace();System.exit(1);}				
			}
		}
		report.labor=laborAllocation;
		return report;
	}

	public Integer numGoods() {
		return l0.length;
	}
	
	public String toString() {
		String ret="Avoidable Cost Production Function\n";
		for (int i=0; i<l0.length; i++) {
			ret=ret+"Good "+i+", L0: "+l0[i]+"\n";
		}
		return ret;
	}
	
	public String type() {
		return "AvoidCost";
	}
}
