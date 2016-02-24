package model;
import java.io.*;

public class Transaction implements Serializable {
	public int pay;
	public int sell;
	public double payQty;
	public double sellQty;
	public int buyerID;
	public double payQtyLeft; // Quantity of the "pay good" left with the retailer after the transaction
	public double sellQtyLeft; // ... and of the "sell good"
	public int turn;
	
	public Transaction(Agent buyer, Trader seller, int p, int s, double pq, double sq) {
		buyerID=buyer.id;
		pay=p;
		sell=s;
		payQty=pq;
		sellQty=sq;
		payQtyLeft=seller.inventory(pay);
		sellQtyLeft=seller.inventory(sell);
		turn=buyer.getEconomy().turn;
	}
}
