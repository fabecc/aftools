package com.android.aft.AFCoreTools;

import java.text.Collator;
import java.text.ParseException;
import java.text.RuleBasedCollator;
import java.util.Comparator;

public class StandardContactNameComparator implements Comparator<MultiSourceCursor.CursorMapping> {

	public static Collator sContactCollator;

	static {
		try {
		sContactCollator = new RuleBasedCollator("< '#' < '_' < '+' < '-' < '.' < '\"' < '[' < ']'"
					+ " < 0 < 1 < 2 < 3 < 4 < 5 < 6 < 7 < 8 < 9"
					+ " < a,A < b,B < c,C < d,D < e,E < f,F < g,G < h,H < i,I < j,J"
					+ " < k,K < l,L < m,M < n,N < o,O < p,P < q,Q < r,R < s,S < t,T"
					+ " < u,U < v,V < w,W < x,X < y,Y < z,Z");
			
		sContactCollator.setStrength(java.text.Collator.TERTIARY);	

		}
		catch (ParseException e) {
			e.printStackTrace();
		}
	}

	@Override
	public int compare(MultiSourceCursor.CursorMapping c1, MultiSourceCursor.CursorMapping c2) {
		String c1_str = c1.getString(1);
		String c2_str = c2.getString(1);

		if (c1_str == null) {
			if (c2_str.equalsIgnoreCase("#"))
				return 1;
			return -1;
		}

		if (c2_str == null) {
			if (c1_str.equalsIgnoreCase("#"))
				return -1;
			return 1;
		}

		return sContactCollator != null ? sContactCollator.compare(c1_str, c2_str) : 0;
	}
	
}
