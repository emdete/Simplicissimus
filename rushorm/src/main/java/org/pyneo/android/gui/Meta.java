package org.pyneo.android.gui;

import android.content.ClipData;
import android.util.Log;
import co.uk.rushorm.core.annotations.RushIgnore;
import co.uk.rushorm.core.annotations.RushList;
import co.uk.rushorm.core.RushObject;
import co.uk.rushorm.core.RushSearch;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Meta extends RushObject {
	private static final String TAG = "org.pyneo.sample";
	private static final boolean DEBUG = true;
	// @RushUnique @RushNotNull
	String name;
	String description;
	Date timestamp = new Date();
	//@RushList(classType = Item.class) List<Item> items = new ArrayList<>();
	@RushIgnore List<Item> items;

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

	public void save() {
		super.save();
		if (items != null) {
			for (Item item: items) {
				item.metaId = getId();
				item.save();
			}
		}
	}

	public List<Item> getItems() {
		if (items == null) {
			items = new RushSearch().whereEqual("metaId", getId()).find(Item.class);
		}
		return items;
	}

	public void add(Item item) {
		getItems().add(item);
	}

	public int getItemCount() {
		return getItems().size();
	}
}
