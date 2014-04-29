package com.android.aft.AFCoreTools;

import java.util.Comparator;

public class StandardLongComparator implements Comparator<MultiSourceCursor.CursorMapping> {

	public static final int DIRECTION_ASC = 0;
	public static final int DIRECTION_DESC = 1;

	private int mComparedIndex;
	private int mDirection;

	public StandardLongComparator(int comparedIndex, int direction) {
		mComparedIndex = comparedIndex;
		mDirection = direction;
	}

	@Override
	public int compare(MultiSourceCursor.CursorMapping c1, MultiSourceCursor.CursorMapping c2) {
		long v = mDirection == DIRECTION_DESC ? c2.getLong(mComparedIndex) - c1.getLong(mComparedIndex) : c1.getLong(mComparedIndex) - c2.getLong(mComparedIndex);
		return v < 0 ? -1 : (v > 0 ? 1 : 0);
	}

}
