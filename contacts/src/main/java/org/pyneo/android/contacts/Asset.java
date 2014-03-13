package org.pyneo.android.contacts;

import android.database.Cursor;
import android.content.Context;

import java.util.HashMap;

public class Asset extends HashMap<String,Object> {
	static final String TAG = Asset.class.getName();

	Context context;

	public Asset(Context context, Cursor cursor, String[] columnNames) {
		this.context = context;
		for (int i=0;i<columnNames.length;i++) {
			switch (cursor.getType(i)) {
				case Cursor.FIELD_TYPE_BLOB: put(columnNames[i], cursor.getBlob(i)); break;
				case Cursor.FIELD_TYPE_FLOAT: put(columnNames[i], cursor.getDouble(i)); break;
				case Cursor.FIELD_TYPE_INTEGER: put(columnNames[i], cursor.getLong(i)); break;
				case Cursor.FIELD_TYPE_NULL: break;
				case Cursor.FIELD_TYPE_STRING: put(columnNames[i], cursor.getString(i)); break;
			}
		}
	}
}
