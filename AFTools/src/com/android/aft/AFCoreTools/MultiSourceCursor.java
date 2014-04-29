/*
 *   Licensed to the Apache Software Foundation (ASF) under one
 *   or more contributor license agreements.  See the NOTICE file
 *   distributed with this work for additional information
 *   regarding copyright ownership.  The ASF licenses this file
 *   to you under the Apache License, Version 2.0 (the
 *   "License"); you may not use this file except in compliance
 *   with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 */

package com.android.aft.AFCoreTools;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;

import android.content.ContentResolver;
import android.database.CharArrayBuffer;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;

public class MultiSourceCursor implements Cursor {

	// List of cursor
	private ArrayList<CursorMapping> mCursors;

	// Current cursor index
	private int mCurrentCursorIdx;

	// Comparator to compare to cursor current row
	// It is used to find the order of the data
	private Comparator<CursorMapping> mComparator;

	// Names of all columns
	private String[] mColumnNames;

	private DebugTools.Logger dbg = new DebugTools.Logger("MultiSourceCursor");
	public static boolean debug = false;

	/**
	 * Ctr
	 *
	 * @param columnNames
	 *          The list of column name for the cursor
	 */
	public MultiSourceCursor(String... columnNames) {
		mCursors = new ArrayList<CursorMapping>();
		mCurrentCursorIdx = -1;

		mColumnNames = columnNames;

		if (!debug)
			dbg.disable();
	}

	/**
	 * Add a new cursor
	 *
	 * @param type
	 *          type of cursor
	 * @param c
	 *          The new cursor
	 * @param columnsMapping
	 *          The column mapping to correspond MultiSourceCursor column to this cursor.
	 *          For example addCursor(c, 1, 6) indicates:
	 *              - the columns 0 of MultiSourceCursor correspond to the columns 1 of c
	 *              - the columns 1 of MultiSourceCursor correspond to the columns 6 of c
	 *
	 * @return true if success
	 */
	public boolean addCursor(int type, Cursor c, int... columnsMapping) {
		mCursors.add(new CursorMapping(type, c, columnsMapping));

		return moveToFirst();
	}

	public boolean addCursor(Cursor c, int... columnsMapping) {
		return addCursor(-1, c, columnsMapping);
	}

	public boolean addCursor(int type, Cursor c, String... columnsMappingName) {
		int[] columnsMapping = new int[columnsMappingName.length];
		int i = 0;
		for (String columns: columnsMappingName)
			columnsMapping[i++] = c.getColumnIndex(columns);

		return addCursor(type, c, columnsMapping);
	}

	public Cursor getCurrentCursor() {
		if (mCurrentCursorIdx == -1)
			return null;

		return mCursors.get(mCurrentCursorIdx).cursor;
	}

	public int getCurrentCursorType() {
		if (mCurrentCursorIdx == -1)
			return -1;

		return mCursors.get(mCurrentCursorIdx).type;
	}

	public ArrayList<Cursor> getCursors() {
		ArrayList<Cursor> cursors = new ArrayList<Cursor>(mCursors.size());

		for (CursorMapping cursorMapping : mCursors) {
			cursors.add(cursorMapping.cursor);
		}

		return cursors;
	}

	public ArrayList<Cursor> getCursors(int cursorType) {
		ArrayList<Cursor> cursors = new ArrayList<Cursor>();

		for (CursorMapping cursorMapping : mCursors) {
			if (cursorMapping.type == cursorType) {
				cursors.add(cursorMapping.cursor);
			}
		}

		return cursors;
	}

	public MultiSourceCursor clone() {
		MultiSourceCursor newCursor = new MultiSourceCursor(mColumnNames);
		newCursor.setComparator(mComparator);
		for (CursorMapping cursor: mCursors)
			newCursor.addCursor(cursor.type , cursor.cursor, cursor.mColumnsOrder);

		return newCursor;
	}

