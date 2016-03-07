package org.pyneo.android.gui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class Sample extends Activity {
	static final String TAG = "org.pyneo.sample";
	static boolean DEBUG = true;
	// static { DEBUG = Log.isLoggable(TAG, Log.DEBUG); }
    static class DatabaseHelper extends SQLiteOpenHelper {
        DatabaseHelper(Context ctx) {
            super(ctx, "sample", null, 1);
        }

        @Override public void onCreate(SQLiteDatabase db) {
			Log.d(TAG, "create=" + StoreObject.create(db, Meta.class));
			Log.d(TAG, "create=" + StoreObject.create(db, Item.class));
        }

        @Override public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }
    }
	DatabaseHelper dbHelper;

	Context context;

	@Override public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (DEBUG) Log.d(TAG, "onCreate");
		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
			@Override public void uncaughtException(Thread thread, Throwable e) {
				Log.e(TAG, "error e=" + e, e);
				finish();
			}
		});
		setContentView(R.layout.main);
		context = getBaseContext();
        dbHelper = new DatabaseHelper(context);
		Button button = (Button)findViewById(R.id.button);
		button.setOnClickListener(new View.OnClickListener() {
			@Override public void onClick(View view) {
				if (DEBUG) Log.d(TAG, "onClick");
				doTest(context);
			}
		});
	}

	public void doTest(Context context) {
		Button button = (Button)findViewById(R.id.button);
		try {
			SQLiteDatabase db = dbHelper.getWritableDatabase();
			for (int i=0;i<10;i++) {
				if (DEBUG) Log.d(TAG, "new item=" +
					new Item("Item " + i, "This is another famous item").insert(db));
			}
			for (StoreObject item: StoreObject.select(db, Item.class)) {
				((Item)item).description = "Blullulul";
				item.update(db);
				//item.delete(db);
				if (DEBUG) Log.d(TAG, "updated item=" + item);
			}
			for (StoreObject item: StoreObject.query(db, Item.class)
				.where("name").identity("Item 0")
				.and("description").like("B%")
				.order_by("date")
				.fetchAll()) {
				if (DEBUG) Log.d(TAG, "queried item=" + item);
			}
			button.setText("Started");
		}
		catch (Exception e) {
			Log.e(TAG, "exception", e);
			button.setText(e.toString());
		}
	}
}
