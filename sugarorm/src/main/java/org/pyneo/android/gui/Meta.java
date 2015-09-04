package org.pyneo.android.gui;

import com.orm.SugarRecord;
import com.orm.dsl.Ignore;
import com.orm.dsl.NotNull;
import com.orm.dsl.Unique;

import java.util.ArrayList;
import java.util.List;

public class Meta extends SugarRecord {
	@Unique @NotNull
	String name;
	String description;
	@Ignore
	List<Item> items; // = new ArrayList<>();

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
		if (items == null)
			if (getId() == null)
				items = new ArrayList<>();
			else
				items = Item.find(Item.class, "meta = ?", new String[]{Long.toString(getId())});
		return items;
	}

	void _save() {
		super.save();
		for (Item item: items) {
			item.meta = this;
			item.save();
		}
	}

	public void add(Item item) {
		item.meta = this;
		getItems().add(item);
	}

	public int getItemCount() {
		return getItems().size();
	}
}
