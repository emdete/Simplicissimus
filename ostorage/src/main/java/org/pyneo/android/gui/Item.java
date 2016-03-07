package org.pyneo.android.gui;

public class Item extends StoreObject {
	String name;
	String description;
	String metaId;

	public Item() {
	}

	public Item(String name, String description) {
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
