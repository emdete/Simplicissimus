package org.pyneo.android.contacts;

import android.util.Log;
import android.database.Cursor;
import android.content.Context;
import android.provider.ContactsContract.Contacts;

import java.util.HashMap;

public class Person extends HashMap<String,Object> {
	static final String TAG = Person.class.getName();

	Context context;

	public Person(Context context, Cursor cursor, String[] columnNames) {
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

	public Parcels getParcels() {
		return new Parcels(context, (Long)get(Contacts._ID));
	}

	public static String test(Context context) {
		int a = 0;
		int b = 0;
		int c = 0;
		for (Person person : new Persons(context)) {
			a++;
			// Log.d(TAG, "person=" + person);
			for (Parcel parcel : person.getParcels()) {
				b++;
				// Log.d(TAG, "parcel=" + parcel);
				for (Asset asset : parcel.getDs()) {
					c++;
					Log.d(TAG, "asset=" + asset);
				}
			}
		}
		return "read " + a + '/' + b + '/' + c;
	}
}
