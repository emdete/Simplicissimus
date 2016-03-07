package org.pyneo.android.gui;

import java.util.Date;

public class Item extends StoreObject {
	String name;
	String description;
	String metaId;
	Date timestamp;

	public Item() {
		timestamp = new Date();
	}

	public Item(String name, String description) {
		this.timestamp = new Date();
		this.name = name;
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public String getName() {
		return name;
	}

	public Meta getMeta() {
		return null; //new Search().whereId(metaId).findSingle(Meta.class);
	}
}
