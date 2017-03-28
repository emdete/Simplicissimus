package de.emdete.android;

import android.database.Cursor;
import android.content.Context;
import android.provider.ContactsContract.RawContacts;

import java.util.HashMap;
import java.util.Iterator;
import java.lang.UnsupportedOperationException;

public abstract class EasyDb<S extends HashMap<String,Object>> extends HashMap<String,Object> implements Iterator<S>, Iterable<S> {
	static final String TAG = EasyDb.class.getName();

	Context context;
	Cursor cursor;
	String[] columnNames;

	public EasyDb(long contactId) {
		cursor = context.getContentResolver().query(
			RawContacts.CONTENT_URI,
			new String[]{RawContacts._ID,
				RawContacts.CONTACT_ID, RawContacts.AGGREGATION_MODE,
				RawContacts.DELETED, RawContacts.TIMES_CONTACTED,
				RawContacts.LAST_TIME_CONTACTED, RawContacts.STARRED,
				RawContacts.CUSTOM_RINGTONE, RawContacts.SEND_TO_VOICEMAIL,
				RawContacts.ACCOUNT_NAME, RawContacts.ACCOUNT_TYPE,
				RawContacts.DATA_SET, RawContacts.SOURCE_ID,
				RawContacts.VERSION, RawContacts.DIRTY, RawContacts.SYNC1,
				RawContacts.SYNC2, RawContacts.SYNC3, RawContacts.SYNC4, },
			RawContacts.CONTACT_ID + "=?",
			new String[]{String.valueOf(contactId)},
			null);
		cursor.moveToFirst();
		columnNames = cursor.getColumnNames();
	}

	public Iterator<S> iterator() {
		return this;
	}

	public boolean hasNext() {
		return !cursor.isClosed() && !cursor.isAfterLast();
	}

	public S next() {
		S map = getSub();
		for (int i=0;i<columnNames.length;i++) {
			switch (cursor.getType(i)) {
				case Cursor.FIELD_TYPE_BLOB: map.put(columnNames[i], cursor.getBlob(i)); break;
				case Cursor.FIELD_TYPE_FLOAT: map.put(columnNames[i], cursor.getDouble(i)); break;
				case Cursor.FIELD_TYPE_INTEGER: map.put(columnNames[i], cursor.getLong(i)); break;
				case Cursor.FIELD_TYPE_NULL: break;
				case Cursor.FIELD_TYPE_STRING: map.put(columnNames[i], cursor.getString(i)); break;
			}
		}
		if (!cursor.moveToNext()) {
			cursor.close();
			// TODO: what if iterator doesnt run fully?
		}
		return map;
	}

	public void remove() {
		throw new UnsupportedOperationException();
	}

	public abstract S getSub();
}

