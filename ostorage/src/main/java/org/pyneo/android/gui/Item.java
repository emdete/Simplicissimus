package org.pyneo.android.gui;

import java.util.Date;

public class Item extends StoreObject {
	String name;
	protected String description;
	public long metaId = -1;
	public Date timestamp;

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

	public String toString() {
		return "Item " +
			"name=" + name +
			", description=" + description +
			", metaId=" + metaId +
			", timestamp=" + timestamp;
	}
}
