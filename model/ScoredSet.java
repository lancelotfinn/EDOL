package model;
import java.io.Serializable;
import java.util.*;

public class ScoredSet implements Serializable {
	public ArrayList<Integer> set;
	public double score;
	
	public String toString() {
		String ret="Set: ";
		for (Integer i: set) {
			ret=ret+i+", ";
		}
		ret=ret+"\nScore: "+score;
		return ret;
	}
	
	public boolean isEquivalentTo(ScoredSet s) {
		boolean testing=false;
		if (testing) System.out.println("TESTING isEquivalentTo() with comparator:\n    "+s.toString()
				+"\nand defender: \n    "+toString());
		if (isVersionOf(s.set)==false) {
			return false;
		}
//		if (score!=s.score) {
//			return false;
//		}
		return true;
	}
	
	public boolean isVersionOf(ArrayList<Integer> comparator) {
		boolean testing=false;
		int comp_size=comparator.size();
		if (testing) System.out.println("TESTING ScoredSet.isVersionOf() with comparator = "+comparator.toString()+
				". size="+comp_size);
		if (comp_size==0) {
			if (set.size()==0) return true;
			else return false;
		}
		if (set.size()!=comparator.size()) return false;
		Collections.sort(set);
		Collections.sort(comparator);
		if (testing) System.out.println("TESTING ScoredSet.isVersionOf(). Line 29");
		for (int i=0; i<set.size(); i++) {
			Integer set1_item=set.get(i);
			Integer set2_item=comparator.get(i);
			if (set1_item!=set2_item) return false;
		}
		if (testing) System.out.println("TESTING ScoredSet.isVersionOf(). Line 35");
		return true;
	}
	
	public ScoredSet clone() {
		ScoredSet ret=new ScoredSet();
		ret.set=(ArrayList<Integer>) set.clone();
		ret.score=score;
		return ret;
	}
}
