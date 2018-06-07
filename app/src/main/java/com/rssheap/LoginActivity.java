package com.rssheap;

import com.crashlytics.android.Crashlytics;
import io.fabric.sdk.android.Fabric;
import twitter4j.auth.RequestToken;

import org.json.JSONException;
import org.json.JSONObject;
import android.accounts.AccountManager;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.*;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.AccountPicker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.rssheap.asynctasks.GetUserFromGoogleTask;
import com.rssheap.asynctasks.GetUserFromTwitterTask;
import com.rssheap.interfaces.IJSONTaskCompleted;
import com.rssheap.interfaces.IObjectTaskCompleted;
import com.rssheap.model.User;
import com.rssheap.utilities.AnalyticsTrackers;
import com.rssheap.utilities.JsonRequest;
import com.rssheap.utilities.Utilities;

public class LoginActivity extends BaseActivity {

    static final int REQUEST_CODE_PICK_ACCOUNT = 1000;

    private EditText mEmailView;
    private EditText mPasswordView;

    private CallbackManager mFacebookCallbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_login);

        if (isDeviceOnline()) {

            showLoadingDialog();
            executeCheckIfAppNeedsToUpdateTask(new IJSONTaskCompleted() {

                @Override
                public void onTaskCompleted(JSONObject latestVersion) {

                    boolean appNeedsUpdate = compareLatestToLocalVersion(latestVersion);
                    if(appNeedsUpdate) {
                        rediretToGooglePlayToUpdate();
                        finish();
                        return;
                    }

                    if (isUserAlreadyLoggedIn()) {

                        executeRefreshUserInfoFromServerTask(new IJSONTaskCompleted() {

                            @Override
                            public void onTaskCompleted(JSONObject userJson) {
                                if (userJson != null) {
                                    User user = Utilities.getUserFromSharedPreferences(getApplicationContext());
                                    redirectFromLoginScreen(user);
                                    hideLoadingDialog();
                                } else {
                                    Toast.makeText(getApplicationContext(), "Unknow username or password", Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    } else {
                        hideLoadingDialog();
                    }
                }
            });
        }

        mFacebookCallbackManager = CallbackManager.Factory.create();

        mEmailView = (EditText) findViewById(R.id.email);

        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.containsKey("extra_accountname")) {
            String email = extras.getString("extra_accountname");
            String type = extras.getString("extra_accounttype");
            executeGetUserFromGoogleTask(email, type);
        }

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView
                .setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView textView, int id,
                                                  KeyEvent keyEvent) {
                        if (id == R.id.login || id == EditorInfo.IME_NULL) {
                            attemptLogin();
                            return true;
                        }
                        return false;
                    }
                });

        findViewById(R.id.login_form);
        findViewById(R.id.login_status);

        findViewById(R.id.login_with_twitter).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (isDeviceOnline()) {
                            executeGetUserFromTwitterTask("token", null);
                        }
                    }
                });

        findViewById(R.id.sign_in_button).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (isDeviceOnline()) {
                            attemptLogin();
                        }
                    }
                });

        findViewById(R.id.login_with_google).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        if (isDeviceOnline()) {
                            pickUserAccount();
                        }
                    }
                });

        findViewById(R.id.login_with_facebook).setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        if (isDeviceOnline()) {
                            openFacebookLoginScreen();
                        }
                    }
                });

        Tracker t = AnalyticsTrackers.getInstance().get();
        t.setScreenName("Login");
        t.send(new HitBuilders.ScreenViewBuilder().build());
    }

    private boolean isUserAlreadyLoggedIn() {
        return !TextUtils.isEmpty(Utilities.getUserGUID(LoginActivity.this));
    }

    private void redirectFromLoginScreen(User user)
    {
        if(user.TagIds.size() > 0) {
            Intent intent = new Intent(LoginActivity.this, ArticleListActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        } else {
            Intent intent = new Intent(LoginActivity.this, TagsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
    }

    private boolean compareLatestToLocalVersion(JSONObject jsonObject) {

        boolean needsUpdate = false;
        try {
            String latestVersion = jsonObject.getString("version");

            String currentVersion = Utilities.getVersionFromSharedPreferences(getApplicationContext());
            if(currentVersion.equals("")) { //clean install, just set the version value
                Utilities.setVersionToSharedPreferences(getApplicationContext(), latestVersion);
            } else if (!latestVersion.equals(currentVersion)) {
                needsUpdate = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return needsUpdate;
    }

    private void rediretToGooglePlayToUpdate() {
        final String appPackageName = getPackageName();
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
        }
    }

    private void openFacebookLoginScreen() {

        showLoadingDialog();

        LoginManager.getInstance().registerCallback(mFacebookCallbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {

                        GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(),
                                new GraphRequest.GraphJSONObjectCallback() {
                                    @Override
                                    public void onCompleted(JSONObject facebookUser, GraphResponse response) {

                                        executeGetUserFromFacebookTask(facebookUser, new IJSONTaskCompleted() {
                                            @Override
                                            public void onTaskCompleted(JSONObject userIdAndGuid) {
                                                if(userIdAndGuid != null) {
                                                    Utilities.setUserGUID(getApplicationContext(), userIdAndGuid);

                                                    executeRefreshUserInfoFromServerTask(new IJSONTaskCompleted() {
                                                        @Override
                                                        public void onTaskCompleted(JSONObject userJson) {
                                                            if(userJson != null) {
                                                                User user = Utilities.setUserToSharedPreferences(getApplicationContext(), userJson);
                                                                redirectFromLoginScreen(user);
                                                                hideLoadingDialog();
                                                            } else {
                                                                Toast.makeText(getApplicationContext(), "There was an error in retrieving the user", Toast.LENGTH_LONG).show();
                                                            }
                                                        }
                                                    });
                                                }
                                            }
                                        });
                                    }
                                });
                        Bundle parameters = new Bundle();
                        parameters.putString("fields", "id,name,email,first_name,last_name");
                        request.setParameters(parameters);
                        request.executeAsync();
                    }

                    @Override
                    public void onCancel() {
                        hideLoadingDialog();
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        hideLoadingDialog();
                        Toast.makeText(LoginActivity.this, exception.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                    }
                });

        LoginManager.getInstance().logOut();
        LoginManager.getInstance().logInWithReadPermissions(this, null);
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		//getMenuInflater().inflate(R.menu.activity_login, menu);
		return true;
	}

	public void attemptLogin() {
		// Reset errors.
		mEmailView.setError(null);
		mPasswordView.setError(null);

		// Store values at the time of the login attempt.
		String email = mEmailView.getText().toString();
		String password = mPasswordView.getText().toString();

		boolean cancel = false;

		// Check for a valid password.
		if (TextUtils.isEmpty(password)) {
			mPasswordView.setError(getString(R.string.error_field_required));
			cancel = true;
		}

		// Check for a valid email address.
		if (TextUtils.isEmpty(email)) {
			mEmailView.setError(getString(R.string.error_field_required));
			cancel = true;
		} else if (!email.contains("@")) {
			mEmailView.setError(getString(R.string.error_invalid_email));
			cancel = true;
		}

		if (!cancel) {
			// perform the user login attempt.
            showLoadingDialog();
            executeGetUserFromUserNameAndPasswordTask(email, password, new IJSONTaskCompleted() {
                @Override
                public void onTaskCompleted(JSONObject userIdAndGuidJson) {

                    if(userIdAndGuidJson != null) {

                        Utilities.setUserGUID(getApplicationContext(), userIdAndGuidJson);
                        executeRefreshUserInfoFromServerTask(new IJSONTaskCompleted() {
                            @Override
                            public void onTaskCompleted(JSONObject userJson) {
                                if(userJson != null) {
                                    User user = Utilities.setUserToSharedPreferences(getApplicationContext(), userJson);
                                    redirectFromLoginScreen(user);
                                    hideLoadingDialog();
                                }
                            }
                        });

                    } else {
                        hideLoadingDialog();
                        Toast.makeText(getApplicationContext(), "Unknown username or password", Toast.LENGTH_LONG).show();
                    }
                }
            });
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		Uri uri = getIntent().getData();
        if(uri == null) return;

        if (uri.toString().startsWith("oauth://com.rssheap.Login")) {
            String verifier = uri.getQueryParameter("oauth_verifier");
            executeGetUserFromTwitterTask("user", verifier);
        } else {
            executeGetUserFromTwitterTask("user", null);
        }
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == REQUEST_CODE_PICK_ACCOUNT) {
			// Receiving a result from the AccountPicker
			if (resultCode == RESULT_OK) {
				String email = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                String type = data.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE);

				if (isGooglePlayAvailable()) {
                    executeGetUserFromGoogleTask(email, type);
				}
			} else if (resultCode == RESULT_CANCELED) {
				Toast.makeText(this, "You must pick an account to proceed",
						Toast.LENGTH_SHORT).show();
			}
		} else if (requestCode == 2000) {
            String email = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
            String type = data.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE);
            executeGetUserFromGoogleTask(email, type);
		}

		super.onActivityResult(requestCode, resultCode, data);
        mFacebookCallbackManager.onActivityResult(requestCode, resultCode, data);
	}

    private boolean isGooglePlayAvailable() {

        int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(this);
        if(result != ConnectionResult.SUCCESS) {
            if(googleAPI.isUserResolvableError(result)) {
                googleAPI.getErrorDialog(this, result, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            }

            return false;
        }

        return true;
    }

	private void pickUserAccount() {
		String[] accountTypes = new String[] { "com.google" };
		Intent intent = AccountPicker.newChooseAccountIntent(null, null, accountTypes, false, null, null, null, null);
		startActivityForResult(intent, REQUEST_CODE_PICK_ACCOUNT);
	}

    private void executeGetUserFromUserNameAndPasswordTask(final String email, final String password, final IJSONTaskCompleted callback) {

        new AsyncTask<Void, Void, JSONObject>() {
            @Override
            protected JSONObject doInBackground(Void... params) {
                JSONObject json = new JSONObject();
                try {
                    json.put("username", email);
                    json.put("password", password);
                    json.put("provider", "internal");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return new JsonRequest(LoginActivity.this, "/api/GetUser").Post(json);
            }

            @Override
            protected void onPostExecute(JSONObject jsonObject) {
                if(callback != null) callback.onTaskCompleted(jsonObject);
            }
        }.execute();
    }

    private void executeRefreshUserInfoFromServerTask(final IJSONTaskCompleted callback) {

        new AsyncTask<Void, Void, JSONObject>() {
            @Override
            protected JSONObject doInBackground(Void... params) {
                return new JsonRequest(LoginActivity.this, "/api/RefreshUserInfo").Post(null);
            }

            @Override
            protected void onPostExecute(JSONObject jsonObject) {
                if(callback != null) callback.onTaskCompleted(jsonObject);
            }
        }.execute();
    }

    private void executeCheckIfAppNeedsToUpdateTask(final IJSONTaskCompleted callback) {
        new AsyncTask<Void, Void, JSONObject>() {

            @Override
            protected JSONObject doInBackground(Void... params) {
                return new JsonRequest(getApplicationContext(), "/api/GetVersion").Post(null);
            }

            @Override
            protected void onPostExecute(JSONObject jsonObject) {
                if (callback != null) callback.onTaskCompleted(jsonObject);
            }
        }.execute();
    }

    private void executeGetUserFromFacebookTask(final JSONObject facebookUser, final IJSONTaskCompleted callback) {

        //GetUserTask
        new AsyncTask<Void, Void, JSONObject>() {

            @Override
            protected JSONObject doInBackground(Void... params) {
                JSONObject json = new JSONObject();
                try {
                    json.put("id", facebookUser.getString("id"));
                    json.put("firstname", facebookUser.getString("first_name"));
                    json.put("email", facebookUser.getString("email"));
                    json.put("name", facebookUser.getString("name"));
                    json.put("lastname", facebookUser.getString("last_name"));
                    json.put("provider", "facebook");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                return new JsonRequest(getApplicationContext(), "/api/GetUser").Post(json);
            }

            @Override
            protected void onPostExecute(JSONObject jsonObject) {
                if(callback != null) callback.onTaskCompleted(jsonObject);
            }
        }.execute();
    }

    private void executeGetUserFromTwitterTask(final String action, String verifier) {

        new GetUserFromTwitterTask(LoginActivity.this, action, verifier, new IObjectTaskCompleted() {

            @Override
            public void onTaskCompleted(final Object result) {

                switch (action)
                {
                    case "user":
                        final JSONObject twitterUserJson = (JSONObject) result;
                        if(twitterUserJson != null) {

                            //GetUserTask
                            new AsyncTask<Void, Void, JSONObject>() {

                                @Override
                                protected JSONObject doInBackground(Void... params) {
                                    try {
                                        twitterUserJson.put("provider", "twitter");
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                    return new JsonRequest(getApplicationContext(), "/api/GetUser").Post(twitterUserJson);
                                }

                                @Override
                                protected void onPostExecute(JSONObject idAndGuidJson) {

                                    if(idAndGuidJson != null) {

                                        Utilities.setUserGUID(getApplicationContext(), idAndGuidJson);

                                        executeRefreshUserInfoFromServerTask(new IJSONTaskCompleted() {
                                            @Override
                                            public void onTaskCompleted(JSONObject userJson) {
                                                User user = Utilities.setUserToSharedPreferences(getApplicationContext(), userJson);
                                                redirectFromLoginScreen(user);
                                                hideLoadingDialog();
                                            }
                                        });
                                    }
                                }
                            }.execute();
                        }
                        break;
                    case "token":
                        RequestToken requestToken = (RequestToken) result;

                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(requestToken.getAuthenticationURL()));
                        startActivity(intent);
                        break;
                }
            }
        }).execute();
    }

    private void executeGetUserFromGoogleTask(final String email, final String type) {

        showLoadingDialog();

        new GetUserFromGoogleTask(LoginActivity.this, email, type, new IJSONTaskCompleted() {
            @Override
            public void onTaskCompleted(final JSONObject profile) {
                if(profile != null) {

                    //GetUserTask
                    new AsyncTask<Void, Void, JSONObject>() {

                        @Override
                        protected JSONObject doInBackground(Void... params) {
                            try {
                                profile.put("provider", "google");
                                profile.put("email", email);

                                return new JsonRequest(LoginActivity.this, "/api/GetUser").Post(profile);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            return null;
                        }

                        @Override
                        protected void onPostExecute(JSONObject idAndGuidJson) {
                            if(idAndGuidJson != null) {

                                Utilities.setUserGUID(getApplicationContext(), idAndGuidJson);

                                executeRefreshUserInfoFromServerTask(new IJSONTaskCompleted() {
                                    @Override
                                    public void onTaskCompleted(JSONObject userJson) {
                                        User user = Utilities.setUserToSharedPreferences(getApplicationContext(), userJson);
                                        redirectFromLoginScreen(user);
                                        hideLoadingDialog();
                                    }
                                });
                            }
                        }
                    }.execute();
                }
            }
        }).execute();
    }
}
