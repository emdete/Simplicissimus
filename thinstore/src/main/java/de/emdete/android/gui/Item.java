package de.emdete.android.gui;

import java.util.Date;
import de.emdete.thinstore.Col;
import de.emdete.thinstore.StoreObject;

public class Item extends StoreObject {
	protected String name;
	protected String description;
	protected long metaId = -1;
	protected Date timestamp;

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