	/**
	 * Set the cursor comparator
	 *
	 * @param comparator
	 */
	public void setComparator(Comparator<CursorMapping> comparator) {
		mComparator = comparator;
	}

	@Override
	public void close() {
		for (CursorMapping cm: mCursors)
			cm.cursor.close();
	}

	@Override
	public void copyStringToBuffer(int columnIndex, CharArrayBuffer buffer) {
		if (mCurrentCursorIdx == -1)
			return ;

		CursorMapping cm = mCursors.get(mCurrentCursorIdx);
		cm.cursor.copyStringToBuffer(cm.getColumnIndex(columnIndex), buffer);
	}

	// Mandatory warning to be conform to Cursor interface for all version
	@SuppressWarnings("deprecation")
    @Override
	public void deactivate() {
		for (CursorMapping cm: mCursors)
			cm.cursor.deactivate();
	}

	@Override
	public byte[] getBlob(int columnIndex) {
		if (mCurrentCursorIdx == -1)
			return null;

		return mCursors.get(mCurrentCursorIdx).getBlob(columnIndex);
	}

	@Override
	public int getColumnCount() {
		if (mCurrentCursorIdx == -1)
			return 0;

		return mColumnNames.length;
	}

	@Override
	public synchronized int getColumnIndex(String columnName) {
		if (mCurrentCursorIdx == -1)
			return 0;

		for (int idx = 0; idx < mColumnNames.length; ++idx)
			if (mColumnNames[idx].equals(columnName))
				return idx;

		return -1;
	}

	@Override
	public int getColumnIndexOrThrow(String columnName) throws IllegalArgumentException {
		if (mCurrentCursorIdx == -1)
			return 0;

		return getColumnIndex(columnName);
	}

	@Override
	public String getColumnName(int columnIndex) {
		if (mCurrentCursorIdx == -1)
			return null;

		if (columnIndex < 0 || columnIndex >= mColumnNames.length)
			return null;

		return mColumnNames[columnIndex];
	}

	@Override
	public String[] getColumnNames() {
		if (mCurrentCursorIdx == -1)
			return null;

		return mColumnNames;
	}

	@Override
	public int getCount() {
		int count = 0;

		for (CursorMapping cm: mCursors)
			count += cm.cursor.getCount();

		return count;
	}

	@Override
	public double getDouble(int columnIndex) {
		if (mCurrentCursorIdx == -1)
			return 0;

		return mCursors.get(mCurrentCursorIdx).getDouble(columnIndex);
	}

	@Override
	public Bundle getExtras() {
		if (mCurrentCursorIdx == -1)
			return null;

		return mCursors.get(mCurrentCursorIdx).cursor.getExtras();
	}

	@Override
	public float getFloat(int columnIndex) {
		if (mCurrentCursorIdx == -1)
			return 0;

		return mCursors.get(mCurrentCursorIdx).getFloat(columnIndex);
	}

	@Override
	public int getInt(int columnIndex) {
		if (mCurrentCursorIdx == -1)
			return 0;

		return mCursors.get(mCurrentCursorIdx).getInt(columnIndex);
	}

	@Override
	public long getLong(int columnIndex) {
		if (mCurrentCursorIdx == -1)
			return 0;

		return mCursors.get(mCurrentCursorIdx).getLong(columnIndex);
	}

	@Override
	public short getShort(int columnIndex) {
		if (mCurrentCursorIdx == -1)
			return 0;

		return mCursors.get(mCurrentCursorIdx).getShort(columnIndex);
	}

	@Override
	public String getString(int columnIndex) {
		if (mCurrentCursorIdx == -1)
			return null;

		return mCursors.get(mCurrentCursorIdx).getString(columnIndex);
	}

