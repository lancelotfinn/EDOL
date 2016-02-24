package model;
import java.util.*;
import java.io.*;

public class Memory implements Serializable {
	public void purge(Agent a) {
		boolean testing=false;
		if (testing) {
			System.out.println("TESTING Memory.purge()\nBEFORE");
			for (Trader t: a.knows) {
				System.out.println("TRADER: "+t.toString());
			}
		}
		ArrayList<Trader> knows=new ArrayList<Trader>();
		for (Trader t: a.knows) {
			if ((knows.contains(t)==false)&(t.defunct()==false)) knows.add(t);
		}
		a.knows=knows;
		if (testing) {
			System.out.println("AFTER");
			for (Trader t: a.knows) {
				System.out.println("TRADER: "+t.toString());
			}
		}
	}
	
	public void forgets(Agent a) {
		if (a.knows.size()>a.getEconomy().AGENT_MEMORY_MAX-5) {
			int forgets=a.knows.size()-a.getEconomy().AGENT_MEMORY_MAX+5;
			for (int i=0; i<forgets; i++) {
				int selector=(int) (Math.random()*a.knows.size());
				a.knows.remove(selector);
			}
		}		
	}
	
	public ArrayList<Trader> friendsKnow(Agent a) {
		ArrayList<Trader> friendsKnow=new ArrayList<Trader>();
		for (Agent b: a.friends) {
			for (Trader t: b.enterprises) {
				if ((friendsKnow.contains(t)==false)&(a.knows.contains(t)==false)) friendsKnow.add(t);				
			}
			for (Trader t: b.knows) {
				if ((friendsKnow.contains(t)==false)&(a.knows.contains(t)==false)) friendsKnow.add(t);
			}
			for (Agent c: b.friends) {
				for (Trader t: c.enterprises) {
					if ((friendsKnow.contains(t)==false)&(a.knows.contains(t)==false)) friendsKnow.add(t);				
				}
				for (Trader t: c.knows) {
					if ((friendsKnow.contains(t)==false)&(a.knows.contains(t)==false)) friendsKnow.add(t);
				}				
			}
		}		
		return friendsKnow;
	}
	
	public void memory(Agent a) {
		boolean testing=false;
		if (testing==true) System.out.println("SimpleMemory. Start: "+new Date().getTime());
		purge(a);
		forgets(a);
		int willLearn=a.getEconomy().AGENT_MEMORY_MAX-a.knows.size();
		int count1=0;
		int count2=0;
		while ((count1<willLearn)&(count2<500)&(a.getEconomy().traders.size()>0)) {
			if (testing) {
				for (Trader t: a.knows) {
					System.out.println("TRADER "+t.id());
				}
			}
			int selector=(int) (Math.random()*a.getEconomy().traders.size());
			Trader t=a.getEconomy().traders.get(selector);
			if (a.knows.contains(t)==false) {
				a.knows.add(t);
				count1++;
			}
			count2++;
		}
		// Also organize the agent's memory
		ArrayList<ArrayList<Retailer_w_Price>> structuredMemory=new ArrayList<ArrayList<Retailer_w_Price>>();
		for (int i=0; i<a.getEconomy().numGoods; i++) {
			ArrayList<Retailer_w_Price> retailers_i=new ArrayList<Retailer_w_Price>();
			for (Trader t: a.knows) {
				Retailer r=(Retailer) t;
				if (r.r_product==i) {
					Retailer_w_Price rwp=new Retailer_w_Price(r);
					retailers_i.add(rwp);
				}
			}
			Collections.sort(retailers_i);
			structuredMemory.add(retailers_i);
		}
		ArrayList<Trader> newMemory=new ArrayList<Trader>();
		for (ArrayList<Retailer_w_Price> retailers_i: structuredMemory) {
			for (Retailer_w_Price r: retailers_i) {
				newMemory.add((Trader) r.r);
			}
		}
		a.knows=newMemory;
		if (testing==true) System.out.println("SimpleMemory. End: "+new Date().getTime());
	}
	
	public class Retailer_w_Price implements Comparable<Retailer_w_Price> {
		Retailer r;
		double price;
		
		public Retailer_w_Price(Retailer retailer) {
			r=retailer;
			price=r.salesManager.getPrice();
		}
		
		public int compareTo(Retailer_w_Price other) {
			if (other.price>price) return 1;
			if (other.price<price) return -1;
			return 0;
		}
	}
}
