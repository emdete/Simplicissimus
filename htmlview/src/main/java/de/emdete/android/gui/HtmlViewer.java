package de.emdete.android.gui;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebChromeClient;
import android.widget.Button;
import android.util.AttributeSet;

public class HtmlViewer extends WebView {
	public HtmlViewer(Context context, AttributeSet a) {
		super(context, a);
		getSettings().setLoadsImagesAutomatically(true);
		getSettings().setJavaScriptEnabled(false);
		getSettings().setAllowContentAccess(false);
		setWebViewClient(new WebViewClient (){
			@Override public boolean shouldOverrideUrlLoading(WebView view, String url) {
				if (Sample.DEBUG) Log.d(Sample.TAG, "shouldOverrideUrlLoading url=" + url);
				view.loadUrl("file:///android_asset/michael.dietrich.jpg");
				return true;
			}
			@Override public WebResourceResponse shouldInterceptRequest (WebView view, WebResourceRequest request) {
				if (Sample.DEBUG) Log.d(Sample.TAG, "shouldInterceptRequest request=" + request);
				return null;//super.shouldInterceptRequest(view, request);
			}
			@Override public WebResourceResponse shouldInterceptRequest (WebView view, String url) {
				if (Sample.DEBUG) Log.d(Sample.TAG, "shouldInterceptRequest url=" + url);
				return null;//super.shouldInterceptRequest(view, url);
			}
			@Override public void onLoadResource (WebView view, String url) {
				if (Sample.DEBUG) Log.d(Sample.TAG, "onLoadResource url=" + url);
				super.onLoadResource(view, url);
			}
		});
		setWebChromeClient(new WebChromeClient() {
		});
	}

	boolean back() {
		if (canGoBack()) {
			goBack();
			return true;
		}
		return false;
	}
}