	public int getType(int columnIndex) {
		if (mCurrentCursorIdx == -1)
			return 0;

		// The function getType exist from sdk version 11
		if (android.os.Build.VERSION.SDK_INT < 11) {
			dbg.e("Cursor::getType() no such method on sdk version: " + android.os.Build.VERSION.SDK_INT);
			return 0;
		}

		// Try to call the getType method with reflection.
		CursorMapping cm = mCursors.get(mCurrentCursorIdx);
		try {
			Method getType = cm.cursor.getClass().getMethod("getType", Integer.TYPE);
			return (Integer)getType.invoke(cm.cursor, Integer.valueOf(cm.getColumnIndex(columnIndex)));
		} catch (Exception e) {
			dbg.e("Cannot invoke Cursor::getType()", e);
		}

		return 0;
	}

	@Override
	public boolean getWantsAllOnMoveCalls() {
		if (mCurrentCursorIdx == -1)
			return false;

		return mCursors.get(mCurrentCursorIdx).cursor.getWantsAllOnMoveCalls();
	}

	@Override
	public boolean isBeforeFirst() {
		return mCurrentCursorIdx == -1;
	}

	@Override
	public boolean isAfterLast() {
		for (CursorMapping cm: mCursors)
			if (!cm.cursor.isAfterLast())
				return false;
		return true;
	}

	@Override
	public boolean isClosed() {
		for (CursorMapping cm: mCursors)
			if (!cm.cursor.isClosed())
				return false;
		return true;
	}

	@Override
	public boolean isFirst() {
		for (CursorMapping cm: mCursors)
			if (!cm.cursor.isFirst())
				return false;
		return true;
	}

	@Override
	public boolean isLast() {
		// To be in last need:
			// _ one cursor in last position
		// _ all other cursor after last position
		boolean findCursorInLast = false;
		for (CursorMapping cm: mCursors) {
			if (cm.cursor.isLast()) {
				if (findCursorInLast) {
					// There is already a cursor on last element
					return false;
				}
				findCursorInLast = true;
			}
			else if (cm.cursor.isAfterLast())
				;
			else
				return false;
		}

		return true;
	}

	@Override
	public boolean isNull(int columnIndex) {
		if (mCurrentCursorIdx == -1)
			return false;

		return mCursors.get(mCurrentCursorIdx).isNull(columnIndex);
	}

	/**
	 * Dump the current MultiSourceCursor status
	 */
	private void dump() {
		if (!debug)
			return ;

		dbg.d("AggregateCursor status:");
		dbg.d("  - Current position: " + getPosition());
		dbg.d("  - Current cursor: " + mCurrentCursorIdx);
		for (CursorMapping cm: mCursors) {
			dbg.d("  - Cursor(length: " + cm.cursor.getCount() + ", position: " +  cm.cursor.getPosition() + ")");
		}
	}

	@Override
	public synchronized boolean moveToFirst() {
		if (debug)
			dbg.d("------->");

		boolean status = _moveToFirst();

		if (debug) {
			dump();
			dbg.d("<-------");
		}

		return status;
	}

	@Override
	public synchronized boolean moveToLast() {
		if (debug)
			dbg.d("------->");

		boolean status = _moveToLast();

		if (debug) {
			dump();
			dbg.d("<-------");
		}

		return status;
	}

	@Override
	public synchronized boolean moveToNext() {
		if (debug)
			dbg.d("------->");

		boolean status = _moveToNext();

		if (debug) {
			dump();
			dbg.d("<-------");
		}

		return status;
	}

	@Override
	public synchronized boolean moveToPrevious() {
		if (debug)
			dbg.d("------->");

		boolean status = _moveToPrevious();

		if (debug) {
			dump();
			dbg.d("<-------");
		}

		return status;
	}

	@Override
	public synchronized boolean move(int offset) {
		if (debug)
			dbg.d("------->");

		boolean status = _move(offset);

		if (debug) {
			dump();
			dbg.d("<-------");
		}

		return status;
	}

	@Override
	public synchronized boolean moveToPosition(int position) {
		if (debug)
			dbg.d("------->");

		boolean status = _moveToPosition(position);

		if (debug) {
			dump();
			dbg.d("<-------");
		}

		return status;
	}

