package de.emdete.android.contacts;

import android.database.Cursor;
import android.content.Context;
import android.provider.ContactsContract.Contacts;
import android.util.Log;

import java.util.Iterator;
import java.lang.UnsupportedOperationException;

class Persons implements Iterator<Person>, Iterable<Person> {
	static final String TAG = Persons.class.getName();

	Context context;
	Cursor cursor;
	String[] columnNames;

	public Persons(Context context) {
		this.context = context;
		cursor = context.getContentResolver().query(
			Contacts.CONTENT_URI,
			new String[]{Contacts._ID,
				//Contacts.NAME_RAW_CONTACT_ID,
				Contacts.LOOKUP_KEY, Contacts.DISPLAY_NAME_PRIMARY,
				Contacts.PHOTO_ID, Contacts.PHOTO_URI,
				Contacts.PHOTO_THUMBNAIL_URI, Contacts.IN_VISIBLE_GROUP,
				Contacts.HAS_PHONE_NUMBER, Contacts.TIMES_CONTACTED,
				Contacts.LAST_TIME_CONTACTED, Contacts.STARRED,
				Contacts.CUSTOM_RINGTONE, Contacts.SEND_TO_VOICEMAIL,
				Contacts.CONTACT_PRESENCE, Contacts.CONTACT_STATUS,
				Contacts.CONTACT_STATUS_TIMESTAMP,
				Contacts.CONTACT_STATUS_RES_PACKAGE,
				Contacts.CONTACT_STATUS_LABEL, Contacts.CONTACT_STATUS_ICON, },
			null,
			null,
			null);
		//cursor.moveToFirst();
		columnNames = cursor.getColumnNames();
	}

	public Iterator<Person> iterator() {
		return this;
	}

	public boolean hasNext() {
		return !cursor.isClosed() && cursor.getCount() > 0 && !cursor.isLast();
	}

	public Person next() {
		Person person = null;
		if (cursor.moveToNext()) {
			person = new Person(context, cursor, columnNames);
		}
		if (cursor.isLast()) {
			cursor.close(); // TODO: what if iterator doesnt run fully?
		}
		return person;
	}

	public void remove() {
		throw new UnsupportedOperationException();
	}
}
