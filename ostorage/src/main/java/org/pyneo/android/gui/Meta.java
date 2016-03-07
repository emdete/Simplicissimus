package org.pyneo.android.gui;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Meta extends StoreObject {
	private static final String TAG = "org.pyneo.sample";
	private static final boolean DEBUG = true;
	String name;
	String description;
	Date timestamp = new Date();
	List<Item> items;

	public Meta() {
	}

	public Meta(String name, String description) {
		this.name = name;
		this.description = description;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public List<Item> getItems(SQLiteDatabase db) throws Exception {
		if (items == null) {
			items = new ArrayList();
			for (StoreObject item: query(db, Item.class).where("metaId").identity(id).fetchAll()) {
				items.add((Item)item);
			}
		}
		return items;
	}

	public int getItemCount(SQLiteDatabase db) throws Exception {
		return getItems(db).size();
	}

	public String toString() {
		return "Meta " +
			"name=" + name +
			", description=" + description +
			", timestamp=" + timestamp;
	}
}
