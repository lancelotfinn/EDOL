package model;
import java.io.*; 

public abstract class ProductionFunction implements Serializable {
	public LaborReport produce(Agent a) {return null;}
	public int[] makeables() {return null;}
	public Integer numGoods() {return null;}
	public String type() {
		boolean testing=false;
		if (testing==true) System.out.println("TESTING ProductionFunction.type()");
		return "GENERIC";
	}
}
