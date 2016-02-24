package model;
import java.io.*;

public class UtilityFunction implements Serializable {
	public Double[] coeffs;
	public Double sigma;
	
	public UtilityFunction(int numGoods) {
		coeffs=new Double[numGoods];
		sigma=0.5;
/*		for (int i=0; i<numGoods-1; i++) {
			coeffs[i]=1; // The default is for all goods to be equally preferred.
		}*/
	}
	
	public UtilityFunction clone() {
		UtilityFunction ret=new UtilityFunction(coeffs.length); 
		ret.sigma=sigma;
		for (int i=0; i<coeffs.length; i++) {
			ret.coeffs[i]=coeffs[i];
		}
		return ret;
	}
	
	public double utility(double[] qtys) {
		double innerSummation=0; // This is to be a sum of ai*qi^rho, where ai is a coefficient, qi a qty of good xi, and rho the exponent.
		for (int i=0; i<coeffs.length; i++) {
			if (qtys[i]<0) {
				System.out.println("ERROR: Agent is thinking of consuming a negative quantity of good "+i);
				qtys[i]=0;
			}
			double summationTerm=coeffs[i]*Math.pow(qtys[i],sigma);
			innerSummation=innerSummation+summationTerm;
		}
		double utility=Math.pow(innerSummation,1/sigma);
		return utility;
	}

	public ConsumptionReport consume(Agent agent, 	// the quantities of goods that the agent consumes
			int money, 								// identifies the money good, which does not contribute to utility at this stage
			int capital,							// identifies the capital good, which does not contribute to utility at this stage
			double savingsRate,						// determines how much foregone consumption will be diverted to capital
			double capitalConversionRate,			// the rate at which foregone consumption is converted to capital
			double depreciationRate					// the rate at which capital depreciates
			) throws DimensionalityException, NegativeConsumptionException {
		boolean testing=false;
		if (testing) {
			System.out.println("In UtilityFunction.consume(). Agent is about to start consuming, with inventory... ");
			for (int i=0; i<agent.has.length; i++) {
				System.out.println("    "+agent.has[i]+" units of good "+i);
			}
			System.out.println("Deprection rate = "+depreciationRate);
			System.out.println("Capital conversion rate = "+capitalConversionRate);
			System.out.println("Savings rate = "+savingsRate);
			System.out.println("sigma = "+sigma);
		}
		ConsumptionReport report=new ConsumptionReport(coeffs.length);
		double innerSummation=0; // This is to be a sum of ai*qi^rho, where ai is a coefficient, qi a qty of good xi, and rho the exponent.
		for (int i=0; i<coeffs.length; i++) {
			if ((i!=money)&(i!=capital)) {
				if (agent.has[i]<0) {
					System.out.println("ERROR: Agent is thinking of consuming a negative quantity of good "+i);
				}
				double summationTerm=coeffs[i]*Math.pow(agent.has[i],sigma);
				innerSummation=innerSummation+summationTerm;
				if (coeffs[i]>0) report.used[i]=agent.has[i];
				if (coeffs[i]==0) report.wasted[i]=agent.has[i];	
				agent.has[i]=0;
			}
		}
		double fullUtility=Math.pow(innerSummation,1/sigma); // Exponent exterior to the parenthesis in the CES utility function
		report.utility=fullUtility*(1-savingsRate); // Whatever is not saved is enjoyed as consumption
		double investment=fullUtility*savingsRate*capitalConversionRate;
		agent.has[capital]=agent.has[capital]*(1-depreciationRate)+investment; // Capital depreciation and investment offset each other
		report.investment=investment; // This is total investment, not investment net of depreciation
/*		if (((fullUtility>0)|(fullUtility==0)|(fullUtility<0))==false) {
			System.out.println("ERROR in UtilityFunction.utility(). \nCONSUMPTION");
			OutputOMatic.printNumSeries(agent.has);
			throw new NegativeConsumptionException();
		} */
		if (testing) {
			System.out.println("Full utility = "+fullUtility);
			System.out.println("Utility = "+report.utility);
			System.out.println("Investment = "+report.investment);
		}
		return report;
	}

/*	
	public boolean canEnjoy(int good) {
		boolean canEnjoy=coeffs[good]>0;
		return canEnjoy;
	}
	
	public int[] canEnjoy() {
		int count=0;
		for (int i=0; i<coeffs.length; i++) {
			if (coeffs[i]>0) count++;
		}
		int[] consumables=new int[count];
		count=0;
		for (int i=0; i<coeffs.length; i++) {
			if (coeffs[i]>0) {
				consumables[count]=i;
				count++;
			}
		}
		return consumables;
	}
*/	
	public String toString() {
		String ret="Utility Function (CES)\n";
		for (int i=0; i<coeffs.length; i++) {
			ret=ret+"Good "+i+" a: "+coeffs[i]+"\n";
		}
		return ret;
	}
/*	
	public double mrs(Agent a, int good1, int good2) {
		// Formula: (du/dxi)/(du/dxj)=(ai/aj)*(xi/xj)^(sigma-1)
		boolean testing=false;
		double ai=coeffs[good1];
		double aj=coeffs[good2];
		double xi=a.has(good1);
		double xj=a.has(good2);
		double ret=1;
		if ((ai>0)&(aj>0)&(xi>0)&(xj>0)) ret=(ai/aj)*Math.pow(xi/xj,sigma-1);
		if (testing==true) System.out.println("UtilityFunction.mrs(). i="+good1+". j="+good2+". ai="
				+ai+". aj="+aj+". xi="+xi+". xj="+xj+". mrs="+ret);
		return ret;
	}
*/
}
