package model;

public class BuyNegativeQtyException extends Exception {
	TradeOffer offer;
	double qty;
	public BuyNegativeQtyException(TradeOffer t, double q) {
		offer=t;
		qty=q;
	}
}
