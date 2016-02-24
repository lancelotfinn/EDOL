package model;
import java.util.*;

public abstract class Transactions {
	public static double buy(Agent buyer, TradeOffer seller, double qty) throws BuyNegativeQtyException, SellerHasNoTraderException, Exception {
		// qty is qty of the pay good that the buyer offers
		boolean testing=false;
		if (testing==true) System.out.println("Transaction. buyer id="+buyer.id+". pay good="+seller.pay+
				". sell good="+seller.sell+". qty="+qty);
		double epsilon=0.00000000001;
		int transactionID=(int) (Math.random()*10000);
		if (qty<0) throw new BuyNegativeQtyException(seller,qty);
		if ((qty<0)&(qty>-epsilon)) qty=0;
		int payGood=seller.pay;
		int sellGood=seller.sell;
		if (testing==true) {
			double totPayGood=buyer.has(payGood)+seller.trader.inventory(payGood);
			double totSellGood=buyer.has(sellGood)+seller.trader.inventory(sellGood);
			System.out.println("Transaction, ID "+transactionID+". BEFORE TRADE. pay qty="+totPayGood+". sell qty="+totSellGood);
		}
		double maxBuyerPayQty=buyer.has(payGood)*(1-epsilon);
		double maxBuyerCanAfford=maxBuyerPayQty/seller.getPrice();
		double buyersQty=Math.min(qty/seller.getPrice(), maxBuyerCanAfford)/(1+epsilon);
		// The seller's price represents the quantity of the pay good (the good the seller is buying) that will pay for one unit of the sell good
		double sellerStock=seller.limit();
		double bothCanAfford=Math.min(buyersQty, sellerStock);
		double transactQty=Math.max(0,bothCanAfford);
//		if (testing==true) {
//			System.out.println("TRANSACTION. sell good="+seller.sell+". buy with good="+seller.buy+
//					". bothCanAfford="+bothCanAfford+". transactQty="+transactQty);
//			System.out.println("maxBuyerPayQty="+maxBuyerPayQty+". maxBuyerCanAfford="+maxBuyerCanAfford+
//					". sellerStock="+sellerStock);
//		}
		double payQty=transactQty*seller.getPrice();
//		if (buyer.has[payGood]<0) buyer.has[payGood]=0; // Rounding errors can cause minor negative inventory. Restore to zero.
		buyer.gets(payGood, -payQty);			
		buyer.gets(sellGood,transactQty);
/*		try {
		} catch(Exception ex) {
			System.out.println("Problem at Agent.gets(). Here's what I (Transactions) know.");
			System.out.println("buyer="+buyer.id);
			Retailer r=(Retailer)seller.trader;
			System.out.println("seller="+r.id+", trades in "+r.r_product+". payGood="+payGood+". sellGood="+sellGood);
			System.out.println("qty="+qty+". payQty="+payQty);
			ex.printStackTrace();
		}*/
		seller.trader.executeSale(transactQty,seller.getPrice(),payGood,sellGood);
		int[] goods={seller.pay,seller.sell};
		double[] qtys={payQty,transactQty};
		buyer.getEconomy().registerTransaction(goods,qtys);
		if (seller.trader==null) throw new SellerHasNoTraderException(seller);
		buyer.shoppedAt.add(seller.trader); 
		Transaction trans=new Transaction(buyer,seller.trader,payGood,sellGood,payQty,transactQty);
		buyer.getEconomy().transactionRecords.add(trans);
		if (buyer.transactions==null) buyer.transactions=new ArrayList<Transaction>(); // Supply an "accounting book" if it is missing.
		buyer.transactions.add(trans);
		seller.trader.getTransactions().add(trans);
		if (testing==true) {
			double totPayGood=buyer.has(payGood)+seller.trader.inventory(payGood);
			double totSellGood=buyer.has(sellGood)+seller.trader.inventory(sellGood);
			System.out.println("Transaction, ID "+transactionID+". AFTER TRADE. pay qty="+totPayGood+". sell qty="+totSellGood);
		}
		return transactQty;
	}
	public static void borrow(Agent borrower,int good,double qty) {
		boolean testing=false;
		if (testing) {
			System.out.println("TESTING Transactions.borrow()");
			double beforeGood=borrower.has(good)-borrower.owes(good);
			System.out.println("BEFORE BORROW. Agent owns net qty="+beforeGood+" of good "+good);
		}
		try {
			borrower.gets(good,qty);
		} catch(Exception ex) {ex.printStackTrace();}
		try {
			borrower.borrows(good,qty);
		} catch(Exception ex) {
			ex.printStackTrace();
			System.exit(1);
		}
		if (testing) {
			double afterGood=borrower.has(good)-borrower.owes(good);
			System.out.println("AFTER BORROW. Agent owns net qty="+afterGood+" of good "+good);
		}
	}
	public static void repay(Agent borrower,int good,double qty) throws Exception {
		boolean testing=false;
		if (testing) {
			System.out.println("TESTING Transactions.repay()");
			double beforeGood=borrower.has(good)-borrower.owes(good);
			System.out.println("BEFORE REPAY. Agent owns net qty="+beforeGood+" of good "+good);
		}
		if (borrower.has(good)>=qty) {
			try {
				borrower.gets(good,-qty);
			} catch(Exception ex) {
				ex.printStackTrace();
				System.exit(1);
			}
			try {
				borrower.borrows(good,-qty);			
			} catch(Exception ex) {
				ex.printStackTrace();
				System.exit(1);
			}
		} else throw new Exception();
		if (testing) {
			double afterGood=borrower.has(good)-borrower.owes(good);
			System.out.println("AFTER REPAY. Agent owns net qty="+afterGood+" of good "+good);
		}
	}
	public static void invest(Agent transferer,Trader transferee,int good,double qty) {
		boolean testing=false;
		if (testing==true) {
			System.out.println("TESTING Transactions.invest()");
			double totGood=transferer.has(good)+transferee.inventory(good);
			System.out.println("BEFORE INVEST. tot qty="+totGood+" of good "+good);
		}
		qty=Math.max(qty, transferer.has(good));
		try {
			transferer.gets(good,-qty);
		} catch(Exception ex) {ex.printStackTrace();}
		transferee.acceptInvestment(good,qty);
		if (testing==true) {
			double totGood=transferer.has(good)+transferee.inventory(good);
			System.out.println("AFTER INVEST. tot qty="+totGood+" of good "+good);
		}
	}
	public static void dividend(GenericTrader trader,Agent owner,int good,double qty) {
		boolean testing=false;
		if (testing==true) {
			System.out.println("TESTING Transactions.dividend()");
			double totGood=owner.has(good)+trader.inventory(good);
			System.out.println("BEFORE DIVIDEND. tot qty="+totGood+" of good "+good);
		}
		if (owner!=null) {
			try {
				owner.gets(good,qty);
			} catch(Exception ex) {ex.printStackTrace();}			
		}
		trader.getInventory()[good]=trader.getInventory()[good]-qty;
		if (testing==true) {
			double totGood=owner.has(good)+trader.inventory(good);
			System.out.println("AFTER DIVIDEND. tot qty="+totGood+" of good "+good);
		}
	}
}
