package model;

public class SellerHasNoTraderException extends Exception {
	TradeOffer offer;
	public SellerHasNoTraderException(TradeOffer t) {
		offer=t;
	}
}
