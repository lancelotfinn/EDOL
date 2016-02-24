package model;
import java.util.*;

public class GenericTrader implements Trader {
	public Agent owner;
	public ArrayList<TradeOffer> tradeOffers;
	private double[] inventory;
	public int id;
	public ArrayList<Transaction> transactions;
	
	public double[] getInventory() {
		return inventory;
	} 
	
	public ArrayList<Transaction> getTransactions() {
		return transactions;
	}
	
	public void increaseInventory(int good,double qty) {
		if ((good>inventory.length)|(((qty>0)|(qty<0)|(qty==0))==false)) {
			new Exception().printStackTrace();
			System.exit(1);
		}
		inventory[good]=inventory[good]+qty;
	}
	
	public GenericTrader(Agent o, Economy w) {
		owner=o;
		inventory=new double[w.numGoods];
		for (int i=0; i<inventory.length; i++) {
			inventory[i]=0;
		}
		tradeOffers=new ArrayList<TradeOffer>();
		transactions=new ArrayList<Transaction>();
	}
	
	public String id() {
		String ret="Trader "+id;
		return ret;
	}
	
	public ArrayList<TradeOffer> tradeOffers() { // Convert the trade offers ArrayList to an array and return
		return tradeOffers;
	}
	
	public TradeOffer startTrading(int buy, int sell, double price) {
		TradeOffer t=new TradeOffer(this,sell,buy,price);
		tradeOffers.add(t);
		return t;
	}
	
	public boolean defunct() {
		return false; // No exit algorithm here.
	}
	
	public void acceptInvestment(int good, double qty) {
		inventory[good]=inventory[good]+qty;
	}
	
	public TradeOffer getPrice(int sell, int buy) { // Only return a trade offer if BOTH buy and sell goods match
		TradeOffer ret=null;
		Y: for (TradeOffer t: tradeOffers) {
			if ((t.pay==sell)&(t.sell==buy)) { // If the trade offer is buying what the agent has to sell and selling what it has to buy...
				ret=t;
				break Y;
			}
		}
		return ret;
	}
	
	public double inventory(int good) { 
/*		if ((Double)(inventory[good])==null) {
			System.out.println("Trader's inventory of good "+good+" is null.");
			new Exception().printStackTrace();
			System.exit(1);
		} */
		return inventory[good];
	}
	
	public void executeSale(double qty,double price,int buy,int sell) {
		inventory[buy]=inventory[buy]+qty*price;
		inventory[sell]=inventory[sell]-qty;
		if (inventory[sell]<0.0000000000000001) inventory[sell]=0;
	}
	
	public void activate() {}
}
