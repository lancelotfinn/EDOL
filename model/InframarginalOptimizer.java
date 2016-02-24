package model;
import java.util.*;
import java.text.*;

public class InframarginalOptimizer {
	public static ScoredSet optimalSet(ArrayList<Integer> universe,MarketParameters m,ArrayList<Integer> fixedSet,
			ValueFunction v,PreferenceRelation p) {
		boolean testing=false;
		boolean testing2=false;
		int methodID=(int) (Math.random()*90000+10000);
		if (testing) {
			System.out.println("TESTING InframarginalOptimizer.optimalSet(). methodID="+methodID);
			System.out.println("   Value function: "+v.type());
			System.out.println("   Preference relation: "+p.type());
			System.out.print("   Universe: ");
			for (Integer i: universe) {
				System.out.print(i+", ");
			}
			System.out.print("\n   Fixed set: ");
			for (Integer i: fixedSet) {
				System.out.print(i+", ");
			}
			System.out.println();
			if (Math.random()<0.05) System.out.println("\n\n"+m.toString());
		}
		int n=universe.size();
		boolean[][] prefGrid=new boolean[n][n];
		for (int i=0; i<n; i++) {
			for (int j=0; j<n; j++) {
				int good_i=universe.get(i);
				int good_j=universe.get(j);
				prefGrid[i][j]=p.preferredTo(m,fixedSet,good_i,good_j);
			}
		}
		if (testing) {
			System.out.println("TESTING InfOpt. methodID="+methodID+". \nPREFERENCE GRID");
			System.out.print("    ");
			for (int i=0; i<n; i++) {
				System.out.print(universe.get(i)+"   ");
			}
			System.out.println();
			for (int i=0; i<n; i++) {
				System.out.print(universe.get(i)+"   ");
				for (int j=0; j<n; j++) {
					if (prefGrid[i][j]==true) System.out.print("T");
					else System.out.print("F");
					System.out.print("   ");
				}
				System.out.println();
			}
		}
		ArrayList<ScoredSet> scoredSets=new ArrayList<ScoredSet>();
		ArrayList<Integer> set=new ArrayList<Integer>();
		if (testing) System.out.println("ABOUT TO LAUNCH VALUE FUNCTION "+v.type()+"... from InframarginalOptimizer.optimize()");
		ScoredSet bestU=v.utility(m, union(set, fixedSet));
		if (testing) System.out.println("JUST FINISHED WITH VALUE FUNCTION "+v.type()+"... from InframarginalOptimizer.optimize()");
		scoredSets.add(bestU.clone());
		boolean optimal=false;
		int countIter=0;
		while (optimal==false) {
			countIter++;
			if (testing) {
				System.out.println("TESTING InfOpt. methodID="+methodID+". \nInframarginalOptimizer ITERATION "+countIter);
				System.out.println("Defending champion: \n"+bestU.toString());
			}
			ArrayList<Integer> innermost_outset=candidatesForInclusion(universe,set,prefGrid);
			ArrayList<Integer> outermost_inset=candidatesForExclusion(universe,set,prefGrid);
			ArrayList<Integer> core=(ArrayList<Integer>) set.clone();
			for (Integer i: outermost_inset) {
				core.remove(i);
			}
			if (testing) {
				System.out.println("TESTING InfOpt. methodID="+methodID+"\nCORE: "+printInts(core));
				System.out.println("INNERMOST OUTSET: "+printInts(innermost_outset));
				System.out.println("OUTERMOST INSET: "+printInts(outermost_inset));
			}
			ArrayList<ArrayList<Integer>> combinations=combinations(union(innermost_outset,outermost_inset));
			ArrayList<Integer> next_set=(ArrayList<Integer>) set.clone();
			ScoredSet next_bestU=(ScoredSet) bestU.clone();
			if (testing2) System.out.println("COMBINATIONS");
			for (ArrayList<Integer> combo: combinations) {
				ArrayList<Integer> step1=union(combo,core);
				ArrayList<Integer> step2=union(step1,fixedSet);
				ScoredSet scoredSet=lookupScore(scoredSets,step2);
				if (scoredSet==null) {
//					if (testing) System.out.println("ABOUT TO CALL THE utility() METHOD IN THE VALUE FUNCTION");
					scoredSet=v.utility(m, step2);
//					if (testing) System.out.println("JUST FINISHED THE utility() METHOD IN THE VALUE FUNCTION");
					scoredSets.add(scoredSet);
				}
				if (testing2) System.out.println("   "+scoredSet.toString());
				if (scoredSet.score>next_bestU.score) {
					next_set=step1; // The "next set" should not include the fixed set.
					next_bestU=scoredSet;
				}
			}
			if (testing) {
				System.out.println("TESTING InfOpt. methodID="+methodID);
				System.out.println("At the end of round "+countIter+" of the iteration, we must compare...");
				System.out.println("NEW BEST SET: \n"+next_bestU.toString());
				System.out.println("OLD BEST SET: \n"+bestU.toString());
				System.out.println("SETS ARE EQUIVALENT: "+next_bestU.isEquivalentTo(bestU));
			}
			if (next_bestU.isEquivalentTo(bestU)) optimal=true;
			bestU=next_bestU;
			set=next_set;
		}
/* I think these are useless; if no errors, then delete.
		ScoredSet ret=new ScoredSet();
		ret.set=set;
		ret.score=bestU.score;
*/
		if (testing) System.out.println("FINISHED InframarginalOptimizer.optimalSet()");
		return bestU;
	}
	
