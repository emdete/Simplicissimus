package org.pyneo.android.gui;

import com.orm.SugarRecord;
import com.orm.dsl.Ignore;
import com.orm.dsl.NotNull;
import com.orm.dsl.Unique;
import java.util.ArrayList;
import android.util.Log;
import java.util.List;

public class Meta extends SugarRecord {
	private static final String TAG = "org.pyneo.sample";
	private static final boolean DEBUG = true;
	@Unique @NotNull String name;
	String description;
	@Ignore List<Item> items; // = new ArrayList<>();

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
		if (DEBUG) Log.d(TAG, "getItems:");
		if (items == null) {
			if (DEBUG) Log.d(TAG, "getItems: is null");
			if (getId() == null) {
				items = new ArrayList<>();
			}
			else {
				if (DEBUG) Log.d(TAG, "getItems: id is not null");
				items = Item.find(Item.class, "meta = ?", new String[]{Long.toString(getId())});
			}
		}
		return items;
	}

	public long save() {
		long id = super.save();
		for (Item item: items) {
			item.meta = this;
			item.save();
		}
		return id;
	}

	public void add(Item item) {
		getItems().add(item);
	}

	public int getItemCount() {
		return getItems().size();
	}
}
