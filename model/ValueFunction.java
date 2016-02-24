package model;
import java.util.*;

public interface ValueFunction {
	public ScoredSet utility(MarketParameters m,ArrayList<Integer> fixedSet);
	public String type();
}
