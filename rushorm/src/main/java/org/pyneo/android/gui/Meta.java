package org.pyneo.android.gui;

import java.util.ArrayList;
import android.content.ClipData;
import android.util.Log;
import java.util.List;
import java.util.Date;
import co.uk.rushorm.core.RushObject;
import co.uk.rushorm.core.annotations.RushIgnore;
import co.uk.rushorm.core.annotations.RushList;

public class Meta extends RushObject {
	private static final String TAG = "org.pyneo.sample";
	private static final boolean DEBUG = true;
	// @RushUnique @RushNotNull
	String name;
	String description;
	Date timestamp = new Date();
	@RushList(classType = Item.class)
	List<Item> items = new ArrayList<>();

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

	public void add(Item item) {
		getItems().add(item);
	}

	public int getItemCount() {
		return getItems().size();
	}
}
