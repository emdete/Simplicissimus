package de.emdete.android.gui;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import de.emdete.thinstore.StoreObject;

public class Meta extends StoreObject {
	private static final String TAG = "de.emdete.sample";
	private static final boolean DEBUG = true;
	protected String name;
	protected String description;
	protected Date timestamp = new Date();
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
			for (StoreObject item: query(db, Item.class).where("metaId").equal(id).fetchAll()) {
				items.add((Item)item);
			}
		}
		return items;
	}

	public int getItemCount(SQLiteDatabase db) throws Exception {
		return getItems(db).size();
	}

	public Item add(SQLiteDatabase db, Item item) throws Exception {
		item.metaId = this.id;
		return (Item)add(db, getItems(db), item);
	}

	public void delete(SQLiteDatabase db) throws Exception {
		for (Item item: getItems(db)) {
			item.delete(db);
		}
		super.delete(db);
	}

	public String toString() {
		return "Meta " +
			"name=" + name +
			", description=" + description +
			", timestamp=" + timestamp;
	}
}