	public static ArrayList<Integer> candidatesForInclusion(ArrayList<Integer> universe,
			ArrayList<Integer> inSet,boolean[][] prefGrid) {
		boolean testing=false;
		if (testing==true) System.out.println("TESTING InframarginalOptimizer.candidatesForInclusion()");
		ArrayList<Integer> candidates=new ArrayList<Integer>();
		ArrayList<Integer> outSetGridPositions=new ArrayList<Integer>();
		// Confusingly but unavoidably, the integers that comprise the universe are not the same as the 
		// *positions* of these integers in the preference grid. The universe may consist of 1, 4, 5, and 7.
		// The positions in the grid would be 1, 2, 3, 4. The "out set grid positions" are the row/column 
		// headings of goods that happen to be in the outset, not the IDs of the goods themselves.
		for (int i=0; i<universe.size(); i++) {
			Integer good=universe.get(i);
			if (inSet.contains(good)==false) outSetGridPositions.add(i);
		}
		if (testing) {
			System.out.println("PREFERENCE GRID");
			System.out.print("    ");
			for (Integer i: outSetGridPositions) {
				System.out.print(universe.get(i)+"   ");
			}
			System.out.println();
			for (Integer i: outSetGridPositions) {
				System.out.print(universe.get(i)+"   ");
				for (Integer j: outSetGridPositions) {
					if (prefGrid[i][j]==true) System.out.print("T");
					else System.out.print("F");
					System.out.print("   ");
				}
				System.out.println();
			}
		}
		for (Integer j: outSetGridPositions) {
			boolean jIsCandidate=true;
			Y: for (Integer i: outSetGridPositions) {
				if (prefGrid[i][j]==true) {
					jIsCandidate=false;
					break Y;
				}
			}
			if (jIsCandidate==true) candidates.add(universe.get(j));
		}
		if (testing) {
			System.out.print("Candidates: ");
			for (Integer i: candidates) {
				System.out.print(i+", ");
			}
			System.out.println();
		}
		return candidates;
	}
	
	public static ArrayList<Integer> union(ArrayList<Integer> set1, ArrayList<Integer> set2) {
		boolean testing=false;
		if (testing) System.out.println("TESTING InframarginalOptimizer.union()");
		if (set1==null) return set2;
		if (set2==null) return set1;
		ArrayList<Integer> ret=new ArrayList<Integer>();
		for (Integer i: set1) {
			ret.add(i);
		}
		for (Integer i: set2) {
			ret.add(i);
		}
		return ret;
	}

	public static ArrayList<Integer> candidatesForExclusion(ArrayList<Integer> universe,
			ArrayList<Integer> inSet,boolean[][] prefGrid) {
		ArrayList<Integer> candidates=new ArrayList<Integer>();
		ArrayList<Integer> inSetGridPositions=new ArrayList<Integer>();
		for (int i=0; i<universe.size(); i++) {
			Integer good=universe.get(i);
			if (inSet.contains(good)==true) inSetGridPositions.add(i);
		}
		for (Integer j: inSetGridPositions) {
			boolean jIsCandidate=true;
			Y: for (Integer i: inSetGridPositions) {
				if (prefGrid[j][i]==true) {
					jIsCandidate=false;
					break Y;
				}
			}
			if (jIsCandidate==true) candidates.add(universe.get(j));
		}
		return candidates;
	}
	
	public static ArrayList<ArrayList<Integer>> combinations(ArrayList<Integer> source) {
		boolean testing=false;
		int n=source.size();
		int numCombos=(int) (Math.pow(2,n));
		boolean[][] code=new boolean[numCombos][n];
		for (int i=0; i<numCombos; i++) {
			for (int j=0; j<n; j++) {
				int position=n-j-1;
				int placeValue=(int) (Math.pow(2,j));
				int frac=(int) (i/placeValue);
				int digit=frac%2;
				if (digit==1) code[i][position]=false;
				if (digit==0) code[i][position]=true;
				if (testing) System.out.println("combo id "+i+", term "+j+": "+code[i][position]);
			}
		}
		ArrayList<ArrayList<Integer>> ret=new ArrayList<ArrayList<Integer>>();
		for (int i=0; i<numCombos; i++) {
			ArrayList<Integer> combo=new ArrayList<Integer>();
			ret.add(combo);
			for (int j=0; j<n; j++) {
				if (code[i][j]) combo.add(source.get(j));
			}
		}
		return ret;
	}
	
	public static ScoredSet scoredSet_max(ArrayList<ScoredSet> set) {
		if (set.size()==0) return null;
		ScoredSet ret=null;
		double bestScore=set.get(0).score;
		for (ScoredSet s: set) {
			if (s.score>bestScore) {
				ret=s;
				bestScore=s.score;
			}
		}
		return ret;
	}
	
	public static String printInts(ArrayList<Integer> data) {
		String ret="";
		for (Integer i: data) {
			ret=ret+i+", ";
		}
		return ret;
	}
	
	public static ScoredSet lookupScore(ArrayList<ScoredSet> sets,ArrayList<Integer> set) {
		boolean testing=false;
		if (testing) System.out.println("TESTING InframarginalOptimizer.lookupScore(). look up set: "+set.toString());
		for (ScoredSet s: sets) {
			if (testing) System.out.println("LOOKING AT SET "+s.toString());
			if (s.isVersionOf(set)) return s;
		}
		return null;
	}
}