	@Override
	public void registerContentObserver(ContentObserver observer) {
		for (CursorMapping cm: mCursors)
			cm.cursor.registerContentObserver(observer);
	}

	@Override
	public void registerDataSetObserver(DataSetObserver observer) {
		for (CursorMapping cm: mCursors)
			cm.cursor.registerDataSetObserver(observer);
	}

	@Override
	@Deprecated
	public boolean requery() {
		for (CursorMapping cm: mCursors)
			cm.cursor.requery();

		return true;
	}

	@Override
	public Bundle respond(Bundle extras) {
		if (mCurrentCursorIdx == -1)
			return null;

		return mCursors.get(mCurrentCursorIdx).cursor.respond(extras);
	}

	@Override
	public void setNotificationUri(ContentResolver cr, Uri uri) {
		for (CursorMapping cm: mCursors)
			cm.cursor.setNotificationUri(cr, uri);
	}

	@Override
	public void unregisterContentObserver(ContentObserver observer) {
		for (CursorMapping cm: mCursors)
			cm.cursor.unregisterContentObserver(observer);
	}

	@Override
	public void unregisterDataSetObserver(DataSetObserver observer) {
		for (CursorMapping cm: mCursors)
			cm.cursor.unregisterDataSetObserver(observer);
	}

	@Override
	public int getPosition() {
		if (mCurrentCursorIdx == -1)
			return -1;

		int num_element = 0;
		for (CursorMapping cm: mCursors)
			num_element += cm.cursor.getPosition();

		if (mCurrentCursorIdx != -1)
			++num_element;

		return num_element - 1;
	}

	private boolean _moveToPosition(int position) {
		if (debug)
			dbg.d("moveToPosition " + position);

		int currentPosition = getPosition();
		if (currentPosition == position)
			return true;
		else if (position == 0)
			return _moveToFirst();
		else
			return _move(position - currentPosition);
	}

	private boolean _move(int offset) {
		if (debug)
			dbg.d("move " + offset);

		if (offset < 0) {
			for (; offset != 0; ++offset)
				if (!_moveToPrevious())
					return false;
			return true;
		}

		if (offset > 0) {
			for (; offset != 0; --offset)
				if (!_moveToNext())
					return false;

			return true;
		}

		return true;
	}

	private boolean _moveToFirst() {
		if (debug)
			dbg.d("moveToFirst");

		for (CursorMapping cm: mCursors)
			if (!cm.cursor.moveToFirst()) {
				if (cm.cursor.getCount() != 0)
					return false;
			}

		int idx = findCursorIdxOnMinValue();
		if (idx == -1)
			return false;

		mCurrentCursorIdx = idx;

		return true;
	}

	private boolean _moveToLast() {
		if (debug)
			dbg.d("moveToLast");

		for (CursorMapping cm: mCursors) {
			if (!cm.cursor.moveToPosition(cm.cursor.getCount()))
				return false;
		}

		_moveToPrevious();

		return true;
	}

	private boolean _moveToNext() {
		if (debug)
			dbg.d("moveToNext");

		if (mCurrentCursorIdx != -1) {
			Cursor cur = mCursors.get(mCurrentCursorIdx).cursor;

			if (cur.getPosition() == cur.getCount())
				// Nothing to do
				;
			else if (cur.getPosition() == cur.getCount() - 1)
				cur.moveToNext();
			else if (!cur.moveToNext())
				return false;
		}

		int idx = findCursorIdxOnMinValue();
		if (idx == -1)
			return false;

		mCurrentCursorIdx = idx;

		return true;
	}

