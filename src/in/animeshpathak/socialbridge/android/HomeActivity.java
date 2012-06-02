/*
 * Copyright 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package in.animeshpathak.socialbridge.android;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.google.api.services.plus.Plus;
import com.google.api.services.plus.model.Activity;

import in.animeshpathak.socialbridge.android.R;
import in.animeshpathak.socialbridge.android.auth.AuthUtils;

import java.io.IOException;
import java.util.List;

/**
 * Shows a list of Google+ activities of the user.
 * 
 * @author Animesh Pathak
 */
public class HomeActivity extends android.app.Activity {
	public static final String TAG = HomeActivity.class.getName();
	private ListView mListView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final Intent intent = getIntent();
		if (intent == null || !intent.hasExtra("token")) {
			Log.d(TAG, "no token found. Getting a fresh one.");
			AuthUtils.refreshAuthToken(this);
			return;
		}

		setContentView(R.layout.activity_list);
		mListView = (ListView) findViewById(R.id.activityList);

		AsyncTask<String, Void, List<Activity>> task = new AsyncTask<String, Void, List<Activity>>() {

			private ProgressDialog p;

			@Override
			protected void onPreExecute() {
				p = ProgressDialog.show(HomeActivity.this, "",
						"Fetching new posts. Please wait...", true);

			}

			@Override
			protected List<Activity> doInBackground(String... params) {
				try {
					Plus plus = new PlusWrap(HomeActivity.this).get();
					return plus.activities().list("me", "public").execute()
							.getItems();
				} catch (IOException e) {
					Log.e(TAG, "Unable to list activities", e);
				}
				return null;
			}

			@Override
			protected void onPostExecute(List<Activity> feed) {
				p.cancel();
				if (feed != null) {
					mListView.setAdapter(new ActivityArrayAdapter(
							getApplicationContext(), feed));
					mListView.setOnItemClickListener(new OnItemClickListener() {
						public void onItemClick(AdapterView<?> parent,
								View view, int position, long id) {
							// When clicked, show a toast with the TextView text
							String title = ((TextView) view
									.findViewById(R.id.title)).getText().toString();

							String url = ((TextView) view
									.findViewById(R.id.url)).getText().toString();
							
							String postingText = title + " ... " + url;
							
							Toast.makeText(
									getApplicationContext(),postingText, Toast.LENGTH_SHORT)
									.show();
							
							Intent i = new Intent(Intent.ACTION_SEND);
							i.setType("text/plain"); // use from live device
							// TODO pull these strings into a constants file
							i.putExtra(Intent.EXTRA_TEXT, postingText);
							startActivity(Intent.createChooser(i,
									"Share link using..."));

							
						}
					});
					

					
				}
			}
		};
		task.execute("me");
	}
}
