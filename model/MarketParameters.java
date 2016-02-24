package model;
import java.util.*;

import model.Agent;
import model.ProductionFunction;
import model.Retailer;
import model.TradeOffer;
import model.Trader;
import model.UtilityFunction;

public class MarketParameters {
	// Externally defined market variables.
	public int agentID;
	public UtilityFunction uFunction;
	public ProductionFunction pFunction;
	public ArrayList<TradeOffer>[] buyers;
	public ArrayList<TradeOffer>[] sellers;
	public ArrayList<Integer> productionUniverse;
	public int moneyID;
	public int capitalID;
	public double money;
	public double capital;
	public double labor;
	
	public String toString() {
		String ret="MARKET PARAMETERS.";
//		ret=ret+"\n"+uFunction.toString();
//		ret=ret+"\n"+pFunction.toString();
		for (int i=0; i<buyers.length; i++) {
			if (i!=moneyID) {
				ret=ret+"\nBUYERS OF GOOD "+i+". ";
				for (TradeOffer offer: buyers[i]) {
					ret=ret+"\n"+offer.toString()+" from trader with lifespan "+((Retailer)offer.trader).lifespan;
				}
				ret=ret+"\nSELLERS OF GOOD "+i+". ";
				for (TradeOffer offer: sellers[i]) {
					ret=ret+"\n"+offer.toString()+" from trader with lifespan "+((Retailer)offer.trader).lifespan;
				}				
			}
		}
		ret=ret+"\nMONEY: "+money+" OF MONETARY GOOD "+moneyID;
		ret=ret+"\nLABOR: "+labor;
		return ret;
	}
	
	public void populateData(Agent a) {
		boolean testing=false;
		boolean testing2=false;
		if (testing) System.out.println("TESTING MarketParameters.populateData()");
		if (testing2) {
			System.out.println("TESTING MarketParameters.populateData(). Agent ID="+a.id+"\n INVENTORY:");
			for (int i=0; i<a.has.length; i++) {
				System.out.println("     Good "+i+": "+a.has[i]);
			}
		}
		agentID=a.id;
		moneyID=a.getEconomy().money;
		capitalID=a.getEconomy().capital;
		money=a.has(moneyID);
		capital=a.has(capitalID);
		labor=a.laborSupply();
		int n=a.getEconomy().numGoods;
		buyers=(ArrayList<TradeOffer>[]) new ArrayList[n];
		sellers=(ArrayList<TradeOffer>[]) new ArrayList[n];
		for (int i=0; i<n; i++) {
			if (i!=moneyID) {
				buyers[i]=new ArrayList<TradeOffer>();
				sellers[i]=new ArrayList<TradeOffer>();
			}
		}
		for (Trader t: a.knows) {
			for (TradeOffer offer: t.tradeOffers()) {
				if (offer.pay==moneyID) {
					if (offer.limit()>0) sellers[offer.sell].add(offer);
				}
				if (offer.sell==moneyID) {
					if (offer.limit()>0) buyers[offer.pay].add(offer);
				}
			}
		}
		ArrayList<TradeOffer> altoff=new ArrayList<TradeOffer>();
		for (int i=0; i<buyers.length; i++) {
			ArrayList<TradeOffer> list=buyers[i];
			if (i!=moneyID) {
				for (int j=0; j<list.size(); j++) {
					TradeOffer offer=list.get(j);
					if (altoff.contains(offer)) {
						list.remove(j);
						j--;
					}
					altoff.add(offer);
				}
				altoff=new ArrayList<TradeOffer>();				
			}
			list=sellers[i];
			if (i!=moneyID) {
				for (int j=0; j<list.size(); j++) {
					TradeOffer offer=list.get(j);
					if (altoff.contains(offer)) {
						list.remove(j);
						j--;
					}
					altoff.add(offer);
				}
				altoff=new ArrayList<TradeOffer>();				
			}
		}		
		// Check for duplicate trade offers
		int duplicates=0;
		altoff=new ArrayList<TradeOffer>();
		for (int i=0; i<buyers.length; i++) {
			ArrayList<TradeOffer> list=buyers[i];
			if (i!=moneyID) {
				for (TradeOffer offer: list) {
					if (altoff.contains(offer)) duplicates++;
					altoff.add(offer);
				}
				altoff=new ArrayList<TradeOffer>();				
			}
			list=sellers[i];
			if (i!=moneyID) {
				for (TradeOffer offer: list) {
					if (altoff.contains(offer)) duplicates++;
					altoff.add(offer);
				}
				altoff=new ArrayList<TradeOffer>();				
			}
		}
		if (duplicates>0) {
			System.out.println("MarketParameters.populateData() compiled with "+duplicates+" duplicates");
			System.exit(1);
		}
		for (int i=0; i<n; i++) {
			if (i!=moneyID) {
				Collections.sort(buyers[i]);
				Collections.sort(sellers[i]);
			}
		}
		uFunction=a.likes();
		pFunction=a.makes();
		productionUniverse=new ArrayList<Integer>();
		for (int i=0; i<n; i++) {
			if (i!=moneyID) productionUniverse.add(i);
		}
		if (testing) System.out.println(toString());
	}
}
