package de.emdete.android.contacts;

import android.database.Cursor;
import android.content.Context;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.CommonDataKinds.Nickname;
import android.provider.ContactsContract.CommonDataKinds.Im;
import android.provider.ContactsContract.CommonDataKinds.Photo;

import java.util.Iterator;
import java.lang.UnsupportedOperationException;

public class Assets implements Iterator<Asset>, Iterable<Asset> {
	static final String TAG = Assets.class.getName();

	Context context;
	Cursor cursor;
	String[] columnNames;

	public Assets(Context context, long contactId) {
		this.context = context;
		cursor = context.getContentResolver().query(
			Data.CONTENT_URI,
			new String[]{Data._ID,
				Data.RAW_CONTACT_ID, Data.MIMETYPE, Data.IS_PRIMARY,
				Data.IS_SUPER_PRIMARY, Data.DATA_VERSION, Data.DATA1,
				Data.DATA2, Data.DATA3, Data.DATA4, Data.DATA5, Data.DATA6,
				Data.DATA7, Data.DATA8, Data.DATA9, Data.DATA10, Data.DATA11,
				Data.DATA12, Data.DATA13, Data.DATA14, Data.DATA15, Data.SYNC1,
				Data.SYNC2, Data.SYNC3, Data.SYNC4, },
			Data.RAW_CONTACT_ID + "=? AND " + Data.MIMETYPE + " IN ( ?, ?, ?, ?, ?, ? )",
			new String[]{
				String.valueOf(contactId),
				Nickname.CONTENT_ITEM_TYPE,
				Im.CONTENT_ITEM_TYPE,
				Photo.CONTENT_ITEM_TYPE,
				},
			null);
		//cursor.moveToFirst();
		columnNames = cursor.getColumnNames();
	}

	public Iterator<Asset> iterator() {
		return this;
	}

	public boolean hasNext() {
		return !cursor.isClosed() && cursor.getCount() > 0 && !cursor.isLast();
	}

	public Asset next() {
		Asset c = null;
		if (cursor.moveToNext()) {
			c = new Asset(context, cursor, columnNames);
		}
		if (cursor.isLast()) {
			cursor.close(); // TODO: what if iterator doesnt run fully?
		}
		return c;
	}

	public void remove() {
		throw new UnsupportedOperationException();
	}
}

