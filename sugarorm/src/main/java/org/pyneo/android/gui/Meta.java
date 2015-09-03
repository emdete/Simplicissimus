package org.pyneo.android.gui;

import android.database.Cursor;

import com.orm.SugarRecord;
import com.orm.dsl.Ignore;

import java.util.ArrayList;
import java.util.List;

public class Meta extends SugarRecord<Meta> {
	String name;
	String description;
	@Ignore
	List<Item> items; // = new ArrayList<>();

	void _inflate(Cursor c) {
		//super.inflate(c);
		if (getId() == null) items = new ArrayList<>(); else
		items = Item.find(Item.class, "meta = ?", new String[]{Long.toString(getId())});
	}

	public Meta() {
	}

	public Meta(String name, String description) {
		this.name = name;
		this.description = description;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<Item> getItems() {
		return items;
	}

	public void setItems(List<Item> items) {
		this.items = items;
	}

	public void save() {
		super.save();
		for (Item item: items) {
			item.meta = this;
			item.save();
		}
	}

	public void add(Item item) {
		if (items == null) _inflate(null);
		item.meta = this;
		items.add(item);
	}

	public int getItemCount() {
		if (items == null) _inflate(null);
		return items.size();
	}
}
