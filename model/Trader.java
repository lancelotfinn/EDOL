package model; 
import java.util.*;
import java.io.*;
 
public interface Trader extends Serializable {
	public ArrayList<Transaction> getTransactions();
	public String id();
	public ArrayList<TradeOffer> tradeOffers();
	public TradeOffer getPrice(int sell, int buy);
	public double inventory(int good);
	public void executeSale(double qty,double price,int buy,int sell);
	public boolean defunct();
	public void acceptInvestment(int good,double qty);
}
