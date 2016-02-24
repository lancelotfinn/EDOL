package model;

import java.io.Serializable;

public class TradeOffer implements Comparable<TradeOffer>, Serializable {
	public Trader trader;
	public int sell;
	public int pay;
	private double price;
	public TradeOffer(Trader t, int s, int b, double p) {
		sell=s;
		pay=b;
		price=p;
		trader=t;
	}
	
	public double limit() {
		double ret=-99;
		if (trader!=null) {
			ret=trader.inventory(sell);
			if (((ret>0)|(ret<0)|(ret==0))==false) {
				System.out.println("problem at TradeOffer.limit(). ret="+ret);
				new Exception().printStackTrace();
				System.exit(1);
			}
		}
		return ret;
	}
	
	public int compareTo(TradeOffer other) {
		if (price<other.getPrice()) return -1;
		return 1;
	}
	
	public String toString() {
		String ret="sell="+sell+". pay="+pay+". price="+price+". limit="+limit();
		return ret;
	}
	
	public double getPrice() {
		return price;
	}
	public void setPrice(double p) {
		price=p;
	}
}
