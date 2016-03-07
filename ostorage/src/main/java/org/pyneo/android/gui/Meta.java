package org.pyneo.android.gui;

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

	public List<Item> getItems() {
		if (items == null) {
			items = null; //new Search().whereEqual("metaId", getId()).find(Item.class);
		}
		return items;
	}

	public int getItemCount() {
		return getItems().size();
	}
}
