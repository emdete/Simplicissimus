package de.emdete.android.contacts;

import android.database.Cursor;
import android.content.Context;
import android.provider.ContactsContract.RawContacts;

import java.util.Iterator;
import java.lang.UnsupportedOperationException;

public class Parcels implements Iterator<Parcel>, Iterable<Parcel> {
	static final String TAG = Parcels.class.getName();

	Context context;
	Cursor cursor;
	String[] columnNames;

	public Parcels(Context context, long contactId) {
		this.context = context;
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
		//cursor.moveToFirst();
		columnNames = cursor.getColumnNames();
	}

	public Iterator<Parcel> iterator() {
		return this;
	}

	public boolean hasNext() {
		return !cursor.isClosed() && !cursor.isLast();
	}

	public Parcel next() {
		Parcel parcel = null;
		if (cursor.moveToNext()) {
			parcel = new Parcel(context, cursor, columnNames);
		}
		if (cursor.isLast()) {
			cursor.close(); // TODO: what if iterator doesnt run fully?
		}
		return parcel;
	}

	public void remove() {
		throw new UnsupportedOperationException();
	}
}