	private boolean _moveToPrevious() {
		if (debug)
			dbg.d("moveToPrevious");

		if (mCurrentCursorIdx == -1)
			return false;

		// Find the new element by iterate on all cursor to find the max value of all the cursor top
		CursorMapping new_cur = null;
		int new_cur_idx = 0;
		for (int idx = 0; idx < mCursors.size(); ++idx) {
			CursorMapping tmp = mCursors.get(idx);

			if (tmp.cursor.getPosition() <= 0)
				continue;

			tmp.cursor.moveToPrevious();

			if (new_cur == null) {
				new_cur_idx = idx;
				new_cur = tmp;
				continue ;
			}

			if (mComparator.compare(tmp, new_cur) > 0) {
				new_cur.cursor.moveToNext();
				new_cur_idx = idx;
				new_cur = tmp;
			}
			else
				tmp.cursor.moveToNext();
		}

		if (new_cur == null) {
			if (debug)
				dbg.d("Cannot find previous");
			return false;
		}

		mCurrentCursorIdx = new_cur_idx;

		return true;
	}

	private int findCursorIdxOnMinValue() {
		int min_idx = -1;
		for (int cur_idx = 0; cur_idx < mCursors.size(); ++cur_idx) {
			CursorMapping cur = mCursors.get(cur_idx);
			if (cur.cursor.isAfterLast() || cur.cursor.isBeforeFirst() || cur.cursor.isClosed())
				continue ;

			if (min_idx == -1)
				min_idx = cur_idx;
			else if (min_idx != cur_idx)
				if (mComparator.compare(cur, mCursors.get(min_idx)) < 0)
					min_idx = cur_idx;
		}

		if (min_idx == -1) {
			if (debug)
				dbg.d("Failed to find min");
			return -1;
		}

		if (debug)
			DebugTools.d("Current cursor: " + mCurrentCursorIdx + " -> " + min_idx);

		return min_idx;
	}

	public class CursorMapping {

		// The cursor
		public Cursor cursor;

		// Type of cursor
		public int type;

		// Columns order mapping
		public int[] mColumnsOrder;

		public CursorMapping(int type, Cursor c, int[] columnsOrder) {
			this.type = type;
			cursor = c;
			mColumnsOrder = columnsOrder;
		}

		public int getColumnIndex(int index) {
			if (index < 0 || index >= mColumnsOrder.length)
				return -1;

			return mColumnsOrder[index];
		}

		public double getDouble(int columnIndex) {
			if (cursor.getPosition() == -1)
				return 0;

			int idx = getColumnIndex(columnIndex);
			if (idx == -1)
				return 0;

			return cursor.getDouble(idx);
		}

		public float getFloat(int columnIndex) {
			if (cursor.getPosition() == -1)
				return 0;

			int idx = getColumnIndex(columnIndex);
			if (idx == -1)
				return 0;

			return cursor.getFloat(idx);
		}

		public int getInt(int columnIndex) {
			if (cursor.getPosition() == -1)
				return 0;

			int idx = getColumnIndex(columnIndex);
			if (idx == -1)
				return 0;

			return cursor.getInt(idx);
		}

		public long getLong(int columnIndex) {
			if (cursor.getPosition() == -1)
				return 0;

			int idx = getColumnIndex(columnIndex);
			if (idx == -1)
				return 0;

			return cursor.getLong(idx);
		}

		public short getShort(int columnIndex) {
			if (cursor.getPosition() == -1)
				return 0;

			int idx = getColumnIndex(columnIndex);
			if (idx == -1)
				return 0;

			return cursor.getShort(idx);
		}

		public String getString(int columnIndex) {
			if (cursor.getPosition() == -1)
				return null;

			int idx = getColumnIndex(columnIndex);
			if (idx == -1)
				return null;

			return cursor.getString(idx);
		}

		public byte[] getBlob(int columnIndex) {
			if (cursor.getPosition() == -1)
				return null;

			int idx = getColumnIndex(columnIndex);
			if (idx == -1)
				return null;

			return cursor.getBlob(idx);
		}

		public boolean isNull(int columnIndex) {
			if (cursor.getPosition() == -1)
				return false;

			int idx = getColumnIndex(columnIndex);
			if (idx == -1)
				return false;

			return cursor.isNull(idx);
		}

	}

}
