package org.pyneo.android.gui;

import com.orm.SugarRecord;

public class Item extends SugarRecord<Item> {
	String name;
	String description;
	Meta meta;

	public Item() {
	}

	public Item(String name, String description) {
		this.name = name;
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
