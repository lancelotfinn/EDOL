package model;
import java.util.*;

public class StatGenerator {
	/*
	public static double totUtil(ArrayList<Agent> population) {
		double totUtil=0;
		for (Agent a: population) {
			totUtil=totUtil+a.cons.utility;
		}
		return totUtil;
	}
	
	public static double avgUtil(ArrayList<Agent> population) {
		double totUtil=totUtil(population);
		double avgUtil=totUtil/population.size();
		return avgUtil;
	}
	
	public static double avgRelativePrice(ArrayList<Retailer> retailers) {
		double sumRelPrices=0;
		for (Retailer r: retailers) {
			double highPrice=Math.max(r.purchasingManager.getPrice(),r.salesManager.getPrice());
			double lowPrice=Math.min(r.purchasingManager.getPrice(),r.salesManager.getPrice());
			double relPrice=Math.pow(highPrice/lowPrice, 0.5);
			sumRelPrices=sumRelPrices+relPrice;
		}
		double avgRelPrice=sumRelPrices/retailers.size();
		return avgRelPrice;
	}
	
	public static double avgRoundaboutPrice(ArrayList<Retailer> retailers) {
		double sumRoundPrices=0;
		for (Retailer r: retailers) {
			sumRoundPrices=sumRoundPrices+r.purchasingManager.getPrice()*r.salesManager.getPrice();
		}
		double avgRoundPrice=sumRoundPrices/retailers.size();
		return avgRoundPrice;
	}
	
	public static double retailerExcessUtility(ArrayList<Agent> population) {
		double sumNWelfare=0;
		int nonRetailers=0;
		for (Agent a: population) {
			if (a.enterprises.size()==0) {
				nonRetailers++;
				sumNWelfare=sumNWelfare+a.cons.utility;
			}
		}
		double nWelfare=sumNWelfare/nonRetailers;
		double sumrwinc=0; // The variable name signifies "sum of retailer welfare increments"
		double numRetailers=0;
		for (Agent a: population) {
			if (a.enterprises.size()>0) {
				numRetailers=numRetailers+a.enterprises.size();
				sumrwinc=sumrwinc+(a.cons.utility-nWelfare)/a.enterprises.size();				
			}
		}
		double rwinc=sumrwinc/numRetailers;
		return rwinc;
	}
	
	public static Double giniCoeff(ArrayList<Double> data) {
		// Formula: G=(1/n)*(n+1-2*(SUM((n-i)*yi)/SUM(yi)))
		ArrayList<Integer> nan=new ArrayList<Integer>();
		for (int i=0; i<data.size(); i++) {
			if (data.get(i).isNaN()) nan.add(i);
		}
		Collections.sort(nan);
		int sizeNan=nan.size();
		for (int i=sizeNan-1; i>=0; i--) {
			data.remove(i);
		}
		if (data.size()==0) return null;
		Collections.sort(data);
//		for (Double d: data) {
//			System.out.print(d+",");
//		}
//		System.out.println();
		int n=data.size();
//		System.out.println("N: "+n);
		// Get inner denominator, SUM(yi)
		double denom=0;
		for (Double d: data) {
			denom=denom+d;
		}
//		System.out.println("DENOM: "+denom);
		// Get inner numerator, SUM((n+1+i)yi)
		double numer=0;
		for (int i=0; i<n; i++) {
			Double d=data.get(i);
			numer=numer+(n-i)*d;
		}
//		System.out.println("NUMER: "+numer);
		// Find the Gini coefficient
		double gini=(n+1-2*(numer/denom))/n;
		return gini;
	}
	
	public static double herfindahl(ArrayList<Double> data) {
		double sum=0;
		for (Double d: data) {
			sum=sum+d;
		}
		double herfindahl=0;
		for (Double d: data) {
			double increment=Math.pow(d/sum, 2);
			herfindahl=herfindahl+increment;
		}
		return herfindahl;
	}
	
	public static double welfareGini(ArrayList<Agent> population) {
		ArrayList<Double> data=new ArrayList<Double>();
		for (Agent a: population) {
			data.add(a.cons.utility);
		}
		double gini=giniCoeff(data);
		return gini;
	}
	
	public static int arbitrageurs(ArrayList<Agent> population) {
		int arbitrageurs=0;
		for (Agent a: population) {
			boolean arbitrage=false;
			double[] earnings=a.arbitrageEarnings();
			for (int i=0; i<earnings.length; i++) {
				if (earnings[i]>0) arbitrage=true;
			}
			if (arbitrage==true) arbitrageurs++;
		}
		return arbitrageurs;
	}

	public static double avgMarkup(ArrayList<Retailer> retailers) {
		double prodMarkup=1;
		int count=0;
		for (Retailer r: retailers) {
			if (r.lifespan>=5) {
				prodMarkup=prodMarkup*(1+r.markup);		
				count++;
			}
		}
		double avgMarkup=-99;
		if (count>0) avgMarkup=Math.pow(prodMarkup,1/(double)count)-1;
		return avgMarkup;
	}
	
	public static double totInventory(ArrayList<Retailer> retailers) {
		double totInventory=0;
		for (Retailer r: retailers) {
//			System.out.println("StatGenerator.totInventory(). r.inventory[r.good1]="+r.inventory[r.good1]+
//					". r.inventory[r.good2]="+r.inventory[r.good2]);
			totInventory=totInventory+r.getInventory()[r.r_money]+r.getInventory()[r.r_product];
		}
		return totInventory;
	}

	public static double monetariness(int[] transactions) {
		int maxTrans=-99; // "maximum transactions" or most transactions-intensive good
		int mtPosition=-99; // position of maximum transactions good
		for (int i=0; i<transactions.length; i++) {
			if (transactions[i]>maxTrans) {
				maxTrans=transactions[i];
				mtPosition=i;
			}
		}
		int sumOtherTrans=0; // the sum of transactions for all other (non max. trans.) goods
		for (int i=0; i<transactions.length; i++) {
			if (i!=mtPosition) {
				sumOtherTrans=sumOtherTrans+transactions[i];
			}
		}
		double monetariness=(double) maxTrans/(double) sumOtherTrans; // =1 if all transactions are mediated through "money"
		return monetariness;
	}
	
	public static double[][] useOrWaste(ArrayList<Agent> population) {
		int numGoods=population.get(0).getEconomy().numGoods;
		double[][] ret=new double[numGoods][2];
		for (int i=0; i<numGoods; i++) {
			ret[i][0]=0;
			ret[i][1]=0;
		}
		for (Agent a: population) {
			for (int i=0; i<numGoods; i++) { // 0=used, 1=waste
				ret[i][0]=ret[i][0]+a.cons.used[i];
				ret[i][1]=ret[i][1]+a.cons.wasted[i];
			}
		}
		return ret;
	}
	
	public static double[] useOrWaste(ArrayList<Agent> population, int signal) {
		double[][] useOrWaste=useOrWaste(population);
		double use=0;
		double waste=0;
		for (int i=0; i<useOrWaste.length; i++) {
			use=use+useOrWaste[i][0];
			waste=waste+useOrWaste[i][1];
		}
		double[] ret={use,waste};
		return ret;
	}

	/*
	public static double[] marketNetworkSummaryStats(ArrayList<Agent> population) {
		int sumKnows=0;
		int sumLoops=0;
		int sumGoalPaths=0;
		for (Agent a: population) {
			sumKnows=sumKnows+a.knows.size();
			sumLoops=sumLoops+a.loops;
			sumGoalPaths=sumGoalPaths+a.goalPaths;
		}
		double avgKnows=(double)sumKnows/(double)population.size();
		double avgLoops=(double)sumLoops/(double)population.size();
		double avgGoalPaths=(double)sumGoalPaths/(double)population.size();
		double[] ret={avgKnows, avgLoops, avgGoalPaths}; // 0: knows. 1: loops. 2: goals.
		return ret;
	}
	
	public static Double[][][] compileSummary(int numGoods, ArrayList<Transaction> data, int money) {
		boolean testing=true;
		// First, find all the "purchases" in the economy, i.e., where the pay good is money.
		ArrayList<AllTransactionsOfType> purchases=new ArrayList<AllTransactionsOfType>();
		Y: for (Transaction t: data) {
			boolean placed=false;
			if (t.pay!=money) continue Y;
			Z: for (AllTransactionsOfType a: purchases) {
				if (t.sell==a.sell) {
					a.transactions.add(t);
					placed=true;
					break Z;
				}
			}
			if (placed==false) {
				AllTransactionsOfType a=new AllTransactionsOfType(money, t.sell);
				purchases.add(a);
				a.transactions.add(t);
			}
		}
		if (testing==true) {
			System.out.println("CompileSummary in StatGenerator.\nPURCHASES");
			for (AllTransactionsOfType a: purchases) {
				System.out.println(a.summarize());
			}
		}
		// Now do the same thing for "supply," i.e., where the pay good is non-money (agents supplying retailers in exchange for money).
		ArrayList<AllTransactionsOfType> supply=new ArrayList<AllTransactionsOfType>();
		Y: for (Transaction t: data) {
			boolean placed=false;
			if (t.sell!=money) continue Y;
			Z: for (AllTransactionsOfType a: supply) {
				if (t.pay==a.pay) {
					a.transactions.add(t);
					placed=true;
					break Z;
				}
			}
			if (placed==false) {
				AllTransactionsOfType a=new AllTransactionsOfType(t.pay, money);
				supply.add(a);
				a.transactions.add(t);
			}
		}
		if (testing==true) {
			System.out.println("CompileSummary in StatGenerator.\nSUPPLY");
			for (AllTransactionsOfType a: supply) {
				System.out.println(a.summarize());
			}
		}
		// Now "compile" each of these sets of transactions to calculate the average buy and sell price of each good.
		for (AllTransactionsOfType a: purchases) {
			a.compile();
		}
		for (AllTransactionsOfType a: supply) {
			a.compile();
		}
		// The return takes the form of a three-dimensional matrix.
		// Position 1 indicates the good, Position 2 the price, Position 3 the quantity.
		// For position 2, 0=purchases, 1=supply.
		Double[][][] summary=new Double[numGoods][2][2];
		for (AllTransactionsOfType a: purchases) {
			summary[a.sell][0][0]=a.price;
			summary[a.sell][0][1]=a.qty;
		}
		for (AllTransactionsOfType a: supply) {
			// In order to get values denominated in money, we have to convert them.
			double price=1/a.price;
			double qty=a.qty*a.price; // Use the price to convert the quantity of money "sold" to the quantity of good supplied in return
			summary[a.pay][1][0]=price;
			summary[a.pay][1][1]=qty;
		}
		return summary;
	}
	
	public static int estR(int turns,ArrayList<Retailer> retailers) {
		int estR=0;
		for (Retailer r: retailers) {
			if (r.lifespan>=turns) estR++;
		}
		return estR;
	}
	
	public static double[][] matrixM(MoneyEconomy e) {
		double[][] m=new double[e.numGoods-1][e.numGoods-1];
		double[] qs=new double[e.numGoods-1];
		for (int i=0; i<e.numGoods-1; i++) {
			for (int j=0; j<e.numGoods-1; j++) {
				m[i][j]=0;
			}
			qs[i]=0;
		}
		for (Agent a: e.population) {
			int i=a.likes().favorite();
			if (i>=a.getEconomy().money) i--;
			int j=a.makes().makeables()[0];
			if (j>=a.getEconomy().money) j--;
			double productivity=((HowClowProductionFunction)a.makes()).productivity;
			m[i][j]=m[i][j]+productivity;
			qs[j]=qs[j]+productivity;
		}
		for (int i=0; i<e.numGoods-1; i++) {
			for (int j=0; j<e.numGoods-1; j++) {
				m[i][j]=m[i][j]/qs[i];
			}
		}
		return m;
	}
*/	
/*
	public static double[] walras(MoneyEconomy e) {
		double[][] prices=new double[e.numGoods-1][1];
		double[][] ones=new double[e.numGoods-1][1];
		for (int i=0; i<e.numGoods-1; i++) {
			prices[i][0]=1;
			ones[i][0]=1; 
		}
		double[][] m=matrixM(e);
		for (int i=0; i<500; i++) {
			prices=normalize(MatrixAlgebra.multiply(m,prices));
		}
		double[] ret=new double[e.numGoods-1];
		for (int i=0; i<e.numGoods-1; i++) {
			ret[i]=prices[i][0];
		}
		return ret;
	}
	
	public static double[][] normalize(double[][] matrix) {
		// This "normalizes" a matrix so that all its elements add up to one.
		double sumElements=0;
		for (int i=0; i<matrix.length; i++) {
			for (int j=0; j<matrix[0].length; j++) {
				sumElements=sumElements+matrix[i][j];
			}
		}
		matrix=MatrixAlgebra.scalarMultiply(matrix, 1/sumElements);
		return matrix;
	} */
}
