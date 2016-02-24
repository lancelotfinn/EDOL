package model;

public class DimensionalityException extends Exception {
	public String description;
	public String toString() {
		String ret="Dimensionality exception. "+description;
		return ret;
	}
}
