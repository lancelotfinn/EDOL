package model;
import java.util.*;

public interface PreferenceRelation {
	public boolean preferredTo(MarketParameters m,ArrayList<Integer> fixedSet,int i,int j);
	public String type();
}
