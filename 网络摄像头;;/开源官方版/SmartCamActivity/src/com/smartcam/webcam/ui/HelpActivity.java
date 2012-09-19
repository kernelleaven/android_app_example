/*
 * Copyright (C) 2010 Ionut Dediu <deionut@yahoo.com>
 *
 * Licensed under the GNU General Public License Version 2
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.smartcam.webcam.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import com.smartcam.webcam.R;

public final class HelpActivity extends Activity
{
	private static final String TAG = HelpActivity.class.getSimpleName();

	public static final String DEFAULT_PAGE = "index.html";
	public static final String WHATS_NEW_PAGE = "whatsnew.html";
	private static final String BASE_URL = "file:///android_asset/html/";

	private WebView webView;
	private Button backButton;

	private final Button.OnClickListener backListener = 
		new Button.OnClickListener()
		{
			public void onClick(View view)
			{
				webView.goBack();
			}
		};

	private final Button.OnClickListener doneListener = 
		new Button.OnClickListener()
		{
			public void onClick(View view)
			{
				finish();
			}
		};

	@Override
	protected void onCreate(Bundle icicle)
	{
		super.onCreate(icicle);
		setContentView(R.layout.help);

		webView = (WebView)findViewById(R.id.help_contents);
		webView.setWebViewClient(new HelpClient());

		Intent intent = getIntent();
		if(icicle != null)
		{
			webView.restoreState(icicle);
		}
		else if(intent != null)
		{
			webView.loadUrl(BASE_URL + DEFAULT_PAGE);
		}

		backButton = (Button)findViewById(R.id.back_button);
		backButton.setOnClickListener(backListener);
		Button doneButton = (Button)findViewById(R.id.done_button);
		doneButton.setOnClickListener(doneListener);
	}

	@Override
	protected void onSaveInstanceState(Bundle state)
	{
		webView.saveState(state);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if(keyCode == KeyEvent.KEYCODE_BACK)
		{
			if(webView.canGoBack())
			{
				webView.goBack();
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}

	private final class HelpClient extends WebViewClient
	{
		@Override
		public void onPageFinished(WebView view, String url)
		{
			setTitle(view.getTitle());
			backButton.setEnabled(view.canGoBack());
		}
	}
}
