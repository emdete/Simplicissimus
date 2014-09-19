package org.pyneo.android.gui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.EncodeHintType;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.google.zxing.integration.android.IntentResult;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.WriterException;
import com.google.zxing.integration.android.IntentIntegrator;
import java.util.Hashtable;

public class Sample extends Activity {
	static final String TAG = Sample.class.getName();
	static boolean DEBUG = true;
	// static { DEBUG = Log.isLoggable("org.pyneo.android", Log.DEBUG); }
	static final QRCodeWriter QR_CODE_WRITER = new QRCodeWriter();
	static final int QR_CODE_SIZE = 1000;
	static final private String protocol = "xmpp";

	Context context;
	Button share;
	Button scan;
	ImageView imageView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (DEBUG) Log.d(TAG, "onCreate");
		setContentView(R.layout.main);
		context = getBaseContext();
		share = (Button)findViewById(R.id.share);
		imageView = (ImageView)findViewById(R.id.image);
		scan = (Button)findViewById(R.id.scan);
		share.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (DEBUG) Log.d(TAG, "onClick");
				doShare(context);
			}
		});
		scan.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (DEBUG) Log.d(TAG, "onClick");
				doScan(context);
			}
		});
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (DEBUG) Log.d(TAG, "onStart");
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		if (DEBUG) Log.d(TAG, "onRestart");
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (DEBUG) Log.d(TAG, "onResume");
		Intent intent = getIntent();
		if (DEBUG) Log.d(TAG, "onResume: intent=" + intent);
		if (intent != null) {
			String action = intent.getAction();
			if (DEBUG) Log.d(TAG, "onResume: action=" + action);
			if (Intent.ACTION_VIEW.equals(action)) {
				Uri uri = getIntent().getData();
				if (DEBUG) Log.d(TAG, "onResume: uri=" + uri);
				if (protocol.equals(uri.getScheme())) {
					String id = uri.toString().substring(protocol.length()+1);
					try {
						;
					}
					catch (Exception e) {
						Log.e(TAG, "onResume: e=" + e, e);
					}
				}
			}
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (DEBUG) Log.d(TAG, "onPause");
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (DEBUG) Log.d(TAG, "onStop");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (DEBUG) Log.d(TAG, "onDestroy");
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		switch (requestCode & 0xFFFF) {
			case IntentIntegrator.REQUEST_CODE: {
				IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
				if (scanResult != null && scanResult.getFormatName() != null) {
					String data = scanResult.getContents();
					Log.d(TAG, "data: " + data);
					scan.setText(data);
				}
				break;
			}
			default: {
				super.onActivityResult(requestCode, resultCode, intent);
				break;
			}
		}
	}

	static Bitmap getQRCodeBitmap(final String input, final int size) {
		try {
			final Hashtable<EncodeHintType, Object> hints = new Hashtable<EncodeHintType, Object>();
			hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
			final BitMatrix result = QR_CODE_WRITER.encode(input, BarcodeFormat.QR_CODE, size, size, hints);
			final int width = result.getWidth();
			final int height = result.getHeight();
			final int[] pixels = new int[width * height];
			for (int y = 0; y < height; y++) {
				final int offset = y * width;
				for (int x = 0; x < width; x++) {
					pixels[offset + x] = result.get(x, y) ? Color.BLACK : Color.TRANSPARENT;
				}
			}
			final Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
			bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
			return bitmap;
		} catch (final WriterException e) {
			Log.e(TAG, "QrCodeUtils", e);
			return null;
		}
	}

	public void doScan(Context context) {
		new IntentIntegrator(this).initiateScan();
	}

	public void doShare(Context context) {
		String data = protocol + "://mdt@emdete.de";
		imageView.setImageBitmap(getQRCodeBitmap(data, QR_CODE_SIZE));
		imageView.setBackgroundColor(Color.rgb(100, 100, 50));
		share.setText(data);
	}
}
