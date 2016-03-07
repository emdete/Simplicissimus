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
			Item item = new Item();
			item.insert(db);
		/*
		//Log.d(TAG, "in db count=" + new Search().find(Meta.class).size());
		for (int j=0;j<1;j++) {
			Meta m = new Meta("initial master", "this is a sample master");
			//Log.d(TAG, "before save id=" + m.getId());
			for (int i=0;i<9;i++) {
				//m.add(new Item("item" + i, "this is the item no " + i));
			}
			//m.save();
			String id = ""; //m.getId();
			//Log.d(TAG, "after save id=" + m.getId());
			Item k = new Item("item" + -1, "this is the item no " + -1);
			//m.add(k);
			//k.save();
			m = null; // new Search().whereId(id).findSingle(Meta.class);
			Log.d(TAG, "after load count=" + m.getItemCount() + ", timestamp=" + m.timestamp.toGMTString() + ", meta=" + m);
			Log.d(TAG, "meta=" + k.getMeta());
			//for (Item item: m.items) { Log.d(TAG, "after load meta=" + item.meta); }
		}
		// Meta.saveInTx();
		*/
			button.setText("Started");
		}
		catch (Exception e) {
			Log.e(TAG, "exception", e);
			button.setText(e.toString());
		}
	}
}
